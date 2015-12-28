package ca.uwaterloo.gsd.rangeFix
import org.scalatest._
import org.scalatest.matchers.ShouldMatchers

object ComTestHelper{
  def loadModelFile(model:String, file:String, printDetail:Boolean=true):KconfigLoader={
    if (printDetail)
      println("load file: %s, %s" format(model, file))
    val loader = new KconfigLoader(model, file)
    if (printDetail)
      println("loaded!")
    return loader
  }
}
class KConfigExpressionTest extends FunSuite with ShouldMatchers{
  test ("translate TExpr to Expression  with Literals"){
    import Kconfig._
    def translateAndsimplify(e:TExpr)={
      ExpressionHelper.simplify(e.toExpression)
    }
    //test bool type
    TMax(BoolTrue,BoolFalse).t should equal (TBoolType)
    
    translateAndsimplify(TMin(BoolTrue,BoolTrue)) should equal (BoolLiteral(true))
    translateAndsimplify(TMax(BoolTrue,BoolTrue)) should equal (BoolLiteral(true))

    translateAndsimplify(TMin(BoolTrue,BoolFalse)) should equal (BoolLiteral(false))
    translateAndsimplify(TMax(BoolTrue,BoolFalse)) should equal (BoolLiteral(true))

    translateAndsimplify(TMin(BoolFalse,BoolTrue)) should equal (BoolLiteral(false))
    translateAndsimplify(TMax(BoolFalse,BoolTrue)) should equal (BoolLiteral(true))

    translateAndsimplify(TMin(BoolFalse,BoolFalse)) should equal (BoolLiteral(false))
    translateAndsimplify(TMax(BoolFalse,BoolFalse)) should equal (BoolLiteral(false))

    //test tristate type
    TYes.toExpression should equal (tristateYes)
    TMod.toExpression should equal (tristateMod)
    TNo.toExpression should equal (tristateNo)
    Array(TYes, TMod, TNo).foreach(l=> Array(TYes,TMod, TNo).foreach(r=>{
      TMin(l, r).t should equal (TTristateType)
      expect(tristateMin(l.toExpression, r.toExpression)){
        TMin(l,r).toExpression
      }
      expect(tristateMax(l.toExpression, r.toExpression)){
        TMax(l,r).toExpression
      }
    }))
    
    // test tristate type and bool type
    TMax(TYes, BoolTrue).t should equal(TTristateType)
    TMin(BoolTrue, TNo).t should equal (TTristateType)
    translateAndsimplify(TMin(TYes, BoolTrue)) should equal (tristateYes)
    translateAndsimplify(TMax(TYes, BoolTrue)) should equal (tristateYes)

    translateAndsimplify(TMin(TYes, BoolFalse)) should equal (tristateNo)
    translateAndsimplify(TMax(TYes, BoolFalse)) should equal (tristateYes)

    translateAndsimplify(TMin(TMod, BoolTrue)) should equal (tristateMod)
    translateAndsimplify(TMax(TMod, BoolTrue)) should equal (tristateYes)

    translateAndsimplify(TMin(TMod, BoolFalse)) should equal (tristateNo)
    translateAndsimplify(TMax(TMod, BoolFalse)) should equal (tristateMod)

    translateAndsimplify(TMin(TNo, BoolTrue)) should equal (tristateNo)
    translateAndsimplify(TMax(TNo, BoolTrue)) should equal (tristateYes)

    translateAndsimplify(TMin(TNo, BoolFalse)) should equal (tristateNo)
    translateAndsimplify(TMax(TNo, BoolFalse)) should equal (tristateNo)
    Array(TYes, TMod, TNo).foreach(l=>Array(BoolTrue, BoolFalse).foreach(r=>{
      expect( (translateAndsimplify(TMin(l,r)),translateAndsimplify(TMax(l,r))) ){
        (translateAndsimplify(TMin(r,l)),translateAndsimplify(TMax(r,l)) )
      }
    }))

    //int type
    translateAndsimplify(TMax(TInt(5), TInt(6))) should equal (IntLiteral(6))
    translateAndsimplify(TMax(TInt(6), TInt(5))) should equal (IntLiteral(6))
    translateAndsimplify(TMin(TInt(5), TInt(6))) should equal (IntLiteral(5))
    translateAndsimplify(TMin(TInt(6), TInt(5))) should equal (IntLiteral(5))
    intercept[AssertionError]{
      val t =TMin(TInt(6), TYes).toExpression
    }
    intercept[AssertionError]{
      val t =TMax(BoolTrue, TInt(5)).toExpression
    }
  }
  test ("to continued"){
    println("hello")
  }
}
class DivideConstraintTest extends FunSuite with ShouldMatchers {
  val z3 = new Z3()
  val id1 = IdentifierRef("p")
  val id2 = IdentifierRef("q")
  val id3 = IdentifierRef("r")
  val id4 = IdentifierRef("t")
  test ("!(p=q) && p=1"){
    z3.push()
    val cons = And(Not(id1===id2), id1===IntLiteral(1))
    val types = Map[String, SingleType]("p"->NumberType,
               "q"->NumberType)
    val translator = new Expression2SMT(Set(cons),Map[String, Literal](), types)
    val (variableName,variableType,variableValue)=SMTFixGenerator.getVariableValue(z3, translator, types, cons)
    variableName.foreach(println)
    variableValue.foreach(println)
    var my=SMTFixGenerator.temp_divideConstraint(z3, translator, cons,variableName,variableType,variableValue)
    var set = my.keySet
    var i = set.iterator
    while(i.hasNext){//遍历   
            var str = i.next
            str.foreach(println)
            println(my(str))
    }
    z3.pop()
    println("|||||||||||||||||***************1111111111111******************|||||||||||||")
  }

  test ("p+q+r=5 && p=5 && q!=3"){
    z3.push()
    val cons = And(And(Plus(Plus(id1,id2),id3)===IntLiteral(5), id1===IntLiteral(5)), Not(id2===IntLiteral(3)))
    val types = Map[String, SingleType]("p"->NumberType,
                                        "q"->NumberType,
                                        "r"->NumberType
                                      )
    val translator = new Expression2SMT(Set(cons),Map[String, Literal](), types)
    val (variableName,variableType,variableValue)=SMTFixGenerator.getVariableValue(z3, translator, types, cons)
    variableName.foreach(println)
    variableValue.foreach(println)
    var my=SMTFixGenerator.temp_divideConstraint(z3, translator, cons,variableName,variableType,variableValue)
    var set = my.keySet
    var i = set.iterator
    while(i.hasNext){//遍历   
            var str = i.next
            str.foreach(println)
            println(my(str))
    }
    z3.pop()
    println("|||||||||||||||||*************222222222222222222***************|||||||||||||")
  }
    test ("p+q+r=5 && p=5&& q!=3&&t=4"){
    z3.push()
    val cons = And(And(And(Plus(Plus(id1,id2),id3)===IntLiteral(5), id1===IntLiteral(5)), Not(id2===IntLiteral(3))),id4===IntLiteral(4))
    val types = Map[String, SingleType]("p"->NumberType,
                                        "q"->NumberType,
                                        "r"->NumberType,
                                        "t"->NumberType
                                      )
    val translator = new Expression2SMT(Set(cons),Map[String, Literal](), types)
    val (variableName,variableType,variableValue)=SMTFixGenerator.getVariableValue(z3, translator, types, cons)
    variableName.foreach(println)
    variableValue.foreach(println)
    var my=SMTFixGenerator.temp_divideConstraint(z3, translator, cons,variableName,variableType,variableValue)
    var set = my.keySet
    var i = set.iterator
    while(i.hasNext){//遍历   
            var str = i.next
            str.foreach(println)
            println(my(str))
    }
    z3.pop()
    println("|||||||||||||||||**********33333333333333333*******************|||||||||||||")
  }
      test ("!(p>1|q<6|r>7|t<8)"){
    z3.push()
    //=Not(( IdentifierRef("a") <10)|( IdentifierRef("b") <10))
    val cons = Not(id1>1|id2<6|id3>7|id4<8)
    val types = Map[String, SingleType]("p"->NumberType,
                                        "q"->NumberType,
                                        "r"->NumberType,
                                        "t"->NumberType
                                      )
    val translator = new Expression2SMT(Set(cons),Map[String, Literal](), types)
    val (variableName,variableType,variableValue)=SMTFixGenerator.getVariableValue(z3, translator, types, cons)
    variableName.foreach(println)
    variableValue.foreach(println)
    var my=SMTFixGenerator.temp_divideConstraint(z3, translator, cons,variableName,variableType,variableValue)
    var set = my.keySet
    var i = set.iterator
    while(i.hasNext){//遍历   
            var str = i.next
            str.foreach(println)
            println(my(str))
    }
    z3.pop()
    println("|||||||||||||||||**********444444444444444******************|||||||||||||")
  }
    test ("!(p>1|q<6|r>7)&&t<8"){
    z3.push()
    //=Not(( IdentifierRef("a") <10)|( IdentifierRef("b") <10))
    val cons = And(Not(id1>1|id2<6|id3>7),id4<8)
    val types = Map[String, SingleType]("p"->NumberType,
                                        "q"->NumberType,
                                        "r"->NumberType,
                                        "t"->NumberType
                                      )
    val translator = new Expression2SMT(Set(cons),Map[String, Literal](), types)
    val (variableName,variableType,variableValue)=SMTFixGenerator.getVariableValue(z3, translator, types, cons)
    variableName.foreach(println)
    variableValue.foreach(println)
    var my=SMTFixGenerator.temp_divideConstraint(z3, translator, cons,variableName,variableType,variableValue)
    var set = my.keySet
    var i = set.iterator
    while(i.hasNext){//遍历   
            var str = i.next
            str.foreach(println)
            println(my(str))
    }
    z3.pop()
    println("|||||||||||||||||***********55555555555555555******************|||||||||||||")
  }
    test ("!(!(p>5&&q>10)|!(r>4&&t<3))"){
    z3.push()
    //=Not(( IdentifierRef("a") <10)|( IdentifierRef("b") <10))
    val cons = Not(Not(And(id1>5,id2>10))|Not(And(id3>4,id4<3)))
    val types = Map[String, SingleType]("p"->NumberType,
                                        "q"->NumberType,
                                        "r"->NumberType,
                                        "t"->NumberType
                                      )
    val translator = new Expression2SMT(Set(cons),Map[String, Literal](), types)
    val (variableName,variableType,variableValue)=SMTFixGenerator.getVariableValue(z3, translator, types, cons)
    variableName.foreach(println)
    variableValue.foreach(println)
    var my=SMTFixGenerator.temp_divideConstraint(z3, translator, cons,variableName,variableType,variableValue)
    var set = my.keySet
    var i = set.iterator
    while(i.hasNext){//遍历   
            var str = i.next
            str.foreach(println)
            println(my(str))
    }
    z3.pop()
    println("|||||||||||||||||************666666666666666****************|||||||||||||")
  }
    test ("!((p|q)&&(r|t))"){//拆的不彻底
    z3.push()
    //=Not(( IdentifierRef("a") <10)|( IdentifierRef("b") <10))
    val cons = Not(And((id1|id2),(id3|id4)))
    val types = Map[String, SingleType]("p"->BoolType,
                                        "q"->BoolType,
                                        "r"->BoolType,
                                        "t"->BoolType
                                      )
    val translator = new Expression2SMT(Set(cons),Map[String, Literal](), types)
    val (variableName,variableType,variableValue)=SMTFixGenerator.getVariableValue(z3, translator, types, cons)
    variableName.foreach(println)
    variableValue.foreach(println)
    var my=SMTFixGenerator.temp_divideConstraint(z3, translator, cons,variableName,variableType,variableValue)
    var set = my.keySet
    var i = set.iterator
    while(i.hasNext){//遍历   
            var str = i.next
            str.foreach(println)
            println(my(str))
    }
    z3.pop()
    println("|||||||||||||||||************777777777777777**************|||||||||||||")
  }
    test ("!(p|q|r|t)"){
    z3.push()
    //=Not(( IdentifierRef("a") <10)|( IdentifierRef("b") <10))
    val cons = Not(id1|id2|id3|id4)
    val types = Map[String, SingleType]("p"->BoolType,
                                        "q"->BoolType,
                                        "r"->BoolType,
                                        "t"->BoolType
                                      )
    val translator = new Expression2SMT(Set(cons),Map[String, Literal](), types)
    val (variableName,variableType,variableValue)=SMTFixGenerator.getVariableValue(z3, translator, types, cons)
    variableName.foreach(println)
    variableValue.foreach(println)
    var my=SMTFixGenerator.temp_divideConstraint(z3, translator, cons,variableName,variableType,variableValue)
    var set = my.keySet
    var i = set.iterator
    while(i.hasNext){//遍历   
            var str = i.next
            str.foreach(println)
            println(my(str))
    }
    z3.pop()
    println("|||||||||||||||||***********8888888888888888***************|||||||||||||")
  }
      test ("!(p|q|r)&&t"){
    z3.push()
    //=Not(( IdentifierRef("a") <10)|( IdentifierRef("b") <10))
    val cons = And(Not(id1|id2|id3),id4)
    val types = Map[String, SingleType]("p"->BoolType,
                                        "q"->BoolType,
                                        "r"->BoolType,
                                        "t"->BoolType
                                      )
    val translator = new Expression2SMT(Set(cons),Map[String, Literal](), types)
    val (variableName,variableType,variableValue)=SMTFixGenerator.getVariableValue(z3, translator, types, cons)
    variableName.foreach(println)
    variableValue.foreach(println)
    var my=SMTFixGenerator.temp_divideConstraint(z3, translator, cons,variableName,variableType,variableValue)
    var set = my.keySet
    var i = set.iterator
    while(i.hasNext){//遍历   
            var str = i.next
            str.foreach(println)
            println(my(str))
    }
    z3.pop()
    println("|||||||||||||||||***********9999999999999999999******************|||||||||||||")
  }
    test ("!(!(p&&q)|!(r&&t))"){
    z3.push()
    //=Not(( IdentifierRef("a") <10)|( IdentifierRef("b") <10))
    val cons = Not(Not(And(id1,id2))|Not(And(id3,id4)))
    val types = Map[String, SingleType]("p"->BoolType,
                                        "q"->BoolType,
                                        "r"->BoolType,
                                        "t"->BoolType
                                      )
    val translator = new Expression2SMT(Set(cons),Map[String, Literal](), types)
    val (variableName,variableType,variableValue)=SMTFixGenerator.getVariableValue(z3, translator, types, cons)
    variableName.foreach(println)
    variableValue.foreach(println)
    var my=SMTFixGenerator.temp_divideConstraint(z3, translator, cons,variableName,variableType,variableValue)
    var set = my.keySet
    var i = set.iterator
    while(i.hasNext){//遍历   
            var str = i.next
            str.foreach(println)
            println(my(str))
    }
    z3.pop()
    println("|||||||||||||||||************AAAAAAAAAAAAAAAA**************|||||||||||||")
  }
      test ("!( p!=Enum[0,1,2].0  | q!=Enum[0,1,2].1 | r!=Enum[0,1,2].2 )"){
    z3.push()
    //=Not(( IdentifierRef("a") <10)|( IdentifierRef("b") <10))
    val cons = Not(Or(Or( NEq(id1,Kconfig.tristateNo),NEq(id2,Kconfig.tristateMod) ),NEq(id3,Kconfig.tristateYes) ))
    val types = Map[String, SingleType]("p"->Kconfig.tristateType,
                                        "q"->Kconfig.tristateType,
                                        "r"->Kconfig.tristateType,
                                        "t"->Kconfig.tristateType
                                      )
    val translator = new Expression2SMT(Set(cons),Map[String, Literal](), types)
    val (variableName,variableType,variableValue)=SMTFixGenerator.getVariableValue(z3, translator, types, cons)
    variableName.foreach(println)
    variableValue.foreach(println)
    var my=SMTFixGenerator.temp_divideConstraint(z3, translator, cons,variableName,variableType,variableValue)
    var set = my.keySet
    var i = set.iterator
    while(i.hasNext){//遍历   
            var str = i.next
            str.foreach(println)
            println(my(str))
    }
    z3.pop()
    println("|||||||||||||||||************BBBBBBBBBBBBBBBB**************|||||||||||||")
  }

    test ("!(!(p!=q)|!(r!=t))"){
    z3.push()
    //=Not(( IdentifierRef("a") <10)|( IdentifierRef("b") <10))
    val cons = Not(Or(Not(NEq(id1,id2)),Not(NEq(id3,id4))))
    val types = Map[String, SingleType]("p"->Kconfig.tristateType,
                                        "q"->Kconfig.tristateType,
                                        "r"->Kconfig.tristateType,
                                        "t"->Kconfig.tristateType
                                      )
    val translator = new Expression2SMT(Set(cons),Map[String, Literal](), types)
    val (variableName,variableType,variableValue)=SMTFixGenerator.getVariableValue(z3, translator, types, cons)
    variableName.foreach(println)
    variableValue.foreach(println)
    var my=SMTFixGenerator.temp_divideConstraint(z3, translator, cons,variableName,variableType,variableValue)
    var set = my.keySet
    var i = set.iterator
    while(i.hasNext){//遍历   
            var str = i.next
            str.foreach(println)
            println(my(str))
    }
    z3.pop()
    println("|||||||||||||||||************CCCCCCCCCCCCCCCCC**************|||||||||||||")
  }

        test ("!(!(p!=q)|!(p!=Enum[0,1,2].2))"){//这个也不能拆，是算法本身的问题，不是程序的问题
    z3.push()
    //=Not(( IdentifierRef("a") <10)|( IdentifierRef("b") <10))
    val cons = Not(Or(Not(NEq(id1,id2)),Not(NEq(id1,Kconfig.tristateYes))))
    val types = Map[String, SingleType]("p"->Kconfig.tristateType,
                                        "q"->Kconfig.tristateType,
                                        "r"->Kconfig.tristateType,
                                        "t"->Kconfig.tristateType
                                      )
    val translator = new Expression2SMT(Set(cons),Map[String, Literal](), types)
    val (variableName,variableType,variableValue)=SMTFixGenerator.getVariableValue(z3, translator, types, cons)
    variableName.foreach(println)
    variableValue.foreach(println)
    var my=SMTFixGenerator.temp_divideConstraint(z3, translator, cons,variableName,variableType,variableValue)
    var set = my.keySet
    var i = set.iterator
    while(i.hasNext){//遍历   
            var str = i.next
            str.foreach(println)
            println(my(str))
    }
    z3.pop()
    println("|||||||||||||||||************DDDDDDDDDDDDDDDDDDDDDDDDD**************|||||||||||||")
  }

        test ("!(p!=q|r!=Enum[0,1,2].2|t!=Enum[0,1,2].1)"){
    z3.push()
    //=Not(( IdentifierRef("a") <10)|( IdentifierRef("b") <10))
    val cons = Not(Or(Or(NEq(id1,id2),NEq(id3,Kconfig.tristateYes)),NEq(id4,Kconfig.tristateMod)))
    val types = Map[String, SingleType]("p"->Kconfig.tristateType,
                                        "q"->Kconfig.tristateType,
                                        "r"->Kconfig.tristateType,
                                        "t"->Kconfig.tristateType
                                      )
    val translator = new Expression2SMT(Set(cons),Map[String, Literal](), types)
    val (variableName,variableType,variableValue)=SMTFixGenerator.getVariableValue(z3, translator, types, cons)
    variableName.foreach(println)
    variableValue.foreach(println)
    var my=SMTFixGenerator.temp_divideConstraint(z3, translator, cons,variableName,variableType,variableValue)
    var set = my.keySet
    var i = set.iterator
    while(i.hasNext){//遍历   
            var str = i.next
            str.foreach(println)
            println(my(str))
    }
    z3.pop()
    println("|||||||||||||||||************EEEEEEEEEEEEEEEEEE**************|||||||||||||")
  }
        test ("!(p=q|r=Enum[0,1,2].2|t=Enum[0,1,2].1)"){
    z3.push()
    //=Not(( IdentifierRef("a") <10)|( IdentifierRef("b") <10))
    val cons = Not(Or(Or(Eq(id1,id2),Eq(id3,Kconfig.tristateYes)),Eq(id4,Kconfig.tristateMod)))
    val types = Map[String, SingleType]("p"->Kconfig.tristateType,
                                        "q"->Kconfig.tristateType,
                                        "r"->Kconfig.tristateType,
                                        "t"->Kconfig.tristateType
                                      )
    val translator = new Expression2SMT(Set(cons),Map[String, Literal](), types)
    val (variableName,variableType,variableValue)=SMTFixGenerator.getVariableValue(z3, translator, types, cons)
    variableName.foreach(println)
    variableValue.foreach(println)
    var my=SMTFixGenerator.temp_divideConstraint(z3, translator, cons,variableName,variableType,variableValue)
    var set = my.keySet
    var i = set.iterator
    while(i.hasNext){//遍历   
            var str = i.next
            str.foreach(println)
            println(my(str))
    }
    z3.pop()
    println("|||||||||||||||||************FFFFFFFFFFFFFFFFFFF**************|||||||||||||")
  }
    
   test ("!(!IsSubstr(p,SetLiteral(xx,yy))|!IsSubstr(q,SetLiteral(yy,zz)))"){//这个也不能拆，是算法本身的问题，不是程序的问题
    z3.push()
    val cons = Not(Or(Not(IsSubstr(id1,SetLiteral(Set("xx","yy")))),Not(IsSubstr(id2,SetLiteral(Set("zz","ww"))))))
    println(cons)
    val types = Map[String, SingleType]("p"->SetType,
                                        "q"->SetType,
                                        "r"->SetType,
                                        "t"->SetType
                                      )
    val translator = new Expression2SMT(Set(cons),Map[String, Literal](), types)
    val (variableName,variableType,variableValue)=SMTFixGenerator.getVariableValue(z3, translator, types, cons)
    variableName.foreach(println)
    variableValue.foreach(println)
    var my=SMTFixGenerator.temp_divideConstraint(z3, translator, cons,variableName,variableType,variableValue)
    var set = my.keySet
    var i = set.iterator
    while(i.hasNext){//遍历   
            var str = i.next
            str.foreach(println)
            println(my(str))
    }
    z3.pop()
    println("|||||||||||||||||************GGGGGGGGGGGGGGGGGGG**************|||||||||||||")
  }
     test ("!(!Eq(p,SetLiteral(xx,yy))|!Eq(q,SetLiteral(yy,zz))|!Eq(r,SetLiteral(xx,zz))|!Eq(t,SetLiteral(xx,yy,zz)))"){//这个也不能拆，是算法本身的问题，不是程序的问题
    z3.push()
    val cons = Not(Or(Or(Or(
                            Not(Eq(id1,SetLiteral(Set("xx","yy")))),
                            Not(Eq(id2,SetLiteral(Set("zz","yy"))))
                            ),
                         Not(Eq(id3,SetLiteral(Set("xx","zz"))))),
                       Not(Eq(id4,SetLiteral(Set("xx","yy","zz"))))))
    val types = Map[String, SingleType]("p"->SetType,
                                        "q"->SetType,
                                        "r"->SetType,
                                        "t"->SetType
                                      )
    val translator = new Expression2SMT(Set(cons),Map[String, Literal](), types)
    val (variableName,variableType,variableValue)=SMTFixGenerator.getVariableValue(z3, translator, types, cons)
    variableName.foreach(println)
    variableValue.foreach(println)
    var my=SMTFixGenerator.temp_divideConstraint(z3, translator, cons,variableName,variableType,variableValue)
    var set = my.keySet
    var i = set.iterator
    while(i.hasNext){//遍历   
            var str = i.next
            str.foreach(println)
            println(my(str))
    }
    z3.pop()
    println("|||||||||||||||||************HHHHHHHHHHHHHHHHH**************|||||||||||||")
  }
       test ("!(!NEq(p,SetLiteral(xx,yy))|!NEq(q,SetLiteral(yy,zz))|!NEq(r,SetLiteral(xx,zz))|!NEq(t,SetLiteral(xx,yy,zz)))"){//这个也不能拆，是算法本身的问题，不是程序的问题
    z3.push()
    val cons = Not(Or(Or(Or(
                            Not(NEq(id1,SetLiteral(Set("xx","yy")))),
                            Not(NEq(id2,SetLiteral(Set("zz","yy"))))
                            ),
                         Not(NEq(id3,SetLiteral(Set("xx","zz"))))),
                       Not(NEq(id4,SetLiteral(Set("xx","yy","zz"))))))
    val types = Map[String, SingleType]("p"->SetType,
                                        "q"->SetType,
                                        "r"->SetType,
                                        "t"->SetType
                                      )
    val translator = new Expression2SMT(Set(cons),Map[String, Literal](), types)
    val (variableName,variableType,variableValue)=SMTFixGenerator.getVariableValue(z3, translator, types, cons)
    variableName.foreach(println)
    variableValue.foreach(println)
    var my=SMTFixGenerator.temp_divideConstraint(z3, translator, cons,variableName,variableType,variableValue)
    var set = my.keySet
    var i = set.iterator
    while(i.hasNext){//遍历   
            var str = i.next
            str.foreach(println)
            println(my(str))
    }
    z3.pop()
    println("|||||||||||||||||************IIIIIIIIIIIIIIIIII**************|||||||||||||")
  }
         test ("!(!NEq(p,SetLiteral(xx,yy))|!NEq(q,SetLiteral(yy,zz))|!IsSubstr(r,SetLiteral(xx,zz))|!IsSubstr(t,SetLiteral(xx,yy,zz)))"){//这个也不能拆，是算法本身的问题，不是程序的问题
    z3.push()
    val cons = Not(Or(Or(Or(
                            Not(NEq(id1,SetLiteral(Set("xx","yy")))),
                            Not(NEq(id2,SetLiteral(Set("zz","yy"))))
                            ),
                         Not(IsSubstr(id3,SetLiteral(Set("xx","zz"))))),
                       Not(IsSubstr(id4,SetLiteral(Set("xx","yy","zz"))))))
    val types = Map[String, SingleType]("p"->SetType,
                                        "q"->SetType,
                                        "r"->SetType,
                                        "t"->SetType
                                      )
    val translator = new Expression2SMT(Set(cons),Map[String, Literal](), types)
    val (variableName,variableType,variableValue)=SMTFixGenerator.getVariableValue(z3, translator, types, cons)
    variableName.foreach(println)
    variableValue.foreach(println)
    var my=SMTFixGenerator.temp_divideConstraint(z3, translator, cons,variableName,variableType,variableValue)
    var set = my.keySet
    var i = set.iterator
    while(i.hasNext){//遍历   
            var str = i.next
            str.foreach(println)
            println(my(str))
    }
    z3.pop()
    println("|||||||||||||||||************JJJJJJJJJJJJJJJJJJJJJ**************|||||||||||||")
  }
}
/*class DivideConstraintTest extends FunSuite with ShouldMatchers {
  val z3 = new Z3()
  val id1 = IdentifierRef("p")
  val id2 = IdentifierRef("q")
  val id3 = IdentifierRef("r")
  test ("!(p=q) && p=1"){
    val cons = And(Not(id1===id2), id1===IntLiteral(1))
    val types = Map[String, SingleType]("p"->NumberType,
               "q"->NumberType)
    val translator = new Expression2SMT(Set(cons),Map[String, Literal](), types)
    val rlt = SMTFixGenerator.divideConstraint(z3, translator, types, cons)
    println(rlt.toString)
  }
  test ("p+q+r=5 && p=5 && q!=3"){
    val cons = And(And(Plus(Plus(id1,id2),id3)===IntLiteral(5), id1===IntLiteral(5)), Not(id2===IntLiteral(3)))
    val types = Map[String, SingleType]("p"->NumberType,
                                        "q"->NumberType,
                                        "r"->NumberType
                                      )
    val translator = new Expression2SMT(Set(cons),Map[String, Literal](), types)
    val rlt = SMTFixGenerator.divideConstraint(z3, translator, types, cons)
    println(rlt.toString)
  }
  test("3"){
    println(2)
  }
}*/
class ActiveVariableTest extends FunSuite with ShouldMatchers{
  def ChangeConfig(model:String, file:String, id:String, l:Literal)={
    val loader = ComTestHelper.loadModelFile(model,file)
    print("Preparing the fix generator...")
    val manager = new KconfigManager(loader, 1)
    println("done.")
    println("Computing fixes...")
    val v = if (l==Kconfig.tristateYes && loader.getVarType(id).get==BoolType)
              BoolLiteral(true)
            else l
    val result = manager.setFeature(id, v)
    val fixes = result.fixes
    if (fixes.size == 0)
      println("It is not possible to change the config.")
    else
      fixes.foreach(f => println("\t" + f))
    println("completed")
  }
  test("every version of constraints") {
    val path = "./experiment/Kconfig/"
    //ChangeConfig(path+"constraint_files/2.6.17.exconfig", path+"configuration_files/astlinux_2.6.17_281.config", "NLS", Kconfig.tristateYes)//error :unmatched
    //ChangeConfig(path+"constraint_files/2.6.29.exconfig", path+"configuration_files/CdMa-HeRoc-2.6.29_2.6.29_2.config", "CRC16", Kconfig.tristateYes)//error :unmatched
    //ChangeConfig(path+"constraint_files/2.6.19.exconfig", path+"configuration_files/crux-arm_2.6.19_2.config", "CRYPTO_ALGAPI", Kconfig.tristateYes)//too long

    /*ChangeConfig(path+"constraint_files/2.6.12.exconfig", path+"configuration_files/tuxbox_2.6.12_1.config", "HIGHMEM", BoolLiteral(true)) //okay
    ChangeConfig(path+"constraint_files/2.6.19.exconfig", path+"configuration_files/crux-arm_2.6.19_2.config", "CRYPTO_TEST", Kconfig.tristateYes)//not possible
    ChangeConfig(path+"constraint_files/2.6.19.exconfig", path+"configuration_files/crux-arm_2.6.19_2.config", "PARTITION_ADVANCED", Kconfig.tristateYes)//okay
    ChangeConfig(path+"constraint_files/2.6.32.exconfig", path+"configuration_files/lustre-release_2.6.32_3.config", "KERNEL_LZMA", Kconfig.tristateYes)//okay
    
    ChangeConfig(path+"constraint_files/2.6.32.exconfig", path+"configuration_files/lustre-release_2.6.32_3.config", "USER_SCHED", Kconfig.tristateYes)//okay,12s
    ChangeConfig(path+"constraint_files/2.6.32.exconfig", path+"configuration_files/pdaxroom_2.6.32_1.config", "SERIAL_MAX3100", Kconfig.tristateYes)//okay
    ChangeConfig(path+"constraint_files/2.6.32.exconfig", path+"configuration_files/pdaxroom_2.6.32_1.config", "R3964", Kconfig.tristateYes)//okay
    ChangeConfig(path+"constraint_files/2.6.32.exconfig", path+"configuration_files/pdaxroom_2.6.32_1.config", "SPI_SPIDEV", Kconfig.tristateYes)//okay
    ChangeConfig(path+"constraint_files/2.6.20.exconfig", path+"configuration_files/runnix_2.6.20_runnix_50.config", "USB_GADGET", Kconfig.tristateYes)//okay,4s
    ChangeConfig(path+"constraint_files/2.6.20.exconfig", path+"configuration_files/runnix_2.6.20_runnix_50.config", "PROC_KCORE", Kconfig.tristateYes)//okay
    ChangeConfig(path+"constraint_files/2.6.20.exconfig", path+"configuration_files/runnix_2.6.20_runnix_50.config", "USB_GADGET", Kconfig.tristateYes)//okay
    ChangeConfig(path+"constraint_files/2.6.20.exconfig", path+"configuration_files/runnix_2.6.20_runnix_50.config", "DLM", Kconfig.tristateYes)//okay, complicated
*/
    //ChangeConfig(path+"constraint_files/2.6.32.exconfig", path+"configuration_files/runnix_2.6.20_runnix_50.config", "BH_LRU_SIZE", IntLiteral(2))//feature cannot be found
    
    ChangeConfig(path+"constraint_files/2.6.32.exconfig", path+"configuration_files/lustre-release_2.6.32_3.config","MODULE_UNLOAD" , BoolLiteral(false))//okay, complicated
  }
}
class ActiveAllVariablesTest extends FunSuite with ShouldMatchers{
  test("active all variable tests"){
    val path = "./experiment/Kconfig/"
    val configPath = path+"configuration_files/runnix_2.6.20_runnix_50.config"
    val loader = ComTestHelper.loadModelFile(path+"constraint_files/2.6.20.exconfig", configPath)
    val manager = new KconfigManager(loader, 1);
    import java.io.FileReader
    import java.io.BufferedReader
    import java.io.File
    val br = new BufferedReader(new FileReader(new File(configPath)))
    var line:String = br.readLine
    var totalExcutionTime = 0L
    var excutionTime = 0L
    var successCt = 0
    var totalCt = 0
    var fixesCt = 0
    var fixesUnit =0
    var unitVars =0
    var fixRt:FixGenResult = null
    while (line!=null){
      var varName:String = ""
      val unsatConfigFlag = "# CONFIG_"
      if (line.contains(unsatConfigFlag)){
        varName = line.substring(line.indexOf(unsatConfigFlag)+unsatConfigFlag.length())
        varName = varName.substring(0, varName.indexOf(" "))
        val optionType = loader.getVarType(varName)
        if(optionType != None && optionType.get==Kconfig.tristateType){
          import java.lang.Runnable
          import java.lang.Thread
          var flag:Boolean = false
          val task = new Runnable{
            override def run()={
              fixRt = Timer.measureTime(manager.setFeature (varName, Kconfig.tristateYes))
              excutionTime = Timer.lastExecutionMillis
              flag = true
            }
          }
          val thread = new Thread(task)
          thread.start()
          thread.join(20000)
          thread.stop
          totalCt+=1
          if (flag){
            successCt += 1
            totalExcutionTime += excutionTime
            fixesCt += fixRt.fixes.size
            fixRt.fixes.foreach(fix=>{
              fixesUnit += fix.units.size
              fix.units.foreach(unit=> unitVars += unit.variables.size)
            })
          }
        }
      }
      line = br.readLine
    }
    printf("total: %d, sucess:%d, %f\n", totalCt, successCt, successCt.toFloat/totalCt.toFloat)
    printf("total time: %s ms, average time:%s ms\n", totalExcutionTime.toString, (totalExcutionTime/successCt).toString())
    printf("total fixes: %d, average:%f\n", fixesCt, fixesCt.toFloat/successCt.toFloat)
    printf("total unit: %d, average:%f per fix\n", fixesUnit, fixesUnit.toFloat/fixesCt.toFloat )
    printf("total variables:%d average:%f per unit\n", unitVars, unitVars.toFloat/fixesUnit.toFloat)
    br.close
  }
}
