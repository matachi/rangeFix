/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gsd.cdl.parser.adapter

import gsd.iml.ast.expression._
import gsd.iml.ast.expression
import gsd.iml.ast.feature._
import gsd.iml.ast.constraint._
import ca.uwaterloo.gsd.rangeFix
import ca.uwaterloo.gsd.rangeFix._

object ImlExpressionToCDLExpression {

  def apply(e:expression.Expression) : rangeFix.Expression = {

    if (e.isInstanceOf[AndExpression]) {
      val exp = e.asInstanceOf[AndExpression]
      return new And(ImlExpressionToCDLExpression(exp.getLeft()),
                      ImlExpressionToCDLExpression(exp.getRight()))
    }

    if (e.isInstanceOf[OrExpression]) {
      val exp = e.asInstanceOf[OrExpression]
      return new Or(ImlExpressionToCDLExpression(exp.getLeft()),
                     ImlExpressionToCDLExpression(exp.getRight()))
    }

    if (e.isInstanceOf[BitwiseAndExpression]) {
      val exp = e.asInstanceOf[BitwiseAndExpression]
	  throw new RuntimeException("unexpected band operator")
      // return new BtAnd(ImlExpressionToCDLExpression(exp.getLeft()),
                        // ImlExpressionToCDLExpression(exp.getRight()))
    }

    if (e.isInstanceOf[BitwiseOrExpression]) {
      val exp = e.asInstanceOf[BitwiseOrExpression]
	  throw new RuntimeException("unexpected bor operator")
      // return new BtOr(ImlExpressionToCDLExpression(exp.getLeft()),
                      // ImlExpressionToCDLExpression(exp.getRight()))
    }

    if (e.isInstanceOf[BitwiseXorExpression]) {
      val exp = e.asInstanceOf[BitwiseXorExpression]
	  throw new RuntimeException("unexpected xor operator")
      // return new BtXor(ImlExpressionToCDLExpression(exp.getLeft()),
                        // ImlExpressionToCDLExpression(exp.getRight()))
    }

    if (e.isInstanceOf[BitwiseLeftShiftExpression]) {
      val exp = e.asInstanceOf[BitwiseLeftShiftExpression]
	  throw new RuntimeException("unexpected lshift operator")
      // return new BtLeft(ImlExpressionToCDLExpression(exp.getLeft()),
                        // ImlExpressionToCDLExpression(exp.getRight()))
    }

    if (e.isInstanceOf[BitwiseRightShiftExpression]) {
      val exp = e.asInstanceOf[BitwiseRightShiftExpression]
	  throw new RuntimeException("unexpected rshit operator")
      // return new BtRight(ImlExpressionToCDLExpression(exp.getLeft()),
                          // ImlExpressionToCDLExpression(exp.getRight()))
    }

    if (e.isInstanceOf[FunctionCallExpression]) {
      val exp = e.asInstanceOf[FunctionCallExpression]
      val args = scala.collection.mutable.ListBuffer[rangeFix.Expression]()

      val iterator = exp.getArguments().iterator
      while(iterator.hasNext) {
        args += ImlExpressionToCDLExpression(iterator.next)
      }
	  
	  exp.getFunctionName match {
		case "get_data" => 
			if (args.size != 1) throw new RuntimeException("unexpected arguments: " + args)
			return new GetData(args.head.asInstanceOf[IdentifierRef].id)
		case "is_active" =>
			if (args.size != 1) throw new RuntimeException("unexpected arguments: " + args)
			return new IsActive(args.head.asInstanceOf[IdentifierRef].id)
		case "is_enabled" => 
			if (args.size != 1) throw new RuntimeException("unexpected arguments: " + args)
			return new IsEnabled(args.head.asInstanceOf[IdentifierRef].id)
		case "is_loaded" => 
			if (args.size != 1) throw new RuntimeException("unexpected arguments: " + args)
			return new IsLoaded(args.head.asInstanceOf[IdentifierRef].id)
		case "is_substr" => 
			if (args.size != 2) throw new RuntimeException("unexpected arguments: " + args)
			return new IsSubstr(args.head, args.tail.head)
		case _ => throw new RuntimeException("unexpected functions: " + exp.getFunctionName)
	  }
    }

    if (e.isInstanceOf[EqualExpression]) {
      val exp = e.asInstanceOf[EqualExpression]
      return new Eq(ImlExpressionToCDLExpression(exp.getLeft()),
                    ImlExpressionToCDLExpression(exp.getRight))
    }

    if (e.isInstanceOf[NotEqualExpression]) {
      val exp = e.asInstanceOf[NotEqualExpression]
      return new NEq(ImlExpressionToCDLExpression(exp.getLeft()),
                     ImlExpressionToCDLExpression(exp.getRight))
    }

    if (e.isInstanceOf[LessThanExpression]) {
      val exp = e.asInstanceOf[LessThanExpression]
      return new LessThan(ImlExpressionToCDLExpression(exp.getLeft()),
                          ImlExpressionToCDLExpression(exp.getRight))
    }

    if (e.isInstanceOf[LessThanEqualExpression]) {
      val exp = e.asInstanceOf[LessThanEqualExpression]
      return new LessThanOrEq(ImlExpressionToCDLExpression(exp.getLeft()),
                              ImlExpressionToCDLExpression(exp.getRight))
    }

    if (e.isInstanceOf[GreaterThanExpression]) {
      val exp = e.asInstanceOf[GreaterThanExpression]
      return new GreaterThan(ImlExpressionToCDLExpression(exp.getLeft()),
                             ImlExpressionToCDLExpression(exp.getRight))
    }

    if (e.isInstanceOf[GreaterThanEqualExpression]) {
      val exp = e.asInstanceOf[GreaterThanEqualExpression]
      return new GreaterThanOrEq(ImlExpressionToCDLExpression(exp.getLeft()),
                                 ImlExpressionToCDLExpression(exp.getRight))
    }

    if (e.isInstanceOf[ModExpression]) {
      val exp = e.asInstanceOf[ModExpression]
      return new Mod(ImlExpressionToCDLExpression(exp.getLeft()),
                      ImlExpressionToCDLExpression(exp.getRight))
    }

    if (e.isInstanceOf[PlusExpression]) {
      val exp = e.asInstanceOf[PlusExpression]
      return new Plus(ImlExpressionToCDLExpression(exp.getLeft()),
                      ImlExpressionToCDLExpression(exp.getRight))
    }

    if (e.isInstanceOf[MinusExpression]) {
      val exp = e.asInstanceOf[MinusExpression]
      return new Minus(ImlExpressionToCDLExpression(exp.getLeft()),
                        ImlExpressionToCDLExpression(exp.getRight))
    }

    if (e.isInstanceOf[TimesExpression]) {
      val exp = e.asInstanceOf[TimesExpression]
      return new Times(ImlExpressionToCDLExpression(exp.getLeft()),
                       ImlExpressionToCDLExpression(exp.getRight))
    }

    if (e.isInstanceOf[DivideExpression]) {
      val exp = e.asInstanceOf[DivideExpression]
      return new Div(ImlExpressionToCDLExpression(exp.getLeft()),
                      ImlExpressionToCDLExpression(exp.getRight))
    }

    if (e.isInstanceOf[DotExpression]) {
      val exp = e.asInstanceOf[DotExpression]
      return new Dot(ImlExpressionToCDLExpression(exp.getLeft()),
                     ImlExpressionToCDLExpression(exp.getRight))
    }

    if (e.isInstanceOf[NotExpression]) {
      val exp = e.asInstanceOf[NotExpression]
      return new Not(ImlExpressionToCDLExpression(exp.getExpression()))
    }

    if (e.isInstanceOf[IdentifierExpression]) {
      val exp = e.asInstanceOf[IdentifierExpression]
      return new IdentifierRef(exp.getId)
    }

    if (e.isInstanceOf[LongLiteralExpression]) {
      val exp = e.asInstanceOf[LongLiteralExpression]
      return new IntLiteral(exp.get().longValue())
    }

    if (e.isInstanceOf[StringLiteralExpression]) {
      val exp = e.asInstanceOf[StringLiteralExpression]
      return new StringLiteral(exp.get())
    }

    if (e.isInstanceOf[BooleanLiteralExpression]) {
      val exp = e.asInstanceOf[BooleanLiteralExpression]
	  throw new RuntimeException("unexpected Boolean value")
      // if (exp.get.equals(java.lang.Boolean.TRUE))
        // return new True()

      // return new False()
    }

    if (e.isInstanceOf[ConditionalExpression]) {
      val exp = e.asInstanceOf[ConditionalExpression]
      return new Conditional(ImlExpressionToCDLExpression(exp.getCondition()),
                             ImlExpressionToCDLExpression(exp.getPass()),
                             ImlExpressionToCDLExpression(exp.getFail()))
    }

    //TODO: DEAL WITH DOUBLES
    if (e.isInstanceOf[DoubleLiteralExpression]) {
      val value: scala.Double = (e.asInstanceOf[DoubleLiteralExpression].get)
      return new RealLiteral(value)
    }

    if (e.isInstanceOf[ImpliesExpression]) {
      val exp = e.asInstanceOf[ImpliesExpression]
      return new Implies(ImlExpressionToCDLExpression(exp.getLeft()),
                    ImlExpressionToCDLExpression(exp.getRight()))
    }

    throw new Exception("Unknown adapter for " + e.getClass().getName())
  }
}

/* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

object ImlFeatureToNode {
  def apply(f:Feature) : Node = {

    val id = f.getId

    val cdlType = ImlFeatureTypeToCDLType(f.getType())

    val display = f.getDisplay()

    var description : Option[String] = None
    if (f.getDescription != null)
      description = Some(f.getDescription)

    val flavor = ImlFlavorToCDLFlavor(f.getFlavor())	

    var defaultValue : Option[rangeFix.Expression] = None
    if (f.getDefaultValue() != null)
      defaultValue = Some(ImlExpressionToCDLExpression(f.getDefaultValue().getExpression()))

    var calculated : Option[rangeFix.Expression] = None
    if (f.getCalculated() != null)
     calculated = Some(ImlExpressionToCDLExpression(f.getCalculated().getExpression()))

    var legalValues : Option[LegalValuesOption] = None
    if (f.getLegalValues() != null)
      legalValues = Some(ImlLegalValuesConstraintToCDLValuesOption(f.getLegalValues()))

    val reqs = ImlConstraintsToCDLExpressions(f.getRequires)
    val activeIf = ImlConstraintsToCDLExpressions(f.getActiveIfs)
    val impls = ImlConstraintsToCDLExpressions(f.getImpls).map(n => n match {
			case IdentifierRef(id) => id
			case _ => throw new RuntimeException("unexpected implements expression")
		}).toSet
    val children = ImlFeatureListToImlNodeList(f.getSubfeatures)

    return new Node(
       id,
       cdlType,
       display,
       description,
       flavor,
       defaultValue,
       calculated,
       legalValues,
       reqs,
       activeIf,
       impls,
       children)
  }
}

/* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

object ImlFeatureTypeToCDLType {
  def apply(t:FeatureType) : CdlType.Value = {
	import CdlType._
    if (t.isInstanceOf[ComponentFeatureType])
      return Component

    if (t.isInstanceOf[InterfaceFeatureType])
      return Interface

    if (t.isInstanceOf[PackageFeatureType])
      return Package

    return Option
  }
}

/* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

object ImlFlavorToCDLFlavor {
  def apply(f:gsd.iml.ast.flavor.Flavor) : Flavor.Value = {
	import Flavor._
	
    if (f.isInstanceOf[gsd.iml.ast.flavor.NoneFlavor])
      return None

    if (f.isInstanceOf[gsd.iml.ast.flavor.BoolFlavor])
      return Bool

    if (f.isInstanceOf[gsd.iml.ast.flavor.DataFlavor])
      return Data;

    return BoolData;
  }
}

/* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

object ImlLegalValuesConstraintToCDLValuesOption {
  def apply(legalValues : LegalValuesConstraint) : LegalValuesOption =  {
    val ranges = scala.collection.mutable.ListBuffer[Range]()
    val iterator = legalValues.getExpressions().iterator

    while(iterator.hasNext) {
      val exp = iterator.next
      if (exp.isInstanceOf[IntervalExpression])
        ranges += (ImlIntervalExpressionToMinMaxRange(exp.asInstanceOf[IntervalExpression]))
      else 
        ranges += (new SingleValueRange(ImlExpressionToCDLExpression(exp)))
    }    
    return new LegalValuesOption(ranges.toList)
  }
}

/* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

object ImlIntervalExpressionToMinMaxRange {
  def apply(e: IntervalExpression) : Range = {
      val interval = e.asInstanceOf[IntervalExpression] ;
      return new MinMaxRange(ImlExpressionToCDLExpression(interval.getFrom()),
                             ImlExpressionToCDLExpression(interval.getTo()))
  }
}

/* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

object ImlConstraintsToCDLExpressions {
  def apply(constraints:java.util.List[_ <: UnaryImlConstraint]) : List[rangeFix.Expression] = {
    var res = scala.collection.mutable.ListBuffer[rangeFix.Expression]()
    if (! gsd.iml.util.CollectionsUtils.isEmpty(constraints)) {
      val iterator = constraints.iterator
      while(iterator.hasNext) {
        res += (ImlExpressionToCDLExpression(iterator.next.getExpression()))
      }
    }
    return res.toList
  }
}

/* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

object ImlFeatureListToImlNodeList {
  def apply(features:java.util.List[Feature]) : List[Node] = {

    var res = scala.collection.mutable.ListBuffer[Node]()
    if (! gsd.iml.util.CollectionsUtils.isEmpty(features)) {
      val iterator = features.iterator
      while(iterator.hasNext) {
        res += (ImlFeatureToNode(iterator.next))
      }
    }
    return res.toList
  }
}


