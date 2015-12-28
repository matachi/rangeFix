package ca.uwaterloo.gsd.rangeFix
import collection._

class LazyMap[K, V](default:K=>V) extends (K=>V) {
	private var buffer:mutable.Map[K, V] = mutable.Map[K, V]()
	def get(key:K):V = {
		val result = buffer.get(key)
		if (result.isDefined) result.get
		else {
			val newResult = default(key)
			buffer += key -> newResult
			newResult
		}
	}
	
	def apply(key:K):V = get(key)
	
}
