package ca.uwaterloo.gsd.rangeFix
import java.io._
import collection._
import ExpressionHelper._
import Expression._

import scala.collection.mutable.ArrayBuffer

class ErrorChecker(constraints:IndexedSeq[Expression], 
                   valuation:Map[String, Literal]) {

  def getSatisfiedConstraintIndexes():Iterable[Int] = {
    if (needsUpdate) update() 
    satisfiedConstraintIndexes
  }
  def getUnsatisfiedConstraintIndexes():Iterable[Int] = {
    if (needsUpdate) update()
    unsatisfiedConstraintIndexes
  }
  def changeValuation(id:String, value:Literal) {
    curValuation = curValuation + (id -> value)
    needsUpdate = true
  }

  private var needsUpdate:Boolean = true
  private var satisfiedConstraintIndexes:Iterable[Int] = List()
  private var unsatisfiedConstraintIndexes:Iterable[Int] = List()
  private var curValuation = valuation
  private def update() {
    val satisfiedConstraints = mutable.Set[Int]()
    val unsatisfiedConstraints = mutable.Set[Int]()
    for (i <- 0 until constraints.size) {
      val result = ExpressionHelper.evaluateTypeCorrectExpression(
        constraints(i), curValuation)
      assert(result.isInstanceOf[BoolLiteral])
      if (result == BoolLiteral(true))
        satisfiedConstraints += i
      else
        unsatisfiedConstraints += i
    }
    satisfiedConstraintIndexes = satisfiedConstraints
    unsatisfiedConstraintIndexes = unsatisfiedConstraints
    needsUpdate = false
  }

}

case class FixGenResult(
  fixes:Iterable[DataFix],
  milliseconds:Long
)

class MultiConstraintFixer(allConstraints:IndexedSeq[Expression],
                           reqConstraintSize:Int,
                           valuation:Map[String, Literal],
                           executionTime:Int = 1) {
  val errorChecker = new ErrorChecker(allConstraints.slice(0, reqConstraintSize), valuation)
  val fg = FixGenerator.create(allConstraints, configuration2Types(valuation), valuation)
  
  def fixWithIgnorance(constraintIndex:Int):FixGenResult = fixIgnoranceImpl(constraintIndex, fg)
  def fixWithElimination(constraintIndex:Int):FixGenResult = fixEliminationImpl(constraintIndex, fg)
  def fixWithPropagation(constraintIndex:Int):FixGenResult = fixPropagationImpl(Set(constraintIndex), fg)

  private def fixImpl(f: =>Iterable[DataFix]):FixGenResult = {
    val result = Timer.measureTime(executionTime) {
      f
    }
    FixGenResult(result, Timer.lastExecutionMillis)
  }

  private def fixIgnoranceImpl(cIndex:Int, fg:FixGenerator):FixGenResult = fixImpl {
    fg.fixWithIgnorance(cIndex)
  }

  private def fixEliminationImpl(cIndex:Int, fg:FixGenerator):FixGenResult = fixImpl {
    fg.fixWithElimination(cIndex, errorChecker.getSatisfiedConstraintIndexes.toSet)
  }

  private def fixPropagationImpl(cIndex: Set[Int], fg:FixGenerator):FixGenResult = fixImpl {
    val satisfiedConstraintIndexes = Timer.measureTime(errorChecker.getSatisfiedConstraintIndexes.toSet)
    fg.fix(cIndex, satisfiedConstraintIndexes, Set[String]())
  }

  def addEqualConstraint(l: Literal, exprIndex: Int) = {

  }

  // fix a violated new equal constraint
  def fixEqIgnorance(features: ArrayBuffer[(Literal, Int)]): FixGenResult = {
    var nfg: FixGenerator = fg
    var cIndex: Int = 0
    for ((l, exprIndex) <- features) {
      val tuple: (FixGenerator, Int) = FixGenerator.addEqualConstraint(fg, l, exprIndex)
      nfg = tuple._1
      cIndex = tuple._2
    }
    fixIgnoranceImpl(cIndex, nfg)
  }
  def fixEqElimination(features: ArrayBuffer[(Literal, Int)]): FixGenResult = {
    var nfg: FixGenerator = fg
    var cIndex: Int = 0
    for ((l, exprIndex) <- features) {
      val tuple: (FixGenerator, Int) = FixGenerator.addEqualConstraint(fg, l, exprIndex)
      nfg = tuple._1
      cIndex = tuple._2
    }
    fixEliminationImpl(cIndex, nfg)
  }
  def fixEqPropagation(features: ArrayBuffer[(Literal, Int)]): FixGenResult = {
    var nfg: FixGenerator = fg
    var cIndex: Set[Int] = Set()
    for ((l, exprIndex) <- features) {
      val tuple: (FixGenerator, Int) = FixGenerator.addEqualConstraint(nfg, l, exprIndex)
      nfg = tuple._1
      cIndex += tuple._2
    }
    fixPropagationImpl(cIndex, nfg)
  }
}

class SerializedEccManager(serializedArray: Array[Byte]) extends Serializable {
  def this(eccManager:EccManager) = this{
    val byteArrayStream = new ByteArrayOutputStream
    val output = new ObjectOutputStream(byteArrayStream)
    try {
      eccManager.saveExcutionTimesAndLoaderPath(output)
      eccManager.saveLoaderConfig(output)
    } finally {
      output.close()
    }
    val result = byteArrayStream.toByteArray
    result
  }

  def get() = {
    val input = new ObjectInputStream(new ByteArrayInputStream(serializedArray))
    try {
      val et = input.readInt()
      val file = input.readObject.asInstanceOf[String]
      val annotation = input.readObject.asInstanceOf[String]
      val eccManager = new EccManager(new EccLoader(file,annotation),et)
      eccManager.loadLoaderConfig(input)
      eccManager
    } finally {
      input.close
    }
  }
}


class EccManager(loader:EccLoader, executionTime:Int=1)
extends ConfigManager(loader, executionTime) with Serializable {
  def activateFeature(id: String, strategy: Strategy = PropagationStrategy): FixGenResult = {
    if (getActiveConstraintIndex(id).isEmpty)
      throw new java.lang.IllegalArgumentException("feature cannot be found.")
    val index = getActiveConstraintIndex(id).get
    strategy.fix(index)
  }
  def getConstraintSize =  loader.reqConstraintSize
  def getFeatureSize = loader.getFeatureSize
  def getActiveConstraintIndex(id:String):Option[Int] =
    loader.getActiveConstraintIndex(id)
  def isNodeActive(id:String) = {
    val optIndex = getActiveConstraintIndex(id)
    if (optIndex.isEmpty) throw new Exception(id + " not found.")
    ExpressionHelper.evaluateTypeCorrectExpression(
      loader.allExpressions(
        getActiveConstraintIndex(id).get) getConstraint,
      loader.valuation) == BoolLiteral(true)
    }
  def save(file:String){
    val oos = new ObjectOutputStream(new FileOutputStream(file))
    try {
      oos.writeObject(this)
    }finally{oos.close()}
  }
  def saveExcutionTimesAndLoaderPath(out:ObjectOutputStream){
    out.writeInt(executionTime)
    loader.savePath(out)
  }
  def saveLoaderConfig(out:ObjectOutputStream){
    loader.saveConfig(out)
  }
  def loadLoaderConfig(in:ObjectInputStream){
    loader.loadConfig(in)
    _fixerShouldUpdate = true
  }
  def convertSingleOptionValueToValuation(id: String, value: OptionValue): Iterable[(String, Literal)] =
    loader.convertOptionValue(value, id) match {
      case SingleConfigValue(v) =>
        if (getFeatureFlavor(id) == Flavor.Bool)
          List((NodeHelper.toBoolVar(id), v))
        else {
          assert(getFeatureFlavor(id) == Flavor.Data)
          List((NodeHelper.toDataVar(id), v))
        }
      case DoubleConfigValue(b, v) => List((NodeHelper.toBoolVar(id), b), (NodeHelper.toDataVar(id), v))
    }
  def getFeatureFlavor(id:String) = loader.getFeatureFlavor(id)
  def getCloneEccManager():EccManager={
    val byteArrayOut = new ByteArrayOutputStream
    val objectOut = new ObjectOutputStream(byteArrayOut)
    try {
      loader.testSerializable()
      objectOut.writeObject(this)
    }
    finally {objectOut.close()}
    val objectIn = new ObjectInputStream(new ByteArrayInputStream(byteArrayOut.toByteArray()))
    try {
      objectIn.readObject.asInstanceOf[EccManager]
    }finally{
      objectIn.close()
    }
  }
  def getValuation=loader.valuation
  def changeFeature(id:String,value:OptionValue){
    loader.changeFeature(id,value)
    _fixerShouldUpdate = true
  }
}

class KconfigManager(loader:KconfigLoader, executionTime:Int=1) extends ConfigManager(loader, executionTime) {
  private val features: ArrayBuffer[(Literal, Int)] = ArrayBuffer[(Literal, Int)]()

  def setFeature(id:String, l:Literal) = {
    val optId = loader.getEffectiveIndex(id)
    if (optId.isEmpty)
      throw new java.lang.IllegalArgumentException("feature cannot be found.")
    features += ((l, optId.get))
  }

  def getFixes(strategy:Strategy = PropagationStrategy): FixGenResult = {
    strategy.fixEq(features)
  }
}

class ConfigManager(loader:ModelLoader, executionTime:Int=1){

  protected abstract class Strategy {
    def fix(index:Int):FixGenResult
    def fixEq(features: ArrayBuffer[(Literal, Int)]):FixGenResult
  }

  object PropagationStrategy
  extends Strategy {
    override def fix(index:Int) = {
      fixer.fixWithPropagation(index)
    }
    override def fixEq(features: ArrayBuffer[(Literal, Int)]):FixGenResult = {
      fixer.fixEqPropagation(features)
    }
  }
  object EliminationStrategy
  extends Strategy {
    override def fix(index:Int) = {
      fixer.fixWithElimination(index)
    }
    override def fixEq(features: ArrayBuffer[(Literal, Int)]):FixGenResult = {
      fixer.fixEqElimination(features)
    }
  }
  object IgnoranceStrategy
  extends Strategy {
    override def fix(index:Int) = {
      fixer.fixWithIgnorance(index)
    }
    override def fixEq(features: ArrayBuffer[(Literal, Int)]):FixGenResult = {
      fixer.fixEqElimination(features)
    }
  }
  var _fixerShouldUpdate:Boolean = true
  var _fixer:MultiConstraintFixer = null
  private def fixer():MultiConstraintFixer={
    if (_fixerShouldUpdate){
      val allConstraintsNoSource = loader.allExpressions.map(_.getConstraint)
      _fixer = new MultiConstraintFixer(
        allConstraintsNoSource,
        loader.reqConstraintSize,
        loader.valuation,
        executionTime)
    }
    _fixerShouldUpdate=false
    _fixer
  }
  def getSatisfiedConstraintIndexes = fixer.errorChecker.getSatisfiedConstraintIndexes
  def getUnsatisfiedConstraintIndexes =
    fixer.errorChecker.getUnsatisfiedConstraintIndexes
  def getConstraint(index:Int):ConstraintWithSource = loader.allExpressions(index)

  def generateFix(index:Int, strategy:Strategy=PropagationStrategy) =
    strategy.fix(index)
  
}

trait ModelLoader {
  def allExpressions:IndexedSeq[ConstraintWithSource]
  def reqConstraintSize:Int
  def valuation:Map[String, Literal]
}

abstract class ConstraintWithSource(constraint:Expression, source:Source) {
  def getConstraint():Expression = constraint
  def getSource():String = source.getSource
  def getSourceObject() = source
  def getNodeID():String = source.nodeID
  override def toString():String = getSource
}

abstract class Source(val nodeID:String) {
  def getSource():String
}

class KconfigEffectiveSource(nodeID:String) extends Source(nodeID) {
  override def getSource():String = "Effective(%s)".format(nodeID)
}
object KconfigChoiceSource extends Source("choice") {
  override def getSource():String = "Choice"
}
class KconfigDomainSource(nodeID:String) extends Source(nodeID) {
  override def getSource():String = "Domain(%s)".format(nodeID)
}

case class EffectiveExpr(e:Expression, id:String)
     extends ConstraintWithSource(e, new KconfigEffectiveSource(id))
case class ChoiceConstraint(c:Expression)
     extends ConstraintWithSource(c, KconfigChoiceSource)
case class DomainConstraint(c:Expression, id:String)
     extends ConstraintWithSource(c, new KconfigDomainSource(id))


