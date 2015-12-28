package ca.uwaterloo.gsd.rangeFix
import java.io._
import collection._

object SerializedEccManipulator {
  // serialization does not work because of some strange bugs in Scala. Have to do it manually
  def saveConfig(config: Map[String, ConfigValue], o: ObjectOutputStream) {
    o.writeInt(config.size)
    for ((k, v) <- config) {
      o.writeObject(k)
      v.save(o)
    }
  }

  def loadConfig(i: ObjectInputStream): Map[String, ConfigValue] = {
    val size = i.readInt
    (for (index <- 1 to size) yield {
      val k = i.readObject.asInstanceOf[String]
      val v = ConfigValue.load(i)
                              (k, v)
    }).toMap
  }
}

abstract class Strategy {
  def fix(smt: FixGenerator, constraintIndex: Int, satisfiedConstraints: Set[Int]): Iterable[DataFix]
}

object PropagationStrategy extends Strategy {
  def fix(smt: FixGenerator, constraintIndex: Int, satisfiedConstraints: Set[Int]) = {
    smt.fix(constraintIndex, satisfiedConstraints)
  }
  override def toString="propagation"
}
object IgnoranceStrategy extends Strategy {
  def fix(smt: FixGenerator, constraintIndex: Int, satisfiedConstraints: Set[Int]) = {
    smt.fixWithIgnorance(constraintIndex)
  }
  override def toString="ignorance"
}
object EliminationStrategy extends Strategy {
  def fix(smt: FixGenerator, constraintIndex: Int, satisfiedConstraints: Set[Int]) = {
    smt.fixWithElimination(constraintIndex, satisfiedConstraints)
  }
  override def toString="elemination"
}

class SerializedEccManipulator(serializedArray: Array[Byte]) extends Serializable {
  def this(ecc: EccManipulator) = this{
    val byteArrayStream = new ByteArrayOutputStream
    val output = new ObjectOutputStream(byteArrayStream)
    try {
      output.writeObject(ecc.file)
      output.writeObject(ecc.globalAnnotationPath)
      SerializedEccManipulator.saveConfig(ecc.config, output)
    } finally {
      output.close()
    }
    val result = byteArrayStream.toByteArray
    assert({
      val input = new ObjectInputStream(new ByteArrayInputStream(result))
      val first = ecc.file == input.readObject.asInstanceOf[String]
      val second = ecc.globalAnnotationPath == input.readObject.asInstanceOf[String]
      val third = ecc.config == SerializedEccManipulator.loadConfig(input)
      first && second && third
    })
    result
  }

  def get() = {
    val input = new ObjectInputStream(new ByteArrayInputStream(serializedArray))
    try {
      val file = input.readObject.asInstanceOf[String]
      val annotation = input.readObject.asInstanceOf[String]
      val result = new EccManipulator(file, annotation)
      result.config = SerializedEccManipulator.loadConfig(input)
      result
    } finally {
      input.close
    }
  }
}



class EccManipulator(val file: String, val globalAnnotationPath: String = "testfiles/realworld/allModels.annotation") {

  def getUnsatisfiedConstraints(): Set[ConstraintWithSource] = getUnsatisfiedConstraintIndexes.map(getConstraint)
  def getSatisfiedConstraints(): Set[ConstraintWithSource] = getSatisfiedConstraintIndexes.map(getConstraint)
  def getUnsatisfiedConstraintIndexes(): Set[Int] = {
    if (satisficationNeedsUpdate) {
      checkConflictsWithIndex
    }
    unsatisfiedConstraints
  }
  def getSatisfiedConstraintIndexes(): Set[Int] = {
    if (satisficationNeedsUpdate) {
      checkConflictsWithIndex
    }
    satisfiedConstraints
  }

  // This function is only used in tests to locate the index of a req constraint by name
  def findReqConstraint(featureID: String, reqIndex: Int): Option[Int] = {
    for {
      i <- constraintRange
      if allConditions(i).getSourceObject().isInstanceOf[ReqSource]
      c = allConditions(i).getSourceObject().asInstanceOf[ReqSource]
    } {
      if (c.nodeID == featureID && reqIndex == c.reqIndex) return Some(i)
    }
    return None
  }
  def findActiveIfConstraint(featureID: String): Option[Int] = getActiveConditionIndex(featureID)

  def getConstraint(index: Int): ConstraintWithSource = {
    if (!constraintRange.contains(index))
      throw new java.lang.IllegalArgumentException("constraint index out of range")
    allConditions(constraintRange.head + index)
  }
  def getConstraintSize = constraintRange.size
  def getFeatureSize = ecc.allNodes.size
  def isFeatureActive(id: String): Boolean = {
    if (getActiveConditionIndex(id).isEmpty) throw new Exception(id + " not found.")
    ExpressionHelper.evaluateTypeCorrectExpression(
      getActiveCondition(id).get, getValuation) == BoolLiteral(true)
  }
  def changeFeature(id: String, value: OptionValue) = {
    val cValue = ecc.convertOptionValue(value, id)
    config = config + (id -> cValue)
    valuationNeedsUpdate = true
    satisficationNeedsUpdate = true
    preTranslationNeedsUpdate = true
  }
  def getValuation: Map[String, Literal] = {
    if (valuationNeedsUpdate) updateValuation
    valuation
  }
  def convertSingleOptionValueToValuation(id: String, value: OptionValue): Iterable[(String, Literal)] =
    ecc.convertOptionValue(value, id) match {
      case SingleConfigValue(v) =>
        if (getFeatureFlavor(id) == Flavor.Bool)
          List((NodeHelper.toBoolVar(id), v))
        else {
          assert(getFeatureFlavor(id) == Flavor.Data)
          List((NodeHelper.toDataVar(id), v))
        }
      case DoubleConfigValue(b, v) => List((NodeHelper.toBoolVar(id), b), (NodeHelper.toDataVar(id), v))
    }
  def getFeatureFlavor(id: String) = {
      ecc.allNodes(id).flavor
    }
  def save(outFile: String) = {
    def getHeader = {
      val in = new BufferedReader(new FileReader(file))
      try {
        val configStartPattern = "cdl_configuration eCos".r
        val endPattern = "cdl_component|cdl_package|cdl_option|cdl_interface".r
        val sb = new StringBuilder
        var nextLine = in.readLine
        while (nextLine != null && configStartPattern.findFirstIn(nextLine).isEmpty) {
          sb ++= nextLine
          sb ++= "\n"
          nextLine = in.readLine
        }
        while (nextLine != null && endPattern.findFirstIn(nextLine).isEmpty) {
          sb ++= nextLine
          sb ++= "\n"
          nextLine = in.readLine
        }
        sb.toString
      } finally {
        in.close
      }
    }

    val header = getHeader
    val out = new BufferedWriter(new FileWriter(outFile))
    try {
      out.write(header)

      for ((id, value) <- config) {
        val typeStr = ecc.allNodes(id).cdlType match {
          case CdlType.Package => "cdl_package"
          case CdlType.Component => "cdl_component"
          case CdlType.Interface => "cdl_interface"
          case CdlType.Option => "cdl_option"
        }
        out.write(typeStr)
        out.write(" ")
        out.write(id)
        out.write(" {\n\t")

        val userValue = value match {
          case SingleConfigValue(l) => "user_value " + literal2EccString(l)
          case DoubleConfigValue(b, l) => "user_value " + literal2EccString(b) + " " + literal2EccString(l)
          case NoneConfigValue => ""
        }
        out.write(userValue)
        out.write("\n}\n\n")
      }
    } finally {
      out.close()
    }
  }

  private def encodeString(str: String): String =
    str.map(_ match {
      case '\\' => "\\\\"
      case '\"' => "\\\"" //" This stupid comment is to get the more stupid Notepad++ to work
      case '\'' => "\\\'" //" This stupid comment is to get the more stupid Notepad++ to work
      case '$' => "\\$"
      case c: Char => new String(Array(c))
    }).reduce(_ + _)

  def literal2EccString(v: Literal): String = v match {
    case StringLiteral(s) => "\"" + encodeString(s) + "\""
    case IntLiteral(i) => i.toString
    case EnumLiteral(v, t) => literal2EccString(v)
    case BoolLiteral(true) => "1"
    case BoolLiteral(false) => "0"
    case RealLiteral(r) => r.toString
    case SetLiteral(values) => "\"" + values.map(encodeString).reduceLeft(_ + " " + _) + "\""
  }

  def generateFix(constraintIndex: Int, strategy: Strategy): Iterable[DataFix] = {
    generateFixWithTime(constraintIndex, 1, strategy)._1
  }

  def generateFixWithTime(constraintIndex: Int, executionTimes: Int, strategy: Strategy): (Iterable[DataFix], Long) = {
    fixConstraintWithTime(constraintIndex, executionTimes, strategy)
  }

  private def prepareFix(constraintIndex: Int) {
    if (!(0 until allConditions.size).contains(constraintIndex))
      throw new java.lang.IllegalArgumentException("constraint index out of range")
    if (satisficationNeedsUpdate) checkConflictsWithIndex
    if (preTranslationNeedsUpdate) updateFixGenerator
  }
  private def prepareFixes(constraintIndexes: Set[Int]) {
    for (constraintIndex <- constraintIndexes)
    if (!(0 until allConditions.size).contains(constraintIndex))
      throw new java.lang.IllegalArgumentException("constraint index out of range")
    if (satisficationNeedsUpdate) checkConflictsWithIndex
    if (preTranslationNeedsUpdate) updateFixGenerator
  }

  def generateFixWithUnchangeableVars(constraintIndex: Int, unchangeableVars: Set[String]): Iterable[DataFix] = {
    generateFixWithUnchangeableVars(Set(constraintIndex), unchangeableVars)
  }

  def generateFixWithUnchangeableVars(constraintIndexes: Set[Int], unchangeableVars: Set[String]): Iterable[DataFix] = {
    prepareFixes(constraintIndexes)
    preTranslation.fix(constraintIndexes, satisfiedConstraints, unchangeableVars)
  }

  // // return value 1: related variables
  // // return value 2: the indexes of related constarints
  // def getRelatedVarsAndConstraints(constraintIndex: Int): (Set[String], Set[Int]) = {
  //   prepareFix(constraintIndex)
  //   preTranslation.getRelatedConstraints(Set(constraintIndex), satisfiedConstraints)
  // }

  private def fixConstraintWithTime(constraintIndex: Int, executionTimes: Int, strategy: Strategy): (Iterable[DataFix], Long) = {
    prepareFix(constraintIndex)
    val result = Timer.measureTime {
      val result = strategy.fix(preTranslation, constraintIndex, satisfiedConstraints)
      for (i <- 1 until executionTimes)
      strategy.fix(preTranslation, constraintIndex, satisfiedConstraints)
      result
    }
    (result, Timer.lastExecutionMillis / executionTimes)
  }

  def activateFeature(id: String, strategy: Strategy): Iterable[DataFix] = {
    activateFeatureWithTime(id, 1, strategy)._1
  }
  def activateFeatureWithTime(id: String, executionTimes: Int, strategy: Strategy): (Iterable[DataFix], Long) = {
    if (getActiveConditionIndex(id).isEmpty)
      throw new java.lang.IllegalArgumentException("feature cannot be found.")
    val constraintIndex = getActiveConditionIndex(id).get
    fixConstraintWithTime(constraintIndex, executionTimes, strategy)
  }

  def getActiveCondition(id: String):Option[Expression] =
    getActiveConditionIndex(id).map(allConditions).map(_.getConstraint)

  private def updateFixGenerator() {
    preTranslation = FixGenerator.create(
      allConditions.map(_.getConstraint),
      imlConstratints.types,
      getValuation)
  }

  private val ecc: TypedEcc = loadEccFileAndInferTypes(file)
  private val imlConstratints: ImlConstraints = convertNodes(ecc, true)
  private val (allConditions, constraintRange: immutable.Range, getActiveConditionIndex) = {
    val allConstraints = Vector[ConstraintWithSource]() ++ imlConstratints.allConstraints.filter(_.getConstraint != BoolLiteral(true))
    val orderedIds = Vector() ++ ecc.allNodes.keySet
    val activeConditionMap = (for (i <- 0 until orderedIds.size) yield (orderedIds(i), i + allConstraints.size)).toMap
    (
      allConstraints ++ orderedIds.map(id => ActiveIfCondition(imlConstratints.activeConditions(id), id)).asInstanceOf[IndexedSeq[ConstraintWithSource]],
      0 until allConstraints.size,
      activeConditionMap.get _)
  }

  var config: Map[String, ConfigValue] = ecc.config
  private var valuationNeedsUpdate = true
  private var valuation: Map[String, Literal] = null
  private def updateValuation() {
    valuation = convertConfig(config, ecc, imlConstratints)._1
    valuationNeedsUpdate = false
  }
  private var satisficationNeedsUpdate = true
  private var satisfiedConstraints: Set[Int] = Set()
  private var unsatisfiedConstraints: Set[Int] = Set()
  private var preTranslationNeedsUpdate = true
  private var preTranslation: FixGenerator = null

  private case class TypedEcc(allNodes: Map[String, Node],
                              types: Map[String, SingleType],
                              config: Map[String, ConfigValue],
                              convertOptionValue: (OptionValue, String) => ConfigValue)
  private def loadEccFileAndInferTypes(file: String): TypedEcc = {
    // load an ecc file
    val (orgNodes, eccFile) = parseEcc(file)
    val orgValues = eccFile.values.toMap
    val (nodeAnns, allTypeAnns) = parseAnnotations()
    val fileName = {
      val t = new java.io.File(file).getName;
      val i = t.indexOf(".");
      if (i >= 0) t.substring(0, i) else t
    }
    val annotatedNodes = nodeAnns.apply(orgNodes, fileName)
    val typeAnns = allTypeAnns.filter(orgNodes, fileName)

    // infer types and rewrite expressions
    val (nodes, types, rewriteFunc) = TypeHelper.getTypesAndRewriteNodes(annotatedNodes, orgValues, typeAnns.toTypeConstraints)
    val config = TypeHelper.convertConfig(orgValues, types, rewriteFunc)

    val allNodes = (org.kiama.rewriting.Rewriter.collectl { case n: Node => n }(nodes)).map(n => (n.id, n)).toMap

    def convertOptionValue(v: OptionValue, id: String) = TypeHelper.convertOptionValue(v, rewriteFunc(_, types(id)))

    TypedEcc(allNodes, types, config, convertOptionValue)
  }

  private def parseAnnotations() = {
    val annLexer = new AnnotationLexer(new FileReader(globalAnnotationPath))
    val annParser = new AnnotationParser(annLexer)
    annParser.parse()
    if (annParser.errors.size > 0) throw new Exception(annParser.errors.toString)
                                                      (annParser.getNodeAnnotations, annParser.getTypeAnnotations)
  }

  private def parseEcc(fileName: String): (Iterable[Node], EccFile) = {
    import collection.JavaConversions._
    val eccLexer = new EccFullLexer(new FileReader(fileName))
    val eccParser = new EccFullParser(eccLexer)
    eccParser.parse()
    if (eccParser.errors.size > 0) throw new Exception(eccParser.errors.toString)
    val translatedNodes = NodeHelper.EccNodes2Nodes(eccParser.allNodes())
                                                   (translatedNodes, eccParser.getEccFile())
  }

  private def convertNodes(ecc: TypedEcc, replacedSemanticVars: Boolean = false): ImlConstraints =
    NodeHelper.typeCorrectNodes2Constraints(ecc.allNodes, ecc.types, replacedSemanticVars)

  private def convertConfig(config: Map[String, ConfigValue],
                            ecc: TypedEcc,
                            imlConstratints: ImlConstraints): (Map[String, Literal], Map[String, Literal]) =
    NodeHelper.values2configuration(config, ecc.allNodes, ecc.types, imlConstratints.defaults, imlConstratints.semanticVars)

  private def checkConflictsWithIndex() {
    val satisfiedConstraints = mutable.Set[Int]()
    val unsatisfiedConstraints = mutable.Set[Int]()
    for (i <- constraintRange) {
      val result = ExpressionHelper.evaluateTypeCorrectExpression(allConditions(i).getConstraint, getValuation)
      assert(result.isInstanceOf[BoolLiteral])
      if (result == BoolLiteral(true))
        satisfiedConstraints += i
      else
        unsatisfiedConstraints += i
    }
    this.satisfiedConstraints = satisfiedConstraints
    this.unsatisfiedConstraints = unsatisfiedConstraints
    satisficationNeedsUpdate = false
  }
}

