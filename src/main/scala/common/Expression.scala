package ca.uwaterloo.gsd.rangeFix
import collection._

object Expression {
  implicit def string2VarRef(s: String) = IdentifierRef(s)
  implicit def int2literal(s: Int) = IntLiteral(s)
  implicit def bool2literal(s: Boolean) = BoolLiteral(s)

  type Types = collection.Map[String, SingleType]
  type Configuration = collection.Map[String, Literal]

  import org.kiama.rewriting.Rewriter._
  import org.kiama.rewriting.Strategy
  import MyRewriter._
  // private def rememberFuncDef(s:mutable.Map[String, Term], f: =>Strategy):Strategy =
  //   new Strategy {
  //     def apply (t : Term) : Option[Term] = {
  //       val result = f(t)
  //       t match {
  //         case x:GFunctionDef[_] => {
  //           s += x.name -> result.getOrElse(x)
  //         }
  //         case _ => 
  //       }
  //       result
  //     }
  //   }

  // private def bypassFuncDef(s:Map[String, Term]) = rule {
  //   case x:GFunctionDef[_] if (s.contains(x.name)) => s(x.name)
  // }

  private def bypassFuncDef() = rule[GFunctionDef[_]] {
    case x:GFunctionDef[_] => x
  }

  def everywheretd(s : => Strategy):Strategy =
    everywheretd(s, _ => s)

  def everywheretd(s : => Strategy,
                   fs : GFunctionDef[_] => Strategy,
                   buffer : mutable.Map[String, Any] = mutable.Map()):Strategy =
    everywhereConstructor(s, fs,
                        (top, children) => attempt(top) <* all(children), buffer)

  def everywherebu(s : => Strategy):Strategy =
    everywherebu(s, _ => s)

  def everywherebu(s : => Strategy,
                   fs : GFunctionDef[_] => Strategy,
                   buffer : mutable.Map[String, Any] = mutable.Map()):Strategy =
    everywhereConstructor(s, fs,
                        (top, children) => all(children) <* attempt(top), buffer)

  
  private def everywhereConstructor(
    s : => Strategy,
    fs : GFunctionDef[_] => Strategy,
    constructEverywhere : (Strategy, Strategy) => Strategy,
    buffer : mutable.Map[String, Any]
  ): Strategy = {
    new Strategy("Test") {
      override val body : Any => Option[Any] = (r : Any) => {
        r match {
          case f:GFunctionDef[_] =>
            if (buffer.contains(f.name)) {
              Some(buffer(f.name))
            } else {
              val newS = fs(f)
              val result = rewrite(constructEverywhere(s, everywhereConstructor(newS, fs, constructEverywhere, buffer)))(r)
              buffer += f.name -> result
              Some(result)
            }
          case _ => constructEverywhere(s, everywhereConstructor(s, fs, constructEverywhere, buffer))(r)
        }
      }
    }
  }
    
  //   def everywheretdImpl : Strategy = {
  //     bypassFuncDef(a) <+ rememberFuncDef(a, attempt(s) <* (all (everywheretdImpl)))
  //   }
  // everywheretdImpl


  // def everywheretd(s: => Strategy):Strategy = {
  //   val a = mutable.Map[String, GFunctionDef[_]]()
  //   everywheretdWithGuard(bypassFuncDef(a), rememberFuncDef(a, s))
  // }

  // def everywherebu(s : => Strategy):Strategy = {
  //   val a = mutable.Map[String, Term]()
  //   def everywherebuImpl : Strategy = {
  //     bypassFuncDef(a) <+ rememberFuncDef(a, (all(everywherebuImpl)) <* attempt(s))
  //   }
  //   everywherebuImpl
  // }

  // def everywherebu(s: => Strategy):Strategy = {
  //   val a = mutable.Map[String, GFunctionDef[_]]()
  //   everywherebuWithGuard(bypassFuncDef(a), rememberFuncDef(a, s))
  // }
  
  def everywheretdNoDef(s: => Strategy):Strategy = {
    everywheretdWithGuard(bypassFuncDef, s)
  }

  def everywherebuNoDef(s: => Strategy):Strategy = {
    everywherebuWithGuard(bypassFuncDef, s)
  }

  class GlobalVarCollector {
    private var func2varsMap = Map[String, Set[String]]()
    private def collectVars(lvars:Set[String],ex:Any):Set[String] = {
      val vars = mutable.Set[String]()
      val captureFuncDef = rule[GFunctionDef[_]] {
        case f:GFunctionDef[Any] =>
          if (func2varsMap.contains(f.name))
            vars ++= func2varsMap(f.name)
          else {
              val varsInFunc = collectVars(f.paramNames.toSet, f.body)
              vars ++= varsInFunc
              func2varsMap += (f.name -> varsInFunc)
            }
          f
      }
      val storeVars = rule[GIdentifierRef] {
        case i:GIdentifierRef =>
          if (!(lvars contains i.id)) vars += i.id
          i
      }
      rewrite(everywheretdWithGuard(captureFuncDef, storeVars))(ex)
      vars
    }

    def allGlobalVars(e:Any):Set[String] = {
      collectVars(Set(), e)
    }
  }

  /**collect variables from Expression
   * @param localVars local variables
   * @param e: Expression
   * @return a set contains all the global variebles
   */
  def collectGlobalVars(e:Any):Set[String] = new GlobalVarCollector().allGlobalVars(e)
  // def collectGlobalVars(e:Term):Set[String]={
  //   var func2varsMap = Set[String]()
  //   def collectVars(lvars:Set[String],ex:Term):Set[String]={
  //     val vars = mutable.Set[String]()
  //     val captureFuncDef = rule {
  //       case f:GFunctionDef[_] =>
  //         if (!func2varsMap.contains(f.name)) {
  //             val varsInFunc = collectVars(f.paramNames.toSet, f.body)
  //             vars ++= varsInFunc
  //             func2varsMap += f.name
  //           }
  //         f
  //     }
  //     val storeVars = rule {
  //       case i:GIdentifierRef =>
  //         if (!(lvars contains i.id)) vars += i.id
  //         i
  //     }
  //     rewrite(everywheretdWithGuard(captureFuncDef, storeVars))(ex)
  //     vars
  //   }
  //   collectVars(Set(),e)
  // }
  

  def replaceVars[T](replace:String => Option[T], localVars:Iterable[String] = Iterable()):Strategy = {
    val func2ResultMap = mutable.Map[String, GFunctionDef[_]]()
    val captureFuncDef = rule[GFunctionDef[_]] {
        case f:GFunctionDef[_] =>
          if (func2ResultMap.contains(f.name))
            func2ResultMap(f.name)
          else {
            val newBody = rewrite(replaceVars(replace, f.paramNames))(f.body)
            val result = f.replaceBody(newBody)
            func2ResultMap += f.name -> result
            result
          }
      }
    val storeVars = rule[Any] {
      case i:GIdentifierRef if !localVars.exists(_ == i.id)=>
        replace(i.id).getOrElse(i)
    }
    everywheretdWithGuard(captureFuncDef, storeVars)
  }


  def flatUserFuncCalls(
    types:Types,
    replace:String=>Option[Expression] = (_)=>None):Strategy = {
    val buffer = mutable.Map[(String, Seq[Expression]), Expression]()
    val captureFuncCall = rule[Expression] {
      case f:UserFunctionCall =>
        assert(f.func.params.size == f.args.size)
        println(f.args)
        val flattenedArgs =
          f.args.map(rewrite(flatUserFuncCalls(types, replace)))
        println(flattenedArgs)
        val key = (f.func.name, flattenedArgs)
        if (buffer.contains(key)) buffer(key)
        else {
          def newReplace = (id:String) => {
            val argIndex = f.func.params.map(_._1).indexOf(id)
            if (argIndex >= 0) Some(flattenedArgs(argIndex)) else None
          }
          val unsimplified = rewrite(flatUserFuncCalls(types, newReplace))(f.func.body)
          val result = ExpressionHelper.simplifyWithReplacement(unsimplified, types)
          println(f.func.name)
          buffer += key -> result
          result
        }
    }
    val replaceParams = rule[Expression] {
      case x@IdentifierRef(id) => replace(id).getOrElse(x)
    }
    def simplify = rule[Expression] {
      case x:Expression => ExpressionHelper.simplifyWithReplacement(x, types)
    }
    everywherebuWithGuard(captureFuncCall, replaceParams) <* simplify
  }
  
  /**
   * assign variebles in an expression
   * @param expr the expression
   * @param assign the map contains assignment of variebles
   * @return the
   */
  def assignVar0(expr:Expression, assign:String => Option[Expression], types:Types):Expression = {
    val theRule = replaceVars(assign) <* flatUserFuncCalls(types)
    rewrite(theRule)(expr)
  }
  def assignVar(expr:Expression, assign:String => Option[Expression], types:Types):Expression = {
    var funcCalls:Set[(UserFunctionCall,Expression)]=Set()
    var nextC:Int = 0
    var list:List[(String,Expression)] = List[(String,Expression)]()
    import org.kiama.rewriting.Rewriter._
    val ruleFuncDef=rule[FunctionDef]{
      case x:FunctionDef => x
    }
    def commonRule:Strategy=rule[Expression]{
     case IdentifierRef(vname)=>{
        val rlt = assign(vname)
        if (rlt isEmpty) IdentifierRef(vname) else rlt get
      }
      case x:UserFunctionCall=>{
        val getCache = funcCalls.find(_._1==x)
        if (getCache!=None){
          getCache.get._2
        }
        else{
         var funcDef = x.func
          assert(funcDef.params.size == x.args.size)
          var ruleParas=rule[Expression]{
            case IdentifierRef(vname) if (funcDef.params.exists(A=>{A._1==vname}))=>{
              val index = funcDef.params.toIndexedSeq.indexWhere(A=>{A._1==vname})
              x.args(index)
            }
          }
          def rulet:Strategy={
            ruleFuncDef<+ruleParas<+(all(rulet)<*commonRule)
          }
          var getC:Int = nextC;
          nextC = nextC+1
          var r:Expression = rewrite(rulet)(funcDef.body)
          val result = ExpressionHelper.simplifyWithReplacement(r, types)
          val value = (x, result)
          funcCalls += value
          result
        }
      }
      case x:Expression => x
      case x=>x
    }
    def rulea:Strategy = {
      ruleFuncDef<+(all(rulea)<*commonRule)
    }
    val rlt = rewrite(rulea)(expr)
    rlt
  }

  class CollectorS[T](f: PartialFunction[Any, T]) {
    var func2TMap = Map[String, Set[T]]()
    def collect(ex:Any):Set[T]={
      val result = mutable.Set[T]()
      def captureFuncDef() = rule[GFunctionDef[_]] {
        case x:GFunctionDef[Any] =>
          if (func2TMap.contains(x.name)) {
            result ++= func2TMap(x.name)
          }
          else {
            val innerResult = collect(x.body)
            result ++= innerResult
            func2TMap += x.name -> innerResult
          }
          x
      }
      val add: (T) => Unit = (v:T) => result += v
      val qs:Strategy = query(f andThen add)
      rewrite(MyRewriter.everywheretdWithGuard(captureFuncDef, qs))(ex)
      result
    }    
  }

  class CollectorL[T](f: PartialFunction[Any, T]) {
    var func2TMap = Map[String, List[T]]()
    def collect(ex:Any):List[T]={
      val result = mutable.ListBuffer[T]()
      val visitedFuncs = mutable.Set[String]()
      def captureFuncDef() = rule[GFunctionDef[_]] {
        case x:GFunctionDef[Any] =>
          if (func2TMap.contains(x.name)) {
            if (!visitedFuncs.contains(x.name)) result ++= func2TMap(x.name)
          }
          else {
            val innerResult = collect(x.body)
            result ++= innerResult
            func2TMap += x.name -> innerResult
          }
          visitedFuncs += x.name
          x
      }
      val add : (T) => Unit = (v:T) => result += v
      val qs:Strategy = query(f andThen add)
      rewrite(MyRewriter.everywheretdWithGuard(captureFuncDef, qs))(ex)
      result.toList
    }    
  }
  
  // def collects[T](f: PartialFunction[Term, T])(e:Term):Set[T] = {
  //   val b = mutable.Set[T]()
  //   val add = (v:T) => b += v 
  //   val qs:Strategy = query(f andThen add)
  //   this.everywheretd(qs)(e)
  //   b
  // }

  private def collectConstructor[T](s:PartialFunction[Any, T],
                            fs:GFunctionDef[_]=>PartialFunction[Any, T],
                            b:collection.generic.Growable[T]):Strategy = {
    val add : (T) => Unit = (v:T) => b += v 
    val qs:Strategy = query(s andThen add)
    def newFs(f:GFunctionDef[_]):Strategy = query(fs(f) andThen add)
    this.everywheretd(qs, newFs)
  }

  def collectl[T](s: PartialFunction[Any, T],
                  fs:GFunctionDef[_]=>PartialFunction[Any, T])(e:Any):List[T] = {
    val b = mutable.ListBuffer[T]()
    collectConstructor(s, fs, b)(e)
    b.toList    
  }

  def collectl[T](f: PartialFunction[Any, T])(e:Any):List[T] = {
    collectl(f, _ => f)(e)
  }
  
  def collects[T](s: PartialFunction[Any, T],
                  fs:GFunctionDef[_]=>PartialFunction[Any, T])(e:Any):Set[T] = {
    val b = mutable.Set[T]()
    collectConstructor(s, fs, b)(e)
    b    
  }

  def collects[T](f: PartialFunction[Any, T])(e:Any):Set[T] = {
    collects(f, _ => f)(e)
  }

  // def collectl[T](f: PartialFunction[Any, T])(e:Any):List[T] = {
  //   val b = mutable.ListBuffer[T]()
  //   val add = (v:T) => b += v 
  //   val qs:Strategy = query(f andThen add)
  //   this.everywheretd(qs)(e)
  //   b.toList
  // } 

  def collectsNoDef[T](f: PartialFunction[Any, T])(e:Any):Set[T] = {
    val b = mutable.Set[T]()
    val add : (T) => Unit = (v:T) => b += v 
    val qs:Strategy = query(f andThen add)
    this.everywheretdNoDef(qs)(e)
    b
  } 

  def collectlNoDef[T](f: PartialFunction[Any, T])(e:Any):List[T] = {
    val b = mutable.ListBuffer[T]()
    val add : (T) => Unit = (v:T) => b += v 
    val qs:Strategy = query(f andThen add)
    this.everywheretdNoDef(qs)(e)
    b.toList
  } 


  // def allGlobalVars(e:Expression, localVars:Set[String]=Set(),
  //                   func2varsMap:mutable.Map[String, Set[String]]=mutable.Map[String, Set[String]]())
  // :Set[String] = {
  //   val vars = mutable.Set[String]()
  //   val captureFuncDef = rule {
  //     case f:FunctionDef =>
  //       if (func2varsMap.contains(f.name))
  //         vars ++= func2varsMap(f.name)
  //       else {
  //           val varsInFunc = allGlobalVars(f.body, localVars ++ f.params.map(_._1), func2varsMap)
  //           vars ++= varsInFunc
  //           func2varsMap += (f.name -> varsInFunc)
  //         }
  //       f
  //   }
  //   val storeVars = rule {
  //     case i@IdentifierRef(id) =>
  //       if (!(localVars contains id)) vars += id
  //       i
  //   }
  //   everywheretdWithGuard(captureFuncDef, storeVars)(e)
  //   vars       
  // }

  // def allGlobalVars(es:Iterable[Expression]):Set[String] = {
  //   val func2varsMap = mutable.Map[String, Set[String]]()
  //   es.map(allGlobalVars(_, Set(), func2varsMap)).flatten(x => x).toSet
  // }

  def sortFuncDef(cs:Any):Seq[FunctionDef] = {
    val nodeMap = mutable.Map[String, Node]()
    val rootNodes = mutable.Set[Node]()
    case class Node(func:FunctionDef) {
      private val incomings = mutable.Set[Node]()
      private val outgoings = mutable.Set[Node]()
      rootNodes.add(this)
      def addOutgoing(n:Node) {
        outgoings += n
        n.incomings += this
        rootNodes.remove(n)
      }
      def removeOutgoing(n:Node) {
        outgoings -= n
        n.incomings -= this
        if (n.incomings.size == 0) rootNodes += n
      }
      def delete {
        while(incomings.size > 0)
        incomings.head.removeOutgoing(this)
        while(outgoings.size > 0)
        removeOutgoing(outgoings.head)
        rootNodes.remove(this)
      }
    }
    implicit def get(f:FunctionDef):Node = nodeMap.getOrElse(f.name, {
        val newNode = Node(f)
        nodeMap += f.name -> newNode
        newNode
    })
    

    val rootRule = query[UserFunctionCall]{
      case f:UserFunctionCall => get(f.func)
    }

    def childrenRule(p:GFunctionDef[_]):Strategy = query[UserFunctionCall] {
      case f:UserFunctionCall =>
        f.func.addOutgoing(p.asInstanceOf[FunctionDef])
    }
    
    rewrite(Expression.everywheretd(rootRule, childrenRule))(cs)
    
    val result = mutable.ListBuffer[FunctionDef]()
    while(rootNodes.size > 0) {
      val n = rootNodes.head
      result += n.func
      n.delete
    }
    
    result
  
  }


  // helper methods
  def func(name:String, body:Expression) = new FunctionDef(
    name,
    Seq(),
    ExpressionHelper.getType(body, Map()),
    body
  )


  
}

trait GExpression
sealed abstract class Expression extends Serializable with GExpression {
  def children(): List[Expression] =
    (for (
      i <- 0 until this.asInstanceOf[Product].productArity;
      child = this.asInstanceOf[Product].productElement(i);
      if (child.isInstanceOf[Expression])
    ) yield child.asInstanceOf[Expression]).toList

  def <(r: Expression) = LessThan(this, r)
  def >(r: Expression) = GreaterThan(this, r)
  def +(r: Expression) = Plus(this, r)
  def -(r: Expression) = Minus(this, r)
  def &(r: Expression) = And(this, r)
  def |(r: Expression) = Or(this, r)
  def ==>(r: Expression) = Implies(this, r)
  def <=(r: Expression) = LessThanOrEq(this, r)
  def >=(r: Expression) = GreaterThanOrEq(this, r)
  def ===(r: Expression) = Eq(this, r)
  def !==(r: Expression) = NEq(this, r)
  def unary_! : Expression = Not(this)

}

abstract class Literal extends Expression
abstract class EnumItemLiteral extends Literal

case class StringLiteral(value: String) extends EnumItemLiteral {
  override def toString = "\"" + value + "\""
}
case class IntLiteral(value: Long) extends EnumItemLiteral {
  override def toString = value.toString
}
case class BoolLiteral(value: Boolean) extends Literal {
  override def toString = if (value) "true" else "false"
}
case class RealLiteral(value: Double) extends EnumItemLiteral {
  override def toString = value.toString
}
case class SetLiteral(values: Set[String]) extends Literal {
  override def toString = values.toString
}
// object Tristate extends Enumeration {
// 	val Yes, Mod, No = Value
// 	def max(l:Value, r:Value) = l match {
// 		case Yes => Yes
// 		case Mod => r match {
// 			case Yes => Yes
// 			case _ => Mod 
// 		}
// 		case No => r
// 	}
// 	def min(l:Value, r:Value) = l match {
// 		case Yes => r
// 		case Mod => r match {
// 			case No => No
// 			case _ => Mod
// 		}
// 		case No => No
// 	}
// }
// case class TristateLiteral(value : Tristate.Value) extends Literal {
// 	override def toString = "Tristate." + value.toString
// }

case class EnumLiteral(value: EnumItemLiteral, t: EnumType) extends Literal {
  assert(t.items.contains(value))
  override def toString = t.toString + "." + value
}

trait GIdentifierRef {
  val id:String
}

case class IdentifierRef(id: String) extends Expression with GIdentifierRef {
  override def toString = id
}

case class Conditional(cond: Expression,
  pass: Expression,
  fail: Expression) extends Expression {
  override def toString = String.format("( if %s then %s else %s )", cond, pass, fail)
}

sealed abstract class UnaryExpression(e: Expression,
  op: String) extends Expression {
  override def toString = op + e
}

sealed abstract class BinaryExpression(l: Expression,
  r: Expression,
  op: String) extends Expression {
  override def toString = String.format("(%s %s %s)", l, op, r)
}

case class Or(left: Expression, right: Expression)
  extends BinaryExpression(left, right, "||")
case class And(left: Expression, right: Expression)
  extends BinaryExpression(left, right, "&&")
case class Implies(left: Expression, right: Expression)
  extends BinaryExpression(left, right, "implies")

case class Eq(left: Expression, right: Expression)
  extends BinaryExpression(left, right, "==")
case class NEq(left: Expression, right: Expression)
  extends BinaryExpression(left, right, "!=")

case class LessThan(left: Expression, right: Expression)
  extends BinaryExpression(left, right, "<")
case class LessThanOrEq(left: Expression, right: Expression)
  extends BinaryExpression(left, right, "<=")
case class GreaterThan(left: Expression, right: Expression)
  extends BinaryExpression(left, right, ">")
case class GreaterThanOrEq(left: Expression, right: Expression)
  extends BinaryExpression(left, right, ">=")

case class Plus(left: Expression, right: Expression)
  extends BinaryExpression(left, right, "+")
case class Minus(left: Expression, right: Expression)
  extends BinaryExpression(left, right, "-")

case class Times(left: Expression, right: Expression)
  extends BinaryExpression(left, right, "*")
case class Div(left: Expression, right: Expression)
  extends BinaryExpression(left, right, "/")
case class Mod(left: Expression, right: Expression)
  extends BinaryExpression(left, right, "%")

case class Dot(left: Expression, right: Expression)
  extends BinaryExpression(left, right, ".")

case class Not(expr: Expression)
  extends UnaryExpression(expr, "!")

abstract class FunctionCall(name: String, arguments: String*) extends Expression {
  lazy val argumentStrings = "(" + arguments.foldLeft("")((a, b) => a + (if (a != "") "," else "") + b) + ")"
  override def toString() = name + argumentStrings
}

case class IsSubstr(container: Expression, containee: Expression)
  extends FunctionCall("is_substr",
    container.toString,
    containee.toString)
case class ToInt(n: Expression) extends FunctionCall("toInt", n.toString)
case class ToString(n: Expression) extends FunctionCall("toString", n.toString)
case class ToBool(n: Expression) extends FunctionCall("toBool", n.toString)
// case class Min(left:Expression, right:Expression) 
//      extends FunctionCall("min", left.toString, right.toString)
// case class Max(left:Expression, right:Expression) 
//      extends FunctionCall("max", left.toString, right.toString)
case class UserFunctionCall(func: FunctionDef, args: Seq[Expression]) extends FunctionCall(func.name, args.map(_.toString): _*) {
  assert(func.params.size == args.size)
}

// class FunctionDef(val name: String,
//                   val params: Iterable[(String, Type)],
//                   val returnType: Type,
//                   val body: Expression) {

//   override def equals(that:Any) = {
//     that match {
//       case f:FunctionDef =>
//         if(f.name == name) {
//           assert(f.params == params && f.returnType == returnType && f.body == body)
//           true
//         }
//         else false
//       case _ => false
//     }
//   }

//   override def hashCode():Int = name.hashCode
// }

trait GFunctionDef[TExpr] {
  val name:String
  def paramNames : Seq[String]
  def body : TExpr
  def replaceBody(newBody:TExpr):GFunctionDef[TExpr]
}

case class FunctionDef(override val name: String, params: Seq[(String, Type)], returnType: Type, body: Expression) extends GFunctionDef[Expression] {
  override def equals(obj:Any):Boolean={
    if (!obj.isInstanceOf[FunctionDef])
      return false;
    val objFun = obj.asInstanceOf[FunctionDef]
    // if (objFun.name != this.name)
    //   return false;
    // if (params.toIndexedSeq != objFun.params.toIndexedSeq)
    //   return false;
    // if (objFun.returnType != returnType)
    //   return false;
    // if (objFun.body != body)
    //   return false;
    // return true;
    objFun.name == this.name
  }

  override def hashCode() :Int={
    return name.hashCode();
  }

  override def paramNames = {
    params.map(_._1)
  }

  override def replaceBody(newBody:Expression) =
    FunctionDef(name, params, returnType, newBody)

  override def toString() = "%s(%s):%s=%s".format(name, params.map(p => p._1 + ":" + p._2).reduceOption(_ + "," + _).getOrElse(""), returnType, body)
  
  def $(args:Expression*) : Expression = UserFunctionCall(this, args)
}
  

object ExpressionHelper {
  import Expression._
  private def toInt(l: Literal): Long = l match {
    case StringLiteral(s) =>
      try {
        s.toInt
      } catch {
        case _: java.lang.NumberFormatException => 0
      }
    case IntLiteral(i) => i
    case BoolLiteral(b) => if (b) 1 else 0
    case EnumLiteral(v, t) => toInt(v)
    case SetLiteral(_) => toInt(StringLiteral(toString(l)))
    // case TristateLiteral(Tristate.Yes) => 2
    // case TristateLiteral(Tristate.Mod) => 1
    // case TristateLiteral(Tristate.No) => 0		
  }
  private def toString(l: Literal): String = l match {
    case StringLiteral(s) => s
    case IntLiteral(i) => i.toString
    case b: BoolLiteral => toInt(b).toString
    case EnumLiteral(v, t) => toString(v)
    case SetLiteral(vs) => if (vs.size > 0) vs.reduce(_ + " " + _) else ""
    // case TristateLiteral(Tristate.Yes) => "yes"
    // case TristateLiteral(Tristate.Mod) => "mod"
    // case TristateLiteral(Tristate.No) => "no"		
  }
  private def toBoolean(l: Literal): Boolean = {
    l match {
      case StringLiteral(s) if s == "0" || s == "" => false
      case StringLiteral(s) => true
      case EnumLiteral(v, t) => toBoolean(v)
      case _ => toInt(l) != 0
    }
  }

  def configuration2Types(c:Configuration):Types = c.mapValues(getLiteralType)

  
  
  def evaluateUntypedExpr(exp: Expression, valuation: Map[String, EnumItemLiteral] = Map[String, EnumItemLiteral]()): EnumItemLiteral = {
    def evaluateAsGeneralExpr(exp: Expression): EnumItemLiteral = ExpressionHelper.evaluateUntypedExpr(exp, valuation)
    def toLiteral(b: Boolean): EnumItemLiteral = if (b) IntLiteral(1) else IntLiteral(0)
    exp match {
      case l: EnumItemLiteral => l
      case i: IdentifierRef => valuation.getOrElse(i.id, IntLiteral(0))
      case Conditional(c, p, f) => if (toBoolean(evaluateAsGeneralExpr(c))) evaluateAsGeneralExpr(p) else evaluateAsGeneralExpr(f)
      case Or(l, r) =>
        if (toBoolean(evaluateAsGeneralExpr(l))) IntLiteral(1)
        else if (toBoolean(evaluateAsGeneralExpr(r))) IntLiteral(1)
        else IntLiteral(0)
      case And(l, r) =>
        if (!toBoolean(evaluateAsGeneralExpr(l))) IntLiteral(0)
        else if (toBoolean(evaluateAsGeneralExpr(r))) IntLiteral(1)
        else IntLiteral(0)
      case Implies(l, r) => evaluateAsGeneralExpr(Or(Not(l), r))
      case Eq(l, r) =>
        if (toString(evaluateAsGeneralExpr(l)) == toString(evaluateAsGeneralExpr(r))) IntLiteral(1)
        else IntLiteral(0)
      case NEq(l, r) => evaluateAsGeneralExpr(Not(Eq(l, r)))
      case LessThan(l, r) => toLiteral(toInt(evaluateAsGeneralExpr(l)) < toInt(evaluateAsGeneralExpr(r)))
      case LessThanOrEq(l, r) =>
        toLiteral(toInt(evaluateAsGeneralExpr(l)) <= toInt(evaluateAsGeneralExpr(r)))
      case GreaterThanOrEq(l, r) =>
        toLiteral(toInt(evaluateAsGeneralExpr(l)) >= toInt(evaluateAsGeneralExpr(r)))
      case GreaterThan(l, r) => toLiteral(toInt(evaluateAsGeneralExpr(l)) > toInt(evaluateAsGeneralExpr(r)))
      case Plus(l, r) => IntLiteral(toInt(evaluateAsGeneralExpr(l)) + toInt(evaluateAsGeneralExpr(r)))
      case Minus(l, r) => IntLiteral(toInt(evaluateAsGeneralExpr(l)) - toInt(evaluateAsGeneralExpr(r)))
      case Times(l, r) => IntLiteral(toInt(evaluateAsGeneralExpr(l)) * toInt(evaluateAsGeneralExpr(r)))
      case Div(l, r) => IntLiteral(toInt(evaluateAsGeneralExpr(l)) / toInt(evaluateAsGeneralExpr(r)))
      case Mod(l, r) => IntLiteral(toInt(evaluateAsGeneralExpr(l)) % toInt(evaluateAsGeneralExpr(r)))
      case Dot(l, r) => StringLiteral(toString(evaluateAsGeneralExpr(l)) + toString(evaluateAsGeneralExpr(r)))
      case Not(l) => toLiteral(!toBoolean(evaluateAsGeneralExpr(l)))
      case IsSubstr(l, r) => toLiteral(toString(evaluateAsGeneralExpr(l)).contains(toString(evaluateAsGeneralExpr(r))))
      case ToInt(l) => IntLiteral(toInt(evaluateAsGeneralExpr(l)))
      case ToString(l) => StringLiteral(toString(evaluateAsGeneralExpr(l)))
      case ToBool(l) => toLiteral(toBoolean(evaluateAsGeneralExpr(l)))
      case _ => throw new IllegalArgumentException("The experssion contains " + exp)
    }
  }

  type FuncEvalBuffer = collection.mutable.Map[(String, Iterable[Literal]), Literal]

  def evaluateTypeCorrectExpression(e: Expression, valuation: String => Literal = Map(), evaluatedFuncs: FuncEvalBuffer = mutable.Map()): Literal = {
    def evaluate(e: Expression): Literal = {
      StateDebugger.trace("evalTrace")(String.format("Current: %s\n", e.toString))
      def isBoolTrue(l: Literal) =
        if (l == BoolLiteral(true)) true
        else {
          assert(l == BoolLiteral(false), l)
          false
        }
      def asInt(l: Literal) = {
        assert(l.isInstanceOf[IntLiteral], l)
        l.asInstanceOf[IntLiteral].value
      }
      // def asTristate(l:Literal) = {
      // 	assert(l.isInstanceOf[TristateLiteral], l)
      // 	l.asInstanceOf[TristateLiteral].value
      // }
      def asString(l: Literal) = {
        assert(l.isInstanceOf[StringLiteral])
        l.asInstanceOf[StringLiteral].value
      }
      def asSet(l: Literal) = {
        assert(l.isInstanceOf[SetLiteral])
        l.asInstanceOf[SetLiteral].values
      }
      def applyElse[T1, T2](first: T1 => T2, second: T1 => T2): T1 => T2 = c => {
        try {
          first(c)
        } catch {
          case _: java.util.NoSuchElementException => second(c)
        }
      }
      val result = e match {
        case l: Literal => l
        case i: IdentifierRef => valuation(i.id)
        case Conditional(c, p, f) =>
          if (isBoolTrue(evaluate(c))) evaluate(p) else evaluate(f)
        case Or(l, r) =>
          if (isBoolTrue(evaluate(l))) BoolLiteral(true)
          else if (isBoolTrue(evaluate(r))) BoolLiteral(true)
          else BoolLiteral(false)
        case And(l, r) =>
          if (!isBoolTrue(evaluate(l))) BoolLiteral(false)
          else if (isBoolTrue(evaluate(r))) BoolLiteral(true)
          else BoolLiteral(false)
        case Implies(l, r) => evaluate(Or(Not(l), r))
        case Eq(l, r) =>
          assert(getLiteralType(evaluate(l)) == getLiteralType(evaluate(r)), Eq(l, r).toString + " where " + l + ":" + getLiteralType(evaluate(l)) + " and " + r + ":" + getLiteralType(evaluate(r)))
          if (evaluate(l) == evaluate(r)) BoolLiteral(true)
          else BoolLiteral(false)
        case NEq(l, r) => evaluate(Not(Eq(l, r)))
        case LessThan(l, r) => BoolLiteral(asInt(evaluate(l)) < asInt(evaluate(r)))
        case LessThanOrEq(l, r) =>
          BoolLiteral(asInt(evaluate(l)) <= asInt(evaluate(r)))
        case GreaterThanOrEq(l, r) =>
          BoolLiteral(asInt(evaluate(l)) >= asInt(evaluate(r)))
        case GreaterThan(l, r) => BoolLiteral(asInt(evaluate(l)) > asInt(evaluate(r)))
        case Plus(l, r) => IntLiteral(asInt(evaluate(l)) + asInt(evaluate(r)))
        case Minus(l, r) => IntLiteral(asInt(evaluate(l)) - asInt(evaluate(r)))
        case Times(l, r) => IntLiteral(asInt(evaluate(l)) * asInt(evaluate(r)))
        case Div(l, r) => IntLiteral(asInt(evaluate(l)) / asInt(evaluate(r)))
        case Mod(l, r) => IntLiteral(asInt(evaluate(l)) % asInt(evaluate(r)))
        case Dot(l, r) => SetLiteral(asSet(evaluate(l)) ++ asSet(evaluate(r))) //StringLiteral(asString(evaluate(l)) + asString(evaluate(r)))
        case Not(l) => BoolLiteral(!isBoolTrue(evaluate(l)))
        case IsSubstr(l, r) => BoolLiteral(asSet(evaluate(r)).subsetOf(asSet(evaluate(l)))) //BoolLiteral(asString(evaluate(l)).contains(asString(evaluate(r))))
        case ToInt(l) => IntLiteral(toInt(evaluate(l)))
        case ToString(l) => StringLiteral(toString(evaluate(l)))
        case ToBool(l) => BoolLiteral(toBoolean(evaluate(l)))
        // case Max(l, r) => {
        // 	TristateLiteral(Tristate.max(asTristate(evaluate(l)), asTristate(evaluate(r))))
        // }
        // case Min(l, r) => {
        // 	TristateLiteral(Tristate.min(asTristate(evaluate(l)), asTristate(evaluate(r))))
        // }

        case UserFunctionCall(func, args) =>
          assert(args.size == func.params.size)
          val evaluatedArgs = args.map(evaluate)
          evaluatedFuncs.getOrElse(
            (func.name, evaluatedArgs),
            {
              StateDebugger.trace("evalTrace")(String.format("evaluating call %s(%s)...\n", func.name, evaluatedArgs.map(_.toString).reduceOption(_ + ", " + _).getOrElse("")))
              StateDebugger.trace("evalTrace")(String.format("where the body is: %s\n", func.body))
              val result = evaluateTypeCorrectExpression(
                func.body,
                applyElse((evaluatedArgs zip func.params).map {
                  case (arg, (name, expectedType)) =>
                    assert(getLiteralType(arg) == expectedType)
                    name -> arg
                } toMap, valuation),
                evaluatedFuncs)
              StateDebugger.trace("evalTrace")(String.format("%s(%s)=%s\n", func.name, evaluatedArgs.map(_.toString).reduceOption(_ + ", " + _).getOrElse(""), result))
              evaluatedFuncs += (func.name, evaluatedArgs) -> result;
              StateDebugger.trace("evalTrace")(String.format("number of buffered functions: %s \n", evaluatedFuncs.size.toString));
              result
            })

        case _ => throw new IllegalArgumentException("The experssion contains " + e)

      }
      result
    }
    evaluate(e)
  }

  def getLiteralType(l: Literal): SingleType = l match {
    case _: IntLiteral => NumberType
    case _: StringLiteral => StringType
    case EnumLiteral(v, t) => t
    case _: RealLiteral => NumberType
    case _: BoolLiteral => BoolType
    case _: SetLiteral => SetType
    // case _:TristateLiteral => TristateType
  }

  // assuming expr is type correct
  def getType(expr: Expression, types: collection.Map[String, Type], getLiteralType: (Literal) => Type = this.getLiteralType): Type = {
    expr match {
      case l: Literal => getLiteralType(l)
      case IdentifierRef(id) => types.getOrElse(id, TypeHelper.anyType)
      case Conditional(c, p, f) => (getType(p, types, getLiteralType) & getType(f, types, getLiteralType)).get
      case Or(l, r) => BoolType
      case And(l, r) => BoolType
      case Implies(l, r) => BoolType
      case Eq(l, r) => BoolType
      case NEq(l, r) => BoolType
      case LessThan(l, r) => BoolType
      case LessThanOrEq(l, r) => BoolType
      case GreaterThanOrEq(l, r) => BoolType
      case GreaterThan(l, r) => BoolType
      case Plus(l, r) => NumberType
      case Minus(l, r) => NumberType
      case Times(l, r) => NumberType
      case Div(l, r) => NumberType
      case Mod(l, r) => NumberType
      case Dot(l, r) => SetType
      case Not(l) => BoolType
      case IsSubstr(l, r) => BoolType
      case ToInt(l) => NumberType
      case ToString(l) => StringType
      case ToBool(l) => BoolType
      case GetData(id) => types.getOrElse(id, TypeHelper.anyType)
      // case _:Min => TristateType
      // case _:Max => TristateType
      case _: IsActive => BoolType
      case _: IsEnabled => BoolType
      case _: IsLoaded => BoolType
      case g:UserFunctionCall => g.func.returnType
    }
  }

  private def containsIdentifier(expr: Expression) = {
    import org.kiama.rewriting.Rewriter._
    var result = false
    rewrite(everywhere(query[Expression] {
      case a: IdentifierRef =>
        result = true
    }))(expr)
    result
  }

  // only apply to type corrected expr
  def removeToBool(expr: Expression, types: Expression.Types): Expression = {
    import org.kiama.rewriting.Rewriter._
    rewrite(everywherebu(rule[Expression] {
      case ToBool(e) => Not(e === TypeHelper.ZeroToType {
        val t = getType(e, types)
        assert(t.isInstanceOf[SingleType], e.toString + ":" + t)
        t.asInstanceOf[SingleType]
      })
    }))(expr)
  }

  // only apply to type corrected expr
  def removeConditional(expr: Expression, types: Expression.Types): Expression = {
    import org.kiama.rewriting.Rewriter._
    def removeSingleBooleanConditional(t: Any): Any = {
      t match {
        case c @ Conditional(b, p, f) if getType(c, types) == BoolType => (b & p) | (Not(b) & f)
        case _ => t
      }
    }

    def removeSingleDataConditional(t: Any): Any = {
      object ContainsConditional {
        def unapply(expr: Expression): Option[Conditional] = {
          var r: Option[Conditional] = None
          everywheretd(query[Conditional]{
            case x: Conditional if r == None => { r = Some(x); x }
          })(t)
          r
        }
      }

      def replaceSingleConditional(src: Conditional, tgt: Expression): PartialFunction[Any, Any] = {
        case e: Conditional if e == src => tgt
      }

      def replaceConditional(src: Conditional, tgt: Expression)(e: Expression): Expression =
        rewrite(everywheretd(rule(replaceSingleConditional(src, tgt))))(e)

      t match {
        case e: Expression if getType(e, types) == BoolType && !e.children.exists(getType(_, types) == BoolType) =>
          e match {
            case ContainsConditional(c @ Conditional(b, p, f)) =>
              (b & replaceConditional(c, p)(e)) |
                (Not(b) & replaceConditional(c, f)(e))
            case _ => e
          }
        case _ => t
      }
    }

    rewrite(everywheretd(rulef(removeSingleBooleanConditional)) <* everywheretd(rulef(removeSingleDataConditional)))(expr)
  }

  def removeBooleanEq(expr: Expression, types: Expression.Types): Expression = {
    import org.kiama.rewriting.Rewriter._
    rewrite(everywherebu(rule[Expression] {
      case Eq(x, y) if getType(x, types) == BoolType && getType(y, types) == BoolType =>
        (x & y) | (Not(x) & Not(y))
    }))(expr)
  }

  def simplifyWithReplacement(expr: Expression, types: Expression.Types): Expression = {
    simplify(removeToBool(removeBooleanEq(removeConditional(simplify(expr), types), types), types))
  }

  // determine if e1 implies e2. Return true means e1 implies e2, otherwise the result is unknown.
  private def isImplication(e1:Expression, e2:Expression):Boolean = {
    val cond1:Option[(Expression, Literal)] = e1 match {
      case Eq(v1, l1:Literal) => Some((v1, l1))
      case Eq(l1:Literal, v1) => Some((v1, l1))
      case _ => None
    }
    if (cond1.isDefined) {
      val (v1, l1) = cond1.get
      e2 match {
        case Not(Eq(v2, l2:Literal)) if v2 == v1 && l1 != l2 => return true
        case Not(Eq(l2:Literal, v2)) if v2 == v1 && l1 != l2 => return true
        case Eq(l2:Literal, v2) if v2 == v1 && l1 == l2 => return true
        case Eq(v2, l2:Literal) if v2 == v1 && l1 == l2 => return true
        case _ =>
      }
    }
    return false
  }

  import org.kiama.rewriting.Rewriter._
  def simplify(expr: Expression): Expression = {
    rewrite(Expression.everywherebu(rule[Expression] {
      case And(_, BoolLiteral(false)) => BoolLiteral(false)
      case And(BoolLiteral(false), _) => BoolLiteral(false)
      case And(BoolLiteral(true), e) => e
      case And(e, BoolLiteral(true)) => e
      case And(e1, e2) if isImplication(e1, e2) => e1
      case And(e1, e2) if isImplication(e2, e1) => e2
      case And(e1, Not(e2)) if e1 == e2 => BoolLiteral(false)
      case And(Not(e1), e2) if e1 == e2 => BoolLiteral(false)
      case Or(_, BoolLiteral(true)) => BoolLiteral(true)
      case Or(BoolLiteral(true), _) => BoolLiteral(true)
      case Or(BoolLiteral(false), e) => e
      case Or(e, BoolLiteral(false)) => e
      case Or(e1, e2) if (isImplication(e1, e2)) => e2
      case Or(e1, e2) if (isImplication(e2, e1)) => e1
      case Conditional(BoolLiteral(true), pass, fail) => pass
      case Conditional(BoolLiteral(false), pass, fail) => fail
      case Conditional(Eq(v0, l0), l1, v1) if v0 == v1 && l0 == l1
        => v0 // parttern specially designed for kconfig expressions
      case Eq(i1, BoolLiteral(true)) => i1
      case Eq(i1, BoolLiteral(false)) => Not(i1)
      case Plus(IntLiteral(0), e) => e
      case Plus(e, IntLiteral(0)) => e
      case Conditional(e1, e2, BoolLiteral(false)) if e1 == e2 | e2 == BoolLiteral(true) => e1
      case Conditional(e1, BoolLiteral(false), BoolLiteral(true)) => Not(e1)
      case Conditional(e1, BoolLiteral(true), BoolLiteral(false)) => (e1)
      case Conditional(e1, e2, e3) if e2 == e3 => e2
      case GreaterThan(IntLiteral(i1), IntLiteral(i2)) =>
        if (i1 > i2) BoolLiteral(true)
        else BoolLiteral(false)
      case GreaterThanOrEq(IntLiteral(i1), IntLiteral(i2)) =>
        if (i1 >= i2) BoolLiteral(true)
        else BoolLiteral(false)
      case LessThan(IntLiteral(i1), IntLiteral(i2)) =>
        if (i1 < i2) BoolLiteral(true)
        else BoolLiteral(false)
      case LessThanOrEq(IntLiteral(i1), IntLiteral(i2)) =>
        if (i1 <= i2) BoolLiteral(true)
        else BoolLiteral(false)
      case Eq(i1: Literal, i2: Literal) =>
        if (i1 == i2) BoolLiteral(true)
        else BoolLiteral(false)
      case IsSubstr(SetLiteral(l), SetLiteral(r)) => BoolLiteral(r.subsetOf(l))
      // case IsSubstr(StringLiteral(whole), StringLiteral(part)) =>
      // if (whole.containsSlice(part))  BoolLiteral(true)
      // else  BoolLiteral(false)
      case Implies(l, r) => simplify(Or(Not(l), r))
      case NEq(l, r) => simplify(Not(Eq(l, r)))
      case Plus(IntLiteral(l), IntLiteral(r)) => IntLiteral(l + r)
      case Minus(IntLiteral(l), IntLiteral(r)) => IntLiteral(l - r)
      case Times(IntLiteral(l), IntLiteral(r)) => IntLiteral(l * r)
      case Div(IntLiteral(l), IntLiteral(r)) => IntLiteral(l / r)
      case Mod(IntLiteral(l), IntLiteral(r)) => IntLiteral(l % r)
      // case Dot(StringLiteral(l), StringLiteral(r)) => StringLiteral(l + r)
      case Dot(SetLiteral(l), SetLiteral(r)) => SetLiteral(l ++ r)
      case Not(Not(e)) => e
      case Not(BoolLiteral(b)) => BoolLiteral(!b)
      case ToInt(i: Literal) => IntLiteral(toInt(i))
      case ToString(i: Literal) => StringLiteral(toString(i))
      case ToBool(i: Literal) => BoolLiteral(toBoolean(i))
      // case Max(TristateLiteral(Tristate.Yes), _) => TristateLiteral(Tristate.Yes)
      // case Max(_, TristateLiteral(Tristate.Yes)) => TristateLiteral(Tristate.Yes)
      // case Max(TristateLiteral(Tristate.No), r) => r
      // case Max(l, TristateLiteral(Tristate.No)) => l
      // case Max(TristateLiteral(Tristate.Mod), TristateLiteral(Tristate.Mod)) => TristateLiteral(Tristate.Mod)
      // case Min(TristateLiteral(Tristate.No), _) => TristateLiteral(Tristate.No)
      // case Min(_, TristateLiteral(Tristate.No)) => TristateLiteral(Tristate.No)
      // case Min(TristateLiteral(Tristate.Yes), r) => r
      // case Min(l, TristateLiteral(Tristate.Yes)) => l
      // case Min(TristateLiteral(Tristate.Mod), TristateLiteral(Tristate.Mod)) => TristateLiteral(Tristate.Mod)
    }))(expr)
  }
}
