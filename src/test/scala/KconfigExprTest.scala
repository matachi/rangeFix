package ca.uwaterloo.gsd.rangeFix

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers

class KconfigExprTest extends FunSuite with ShouldMatchers {

  import Kconfig._

  implicit def toTInt(i: Int) = TInt(i)
  implicit def toTId(s: String) = TId(s, TTristateType)

  def toExpression(m: Map[String, TLiteral]) = m mapValues { _.toExpression }

  def eval(e: Expression, config: String => Literal = Map()) =
    ExpressionHelper.evaluateTypeCorrectExpression(e, config)

  test("min max mix") {
    eval(TMax(TMin(TYes, TNo), TYes).toExpression) should be (tristateYes)

    eval(TMax(TMin("A", "B"), "C").toExpression,
      toExpression(Map("A" -> TYes, "B" -> TNo, "C" -> TYes))) should be (tristateYes)

    eval(TMax(TMax(TMax(TMax("A", "B"), "C"), "A"), "B").toExpression,
      toExpression(Map("A" -> TYes, "B" -> TNo, "C" -> TYes))) should be (tristateYes)

    eval(mkNestedMax(40) toExpression, { k =>
      if (scala.util.Random.nextBoolean) tristateYes else tristateNo
    })
  }


  def mkNestedMax(initDepth: Int): TExpr = {
    def _mk(depth: Int): TExpr =
      if (depth == 0) TId(depth + "", TTristateType)
      else TMax(TId(depth + "", TTristateType), _mk(depth - 1))

    _mk(initDepth)
  }

}
