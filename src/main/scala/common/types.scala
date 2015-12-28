package ca.uwaterloo.gsd.rangeFix
import scala.collection._

abstract class Type extends Serializable{
	def getTypeSet():Set[Type] = {
		assert (!this.isInstanceOf[NegativeType])
		this match {
			case DisjunctiveType(s) => s
			case _ => Set(this)
		}
	}
	
	private def set2Type(newTypes:Set[Type]):Option[Type] = {
		if (newTypes.size == 1) return Some(newTypes.head)
		else if (newTypes.size == 0) return None
		else return Some(DisjunctiveType(newTypes))
	}
	
	def & (that:Type) : Option[Type] = {
		this match {
			case NegativeType(excluded) => that match {
				case NegativeType(excluded2) => Some(NegativeType(excluded++excluded2))
				case _ => set2Type(that.getTypeSet -- excluded)
			}
			case _ => that match {
				case _:NegativeType => that & this
				case _ => 
					val thisTypes = this.getTypeSet
					val thatTypes = that.getTypeSet
					val newTypes:Set[Type] = thisTypes & thatTypes
					set2Type(newTypes)
			}
		}
	}
	
	def neg : Option[Type] = this match {
		case NegativeType(t) if t == Set() => None
		case NegativeType(t) => set2Type(t)
		case _ => Some(NegativeType(this.getTypeSet))
	}
	
	def | (that:Type) : Type = {
		if (this == TypeHelper.anyType | that == TypeHelper.anyType) TypeHelper.anyType
		else {
			val x = (this.neg.get & that.neg.get)
			if (x.isDefined) x.get.neg.get
			else TypeHelper.anyType
		}
	}
	
	def - (that:Type) : Option[Type] = that.neg.map(this & _).getOrElse(None)
	
	def >= (that:Type):Boolean = (this & that) == Some(that)
	
	def <= (that:Type) : Boolean = that >= this
	def > (that:Type) : Boolean = this >= that && this != that
	def < (that:Type) : Boolean = that > this

}

trait SingleType extends Type
case object StringType extends SingleType {
	override def toString = "String"
}
case object NumberType extends SingleType {
	override def toString = "Number"
}
// case object RealType extends Type {
	// override def toString = "Real"
// }
case object BoolType extends SingleType {
	override def toString = "Bool"
}
case class EnumType(items:immutable.ListSet[EnumItemLiteral]) extends SingleType {
	assert (items.size > 0)
	def this(items:Iterable[EnumItemLiteral]) = this(immutable.ListSet() ++ items)
	override def toString = "Enum[" + items.map(_.toString).reduceLeft((a, b)=>a + "," + b) + "]"
	def isStringEnum:Boolean = items.forall(i => i.isInstanceOf[StringLiteral] | i == IntLiteral(0))
	def isIntEnum:Boolean = items.forall(_.isInstanceOf[IntLiteral])
}

case object SetType extends SingleType {
	override def toString = "StringSet"
}
// case object TristateType extends SingleType {
// 	override def toString = "Tristate"
// }

case class NegativeType(excludedTypes:Set[Type]) extends Type {
	assert(excludedTypes.forall(!_.isInstanceOf[DisjunctiveType]))
	assert(excludedTypes.forall(!_.isInstanceOf[NegativeType]))
	def this(types:Type*) = this(new immutable.ListSet() ++ types)
	override def toString = if (excludedTypes == Set()) "Any" 
		else "~(" + excludedTypes.map(_.toString).reduceLeft(_ + "|" + _.toString) + ")"
}

case class DisjunctiveType(types:Set[Type]) extends Type {
	assert(types.size > 1)
	assert(types.forall(!_.isInstanceOf[DisjunctiveType]))
	assert(types.forall(!_.isInstanceOf[NegativeType]))
	
	def this(types:Type*) = this(new immutable.ListSet() ++ types)
			
	override def toString = "(" + types.tail.foldLeft(types.head.toString)(_ + "|" + _.toString) + ")"
}
