package ca.uwaterloo.gsd.rangeFix
import scala.collection.mutable.Queue
import java.util.Date
import scala.collection.mutable.Stack
import scala.collection._

abstract class GraphBase[T, U] {
  case class Edge(source: Node, target: Node, h: U) {
    def toTuple = (source.conflicts, target.conflicts, h)
  }
  case class Node(value: T) {
    var conflicts = value
    var descendants: List[Edge] = Nil
    var ancestors: List[Edge] = Nil
  }
  var nodes: List[Node] = Nil
}

class HSDAG[T] extends GraphBase[List[T], T]{

  private def addArc(source: Node, target: Node, value: T): Edge = {
	assert(source.conflicts.contains(value))
	assert(!target.conflicts.contains(value))
    val e = new Edge(source, target, value)
    source.descendants = e :: source.descendants
    target.ancestors = e :: target.ancestors
    e
  }

  private def addNode(value: List[T]) = {
    val n = new Node(value)
    nodes = n :: nodes
    n
  }

  private def getH(n: Node): List[T] = {
    var Hn: List[T] = Nil

    if (n.ancestors.isEmpty) // the source node has no ancestors
      Hn = Nil
    else { // the source node has ancestors, we have to recursively compute H(n)
      val ancestor  = n.ancestors.head  // we take only the first element because every H(n) of ancestors
                                        // will be the same (see 1. Reusing Nodes: (a))
      Hn = ancestor.h :: getH(ancestor.source)
    }
    return Hn
  }
  
  private def reuseNode(Hm: List[T]): Node = {
    for (n <- nodes) {
	  val Hn = getH(n)
      if (Hm forall Hn.contains)
        return n
    }
    return null
  }
  
  private def dropIndex[T](xs: List[T], n: Int): List[T] = {
    val (l1, l2) = xs splitAt n
    l1 ++ l2.tail
  }


  private def getSupersetNodes(conflict: List[T]): List[Node] = {
    var supersetNodes: List[Node] = Nil
    var isSuperset = true

    for (n <- nodes) {
      if(n.conflicts.length>conflict.length) {
        for(c <- conflict) {
          if(!n.conflicts.contains(c))
            isSuperset = false

        }
      } else {
        isSuperset = false
      }

      if(isSuperset) {
        supersetNodes = n :: supersetNodes
      }
      isSuperset = true
    }
    return supersetNodes
  }

  private def cutBranch(edge: Edge) : List[Node] = {
    var deletedNodes: List[Node] = Nil
    val parent = edge.source
    val nodeToPrune = edge.target
    // remove the edge from the parent
    var i = parent.descendants.indexOf(edge)
    parent.descendants = dropIndex(parent.descendants,i)
    // cut the branch
    if(nodeToPrune.ancestors.length==1) {
      // cut the children
      for(dn <- nodeToPrune.descendants) {
        deletedNodes = cutBranch(dn) ++ deletedNodes
      }

      // remove the node from the list of nodes
      i = nodes.indexOf(nodeToPrune)
      nodes = dropIndex(nodes,i)
      deletedNodes = nodeToPrune :: deletedNodes
	  
	  // remove the edge from the ancestor list
	  nodeToPrune.ancestors = Nil
    }
	else {
		// remove the edge from the ancestor list
		var i = nodeToPrune.ancestors.indexOf(edge)
		nodeToPrune.ancestors = dropIndex(nodeToPrune.ancestors,i)
    }
    return deletedNodes
  }
  
  private def pruneDescendants (node: Node, conflicts: List[T] ) : List[Node] = {
    var deletedNodes: List[Node] = Nil
    for(c <- node.conflicts) {
      if(!conflicts.contains(c)) {
        for(d <- node.descendants) {
          if(d.h==c) {
            deletedNodes = cutBranch(d) ++ deletedNodes
          }
        }
      }
    }
    return deletedNodes
  }
  
  private def closingNode(Hn: List[T]): Boolean = {
    var isSuperset = true

    for (m <- nodes) {
      var Hm = getH(m)
      if(Hm.length<Hn.length && m.conflicts.isEmpty) {
        for(h <- Hm) {
          if(!Hn.contains(h))
            isSuperset = false
        }
      } else {
        isSuperset = false
      }

      if(isSuperset) {
        return true
      }
      isSuperset = true
    }
    return false

  }


  private def relabel(node: Node, label: List[T]) = {
    node.conflicts = label
  }
  
  private def removeSubset[T](subset: List[T], list: List[T]) : List[T] = {
	list.filterNot(subset.contains)
  }
  
  def getDiagnoses(getCore : Traversable[T] => Option[List[T]], coreMinimal : Boolean = false): List[List[T]] = {
    var diagnoses: List[List[T]] = Nil
    var stack = new Stack[Node]

    val root = computeHSDAG(getCore, coreMinimal)

    if(!root.isEmpty)
      stack.push(root.get)

    var i: Int = 0
    while(!stack.isEmpty) {
      i += 1
      var node = stack.pop

      if(node.conflicts.isEmpty) {
        var Hn = getH(node)
        if (!diagnoses.contains(Hn))
          diagnoses = getH(node) :: diagnoses
      } else {
        for (e <- node.descendants) {
          stack.push(e.target)
        }
      }
     }
     print("%d iterations ".format(i))

    return diagnoses
  } 
  
  private val reportTime = false
  private val showTrace = false
  
  // None means unsat
  private def computeHSDAG(getCore : List[T] => Option[List[T]], coreMinimal : Boolean = false): Option[Node] = {
	var getCoreMillis = 0L
	var getCoreTimes = 0
  	def getCoreWithTimeReport(l:List[T]):Option[List[T]] = {
		if (reportTime) {
			val result = Timer.measureTime (getCore(l))
			getCoreMillis += Timer.lastExecutionMillis
			getCoreTimes += 1
			result
		}
		else getCore(l)
	}
	def trace(msg:Object) { if (showTrace) println(msg.toString) }
    var mus  = getCoreWithTimeReport(List()) // minimal unsat subset (QuickXplain)
	trace(mus)
	var conflicts = if (mus!=None) {
	  if (mus.get.isEmpty) return None
	  mus.get
	} else {
	  Nil
	}
	// Step 1: create root
	val root = addNode(conflicts)
	// Step 2: process nodes in BFS
	val queue = new Queue[Node]
	queue += root
	while (!queue.isEmpty) {
		var node = queue.dequeue

		if(!node.conflicts.isEmpty) {  // we only look into nodes that are not marked with V

		  var conflictsInCurNode = node.conflicts
		  while(conflictsInCurNode.length > 0){
			val c = conflictsInCurNode.head
			conflictsInCurNode = conflictsInCurNode.tail
			trace("Trying..." + c)
			var Hm = c :: getH(node)
			if(!closingNode(Hm)) {  // 2. Closing. Here closing is implemented by not exploring the
									// descendants, i.e. not putting it in the queue and expanding the node.
			  var reusedNode = reuseNode(Hm)
			  if(reusedNode!=null) { // 1. Reusing Nodes
				addArc(node,reusedNode,c) // Reuse node
				trace("Reused")
			  } else {
				mus  = getCoreWithTimeReport(Hm)
				trace(mus)
				if (showTrace) if (Console.readLine.trim == "x") return None
				if (mus!=None) {
				  if (mus.get.isEmpty) return None
				  conflicts = mus.get
				} else {
				  conflicts = Nil
				}
				var cnode = addNode(conflicts)
				addArc(node,cnode,c)
				queue += cnode
				if(!coreMinimal && !conflicts.isEmpty) {          // 3. Pruning, which is only needed when the core can be non-minimal
				  var supersetNodes = getSupersetNodes(conflicts)
				  if(!supersetNodes.isEmpty) {
					while(!supersetNodes.isEmpty) {
					  // remove the first element from the list
					  var s = supersetNodes.head
					  supersetNodes = supersetNodes.tail
					  // cut branches that that are not in conflict
					  var removedNodes = pruneDescendants(s,conflicts)
					  // remove the nodes from the list that have been pruned
					  supersetNodes = removeSubset[Node](removedNodes,supersetNodes)
	
					  queue.dequeueAll(removedNodes.contains)
					  if (removedNodes.contains(node)) {
						conflictsInCurNode = List()
					  }
					  else if (s == node) {
						conflictsInCurNode = conflictsInCurNode.filterNot(s.conflicts.filterNot(conflicts.contains).contains)
					  }

					  // relabel s with conflict
					  relabel(s,conflicts)
					  
					  trace("pruned")
					}
				  }
				}
			  }
			}
			else trace("Closed")
		  }
		}
	}
	val end = (new Date).getTime()
	if (reportTime) {
		println("Total number of cores queried:" + getCoreTimes)
		println("Average Time per call(ms):" + (getCoreMillis.toDouble  / getCoreTimes))
	}
	return Some(root)
  }
}