package ca.uwaterloo.gsd.rangeFix
import collection._

object SMTExpression {
	implicit def string2VarRef(s:String) = SMTVarRef(s)
	implicit def int2literal(s:Int) = SMTIntLiteral(s)
}
trait SMTExpression extends GExpression {
	
	def < (r:SMTExpression) = SMTLessThan(this, r)
	def <= (r:SMTExpression) = SMTLessEqThan(this, r)
	def > (r:SMTExpression) = SMTGreaterThan(this, r)
	def >= (r:SMTExpression) = SMTGreaterEqThan(this, r)
	def + (r:SMTExpression) = SMTPlus(this, r)
	def - (r:SMTExpression) = SMTMinus(this, r)
	def & (r:SMTExpression) = SMTAnd(this, r)
	def | (r:SMTExpression) = SMTOr(this, r)
	def ==> (r:SMTExpression) = SMTImplies(this, r)
	def === (r:SMTExpression) = SMTEq(this, r)
	def unary_! : SMTExpression = SMTNot(this)
}

abstract class SMTFuncCall(func:String, parameters:SMTExpression*) extends SMTExpression {
  override def toString() =
    if (parameters.size > 0)
      "(%s %s)".format(func, parameters.map(_.toString).reduceLeft(_ + " " + _))
    else
      func
}

case class SMTAnd(l:SMTExpression, r:SMTExpression) extends SMTFuncCall("and", l, r)
case class SMTOr(l:SMTExpression, r:SMTExpression) extends SMTFuncCall("or", l, r)
case class SMTImplies(l:SMTExpression, r:SMTExpression) extends SMTFuncCall("=>", l, r)
case class SMTNot(i:SMTExpression) extends SMTFuncCall("not", i)
case class SMTPlus(l:SMTExpression, r:SMTExpression) extends SMTFuncCall("+", l, r)
case class SMTMinus(l:SMTExpression, r:SMTExpression) extends SMTFuncCall("-", l, r)
case class SMTTimes(l:SMTExpression, r:SMTExpression) extends SMTFuncCall("*", l, r)
case class SMTDivide(l:SMTExpression, r:SMTExpression) extends SMTFuncCall("div", l, r)
case class SMTMod(l:SMTExpression, r:SMTExpression) extends SMTFuncCall("mod", l, r)
case class SMTLessThan(l:SMTExpression, r:SMTExpression) extends SMTFuncCall("<", l, r)
case class SMTGreaterThan(l:SMTExpression, r:SMTExpression) extends SMTFuncCall(">", l, r)
case class SMTLessEqThan(l:SMTExpression, r:SMTExpression) extends SMTFuncCall("<=", l, r)
case class SMTGreaterEqThan(l:SMTExpression, r:SMTExpression) extends SMTFuncCall(">=", l, r)
case class SMTEq(l:SMTExpression, r:SMTExpression) extends SMTFuncCall("=", l, r)
case class SMTBAnd(l:SMTExpression, r:SMTExpression) extends SMTFuncCall("bvand", l, r)
case class SMTBOr(l:SMTExpression, r:SMTExpression) extends SMTFuncCall("bvor", l, r)
case class SMTBNot(l:SMTExpression) extends SMTFuncCall("bvnot", l)
case class SMTConditional(cond:SMTExpression, pass:SMTExpression, fail:SMTExpression) extends SMTFuncCall("ite", cond, pass, fail)
case class SMTUserFuncCall(func:String, parameters:SMTExpression*) extends SMTFuncCall(func, parameters:_*)
trait SMTLiteral extends SMTExpression
case class SMTIntLiteral(v:Long) extends SMTLiteral {
	override def toString() = if (v >= 0) v.toString else "(- " + (-v).toString + ")"
}
case class SMTScalarLiteral(v:String) extends SMTLiteral {
	override def toString() = SMTScalarType.header + v
}
case class SMTBoolLiteral(b:Boolean) extends SMTLiteral {
	override def toString() = if (b) "true" else "false"
}
case class SMTBVLiteral(bv:Array[Boolean]) extends SMTLiteral {
	override def toString() = "#b" + bv.map(if (_) "1" else "0").reduceLeft(_ + _)
}
case class SMTVarRef(id:String) extends SMTLiteral with GIdentifierRef {
	override def toString = id
}

trait SMTType {
	def toDeclaration = ""
}

case object SMTIntType extends SMTType {
	override def toString = "Int"
}
case class SMTScalarType(id:String, contents:Set[String]) extends SMTType {
	override def toString = id
	
	assert (contents.size > 0)
	
	override def toDeclaration = "(" + id + 
		contents.map(" " + SMTScalarType.header + _.toString ).reduceLeft(_ + _) + ")"
}
object SMTScalarType {
	def header = "__s"
}
case object SMTBoolType extends SMTType {
	override def toString = "Bool"
}
case class SMTBVType(length:Int) extends SMTType { // bit vector
	override def toString = "(_ BitVec " + length + ")"
}


case class SMTFuncDefine(val name:String, parameters:Seq[(String, SMTType)], returnType:SMTType, body:SMTExpression) extends GFunctionDef[SMTExpression] {
  def toDefString = "(define-fun %s (%s) %s %s)".format(name, parameters.map(pair => "(" + pair._1 + " " + pair._2 + ")").reduceOption(_ + " " + _).getOrElse(""), returnType, body)

  override def equals(obj:Any):Boolean={
    if (!obj.isInstanceOf[SMTFuncDefine])
      return false
    val objFun = obj.asInstanceOf[SMTFuncDefine]
    objFun.name == this.name
  }

  override def hashCode() :Int={
    return name.hashCode();
  }

  override def paramNames = {
    parameters.map(_._1)
  }

  override def replaceBody(newBody:SMTExpression) =
    SMTFuncDefine(name, parameters, returnType, newBody)

}
