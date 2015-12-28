package ca.uwaterloo.gsd.rangeFix
import collection._

object Util {
	def reverse[K,V](m:Iterable[(K,V)]):Map[V,List[K]] = {
		val result = mutable.Map[V, List[K]]()
		for((k,v) <- m) {
			result.get(v) match {
				case Some(collection) => result.put(v, k::collection)
				case None => result.put(v, List(k))
			}
		}
		result.toMap
	} }