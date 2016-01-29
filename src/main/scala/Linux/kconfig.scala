package ca.uwaterloo.gsd.rangeFix

import Expression._

import Kconfig._
import collection._
import org.kiama.rewriting.Rewriter
import gsd.linux.{ KConfigParser, AChoice, And => KAnd, Or => KOr, Eq => KEq, NEq => KNEq, Not => KNot, NonCanonEq, Group, Id, KHex, KInt, Literal => KLiteral, Yes, No, Mod => Module, KExpr, KBoolType, KTriType, KIntType, KHexType, KStringType, AbstractKConfig, AConfig, ADefault }

object Kconfig {

  // encode the names of the identifiers so that they match usual convention
  def encode(n:String):String = // "_" + n
    if (n.size == 0 || n(0).isDigit) "_" + n else n


  // null int is actually empty string. But since the only operators between integers are "==" and "!=", we can model empty string as Long.MinValue since that value is almost never used
  val INT_NULL_VALUE = Long.MinValue 
  val INT_NULL_LITERAL = TInt(INT_NULL_VALUE)
  val GEN_PREFIX = "__GEN_"
  val tristateType = EnumType(immutable.ListSet(IntLiteral(0), IntLiteral(1), IntLiteral(2)))
  val tristateNo = EnumLiteral(IntLiteral(0), tristateType)
  val tristateMod = EnumLiteral(IntLiteral(1), tristateType)
  val tristateYes = EnumLiteral(IntLiteral(2), tristateType)
  val maxFunc = {
    val a = GEN_PREFIX + "param1"
    val b = GEN_PREFIX + "param2"
    new FunctionDef(
        "__max",
        List((a, tristateType), (b, tristateType)),
        tristateType,
        Conditional(
          a === tristateNo,
          b,
          Conditional(
            a === tristateYes,
            a,
            Conditional(
              b === tristateYes,
              b,
              a)
          )
        ))
  }

  val minFunc = {
    val a = GEN_PREFIX + "param1"
    val b = GEN_PREFIX + "param2"
    new FunctionDef(
          "__min",
          List((a, tristateType), (b, tristateType)),
          tristateType,
          Conditional(
            a === tristateYes,
            b,
            Conditional(
              a === tristateNo,
              a,
              Conditional(
                b === tristateNo,
                b,
                a)
            )
          ))
  }
  val bool2tristateFunc = {
    val a = GEN_PREFIX + "param1"
    new FunctionDef(
      "__bool2tristate",
      List((a,BoolType)),
      tristateType,
      Conditional(a, tristateYes, tristateNo)
    )
  }
  def bool2tristate (a:BoolLiteral)=
    UserFunctionCall(bool2tristateFunc, List(a))
  def tristateMax(a:Expression, b:Expression) =
    UserFunctionCall(maxFunc, List(a, b))
  def tristateMin(a:Expression, b:Expression) = 
      UserFunctionCall(minFunc, List(a, b))

  
  import MyRewriter._
  import Rewriter.rule
  import org.kiama.rewriting
  import org.kiama.rewriting.Rewriter.rewrite

  private lazy val bypassFuncDef = 
    rule[Any] {
      case x:TFuncDef => x
    }
  
  def rewritetdBypassingFuncDefs[T](s : => rewriting.Strategy):T=>T = {
    rewrite(everywheretdWithGuard(bypassFuncDef, s))
  }
  def rewritebuBypassingFuncDefs[T](s : => rewriting.Strategy):T=>T = {
    rewrite(everywherebuWithGuard(bypassFuncDef, s))
  }



  def warn(msg: String) {
    System.err.println(msg)
  }
  def error(msg: String) {
    System.err.println(msg)
  }

  sealed trait TType extends SingleType {
    def toType:SingleType
  }
  case object TTristateType extends TType {
    override def toType = tristateType
  }
  case object TStringType extends TType {
    override def toType = StringType
  }
  case object TIntType extends TType {
    override def toType = NumberType
  }
  case object TBoolType extends TType{
    override def toType = BoolType
  }
  sealed abstract class BoolExpr extends TExpr(TBoolType) {
    override def toExpression: Expression
    override lazy val e = toExpression

    def |(o: BoolExpr): BoolExpr = BOr(this, o)
    def ==>(o: BoolExpr): BoolExpr = BImplies(this, o)

  }
  case class BId(name:String) extends BoolExpr{
    override def toExpression = IdentifierRef(name)
    override def toString = name
  }
  case object BoolTrue extends BoolExpr{
    override def toExpression = BoolLiteral(true)
    override def toString = "T"
  }

  case object BoolFalse extends BoolExpr {
    override def toExpression = BoolLiteral(false)
    override def toString = "F"
  }

  case class BGt(l: TExpr, r: TExpr) extends BoolExpr {
    assert(false, "BGT found")
    assert(l.t == TTristateType && r.t == TTristateType || l.t != TTristateType && r.t != TTristateType)
    override def toExpression = l.t match {
      case TTristateType => (tristateMax(l.e, r.e) === l.e) & (l.e !== r.e)
      case _ => l.e > r.e
    }
    override def toString = "(" + l + " > " + r + ")"
  }
  case class BGte(l: TExpr, r: TExpr) extends BoolExpr {
    assert(false, "BGte found")
    assert(l.t == TTristateType && r.t == TTristateType || l.t != TTristateType && r.t != TTristateType)
    override def toExpression = l.t match {
      case TTristateType => tristateMax(l.e, r.e) === l.e
      case _ => l.e >= r.e
    }
    override def toString = "(" + l + " >= " + r + ")"
  }
  case class BLt(l: TExpr, r: TExpr) extends BoolExpr {
    assert(false, "BLt found")
    assert(l.t == TTristateType && r.t == TTristateType || l.t != TTristateType && r.t != TTristateType)
    override def toExpression = l.t match {
      case TTristateType => (tristateMax(l.e, r.e) === r.e) & (l.e !== r.e)
      case _ => l.e < r.e
    }
    override def toString = "(" + l + " < " + r + ")"
  }
  case class BLte(l: TExpr, r: TExpr) extends BoolExpr {
    assert(false, "BLte found")
    assert(l.t == TTristateType && r.t == TTristateType || l.t != TTristateType && r.t != TTristateType)
    override def toExpression = l.t match {
      case TTristateType => tristateMax(l.e, r.e) === r.e
      case _ => l.e <= r.e
    }
    override def toString = "(" + l + " <= " + r + ")"
  }
  case class BOr(l: BoolExpr, r: BoolExpr) extends BoolExpr {
    override def toExpression = l.e | r.e
    override def toString = "(" + l + " || " + r + ")"
  }
  case class BEq(l: TExpr, r: TExpr) extends BoolExpr{
    getBinaryOpExprType(l.t,r.t)
//    assert(l.t == r.t, String.format("type mismatch: %s:%s<->%s:%s", l, l.t, r, r.t))
    override def toExpression = l.t match{
      case r.t => l.e===r.e
      case TBoolType => {
        assert (r.t==TTristateType)
        Conditional(l.e, r.e===tristateYes, r.e===tristateNo)
      }
      case TTristateType=>{
        assert(r.t==TBoolType)
        Conditional(r.e, l.e===tristateYes, r.e==tristateNo)
      }
      case _ => {assert(false); BoolLiteral(false)}
    }
    override def toString = "(" + l + " == " + r + ")"
  }
  case class BNeq(l: TExpr, r: TExpr) extends BoolExpr {
    //assert(l.t == r.t)
    getBinaryOpExprType(l.t,r.t)
    override def toExpression = l.t match{
      case r.t => l.e!==r.e
      case TBoolType=>{
        assert(r.t==TTristateType)
        Conditional(l.e, r.e!==tristateYes, r.e!==tristateNo)
      }
      case TTristateType=>{
        assert(r.t==TBoolType)
        Conditional(r.e, l.e!==tristateYes, l.e!==tristateNo)
      }
      case _=> {assert(false); BoolLiteral(false)}
    }
    override def toString = "(" + l + " != " + r + ")"
  }
  case class BImplies(l: BoolExpr, r: BoolExpr) extends BoolExpr {
    override def toExpression = l.e ==> r.e
    override def toString = "(" + l + " ==> " + r + ")"
  }
  case class BConditional(cond: BoolExpr, success: BoolExpr, fail: BoolExpr) extends BoolExpr {
    assert(false, "BConditional found")
    override def toExpression = Conditional(cond.e, success.e, fail.e)
    override def toString = "(if " + cond + " then " + success + " else " + fail + ")"
  }

  // Models tristate expressions with values of 0, 1, 2
  sealed abstract class TExpr(val t: TType) {
    def toExpression: Expression
    lazy val e = toExpression

    def unary_! = TNot(this)

    def beq(o: TExpr) = BEq(this, o)
    def bneq(o: TExpr) = BNeq(this, o)
    def teq(o: TExpr) = TristateEq(this, o)

    def >(o: TExpr): BoolExpr = BGt(this, o)
    def >=(o: TExpr): BoolExpr = BGte(this, o)
    def <(o: TExpr): BoolExpr = BLt(this, o)
    def <=(o: TExpr): BoolExpr = BLte(this, o)
  }

  sealed abstract class TLiteral(override val t: TType) extends TExpr(t) {
    override def toExpression: Literal
  }

  case object TYes extends TLiteral(TTristateType) {
    override def toExpression = tristateYes
  }
  case object TMod extends TLiteral(TTristateType) {
    override def toExpression = tristateMod
  }
  case object TNo extends TLiteral(TTristateType) {
    override def toExpression = tristateNo
  }

  case class TId(name: String, override val t: TType) extends TExpr(t) {
    override def toExpression = IdentifierRef(encode(name))
    override def toString = name
  }

  case class TInt(value: Long) extends TLiteral(TIntType) {
    override def toExpression = IntLiteral(value)
    override def toString = value.toString
  }

  case class TString(value: String) extends TLiteral(TStringType) {
    override def toExpression = StringLiteral(value)
  }
  //return TType of a binary TExpr (eg. TMax, TMin)
  def getBinaryOpExprType(lt: TType, rt: TType):TType = {
    lazy val assertString = String.format("type mismatch in binary operation: %s -> %s", lt, rt)
    lt match {
      case TTristateType => {
        assert (rt==TBoolType||rt==TTristateType, assertString)
        TTristateType
      }
      case TBoolType =>{
        assert (rt==TBoolType||rt==TTristateType, assertString)
        rt
      }
      case _ =>{
        assert(lt == rt, assertString)
        lt
      }
    }
  }
  case class TMin(l: TExpr, r: TExpr) extends TExpr(getBinaryOpExprType(l.t,r.t)) {
    override def toExpression = l.t match {
      case TTristateType => r.t match{
        case TTristateType => tristateMin(l.e, r.e)
        case TBoolType => Conditional(r.e, l.e, tristateNo)
        case _ => {assert(false); BoolLiteral(false)}
      }
      case TBoolType => r.t match{
        case TBoolType => l.e & r.e
        case TTristateType => Conditional(l.e, r.e, tristateNo)
        case _ => {assert(false); BoolLiteral(false)}
      }
      case TIntType => Conditional(l.e < r.e, l.e, r.e)
      case TStringType => assert(false); TNo.e
    }
  }
  case class TMax(l: TExpr, r: TExpr) extends TExpr(getBinaryOpExprType(l.t, r.t)) {
    override def toExpression = l.t match {
      case TTristateType => r.t match{
        case TTristateType => tristateMax(l.e, r.e)
        case TBoolType => Conditional(r.e, tristateYes, l.e)
        case _ => {assert(false); BoolLiteral(false)}
      }
      case TBoolType => r.t match {
        case TBoolType => l.e | r.e
        case TTristateType => Conditional(l.e, tristateYes, r.e)
        case _ => {assert(false); BoolLiteral(false)}
      }
      case TIntType => Conditional(l.e > r.e, l.e, r.e)
      case TStringType => assert(false); TNo.e
    }
  }

  case class TNot(l: TExpr) extends TExpr(l.t) {
    assert(l.t == TTristateType || l.t==TBoolType, String.format("l.t=%s, l=%s",l.t, l))
    override def toExpression = l.t match{
      case TBoolType => !l.e
      case TTristateType=> Conditional(l.e === tristateYes, tristateNo,
                                       Conditional(l.e === tristateMod, tristateMod, tristateYes))
      case _ => {assert(false); BoolLiteral(false)}
    }
  }

  case class TristateEq(l: TExpr, r: TExpr) extends TExpr(TBoolType) {
    override def toExpression = l.t match{
      case TTristateType => r.t match{
        case TTristateType => Conditional(l.e === r.e, BoolLiteral(true), BoolLiteral(false))
        case TBoolType => Conditional(l.e === Conditional(r.e,tristateYes, tristateNo),BoolLiteral(true),BoolLiteral(false))
        case _ => {assert(false); BoolLiteral(false)}
      }
      case TBoolType => r.t match {
        case TTristateType => Conditional(r.e ===Conditional(l.e, tristateYes, tristateNo), BoolLiteral(true), BoolLiteral(false))
        case TBoolType => l.e === r.e
        case _=>{assert(false); BoolLiteral(false)}
      }
      case _ => l.e === r.e
    }
  }
  case class TConditional(cond: TExpr, success: TExpr, fail: TExpr) extends TExpr(getBinaryOpExprType(success.t, fail.t)) {
    //assert(success.t == fail.t)
    assert (cond.t==TBoolType)
    override def toExpression = Conditional(cond.e, success.e, fail.e)
    override def toString = "(if " + cond + " then " + success + " else " + fail + ")"
  }

  case class TFuncCall(func:TFuncDef, args:Seq[TExpr]) extends TExpr(func.body.t) {
    override def toExpression = UserFunctionCall(func.toFunctionDef, args.map(_.toExpression))
    override def toString = func.name + "(" + args.map(_.toString).reduceOption(_ + _).getOrElse("") + ")"
  }  

  case class TFuncDef(name:String, params:Seq[(String, TType)], body:TExpr) {
    lazy val toFunctionDef:FunctionDef = new FunctionDef(encode(name), params.map{case (s, t)=>(s, t.toType)}, body.t.toType, body.toExpression)
    override def toString = name + "(" + params.map(_.toString).reduceOption(_ + _).getOrElse("") + ") = " + body.toString
  }

}

class ExpressionTypeChecker {

  private def isInt(s: String) =
    try {
      Integer.parseInt(s)
      true
    } catch {
      case _ => false
    }

  private def isHex(s: String) =
    try {
      Integer.parseInt(s.substring(2), 16)
      s startsWith "0x"
    } catch {
      case _ => false
    }

  var numErrors = 0

  import Rewriter._

  lazy val fixEq =
    rule[Any] {
      // TODO
      case BEq(l, TString("")) if l.t == TIntType =>
        warn("Mismatch BEq (null) %s:%s = \"\"".format(l, l.t))
        numErrors += 1
        BEq(l, INT_NULL_LITERAL)

      // Fix problem with the kconfig infrastructure outputting the right hand side as a config
      // TODO verify this in the configurator
      case BEq(l, TId(x, TTristateType)) if l.t == TIntType && isHex(x) =>
        warn("Mismatch BEq (hex)  %s = %s".format(l, x))
        numErrors += 1
        BEq(l, TInt(Integer.parseInt(x, 16)))

      case BEq(l @ TId(_, TTristateType), r @ TInt(v)) if v >= 0 && v <= 2 =>
        warn("Mismatch BEq (int)  %s:%s = %s:%s".format(l, l.t, r, r.t))
        numErrors += 1
        BEq(l, v match {
          case 0 => TNo
          case 1 => TMod
          case 2 => TYes
        })

      case BEq(l, r) if l.t != r.t =>
        error("Mismatch BEq        %s:%s = %s:%s".format(l, l.t, r, r.t))
        numErrors += 1
        BoolTrue

      // Fix problem with the kconfig infrastructure outputting the right hand side as a string
      // TODO verify this in the configurator
      case TristateEq(l, TString(x)) if l.t == TIntType && isInt(x) =>
        warn("Mismatch TEq (int)  %s = %s".format(l, x))
        numErrors += 1
        TristateEq(l, TInt(x.toInt))

      case TristateEq(l, TString(x)) if l.t == TTristateType =>
        warn("Mismatch TEq (tri)  %s = %s".format(l, x))
        numErrors += 1
        TristateEq(l, TId(x, TTristateType))

      case TristateEq(l, r) if l.t != r.t =>
        error("Mismatch TEq        %s = %s".format(l, r))
        numErrors += 1
        TYes

    }

  // lazy val fixAnd =
  //   rule {
  //     // Fixes problem with STMMAC_TIMER depends on RTC_HCTOSYS_DEVICE
  //     // where RTC_HTCOSYS_DEVICE is a string config
  //     // TODO verify this in the configurator
  //     case TMin(l, r) if l.t != TTristateType =>
  //       warn("And (not tri)       %s".format(l))
  //       numErrors += 1
  //       TMin(TristateEq(l, TString("")), r)

  //     case TMin(l, r) if r.t != TTristateType =>
  //       warn("And (not tri)       %s".format(r))
  //       numErrors += 1
  //       TMin(l, TristateEq(l, TString("")))
  //   }


  def fixTypeErrors(e: BoolExpr): BoolExpr = {
    rewritetdBypassingFuncDefs(fixEq)(e)
  }

  // FIXME hack
  def fixTypeErrors(e: TExpr): TExpr = {
    rewritetdBypassingFuncDefs(fixEq)(e)
  }

}

class Kconfig2(val ak: AbstractKConfig) {
  private val akMap = ak.configs.map { case x @ AConfig(_, name, _, _, _, _, _, _, _) => name -> x }.toMap

  private val configTypes: Map[String, TType] = {
    ak.configs map {
      case AConfig(_, name, ktype, _, _, _, _, _, _) =>

        val configType: TType = ktype match {
          case KBoolType => TBoolType
          case KTriType => TTristateType
          case KIntType => TIntType
          case KHexType => TIntType
          case KStringType => TStringType
        }

        // return a tuple of the config name to it's rangefix type
        name -> configType
    } toMap
  } withDefaultValue (TTristateType) // FIXME returns TTristate for unknown configs

  def undefinedValue(t: TType): TExpr = t match {
    case TBoolType => BoolFalse
    case TTristateType => TNo
    case TStringType => TString("")
    case TIntType => INT_NULL_LITERAL
  }

  def id(name: String): TId =
    TId(name, configTypes(name))

  def simplify(t: TExpr): TExpr = {
    import Rewriter._
    val sRlt = Simplifier.simplify(t)
    val rlt = sRlt.t match{
      case t.t => sRlt
      case TBoolType if t.t==TTristateType => guaranteeTristate(sRlt)
      case TTristateType if t.t == TBoolType => guaranteeBool(sRlt)
      case _=>
        assert(false)
        sRlt
    }
    rlt
  }

  private def isHex(s: String) =
    try {
      if (s startsWith "0x") {
        Integer.parseInt(s.substring(2), 16)
      }
      else {
        Integer.parseInt(s, 16)
      }
      true
    } catch {
      case _ => false
    }

  private def isInt(s: String) =
    try {
      Integer.parseInt(s)
      true
    } catch {
      case _ => false
    }
  /**
   * some bugs exists when bool and trisate mixed (haven't happened yet)
   */
  private def fixEqualType(e1: TExpr,
                           e2: TExpr,
                           firstCall:Boolean=true):(TExpr, TExpr) = 
  if (e1.t == e2.t) (e1, e2)
  else (e1, e2) match {
    case (_, TString(x)) if e1.t == TIntType && isInt(x) => 
      (e1, TInt(Integer.parseInt(x)))
    case (_, TString(x)) if e1.t == TTristateType =>
      (e1, TId(x, TTristateType))
      
    case _ => 
      if (firstCall) fixEqualType(e2, e1, false)
      else {
        //assert(false, String.format("%s:%s, %s:%s", e1, e1.t, e2, e2.t))
              (INT_NULL_LITERAL, INT_NULL_LITERAL)
      }
  }
  
  private def createFixedTEq(e1:TExpr, e2:TExpr):TExpr = {
    val (fixed1, fixed2) = fixEqualType(e1, e2)
    fixed1 teq fixed2
  }

  private def translateAndFixKInt(e: KExpr):TExpr = {
    e match {
      case KLiteral("") => INT_NULL_LITERAL
      case Id(x) if !akMap.contains(x) && isHex(x) => TInt(Integer.parseInt(x, 16))
      case _ => 
        val result = translate(e)
        if (result.t == TIntType)
          result
        else
          {assert(false, String.format("%s:%s", e, result.t))
           INT_NULL_LITERAL}
    }
  }

  private def fixTristate(e: TExpr):TExpr = {
    if (e.t == TTristateType) e
    else TNo
  }
  // private def fixMisidentifiedInt(e: TExpr):TExpr = {
  //   if (e.t == TIntType) e
  //   else e match {
  //     case TString("") => INT_NULL_LITERAL
  //     case TString(x) if isHex(x) => TInt(Integer.parseInt(x, 16))
  //     case _ => assert(false, String.format("%s:%s", e, e.t)); INT_NULL_LITERAL
  //   }
  // }

  def toBool(e: TExpr): TExpr = {
    assert(e.t == TTristateType)
    TConditional(e bneq TNo, BoolTrue, BoolFalse)
  }

  def inRange(v: TExpr, lower: TExpr, upper: TExpr) = TMax(TMin(v, upper), lower)
  def constructLazyExpr (
    getExpectedType: AConfig => TType, 
    valueName: String, 
    defaultValue: Option[TExpr] = None) (
    f: AConfig => TExpr):String=>TExpr =
      new LazyMap[String, TExpr] ({ name: String =>
        cycleChecker.checkCycle(name + "#0" + valueName) {
          StateDebugger.trace("KConfigTranslation")("generating " + name + "#" + valueName + "...\n")
          val optConfig = akMap.get(name)
          val result = if (optConfig.isDefined) {
            val config = optConfig.get
            val body:TExpr = simplify(f(config))
            assert(body.t==getExpectedType(config))
            var result: TExpr = TFuncCall(TFuncDef(name + "__" + valueName, List(), body), List())
            val expectedType = getExpectedType(config)
            assert(result.t == expectedType, String.format("expected:%s, found:%s", expectedType, result.t))
            result
          } else if (defaultValue.isDefined) defaultValue.get
            else { assert(false); TNo }
          // StateDebugger.trace("KConfigTranslation")(name + "#" + valueName + "=" + result + "\n")
          result
        }
      })

  val inherited = constructLazyExpr(_=>TTristateType, "inherited") {
    config: AConfig =>{
      val rlt = guaranteeTristate(translate(config.inherited))
      assert(rlt.t == TTristateType,String.format("expected: %s, found:%s", TTristateType,rlt.t))
      rlt
    }
  } 

  val cycleChecker = new CycleChecker()


  val effective : String=>TExpr =
    constructLazyExpr(configTypes apply _.name, "effective", Some(TNo)) { c: AConfig =>
      val innerResult = TConditional(inherited(c.name) beq TNo, default(c.name), rangedUserValue(c.name))
      assert(innerResult.t == configTypes(c.name))
      c.ktype match {
        case KBoolType => innerResult
        case _ => innerResult
      }
    }
  def guaranteeBool(expr:TExpr):TExpr={
      assert(expr.t == TBoolType || expr.t==TTristateType, String.format("Cannot change %s to TBoolType", expr.t))
      val result = expr.t match{
        case TBoolType => expr
        case TTristateType => BNeq(expr, TNo)
        case _ => BoolFalse
      }
      assert(result.t==TBoolType)
      result
    }
  def guaranteeTristate(expr:TExpr):TExpr={
    assert(expr.t == TBoolType || expr.t==TTristateType, String.format("Cannot change %s to TBoolType", expr.t))
    val result = expr.t match{
      case TTristateType => expr
      case TBoolType => TConditional(expr, TYes,TNo)
      case _ => BoolFalse
    }
    result
  }
  val default = constructLazyExpr(configTypes apply _.name, "default") { c: AConfig =>
    assert(!c.defs.isEmpty)
    def mkDefaults(in: List[ADefault]): TExpr = in match {
      // The first case is only for the integrity of the expression.
      // Steven's code always generate a null value with condition
      // TYes at the end of default list
      case Nil => undefinedValue(configTypes(c.name))
      case ADefault(value, prevConds, currCond) :: tail =>

        val condition = translate(currCond)
       // assert(condition.t == TTristateType)
        assert(tail != Nil || condition == TYes)

        TConditional(
          condition bneq TNo,
          c.ktype match {
            case KBoolType =>
              val defaultValue = guaranteeBool(translate(value))
              assert(defaultValue.t == TBoolType, String.format("required: KBoolType, found:%s. Expr:%s", defaultValue.t, defaultValue))
              val rlt = inRange(defaultValue, guaranteeBool(lowerBound(c.name)), guaranteeBool(condition))
              assert (rlt.t == TBoolType, String.format("%s found", rlt.t))
              rlt
            case KTriType =>
              val defaultValue = guaranteeTristate(translate(value))
              assert(defaultValue.t == TTristateType)
              inRange(defaultValue, lowerBound(c.name), condition)
            case KIntType | KHexType =>
              val defaultValue = translateAndFixKInt(value)
              assert(defaultValue.t == TIntType,
                     String.format("%s:%s@%s from %s",
                                   defaultValue,
                                   defaultValue.t,
                                   c.name,
                                   value))
              inRange(defaultValue, lowerBound(c.name), upperBound(c.name))
            case _ =>
              val defaultValue = translate(value)             
              assert(defaultValue.t == TStringType,
                     String.format("%s:%s@%s from %s",
                                   defaultValue,
                                   defaultValue.t,
                                   c.name,
                                   value))
              defaultValue
          },
          mkDefaults(tail))
    }
    val rlt = mkDefaults(c.defs)
    assert(rlt.t == configTypes(c.name), String.format("expected:%s, found:%s", configTypes(c.name), rlt.t))
    rlt
  }

  val rangedUserValue = constructLazyExpr(configTypes apply _.name, "rangedUserValue") { c: AConfig =>{
    val rlt = c.ktype match {
      case KStringType => TId(c.name, configTypes(c.name))
      case _ => inRange(TId(c.name, configTypes(c.name)), lowerBound(c.name), upperBound(c.name))
    }
    assert (rlt.t == configTypes(c.name))
    rlt
  }
  }
  val lowerBound = constructLazyExpr(configTypes apply _.name, "lowerBound") {
    c: AConfig =>{
      val rlt = c.ktype match {
        case KBoolType => guaranteeBool(c.rev.map(translate).foldLeft(TNo: TExpr)(TMax(_, _)))
        case KTriType => c.rev.map(translate).foldLeft(TNo: TExpr)(TMax(_, _))
        case _ =>
          assert(c.ktype == KIntType || c.ktype == KHexType, c.ktype + ":" + c.ranges)
          c.ranges.foldRight(TInt(Int.MinValue): TExpr)(
            (l: gsd.linux.Range, r: TExpr) => {
              assert(guaranteeTristate(translate(l.c)).t == TTristateType)
              assert(translate(l.low).t == TIntType)
              TConditional(translate(l.c) bneq TNo, translate(l.low), r)
            })
      }
      assert(rlt.t == configTypes(c.name))
      rlt
    }
  }

  val upperBound = constructLazyExpr(configTypes apply _.name, "upperBound") {
    c: AConfig =>{
      val rlt = c.ktype match {
        case KBoolType =>
          val result = translate(c.inherited)
          guaranteeBool(result)
        case KTriType =>
          val result = guaranteeTristate(translate(c.inherited))
          result
        case _ =>
          assert(c.ktype == KIntType || c.ktype == KHexType)
          c.ranges.foldRight(TInt(Int.MaxValue): TExpr)(
            (l: gsd.linux.Range, r: TExpr) => {
              assert(guaranteeTristate(translate(l.c)).t == TTristateType)
              assert(translate(l.high).t == TIntType)
              TConditional(translate(l.c) bneq TNo, translate(l.high), r)
            })
      }
      assert(rlt.t==configTypes(c.name))
      rlt
    }
  }

  // Translate a KExpr to a rangefix expression
  def translate(in: KExpr): TExpr = {
    def fixType(l:TExpr):TExpr = l.t match{
      case TBoolType => l
      case TTristateType => l
      case _ => TNo
    }
    def fixBool(l:TExpr,r:TExpr):(TExpr, TExpr)=(l,r) match{
      case (_, TYes) => l.t match {
        case TBoolType => (l, BoolTrue)
        case _ =>{assert(l.t==TTristateType); (l, r)}
      }
      case (_, TNo) => l.t match{
        case TBoolType => (l, BoolFalse)
        case _ =>{assert(l.t==TTristateType);(l, r)}
      }
      case (TYes, _) => r.t match{
        case TBoolType => (BoolTrue, r)
        case _ => {assert(l.t ==TTristateType); (l, r)}
      }
      case (TNo, _) => r.t match{
        case TBoolType => (BoolFalse, r)
        case _ => {assert(l.t == TTristateType); (l, r)}
      }
      case (_, _) => (l, r)
    }
    in match {
      case Id(name) => effective(name)
      case KInt(value) => TInt(value)
      case KHex(value) =>
        // Translate to integer
        try {
          TInt(java.lang.Long.parseLong(value.substring(2), 16))
        } catch {
          case _: NumberFormatException =>
            warn("Hex parse fail: " + value)
            TInt(0) // FIXME return
        }
      case KLiteral(value) => TString(value)
      case KAnd(l, r) => {
        val rlt = fixBool(fixType(translate(l)), fixType(translate(r)))
        
        (rlt._1, rlt._2) match{
          case (TNo, _) => TNo
          case (_, TNo)=> TNo
          case (TMod, TYes) => TMod
          case (TYes, TMod) => TMod
          case (TMod, TMod) => TMod
          case (TYes, y) => y
          case (x, TYes) => x
          case (BoolFalse, _) => BoolFalse
          case (_, BoolFalse) =>BoolFalse
          case (BoolTrue, y) =>y
          case (x, BoolTrue) =>x
          case (x, y) => TMin(x, y)
        }
      }
      case KOr(l, r) => {
        val rlt = fixBool(fixType(translate(l)), fixType(translate(r)))

        (rlt._1, rlt._2) match{
          case (TYes, _) => TYes
          case (_, TYes)=>TYes
          case (TMod, TNo) => TMod
          case (TNo, TMod) => TMod
          case (TMod, TMod) => TMod
          case (TNo, y) => y
          case (x, TNo) =>x
          case (BoolTrue, _) => BoolTrue
          case (_,BoolTrue) => BoolTrue
          case (BoolFalse, y) => y
          case (x,BoolFalse) => x 
          case (x, y) => TMax(x, y)
        }
      }
      case KEq(l, r) =>{
        val rlt = fixBool(translate(l), translate(r))
        createFixedTEq(rlt._1, rlt._2)
      }
      case NonCanonEq(l, r) => {
        val rlt = fixBool(translate(l), translate(r))
        createFixedTEq(rlt._1, rlt._2)
      }
      case KNEq(l, r) =>{
        val rlt = fixBool(translate(l), translate(r))
        !createFixedTEq(rlt._1, rlt._2)
      }
      case KNot(l) => !(fixType(translate(l)))
      // TODO: Fix this?
      //case Group(_, x) => translate(x)
      case Yes => TYes
      case Module => TMod
      case No => TNo
    }
  }
  //contraints ?
  lazy val choiceConstraints: List[BoolExpr] = ak.choices flatMap {
    case AChoice(vis, isBool, isMand, members) =>
      val xorExprs: List[BoolExpr] = members flatMap { m1 =>
        val restExprs: List[BoolExpr] =
          members filterNot (_ == m1) map { m2 => id(m2) beq TNo }
        restExprs map { (id(m1) beq TYes) ==> _ }
      }

      val isBoolExpr =
        if (isBool)
          Option(members map { m => id(m) bneq TMod } reduceLeft { BOr(_: BoolExpr, _) })
        else
          None

      val isMandExpr =
        if (isMand && isBool)
          Option(members map { m => id(m) bneq TNo } reduceLeft { BOr(_: BoolExpr, _) })
        else
          None

      (isBoolExpr.toList ::: isMandExpr.toList ::: xorExprs) map ((translate(vis) bneq TNo) ==> _)
  }

  lazy val configDomains: Map[String, Expression] = {
    ak.configs map {
      case AConfig(_, name, ktype, _, _, _, _, _, _) =>
        name -> {
          ktype match {
              //case KBoolType => IdentifierRef(encode(name)) !== tristateMod
            case KBoolType => BoolLiteral(true)
            case KTriType => BoolLiteral(true) 
            case KIntType => BoolLiteral(true)
            case KHexType => IdentifierRef(encode(name)) >= 0
            case KStringType => BoolLiteral(true)
          }
        }
    } toMap
  }

  lazy val visibilityConstraints: Map[String, BoolExpr] = ak.configs map {
    case AConfig(_, name, ktype, inherited, prompt, _, _, _, _) =>
      name -> (translate(inherited) bneq TNo)
  } toMap

  lazy val allConstraints: Traversable[Expression] = choiceConstraints.map(_.toExpression) ++ configDomains.values

  lazy val varTypes: Expression.Types = configTypes map { case (k, v) => encode(k) -> v.toType }
}

@deprecated("Use Kconfig2 instead", "2012/12/10")
class Kconfig(val ak: AbstractKConfig, checker: ExpressionTypeChecker = new ExpressionTypeChecker) {

  import Rewriter._

  val configTypes: Map[String, TType] = {
    ak.configs map {
      case AConfig(_, name, ktype, _, _, _, _, _, _) =>

        val configType: TType = ktype match {
          case KBoolType => TBoolType
          case KTriType => TTristateType
          case KIntType => TIntType
          case KHexType => TIntType
          case KStringType => TStringType
        }

        // return a tuple of the config name to it's rangefix type
        name -> configType
    } toMap
  } withDefaultValue (TTristateType) // FIXME returns TTristate for unknown configs

  /* Returns the literal associated with the undefined value for a type */
  def undefinedValue(t: TType): TLiteral = t match {
    case TTristateType => TNo
    case TStringType => TString("")
    case TIntType => INT_NULL_LITERAL
  }

  // Translate a KExpr to a rangefix expression
  def translate(in: KExpr): TExpr = in match {
    case Id(name) => TId(name, configTypes(name))
    case KInt(value) => TInt(value)
    case KHex(value) =>
      // Translate to integer
      try {
        TInt(Integer.parseInt(value.substring(2), 16))
      } catch {
        case _: NumberFormatException =>
          warn("Hex parse fail: " + value)
          TInt(0) // FIXME return
      }
    case KLiteral(value) => TString(value)
    case KAnd(l, r) => TMin(translate(l), translate(r))
    case KOr(l, r) => TMax(translate(l), translate(r))
    case KEq(l, r) => translate(l) teq translate(r)
    case NonCanonEq(l, r) => translate(l) teq translate(r)
    case KNEq(l, r) => !(translate(l) teq translate(r))
    case KNot(l) => !translate(l)
    // TODO: Fix this?
    //case Group(_, x) => translate(x)
    case Yes => TYes
    case Module => TMod
    case No => TNo
  }

  def id(name: String): TId =
    TId(name, configTypes(name))

  object IdGen {

    def mkTId(id: Int): TId = {
      assert(GEN_PREFIX + id != GEN_PREFIX, id)
      TId(GEN_PREFIX + id, TTristateType)
    }

    private var equivBuffer: collection.mutable.Buffer[TExpr] = new collection.mutable.ArrayBuffer[TExpr]
    private val revMap = new collection.mutable.HashMap[TExpr, Int]

    def addEquivConstraint(e: TExpr): TId =
      revMap.get(e) match {
        case Some(id) =>
          mkTId(id)
        case None =>
          val id: Int = equivBuffer.indices.end
          equivBuffer += e
          revMap += e -> id
          mkTId(id)
      }

    // // atom expr will be removed, so make sure simplifyAll is called first
    // def equivTuples =
    // equivBuffer.indices filterNot (equivBuffer andThen isAtomExpr) map { k => (GEN_PREFIX + k) -> equivBuffer(k) } toMap

    // // The generated variables have dependencies such that variables with lower
    // // ids must be evaluated first
    // def equivConstraints: Traversable[BoolExpr] =
    // equivTuples map { case (id, e) => TId(id, TTristateType) beq e }

    def fixAll(checker: ExpressionTypeChecker) {
      for (i <- equivBuffer.indices)
        equivBuffer(i) = checker fixTypeErrors equivBuffer(i)
    }

    private def isAtomExpr(expr: TExpr) = expr.isInstanceOf[TLiteral] || expr.isInstanceOf[TId]
    private def isGenAtomRef(n: String) = n.startsWith(GEN_PREFIX) && {
      assert(n.substring(GEN_PREFIX.size) != "", n)
      val i = n.substring(GEN_PREFIX.size).toInt
      isAtomExpr(equivBuffer(i))
    }
    private def isGenRef(n: String) = n.startsWith(GEN_PREFIX)

    // this function should be called after all IDs have been generated
    def simplifyAll(allConstraintsWithGenIds: Traversable[BoolExpr]): Traversable[BoolExpr] = {
      val idSubs = rule[Any] {
        case x @ TId(n, _) if isGenRef(n) => {
          val i = n.substring(GEN_PREFIX.size).toInt
          val expr = equivBuffer(i)
          expr
        }
      }
      def subs[T](expr: T): T = rewrite(reduce(Simplifier.allRules + idSubs))(expr)
      for (i <- equivBuffer.indices) {
        equivBuffer(i) = subs(equivBuffer(i))
      }
      val result = allConstraintsWithGenIds map subs filterNot (_ == BoolTrue)

      result.asInstanceOf[Traversable[BoolExpr]]
    }
  }

  // Reverse dependencies are ignored for non-tristate features
  val configConstraints: List[BoolExpr] =
    ak.configs flatMap {
      case AConfig(_, name, ktype, inherited, prompt, defaults, revs, ranges, _) =>
        // println("Working on: " + name)
        // println("prompt:" + prompt)
        // println("defaults:" + defaults)
        //if (name == "A") println(AConfig(name, ktype, inherited, prompt, defaults, revs, ranges))

        // The lower Bound
        // val revDepExpr = (revs map translate _).foldLeft(TNo: TExpr) (TMax(_,_))
        val revDepSubExprs = (revs map translate _)
        val revDepGenIds = revDepSubExprs map { IdGen.addEquivConstraint }

        val revDepExpr = revDepGenIds.foldLeft(TNo: TExpr)(TMax(_, _))
        val revDepId = IdGen.addEquivConstraint(revDepExpr)

        // A config should always have at least one default
        assert(!defaults.isEmpty)

        def mkDefaults(in: List[ADefault]): BoolExpr = in match {
          case Nil => BoolFalse
          case ADefault(value, prevConds, currCond) :: tail =>

            // Handle default y quirk (i.e. if default y, then config takes
            // value of its condition, not y)
            val configValue =
              if (value == Yes) translate(currCond)
              else translate(value)

            BConditional(
              translate(currCond) bneq TNo,
              ktype match {
                case KBoolType =>
                  assert(configValue.t == TTristateType)
                  assert(revDepId.t == TTristateType)
                  BConditional(TMax(configValue, revDepId) beq TNo, id(name) beq TNo, id(name) beq TYes)
                case KTriType =>
                  assert(configValue.t == TTristateType)
                  assert(revDepId.t == TTristateType)
                  id(name) beq TMax(configValue, revDepId)
                case _ =>
                  //assert(configValue.t == id(name).t, "config:" + id(name).t + ", default:" + configValue.t)
                  id(name) beq configValue
              },
              mkDefaults(tail))
        }

        // The upper bound
        // Yingfei's change here. Original was val inhExpr = translate(inherited)
        val inhExpr = TMax(translate(inherited), revDepId)

        // Condition on prompt and if prompt is false, then defaults take effect
        val derivedExpr = (translate(prompt) bneq TNo) | mkDefaults(defaults)
        // println("derivedExpr:" + derivedExpr)

        // Lower and upper bound expressions
        // TODO check when upper bounds are in effect
        // Changed by Yingfei
        // Original:
        // val boundExprs = ktype match {
        //  case KBoolType | KTriType =>
        //    List(id(name) >= revDepId, (inhExpr < revDepId) | id(name) <= inhExpr)
        // case _ => Nil
        // }

        val lowerBound = ktype match {
          case KTriType | KBoolType =>
            assert(revDepId.t == TTristateType)
            List(id(name) >= revDepId)
          case _ => Nil
        }

        val upperBound = ktype match {
          case KTriType => inhExpr.t match {
            case TTristateType => List(id(name) <= inhExpr)
            case _ => List(id(name) beq TNo)
          }
          case KBoolType => List((inhExpr bneq TNo) | (id(name) beq TNo))
          case _ => Nil
        }

        val boundExprs = lowerBound ++ upperBound

        val rangeExprs = ranges flatMap {
          case gsd.linux.Range(low, high, cond) =>
            List(id(name) >= translate(low), id(name) <= translate(high))
        }

        List(derivedExpr) ::: boundExprs ::: rangeExprs
    }

  val choiceConstraints: List[BoolExpr] = ak.choices flatMap {
    case AChoice(vis, isBool, isMand, members) =>

      val xorExprs: List[BoolExpr] = members flatMap { m1 =>
        val restExprs: List[BoolExpr] =
          members filterNot (_ == m1) map { m2 => id(m2) beq TInt(0) }
        restExprs map { (id(m1) beq TInt(2)) ==> _ }
      }

      val isBoolExpr =
        if (isBool)
          //Yingfei's change here. Original: Option(members map { m => id(m) beq TYes } reduceLeft { BOr(_:BoolExpr,_) })
          Option(members map { m => id(m) bneq TMod } reduceLeft { BOr(_: BoolExpr, _) })
        else
          None

      val isMandExpr =
        // Yingfei's change here. Original: if (isMand)
        if (isMand && isBool)
          Option(members map { m => id(m) bneq TNo } reduceLeft { BOr(_: BoolExpr, _) })
        else
          None

      (isBoolExpr.toList ::: isMandExpr.toList ::: xorExprs) map ((translate(vis) bneq TNo) ==> _)
  }

  val visibilityConstraints: Map[String, BoolExpr] = ak.configs map {
    case AConfig(_, name, ktype, inherited, prompt, _, _, _, _) =>
      //Yingfei's change here. Original: name -> (translate(prompt) bneq TNo)
      name -> (translate(inherited) bneq TNo)
  } toMap

  val configDomains: Map[String, Expression] = {
    ak.configs map {
      case AConfig(_, name, ktype, _, _, _, _, _, _) =>
        name -> {
          ktype match {
            case KBoolType => IdentifierRef(name) !== tristateMod
            case KTriType => BoolLiteral(true)
            case KIntType => BoolLiteral(true)
            case KHexType => IdentifierRef(name) >= 0
            case KStringType => BoolLiteral(true)
          }
        }
    } toMap
  }

  val allTConstraints: Traversable[BoolExpr] = {
    val fixed = (configConstraints ++ choiceConstraints) map checker.fixTypeErrors
    IdGen.fixAll(checker)
    IdGen.simplifyAll(fixed)
  }

  lazy val allConstraints = allTConstraints.map(_.toExpression) ++ configDomains.values

}

// /** Mainly used on the command line **/
// object KconfigHelper {
//   def parseFile(f: String): Kconfig =
//     new Kconfig(KConfigParser.parseKConfigFile(f) toAbstractKConfig)
// }

object Simplifier {
  import Rewriter._

  lazy val subsOr =
    rule[Any] {
      case BOr(BoolFalse, y) => y
      case BOr(x, BoolFalse) => x
    }

  lazy val subsEq =
    rule[Any] {
      case BEq(x, y) if (x == y) => BoolTrue
      case BEq(x: TLiteral, y: TLiteral) if (x != y) => BoolFalse
    }

  lazy val subsNeq =
    rule[Any] {
      case BNeq(x, y) if (x == y) => BoolFalse
      case BNeq(x: TLiteral, y: TLiteral) if (x != y) => BoolTrue
    }

  lazy val subsNot =
    rule[Any] {
      case TNot(TYes) => TNo
      case TNot(TMod) => TMod
      case TNot(TNo) => TYes
    }

  lazy val subsGt =
    rule[Any] {
      case BGt(x, y) if x == y => BoolFalse
      case BGt(TNo, y) if y.t == TTristateType => BoolFalse
      case BGt(y, TYes) if y.t == TTristateType => BoolFalse
      case BGt(TInt(Int.MinValue), y) if y.t == TIntType => BoolFalse
      case BGt(y, TInt(Int.MaxValue)) if y.t == TIntType => BoolFalse
      case BGt(TInt(x), TInt(y)) => x > y
      case BGt(TYes, TNo) | BGt(TYes, TMod) | BGt(TMod, TNo) => BoolTrue
      case BGte(x, y) if x == y => BoolTrue
      case BGte(TYes, y) if y.t == TTristateType => BoolTrue
      case BGte(y, TNo) if y.t == TTristateType => BoolTrue
      case BGte(TNo, TYes) | BGte(TNo, TMod) | BGte(TMod, TYes) => BoolFalse
      case BGte(TInt(Int.MaxValue), x) if x.t == TIntType => BoolTrue
      case BGte(x, TInt(Int.MinValue)) if x.t == TIntType => BoolTrue
      case BGte(TInt(x), TInt(y)) => x >= y
    }

  lazy val subsLt =
    rule[Any] {
      case BLt(x, y) if x == y => BoolFalse
      case BLt(TYes, y) if y.t == TTristateType => BoolFalse
      case BLt(y, TNo) if y.t == TTristateType => BoolFalse
      case BLt(TNo, TYes) | BLt(TNo, TMod) | BLt(TMod, TYes) => BoolTrue
      case BLt(TInt(Int.MaxValue), y) if y.t == TIntType => BoolFalse
      case BLt(y, TInt(Int.MinValue)) if y.t == TIntType => BoolFalse
      case BLt(TInt(x), TInt(y)) => x < y
      case BLte(x, y) if x == y => BoolTrue
      case BLte(TNo, y) if y.t == TTristateType => BoolTrue
      case BLte(y, TYes) if y.t == TTristateType => BoolTrue
      case BLte(TMod, TNo) | BLte(TYes, TNo) | BLte(TYes, TMod) => BoolFalse
      case BLte(TInt(Int.MinValue), x) if x.t == TIntType => BoolTrue
      case BLte(x, TInt(Int.MaxValue)) if x.t == TIntType => BoolTrue
      case BLte(TInt(x), TInt(y)) => x <= y
    }

  lazy val subsMax =
    rule[Any] {
      case TMax(x, y) if x == y => x
      case TMax(TYes, y) => TYes
      case TMax(x, TYes) => TYes
      case TMax(TNo, y) => y
      case TMax(x, TNo) => x
    }

  lazy val subsMin =
    rule[Any] {
      case TMin(x, y) if x == y => x
      case TMin(TYes, y) => y
      case TMin(x, TYes) => x
      case TMin(TNo, y) => TNo
      case TMin(x, TNo) => TNo
    }

  lazy val subsConditional =
    rule[Any] {
      case BConditional(BoolTrue, success, _) => success
      case BConditional(BoolFalse, _, fail) => fail
      case TConditional(BoolTrue, success, _) => success
      case TConditional(BoolFalse, _, fail) => fail
    }

  lazy val allRules = subsOr + subsGt + subsEq + subsNeq + subsLt + subsMax + subsMin + subsConditional + subsNot

  def simplify(expr: BoolExpr): BoolExpr =
    rewritebuBypassingFuncDefs(allRules)(expr)
  def simplify(expr: TExpr): TExpr =
    rewritebuBypassingFuncDefs(allRules)(expr)
}
