package ca.uwaterloo.gsd.rangeFix
import scala.collection._
import TypeHelper._

abstract class TypeVar() {
	def getSourceText():String
	val reasons = mutable.Set[String]()
	override def toString = "T[" + getSourceText + "]" 
	def toStringWithValue(types:TypeMap) = "T[" + getSourceText + "]=" + getType(types)
	
	def getType(types:TypeMap):Type = types.getOrElse(this, TypeHelper.anyType)
	def setType(newType:Type, types:TypeMap):Boolean = { 
		if (getType(types) == newType) false
		else {
			types.put(this, newType) 
			true
		}
	}
}

class TypeErrorException(left:TypeVar, right:TypeVar, leftType:Type, rightType:Type, clashFeature:String, leftReasons:Set[String], rightReasons:Set[String]) extends Exception(toString()) {
	override def toString() = String.format("Type clash in %s: \n\tT[%s]=%s from %s\n\tT[%s]=%s from %s.", clashFeature, left.getSourceText, leftType, leftReasons, right.getSourceText, rightType, rightReasons)
	
	def reasons = leftReasons ++ rightReasons
}

class TypeErrorsException(errors:Iterable[TypeErrorException]) extends Exception(errors.map(_.toString).reduce(_ + "\n" + _)) {
	assert(errors.size > 0)
}

case class FeatureTypeVar(id:String) extends TypeVar {
	val featureID = id
	
	override def getSourceText() = id
	
	
}
case class ExpressionTypeVar(expr:Expression, eid:Int) extends TypeVar {
	// import org.kiama.rewriting.Rewriter._ 
	// val isConstant = collectl { case i:IdentifierRef => i } (expr).size == 0
	override def getSourceText() = expr.toString
	// override def setType(newType:Type, types:TypeMap):Boolean = 
		// if (isConstant) false else super.setType(newType, types)
}

case class ConstantTypeVar(t:Type) extends TypeVar {
	def this(ts:Type*) = this(new DisjunctiveType(Set() ++ ts))
	override def getSourceText() = t.toString
	override def getType(types:TypeMap) = t
	override def setType(newType:Type, types:TypeMap):Boolean = false
}

trait TypeConstraint {
	def enforce(types:TypeMap, reasons:ReasonMap):(Boolean, Boolean)
	val left:TypeVar
	val right:TypeVar
	val operator:String
	val sourceFeatureID:String
	override def toString() = "T[" + left.getSourceText + "] " + operator + " T[" + right.getSourceText + "] from " + sourceFeatureID

	protected def setType(t:Type, v:TypeVar, other:TypeVar, types:TypeMap, reasons:ReasonMap):Boolean = {
		if (v.setType(t, types)) {
			reasons.put (v, reasons.getOrElse(v, Set()) ++ reasons.getOrElse(other, Set()) + sourceFeatureID)
			true
		}		
		else false
	}
}

class RightConvertibleToBoolConstraint(override val left:TypeVar, override val right:TypeVar, val sourceFeatureID:String) extends TypeConstraint {
	override def enforce(types:TypeMap, reasons:ReasonMap):(Boolean, Boolean) = {
		(left.getType(types) & (right.getType(types) | BoolType)) match {
			case Some(lt) => 
				val leftChanged = setType(lt, left, right, types, reasons)
				val rightChanged = if (lt >= BoolType) false else setType(lt, right, left, types, reasons) 
				(leftChanged, rightChanged)
			case None => throw new TypeErrorException(left, right, left.getType(types), right.getType(types), sourceFeatureID, reasons.getOrElse(left, Set()), reasons.getOrElse(right, Set()))
		}
	}
	
	override val operator = "= Bool + "
	
}

class RightEnumConvertibleConstraint(override val left:TypeVar, override val right:TypeVar, val sourceFeatureID:String) extends TypeConstraint {
	override def enforce(types:TypeMap, reasons:ReasonMap):(Boolean, Boolean) = {
		val rt = right.getType(types)
		val (convertibleToInt, convertibleToString) = 
			if (rt.isInstanceOf[NegativeType]) (false, false)
			else {
				val enumTypes = rt.getTypeSet.filter(_.isInstanceOf[EnumType]).asInstanceOf[Set[EnumType]]
				(enumTypes.exists(_.isIntEnum), enumTypes.exists(_.isStringEnum))
			}
		val rti = if (convertibleToInt) rt | NumberType else rt
		val rts = if (convertibleToString)  rti | StringType else rti
		(left.getType(types) & rts) match {
			case Some(nlt) => 
				val leftChanged = setType(nlt, left, right, types, reasons)
				val rightChanged = 
					if ((nlt >= NumberType && convertibleToInt) 
						|| (nlt >= StringType && convertibleToString)) 
						false 
					else setType(nlt, right, left, types, reasons) 
				(leftChanged, rightChanged)
			case None => throw new TypeErrorException(left, right, left.getType(types), right.getType(types), sourceFeatureID, reasons.getOrElse(left, Set()), reasons.getOrElse(right, Set()))
		}
	}
	
	override val operator = "= |enumConvert|"
	
}


class EqualConstraint(override val left:TypeVar, override val right:TypeVar, val sourceFeatureID:String) extends TypeConstraint {
	override def enforce(types:TypeMap, reasons:ReasonMap):(Boolean, Boolean) = {
		(left.getType(types) & right.getType(types)) match {
			case Some(t) => 
				val leftChanged = setType(t, left, right, types, reasons)
				val rightChanged = setType(t, right, left, types, reasons)
				(leftChanged, rightChanged)
			case None => throw new TypeErrorException(left, right, left.getType(types), right.getType(types), sourceFeatureID, reasons.getOrElse(left, Set()), reasons.getOrElse(right, Set()))
		}
	}
	
	override val operator = "="
}

object TypeHelper {
	import ConditionalCompilation._
	val anyType = NegativeType(Set[Type]()) 
	type TypeMap = mutable.Map[TypeVar, Type]
	type ReasonMap = mutable.Map[TypeVar, Set[String]]
	implicit def type2TypeVar(s:Type) = new ConstantTypeVar(s)
	implicit def featureID2TypeVar(s:String) = new FeatureTypeVar(s)
	// implicit def expression2TypeVar(s:Expression) = new ExpressionTypeVar(s)

	private def findSmallestPossibleType(a:Type, b:Type, c:Type) = {
		(a & b) match {
			case Some(d) => 
				(d & c) match {
					case Some(e) => e
					case None => d
				}
			case None =>
				(a & c) match {
					case Some (d) => d
					case None => 
						(a & b) match {
							case Some(d) => d
							case None => a
						}
				}
		}
	}
	private def findSmallestPossibleType(a:Type, b:Type) = {
		(a & b) match {
			case Some (c) => c
			case None => a
		}
	}
	
	private def splitStringIntoSet(str:String):Set[String] = {
		//deal with special strings first. Should be generalized to the annotation system later.
		if (str == "arm-eabi") return Set("arm", "-eabi")
		else {
			val result = mutable.Set[String]()
			val curItem = new mutable.StringBuilder()
			var parenLevel = 0
			for (c <- str) {
				if (parenLevel == 0 && c == ' '){
					if (curItem.size > 0) {
						result += curItem.toString
						curItem.clear()
					}
				}
				else if (c == '(' || c == '[' || c == '{') {
					parenLevel += 1
					curItem += c
				}
				else if (c == '}' || c == ']' || c == '}') {
					parenLevel -= 1
					curItem += c
				}
				else curItem += c
			}
			if (curItem.size > 0) result += curItem.toString
			result
		}
	}
	
	def inferTypes(nodes:Iterable[Node], 
				   values:Map[String, OptionValue] = Map(), 
				   extraConstraints:Iterable[TypeConstraint] = List(),
				   literals2enumTypes:Map[Literal, List[EnumType]] = null //only for optimization
				   ):(Map[String, Type], Iterable[TypeErrorException]) = {
		val var2Types = mutable.Map[TypeVar, Type]()
		val var2Reasons = mutable.Map[TypeVar, Set[String]]()
		val constraints = generateTypeConstratins(nodes, values, var2Types, var2Reasons, 
			if (literals2enumTypes == null) getLiteral2Enums(nodes) else literals2enumTypes
			) ++ extraConstraints
		IF[CompilationOptions.TRACE_TYPE_PROPAGATION#v]{ println(constraints) }
		TypeHelper.inferTypes(constraints, var2Types, var2Reasons)
	}
	
	def getTypesAndRewriteNodes(nodes:Iterable[Node], 
				   values:Map[String, OptionValue] = Map(), 
				   extraConstraints:Iterable[TypeConstraint] = List()
				   ):(Iterable[Node], Expression.Types, (EnumItemLiteral, Type)=>Literal) = {
		val literals2enumTypes = getLiteral2Enums(nodes)
		val (types, errors) = inferTypes(nodes, values, extraConstraints, literals2enumTypes)
		if (errors.size > 0) throw new TypeErrorsException(errors)
		val allIds:Set[String] = org.kiama.rewriting.Rewriter.collects {case n:Node => n.id}(nodes)
		val newTypes = autoChooseTypes(types ++ (allIds -- types.keySet).map((_, anyType)).toMap)
		def rewrite(e:Expression, t:Type) = 
			autoChooseAndRewrite(e, newTypes, literals2enumTypes, allIds, t)
		val newNodes = for (n <- nodes) yield {
			def rewriteSingleNode(n:Node):Node = {
				def rewriteSelfExpressions(e:Expression):Expression = {
					rewrite(e, newTypes(n.id))
				}
				def rewriteBooleanExpressions(e:Expression):Expression = {
					rewrite(e, BoolType)
				}
				def mapLegalValueRange(range:Range):Range = range match { 
					case MinMaxRange(l, r) => MinMaxRange(rewriteSelfExpressions(l), rewriteSelfExpressions(r))
					case SingleValueRange(r) => SingleValueRange(rewriteSelfExpressions(r))
				}

				Node(
					n.id,
					n.cdlType,
					n.display,
					n.description,
					n.flavor,
					n.defaultValue.map(rewriteSelfExpressions),
					n.calculated.map(rewriteSelfExpressions),
					n.legalValues.map(l => LegalValuesOption(l.ranges.map(mapLegalValueRange))),
					n.reqs.map(rewriteBooleanExpressions),
					n.activeIfs.map(rewriteBooleanExpressions),
					n.implements,
					n.children.map(rewriteSingleNode)
				)
			}
			rewriteSingleNode(n)
		}
		(newNodes, newTypes, autoChooseAndRewriteLiteral(literals2enumTypes))
	}
	
	def convertConfig(values:Map[String, OptionValue], types:Expression.Types, rewrite:(EnumItemLiteral, Type)=>Literal) = {
		(for ((id, v) <- values) yield {
			(id, convertOptionValue(v, rewrite(_, types(id))))
		} ).toMap
	}

	def convertOptionValue(v:OptionValue, rewrite:EnumItemLiteral=>Literal):ConfigValue = 
		v match {
			case IntOptionValue(i) => 
				SingleConfigValue(rewrite(IntLiteral(i)))
			case StringOptionValue(s) => 
				SingleConfigValue(rewrite(StringLiteral(s)))
			case RealOptionValue(i) =>
				import ConditionalCompilation._
				var literal:EnumItemLiteral = RealLiteral(i)
				IF[CompilationOptions.CONVERT_REAL_TO_INT#v] {
					literal = IntLiteral(i.toLong)
				}
				SingleConfigValue(rewrite(literal))
			case NoneOptionValue => NoneConfigValue
			case DoubleOptionValue(b, l) => 
				DoubleConfigValue(BoolLiteral(b != IntOptionValue(0)), convertOptionValue(l, rewrite).asInstanceOf[SingleConfigValue].l)
		}
	
	
	def getTypesAndRewrite(nodes:Iterable[Node], 
				   values:Map[String, OptionValue] = Map(), 
				   extraConstraints:Iterable[TypeConstraint] = List()
				   ):(Iterable[Node], Expression.Types, Map[String, ConfigValue]) = {
		val (newNodes, newTypes, rewriteFunc) = getTypesAndRewriteNodes(nodes, values, extraConstraints)
		val config = convertConfig(values, newTypes, rewriteFunc)
		(newNodes, newTypes, config)
		// val literals2enumTypes = getLiteral2Enums(nodes)
		// val (types, errors) = inferTypes(nodes, values, extraConstraints, literals2enumTypes)
		// if (errors.size > 0) throw new TypeErrorsException(errors)
		// val allIds:Set[String] = org.kiama.rewriting.Rewriter.collects {case n:Node => n.id}(nodes)
		// val newTypes = autoChooseTypes(types ++ (allIds -- types.keySet).map((_, anyType)).toMap)
		// def rewrite(e:Expression, t:Type) = 
			// autoChooseAndRewrite(e, newTypes, literals2enumTypes, allIds, t)
		// val newNodes = for (n <- nodes) yield {
			// def rewriteSingleNode(n:Node):Node = {
				// def rewriteSelfExpressions(e:Expression):Expression = {
					// rewrite(e, newTypes(n.id))
				// }
				// def rewriteBooleanExpressions(e:Expression):Expression = {
					// rewrite(e, BoolType)
				// }
				// def mapLegalValueRange(range:Range):Range = range match { 
					// case MinMaxRange(l, r) => MinMaxRange(rewriteSelfExpressions(l), rewriteSelfExpressions(r))
					// case SingleValueRange(r) => SingleValueRange(rewriteSelfExpressions(r))
				// }

				// Node(
					// n.id,
					// n.cdlType,
					// n.display,
					// n.description,
					// n.flavor,
					// n.defaultValue.map(rewriteSelfExpressions),
					// n.calculated.map(rewriteSelfExpressions),
					// n.legalValues.map(l => LegalValuesOption(l.ranges.map(mapLegalValueRange))),
					// n.reqs.map(rewriteBooleanExpressions),
					// n.activeIfs.map(rewriteBooleanExpressions),
					// n.implements,
					// n.children.map(rewriteSingleNode)
				// )
			// }
			// rewriteSingleNode(n)
		// }
		// val newConfig = (for ((id, v) <- values) yield {
			// def convertOptionValue(v:OptionValue):ConfigValue = 
				// v match {
					// case IntOptionValue(i) => 
						// SingleConfigValue(rewrite(IntLiteral(i), newTypes(id)).asInstanceOf[Literal])
					// case StringOptionValue(s) => 
						// SingleConfigValue(rewrite(StringLiteral(s), newTypes(id)).asInstanceOf[Literal])
					// case RealOptionValue(i) =>
						// import ConditionalCompilation._
						// var literal:Literal = RealLiteral(i)
						// IF[CompilationOptions.CONVERT_REAL_TO_INT#v] {
							// literal = IntLiteral(i.toLong)
						// }
						// SingleConfigValue(rewrite(literal, newTypes(id)).asInstanceOf[Literal])
					// case NoneOptionValue => NoneConfigValue
					// case DoubleOptionValue(b, l) => 
						// DoubleConfigValue(BoolLiteral(b != IntOptionValue(0)), convertOptionValue(l).asInstanceOf[SingleConfigValue].l)
				// }
			// (id, convertOptionValue(v))
		// } ).toMap
		// (newNodes, newTypes, newConfig)
	}
	
	def ZeroToType(t:SingleType) = {
		if (t == BoolType) BoolLiteral(false)
		else if (t == StringType) StringLiteral("0")
		else if (t == SetType) SetLiteral(Set("0"))
		else if (t.isInstanceOf[EnumType]) EnumLiteral(IntLiteral(0), t.asInstanceOf[EnumType])
		else {
			assert(t == NumberType)
			IntLiteral(0)
		}
	}

	def autoChooseTypes(types:Map[String, Type]):Map[String, SingleType] = types.mapValues( autoChooseOneType(_) )
	
	def autoChooseOneType(t:Type):SingleType = 		
		if (t.isInstanceOf[SingleType]) t.asInstanceOf[SingleType]
		else if (t >= BoolType) BoolType
		else if (t >= NumberType) NumberType
		else if (t >= StringType) StringType
		else if (t >= SetType) SetType
		else throw new Exception("Does not know how to select a type from:" + t)
		
	def autoChooseAndRewriteLiteral(literals2enumTypes:Map[Literal, List[EnumType]])(l:EnumItemLiteral, expected:Type):Literal = {
		val constrainedType = (expected & literal2Type(l, literals2enumTypes))
		if (constrainedType.isEmpty) throw new Exception("Cannot convert " + l.toString + " to " + expected)
		val newType = autoChooseOneType(constrainedType.get)		
		l match {
			case IntLiteral(i) => 
				if (newType == BoolType) {
					if (i == 0) BoolLiteral(false)
					else {
						assert(i == 1)
						BoolLiteral(true)
					}
				}
				else if (newType.isInstanceOf[EnumType]) {
					val t = newType.asInstanceOf[EnumType]
					assert(t.items.contains(IntLiteral(i)))
					EnumLiteral(IntLiteral(i), t)
				}
				else if (newType == StringType) {
					assert(i == 0)
					StringLiteral("0")
				}
				else if (newType == SetType) {
					assert(i == 0)
					SetLiteral(Set("0"))
				}
				else {
					assert(newType == NumberType)
					IntLiteral(i)
				}					
			case StringLiteral(s) => 
				if (newType.isInstanceOf[EnumType]) {
					val t = newType.asInstanceOf[EnumType]
					assert(t.items.contains(StringLiteral(s)))
					EnumLiteral(StringLiteral(s), t)
				}
				else if (newType == SetType) {
					SetLiteral(splitStringIntoSet(s))
				}
				else {
					assert(newType >= StringType)
					StringLiteral(s)
				}	
		}
	}

	// assuming the model is type-correct
	def autoChooseAndRewrite(
		expr:Expression, 
		types:Map[String, SingleType],
		// types:mutable.Map[String, Type], //this types will be changed
		literals2enumTypes:Map[Literal, List[EnumType]],
		allIds:Set[String],
		expected:Type):Expression = 
	{
		
		def getLooseLiteralType(l:Literal):Type = literal2Type(l, literals2enumTypes)
		
		// def addToBool(e:Expression):Expression = {
			// if (expected == BoolType) {
				// if (ExpressionHelper.getType(e, types, getLooseLiteralType) >= BoolType) e
				// else ToBool(e)
			// }
			// else e
		// }

		

		// 1. replace all literals with those of the corresponding types
		// 2. replace all non-existing ids, including those called in functions
		// 3. add "ToBool" function
		// 4. change is_substr to == when comparing enums
		def rewriteExpr(expr:Expression, upperExpected:Type):Expression = {
			// def OneToType(t:SingleType) = {
				// if (t == BoolType) BoolLiteral(true)
				// else if (t == StringType) StringLiteral("1")
				// else if (t == NumberType) IntLiteral(1)
				// else {
					// assert(t.isInstanceOf[EnumType])
					// if (t.asInstanceOf[EnumType].items.contains(IntLiteral(1))) {
						// IntLiteral(1)
					// }
					// else {
						// assert(t.asInstanceOf[EnumType].items.contains(StringLiteral("1")))
						// StringLiteral("1")
					// }
				// }
			// }		
		
			val addToBool:Boolean = (upperExpected == BoolType) && !(ExpressionHelper.getType(expr, types, getLooseLiteralType) >= BoolType)
			val expected = if (addToBool) anyType else upperExpected
			assert( (ExpressionHelper.getType(expr, types, getLooseLiteralType) & expected).isDefined, 
				expr.toString + ":" + ExpressionHelper.getType(expr, types, getLooseLiteralType) + " not match " + expected)
			val result = expr match {
				case IdentifierRef(id) => 
					// types.put(id, (types(id) & expected).get) 
					if (allIds.contains(id)) {
						IdentifierRef(id)
					}
					else {
						val newType = autoChooseOneType((expected & types.getOrElse(id, TypeHelper.anyType)).get)
						ZeroToType(newType)
					}
				case Conditional(c, p, f) => 
					val newChildTypes = 
						(ExpressionHelper.getType(p, types, getLooseLiteralType) & 
						ExpressionHelper.getType(f, types, getLooseLiteralType)).get & expected
					val newChildType = autoChooseOneType(newChildTypes.get)
					Conditional(
						rewriteExpr(c, BoolType),
						rewriteExpr(p, newChildType),
						rewriteExpr(f, newChildType))
				case Or(l, r) => 
					assert (expected >= BoolType)
					Or(rewriteExpr(l, BoolType), rewriteExpr(r, BoolType))
				case And(l, r) => 
					assert (expected >= BoolType)
					And(rewriteExpr(l, BoolType), rewriteExpr(r, BoolType))
				case Implies(l, r) => 
					assert (expected >= BoolType)
					Implies(rewriteExpr(l, BoolType), rewriteExpr(r, BoolType))
				case Eq(l, r) =>
					assert (expected >= BoolType)
					val newChildTypes = 
						(ExpressionHelper.getType(l, types, getLooseLiteralType) & 
						ExpressionHelper.getType(r, types, getLooseLiteralType)).get
					val newChildType = autoChooseOneType(newChildTypes)
					Eq( rewriteExpr(l, newChildType),
						rewriteExpr(r, newChildType))
				case NEq(l, r) => 
					assert (expected >= BoolType)
					val newChildTypes = 
						(ExpressionHelper.getType(l, types, getLooseLiteralType) & 
						ExpressionHelper.getType(r, types, getLooseLiteralType)).get
					val newChildType = autoChooseOneType(newChildTypes)
					NEq( rewriteExpr(l, newChildType),
						rewriteExpr(r, newChildType))
				case LessThan(l, r) => 
					assert (expected >= BoolType)
					LessThan(rewriteExpr(l, NumberType), rewriteExpr(r, NumberType))
				case LessThanOrEq(l, r) => 
					assert (expected >= BoolType)
					LessThanOrEq(rewriteExpr(l, NumberType), rewriteExpr(r, NumberType))
				case GreaterThan(l, r) => 
					assert (expected >= BoolType)
					GreaterThan(rewriteExpr(l, NumberType), rewriteExpr(r, NumberType))
				case GreaterThanOrEq(l, r) => 
					assert (expected >= BoolType)
					GreaterThanOrEq(rewriteExpr(l, NumberType), rewriteExpr(r, NumberType))
				case Plus(l, r) => 
					assert (expected >= NumberType)
					Plus(rewriteExpr(l, NumberType), rewriteExpr(r, NumberType))
				case Minus(l, r) => 
					assert (expected >= NumberType)
					Minus(rewriteExpr(l, NumberType), rewriteExpr(r, NumberType))
				case Times(l, r) => 
					assert (expected >= NumberType)
					Times(rewriteExpr(l, NumberType), rewriteExpr(r, NumberType))
				case Div(l, r) => 
					assert (expected >= NumberType)
					Div(rewriteExpr(l, NumberType), rewriteExpr(r, NumberType))
				case Mod(l, r) => 
					assert (expected >= NumberType)
					Mod(rewriteExpr(l, NumberType), rewriteExpr(r, NumberType))
				case Dot(l, r) => 
					assert (expected >= SetType)
					Dot(rewriteExpr(l, SetType), rewriteExpr(r, SetType))
				case Not(n) => 
					assert (expected >= BoolType)
					Not(rewriteExpr(n, BoolType))
				case IsSubstr(l, r) => 
					assert (expected >= BoolType)
					r match {
						case StringLiteral(s) =>
							val enums = getEnumsFromSubString(s, literals2enumTypes)
							val newExpected = 
								if (enums.size >= 1) new DisjunctiveType(Set[Type]() ++ enums + SetType)
								else SetType
							val newType = autoChooseOneType((newExpected & ExpressionHelper.getType(l, types, getLooseLiteralType)).get)
							if (newType == SetType)
								IsSubstr(rewriteExpr(l, SetType), rewriteExpr(r, SetType))
							else {
								assert (newType.isInstanceOf[EnumType])
								val enum = newType.asInstanceOf[EnumType]
								val literals = enum.items.filter(_ match {
									case StringLiteral(l) => l.contains(s)
									case _ => false
								} )
								assert (literals.size > 1)
								literals.map(rewriteExpr(l, enum) === rewriteExpr(_, enum)).reduce[Expression](_ | _)
							}
						case _ => IsSubstr(rewriteExpr(l, SetType), rewriteExpr(r, SetType))
					}
				case ToInt(n) => 
					assert (expected >= NumberType)
					ToInt(rewriteExpr(n, TypeHelper.anyType))
				case ToString(n) => 
					assert (expected >= StringType)
					ToString(rewriteExpr(n, TypeHelper.anyType))
				case ToBool(n) => 
					assert (expected >= BoolType)
					ToBool(rewriteExpr(n, TypeHelper.anyType))
				case l:EnumItemLiteral =>
					autoChooseAndRewriteLiteral(literals2enumTypes)(l, expected)
				// case IntLiteral(i) => 
					// val newType = autoChooseOneType((expected & getLooseLiteralType(IntLiteral(i))).get)
					// if (newType == BoolType) {
						// if (i == 0) BoolLiteral(false)
						// else {
							// assert(i == 1)
							// BoolLiteral(true)
						// }
					// }
					// else if (newType.isInstanceOf[EnumType]) {
						// val t = newType.asInstanceOf[EnumType]
						// assert(t.items.contains(IntLiteral(i)))
						// EnumLiteral(IntLiteral(i), t)
					// }
					// else if (newType == StringType) {
						// assert(i == 0)
						// StringLiteral("0")
					// }
					// else if (newType == SetType) {
						// assert(i == 0)
						// SetLiteral(Set("0"))
					// }
					// else {
						// assert(newType == NumberType)
						// IntLiteral(i)
					// }					
				// case StringLiteral(s) => 
					// val newType = autoChooseOneType((expected & getLooseLiteralType(StringLiteral(s))).get)
					// if (newType.isInstanceOf[EnumType]) {
						// val t = newType.asInstanceOf[EnumType]
						// assert(t.items.contains(StringLiteral(s)))
						// EnumLiteral(StringLiteral(s), t)
					// }
					// else if (newType == SetType) {
						// SetLiteral(splitStringIntoSet(s))
					// }
					// else {
						// assert(newType >= StringType)
						// StringLiteral(s)
					// }					
				case GetData(n) =>
					if (allIds.contains(n)) GetData(n) // will be translated in typeCorrectNodes2Constraints
					else ZeroToType(autoChooseOneType((expected & types.getOrElse(n, TypeHelper.anyType)).get))
				case IsEnabled(n) => 
					if (allIds.contains(n)) IsEnabled(n) // will be translated in typeCorrectNodes2Constraints
					else BoolLiteral(false) //ZeroToType(autoChooseOneType(expected))
				case IsActive(n) => 
					if (allIds.contains(n)) IsActive(n) // will be translated in typeCorrectNodes2Constraints
					else BoolLiteral(false) //ZeroToType(autoChooseOneType(expected))
				case IsLoaded(n) =>
					if (allIds.contains(n)) BoolLiteral(true) //OneToType(autoChooseOneType(expected))
					else BoolLiteral(false) //ZeroToType(autoChooseOneType(expected))
				case _ => 
					assert (expected >= ExpressionHelper.getType(expr, types, getLooseLiteralType))
					expr
			}
			if (addToBool) ToBool(result) else result
		}
		
		rewriteExpr(expr, expected)
	}
	
	private def inferTypes(constraints:Iterable[TypeConstraint], 
				   var2Types:mutable.Map[TypeVar, Type],
				   var2Reasons:mutable.Map[TypeVar, Set[String]]
				   ):(Map[String, Type], Iterable[TypeErrorException]) = {
		val var2Constraints = mutable.Map[TypeVar, List[TypeConstraint]]()
		val exceptions = mutable.ListBuffer[TypeErrorException]()
		def addPair(t:TypeVar, c:TypeConstraint)  {
			var2Constraints put (t, c::var2Constraints.getOrElse(t, List()))
		}
		for(c <- constraints) {
			addPair(c.left, c)
			addPair(c.right, c)
		}
		
		val toBePropagated:mutable.Set[TypeConstraint] = new mutable.LinkedHashSet[TypeConstraint]()
		toBePropagated ++= constraints
		while(!toBePropagated.isEmpty) {
			val c:TypeConstraint = toBePropagated.head
			IF[CompilationOptions.TRACE_TYPE_PROPAGATION#v] { 
				println("#################### ")
				println("enforcing " + c)
				println("old " + c.left.toStringWithValue(var2Types))
				println("old " + c.right.toStringWithValue(var2Types))
				println("old left reasons: " + var2Reasons.getOrElse(c.left, Set()))
				println("old right reasons: " + var2Reasons.getOrElse(c.right, Set()))
			}
			val (leftChanged, rightChanged) = 
				try {
					c.enforce(var2Types, var2Reasons)
				} catch {
					case e:TypeErrorException => 
						IF[CompilationOptions.TRACE_TYPE_PROPAGATION#v] { 
							println("Exception:" + e)
						}
						exceptions += e
						(false, false)
				}
			IF[CompilationOptions.TRACE_TYPE_PROPAGATION#v] { 
				println("new " + c.left.toStringWithValue(var2Types))
				println("new " + c.right.toStringWithValue(var2Types))
				println("added left constraints:" + (if (leftChanged) var2Constraints.get(c.left).get.toString else "{}"))
				println("added right constraints:" + (if (rightChanged) var2Constraints.get(c.right).get.toString else "{}"))
			}
			if (leftChanged) toBePropagated ++= var2Constraints.get(c.left).get
			if (rightChanged) toBePropagated ++= var2Constraints.get(c.right).get
			toBePropagated -= c
		}
		
		val types = var2Types.keySet.filter(_.isInstanceOf[FeatureTypeVar]).asInstanceOf[Set[FeatureTypeVar]]
			.map(v => (v.id, v.getType(var2Types))).toMap
		(types, exceptions)
	}
	
	private def literal2Type(l:Literal, literals2enumTypes:Map[Literal, List[EnumType]]):Type = l match {
		case i:IntLiteral =>
			val noEnumType:Type = 
				if (i.value == 0) new DisjunctiveType(NumberType, StringType, BoolType, SetType)
				else if (i.value == 1) new DisjunctiveType(NumberType, BoolType) 
				else NumberType
			val optEnums = literals2enumTypes.get(i)
			optEnums.flatten.foldLeft(noEnumType)((l, r) => l | r)
		case s:StringLiteral =>
			val optEnums = literals2enumTypes.get(s)
			optEnums.flatten.foldLeft((new DisjunctiveType(StringType, SetType)):Type)((l, r) => l | r)
		case _:RealLiteral => NumberType
		case EnumLiteral(v, t) => t
		case _:BoolLiteral => BoolType
	}
	
	private def toEnum(lv:LegalValuesOption):Option[Iterable[EnumItemLiteral]] = {
		import org.kiama.rewriting.Rewriter._ 
		val result = new mutable.ListBuffer[EnumItemLiteral]()
		for (r <- lv.ranges) r match {
			case m:MinMaxRange => return None
			case s:SingleValueRange => 
				if (collectl{
						case i:IdentifierRef => i
					}(s.v).size != 0)
					return None
				else {
					result += ExpressionHelper.evaluateUntypedExpr(s.v, immutable.Map[String, EnumItemLiteral]())
				}
		}
		Some(result)
	}
	private def getLiteral2Enums(ns:Iterable[Node]):Map[Literal, List[EnumType]] = {
		val literals2enumTypes = mutable.Map[Literal, List[EnumType]]()
		for (n <- ns) {
			def findEnums(n:Node) {
				if (n.legalValues.isDefined){
					val optEnum = toEnum(n.legalValues.get)
					if (optEnum.isDefined) {
						val enumType = new EnumType(optEnum.get.toSet + IntLiteral(0))
						enumType.items.foreach(l => literals2enumTypes.put(l, enumType::literals2enumTypes.getOrElse(l, List())))
					}				
				}
				n.children.foreach(c => findEnums(c))
			}
			findEnums(n)
		}
		literals2enumTypes
	}
	
	private def getEnumsFromSubString(s:String, literals2enumTypes:Map[Literal, List[EnumType]]) = {
		val matchedLiterals = literals2enumTypes.keySet.filter(_ match {
			case StringLiteral(ls) => ls.contains(s)
			case _ => false
		} )
		val enums = matchedLiterals.map(literals2enumTypes.get(_).get.toSet).flatten
		enums
	}
	
	private def generateTypeConstratins(ns:Iterable[Node],
								eccValues:Map[String, OptionValue],
 							    var2Types:mutable.Map[TypeVar, Type], 
								var2Reasons:mutable.Map[TypeVar, Set[String]],
								literals2enumTypes:Map[Literal, List[EnumType]]
								) : Iterable[TypeConstraint] = {
		// def toEnum(lv:LegalValuesOption):Option[Iterable[Literal]] = {
			// import org.kiama.rewriting.Rewriter._ 
			// val result = new mutable.ListBuffer[Literal]()
			// for (r <- lv.ranges) r match {
				// case m:MinMaxRange => return None
				// case s:SingleValueRange => 
					// if (collectl{
							// case i:IdentifierRef => i
						// }(s.v).size != 0)
						// return None
					// else
						// result += ExpressionHelper.evaluateAsGeneralExpr(s.v, immutable.Map[String, Literal]())
			// }
			// Some(result)
		// }
		val allNodes = (org.kiama.rewriting.Rewriter.collectl { case n:Node => (n.id, n) } (ns)).toMap
		
		def setFeatureType(id:String, t:Type) {
			val v = FeatureTypeVar(id)
			assert(v.getType(var2Types) == anyType, id + ":" + v.getType(var2Types))
			assert(var2Reasons.get(v) == None)
			v.setType(t, var2Types)
			var2Reasons put (v, Set(id))
		}
		def setExprType(v:ExpressionTypeVar, t:Type) {
			assert(v.getType(var2Types) == anyType | v.getType(var2Types) == t)
			var2Types.put(v, t)
		}
		var eid = 0
		def createExprVar(expr:Expression):ExpressionTypeVar = {eid += 1; ExpressionTypeVar(expr, eid)}
		val result = new mutable.ListBuffer[TypeConstraint]
		
		// val literals2enumTypes = mutable.Map[Literal, List[EnumType]]()
		// for (n <- ns) {
			// def findEnums(n:Node) {
				// if (n.legalValues.isDefined){
					// val optEnum = toEnum(n.legalValues.get)
					// if (optEnum.isDefined) {
						// val enumType = new EnumType(optEnum.get.toSet)
						// optEnum.get.foreach(l => literals2enumTypes.put(l, enumType::literals2enumTypes.getOrElse(l, List())))
						// setFeatureType(n.id, enumType)
					// }				
				// }
				// n.children.foreach(c => findEnums(c))
			// }
			// findEnums(n)
		// }
	
		def getTypeConstratinsFromEcc(values:Map[String, OptionValue]):Seq[TypeConstraint] = {
			val result = new mutable.ListBuffer[TypeConstraint]()
			for((id, value) <- values) {
				def addSingle(value:OptionValue) {
					value match {
						case IntOptionValue(v) => result += new EqualConstraint(id, literal2Type(IntLiteral(v), literals2enumTypes), id)
						case RealOptionValue(v) => result += new EqualConstraint(id, literal2Type(RealLiteral(v), literals2enumTypes), id)
						case StringOptionValue(v) => result += new EqualConstraint(id, literal2Type(StringLiteral(v), literals2enumTypes), id)
						case DoubleOptionValue(b, v) => addSingle(v)
						case NoneOptionValue => 
					}
				}
				addSingle(value)
			}
			result
		}
		
		def getTypeConstraints(n:Node) : Iterable[TypeConstraint] = {
		
			val result = new mutable.ListBuffer[TypeConstraint]
			
			n.flavor match {
				case Flavor.None => 
					assert(!n.legalValues.isDefined)
					assert(n.cdlType != CdlType.Package)
					setFeatureType(n.id, NumberType)
				case Flavor.Bool => 
					assert(!n.legalValues.isDefined)
					assert(n.cdlType != CdlType.Package)
					setFeatureType(n.id, BoolType)
				case _ => 
					if (n.cdlType == CdlType.Interface) {
						setFeatureType(n.id, NumberType)
					}
					else if (n.cdlType == CdlType.Package) {
						assert (n.flavor == Flavor.BoolData)
						setFeatureType(n.id, BoolType)
					}
					else if (n.legalValues.isDefined) {
						val optEnum = toEnum(n.legalValues.get)
						if (optEnum.isDefined) {
							val enumType = new EnumType(optEnum.get.toSet + IntLiteral(0))
							setFeatureType(n.id, enumType)
						}
						else {
							var containRange = false
							for(r <- n.legalValues.get.ranges) r match {
								case s:SingleValueRange => addSelfExprConstraints(s.v)
								case m:MinMaxRange => addSelfExprConstraints(m.low); addSelfExprConstraints(m.high)
									val lowVar = createExprVar(m.low)
									result ++= getExpTypeConstraints(lowVar, n.id)
									result += new EqualConstraint(lowVar, NumberType, n.id)
									val highVar = createExprVar(m.high)
									result ++= getExpTypeConstraints(highVar, n.id)
									result += new EqualConstraint(highVar, NumberType, n.id)
									containRange = true
							}
							if (containRange) setFeatureType(n.id, NumberType)
							else setFeatureType(n.id, new NegativeType(BoolType))
						}
					}
					else if (n.legalValues.isEmpty) {
						setFeatureType(n.id, new NegativeType(BoolType))
					}
			}
			
			
			def addSelfExprConstraints(expr:Expression) {
				val exprVar = createExprVar(expr)
				result += new EqualConstraint(n.id, exprVar, n.id)
				result ++= getExpTypeConstraints(exprVar, n.id)
			}
			
			if (n.defaultValue.isDefined) addSelfExprConstraints(n.defaultValue.get)
			if (n.calculated.isDefined) addSelfExprConstraints(n.calculated.get)
			
			for (expr <- n.reqs) {
				val exprVar = createExprVar(expr)
				result ++= getExpTypeConstraints(exprVar, n.id)
				IF[CompilationOptions.ENFORCE_BOOLEAN#v] {
					result += new EqualConstraint(exprVar, BoolType, n.id)
				}
			}
			for (expr <- n.activeIfs) {
				val exprVar = createExprVar(expr)
				result ++= getExpTypeConstraints(exprVar, n.id)
				IF[CompilationOptions.ENFORCE_BOOLEAN#v] {
					result += new EqualConstraint(exprVar, BoolType, n.id)
				}
			}
			for (c <- n.children) {
				result ++= getTypeConstraints(c)
			}
			result				
		}

		def getExpTypeConstraints(
				e:ExpressionTypeVar, 
				nodeID:String) : Iterable[TypeConstraint] = {
			val result = new mutable.ListBuffer[TypeConstraint]
			def createConstraint(left:TypeVar, right:TypeVar) {result += new EqualConstraint(left, right, nodeID)}
			def createInternalConstraints(l:ExpressionTypeVar) { result ++= getExpTypeConstraints(l, nodeID) }
			def createLogicConstraint(l:Expression, r:Expression) {
				val lv = createExprVar(l)
				val rv = createExprVar(r)
				IF[CompilationOptions.ENFORCE_BOOLEAN#v] {
					createConstraint(lv, BoolType)
					createConstraint(rv, BoolType)
				}
				setExprType(e, BoolType)
				createInternalConstraints(lv)
				createInternalConstraints(rv)
			}
			def createEqConstraint(l:Expression, r:Expression) {
				val lv = createExprVar(l)
				val rv = createExprVar(r)
				createConstraint(lv, rv)
				setExprType(e, BoolType)
				createInternalConstraints(lv)
				createInternalConstraints(rv)
			}
			def createIntCompareConstraint(l:Expression, r:Expression) {
				val lv = createExprVar(l)
				val rv = createExprVar(r)
				IF[CompilationOptions.ENUM_CONVERTIBLE#v] {
					result += new RightEnumConvertibleConstraint(NumberType, lv, nodeID)
					result += new RightEnumConvertibleConstraint(NumberType, rv, nodeID)
				}
				IF[(NOT[CompilationOptions.ENUM_CONVERTIBLE])#v] {
					createConstraint(lv, NumberType)
					createConstraint(rv, NumberType)
				}
				setExprType(e, BoolType)
				createInternalConstraints(lv)
				createInternalConstraints(rv)
			}
			def createIntConstraint(l:Expression, r:Expression) {
				val lv = createExprVar(l)
				val rv = createExprVar(r)
				IF[CompilationOptions.ENUM_CONVERTIBLE#v] {
					result += new RightEnumConvertibleConstraint(NumberType, lv, nodeID)
					result += new RightEnumConvertibleConstraint(NumberType, rv, nodeID)
				}
				IF[NOT[CompilationOptions.ENUM_CONVERTIBLE]#v] {
					createConstraint(lv, NumberType)
					createConstraint(rv, NumberType)
				}
				setExprType(e, NumberType)
				createInternalConstraints(lv)
				createInternalConstraints(rv)
			}
				
			e.expr match {
				case l:Literal => setExprType(e, literal2Type(l, literals2enumTypes))
				// case s:StringLiteral => 
					// val optEnums = literals2enumTypes.get(s)
					// if (optEnums.isDefined) {
						// setExprType(e, DisjunctiveType(Set[Type](StringType) ++ optEnums.get))
					// }
					// else setExprType(e, StringType)
				// case i:IntLiteral => 
					// val noEnumType:Type = 
						// if (i.value == 0) new DisjunctiveType(NumberType, StringType, BoolType)
						// else if (i.value == 1) new DisjunctiveType(NumberType, BoolType) 
						// else NumberType
					// val optEnums = literals2enumTypes.get(i)
					// if (optEnums.isDefined) {
						// setExprType(e, optEnums.get.foldLeft(noEnumType)((l, r) => l | r))
					// }
					// else setExprType(e, noEnumType)
				// case i:RealLiteral => setExprType(e, NumberType)
				case i:IdentifierRef => 
					createConstraint(e, i.id) 
				case Conditional(c, p, f) =>
					val cv = createExprVar(c)
					val pv = createExprVar(p)
					val fv = createExprVar(f)
					createConstraint(cv, BoolType)
					createConstraint(pv, fv)
					createConstraint(e, pv)
					createInternalConstraints(cv)
					createInternalConstraints(pv)
					createInternalConstraints(fv)
				case Or(l, r) => createLogicConstraint(l, r)
				case And(l, r) => createLogicConstraint(l, r)
				case Implies(l, r) => createLogicConstraint(l, r)
				case Eq(l, r) => createEqConstraint(l, r)
				case NEq(l, r) => createEqConstraint(l, r)
				case LessThan(l, r) => createIntCompareConstraint(l, r)
				case LessThanOrEq(l, r) => createIntCompareConstraint(l, r)
				case GreaterThanOrEq(l, r) => createIntCompareConstraint(l, r)
				case GreaterThan(l, r) => createIntCompareConstraint(l, r)
				case Plus(l, r) => createIntConstraint(l, r)
				case Minus(l, r) => createIntConstraint(l, r)
				case Times(l, r) => createIntConstraint(l, r)
				case Div(l, r) => createIntConstraint(l, r)
				case Mod(l, r) => createIntConstraint(l, r)
				case Dot(l, r) => 
					val lv = createExprVar(l)
					val rv = createExprVar(r)
					IF[CompilationOptions.ENUM_CONVERTIBLE#v] {
						result += new RightEnumConvertibleConstraint(SetType, lv, nodeID)
						result += new RightEnumConvertibleConstraint(SetType, rv, nodeID)
					}
					IF[(NOT[CompilationOptions.ENUM_CONVERTIBLE])#v] {
						createConstraint(lv, SetType)
						createConstraint(rv, SetType)
					}
					setExprType(e, SetType)
					createInternalConstraints(lv)
					createInternalConstraints(rv)
				case Not(i) => 
					val iv = createExprVar(i)
					// createConstraint(iv, BoolType)
					setExprType(e, BoolType)
					createInternalConstraints(iv)
				case GetData(n) =>
					createConstraint(e, n)
				case IsEnabled(n) => setExprType(e, BoolType)
				case IsActive(n) => setExprType(e, BoolType)
				case IsLoaded(n) => setExprType(e, BoolType)
				case ToInt(n) => 
					setExprType(e, NumberType)
					createInternalConstraints(createExprVar(n))
				case ToBool(n) => 
					setExprType(e, BoolType)
					createInternalConstraints(createExprVar(n))
				case ToString(n) => 
					setExprType(e, StringType)
					createInternalConstraints(createExprVar(n))
				case IsSubstr(l, r) => 
					val lv = createExprVar(l)
					val rv = createExprVar(r)
					setExprType(e, BoolType)
					createConstraint(lv, rv)
					val lrType = r match {
						case StringLiteral(s) => 
							val enums = getEnumsFromSubString(s, literals2enumTypes)
							if (enums.size >= 1) new DisjunctiveType(Set[Type]() ++ enums + SetType)
							else SetType
						case _ => 
							createInternalConstraints(rv)
							SetType
					}
					createConstraint(lv, lrType)
					createConstraint(rv, lrType)
					createInternalConstraints(lv)
			}
			result
		}
		
		for (n <- ns) {
			result ++= getTypeConstraints(n)
		}
		result ++= getTypeConstratinsFromEcc(eccValues)
		
		result
	}
}

