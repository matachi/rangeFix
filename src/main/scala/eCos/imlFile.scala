package ca.uwaterloo.gsd.rangeFix
import scala.collection._

class ReqSource(n:Node, val reqIndex:Int) extends Source(n.id) {
  override def getSource():String = "Req" + reqIndex.toString + "@" + n.id + ":" + n.reqs(reqIndex)
}
class ActiveIfSource(id:String) extends Source(id) {
  override def getSource():String = "ActiveIfCondition@" + id
}
class LegalValueSource(id:String) extends Source(id) {
  override def getSource():String = "LegalValues@" + id
}
case class ReqConstraint(constraint:Expression, n:Node, index:Int) 
     extends ConstraintWithSource(constraint, new ReqSource(n, index))

case class ActiveIfCondition(constraint:Expression, id:String) 
     extends ConstraintWithSource(constraint, new ActiveIfSource(id))


case class LegalValueConstraint(constraint:Expression, id:String) 
     extends ConstraintWithSource(constraint, new LegalValueSource(id))

case class ImlConstraints (
  reqConstraints:Iterable[ReqConstraint], 
  legalValueConstraints:Map[String, Expression],
  activeConditions:Map[String, Expression],
  semanticVars:Map[String, Expression],
  defaults:Map[String, Expression],
  types:Map[String, SingleType]) {
  lazy val allConstraints:Iterable[ConstraintWithSource] = reqConstraints ++ (for ((id, c) <- legalValueConstraints) yield LegalValueConstraint(c, id))
}

object NodeHelper {
  val boolVarSuffix = "_bool"
  val dataVarSuffix = "_data"
  
  def toBoolVar(id:String) = id + boolVarSuffix
  def toDataVar(id:String) = id + dataVarSuffix

  def reverse[K,V](m:Iterable[(K,V)]):Map[V,List[K]] = {
    val result = mutable.Map[V, List[K]]()
    for((k,v) <- m) {
      result.get(v) match {
	case Some(collection) => result.put(v, k::collection)
	case None => result.put(v, List(k))
      }
    }
    result.toMap
  } 
  
  def reverseBack[K,V](m:Iterable[(V, Iterable[K])]):Map[K,V] = {
    val result = for((v, list) <- m; k <- list ) yield (k, v)
    return result.toMap
  } 
  
  def typeCorrectNodes2Constraints(allNodes:Map[String, Node], 
				   types:Map[String, SingleType],
				   replaceSemanticVars:Boolean = false
				 ):ImlConstraints = {
    val cycleChecker = new CycleChecker()
    var newTypes:mutable.Map[String, SingleType] = mutable.Map[String, SingleType]() ++ types
    
    val parents:Map[String, String] = reverseBack(allNodes.values.map(n => (n.id, n.children.map(_.id))))
    val interfaceImpl:Map[String, Seq[String]] = 
      reverse(for (n <- allNodes.values; i <- n.implements) yield (n.id, i)) 
    
    def enabled(n:Node):Expression = cycleChecker.checkCycle(n.id + "#enabled") {
      if (n.flavor == Flavor.None || n.flavor == Flavor.Data || n.cdlType == CdlType.Package) BoolLiteral(true)
      else if (n.calculated.isDefined || n.cdlType == CdlType.Interface) ToBool(calculated(n))
      else {
	newTypes put (n.id + boolVarSuffix, BoolType)
	IdentifierRef(n.id + boolVarSuffix)
      }
    }
    
    def data(n:Node):Expression = cycleChecker.checkCycle(n.id + "#data") {
      if (n.flavor == Flavor.None) IntLiteral(1)
      else if (n.flavor == Flavor.Bool || n.cdlType == CdlType.Package) BoolLiteral(true)
      else if (n.calculated.isDefined || n.cdlType == CdlType.Interface) calculated(n)
      else {
	newTypes put (n.id + dataVarSuffix, types(n.id))
	IdentifierRef(n.id + dataVarSuffix)
      }
    }
    
    def calculated(n:Node):Expression = cycleChecker.checkCycle(n.id + "#calculated") {
      if (n.calculated.isDefined) {
	rewriteExpression(n.calculated.get)
      }
      else {
	assert (n.cdlType == CdlType.Interface)
	val implements = interfaceImpl.getOrElse(n.id, List())
	val t = types(n.id)
	if (t == BoolType)
	  implements.foldLeft[Expression](BoolLiteral(false))((c, id) => c | effective(allNodes(id)))
	else {
	  assert ( t == NumberType )
	  implements.foldLeft[Expression](IntLiteral(0))((c, id) => c + Conditional(effective(allNodes(id)), IntLiteral(1), IntLiteral(0)))
	}
      }
    }
    
    def active(n:Node):Expression = cycleChecker.checkCycle(n.id + "#active") {pActive(n) & eActive(n)}
    def pActive(n:Node):Expression = {
      parents.get(n.id) match {
	case Some(pid) => effective(allNodes(pid))
	case None => BoolLiteral(true)
      }	
    }
    def eActive(n:Node):Expression = {
      n.activeIfs.foldLeft[Expression](BoolLiteral(true))(_ & rewriteExpression(_))
    }

    def effective(n:Node):Expression = cycleChecker.checkCycle(n.id + "#effective") {
      active(n) & enabled(n)
    }

    def value(n:Node):Expression = cycleChecker.checkCycle(n.id + "#value") {
      Conditional(effective(n), data(n), TypeHelper.ZeroToType(types(n.id)))
    }

    def rewriteExpression(e:Expression):Expression = {
      import org.kiama.rewriting.Rewriter._ 
      rewrite(everywherebu(rule[Any]{
	case GetData(id) => 
	  assert(allNodes.keySet.contains(id))
	  data(allNodes(id))
	case IsEnabled(n) => 
	  assert(allNodes.keySet.contains(n))
	  enabled(allNodes(n))
	case IsActive(n) => 
	  assert(allNodes.keySet.contains(n))
	  active(allNodes(n))
	case IdentifierRef(id) if replaceSemanticVars =>
	  value(allNodes(id))
      }))(e)
    }
    
    def legalValue2Constraint(node:Node) = {
      for (l <- node.legalValues) yield {
	val constraint = l.ranges.map( _ match {
	  case MinMaxRange(low, high) => (rewriteExpression(low) <= data(node)) & (data(node) <= rewriteExpression(high))
	  case SingleValueRange(v) => rewriteExpression(v) === data(node)
	}).reduceLeft(_ | _)
	!effective(node) | constraint
      }
    }
    
    val constraints:Iterable[ReqConstraint] = 
    (for(n <- allNodes.values) yield {
      var index = 0
      for (req <- n.reqs) yield {
	val c = !effective(n) | rewriteExpression(req)
	val result = ReqConstraint(c, n, index)
	index += 1
	result
      }
    }).flatten 
    
    val legalValues = allNodes.mapValues(legalValue2Constraint(_)).filter(p => p._2.isDefined).mapValues(_.get)
    
    val activeIfs = allNodes.mapValues(active)
    
    val defaultValues = 
    (for(n <- allNodes.values;
	 default <- n.defaultValue) yield {
      (n.id, ExpressionHelper.simplify(rewriteExpression(default)))
    }).toMap
    
    val semanticVars = 
    (for (n <- allNodes.values) yield (n.id, value(n))).toMap
    
    val simplifiedConstraints = 
      for(ReqConstraint(c, n, index) <- constraints) yield {
	ReqConstraint(ExpressionHelper.simplifyWithReplacement(c, newTypes), n, index)
      }
    
    val simplifiedLegalValues = legalValues.mapValues(ExpressionHelper.simplifyWithReplacement(_, newTypes))
    val simplifiedActiveIfs = activeIfs.mapValues(ExpressionHelper.simplifyWithReplacement(_, newTypes))
    val simplifiedSemanticVars = semanticVars.mapValues(ExpressionHelper.simplifyWithReplacement(_, newTypes))
    
    ImlConstraints(simplifiedConstraints, simplifiedLegalValues, simplifiedActiveIfs, simplifiedSemanticVars, defaultValues, newTypes)
  }

  // allNodes should be type correct
  def values2configuration(values: String => ConfigValue,
			   // packages:Iterable[PackageRef],
			   allNodes:Map[String, Node], 
			   types: String => Type,
			   translatedDefaults:String => Expression,
			   semanticVars:String => Expression):(Map[String, Literal], Map[String, Literal]) = {
    // val packageMap = packages.map(p=>(p.name, p.ver)).toMap
    val cycleChecker = new CycleChecker()
    var semanticConfig = mutable.Map[String, Literal]()
    def getDefaultValue(id:String) = {
      val default = translatedDefaults(id)
      val result = ExpressionHelper.evaluateTypeCorrectExpression(default, getVarValue)
      result 
    }
    def getVarValue(id:String):Literal = cycleChecker.checkCycle(id) {
      if (id.endsWith(boolVarSuffix)) {
	getBoolValue(id.substring(0, id.length - boolVarSuffix.length))
      } 
      else if (id.endsWith(dataVarSuffix)) {
	getDataValue(id.substring(0, id.length - dataVarSuffix.length))
      }
      else { //semantic vars
	val computed = semanticConfig.get(id)
	if (computed.isDefined) computed.get
	else {
	  val result = ExpressionHelper.evaluateTypeCorrectExpression(semanticVars(id), getVarValue)
	  semanticConfig put (id, result)
	  result
	}
      }
    }
    def getBoolValue(id:String):Literal = { 
      if (allNodes(id).cdlType == CdlType.Package) BoolLiteral(true)
      else 
	values(id) match {
	case SingleConfigValue(b) => 
	  assert(b.isInstanceOf[BoolLiteral])
	  b
	case DoubleConfigValue(b, _) => 
	  b
	case NoneConfigValue =>

	  ExpressionHelper.evaluateTypeCorrectExpression(ToBool(getDefaultValue(id)))
      }
    }
    def getDataValue(id:String) = { 
      val n = allNodes(id)
      if (n.cdlType == CdlType.Package) BoolLiteral(true) //StringLiteral(packageMap(n.id))
      else 
	values(id) match {
	case SingleConfigValue(l) => l
	case DoubleConfigValue(_, l) => l
	case NoneConfigValue => getDefaultValue(id)
      }
    }
    val config = (for (n <- allNodes.values) yield {
      if (n.calculated.isDefined || n.cdlType == CdlType.Interface || n.cdlType == CdlType.Package) 
	List[(String, Literal)]()
      else 
	n.flavor match {
	case Flavor.None => List[(String, Literal)]()
	case Flavor.Bool => List((n.id + NodeHelper.boolVarSuffix, getBoolValue(n.id)))
	case Flavor.Data => List((n.id + NodeHelper.dataVarSuffix, getDataValue(n.id)))
	case Flavor.BoolData => 
	  List((n.id + NodeHelper.boolVarSuffix, getBoolValue(n.id)), (n.id + NodeHelper.dataVarSuffix, getDataValue(n.id)))
      }
    }).flatten.toMap
    for (n <- allNodes.values) yield {
      if (!semanticConfig.contains(n.id))
	semanticConfig put (n.id, getVarValue(n.id))
    }
    (config, semanticConfig)
  }



  def fillImplements(nodes:Iterable[EccNode]) {
    val map = new mutable.HashMap[String, EccNode]
    def createMap(n:EccNode) {
      map += n.id -> n
      n.children.foreach(createMap(_))
    }
    nodes.foreach(createMap(_))
    
    def addImplements(i:EccNode) {
      i.implementedBy.foreach(id => {
	val n = map.get(id)
	if (n.isDefined) n.get.implements += i
      })
      i.children.foreach(addImplements(_))
    }
    nodes.foreach(addImplements(_))
  }
  
  // def printIml(nodes:Iterable[EccNode], writer:java.io.Writer):String {
  // var currentIndent = 0;
  // def writeHead {
  // for (i <- 0 to currentIndent) {
  // writer.write("  ")
  // }
  // }
  // def writeline(text:String) {
  // writerhead
  // writer.write(text)
  // writer.write('\n')
  // }
  // def type2Text(cdlType: CdlType.Value):String = cdlType match {
  // case CdlType.Option => "option"
  // case CdlType.Component => "component"
  // case CdlType.Package => "package"
  // case CdlType.Interface => "interface"
  // }
  // def writeNode(n:EccNode) {
  // writeline(type2Text(n.cdlType) + " " + n.id + '{')
  // currentIndent += 1
  // writeline(type2Text(n.cdlType) + " " n.id)
  
  // }
  // for (node <- nodes) {
  
  // }
  // }
  
  def EccNodes2Nodes(nodes:Iterable[EccNode]) = {
    import ConditionalCompilation._
    import org.kiama.rewriting.Rewriter._
    def removeReal[T]:T=>T = rewrite(everywheretd(rule[Any]{
      case RealLiteral(r) => IntLiteral(r.toLong)
      case LegalValuesOption(rs) => LegalValuesOption(List() ++ rs)
    } )) _
    

    def EccNode2Node(n:EccNode):Node = {
      val newNode = Node(n.id,
			 n.cdlType,
			 "",
			 None,
			 n.flavor,
			 n.defaultValue,
			 n.calculated,
			 n.legalValues,
			 n.reqs,
			 n.activeIfs,
			 n.implements.map(_.id),
			 n.children.map(EccNode2Node))
      var result = newNode
      IF[CompilationOptions.CONVERT_REAL_TO_INT#v] {
	result = removeReal(result)
      }
      result
    }
    
    // def EccNode2NodeRemoveReal(n:EccNode):Node = 
    // Node(n.id,
    // n.cdlType,
    // "",
    // None,
    // n.flavor,
    // n.defaultValue.map(removeReal),
    // n.calculated.map(removeReal),
    // n.legalValues.map(removeReal),
    // n.reqs.map(removeReal),
    // n.activeIfs.map(removeReal),
    // n.implements.map(_.id),
    // n.children.map(EccNode2NodeRemoveReal))

    // var convert:EccNode=>Node = EccNode2Node
    // IF[CompilationOptions.CONVERT_REAL_TO_INT#v] {
    // convert = EccNode2NodeRemoveReal
    // }
    

    nodes.map(EccNode2Node)
  }
}

case class GetData(id:String) extends FunctionCall("get_data", id)
case class IsActive(id:String) extends FunctionCall("is_active", id)
case class IsEnabled(id:String) extends FunctionCall("is_enabled", id)
case class IsLoaded(id:String) extends FunctionCall("is_loaded", id)


case class Node(id : String,
                cdlType : CdlType.Value,
                display : String,
                description : Option[String],
                flavor : Flavor.Value,
                defaultValue : Option[Expression], 
                calculated : Option[Expression],
                legalValues : Option[LegalValuesOption],
                reqs : Seq[Expression],
                activeIfs : Seq[Expression],
                implements : Set[String], 
                children : Iterable[Node]) {
  
  override def toString():String = toString(0)
  def toString(indent:Int):String = {
    val sb = new StringBuilder()
    var currentIndent = indent
    def writeHead {
      for (i <- 0 to currentIndent) {
	sb ++= "  "
      }
    }
    def writeline(text:String) {
      writeHead
      sb ++= text
      sb += '\n'
    }
    writeline(cdlType.toString + ' ' + id + '{')
    currentIndent += 1
    writeline("display " + display)
    if (description.isDefined)
      writeline("description " + description.get)
    writeline("flavor " + flavor)
    if (defaultValue.isDefined)
      writeline("defaultValue " + defaultValue.get)
    if (calculated.isDefined)
      writeline("calculated " + calculated.get)
    if (legalValues.isDefined)
      writeline("legal values " + legalValues.get)
    for (req <- reqs)
    writeline("requires " + req)
    for (activeif <- activeIfs)
    writeline("active_if " + activeif)
    for (implement <- implements.toList.sorted)
    writeline("implements " + implement)
    writeline("")
    for (child <- children)
    sb ++= child.toString(currentIndent)
    currentIndent -= 1
    writeline("}")
    writeline("")
    sb.toString
  }
  
}


case class EccNode(id : String,
                   cdlType : CdlType.Value,
                   flavor : Flavor.Value,
                   defaultValue : Option[Expression], 
                   calculated : Option[Expression],
                   legalValues : Option[LegalValuesOption],
                   reqs : Seq[Expression],
                   activeIfs : Seq[Expression],
                   implementedBy : Iterable[String], // identifiers
                   children : mutable.ListBuffer[EccNode] = new mutable.ListBuffer()) {
  
  def this(id:String, cdlType:CdlType.Value, flavor:Flavor.Value, defaultValue:Option[Expression], calculated:Option[Expression], legalValues:Option[LegalValuesOption], reqs:Seq[Expression], activeIfs:Seq[Expression], implementedBy:Iterable[String]) = 
    this(id, cdlType, flavor, defaultValue, calculated, legalValues, reqs, activeIfs, implementedBy, new mutable.ListBuffer())
  
  var implements:mutable.Set[EccNode] = new mutable.HashSet();
  
  def getParent() = this.parent
  var parent:Option[EccNode] = None
  
  def addChild(n:EccNode) {
    n.parent = Some(this)
    children += n;
  }
}

object Flavor extends Enumeration {
  val None, Bool, Data, BoolData = Value
}

object CdlType extends Enumeration {
  val Option, Component, Package, Interface = Value
}

case class LegalValuesOption(val ranges : Seq[Range]) {
  override def toString() = ranges.map(_.toString).reduce((l, r) => l + " " + r)
}
abstract class Range
case class MinMaxRange(low: Expression, high: Expression) extends Range {
  override def toString() = low.toString + " to " + high
}
case class SingleValueRange( v : Expression ) extends Range {
  override def toString() = v.toString
}
