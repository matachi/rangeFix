package ca.uwaterloo.gsd.rangeFix
import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import scala.collection.mutable.Stack
import scala.collection.mutable
import java.io._

class RewritingTest extends FunSuite with ShouldMatchers {
  import Expression._
  import org.kiama.rewriting.Rewriter
  test("test simple bu rewriting") {
    val expr = IdentifierRef("a") === "b"
    val newExpr = Rewriter.rewrite(everywherebu{ Rewriter.rule {
      case IdentifierRef(x) => IntLiteral(1)
    }})(expr)

    newExpr should equal (IntLiteral(1) === IntLiteral(1))
  }
  test("test simple td rewriting") {
    val expr = IdentifierRef("a") === "b"
    val newExpr = Rewriter.rewrite(everywheretd{ Rewriter.rule {
      case IdentifierRef(x) => IntLiteral(1)
    }})(expr)

    newExpr should equal (IntLiteral(1) === IntLiteral(1))
  }
  test("test simple collections") {
    val expr = IdentifierRef("a") === "b"
    val newExpr = collectl {
      case IdentifierRef(x) => x
    }(expr)

    newExpr should equal (List("a", "b"))
  }

  test("test nested function calls") {
    val expr = IdentifierRef("a") + "b"
    val func = FunctionDef("f", List(("a", NumberType), ("b", NumberType)), NumberType, expr)
    val call = UserFunctionCall(func, List(IntLiteral(1), UserFunctionCall(func, List(2, 3)))) + UserFunctionCall(func, List(4, 5))
    val result = collectl {
      case IdentifierRef(x) => x
    }(call)

    result should equal(List("a", "b"))
    
  }
}

class UserFunctionTest extends FunSuite with ShouldMatchers {
  import Expression._
  test("simple function: add") {
    val paras=("a",NumberType)::("b",NumberType)::Nil
    val functionBody = IdentifierRef("a")+ "b";
    {functionBody match{
      case a:Plus =>{println(a.toString)
                     true}
      case _ => {false}
    }} should equal(true)
    val funcDef = FunctionDef("add",paras,NumberType,functionBody)
    var cons1 = UserFunctionCall(funcDef,IdentifierRef("a")::IdentifierRef("b")::Nil) < 10
    var types =Map[String, SingleType]()
    types += ("a"-> NumberType)
    types += ("b"->NumberType)
    types.size should equal(2)
    var confs = Map[String, Literal]()
    confs += ("a"->IntLiteral(6))
    confs += ("b"->IntLiteral(7))
    // var translator = new Expression2SMT(cons1::Nil, confs, types)
    // val SMTConfs:Map[String, SMTLiteral]= confs.map(a =>
    //   (a._1,translator.convert(a._2).asInstanceOf[SMTLiteral]))
    // var SMTTypes:Map[String,SMTType] = types.map(a=>(a._1, translator.type2SMTType(a._2)))
    // confs foreach (a=>println(a))
    val fixer = FixGenerator.create(IndexedSeq(cons1), types, confs)
    val fixesWithIgnorance = fixer.fixWithIgnorance(0);
    fixesWithIgnorance.size should equal(2)
    val fixesWithElimination = fixer.fixWithElimination(0, Set())
    fixesWithElimination should equal(fixesWithIgnorance)
    var fixesWithPropagation = fixer.fix(0, Set())
    fixesWithPropagation should equal(fixesWithIgnorance)
    println("fixes:"+fixesWithIgnorance)
  }

  test("nested function") {
    import Expression._
    val paras=("a",NumberType)::("b",NumberType)::Nil
    val functionBody = IdentifierRef("a")+ "b";
    val funcDef = FunctionDef("add",paras,NumberType,functionBody)
    var cons1 = (funcDef $ ("a", "b")) 
    val funcDef2 = FunctionDef("add2", paras, NumberType, cons1)
    var cons2 = (funcDef2 $ ("a", "b")) < 10
    var types =Map[String, SingleType]()
    types += ("a" -> NumberType)
    types += ("b" -> NumberType)
    var confs = Map[String, Literal]()
    confs += ("a"->IntLiteral(6))
    confs += ("b"->IntLiteral(7))
    val fixer = FixGenerator.create(IndexedSeq(cons2), types, confs)
    val fixesWithIgnorance = fixer.fixWithIgnorance(0);
    fixesWithIgnorance.size should equal(2)
    val fixesWithElimination = fixer.fixWithElimination(0, Set())
    fixesWithElimination should equal(fixesWithIgnorance)
    var fixesWithPropagation = fixer.fix(0, Set())
    fixesWithPropagation should equal(fixesWithIgnorance)
  }

}

class CollectGlobalVarsTest extends FunSuite with ShouldMatchers {
  test ("test single expr") {
    import Expression._
    val expr = IdentifierRef("a") + "b"
    val vars = Expression.collectGlobalVars(expr)
    vars should equal (Set("a", "b"))
  }

  test ("test global vars in functions") {
    import Expression._
    val a = IdentifierRef("a")
    val b = IdentifierRef("b")
    val x = IdentifierRef("x")
    val y = IdentifierRef("y")
    // f2(b)=a+b
    val f2 = FunctionDef("f2", Seq(("b", NumberType)), NumberType, a + b)
    // f1(a, b)=a+y+b+f2(1)
    val f1 = FunctionDef("f1", Seq(("a", NumberType), ("b", NumberType)), NumberType, a + x + b + (f2 $ (1)))
    val expr = f1 $ (1, y)
    val vars = Expression.collectGlobalVars(expr)
    vars should equal (Set("x", "y", "a"))
  }

}

class SortFuncDefTest extends FunSuite with ShouldMatchers {
  test ("test simple case") {
    import Expression._
    val b = func("b", 1)
    val a = func("a", b $ ())
    val expr = a $ ()
    val defs = sortFuncDef(expr).map(_.name)
    defs should equal (Seq("b", "a"))
  }

  test ("test two depends on one") {
    import Expression._
    val c = func("c", 1)
    val a = func("a", c $ ())
    val b = func("b", c $ ())
    val expr = (a $ ()) + (b $ ())
    val defs = sortFuncDef(expr).map(_.name)
    assert(defs == Seq("c", "a", "b") || defs == Seq("c", "b", "a"), defs)
  }

  test ("test diamond dependency") {
    import Expression._
    val d = func("d", 1)
    val c = func("c", d $ ())
    val b = func("b", (c $ ()) +(d $()))
    val a = func("a", (c $ ()) +(b $()))
    val expr = (a $ ())
    val defs = sortFuncDef(expr).map(_.name)
    defs should equal (Seq("d", "c", "b", "a"))
  }

}
class TestRewrite extends FunSuite with ShouldMatchers{
  import org.kiama.rewriting.Rewriter._
  import Expression._
  test ("test rule"){
    def sometopdown (s : => Strategy) : Strategy =
      s <* sometopdown (s)
    var expr:Expression = (IdentifierRef("a") - IdentifierRef("b"))+(IdentifierRef("a") - IdentifierRef("b"))
    var expr2:Expression = IdentifierRef("a") - IdentifierRef("b")
    val rulea = rule{
      case IdentifierRef(s:String)=>{
        println("ir:"+s)
        IntLiteral(10)}
      case x=>x
    }
    val ruleb = rule{
      case Plus(a,b)=>{
        println("and")
        And(a,b)}
      case x@_ =>{ println("ds")}
    }
    val sfun :Strategy= strategy{
        case And(a,b) => Some(a)
        case Plus(a,b) => Some(And(a,b))
        case IdentifierRef("a")=>Some(IntLiteral(10))
        case x => Some(x)
    }
    val result = rewrite(topdown(sfun))(expr)
    println("---"+result+"---")
  }
  test("test 100 levels"){
    import org.kiama.rewriting.Rewriter
    var ct:Int = 0
    def trytest={
      val paras=("a",NumberType)::("b",NumberType)::Nil
      val functionBody = Conditional(IdentifierRef("a")>"b",IdentifierRef("a"),IdentifierRef("b"));
      val funcDef = FunctionDef("max",paras,NumberType,functionBody)
      var expr:Expression = UserFunctionCall(funcDef,IdentifierRef("a")::IdentifierRef("b")::Nil)
      for (i<-2 to 2){
        expr = UserFunctionCall(funcDef,IdentifierRef("a"+i)::expr::Nil)
      }
      val rulea = rule{
        case IdentifierRef("a")=>{
          ct = ct + 1
          IntLiteral(10)}
      }
      Rewriter.rewrite(Rewriter.everywherebu(rulea))(expr)
    }
    val result = Timer.measureTime{trytest}
    println(result)
    println("ct=" + ct)
    println("excutionTime="  + Timer.lastExecutionMillis +"(ms)")
  }
}
class TestAssign extends FunSuite with ShouldMatchers{
  import org.kiama.rewriting.Rewriter._
  import Expression._
  test("assign test") {
    val paras=("a",NumberType)::("b",NumberType)::Nil
    val functionBody =  Conditional(IdentifierRef("a")>"b",IdentifierRef("a"),IdentifierRef("b"))
    val funcDef = FunctionDef("max",paras,NumberType,functionBody)
    var expr:Expression = UserFunctionCall(funcDef,IdentifierRef("a")::IdentifierRef("b")::Nil)
    var assignment=Map[String, Expression]()
    assignment += ("a"->IntLiteral(5))
    for (i<-2 to 6){
      expr = UserFunctionCall(funcDef,IdentifierRef("a"+i)::expr::Nil)
      assignment += ("a"+i.toString->IntLiteral(i))
    }
    val types = Map[String, SingleType]() withDefaultValue NumberType
    var result = Expression.assignVar(expr, assignment.get, types)
    println(result)
  }
}
