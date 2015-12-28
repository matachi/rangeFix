package ca.uwaterloo.gsd.rangeFix
import collection._

object StateDebugger {
    @annotation.elidable(annotation.elidable.ASSERTION)
	def assert(state:Any)(f: =>Boolean) {
		if (enabledStates contains state) {
			Predef.assert(f)
		}
	}
	
    @annotation.elidable(annotation.elidable.ASSERTION)
	def trace(state:Any)(msg: =>Any) {
		if (enabledStates contains state) {
			java.lang.System.out.print(msg.toString)
		}
	}
	
	private val enabledStates = mutable.Set[Any]()
	def enter(state:Any) {
		enabledStates += state
	}
	def exit(state:Any) {
		enabledStates -= state
	}
}
