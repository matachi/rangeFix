package ca.uwaterloo.gsd.rangeFix
import collection.mutable
import collection._
import ConditionalCompilation._

object SMTFixGenerator {
  var testNewAlgorithm:Boolean = false
  val guardPrefix = "__gd__"
  type Diagnosis = Set[String]
  type SemanticDiagnosis = (Diagnosis, Diagnosis)
  type SemanticDiagnoses = Iterable[SemanticDiagnosis]
  var useAlgorithm:Boolean = false
  var divideUnits:Boolean = true
  //true->use new algorithm;false->use the old algorithm
  private def toGuardVar(v:String):String = guardPrefix + v
  private def toNormalVar(v:String):String = v.substring(guardPrefix.size)

  private def conf2constr(v:String, l:SMTLiteral):SMTExpression = {
    val guardVar = toGuardVar(v)
		             (!SMTVarRef(guardVar)) | (SMTVarRef(v) === l)
  }
  
  def generateSimpleDiagnoses(configuration:Map[String, SMTLiteral], 
			      varSet:Set[String], 
			      constraints: Iterable[SMTExpression],
			      types:Map[String, SMTType], 
			      funcsToDeclare:Seq[SMTFuncDefine],
			      optTypesToDeclare:Set[SMTType] = null):Iterable[Diagnosis] = {
    /*if (kconfigtemptest.flag){
      assert(configuration == kconfigtemptest.configuration &&
             varSet == kconfigtemptest.varSet &&
             constraints == kconfigtemptest.constraints &&
             types == kconfigtemptest.types &&
             funcsToDeclare == kconfigtemptest.funcsToDeclare &&
             optTypesToDeclare == kconfigtemptest.optTypesToDeclare)
      println("matched")
    }
    else {
      kconfigtemptest.configuration = configuration
      kconfigtemptest.varSet = varSet
      kconfigtemptest.constraints = constraints
      kconfigtemptest.types = types
      kconfigtemptest.funcsToDeclare = funcsToDeclare
      kconfigtemptest.optTypesToDeclare = optTypesToDeclare
      kconfigtemptest.flag = true
    }*/
    import org.kiama.rewriting.Rewriter._
    val typesToDeclare = if (optTypesToDeclare == null) varSet.map(v => types(v)) else optTypesToDeclare
    val z3 = new Z3()
    z3.enableUnsatCore
    try {
      //  assert ( collects { case x:SMTVarRef => x.id } (constraints) == varSet )
      z3.declareTypes(typesToDeclare)
      z3.declareVariables(varSet.map(v=>(v, types(v))))
      z3.declareVariables(varSet.map(v => (toGuardVar(v), SMTBoolType)))
      funcsToDeclare.foreach(z3.declareFunc)
      for (c <- constraints)
      z3.assertConstraint(c)
      for (v <- varSet)
      z3.assertConstraint(conf2constr(v, configuration(v)))
      def getMinimalCore(removedVars:Traversable[String]):Option[List[String]] = 
	z3.getMinimalUnsatCore(varSet.map(v=>toGuardVar(v)) -- removedVars).map(_.toList)
      def getCore(removedVars:Traversable[String]):Option[List[String]] = 
	  z3.getUnsatCore(varSet.map(v=>toGuardVar(v)) -- removedVars).map(_.toList)
      var diagnoses = List[List[String]]()
     /* def test(){
        val fr= new java.io.FileWriter("newAl.txt",true)
        val d1 = Timer.measureTime(1)(DiagnoseGenerator.getDiagnoses[String](getMinimalCore))
        val t1 = Timer.lastExecutionMillis
        val d2 = Timer.measureTime(1)(DiagnoseGenerator.getDiagnoses[String](getCore))
        val t2 = Timer.lastExecutionMillis
        fr.write("%s\t%s\n%s\t%s\n".format(d1.toString(),t1.toString(),d2.toString(),t2.toString()))
        fr.close
        assert(d1.forall(dd1=>d2.exists(_.intersect(dd1).size>=dd1.size)) && d2.forall(dd2=>d1.exists(_.intersect(dd2).size>=dd2.size)),
             "%s\n%s\n".format(d1,d2))
//        println("%s,%s\n%s%s\n"format(d1.toString(),d2.toString(),t1.toString(),t2.toString()))
      }
      if (testNewAlgorithm)
        test()*/
      if (SMTFixGenerator.useAlgorithm){
	diagnoses = DiagnoseGenerator.getDiagnoses[String](getCore)
      }
      else {
	IF[CompilationOptions.USE_MINIMAL_CORE#v] {
	  diagnoses = (new HSDAG[String]()).getDiagnoses(getMinimalCore, true)
	}
        IF[(NOT[CompilationOptions.USE_MINIMAL_CORE])#v] {
	  diagnoses = (new HSDAG[String]()).getDiagnoses(getCore, false)
	}
      }
 //     println("diagnoses:%s" format diagnoses)
      diagnoses.map(_.map(toNormalVar).toSet)
    }
    finally {
      z3.exit()
    }
  }
  
  // This version is not supported any more, it is kept here because some tests rely on it and these tests also ensure the conformance of Z3
  // configuration by default includes all semantic vars and syntatic vars
  // constraints include semantic var definitions
  @deprecated("Use generateSimpleDiagnoses instead", "Since 2011/12")
  def generateDiagnoses(configuration:Map[String, SMTLiteral], 
			changeableVars:Set[String], 
			semanticVars:Set[String],
			constraints:Iterable[SMTExpression],
			types:Map[String, SMTType],
			funcsToDeclare:Iterable[SMTFuncDefine]):SemanticDiagnoses = {
		                import org.kiama.rewriting.Rewriter._
		                val z3 = new Z3()
		                z3.enableUnsatCore
		                try {
			          val varSet = changeableVars ++ semanticVars
			          assert ( collects { case x:SMTVarRef => x.id } (constraints) == varSet )
			          val allTypes = varSet.map(v => types(v))
			          z3.declareTypes(allTypes)
			          funcsToDeclare.foreach(z3.declareFunc)
			          z3.declareVariables(varSet.map(v=>(v, types(v))))
			          z3.declareVariables(varSet.map(v => (toGuardVar(v), SMTBoolType)))
			          for (c <- constraints)
				  z3.assertConstraint(c)
			          for (v <- varSet)
				  z3.assertConstraint(conf2constr(v, configuration(v)))
			          def getCore(changeableVars:Set[String])(removedVars:Traversable[String]):Option[List[String]] = z3.getUnsatCore(changeableVars.map(v=>toGuardVar(v)) -- removedVars).map(_.toList)
			          val semanticDiagnoses = (new HSDAG[String]()).getDiagnoses(getCore(semanticVars))
			                                                                    (for(sd <- semanticDiagnoses) yield {
				                                                              val nsd = sd.map(toNormalVar(_))
				                                                              val fixedVars = semanticVars.toSet -- nsd
				                                                              z3.push()
				                                                              for (v <- fixedVars)
					                                                      z3.assertConstraint(toGuardVar(v))
				                                                              val diagnoses = (new HSDAG[String]()).getDiagnoses(getCore(changeableVars))
				                                                              z3.pop()
				                                                                    (diagnoses.map(d=>(d.map(toNormalVar(_)).toSet, nsd.toSet)))
			                                                                    } ).flatten
		                }
		                finally {
			          z3.exit()
		                }
	                      }
  
  def simpleDiagnoses2Fixes(configuration:Map[String, Literal],
			    constraints:Iterable[Expression],
                            types:Expression.Types,
			    ds:Iterable[Diagnosis],
                            getRelatedVars:Expression=>Set[String]):Iterable[DataFix] = {
    import org.kiama.rewriting.Rewriter._
    val fixconstraints = 
      for(d <- ds) yield {
	constraints.filter(a=>d.exists(getRelatedVars(a).contains)).map ( c => {
          def replace(id:String) = if (!d.contains(id)) Some(configuration(id)) else None
          val result = Expression.assignVar(c, replace, types)
	  assert(result != BoolLiteral(false))
	  result
	} ).filter(_ != BoolLiteral(true)).map(ExpressionHelper.simplifyWithReplacement(_,types)).filter(_!=BoolLiteral(true))
      }
    val z3 = new Z3()
    try{
      fixconstraints.map(constraint2DataFix(z3, types, _))
    }
    finally{
      z3.exit()
    }
  }


  //wj  begin
  def getVariableValue(z3:Z3, translator:Expression2SMT, varTypes:Map[String, Type], constraint:Expression)
        : (Array[String],Array[String],Array[String])=
  {
                   var SMTTypes:Map[String,SMTType] = varTypes.map(a=>(a._1, translator.type2SMTType(a._2)))
                   z3.declareTypes(SMTTypes.map(_._2).toSet)
                   z3.declareVariables(SMTTypes)
                   z3.push()
                   z3.assertConstraint(translator.convert(constraint)) 
                   assert(z3.checkSat(),"The expression is unsat!")
                   var (resultFromZ3,groupNum)=z3.getValValueMap()
                   z3.pop()
                   var resultInGroup:Array[Array[String]]=new Array(groupNum.toInt)
                   var resultAfterSplit=resultFromZ3.split(" ")
                   var groupLength=List[Int]()
                   var group=0
                   var count=0
                   var braceBegin=0
                   var braceEnd=0
                   var braceNum=0
                   for(eachString<-resultAfterSplit)//将每一个define进行分组，但首先要确定组在split后的list里面的上界和下界，这里用braceBegin表示下界，braceEnd表示上界
                   {
                        var length=eachString.length()//拆分后每个字符串的长度
                        for(i <- 0 to (length-1))//这里确定一组开始和结束的方法是，在每一组开始和结束左括号和右括号都是匹配的，在组的开始处braceNum=0,之后遇到一个左括号该值加一，遇到一个右括号该值减一，到结尾的时候该值为0
                        {
                           if(eachString.charAt(i)=='(')//之所以要遍历一个字符串的每个字符而不用contain，是因为一个字符串里面可能有多个括号，比如"()"，所以要遍历每个字符
                           {
                                if(braceNum==0)
                                  braceBegin=count//count记录当前字符串的位置
                                braceNum=braceNum+1
                           }
                           else if(eachString.charAt(i)==')')
                           {
                                braceNum=braceNum-1
                                if(braceNum==0)
                                {
                                  braceEnd=count
                                  var eachGroupLength=braceEnd-braceBegin+1//每一组的长度
                                  groupLength=groupLength:::List(eachGroupLength)//用list存放每组字符串的个数，之所以要存放，是因为返回BitVec和返回boolean int scalar的字符串的个数不一样，后面三个是一样的
                                  resultInGroup(group)=new Array(eachGroupLength)
                                  for(i <- 0 to (eachGroupLength-1))//将从braceBegin到braceEnd的字符串复制到每一组里面，一组表示对一个变量的赋值
                                    resultInGroup(group)(i)=resultAfterSplit(braceBegin+i)
                                  group=group+1
                                }
                           }
                        }
                        count=count+1
                    }
                    var variableName:Array[String]=new Array(group)//存放每个变量的名字
                    var variableType:Array[String]=new Array(group)//存放每个变量的类型，BitVec存的是BitVec类型
                    var variableValue:Array[String]=new Array(group)//存放每个变量的值
                    var indexCount=0
                    //println(groupLength)
                    for(len <- groupLength)//对每组进行遍历
                    {
                      //println(len)
                      if(len==8)//如果当前组的字符串数目为8，说明为int bool scalar三种类型之一
                      {
                            variableName(indexCount)=resultInGroup(indexCount)(1)
                            variableType(indexCount)=resultInGroup(indexCount)(3)
                            var indexBrace=resultInGroup(indexCount)(7).indexOf(')')//去掉每个值最后一个括号，因为含值的字符串为"value)"
                            resultInGroup(indexCount)(7)=resultInGroup(indexCount)(7).substring(0,indexBrace)
                            variableValue(indexCount)=resultInGroup(indexCount)(7)
                      }
                      else if(len==10)//如果当前组的字符串数目为10，说明为BitVec,但也可能是scala类型的
                      {
                             //println("In BitVec")
                             variableName(indexCount)=resultInGroup(indexCount)(1)
                             variableType(indexCount)=resultInGroup(indexCount)(4)
                             var indexBrace=resultInGroup(indexCount)(9).indexOf(')')
                             resultInGroup(indexCount)(9)=resultInGroup(indexCount)(9).substring(0,indexBrace)
                             variableValue(indexCount)=resultInGroup(indexCount)(9)
                      }
                      indexCount=indexCount+1
                    }
                   (variableName,variableType,variableValue);
        }
        def temp_divideConstraint(z3:Z3, translator:Expression2SMT, constraint:Expression,
          variableName:Array[String],variableType:Array[String],variableValue:Array[String])
        :Map[Array[String], Expression]=
  {
               
                import org.kiama.rewriting.Rewriter._ 
                val variableNum = variableName.size
                var backMap =  Map[Array[String],Expression]()
                if(variableNum==1)
                {
                    backMap+=(variableName -> constraint)
                    return backMap
                }
                else
                {
                    val maxBit=((1<<variableNum)-2)/2
                    var maxGroup=0
                    var k=1
                    var sign=false
                    while(k<=maxBit)
                    {
                         var result=new Array[Expression](2)
                         result(0)=constraint
                         result(1)=constraint
                         var count=0
                          for( i <- 0 to variableNum-1)
                         {      
                                var resultIndex=0
                                if(((1<<i)&k)!=0)
                                 resultIndex=1
                                else 
                                 resultIndex=0
                                if(resultIndex==1) count=count+1//统计result(1)尚有多少变量
                                if(variableType(i)=="Bool") result(resultIndex)= ExpressionHelper.simplify(rewrite(everywherebu(rule[Expression] {  case IdentifierRef(a) if a==variableName(i) =>BoolLiteral(variableValue(i).toBoolean)}))(result(resultIndex)))
                                else if(variableType(i)=="Int")   result(resultIndex)= ExpressionHelper.simplify(rewrite(everywherebu(rule[Expression] {  case IdentifierRef(a) if a==variableName(i) =>IntLiteral(variableValue(i).toInt)}))(result(resultIndex)))
                                else if(variableType(i)=="BitVec")//如果返回的是BitVec类型
                                  result(resultIndex)= ExpressionHelper.simplify(rewrite(everywherebu(rule[Expression] {  case IdentifierRef(a) if a==variableName(i) =>SetLiteral(translator.bitVector2Set(BitVecToBoolean(variableValue(i)))) }))(result(resultIndex)))
                                else //scalar类型
                                  result(resultIndex)= ExpressionHelper.simplify(rewrite(everywherebu(rule[Expression] {  case IdentifierRef(a) if a==variableName(i) => ScalarToEnumLiteral(variableValue(i),variableType(i),translator) }))(result(resultIndex)))
                           }
                         if(checkDivide(z3,result,constraint,translator))//表示可以拆分
                         {
                              sign=true
                               var (tempVariableName,tempVariableType,tempVariableValue)=DivideOfVariable(k,variableNum,count,variableName,variableType,variableValue)
                               var tempMap =  Map[Array[String],Expression]()//记录当前拆分发得到的结果
                               tempMap=tempMap++temp_divideConstraint(z3,translator,result(0),tempVariableName(0),tempVariableType(0),tempVariableValue(0))
                               tempMap=tempMap++temp_divideConstraint(z3,translator,result(1),tempVariableName(1),tempVariableType(1),tempVariableValue(1))
                               if(maxGroup<tempMap.size)
                               {
                                   maxGroup=tempMap.size//寻找能够拆分的最大组数
                                   backMap=tempMap
                               }
                          }
                         k=k+1
                    }
                      if(!sign)//如果不能拆分，就返回原来的表达式和变量
                          backMap+=(variableName -> constraint)
                        return backMap
                }
         
        }
        def DivideOfVariable(k:Int,variableNum:Int,count:Int,variableName:Array[String],variableType:Array[String],variableValue:Array[String]):
        (Array[Array[String]],Array[Array[String]],Array[Array[String]])=
        {
                        var tempVariableName=new Array[Array[String]](2)
                        var tempVariableType=new Array[Array[String]](2)
                        var tempVariableValue=new Array[Array[String]](2)
                        tempVariableName(0)=new Array[String](count)
                        tempVariableName(1)=new Array[String](variableNum-count)
                        tempVariableType(0)=new Array[String](count)
                        tempVariableType(1)=new Array[String](variableNum-count)
                        tempVariableValue(0)=new Array[String](count)
                        tempVariableValue(1)=new Array[String](variableNum-count)                           
                        var firCount=0
                        var secCount=0
                        for( i <- 0 to variableNum-1)
                        {
                              if(((1<<i)&k)!=0)//存放第一个表达式尚有的变量名、类型、以及值
                              {
                                  tempVariableName(0)(firCount)=variableName(i)
                                  tempVariableType(0)(firCount)=variableType(i)
                                  tempVariableValue(0)(firCount)=variableValue(i)
                                  firCount=firCount+1
                              }
                              else//存放第二个表达式尚有的变量名、类型、以及值
                              {
                                  tempVariableName(1)(secCount)=variableName(i)
                                  tempVariableType(1)(secCount)=variableType(i)
                                  tempVariableValue(1)(secCount)=variableValue(i)
                                  secCount=secCount+1
                              }
                        }
                        return (tempVariableName,tempVariableType,tempVariableValue)
        }
        def ScalarToEnumLiteral(varValue:String,varType:String,translator:Expression2SMT)
        :EnumLiteral={
                        var variableValue=varValue.substring(varValue.lastIndexOf('_')+1,varValue.length)//最后一个'_'后面的字符串表示value
                        var variableType=varType.substring(varType.lastIndexOf('_')+1,varType.length)//最后一个'_'后面的字符串表示type
                        var flag=false//用来判断值是string还是int
                        for(j <- 0 to variableValue.length-1)
                        {
                            if(variableValue.charAt(j)>'9')
                            flag=true//flag为true表示此值为string类型
                        }
                        if(flag==false)//int
                          return  EnumLiteral(IntLiteral(variableValue.toLong), translator.scala2EnumType(variableType.toInt))
                        else//string
                            return  EnumLiteral(StringLiteral(variableValue), translator.scala2EnumType(variableType.toInt))
                        
        }
        def BitVecToBoolean(variableValue:String):Array[Boolean]={
                      val bitVecSize=variableValue.length
                      var valueOfVaribale=variableValue.substring(2)//去掉"#x"和"#b"
                      var arrSize=0//读出来的数转成二进制后的位数，也即boolean数组的大小
                      if(variableValue.charAt(1)=='x') //如果是十六进制，转为二进制的时候位数应该乘以4，这里的位数都不包括"#x"和"#b",所以要将Length-2
                      arrSize=(variableValue.length-2)*4 
                      else if(variableValue.charAt(1)=='b')  
                      arrSize=variableValue.length-2
                      var bitVecArray:Array[Boolean]=new Array(arrSize)
                      if(variableValue.charAt(1)=='x')
                          valueOfVaribale=BigInt(valueOfVaribale,16).toString(2) //如果variableValue(i)是十六进制，将十六进制转为二进制
                      var number=arrSize-1
                      for(j <- 0 to valueOfVaribale.length-1)//因为在将十六进制转为二进制的时候忽略前导0，比如0a会转为1010，所以在填充boolean数组时从后往前填，如果variableValue到第0个位置，说明boolean数组前面的都该填为false
                      {
                          if(valueOfVaribale.charAt(valueOfVaribale.length-1-j)=='0')
                              bitVecArray(number)=false
                          else if(valueOfVaribale.charAt(valueOfVaribale.length-1-j)=='1')
                              bitVecArray(number)=true  
                          number=number-1
                      }
                      for(j <- 0 to number)//variableValue到第0个位置，说明boolean数组前面的都该填为false
                          bitVecArray(number-j)=false 
                      return bitVecArray                    
        }
        def checkDivide(z3:Z3,result:Array[Expression],origionConstraint:Expression,translator:Expression2SMT):Boolean=
        {

                    val AndResult=ExpressionHelper.simplify(result(0)&result(1))
                    val AndResultFir=Not(AndResult)&origionConstraint
                    val AndResultSec=Not(origionConstraint)&AndResult
                    z3.push()
                    z3.assertConstraint(translator.convert(ExpressionHelper.simplify(AndResultFir)))
                    if(z3.checkSat())
                    {
                      z3.pop()
                      return false//如果!(P(A,b)&&P(a,B))&&R(A,B)为真，说明不可拆分
                    }
                    z3.pop()
                    z3.push()
                    z3.assertConstraint(translator.convert(ExpressionHelper.simplify(AndResultSec))) 
                    if(z3.checkSat())//如果(P(A,b)&&P(a,B))&&!R(A,B)为真，说明不可拆分   
                    { 
                      z3.pop()
                      return false
                    }
                    z3.pop()
                    return true    
        }
        def divideConstraint(z3:Z3, translator:Expression2SMT, varTypes:Map[String, Type], constraint:Expression)
        : Iterable[(Iterable[String], Expression)]=
       {   
                   var SMTTypes:Map[String,SMTType] = varTypes.map(a=>(a._1, translator.type2SMTType(a._2))) 
                   z3.push()
                   z3.declareVariables(SMTTypes)
                   z3.assertConstraint(translator.convert(constraint)) 
                   assert(z3.checkSat(),"The expression is unsat!")
                   var (resultFromZ3,groupNum)=z3.getValValueMap()
                   var resultInGroup:Array[Array[String]]=new Array(groupNum.toInt)
                  // assert(groupNum.toInt<=2,"The number of variable we get from z3 is more than 2!")
                   //assert(groupNum.toInt>=2,"The number of variable we get from z3 is less than 2!")
                   val result=new Array[Expression](2)
                   var resultAfterSplit=resultFromZ3.split(" ")
                   var groupLength=List[Int]()
                   var group=0
                   var count=0
                   var braceBegin=0
                   var braceEnd=0
                   var braceNum=0
                   for(eachString<-resultAfterSplit)//将每一个define进行分组，但首先要确定组在split后的list里面的上界和下界，这里用braceBegin表示下界，braceEnd表示上界
                   {
                        var length=eachString.length()//拆分后每个字符串的长度
                        for(i <- 0 to (length-1))//这里确定一组开始和结束的方法是，在每一组开始和结束左括号和右括号都是匹配的，在组的开始处braceNum=0,之后遇到一个左括号该值加一，遇到一个右括号该值减一，到结尾的时候该值为0
                        {
                           if(eachString.charAt(i)=='(')//之所以要遍历一个字符串的每个字符而不用contain，是因为一个字符串里面可能有多个括号，比如"()"，所以要遍历每个字符
                           {
                                if(braceNum==0)
                                  braceBegin=count//count记录当前字符串的位置
                                braceNum=braceNum+1
                           }
                           else if(eachString.charAt(i)==')')
                           {
                           	    braceNum=braceNum-1
                           	    if(braceNum==0)
                                {
                                  braceEnd=count
                                  var eachGroupLength=braceEnd-braceBegin+1//每一组的长度
                                  groupLength=groupLength:::List(eachGroupLength)//用list存放每组字符串的个数，之所以要存放，是因为返回BitVec和返回boolean int scalar的字符串的个数不一样，后面三个是一样的
                                  resultInGroup(group)=new Array(eachGroupLength)
                                  for(i <- 0 to (eachGroupLength-1))//将从braceBegin到braceEnd的字符串复制到每一组里面，一组表示对一个变量的赋值
                                    resultInGroup(group)(i)=resultAfterSplit(braceBegin+i)
                                  group=group+1
                                }
                           }
                        }
                        count=count+1
                    }
                    var variableName:Array[String]=new Array(group)//存放每个变量的名字
                    var variableType:Array[String]=new Array(group)//存放每个变量的类型，BitVec存的是BitVec类型
                    var variableValue:Array[String]=new Array(group)//存放每个变量的值
                    var indexCount=0
                    for(len <- groupLength)//对每组进行遍历
                    {
                    	if(len==8)//如果当前组的字符串数目为8，说明为int bool scalar三种类型之一
                    	{
                            variableName(indexCount)=resultInGroup(indexCount)(1)
                            variableType(indexCount)=resultInGroup(indexCount)(3)
                            var indexBrace=resultInGroup(indexCount)(7).indexOf(')')//去掉每个值最后一个括号，因为含值的字符串为"value)"
                            resultInGroup(indexCount)(7)=resultInGroup(indexCount)(7).substring(0,indexBrace)
                            variableValue(indexCount)=resultInGroup(indexCount)(7)
                    	}
                    	else if(len==10)//如果当前组的字符串数目为10，说明为BitVec,但也可能是scala类型的
                    	{
                            variableName(indexCount)=resultInGroup(indexCount)(1)
                            val temp=resultInGroup(indexCount)(7).substring(1,resultInGroup(indexCount)(7).length)
                            if(temp=="as")//scala类型
                            {//(define-fun z () T (as A T))
                               variableType(indexCount)=resultInGroup(indexCount)(3)
                               variableValue(indexCount)=resultInGroup(indexCount)(8)
                            }
                            else//BitVec类型
                            {
                             variableType(indexCount)=resultInGroup(indexCount)(4)
                             var indexBrace=resultInGroup(indexCount)(9).indexOf(')')
                             resultInGroup(indexCount)(9)=resultInGroup(indexCount)(9).substring(0,indexBrace)
                             variableValue(indexCount)=resultInGroup(indexCount)(9)
                            }

                    	}
                    	indexCount=indexCount+1
                    }
                    import org.kiama.rewriting.Rewriter._ 
                    for(i <- 0 to 1)//只考虑拆分成两个表达式
                    {
                        if(variableType(i)=="Bool")  result(i)= ExpressionHelper.simplify(rewrite(everywherebu(rule[Expression] {  case IdentifierRef(a) if a==variableName(i) =>BoolLiteral(variableValue(i).toBoolean)}))(constraint))
                        else if(variableType(i)=="Int")  result(i)= ExpressionHelper.simplify(rewrite(everywherebu(rule[Expression] {  case IdentifierRef(a) if a==variableName(i) =>IntLiteral(variableValue(i).toInt)}))(constraint))
                        else if(variableType(i)=="BitVec")//如果返回的是BitVec类型
                        {
                            val bitVecSize=variableValue(i).length
                            var valueOfVaribale=variableValue(i).substring(2)//去掉"#x"和"#b"
                            var arrSize=0//读出来的数转成二进制后的位数，也即boolean数组的大小
                            if(variableValue(i).charAt(1)=='x') //如果是十六进制，转为二进制的时候位数应该乘以4，这里的位数都不包括"#x"和"#b",所以要将Length-2
                                arrSize=(variableValue(i).length-2)*4 
                            
                            else if(variableValue(i).charAt(1)=='b')  
                                arrSize=variableValue(i).length-2
                            var bitVecArray:Array[Boolean]=new Array(arrSize)
                            if(variableValue(i).charAt(1)=='x')
                              valueOfVaribale=BigInt(valueOfVaribale,16).toString(2) //如果variableValue(i)是十六进制，将十六进制转为二进制
                            var k=arrSize-1
                            for(j <- 0 to valueOfVaribale.length-1)//因为在将十六进制转为二进制的时候忽略前导0，比如0a会转为1010，所以在填充boolean数组时从后往前填，如果variableValue到第0个位置，说明boolean数组前面的都该填为false
                            {
                                if(valueOfVaribale.charAt(valueOfVaribale.length-1-j)=='0')
                                   bitVecArray(k)=false
                                else if(valueOfVaribale.charAt(valueOfVaribale.length-1-j)=='1')
                                   bitVecArray(k)=true  
                                k=k-1
                            }
                            for(j <- 0 to k)//variableValue到第0个位置，说明boolean数组前面的都该填为false
                                bitVecArray(k-j)=false
                            var resultSet = Set[String]()
                            resultSet = translator.bitVector2Set(bitVecArray)
                            //在这里得到的是名为bitVecArray的boolean数组，下面需要将数组传到SMTBVLiteral里面，然后用老师写的函数得到值用rewrite替换变量即可
                            result(i)= ExpressionHelper.simplify(rewrite(everywherebu(rule[GExpression] {  case IdentifierRef(a) if a==variableName(i) =>SMTBVLiteral(bitVecArray)}))(constraint))
                        }
                        else //scalar类型
                        {
                            //在这里得到的是变量的string类型的值，下面需要将该值传到SMTScalarLiteral里面，然后用老师写的函数得到值用rewrite替换变量即可
                            variableValue(i)=variableValue(i).substring(variableValue(i).lastIndexOf('_')+1,variableValue(i).length)//最后一个'_'后面的字符串表示value
                            variableType(i)=variableType(i).substring(variableType(i).lastIndexOf('_')+1,variableType(i).length)//最后一个'_'后面的字符串表示type
                            var flag=false//用来判断值是string还是int
                            for(j <- 0 to variableValue(i).length-1)
                            {
                                if(variableValue(i).charAt(j)>'9')
                                  flag=true//flag为true表示此值为string类型
                            }
                            if(flag==false)
                            {//int
                                  result(i)= ExpressionHelper.simplify(rewrite(everywherebu(rule[Expression] {  case IdentifierRef(a) if a==variableName(i) => EnumLiteral(StringLiteral(variableValue(i)), translator.scala2EnumType(variableType(i).toInt)) }))(constraint))
                            }
                            else
                            {//string
                                  result(i)= ExpressionHelper.simplify(rewrite(everywherebu(rule[Expression] {  case IdentifierRef(a) if a==variableName(i) => EnumLiteral(IntLiteral(variableValue(i).toLong), translator.scala2EnumType(variableType(i).toInt)) }))(constraint))
                            }
                        }
                    }
                    val AndResult=ExpressionHelper.simplify(result(0)&result(1))
                    val AndResultFir=Not(AndResult)&constraint
                    val AndResultSec=Not(constraint)&AndResult
                    z3.assertConstraint(translator.convert(ExpressionHelper.simplify(AndResultFir)))
                    assert(z3.checkSat()==false,"Can not  be divided!")//如果!(P(A,b)&&P(a,B))&&R(A,B)为真，说明不可拆分
                    z3.assertConstraint(translator.convert(ExpressionHelper.simplify(AndResultSec))) 
                    assert(z3.checkSat()==false,"Can not  be divided!")//如果(P(A,b)&&P(a,B))&&!R(A,B)为真，说明不可拆分
                    z3.pop()
                    val varSet = varTypes.keySet
                    for(i <- 0 to 1)
                      yield (varSet.filter(_!=variableName(i)),result(i))//返回两个拆分后的Expression和其含的变量
            }

	//wj  end
  
  
  
  // This version is not supported anymore
  @deprecated("Use simpleDiagnoses2Fixes instead", "Since 2011/12")
  def diagnoses2Fixes(configuration:Map[String, Literal],
		      constraints:Iterable[Expression], // may contain semantic vars
		      semanticVars:Map[String, Expression],
		      semanticVarValues:Map[String, Literal],
		      ds:SemanticDiagnoses):Iterable[DataFix] = {
    import org.kiama.rewriting.Rewriter._
    
    val diagMap = mutable.Map[Diagnosis, Set[Diagnosis]]()
    for ((synt, semtc) <- ds) {
      diagMap.put(synt, diagMap.getOrElse(synt, Set()) + semtc)
    }
    val fixconstraints = 
      for ((synt, semntcs) <- diagMap) yield {
	//semantic part
	assert(semntcs.size > 0)
	val semntcsUnion = semntcs.reduce(_ ++ _)
	def svars2Constrts(svars:Diagnosis):Iterable[Expression] = {
	  svars.map(v => IdentifierRef(v) === semanticVarValues(v))
	}
	val fixedSemntcConstrts = svars2Constrts(semanticVars.keySet -- semntcsUnion)
	val nonFixedConstraints = 
	  if (semntcs.size > 1)
	    List(semntcs.map(d => semntcsUnion -- d).map(svars => {
	      assert(svars.size > 0)
	      svars2Constrts(svars).reduce(_ & _)
	    } ).reduce(_ | _))
	  else List()
	val semntcConstrts = fixedSemntcConstrts ++ nonFixedConstraints
	
	//syntactic & semantic part
	val syntConstrts = (constraints ++ semntcConstrts).map ( c => {
	  val replaced = rewrite(everywheretd(repeat(rule[Expression]{
	    case IdentifierRef(id) if (!synt.contains(id) && !semanticVars.contains(id)) => configuration(id)
	    case IdentifierRef(id) if (semanticVars.contains(id)) => semanticVars(id)
	  } )))(c)
	  val result = ExpressionHelper.simplify(replaced)
	  assert( result != BoolLiteral(false) )
	  result
	} ).filter(_ != BoolLiteral(true))
	
	syntConstrts 
      }

    fixconstraints.map(constraint2DataFix(_))						
  }

  private def constraint2DataFix(constraints:Iterable[Expression]):DataFix = { 
    type Clause = Map[Expression, Boolean]
    
    def clause2GExpr(c:Clause) = c.foldLeft[Expression](BoolLiteral(true))((expr, pair) => {
      val unitExpr = if (pair._2) pair._1 else Not(pair._1)
      if (expr == BoolLiteral(true)) unitExpr else Or(expr, unitExpr)
    } )
    def toCNF(constraint:Expression, expected:Boolean = true):List[Clause] = {
      def times(fs1:List[Clause], fs2:List[Clause]):List[Clause] = {
	val result = 
	(for{f1 <- fs1
	     f2 <- fs2
	     if (f1.forall(pair => {val (e,b1) = pair; val b2= f2.get(e); b2==None || b2.get==b1}))}
	 yield f1 ++ f2)
	simplifyCNF(result)
      }
      constraint match {
	case Not(e) => toCNF(e, !expected)
	case And(e1, e2) => 
	  if (expected) {
	    val fs1:List[Clause] = toCNF(e1, true)
	    val fs2:List[Clause] = toCNF(e2, true)
	    simplifyCNF(fs1 ++ fs2)
	  } 
	  else {
	    toCNF(Or(Not(e1), Not(e2)), true)
	  }
	case Or(e1, e2) =>
	  if (expected) times(toCNF(e1, true), toCNF(e2, true))
	  else toCNF(And(Not(e1), Not(e2)), true)
	case Implies(e1, e2) =>
	  toCNF(Or(Not(e1), e2))
	case e:Expression => {
	  List(Map(e -> expected))
	}
      }
    }
    def simplifyCNF(cnf:Iterable[Clause]) = {
      val toVisitClauses = mutable.Map[Expression, Boolean]() ++ cnf.filter(_.size == 1).map(_.head)
      val visitedClauses = mutable.Map[Expression, Boolean]()
      def removeContradict(cs:Iterable[Clause]):Iterable[Clause] = 
	if (toVisitClauses.size > 0) {
	  val (expr:Expression, b:Boolean) = toVisitClauses.head
	  val resultCs = cs.map(c => 
	    if (c.get(expr) == Some(!b)) {
	      val result = c - expr 
	      if ( result.size == 0 ) { //the whole CNF is false
		return List(Map())
	      }
	      if (result.size == 1) {
		if (visitedClauses.contains(result.head._1))
		  assert(visitedClauses(result.head._1) == result.head._2)
		else
		  toVisitClauses += result.head
	      }
	      result
	    } 
	    else c 
	  )
	  toVisitClauses -= expr
	  visitedClauses put (expr, b)
	  removeContradict(resultCs)
	}
      else cs
      
      
      def included(small:Clause, big:Clause):Boolean = {
	small.forall(pair => {val vb = big.get(pair._1); vb.isDefined && vb.get == pair._2})
      }
      var result = List[Clause]()
      removeContradict(cnf).foreach( f => {
	def filter(result:List[Clause]):List[Clause] = 
	  if (result.isEmpty) List(f)
	  else if (included(f, result.head)) filter(result.tail)
	  else if (included(result.head, f)) result
	  else result.head::(filter(result.tail))
	result = filter(result)
      } )
      result
    }
    
    if (constraints.size == 0) return new DataFix(List())
    
    val orgCNF = constraints.map(toCNF(_)).flatten
    val cnf = simplifyCNF(orgCNF)
    type Pair = (Clause, Set[String])
    val cnfVars:List[Pair] = cnf.map(clause => (clause, 
			                        clause.keySet.map(org.kiama.rewriting.Rewriter.collects {case IdentifierRef(id:String) => id}(_)).flatten))
    
    // find all clauses that share the same set of variables as cur
    def separateEqClass(cur:Pair, remainingClauses:Traversable[Pair]):(List[Pair], List[Pair]) = {
      val toBeAdded = mutable.ListBuffer[Pair]()
      var remaining = List[Pair]()
      var result = mutable.ListBuffer[Pair](cur)
      for(p <- remainingClauses) {
	if (!(cur._2 & p._2).isEmpty) {
	  toBeAdded += p
	}
	else {
	  remaining = p::remaining
	}
      }
      while (toBeAdded.size > 0) {
	val p = toBeAdded.head
	val (newToBeAdded, newRemaining) = separateEqClass(p, remaining)
	assert(newToBeAdded.head == p)
	remaining = newRemaining
	toBeAdded ++= newToBeAdded.tail
	toBeAdded.remove(0)
	result += p
      }
      (result.toList, remaining)
    }
    
    var remaining = cnfVars
    val result = mutable.ListBuffer[FixUnit]()
    while(remaining.size > 0) {
      val (eqClass, newRemaining) = separateEqClass(remaining.head, remaining.tail)
      val rangeUnit = eqClass.map(p => new RangeUnit(p._2, clause2GExpr(p._1))).reduceLeft(_ ++ _)
      val unit = ExpressionHelper.simplify(rangeUnit.constraint) match {
	case Eq(v:IdentifierRef, c:Expression) if rangeUnit.vars.size == 1 => new AssignmentUnit(rangeUnit.vars.head, c)
	case Eq(c:Expression, v:IdentifierRef) if rangeUnit.vars.size == 1 => new AssignmentUnit(rangeUnit.vars.head, c)
	case Not(v:IdentifierRef) => new AssignmentUnit(v.id, BoolLiteral(false))
	case v:IdentifierRef => new AssignmentUnit(v.id, BoolLiteral(true))
	case x => new RangeUnit(rangeUnit.variables, x)
      }
      result += unit
      remaining = newRemaining
    }
    new DataFix(result)
  }
  private def constraint2DataFix(z3:Z3, types:Expression.Types, constraints:Iterable[Expression]):DataFix={
    val dataFix = constraint2DataFix(constraints)
    if (SMTFixGenerator.divideUnits){
      val replacedUnits = dataFix.units.map(
        unit=>{
          if (unit.variables().size <= 1)
            Set(unit)
          else{
            val cons = unit.constraint
            val translator = new Expression2SMT(Set(cons), Map[String, Literal](), types)
            z3.push()
            val (varNames, varTypes, varValues) = getVariableValue(z3:Z3, translator, types, cons)
            val divideResult = temp_divideConstraint(z3:Z3, translator, cons, varNames, varTypes, varValues)
            z3.pop()
            constraint2DataFix(divideResult.map(_._2)).units // suppose the result doesn't need to be divided again
          }
        }
      ).flatten  
      new DataFix(replacedUnits)
    }
    else dataFix
  }
}
