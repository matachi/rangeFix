package ca.uwaterloo.gsd.rangeFix
import collection._

object X {
  // Needs to be in a separate object, since the compiler doesn't know that
  // they in Expression2SMT's alternative constructor do not access
  // Expression2SMT before it has been initialized. See:
  // http://stackoverflow.com/a/30823465/595990
  val collectLiterals: PartialFunction[Any, Literal] = { case a:Literal => a }
  val collectIds: PartialFunction[Any, Any] = { case IdentifierRef(id) => id }
}

// any constraints that is translated later should only contain the
// literals (in the case of EnumLiteral, the literals that are of the
// same enum type) and variables passed to the constructors
class Expression2SMT(literals:Iterable[Literal], types:Expression.Types) {

  def this(constraints:Iterable[Expression], config:Map[String, Literal], types:Expression.Types) = this ( 
    Expression.collectl(X.collectLiterals)(constraints ++ config.values),
    types filterKeys { Expression.collects(X.collectIds)(constraints).contains }
  )
  
  private val StringSeparator = " "
  private val EnumTypePrefix = "__enum__"
  private val TristateTypeName = "__tristate__"
  private val TristateTypeYes = "__tristate__yes"
  private val TristateTypeNo = "__tristate__no"
  private val TristateTypeMod = "__tristate__mod"
  // private val SMTTristateType = SMTScalarType(TristateTypeName, Set(TristateTypeNo, TristateTypeMod, TristateTypeYes))
  // private val MaxFuncName = "__max__"
  // private val MinFuncName = "__min__"
  // private val TristateCompFuncName = "__tristate_comp__"
  private val EnumIntPrefix = "__enum_int__"
  private val EnumStringPrefix = "__enum_str__"
  private val EnumToIntPrefix = "__enum_to_int__"
  private val EnumToStringPrefix = "__enum_to_str__"
  private val FuncPara1 = "__x1__"
  private val FuncPara2 = "__x2__"
  private val (allSetWords, allStrings, allEnumTypes):(Map[String, Int], Map[String, Int], Map[EnumType, Int]) = {
    val setWords = mutable.Set[String]()
    val strings = mutable.Set[String]()
    val enumTypes = mutable.Set[EnumType]()
    literals.foreach {
      case SetLiteral(s) => 
	setWords ++= s
      case StringLiteral(s) =>
	strings += s
      case EnumLiteral(_, t) => 
	enumTypes += t
      case _ =>
    }
    types.keys.map(types).filter(_.isInstanceOf[EnumType]).foreach(enumTypes += _.asInstanceOf[EnumType])
    
    var index = 0
    val resultSetWords = 
      (List(("0", 0)) ++
       (for (s <- (setWords - "0")) yield {
	 index += 1
	 (s, index)
       })).toMap

    index = 1
    val resultStrings = 
      (List(("", 0), ("0", 1)) ++
       (for (s <- strings) yield {
	 index += 1
	 (s, index)
       })).toMap
    
    // val x = new java.io.FileWriter("allStrings.txt")
    // for ((word, i) <- resultSetWords) x.write(word + ":" + i + "\n")
    // x.close
    
    
    index = 0
    val resultEnumTypes = 
      (for (e <- enumTypes) yield {
	index += 1
	(e, index - 1)
      }).toMap
    

    (resultSetWords, resultStrings, resultEnumTypes)
  }
  
  type BitVector = Array[Boolean]
  def BVLength = allSetWords.size
  
  private def enum2ScalarType(e:EnumType) = SMTScalarType(EnumTypePrefix + allEnumTypes(e), e.items.map(enumItem2String(_, e)))
  
  private def enumItem2String(x:EnumItemLiteral, t:EnumType) = x match {
    case StringLiteral(s) => EnumStringPrefix + allEnumTypes(t) + "_" + s
    case IntLiteral(i) => EnumIntPrefix + allEnumTypes(t) + "_" + i
  }
  
  private def enumItem2ScalarLiteral(x:EnumItemLiteral, t:EnumType) = SMTScalarLiteral(enumItem2String(x, t))
  
  def SMTTypes():Map[String, SMTType] = 
    types.mapValues(type2SMTType)
  
  def type2SMTTypeAndFunc(t:Type):(SMTType, Iterable[SMTFuncDefine]) = {(type2SMTType(t), type2SMTFunc(t))}

  def type2SMTType(t:Type):SMTType = {
    t match {
      case NumberType => SMTIntType
      case StringType => SMTIntType
      case BoolType => SMTBoolType
      case SetType => SMTBVType(BVLength)
      // case TristateType => SMTTristateType
      case e:EnumType => enum2ScalarType(e)
    }		
  }

  def type2SMTFunc(t:Type):Iterable[SMTFuncDefine] = {
    t match {
      case e:EnumType => List(enum2IntFunc(e))
      case _ => List()
    }		
  }
  
  def convertTypes(types:Map[String, Type]) = (types.mapValues(type2SMTType), types.values.toSeq.distinct.map(type2SMTFunc).flatten)
    

    // the converstion functions converting enums to ints
    private def enum2IntFunc(t:EnumType) = {
      val index = allEnumTypes(t)
      assert(t.items.size > 1)
      def toInt(el:EnumItemLiteral):SMTExpression = el match {
	case IntLiteral(i) => SMTIntLiteral(i)
	case StringLiteral(s) => 
	  try { 
	    SMTIntLiteral(s.toInt)
	  } catch {
	    case _:NumberFormatException => SMTIntLiteral(0)
	  }
      } 
      val body = t.items.tail.foldRight(toInt(t.items.head))((l, e) =>
        SMTConditional(SMTVarRef(FuncPara1) === enumItem2ScalarLiteral(l, t), toInt(l), e))
      SMTFuncDefine(EnumToIntPrefix + index, List((FuncPara1, enum2ScalarType(t))), SMTIntType, body)
    }
  
  // // the result can be true or false if the two values are equal
  // private def tristateLarger = SMTFuncDefine(
  //   TristateCompFuncName, 
  //   List((FuncPara1, SMTTristateType), (FuncPara2, SMTTristateType)), 
  //   SMTBoolType, 
  //   SMTConditional(SMTVarRef(FuncPara1) === SMTScalarLiteral(TristateTypeYes), 
  //       	   SMTBoolLiteral(true), 
  //       	   SMTConditional(SMTVarRef(FuncPara1) === SMTScalarLiteral(TristateTypeMod),
  //       		          SMTConditional(SMTVarRef(FuncPara1) === SMTScalarLiteral(TristateTypeYes),
  //                                                SMTBoolLiteral(false), SMTBoolLiteral(true)),
  //       		          SMTBoolLiteral(false)
  //       	                )
  //                )
  // )
  
  // private def max() = SMTFuncDefine(
  //   MaxFuncName, 
  //   List((FuncPara1, SMTTristateType), (FuncPara2, SMTTristateType)), 
  //   SMTTristateType, 
  //   SMTConditional(SMTUserFuncCall(TristateCompFuncName, SMTVarRef(FuncPara1), SMTVarRef(FuncPara2)),
  //                  SMTVarRef(FuncPara1), SMTVarRef(FuncPara2))
  // )

  // private def min() = SMTFuncDefine(
  //   MinFuncName, 
  //   List((FuncPara1, SMTTristateType), (FuncPara2, SMTTristateType)),
  //   SMTTristateType, 
  //   SMTConditional(SMTUserFuncCall(TristateCompFuncName, SMTVarRef(FuncPara1), SMTVarRef(FuncPara2)),
  //                  SMTVarRef(FuncPara2), SMTVarRef(FuncPara1))
  // )	
  
  private def allIntFuncs() = {
    for((t, index) <- allEnumTypes) yield {
      assert(t.items.size > 1)
      def toInt(el:EnumItemLiteral):SMTExpression = el match {
	case IntLiteral(i) => SMTIntLiteral(i)
	case StringLiteral(s) => 
	  try { 
	    SMTIntLiteral(s.toInt)
	  } catch {
	    case _:NumberFormatException => SMTIntLiteral(0)
	  }
      } 
      val body = t.items.tail.foldRight(toInt(t.items.head))((l, e) => SMTConditional(SMTVarRef(FuncPara1) === enumItem2ScalarLiteral(l, t), toInt(l), e))
      SMTFuncDefine(EnumToIntPrefix + index, List((FuncPara1, enum2ScalarType(t))), SMTIntType, body)
    }
  }
  
  def allFunctions():Iterable[SMTFuncDefine] = allIntFuncs // String functions are not implemented since we have not encountered them
  
  private def string2Int(str:String):SMTIntLiteral = SMTIntLiteral(allStrings(str))
  
  private def set2BitVector(items:Set[String]):SMTBVLiteral = {
    val result = new BitVector(BVLength)
    for(s <- items) {
      result(allSetWords(s)) = true
    }
    SMTBVLiteral(result)
  }
  //**wj  begin
  def bitVector2Set(items:Array[Boolean]):Set[String]={//根据boolean数组得到满足条件的set
    var resultSet = Set[String]()//用来收集集合中的元素
    var i = allSetWords.keySet.iterator//迭代器
    while(i.hasNext){//遍历   
      var str = i.next
      if(items(allSetWords(str))){//如果allSetWord的value对应的boolean值为true,就将allSetWords的key加入集合
        resultSet += str
       }
     }
     resultSet//返回该集合
  }
  def scala2EnumType(num : Int):EnumType={
      var i=allEnumTypes.keySet.iterator//迭代器
      var enumType=i.next
      var flag=true
      while(i.hasNext&&flag){
        var temp= i.next
        if(allEnumTypes(temp)==num)//寻找和编号num对应的EnumType
        {
         enumType=temp
         flag=false
        }
      }
      enumType//返回和编号num对应的EnumType
  }
 //**wj end
//**hs
  def convertFunctionDefine(funcDef:FunctionDef):SMTFuncDefine = {
    SMTFuncDefine(funcDef.name,
                  funcDef.params.map(x=>(x._1,type2SMTType(x._2))),
                  type2SMTType(funcDef.returnType),convert(funcDef.body))
  }


  def convert(expr:Expression):SMTExpression = expr match {
    case StringLiteral(s) => string2Int(s)
    case SetLiteral(items) => set2BitVector(items)
    case IntLiteral(i) => SMTIntLiteral(i)
    case BoolLiteral(b) => SMTBoolLiteral(b)
    case RealLiteral(r) => throw new IllegalArgumentException("found real value")
    case EnumLiteral(v, t) => enumItem2ScalarLiteral(v, t)
    case IdentifierRef(id) => SMTVarRef(id)
    case Conditional(c, p, f) => SMTConditional(convert(c), convert(p), convert(f))
    case And(l, r) => SMTAnd(convert(l), convert(r))
    case Or(l, r) => SMTOr(convert(l), convert(r))
    case Implies(l, r) => SMTImplies(convert(l), convert(r))
    case Eq(l, r) => SMTEq(convert(l), convert(r))
    case NEq(l, r) => SMTNot(SMTEq(convert(l), convert(r)))
    case LessThan(l, r) => SMTLessThan(convert(l), convert(r))
    case LessThanOrEq(l, r) => SMTLessEqThan(convert(l), convert(r))
    case GreaterThan(l, r) => SMTGreaterThan(convert(l), convert(r))
    case GreaterThanOrEq(l, r) => SMTGreaterEqThan(convert(l), convert(r))
    case Plus(l, r) => SMTPlus(convert(l), convert(r))
    case Minus(l, r) => SMTMinus(convert(l), convert(r))
    case Times(l, r) => SMTTimes(convert(l), convert(r))
    case Div(l, r) => SMTDivide(convert(l), convert(r))
    case Mod(l, r) => SMTMod(convert(l), convert(r))
    case Dot(l, r) => SMTBOr(convert(l), convert(r))
    case Not(l) => SMTNot(convert(l))
    case IsSubstr(l, r) => 
      SMTBAnd(SMTBNot(convert(l)), convert(r)) === SMTBVLiteral(new BitVector(BVLength))
    case ToInt(e) => ExpressionHelper.getType(e, types) match {
      case StringType => throw new IllegalArgumentException("converting string to int")
      case SetType => throw new IllegalArgumentException("converting set to int")
      case NumberType => convert(e)
      case BoolType => SMTConditional(convert(e), SMTIntLiteral(1), SMTIntLiteral(0))
      case t:EnumType => SMTUserFuncCall(EnumToIntPrefix + allEnumTypes(t), convert(e))
    }
    case ToString(e) => throw new IllegalArgumentException("toString is not supported yet")
    case ToBool(e) => ExpressionHelper.getType(e, types) match {
      case SetType => SMTNot(convert(e) === set2BitVector(Set())) & SMTNot(convert(e) === set2BitVector(Set("0")))
      case StringType => SMTNot(convert(e) === string2Int("")) & SMTNot(convert(e) === string2Int("0"))
      case NumberType => SMTNot(convert(e) === 0)
      case BoolType => convert(e)
      case t:EnumType => SMTNot(convert(e) === enumItem2ScalarLiteral(IntLiteral(0), t))
      case x@_ => throw new IllegalArgumentException("unexpected expression:" + x)
    }
    case UserFunctionCall(func, args)=>SMTUserFuncCall(func.name,args.map(convert):_*)
  }
}
