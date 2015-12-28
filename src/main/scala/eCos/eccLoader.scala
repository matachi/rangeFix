package ca.uwaterloo.gsd.rangeFix
import java.io._
import collection._
import ExpressionHelper._
import Expression._

class EccLoader(eccPath:String, val globalAnnotationPath: String = "testfiles/realworld/allModels.annotation") extends ModelLoader with Serializable{

  private case class TypedEcc(allNodes: Map[String, Node],
                              types: Map[String, SingleType],
                              config: Map[String, ConfigValue],
                              convertOptionValue: (OptionValue, String) => ConfigValue){
  }

  private def parseEcc(fileName: String): (Iterable[Node], Map[String, OptionValue]) = {
    import collection.JavaConversions._
    val eccLexer = new EccFullLexer(new FileReader(fileName))
    val eccParser = new EccFullParser(eccLexer)
    eccParser.parse()
    if (eccParser.errors.size > 0) throw new Exception(eccParser.errors.toString)
    val translatedNodes = NodeHelper.EccNodes2Nodes(eccParser.allNodes())
    return (translatedNodes, eccParser.getEccFile().values.toMap)
  }
  private def parseAnnotations() = {
    val annLexer = new AnnotationLexer(new FileReader(globalAnnotationPath))
    val annParser = new AnnotationParser(annLexer)
    annParser.parse()
    if (annParser.errors.size > 0) throw new Exception(annParser.errors.toString)
    (annParser.getNodeAnnotations, annParser.getTypeAnnotations)
  }

  private def loadEccFileAndInferTypes(file: String): TypedEcc = {
    // load an ecc file
    val (orgNodes, orgValues) = parseEcc(file)
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
  
  private def convertNodes(ecc: TypedEcc, replacedSemanticVars: Boolean = false): ImlConstraints =
    NodeHelper.typeCorrectNodes2Constraints(ecc.allNodes, ecc.types, replacedSemanticVars)

  private def convertConfig(ecc: TypedEcc,
                            imlConstratints: ImlConstraints):
  (Map[String, Literal], Map[String, Literal]) =
    NodeHelper.values2configuration(ecc.config, ecc.allNodes, ecc.types, imlConstratints.defaults, imlConstratints.semanticVars)
  private val (orderedIds,origTypedEcc, imlCons, allConditions, reqConstraintNumber:Int, getActiveConditionIndex, config, featureSize) = {
    val ecc: TypedEcc = loadEccFileAndInferTypes(eccPath)
    val imlConstratints: ImlConstraints = convertNodes(ecc, true)
    val allConstraints = Vector[ConstraintWithSource]() ++ imlConstratints.allConstraints.filter(_.getConstraint != BoolLiteral(true))
    val orderedIds = Vector() ++ ecc.allNodes.keySet
    val activeConditionMap = (for (i <- 0 until orderedIds.size) yield (orderedIds(i), i + allConstraints.size)).toMap
      (orderedIds,
      ecc,
       imlConstratints,
      allConstraints ++ orderedIds.map(id => ActiveIfCondition(imlConstratints.activeConditions(id), id)).asInstanceOf[IndexedSeq[ConstraintWithSource]],
      allConstraints.size,
      activeConditionMap.get _,
      convertConfig(ecc, imlConstratints)._1,
      ecc.allNodes.size
    )
    }
  
  private var _valuation = config
  private var _typeEcc:TypedEcc = origTypedEcc
  def getFeatureSize = featureSize
  override def allExpressions:IndexedSeq[ConstraintWithSource] = allConditions
  override def reqConstraintSize:Int = reqConstraintNumber
  override def valuation:Map[String, Literal] = _valuation
  private lazy val allConditionMap = (for(i<-0 until orderedIds.size)yield(orderedIds(i),i+reqConstraintNumber)).toMap
  def getActiveConstraintIndex(nodeID:String):Option[Int] = if (allConditionMap contains nodeID)
                                                              Some(allConditionMap(nodeID)) else None
  def changeFeature(id:String, value:OptionValue){
    val cValue = _typeEcc.convertOptionValue(value,id)
    _typeEcc = TypedEcc(_typeEcc.allNodes,_typeEcc.types,_typeEcc.config+(id->cValue),_typeEcc.convertOptionValue)
    _valuation = convertConfig(_typeEcc, imlCons)._1
  }
  def getFeatureFlavor(id:String)=_typeEcc.allNodes(id).flavor
  def convertOptionValue = _typeEcc.convertOptionValue
  def testSerializable(){
    val byteOut = new ByteArrayOutputStream
    val objOut = new ObjectOutputStream(byteOut)
    try {
      objOut.writeObject(this)
    }finally{objOut.close}
  }
  @throws(classOf[IOException])
  private def writeObject(out: ObjectOutputStream): Unit = {
    out.writeObject(eccPath)
    out.writeObject(globalAnnotationPath)
  }
  @throws(classOf[IOException])
  private def readObject(input: ObjectInputStream): Unit = {
    new EccLoader(input.readObject.asInstanceOf[String], input.readObject.asInstanceOf[String])
  }
  def saveConfig(o: ObjectOutputStream) {
    o.writeInt(_typeEcc.config.size)
    for ((k, v) <- _typeEcc.config) {
      o.writeObject(k)
      v.save(o)
    }
  }
  def savePath(o:ObjectOutputStream){
    o.writeObject(eccPath)
    o.writeObject(globalAnnotationPath)
  }
  def loadConfig(i: ObjectInputStream){
    val size = i.readInt
    val cf = (for (index <- 1 to size) yield {
      val k = i.readObject.asInstanceOf[String]
      val v = ConfigValue.load(i)
                              (k, v)
    }).toMap
    _typeEcc = TypedEcc(_typeEcc.allNodes, _typeEcc.types, cf, _typeEcc.convertOptionValue)
    _valuation = convertConfig(_typeEcc, imlCons)._1
  }
}
