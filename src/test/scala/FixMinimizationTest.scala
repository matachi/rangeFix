package ca.uwaterloo.gsd.rangeFix
import  org.scalatest._
import  org.scalatest.matchers.ShouldMatchers
import  scala.collection.mutable.Stack
import  scala.collection.mutable
import  java.io._

class FixMinimizationTest extends FunSuite with ShouldMatchers{
  import Expression._
  test("print Test"){
    val z3=new Z3()
   


    val expr =Not(IdentifierRef("p")|IdentifierRef("q"))
    var types =Map[String, SingleType]()
    types += ("p"-> BoolType)
    types += ("q"->BoolType)
    var confs = Map[String, Literal]()
    confs += ("p"->BoolLiteral(true))//只是因为测试的时候需要构造translator才需要confs
    confs += ("q"->BoolLiteral(true))
    

   /* val expr =Not(( IdentifierRef("a") <10)|( IdentifierRef("b") <10))
    var types =Map[String, SingleType]()
    types += ("a"-> NumberType)
    types += ("b"->NumberType)
    var confs = Map[String, Literal]()
    confs += ("a"->IntLiteral(6))
    confs += ("b"->IntLiteral(7))*/

    var translator = new Expression2SMT(expr::Nil, confs, types)
    val temp=SMTFixGenerator.divideConstraint(z3,translator,types,expr)
    for(k <- temp)
      println(k)
    }
}
