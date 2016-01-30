package ca.uwaterloo.gsd.rangeFix
import collection._


trait FixUnit {
	def variables():Set[String]
	def constraint():Expression
}

case class DataFix(units:Traversable[FixUnit]) {
	override def toString = {
		val sb = new StringBuilder()
		sb += '['
		if (units.size > 0) sb ++= units.map(_.toString).reduce(_ + ", " + _)
		sb += ']'
		sb.toString
	}
	
	def variables():Set[String] = units.map(_.variables).toSet.flatten
	def constraint() = {
		val constraints = units.map(_.constraint)
		if (constraints.size > 0) {
			constraints.reduceLeft(And(_, _))
		}
		else
			BoolLiteral(true)
	}

}

case class AssignmentUnit(variable:String, expr:Expression) extends FixUnit {
	override def toString = {
		variable + ":=" + expr.toString
	}
	override def variables():Set[String] = {
		Set(variable)
	}
	override def constraint():Expression = Eq(IdentifierRef(variable), expr)
}

case class RangeUnit(vars:Set[String], override val constraint:Expression) extends FixUnit {
	override def toString = {
		val sb = new StringBuilder
		if (vars.size == 1)
			sb ++= vars.head
		else if (vars.size > 1) {
			sb += '('
			sb ++= vars.map(_.toString).reduce(_ + ", " + _)
			sb += ')'
		} else {
			sb ++= "No variables"
		}
		sb += ':'
		sb ++= constraint.toString
		sb.toString
	}
	
	def ++ (that:RangeUnit) = {
		val newConstaint = 
			if (constraint == BoolLiteral(true)) 
				that.constraint
			else if (that.constraint == BoolLiteral(true))
				this.constraint
			else And(constraint, that.constraint)
		new RangeUnit(vars ++ that.vars, newConstaint)
	}
	
	override def variables():Set[String] = vars
}

