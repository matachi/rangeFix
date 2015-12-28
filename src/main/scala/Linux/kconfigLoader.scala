package ca.uwaterloo.gsd.rangeFix
import gsd.linux._
import Kconfig._

class KconfigLoader(modelFile:String, configPath:String) extends ModelLoader {
  private[this] val parsedModel = KConfigParser.parseKConfigFile(modelFile)
  private[this] val kc = new Kconfig2(parsedModel)
  private[this] val choices = kc.choiceConstraints.map(x => ChoiceConstraint(x.toExpression))
  private[this] val domains = kc.configDomains.filter{case (id, expr) => expr != BoolLiteral(true)}.map{case (id, expr) => DomainConstraint(expr, id)}
  private[this] val indexedIds = kc.varTypes.keys.toIndexedSeq
  private[this] val allEffectives = indexedIds.map(x =>  EffectiveExpr(kc.effective(x).toExpression, x))
  private val _effectiveMap = indexedIds zip (0 until indexedIds.size) toMap
  private val _allExpressions = choices.toIndexedSeq[ConstraintWithSource] ++ domains ++ allEffectives
  private val _reqConstraintSize = choices.size + domains.size
  def defaults(t:Type) = t match {
    case _:EnumType => Kconfig.tristateNo
    case NumberType => IntLiteral(0)
    case StringType => StringLiteral("")
    case BoolType => BoolLiteral(false)
  }
  private var _valuation:Map[String, Literal] = kc.varTypes.map{case (k,t)=>(k, defaults(t))}.toMap ++ (DotConfigParser.parseFile(configPath) map {
    case (k, v) =>
      if (kc.varTypes.contains(encode(k)) && kc.varTypes(encode(k))==BoolType){
        val literal = v match{
          case TYes=>BoolLiteral(true)
          case _=>BoolLiteral(false)
        }
        encode(k)->literal
      }
      else Kconfig.encode(k) -> v.toExpression})
  override def allExpressions:IndexedSeq[ConstraintWithSource] = _allExpressions
  override def reqConstraintSize:Int = _reqConstraintSize
  override def valuation:Map[String, Literal] = _valuation
  
  def getEffectiveIndex(id:String):Option[Int] = {
    _effectiveMap.get(id).map(_+_reqConstraintSize)
  }
  def getVarType(id:String):Option[Type]={
    if (kc.varTypes contains id)
      return Some(kc.varTypes (id) )
    return None;
  }
  def modifyConfiguration(changes:Map[String,Literal]){
    val fr = new java.io.FileWriter("log2.txt", true)
    fr.write("-----changes:\n%s\n" format changes.toString)
    fr.close
    _valuation = _valuation ++ changes
  }
}

/*package ca.uwaterloo.gsd.rangeFix
import gsd.linux._
import Expression._
import scala.collection.immutable.TreeMap  
import scala.collection.immutable.Map
import Kconfig._
class KconfigLoader(modelFile:String, configPath:String, kconfigLoaderInfo:Option[Tuple5[IndexedSeq[ConstraintWithSource],
                                                                            Int,
                                                                            Map[String,Int],
                                                                            Expression.Types,
                                                                                         Map[String,Literal]]]=None) extends ModelLoader {
  private [this] val info = if (kconfigLoaderInfo!=None)
                              kconfigLoaderInfo.get
                            else{
    val parsedModel = KConfigParser.parseKConfigFile(modelFile)
    val kc = new Kconfig2(parsedModel)
    val choices = kc.choiceConstraints.map(x => ChoiceConstraint(x.toExpression))
    val domains = kc.configDomains.filter{case (id, expr) => expr != BoolLiteral(true)}.map{case (id, expr) => DomainConstraint(expr, id)}
    val indexedIds = kc.varTypes.keys.toIndexedSeq
    val allEffectives = indexedIds.map(x =>  EffectiveExpr(kc.effective(x).toExpression, x))
    val _effectiveMap = indexedIds zip (0 until indexedIds.size) toMap
    val _allExpressions = choices.toIndexedSeq[ConstraintWithSource] ++ domains ++ allEffectives
    val _reqConstraintSize = choices.size + domains.size
    (_allExpressions,
     _reqConstraintSize,
     _effectiveMap,
     kc.varTypes,
     kc.varTypes.map{case (k,t)=>(k, defaults(t))}.toMap ++ (DotConfigParser.parseFile(configPath) map { case (k, v) => {
       if (( kc.varTypes contains encode(k)) && kc.varTypes(encode(k)) == BoolType){
         val fixedVal = v match{
           case TYes => BoolTrue
           case _=> BoolFalse
         }
         Kconfig.encode(k) -> fixedVal.toExpression
       }
       else Kconfig.encode(k) -> v.toExpression }}))
  }
  private val _effectiveMap = info._3
  private val _allExpressions = info._1;
  private val _reqConstraintSize = info._2;
  private val _varTypes = info._4;
  def defaults(t:Type) = t match {
    case _:EnumType => Kconfig.tristateNo
    case NumberType => IntLiteral(0)
    case StringType => StringLiteral("")
    case BoolType => BoolLiteral(false)
  }
  private var _valuation:Map[String, Literal] = info._5
  override def allExpressions:IndexedSeq[ConstraintWithSource] = _allExpressions
  override def reqConstraintSize:Int = _reqConstraintSize
  override def valuation:Map[String, Literal] = _valuation
  def getEffectiveIndex(id:String):Option[Int] = {
    _effectiveMap.get(id).map(_+_reqConstraintSize)
  }
  def getVarType(id:String):Option[Type]={
    if (_varTypes contains id)
      Some(_varTypes (id) )
    else None
  }
  def modifyConfiguration(modifications: Map[String, Literal]){
    _valuation = _valuation ++ modifications
    //new KconfigLoader("","",Some( (info._1, info._2, info._3, info._4, info._5++modifications)))
  }
}
*/
  /*class KconfigLoader ( 
    val _allExpressions:IndexedSeq[ConstraintWithSource],
    val _reqConstraintSize:Int,
    val _effectiveMap:Map[String,Int],
    val kcVarTypes:collection.Map[String, SingleType],
    val _valuation:Map[String,Literal]
  ) extends ModelLoader {
    
    def create(modelFile:String, configPath:String):KconfigLoader=
    {
      val parsedModel = KConfigParser.parseKConfigFile(modelFile)
      val kc = new Kconfig2(parsedModel)
      val choices = kc.choiceConstraints.map(x => ChoiceConstraint(x.toExpression))
      val domains = kc.configDomains.filter{case (id, expr) => expr != BoolLiteral(true)}.map{case (id, expr) => DomainConstraint(expr, id)}
      println ("------------%s" format domains.size)
      val indexedIds = kc.varTypes.keys.toIndexedSeq
      val allEffectives = indexedIds.map(x =>  EffectiveExpr(kc.effective(x).toExpression, x))
      val _effectiveMap = indexedIds zip (0 until indexedIds.size) toMap
      val _allExpressions = choices.toIndexedSeq[ConstraintWithSource] ++ domains ++ allEffectives
      val _reqConstraintSize = choices.size + domains.size
      val _valuation:Map[String, Literal] = kc.varTypes.map{case (k,t)=>(k, defaults(t))}.toMap ++ (DotConfigParser.parseFile(configPath) map { case (k, v) => Kconfig.encode(k) -> v.toExpression })
      new KconfigLoader( 
        _allExpressions,
        _reqConstraintSize,
        _effectiveMap,
        kc.varTypes,
        _valuation
      ) 
    }
    def this()=this(IndexedSeq(),0,Map(),Map(),Map())


    def defaults(t:Type) = t match {
      case _:EnumType => Kconfig.tristateNo
      case NumberType => IntLiteral(0)
      case StringType => StringLiteral("")
    }

    
    override def allExpressions:IndexedSeq[ConstraintWithSource] = _allExpressions
    override def reqConstraintSize:Int = _reqConstraintSize
    override def valuation:Map[String, Literal] = _valuation

    def getEffectiveIndex(id:String):Option[Int] = {
      
      _effectiveMap.get(id).map(_+_reqConstraintSize)
    }
    def getVarType(id:String):Option[Type]={
      if (kcVarTypes contains id)
        return Some(kcVarTypes (id) )
      return None;
    }
    def getVariableValue(key:String):Literal={
      return _valuation(key)
    }

    def modifyConfiguration(myKey:String,myValue:Literal):KconfigLoader={
      new KconfigLoader(//鐢熸垚鏂扮殑loader
        _allExpressions,
        _reqConstraintSize,
        _effectiveMap,
        kcVarTypes,
        _valuation++Map(myKey->myValue)

      )
      
    }
    
  }
  */
