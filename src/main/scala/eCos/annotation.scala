package ca.uwaterloo.gsd.rangeFix
import scala.collection._


abstract class NodeAnnotation(appliableFiles:Set[String]) {
	def apply(n:Node):Node
	def apply(n:Node, file:String):Node = {
		if (appliableFiles.size > 0 && !appliableFiles.contains(file)) {
			n
		}
		else {
			apply(n)
		}
	}
}

class DefaultAnnotation(newDefault:Expression, oldDefault:Option[Expression], appliableFiles:Set[String] = Set()) extends NodeAnnotation(appliableFiles) { 
	override def apply(n:Node):Node = {
		if (oldDefault.isDefined && n.defaultValue != oldDefault)
			return n
		Node(n.id,
             n.cdlType,
             n.display,
             n.description,
             n.flavor,
             Some(newDefault), 
             n.calculated,
             n.legalValues,
             n.reqs,
             n.activeIfs,
             n.implements, 
             n.children)			
	}
}

case class CalculatedAnnotation(newCalculated:Expression, oldCalculated:Option[Expression], appliableFiles:Set[String] = Set())  extends NodeAnnotation(appliableFiles) {
	override def apply(n:Node):Node = {
		if (oldCalculated.isDefined && n.calculated != oldCalculated)
			return n
		Node(n.id,
             n.cdlType,
             n.display,
             n.description,
             n.flavor,
             n.defaultValue, 
             Some(newCalculated),
             n.legalValues,
             n.reqs,
             n.activeIfs,
             n.implements, 
             n.children)			
	}
}

class LegalValuesAnnotation(legalValues:LegalValuesOption, oldLegalValues:Option[LegalValuesOption], appliableFiles:Set[String] = Set()) extends NodeAnnotation(appliableFiles) {
	override def apply(n:Node):Node = {
		if (oldLegalValues.isDefined && n.legalValues != oldLegalValues)
			return n
		Node(n.id,
             n.cdlType,
             n.display,
             n.description,
             n.flavor,
             n.defaultValue, 
             n.calculated,
             Some(legalValues),
             n.reqs,
             n.activeIfs,
             n.implements, 
             n.children)			
	}
}


class ReqAnnotation(req:Expression, index:Int, oldReq:Option[Expression], appliableFiles:Set[String] = Set()) extends NodeAnnotation(appliableFiles) {
	override def apply(n:Node):Node = {
		if (n.reqs.size - 1 < index)
			return n
		if (oldReq.isDefined && n.reqs(index) != oldReq.get)
			return n
		Node(n.id,
             n.cdlType,
             n.display,
             n.description,
             n.flavor,
             n.defaultValue, 
             n.calculated,
             n.legalValues,
             n.reqs.updated(index, req),
             n.activeIfs,
             n.implements, 
             n.children)			
	}
}

class ActiveIfAnnotation(activeIf:Expression, index:Int, oldActiveIf:Option[Expression], appliableFiles:Set[String] = Set()) extends NodeAnnotation(appliableFiles) {
	override def apply(n:Node):Node = {
		if (n.activeIfs.size - 1 < index)
			return n
		if (oldActiveIf.isDefined && n.activeIfs(index) != oldActiveIf.get)
			return n
		Node(n.id,
             n.cdlType,
             n.display,
             n.description,
             n.flavor,
             n.defaultValue, 
             n.calculated,
             n.legalValues,
             n.reqs,
             n.activeIfs.updated(index, activeIf),
             n.implements, 
             n.children)
	}
}

object NullAnnotation extends NodeAnnotation(Set()) {
	override def apply(n:Node) = n
}

case class TypeAnnotation(id:String, t:Type, applicableFiles:Set[String] = Set[String]())

class TypeAnnotations(anns:Iterable[TypeAnnotation]) {
	def filter(nodes:Iterable[Node], file:String):TypeAnnotations = {
		import org.kiama.rewriting.Rewriter._ 
		val ids = collects {
			case n:Node => n.id
			case i:IdentifierRef => i.id
		} (nodes)
		new TypeAnnotations(anns.filter(x => ids.contains(x.id) && (x.applicableFiles.size == 0 || x.applicableFiles.contains(file))))
	}
	def toTypeConstraints() = {
		anns.map(ann => {
			new EqualConstraint(new FeatureTypeVar(ann.id), new ConstantTypeVar(ann.t), "Annotation")
		})
	}
}

class NodeAnnotations {
	
	val annotations = mutable.Map[String, List[NodeAnnotation]]()
	
	def add (id:String, ann:NodeAnnotation) {
		annotations.put(id, ann::annotations.getOrElse(id, List[NodeAnnotation]()))
	}
	
	def apply(ns:Iterable[Node], file:String):Iterable[Node] = {
		import org.kiama.rewriting.Rewriter._ 
		rewrite(everywherebu ( rule[Any] {
			case n : Node => 
				val applied = apply(n, file, annotations.getOrElse(n.id, List(NullAnnotation)))
				applied
		} )) (ns)
	}
	
	private def apply(n:Node, file:String, annotations:Iterable[NodeAnnotation]) = {
		// if (annotations.size > 0 && annotations.head != NullAnnotation) println(annotations)
		annotations.foldLeft(n)((n, a) => a.apply(n, file))
	}
}
