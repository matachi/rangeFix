package ca.uwaterloo.gsd.rangeFix
import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import scala.collection.mutable.Stack
import scala.collection.mutable
import java.io._

class Diagnoses2FixTest extends FunSuite with ShouldMatchers {
  import Expression._
  test("semantic vars effective") {
    val constrt = (IdentifierRef("c") > 10) 
      val svc = IdentifierRef("a") + "b"
    val svd = IdentifierRef("b")
    val config = Map[String, Literal]("a" -> 1, "b" -> 2, "x" -> 3, "y" -> 2)
    val result = SMTFixGenerator.diagnoses2Fixes(config, List(constrt), 
			                         Map("c" -> svc, "d" -> svd), Map("c" -> IntLiteral(3), "d" -> IntLiteral(2)), List((Set("a"), Set("c"))))
    result.size should equal (1)
    result.head.units.size should equal(1)
    result.head.variables should equal(Set("a"))
    val rc = result.head.units.head.constraint 
    assert(rc == IdentifierRef("a") + 2 > 10) // a more desirable result should be a > 8, this could be achieved by converting the formula to a normal form (k1*x^n, k2*x^(n-1), ... kn*x, k)
  }

  test("contradict literal elimination") {
    val constrt = (IdentifierRef("a") & (!"a" | "b"))
    val config = Map[String, Literal]("a" -> false, "b" -> false)
    val types = Map[String, SingleType]() withDefaultValue BoolType
    def allC(e:Expression):Set[String]=Set("a","b","c")
    val result = SMTFixGenerator.simpleDiagnoses2Fixes(config, List(constrt), types, 
			                               List(Set("a", "b")),allC)
    result.size should equal (1)
    result.head.units.size should equal(2)
    result.head.variables should equal(Set("a", "b"))
    assert(result.head.units.head.isInstanceOf[AssignmentUnit])
    assert(result.head.units.tail.head.isInstanceOf[AssignmentUnit])
  }

  test("realworld test case") {
    val constrts = List[Expression]((!IdentifierRef("a") | "a"), (!IdentifierRef("a") | ("a" & (("a" & !"a") | (!"a" & "a")))), (!"b" | "b"), (IdentifierRef("a") | !"a"), (!"b" | "a"), (!"c"| "a"))
    val config = Map[String, Literal]("a" -> true, "b" -> true, "c" -> true)
    val types = Map[String, SingleType]() withDefaultValue BoolType
    val result = SMTFixGenerator.simpleDiagnoses2Fixes(config, constrts, types,
			                               List(Set("a", "b", "c")),e=>Set("a","b","c"))
    result.size should equal (1)
    result.head.units.size should equal(3)
    result.head.units.toSet should equal (Set(AssignmentUnit("a", false), AssignmentUnit("b", false), AssignmentUnit("c", false)))
  }
  test("sub expression false test") {
    val constrts = List[Expression](((!IdentifierRef("a") & "a") | "b") & "a")
    val config = Map[String, Literal]("a" -> false, "b" -> false)
    val types = Map[String, SingleType]() withDefaultValue BoolType
    val result = SMTFixGenerator.simpleDiagnoses2Fixes(config, constrts, types,
			                               List(Set("a", "b")),e=>Set("a","b","c"))
    result.size should equal (1)
    result.head.units.size should equal(2)
    result.head.variables should equal(Set("a", "b"))
  }

}

class EccManipulatorTest extends FunSuite with ShouldMatchers {
  test("change redboot") {
    val ecc = new EccManipulator("testfiles/realworld/redboot.ecc")
    ecc.getUnsatisfiedConstraints().size should equal (0)
    ecc.isFeatureActive("CYGNUM_HAL_COMMON_INTERRUPTS_STACK_SIZE") should equal (true)
    assert(!ecc.findReqConstraint("CYGSEM_HAL_STATIC_MMU_TABLES", 1).isDefined)
    assert(!ecc.findReqConstraint("hahaha", 0).isDefined)
    assert(ecc.findReqConstraint("CYGSEM_HAL_STATIC_MMU_TABLES", 0).isDefined)
    ecc.generateFix(0, PropagationStrategy).size should equal (1)
    ecc.generateFix(0, PropagationStrategy).head.variables.size should equal (0)
    ecc.generateFixWithUnchangeableVars(Set(0, 1), Set[String]()).head.variables.size should equal (0)
    ecc.generateFixWithUnchangeableVars(Set(0, 1), Set[String]()).head.variables.size should equal (0)
    ecc.changeFeature("CYGNUM_HAL_COMMON_INTERRUPTS_STACK_SIZE", IntOptionValue(100))
    ecc.isFeatureActive("CYGNUM_HAL_COMMON_INTERRUPTS_STACK_SIZE") should equal (true)
    ecc.getUnsatisfiedConstraints().size should equal (1)
    ecc.generateFix(ecc.getUnsatisfiedConstraintIndexes().head, PropagationStrategy).size should equal (1)
    ecc.generateFix(ecc.getUnsatisfiedConstraintIndexes().head, PropagationStrategy).head.variables should equal (Set("CYGNUM_HAL_COMMON_INTERRUPTS_STACK_SIZE_data"))
    ecc.generateFixWithUnchangeableVars(ecc.getUnsatisfiedConstraintIndexes().head, 
			                Set("CYGNUM_HAL_COMMON_INTERRUPTS_STACK_SIZE_data")).size should equal (0)
    ecc.generateFix(0, PropagationStrategy).size should equal (1)
    ecc.generateFix(0, PropagationStrategy).head.variables.size should equal (0)
    ecc.generateFixWithUnchangeableVars(Set(0, ecc.getUnsatisfiedConstraintIndexes().head), 
			                Set[String]()).size should equal (1)
    ecc.activateFeature("CYGNUM_HAL_ARM_LPC24XX_I2C1_INT_PRIO", PropagationStrategy).size should equal (1)
    ecc.activateFeature("CYGNUM_HAL_ARM_LPC24XX_I2C1_INT_PRIO", PropagationStrategy).head.variables should equal (Set("CYGHWR_HAL_ARM_LPC24XX_I2C1_SUPP_bool"))
    ecc.changeFeature("CYGHWR_HAL_ARM_LPC24XX_I2C1_SUPP", IntOptionValue(1))
    ecc.activateFeature("CYGNUM_HAL_ARM_LPC24XX_I2C1_INT_PRIO", PropagationStrategy).size should equal (1)
    ecc.activateFeature("CYGNUM_HAL_ARM_LPC24XX_I2C1_INT_PRIO", PropagationStrategy).head.variables.size should equal (0)
  }

  test("test time consuming file") {
    val ecc = new EccManipulator("testfiles/realworld/error.ecc")
    ecc.getUnsatisfiedConstraints().size should equal (4)
    val optError1 = ecc.findReqConstraint("DISK2", 0)
    val optError2 = ecc.findReqConstraint("DISK3", 0)
    val optError3 = ecc.findReqConstraint("DISK4", 0)
    val optError4 = ecc.findReqConstraint("RAID5", 0)
    assert(optError1.isDefined)
    assert(optError2.isDefined)
    assert(optError3.isDefined)
    assert(optError4.isDefined)
    ecc.getUnsatisfiedConstraintIndexes should equal (Set(optError1.get, optError2.get, optError3.get, optError4.get))
    ecc.generateFixWithUnchangeableVars(Set(optError1.get), Set[String]())
    ecc.generateFixWithUnchangeableVars(Set(optError2.get), Set[String]()) 
    ecc.generateFixWithUnchangeableVars(Set(optError3.get), Set[String]())
    Timer.printTime("Long execution:") {		
      ecc.generateFixWithUnchangeableVars(Set(optError4.get), Set[String]())
    }
  }

}

class DiagnosesTest extends FunSuite with ShouldMatchers {
  import SMTExpression._
  import SMTFixGenerator._
  import collection._
  
  private def toSetDiagnoses(ds:SemanticDiagnoses):Set[SemanticDiagnosis] = ds.toSet
  
  test("semantic vars effective") {
    val cs = List(SMTVarRef("c") > 10, "c" === SMTVarRef("a") + "b", "d" === "b")
    val config = Map[String, SMTLiteral]("a" -> 1, "b" -> 2, "c" -> 3, "d" -> 2)
    val types = Map("a" -> SMTIntType, "b" -> SMTIntType, "c" -> SMTIntType, "d" -> SMTIntType)
    val result = SMTFixGenerator.generateDiagnoses(config, Set("a", "b"), Set("c", "d"), cs, types, List())
    result.size should equal (1)
    result.toSet should contain ((Set("a"), Set("c")))
  }

  test("Scalar variables sets") {
    val c = SMTVarRef("a") === "b" 
    val config = Map[String, SMTLiteral]("a" -> SMTScalarLiteral("x"), "b" -> SMTScalarLiteral("y"))
    val t = SMTScalarType("A", Set("x", "y"))
    val types = Map("a" -> t, "b" -> t)
    val result = toSetDiagnoses(SMTFixGenerator.generateDiagnoses(config, Set("a", "b"), Set("a", "b"), List(c), types, List()))
    result.size should equal (2)
    result.toSet should contain ((Set("a"), Set("a")))
    result.toSet should contain ((Set("b"), Set("b")))
  }

  test("BitVector test") {
    val ba = new Array[Boolean](10)
    ba(9) = true
    val c = SMTBAnd(SMTBNot(SMTBVLiteral(ba)), "b") === SMTBVLiteral(new Array[Boolean](10))
    val ba1 = ba.clone()
    ba1(8) = true
    val config = Map[String, SMTLiteral]("b" -> SMTBVLiteral(ba1))
    val types = Map("b" -> SMTBVType(10))
    val result = toSetDiagnoses(SMTFixGenerator.generateDiagnoses(config, Set("b"), Set("b"), List(c), types, List()))
    result.size should equal (1)
    result.toSet should contain ((Set("b"), Set("b")))
  }
  
  
  test("Same variables sets") {
    val c = SMTVarRef("a") > "b"
    val config = Map[String, SMTLiteral]("a" -> 1, "b" -> 2)
    val types = Map("a" -> SMTIntType, "b" -> SMTIntType)
    val result = toSetDiagnoses(SMTFixGenerator.generateDiagnoses(config, Set("a", "b"), Set("a", "b"), List(c), types, List()))
    result.size should equal (2)
    result.toSet should contain ((Set("a"), Set("a")))
    result.toSet should contain ((Set("b"), Set("b")))
  }

  test("No solution test") {
    val c = (SMTIntLiteral(1) > 2) & (SMTVarRef("a") === "b")
    val config = Map[String, SMTLiteral]("a" -> 1, "b" -> 2)
    val types = Map("a" -> SMTIntType, "b" -> SMTIntType)
    val result = SMTFixGenerator.generateSimpleDiagnoses(config, Set("a", "b"), List(c), types, List())
    result.size should equal (0)
  }
  
}

class TypeTest extends FunSuite with ShouldMatchers {
  test("Negative Test 1") {
    val t1 = new NegativeType(BoolType)
    val t2 = NumberType
    (t1 & t2) should equal (Some(t2))
    (t1 | t2) should equal (t1)
  }
  test("Minus Test") {
    val t1 = new DisjunctiveType(StringType, BoolType)
    val t2 = StringType
    (t2 | BoolType) should equal (t1)
    (t1 & (t2 | BoolType)) should equal (Some(t1))
    (t1 >= BoolType) should equal (true)
  }
  test("Negative Test 2") {
    val t1 = new NegativeType(BoolType)
    val t2 = new DisjunctiveType(BoolType, NumberType)
    (t1 & t2) should equal (Some(NumberType))
    (t1 | t2) should equal (TypeHelper.anyType)
  }
}

class TypeInferrenceTest extends FunSuite with ShouldMatchers {
  import collection.immutable.ListSet

  private def parseIml(fileName:String) = {
    import gsd.cdl.parser.EcosIml
    val imlFile = fileName
    val imlNodes = EcosIml.parseFile(imlFile)
    imlNodes
  }
  
  
  private def inferFromFile(fileName:String) = {
    TypeHelper.inferTypes(parseIml(fileName))
  }
  
  private def inferFromCorrectFile(fileName:String) = {
    val (types, exceptions) = inferFromFile(fileName)
    exceptions.size should equal (0)
    types
  }
  
  val enum0123Type = EnumType(ListSet(IntLiteral(0), IntLiteral(1), IntLiteral(2), IntLiteral(3)))
  
  test("test rewriting") {
    import Expression._
    val expr = IdentifierRef("a") === 0
    val types = mutable.Map[String,Type]("a" -> new DisjunctiveType(enum0123Type, StringType))
    val newTypes = TypeHelper.autoChooseTypes(types)
    val newExpr = TypeHelper.autoChooseAndRewrite(expr, newTypes, Map(IntLiteral(0) -> List(enum0123Type)), Set("a"), BoolType)
    newExpr should equal ("a" === StringLiteral("0"))
    newTypes should equal (Map("a" -> StringType))
    
  }
  
  test("Boolean option") {
    val types = inferFromCorrectFile("testfiles/artificial/testBooleanOption.iml")
    types.size should equal (1)
    types.get("A") should equal (Some(BoolType))
  }

  test("OperatorUnderToInt") {
    val nodes = parseIml("testfiles/artificial/OperatorUnderToInt.iml")
    val n = nodes.head
    assert (n.id == "A")
    val newNode = Node(n.id,
                       n.cdlType,
                       n.display,
                       n.description,
                       n.flavor,
                       n.defaultValue, 
                       Some(ToInt(n.calculated.get)),
                       n.legalValues,
                       n.reqs,
                       n.activeIfs,
                       n.implements, 
                       n.children)			

    val (types, errors) = TypeHelper.inferTypes(List(newNode) ++ nodes.tail)
    errors.size should equal (1)
  }
  
  test("requires enforce no Boolean") {
    val types = inferFromCorrectFile("testfiles/artificial/requiresEnforceNoBool.iml")
    types.size should equal (2)
    types.get("A") should equal (Some(BoolType))
    types.get("B") should equal (Some(NumberType))
  }

  test("Dot Operator") {
    val types = inferFromCorrectFile("testfiles/artificial/dotOperator.iml")
    types.size should equal(3)
    types.get("A") should equal (Some(SetType))
    types.get("B") should equal (Some(SetType))
    types.get("C") should equal (Some(SetType))
  }
  
  test("Int enum type ") {
    val types = inferFromCorrectFile("testfiles/artificial/intEnumTypes.iml")
    types.size should equal (2)
    types.get("A") should equal (Some(EnumType(ListSet(IntLiteral(0), IntLiteral(1), IntLiteral(2), IntLiteral(3)))))
    types.get("B") should equal (Some(EnumType(ListSet(IntLiteral(0), IntLiteral(1), IntLiteral(2), IntLiteral(3)))))
  }

  test("0 1 enum type ") {
    val types = inferFromCorrectFile("testfiles/artificial/01EnumType.iml")
    types.get("A") should equal (Some(EnumType(ListSet(IntLiteral(0), IntLiteral(1)))))
    types.get("B") should equal (Some(EnumType(ListSet(IntLiteral(0), IntLiteral(1), IntLiteral(2)))))
  }
  
  test("String enum type ") {
    val types = inferFromCorrectFile("testfiles/artificial/stringEnumTypes.iml")
    types.size should equal (2)
    types.get("A") should equal (Some(EnumType(ListSet(IntLiteral(0), StringLiteral("aa"), StringLiteral("ab"), StringLiteral("cc")))))
    types.get("B") should equal (Some(EnumType(ListSet(IntLiteral(0), StringLiteral("aa"), StringLiteral("ab"), StringLiteral("cc")))))
  }

  test("Substr to set type ") {
    val types = inferFromCorrectFile("testfiles/artificial/substrToSet.iml")
    types.size should equal (3)
    types("A") should equal (new DisjunctiveType(StringType, SetType))
    types("B") should equal (SetType)
    types("C") should equal (SetType)
  }

  test("enum convertible ") {
    import ConditionalCompilation._
    IF[CompilationOptions.ENUM_CONVERTIBLE#v] {
      val types = inferFromCorrectFile("testfiles/artificial/enumConvertible.iml")
      types.size should equal (2)
      types.get("A") should equal (Some(NumberType))
      types.get("B") should equal (Some(EnumType(ListSet(IntLiteral(0), IntLiteral(1), IntLiteral(2), IntLiteral(4), IntLiteral(6)))))
    }
  }

  test("String enum type use") {
    val types = inferFromCorrectFile("testfiles/artificial/enumTypeUse.iml")
    types.get("A") should equal (Some(EnumType(ListSet(IntLiteral(0), StringLiteral("GDB_stubs"), StringLiteral("Generic")))))
  }

  test("Type conflict") {
    val (types, exceptions) = inferFromFile("testfiles/artificial/typeErrorInDefault.iml")
    assert( exceptions.size >= 1 )
  }

  test("Type conflict between two features") {
    val (types, exceptions) = inferFromFile("testfiles/artificial/typeErrorBetweenDefaults.iml")
    exceptions.size should equal (1)
    exceptions.head.reasons should equal (Set("A", "B"))
  }

  test("Type conflict between two features in different levels") {
    val (types, exceptions) = inferFromFile("testfiles/artificial/hierachicalTypeError.iml")
    exceptions.size should equal (1)
    exceptions.head.reasons should equal (Set("A", "B"))
  }

  test("Interface type error") {
    val (types, exceptions) = inferFromFile("testfiles/artificial/interfaceTypeError.iml")
    exceptions.size should equal (1)
    exceptions.head.reasons should equal (Set("A"))
  }
}

class fullConversionTest extends FunSuite with ShouldMatchers {
  import collection._
  private def parseEcc(fileName:String):(Iterable[Node], EccFile, Map[String, OptionValue]) = {
    import collection.JavaConversions._
    val eccLexer = new EccFullLexer(new FileReader(fileName))
    val eccParser = new EccFullParser(eccLexer)
    eccParser.parse()
    eccParser.errors.size should equal (0)
    val translatedNodes = NodeHelper.EccNodes2Nodes(eccParser.allNodes())
    (translatedNodes, eccParser.getEccFile(), eccParser.getDerivedValues())
  }

  private def parseAnnotations() = {
    import JavaConversions._
    val annotations = "testfiles/realworld/allModels.annotation"
    val annLexer = new AnnotationLexer(new FileReader(annotations))
    val annParser = new AnnotationParser(annLexer)
    annParser.parse()
    annParser.errors.foreach(println)
    annParser.errors.size should equal (0)
    (annParser.getNodeAnnotations, annParser.getTypeAnnotations)
  }

  
  def fixture =
    new {
      val (nodeAnns, typeAnns) = parseAnnotations()
    }

  

  private def outputTypeInferenceProcess(name:String,
			                 nodes:Iterable[Node], 
			                 values:collection.Map[String, OptionValue], 
			                 typeAnns2:TypeAnnotations) = {
    val (types, exceptions) = TypeHelper.inferTypes(nodes, values, typeAnns2.toTypeConstraints)
    val writer = new FileWriter("target/tempFiles/" + name + "-TypeErrors.txt")
    exceptions.foreach(e => writer.write(e + "\n"))
    writer.close()
    
    val unsolvedWriter = new FileWriter("target/tempFiles/" + name + "-UnsolvedTypes.txt")
    val solvedWriter = new FileWriter("target/tempFiles/" + name + "-SolvedTypes.txt")
    for(n <- nodes ) {
      def printOneNode(n:Node) {
	val t = types.getOrElse(n.id, TypeHelper.anyType)
	if (t.isInstanceOf[NegativeType] || t.isInstanceOf[DisjunctiveType])
	  unsolvedWriter.write(n.id + ":" + t + "\n")
	else
	  solvedWriter.write(n.id + ":" + t + "\n")
	n.children.foreach(printOneNode)
      }
      
      printOneNode(n)
    }
    unsolvedWriter.close
    solvedWriter.close
  }
  
  private def configEqual(computed:Map[String, Literal], inConfig:Map[String, ConfigValue]) = {
    val mComputed = mutable.Map[String, Literal]() ++ computed
    
    val unEqual = mutable.Map[String, (Literal, Literal)]()
    
    def boolEqual(id:String, r:Literal) = {
      if (mComputed(id + NodeHelper.boolVarSuffix) != (r))
	unEqual put (id + NodeHelper.boolVarSuffix, (mComputed(id + NodeHelper.boolVarSuffix), r))
      mComputed.remove(id + NodeHelper.boolVarSuffix)
    }
    def dataEqual(id:String, r:Literal) = {
      if (mComputed(id + NodeHelper.dataVarSuffix) != (r))
	unEqual put (id + NodeHelper.dataVarSuffix, (mComputed(id + NodeHelper.dataVarSuffix), r))
      mComputed.remove(id + NodeHelper.dataVarSuffix)
    }
    
    for((id, value) <- inConfig) {
      value match {
	case NoneConfigValue =>
	  case SingleConfigValue(r) if r.isInstanceOf[BoolLiteral] => 
	    boolEqual(id, r)
	case SingleConfigValue(r) =>
	  dataEqual(id, r)
	case DoubleConfigValue(lb, ld) => 
	  boolEqual(id, lb)
	dataEqual(id, ld)
      }
    }
    
    unEqual.foreach(println)
    unEqual.size should equal (0)
    mComputed.foreach(println)
    mComputed.size should equal (0)
  }
  
  private def testTypeCorrectFile(name:String):Iterable[ConstraintWithSource] = {
    new File("target/tempFiles/").mkdirs()
    val (nodes, eccFile, derivedValues) = parseEcc("testfiles/realworld/" + name + ".ecc")
    val values = eccFile.values.toMap
    
    val allNodes = (org.kiama.rewriting.Rewriter.collectl { case n:Node => n } (nodes)).map(n => (n.id, n)).toMap
    val nodes2 = fixture.nodeAnns.apply(nodes, name)
    val typeAnns2 = fixture.typeAnns.filter(nodes, name)
    outputTypeInferenceProcess(name, nodes2, values, typeAnns2)
    

    val (nodes3, types, typeCorrectValues) = TypeHelper.getTypesAndRewrite(nodes2, values, typeAnns2.toTypeConstraints)
    
    val allWriter = new FileWriter("target/tempFiles/" + name + "-ChosenTypes.txt")
    for (n <- allNodes.values) {
      allWriter.write(n.id + ":" + types(n.id) + "\n")
    }
    allWriter.close
    
    val allNodes3 = (org.kiama.rewriting.Rewriter.collectl { case n:Node => n } (nodes3)).map(n => (n.id, n)).toMap
    val imlConstratints = NodeHelper.typeCorrectNodes2Constraints(allNodes3, types)
    val constraintsWriter = new FileWriter("target/tempFiles/" + name + "-constraints.txt")
    constraintsWriter.write("============ Require Constraints =============\n")
    for (c <- imlConstratints.reqConstraints) {
      constraintsWriter.write(c.toString + "\n")
    }
    constraintsWriter.write("============ Legal Values =============\n")
    for ((id, c) <- imlConstratints.legalValueConstraints) {
      constraintsWriter.write(id + ": " + c.toString + "\n")
    }
    constraintsWriter.close()

    val semanticVarWriter = new FileWriter("target/tempFiles/" + name + "-semanticVars.txt")
    for ((id, expr) <- imlConstratints.semanticVars) {
      semanticVarWriter.write(id + ":=" + expr + "\n")
    }
    semanticVarWriter.close()
    
    val (config, semanticConfig) = NodeHelper.values2configuration(typeCorrectValues, allNodes3, types, imlConstratints.defaults, imlConstratints.semanticVars)
    val configWriter = new FileWriter("target/tempFiles/" + name + "-config.txt")
    configWriter.write("============ Real variables =============\n")
    for ((id, literal) <- config) {
      configWriter.write(id + ":=" + literal + "\n")
    }
    configWriter.write("============ Semantic Variables =============\n")
    for ((id, literal) <- semanticConfig) {
      configWriter.write(id + ":=" + literal + "\n")
    }
    configWriter.close()
    
    
    val (_, _, typeCorrectDerivedValues) = TypeHelper.getTypesAndRewrite(nodes2, derivedValues, typeAnns2.toTypeConstraints)
    configEqual(config, (typeCorrectDerivedValues.keySet ++ typeCorrectValues.keySet).map(id =>
      (id, if (typeCorrectDerivedValues(id) != NoneConfigValue) typeCorrectDerivedValues(id)
	   else typeCorrectValues(id))).toMap)

    for (sourceConstrint <- imlConstratints.allConstraints;
	 result = ExpressionHelper.evaluateTypeCorrectExpression(sourceConstrint.getConstraint, config ++ semanticConfig);
	 if result != BoolLiteral(true)) yield sourceConstrint
  }
  
  test("redboot") {
    val unsatisifed = testTypeCorrectFile("redboot")
    unsatisifed.foreach(println)
    unsatisifed.size should equal (0)
  }

  test("vmware-all") {
    val unsatisifed = testTypeCorrectFile("vmware-all")
    unsatisifed.size should equal (1)
    unsatisifed.head.getNodeID should equal ("CYGHWR_IO_FLASH_DEVICE")
  }
  
  test("talktic") {
    val unsatisifed = testTypeCorrectFile("talktic")
    unsatisifed.size should equal (0)
  }

  test("psas") {
    val unsatisifed = testTypeCorrectFile("psas")
    unsatisifed.size should equal (0)
  }

  test("init-default-56-unres") {
    val unsatisifed = testTypeCorrectFile("init-default-56-unres")
    unsatisifed.size should equal (56)
  }
}

class ExpressionTest extends FunSuite with ShouldMatchers {
  test ("evaluate isSubStr") {
    val expr1 = IsSubstr(SetLiteral(Set("-mthumb-interwork", "-mcpu=arm7tdmi", "-Wl,-static", "-nostdlib", "-Wl,--gc-sections", "-g")), SetLiteral(Set("-mthumb")))
    ExpressionHelper.evaluateTypeCorrectExpression(expr1) should equal (BoolLiteral(false))
    ExpressionHelper.simplify(expr1) should equal (BoolLiteral(false))
    val expr2 = IsSubstr(SetLiteral(Set("-mthumb-interwork", "-mcpu=arm7tdmi", "-Wl,-static", "-nostdlib", "-Wl,--gc-sections", "-g")), SetLiteral(Set("-mthumb-interwork")))
    ExpressionHelper.evaluateTypeCorrectExpression(expr2) should equal (BoolLiteral(true))
    ExpressionHelper.simplify(expr2) should equal (BoolLiteral(true))
    val expr3 = IsSubstr(SetLiteral(Set("-mthumb-interwork", "-mcpu=arm7tdmi", "-Wl,-static", "-nostdlib", "-Wl,--gc-sections", "-g")), SetLiteral(Set("-mno-thumb-interwork")))
    ExpressionHelper.evaluateTypeCorrectExpression(expr3) should equal (BoolLiteral(false))
    ExpressionHelper.simplify(expr3) should equal (BoolLiteral(false))
  }
  
}

class EccFullPaserTest extends FunSuite with ShouldMatchers {
  import org.kiama.rewriting.Rewriter._ 

  test("lexer - default") {
    val reader = new StringReader(" # Default value: 1+2\r\n}{")
    val lexer = new EccFullLexer(reader)
    lexer.yylex() should equal (EccFullParser.DEFAULT)
    lexer.getStartPos().getColumn() should equal (2)
    lexer.getEndPos().getColumn() should equal (17)
    lexer.yylex() should equal (EccFullParser.STRING)
    lexer.getLVal().asInstanceOf[Token].getText() should equal ("1+2")
    lexer.getStartPos().getLine() should equal (1)
    lexer.getStartPos().getColumn() should equal (19)
    lexer.getEndPos().getColumn() should equal (21)
    lexer.yylex() should equal ('}')
    lexer.yylex() should equal ('{')
  }
  
  test("lexer - default expression") {
    val reader = new StringReader(" # Default value: 1+2\r\n  #   --> 0 1")
    val lexer = new EccFullLexer(reader)
    lexer.yylex() should equal (EccFullParser.DEFAULT)
    lexer.getStartPos().getColumn() should equal (2)
    lexer.getEndPos().getColumn() should equal (17)
    lexer.yylex() should equal (EccFullParser.INT)
    lexer.getLVal().asInstanceOf[Token].getText() should equal ("1")
    lexer.getStartPos().getLine() should equal (1)
    lexer.getStartPos().getColumn() should equal (19)
    lexer.getEndPos().getColumn() should equal (19)
    lexer.yylex() should equal ('+')
    lexer.yylex() should equal (EccFullParser.INT)
    lexer.yylex() should equal (EccFullParser.EOF)
  }

  test("lexer - default with nothing following") {
    val reader = new StringReader(" # Default value:")
    val lexer = new EccFullLexer(reader)
    lexer.yylex() should equal (EccFullParser.DEFAULT)
    lexer.yylex() should equal (EccFullParser.EOF)
  }

  test("lexer - line break") {
    val reader = new StringReader(
      """    # Default value:  is_loaded(CYGNUM_HAL_KERNEL_COUNTERS_CLOCK_ISR_DEFAULT_PRIORITY) ?
      #      	                      CYGNUM_HAL_KERNEL_COUNTERS_CLOCK_ISR_DEFAULT_PRIORITY : 1 
      #     CYGNUM_HAL_KERNEL_COUNTERS_CLOCK_ISR_DEFAULT_PRIORITY (unknown) == 0
      #     CYGNUM_HAL_KERNEL_COUNTERS_CLOCK_ISR_DEFAULT_PRIORITY (unknown) == 0
      #   --> 1
	                                                    }""")
    val lexer = new EccFullLexer(reader)
    lexer.yylex() should equal (EccFullParser.DEFAULT)
    lexer.yylex() should equal (EccFullParser.ISLOADED)
    lexer.yylex() should equal ('(')
    lexer.yylex() should equal (EccFullParser.ID)
    lexer.yylex() should equal (')')
    lexer.yylex() should equal ('?')
    lexer.yylex() should equal (EccFullParser.ID)
    lexer.yylex() should equal (':')
    lexer.yylex() should equal (EccFullParser.INT)
    lexer.yylex() should equal ('}')
    lexer.yylex() should equal (EccFullParser.EOF)
  }

  
  test("lexer - calculated") {
    val reader = new StringReader("# Calculated value:  (CYGHWR_THUMB ? \" -mthumb \" : \"\") .")
    val lexer = new EccFullLexer(reader)
    lexer.yylex() should equal (EccFullParser.CALCULATED)
    lexer.getStartPos().getColumn() should equal (1)
    lexer.getLVal().asInstanceOf[Token].getLoc().getBegin().getColumn() should equal (1)
    lexer.yylex() should equal ('(')
    lexer.yylex() should equal (EccFullParser.ID)
    lexer.yylex() should equal ('?')
    lexer.yylex() should equal (EccFullParser.STRING)
    lexer.yylex() should equal (':')
    lexer.yylex() should equal (EccFullParser.STRING)
    lexer.yylex() should equal (')')
    lexer.yylex() should equal ('.')
  }

  private def transformDefault(defaultValue:Option[Expression], n:Node):Option[Expression] = {
    defaultValue match {
      case Some(e) => 
	try {
	  Some(ExpressionHelper.evaluateUntypedExpr(e))
	}
      catch {
	case _ => Some(e)
      }
      case None => 
	if (n.flavor == Flavor.None || n.cdlType == CdlType.Interface) None
	else Some(IntLiteral(0))
    }
  }
  
  private def parseCorrectEccFile(fileName:String) = {
    import collection.JavaConversions._
    val eccFile = fileName
    val eccLexer = new EccFullLexer(new FileReader(eccFile))
    val eccParser = new EccFullParser(eccLexer)
    eccParser.parse()
    eccParser.errors.size should equal (0)
    
    val values = eccParser.getEccFile.values.toMap
    val derivedValues = eccParser.getDerivedValues()
    for (id <- (values.keySet & derivedValues.keySet)) {
      (values(id) == NoneOptionValue || derivedValues(id) == NoneOptionValue) should equal (true)
    }

    
    val translatedNodes = NodeHelper.EccNodes2Nodes(eccParser.allNodes())
    rewrite( everywheretd ( rule[Node] {
      case n:Node => new Node(n.id,
			      n.cdlType,
			      "",
			      None,
			      n.flavor,
			      transformDefault(n.defaultValue, n),
			      n.calculated,
			      n.legalValues,
			      n.reqs,
			      n.activeIfs,
			      n.implements,
			      n.children)
    }))(translatedNodes)
  }
  
  private def parseCorrectImlFile(fileName:String) = {
    import gsd.cdl.parser.EcosIml
    val imlFile = fileName
    val imlNodes = EcosIml.parseFile(imlFile)
    val allNames = collects { case n:Node => n.id }(imlNodes)
    val translatedImlNodes = rewrite( everywheretd ( rule[Node] {
      case n:Node => new Node(n.id,
			      n.cdlType,
			      "",
			      None,
			      n.flavor,
			      transformDefault(n.defaultValue, n),
			      n.calculated,
			      n.legalValues,
			      n.reqs,
			      n.activeIfs,
			      n.implements.filter(allNames.contains(_)),
			      n.children)
    }))(imlNodes)
    translatedImlNodes
  }
  
  private def assertNodeEqual(l:Node, r:Node) {
    assert(l.id == r.id, " at " + l.id)
    assert(l.cdlType == r.cdlType, " at " + l.id)
    assert(l.flavor == r.flavor, " at " + l.id)
    assert(l.defaultValue == r.defaultValue, " at " + l.id)
    assert(l.calculated == r.calculated, " at " + l.id)
    assert(l.legalValues == r.legalValues, " at " + l.id + " with " + l.legalValues + " and " + r.legalValues + " not equal.")
    l.reqs.size should equal (r.reqs.size)
    for((r1, r2) <- l.reqs.zip(r.reqs))
      assert(r1 == r2, " at " + l.id + " with " + r1 + " and " + r2 + " not equal.")
    for((r1, r2) <- l.activeIfs.zip(r.activeIfs))
      assert(r1 == r2, " at " + l.id + " with " + r1 + " and " + r2 + " not equal.")
    assert(l.implements == r.implements, " at " + l.id)
    
    l.children.size should equal (r.children.size)
    for((c1, c2) <- l.children.zip(r.children))
      assertNodeEqual(c1, c2)
  }
  
  
  private def parseAndWriteFiles(prefix:String) {
    val eccNodes = parseCorrectEccFile("testfiles/realworld/" + prefix + ".ecc")
    val imlNodes = parseCorrectImlFile("testfiles/realworld/" + prefix + ".iml")
    // val imlWriter = new FileWriter("target/tempFiles/" + prefix + "-iml.txt")
    // imlNodes.foreach(n => imlWriter.write(n.toString))
    // imlWriter.close
    // val eccWriter = new FileWriter("target/tempFiles/" + prefix + "-ecc.txt")
    // eccNodes.foreach(n => eccWriter.write(n.toString))
    // eccWriter.close
    
    imlNodes.size should equal (eccNodes.size)
    for ((n1, n2) <- imlNodes.zip(eccNodes))
      assertNodeEqual(n1, n2)
  }

  test("redboot - compare") {
    parseAndWriteFiles("redboot")
  }
  test("psas - compare") {
    parseAndWriteFiles("psas")
  }
  test("talktic - compare") {
    parseAndWriteFiles("talktic")
  }

  test("vmware-all") {
    import scala.collection.JavaConversions._
    import ConditionalCompilation._
    val file = "testfiles/realworld/vmware-all.ecc"
    val lexer = new EccFullLexer(new FileReader(file))
    val parser = new EccFullParser(lexer)
    parser.parse()
    parser.errors.size should equal (0)
    val allNodes = org.kiama.rewriting.Rewriter.collectl {case n:Node => n} (NodeHelper.EccNodes2Nodes(parser.allNodes))
    // val imlWriter = new FileWriter("target/tempFiles/vmware-iml.txt")
    // allNodes.foreach(n => imlWriter.write(n.toString))
    // imlWriter.close
    allNodes.filter(_.id == "CYGNUM_LIBM_X_TLOSS").size should equal (1)
    val x = allNodes.filter(_.id == "CYGNUM_LIBM_X_TLOSS").head
    x.flavor should equal (Flavor.Data)
    IF[NOT[CompilationOptions.CONVERT_REAL_TO_INT]#v] {
      x.defaultValue should equal (Some(RealLiteral(1.41484755040569E+16)))
      x.legalValues should equal (Some(LegalValuesOption(List(MinMaxRange(IntLiteral(1), RealLiteral(1e308))))))
    }
  }


  test("redboot") {
    import collection.JavaConversions._
    val file = "testfiles/realworld/redboot.ecc"
    val lexer = new EccFullLexer(new FileReader(file))
    val parser = new EccFullParser(lexer)
    parser.parse()
    parser.errors.size should equal (0)
    val translatedNodes = NodeHelper.EccNodes2Nodes(parser.allNodes())
    val allNodes = (org.kiama.rewriting.Rewriter.collectl { case n:Node => (n.id, n) } (translatedNodes)).toMap
    allNodes("CYGFUN_HAL_COMMON_KERNEL_SUPPORT").defaultValue should equal (Some(IdentifierRef("CYGPKG_KERNEL")))
  }

  test("talktic") {
    val file = "testfiles/realworld/talktic.ecc"
    val lexer = new EccFullLexer(new FileReader(file))
    val parser = new EccFullParser(lexer)
    parser.parse()
    parser.errors.size should equal (0)
  }

  test("psas") {
    val file = "testfiles/realworld/psas.ecc"
    val lexer = new EccFullLexer(new FileReader(file))
    val parser = new EccFullParser(lexer)
    parser.parse()
    parser.errors.size should equal (0)
  }
}

class EccPaserTest extends FunSuite with ShouldMatchers {

  test("redboot") {
    val file = "testfiles/realworld/redboot.ecc"
    val lexer = new EccLexer(new FileReader(file))
    val parser = new EccParser(lexer)
    parser.parse()
    parser.errors.size should equal (0)
    parser.parsedFile.packages should have size (21)
    parser.parsedFile.packages should contain (PackageRef("CYGPKG_IO_I2C", "v3_0"))
    parser.parsedFile.packages should contain (PackageRef("CYGPKG_HAL_ARM", "v3_0"))
    parser.parsedFile.packages should contain (PackageRef("CYGPKG_CRC", "v3_0"))
    parser.parsedFile.values("CYGNUM_REDBOOT_CMD_LINE_EDITING") should equal (NoneOptionValue)
    parser.parsedFile.values("CYGDBG_HAL_COMMON_INTERRUPTS_SAVE_MINIMUM_CONTEXT") should equal (IntOptionValue(0))
    parser.parsedFile.values("CYGBLD_ISO_STRTOK_R_HEADER") should equal (DoubleOptionValue(IntOptionValue(1), StringOptionValue("<cyg/libc/string/string.h>")))

  }
  
  test("talktic") {
    val file = "testfiles/realworld/talktic.ecc"
    val reader = new FileReader(file)
    val lexer = new EccLexer(reader)
    val parser = new EccParser(lexer)
    parser.parse()
    parser.errors.size should equal (0)
    parser.parsedFile.packages should have size (28)
    parser.parsedFile.values("CYGDAT_REDBOOT_H8300_LINUX_BOOT_ENTRY") should equal (NoneOptionValue)
    parser.parsedFile.values("CYG_HAL_STARTUP") should equal (StringOptionValue("RAM"))
    parser.parsedFile.values("CYGBLD_GLOBAL_CFLAGS") should equal (StringOptionValue("-Wall -Wpointer-arith -Wstrict-prototypes -Winline -Wundef -Woverloaded-virtual -Os -mh -mint32 -fsigned-char -fdata-sections -fno-rtti -fno-exceptions -fvtable-gc -finit-priority"))

  }

  test("psas") {
    val file = "testfiles/realworld/psas.ecc"
    val reader = new FileReader(file)
    val lexer = new EccLexer(reader)
    val parser = new EccParser(lexer)
    parser.parse()
    parser.errors.size should equal (0)
  }
}



// class allSuites extends Specs(EccFullPaserTest, EccPaserTest, TypeInferrenceTest, TypeTest, DiagnosesTest, Diagnoses2FixTest, fullConversionTest )
// object curSuites extends Specs(Diagnoses2FixTest)
