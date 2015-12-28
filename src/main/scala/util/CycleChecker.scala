package ca.uwaterloo.gsd.rangeFix
import collection._

class CyclicRefException(ids:Iterable[String]) extends Exception ("Cyclic references detected:" + ids)
class CycleChecker {
	val visitedVars = mutable.Stack[String]()
	def checkCycle[T](id:String)(func: => T):T = {
		if (visitedVars.contains(id)) throw new CyclicRefException(id +: visitedVars)
		visitedVars.push(id)
		val r = func
		visitedVars.pop
		r
	}

}