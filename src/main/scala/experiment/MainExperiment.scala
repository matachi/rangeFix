package ca.uwaterloo.gsd.rangeFix

import java.io._
import collection.mutable.ListBuffer
import org.kiama.rewriting.Rewriter._
import scala.collection._

abstract class Conflict(finalSerializedEccManager:Option[SerializedEccManager] = None) extends Serializable {
  protected var lastMilliSecond:Long = 0
  def solve(executionTimes:Int, strategy:Strategy):Iterable[DataFix]
  def getLastSolvingMilliSeconds() = lastMilliSecond
  def save(file:String) {
    val output = new ObjectOutputStream(new FileOutputStream(file))
    try {
      output.writeObject(this)
    }
    finally {
      output.close()
    }
  }
  lazy val finalEccManager = finalSerializedEccManager.map(_.get)
  def isConflictSolvedByUser:Option[Boolean]
  def getUserValuation =finalEccManager.map(_.getValuation)
}

object Conflict extends Serializable {
  def load(file:String):Conflict = {
    val input = new ObjectInputStream(new FileInputStream(file))
    try {
      input.readObject.asInstanceOf[Conflict]
    }
    finally {
      input.close
    }		
  }
}


case class InactiveConflict(serializedEccManager:SerializedEccManager, id:String, finalSerializedEccManager:Option[SerializedEccManager] = None) extends Conflict(finalSerializedEccManager) {
  lazy val eccManager = serializedEccManager.get()
  override def solve(executionTimes:Int, strategy:Strategy):Iterable[DataFix] = {
    var str = strategy match{
      case IgnoranceStrategy => eccManager.IgnoranceStrategy
      case EliminationStrategy => eccManager.EliminationStrategy
      case PropagationStrategy => eccManager.PropagationStrategy
    }
    val rlt = eccManager.activateFeature(id, str)
    lastMilliSecond = rlt.milliseconds
    rlt.fixes
  }
  def saveEcc(file:String) = {
    val objOut = new ObjectOutputStream(new FileOutputStream(file))
    try{
      objOut.writeObject(serializedEccManager)
    }
    finally{
      objOut.close()
    }
  }
  def saveTo(output:ObjectOutputStream) = {
    output.writeObject(eccManager)
    output.writeObject(id)
  }
  override def isConflictSolvedByUser = Some(finalEccManager.get.isNodeActive(id))
}

case class RequiresConflict(serializedEccManager:SerializedEccManager, violatedIndex:Int, finalSerilizedEccManager:Option[SerializedEccManager] = None) 
     extends Conflict(finalSerilizedEccManager) {
  lazy val eccManager = serializedEccManager.get()
  override def solve(executionTimes:Int, strategy:Strategy):Iterable[DataFix] = {
    var str = strategy match {
      case EliminationStrategy => eccManager.EliminationStrategy
      case IgnoranceStrategy => eccManager.IgnoranceStrategy
      case PropagationStrategy=> eccManager.PropagationStrategy
    }
    val rlt = str.fix(violatedIndex)
    lastMilliSecond = rlt.milliseconds
    rlt.fixes
  }
  def saveEcc(file:String) = {
    val objOut = new ObjectOutputStream(new FileOutputStream(file))
    try{
      objOut.writeObject(serializedEccManager)
    }
    finally{
      objOut.close()
    }
  }
  def saveTo(output:ObjectOutputStream) = {
    output.writeObject(eccManager)
    output.writeInt(violatedIndex)
  }
  override def isConflictSolvedByUser = {
    finalEccManager.map(!_.getUnsatisfiedConstraintIndexes.toSet.contains(violatedIndex))
  }
  def getEccManager = eccManager
}


object MainExperiment {
  val executionTimes = 100
  
  def main(args:Array[String]) {
    if (args.size > 0 && args(0) == "-c") {
      produceSimulatedConflicts("experiment/simulation", "experiment/conflicts/simulation/")
      produceDefaultConflicts("experiment/defaults/", "experiment/conflicts/defaults/", "experiment/default-conflict-list-withoutduplicate.txt")
    }
    else if (args.size > 0 && args(0) == "-ct") {
      checkConfigToolCoverage
    }
    else {
      def singleSolveConfilict(newAl:Boolean, divideUnit:Boolean){
        SMTFixGenerator.useAlgorithm = newAl
        SMTFixGenerator.divideUnits = divideUnit
        val baseURL = "experiment/result/" + {if (newAl) "newAlgorithm_" else "oldAlgorithm_"} + {if(divideUnit) "divided/" else "nondivided/"}
        solveConflicts("experiment/conflicts/", baseURL+"propagation/", PropagationStrategy)
        solveConflicts("experiment/conflicts/", baseURL+"elimination/", EliminationStrategy)
        solveConflicts("experiment/conflicts/", baseURL+"ignorance/", IgnoranceStrategy) 
      }
      singleSolveConfilict(true, true)
      singleSolveConfilict(true, false)
      singleSolveConfilict(false, true)
    }
  }

  def createFolder(inputFolder: String): File = {
    var folder = new File(inputFolder)
    if(!folder.exists)
      folder.mkdirs
    folder
  }

  def createFile(inputFile: String): FileWriter = {
    var file = new File(inputFile)
    if(file.exists) {
      file.delete
    } else {
      file.createNewFile
    }
    var fstream = new FileWriter(file,true)
    fstream
  }

  def writeLine(fstream: FileWriter, line: String) {
    try{
      fstream.write(line+"\r\n");
    } catch {
      case e: Exception => println("Error: " + e.getMessage());
    }
  }
  
  def produceSimulatedConflicts(inputFolder:String, outputFolder:String) {
    var eccpath = ""
    var changesArray: List[Array[Change]] = null
    createFolder(outputFolder)
    for (dir <- new File(inputFolder).listFiles) {
      println("\n\n[INFO] Processing "+dir.getAbsolutePath)
      if(!dir.getAbsolutePath.endsWith(".svn")) {
	createFolder(outputFolder+dir.getName)
	// load the test files
	for (file <- new File(dir.getAbsolutePath).listFiles) {
	  if (file.getAbsolutePath.endsWith(".ecc")) {
	    // load the ecc file
	    println("\n\n[INFO] Loading ecc: "+file.getAbsolutePath)
	    eccpath = file.getAbsolutePath
	  } 
	  else if (file.getAbsolutePath.endsWith(".dec")) {
	    // load the decision (change) file
	    println("\n\n[INFO] Loading decisions: "+file.getAbsolutePath)
	    var ecosHandler = new ECOSChangeHandler
	    changesArray = ecosHandler.ECOSChangeParser.getChangesFromFile(file.getAbsolutePath)
	  } // else unknown file type
	}			
	var summaryFW = createFile(outputFolder+dir.getName+File.separatorChar+"summary.txt")
	writeLine(summaryFW,"============================================================================")
	writeLine(summaryFW,"File: "+dir.getName)
	writeLine(summaryFW,"Number of user changes: "+changesArray(0).length)
	writeLine(summaryFW,"")
	writeLine(summaryFW,"Legend: ")
	writeLine(summaryFW,"- [!]: cannot be activated")
	writeLine(summaryFW,"- [A]: activates")
	writeLine(summaryFW,"- [C]: conflicts")
	writeLine(summaryFW,"- [F]: fixes")
	writeLine(summaryFW,"============================================================================")
	var order = 0
	var orderLabel = ""
	val identifiedErrors = mutable.Set[Int]()
	val identifiedInactives = mutable.Set[String]()
	for (ca <- changesArray) {
	  order match {
	    case 0 => writeLine(summaryFW,"\r\nTop-down order") ; orderLabel = "top-down"
	    case 1 => writeLine(summaryFW,"\r\nBottom-up order") ; orderLabel = "bottom-up"
	    case 2 => writeLine(summaryFW,"\r\nRandom order") ; orderLabel = "random"
	  }
	  writeLine(summaryFW,"---------------------------------------------------------")
	  println(orderLabel+" ["+ca.length+"]")
	  var eccManager  = new EccManager(new EccLoader(eccpath),executionTimes)
	  writeLine(summaryFW,"Number of constriants:" + eccManager.getConstraintSize)
	  writeLine(summaryFW,"Number of features:" + eccManager.getFeatureSize)
	  writeLine(summaryFW,"Number of changes:" + ca.length)
	  var res = processChanges(
	    eccManager,
	    ca,
	    summaryFW,
	    outputFolder+dir.getName+File.separatorChar,
	    orderLabel,
	    identifiedErrors,
	    identifiedInactives,
	    outputFolder + dir.getName)
	  order = order + 1
	}
	closeFile(summaryFW)
      }
    }
  }
  def closeFile(fstream: FileWriter) {
    fstream.close
  }
  def processChanges(eccManager:EccManager,
		     changes: Array[Change], 
		     summaryFW: FileWriter,
                     outputFolder: String, 
		     orderLabel: String,
		     identifiedErrors: mutable.Set[Int],
		     identifiedInactives: mutable.Set[String],
		     outputDir:String) {
    
    type InactiveFeatureList = ListBuffer[(Change, SerializedEccManager)]

    var inactiveFeatureList : InactiveFeatureList = new ListBuffer() //(feature_name,status) The status is
    // false if the feature has been
    // activated later on
    var conflictMap = Map[Int,String]() //(index,constraint)
    var fixesList: List[Iterable[DataFix]] = Nil
    var conflictCombination: List[Int] = Nil
    var fixLog = ""
    var activeLog = ""
    
    val finalSerializedEccManager = {
      val em = new SerializedEccManager(eccManager).get
      for (c <- changes) {
	em.changeFeature(c.feature, c.getOptionValue)
      }
      new SerializedEccManager(em)
    }

    writeLine(summaryFW,"Execution log:")
    for (index <- 0 until changes.size) {
      val c = changes(index)

      if(!eccManager.isNodeActive(c.feature)) {
        //        println("\t[!]"+c.feature)
        writeLine(summaryFW,"\t[!]"+c.feature + "\t" + c.getOptionValue)
        //        nInactive = nInactive + 1
        inactiveFeatureList.append((c, new SerializedEccManager(eccManager)))
      } else {
        writeLine(summaryFW,"\t"+c.feature + "\t" + c.getOptionValue)
        // println("\t"+c.feature)
        eccManager.changeFeature(c.feature, c.getOptionValue)
      }
      
      def filterOnce(list:InactiveFeatureList) = list.filter(ift =>
        if(eccManager.isNodeActive(ift._1.feature)) {
          writeLine(summaryFW,"\t\t [A]"+ift._1.feature)
          eccManager.changeFeature(ift._1.feature, ift._1.getOptionValue)
	  if (!identifiedInactives.contains(ift._1.feature)) {
	    val conflict = new InactiveConflict(ift._2, ift._1.feature, Some(finalSerializedEccManager))
	    val fileNameWithoutExtension = outputDir + "\\inactive-" + ift._1.feature
	    conflict.save(fileNameWithoutExtension + ".conflict")
	    conflict.saveEcc(fileNameWithoutExtension + ".ecc")
	    identifiedInactives += ift._1.feature
	    writeLine(summaryFW, "\t\t\t recorded as a conflict")
	  }
	  else {
	    writeLine(summaryFW, "\t\t\t already identified")
	  }
          false
        }
	else true
      )
      
      def filter(list:InactiveFeatureList):InactiveFeatureList = {
	val result = filterOnce(list)
	if (result == list) result
	else filter(result)
      }

      inactiveFeatureList = filter(inactiveFeatureList) 
      
      //lazy val serializedEcc = new SerializedEccManipulator(eccManipulator)
      var unsatisfiedConstraints = eccManager.getUnsatisfiedConstraintIndexes
      for (sci <- unsatisfiedConstraints) {
        writeLine(summaryFW,"\t\t [C] ("+sci+") "+ eccManager.getConstraint(sci).getSource())
        if(!identifiedErrors.contains(sci)) {
          identifiedErrors += sci 
	  val fileNameWithoutExtension = outputDir + "\\error-" + sci
	  eccManager.save(fileNameWithoutExtension + ".ecc")
	  new RequiresConflict(new SerializedEccManager(eccManager), sci, Some(finalSerializedEccManager)).save(fileNameWithoutExtension + ".conflict")
	  writeLine(summaryFW,"\t\t\t recorded as a conflict")
	}
	else {
	  writeLine(summaryFW,"\t\t\t already identified")
	}
      }
    }
  }


  
  def produceDefaultConflicts(inputFolder:String, outputFolder:String, filterFile:String) {
    createFolder(outputFolder)
    val filterReader = new BufferedReader(new FileReader(filterFile))
    val includedConflicts = mutable.Set[String]()
    var line = filterReader.readLine()
    while(line != null) {
      includedConflicts += line
      line = filterReader.readLine()
    }
    
    
    for (file <- new File(inputFolder).listFiles
	 if file.getName.endsWith(".ecc")) {
      
      val outputDir = outputFolder + file.getName + "/"
      val log = new LogPrinter(outputDir + "summary.log")
      log.println("Processing " + file.getAbsolutePath)
      
      val eccManager = new EccManager(new EccLoader(file.getAbsolutePath))
      log.println("Number of Features: " + eccManager.getFeatureSize)
      log.println("Number of Constraints: " + eccManager.getConstraintSize)
      
      //lazy val serializedEcc = new SerializedEccManipulator(eccManipulator)
      var unsatisfiedConstraints = eccManager.getUnsatisfiedConstraintIndexes
      for (sci <- unsatisfiedConstraints) {
	log.println("\t [C] ("+sci+") " + eccManager.getConstraint(sci).getSource())
	if (includedConflicts.contains(file.getName + "\\error-" + sci)) {
	  log.println("\t\tRecorded")
	  val fileNameWithoutExtension = outputDir + "\\error-" + sci
	  new RequiresConflict(new SerializedEccManager(eccManager), sci).save(fileNameWithoutExtension + ".conflict")
	}
	else {
	  log.println("\t\tFiltered out")
	}
      }
      log.println("Number of Conflicts: " + unsatisfiedConstraints.size)
      log.close
    }
  }

  def checkConfigToolCoverage() {
    import scala.xml._
    val root = XML.loadFile("experiment/conflict-results-cleaned-xml-ready.xml")
    // val root = XML.loadFile("experiment/test.xml")
    val relativeRoot = "experiment/conflicts/"
    
    val writer = new BufferedWriter(new FileWriter("experiment/result/configToolCoverage.csv"))
    writer.write("fileName, is covered\n")
    
    for (conflict <- root \ "conflict") {
      val fileID = conflict \ "file" text
      val file = relativeRoot + fileID + ".conflict"
      println("processing " + file)
      val conflictFile = Conflict.load(file)
      writer.write(fileID + ", ")
      assert(conflictFile.isConflictSolvedByUser.isDefined)
      if (conflictFile.isConflictSolvedByUser == Some(true)) {
	assert(conflictFile.getUserValuation.isDefined)
	val valuation = conflictFile.getUserValuation.get
	val fixPairs = for (change <- conflict \ "fix" \ "change") yield {
	  val feature:String = change \ "feature" text
	  val value:String = change \ "value" text
	  val optionValue:SingleOptionValue = 
	    if (value.startsWith("\"")) StringOptionValue(value.substring(1, value.length - 1)) //"
	    else IntOptionValue(value.toInt)
	  assert (conflictFile.finalEccManager.isDefined)
	  val eccManager = conflictFile.finalEccManager.get
	  eccManager.getFeatureFlavor(feature) match {
	    case Flavor.Bool =>
	      eccManager.convertSingleOptionValueToValuation(feature, optionValue).head
	    case Flavor.Data =>
	      val result = eccManager.convertSingleOptionValueToValuation(feature, optionValue).head
	      result
	    case Flavor.BoolData =>
	      if (value == 0 || value == 1) {
		assert(optionValue.isInstanceOf[IntOptionValue])
		eccManager.convertSingleOptionValueToValuation(
		  feature, 
		  DoubleOptionValue(optionValue.asInstanceOf[IntOptionValue], 
				    IntOptionValue(0))
		).head
	      }
	      else {
		eccManager.convertSingleOptionValueToValuation(feature, 
							DoubleOptionValue(IntOptionValue(0), optionValue)).tail.head
	      }
	  }
	}
	val fixExpr = fixPairs.map(pair => IdentifierRef(pair._1) === pair._2).reduce[Expression](_ & _)
	if (ExpressionHelper.evaluateTypeCorrectExpression(fixExpr, valuation) == BoolLiteral(true)) {
	  writer.write("true\n")
	}
	else {
	  writer.write("false\n")
	}
	
      }
      else writer.write("not applicable\n")
    }
    writer.close()
  }

  def solveConflicts(inputFolder:String, outputFolder:String, strategy:Strategy) {
    createFolder(outputFolder)
    val log = new LogPrinter(outputFolder + "execution.log")
    val conflictWriter = new BufferedWriter(new FileWriter(outputFolder + "conflicts.csv"))
    conflictWriter.write("conflictID, Number Of Fixes, Number of all variables, Generation Time (ms), Cover User Change, Bring New Error\n")
    val fixWriter = new BufferedWriter(new FileWriter(outputFolder + "fixes.csv"))
    fixWriter.write("fixID, conflictID, Number of variables, Number of fix units, Cover User Change, Bring new conflict\n")
    val unitWriter = new BufferedWriter(new FileWriter(outputFolder + "units.csv"))
    unitWriter.write("unitID, fixID, conflictID, unitType, Number of variables, Number of operators\n")
    
    for {dir <- new File(inputFolder).listFiles 
	 if !dir.getAbsolutePath.endsWith(".svn")
	 fileFolder <- dir.listFiles
	 conflictFile <- fileFolder.listFiles
	 if (conflictFile.getName.endsWith(".conflict"))}{
      
      log.println("processing " + dir.getName + "\\" + fileFolder + "\\" + conflictFile.getName + "...")
      
      val conflict = Conflict.load(conflictFile.getAbsolutePath)
      val conflictID = dir.getName + "-" + fileFolder.getName + "-" + conflictFile.getName
      val fixes = conflict.solve(executionTimes, strategy).toIndexedSeq
      val optRefFixes = 
	if (strategy == IgnoranceStrategy) Some(conflict.solve(1, EliminationStrategy).toSet)
	else None
      
      conflictWriter.write(conflictID + ", " + fixes.size + ", " + fixes.map(_.variables.size).sum + ", " + conflict.getLastSolvingMilliSeconds + ", ")
      
      fixes.foreach(f => log.println("\t" + f))
      def CoverChange2String(cover:Int) = cover match {
	case 1 => "true"
	case 0 => "not applicable"
	case -1 => "false"
      }
      val isConflictSolvedByUser = conflict.isConflictSolvedByUser
      var bringNewError = false
      val coverChanges = 
	if (fixes.size == 0) {
	  if (isConflictSolvedByUser == Some(true)) -1
	  else 0
	}
      else (for( fixID <- 0 until fixes.size ) yield {
	val singleCoverChange = 
	  if (isConflictSolvedByUser == Some(true)) 
	    if (ExpressionHelper.evaluateTypeCorrectExpression(fixes(fixID).constraint, 
							       conflict.getUserValuation.get) == BoolLiteral(true)) 
	      1
	    else -1
	    else 0
	fixWriter.write(fixID + ", " + conflictID + ", " + fixes(fixID).variables.size + ", " + fixes(fixID).units.size + ", " + CoverChange2String(singleCoverChange) + ",")
	
	if (strategy != IgnoranceStrategy) fixWriter.write("false\n")
	else {
	  val fixBringNewError = !(optRefFixes.get contains fixes(fixID))
	  fixWriter.write(fixBringNewError.toString + "\n")
	  bringNewError = bringNewError || fixBringNewError
	}
	
	val units = fixes(fixID).units.toIndexedSeq
	for (unitID <- 0 until units.size)
	unitWriter.write(unitID + ", " + fixID + ", " + conflictID + ", " + units(unitID).getClass.getName + ", " + units(unitID).variables.size + ", " + countOperators(units(unitID).constraint) + "\n")
	
	singleCoverChange
      }) reduce (java.lang.Math.max(_, _))
      conflictWriter.write(CoverChange2String(coverChanges) + "," + bringNewError + "\n")
    }
    conflictWriter.close
    fixWriter.close
    unitWriter.close
    log.close
  }
  def countOperators(c:Expression):Int = {
    var result = 0
    rewrite{everywhere{query[Any]{
      case _:BinaryExpression => result += 1
      case _:Conditional => result += 1
      case _:UnaryExpression => result += 1
      case _:FunctionCall => result += 1
    }}}(c)
    result
  }
}

class LogPrinter(logFile:String) {
 val logFileObj = new File(logFile)
 if (!logFileObj.getParentFile.exists) logFileObj.getParentFile.mkdirs
 val writer = new BufferedWriter(new FileWriter(logFileObj))
 def println(msg:String) {
 Predef.println(msg)
 writer.write(msg + "\n")
 }
 def close()  { writer.close() }
 }
/*class LogPrinter(logFile:String) {
 val logFileObj = new File(logFile)
 if (!logFileObj.getParentFile.exists) logFileObj.getParentFile.mkdirs
 val writer = new BufferedWriter(new FileWriter(logFileObj))
 def println(msg:String) {
 Predef.println(msg)
 writer.write(msg + "\n")
 }
 def close()  { writer.close() }
 }

abstract class Conflict(finalEcc:Option[SerializedEccManipulator] = None) extends Serializable {
protected var lastMilliSecond:Long = 0
def solve(executionTimes:Int, strategy:Strategy):Iterable[DataFix]
def getLastSolvingMilliSeconds() = lastMilliSecond
def save(file:String) {
val output = new ObjectOutputStream(new FileOutputStream(file))
try {
// this match {
// case r:RequiresConflict => output.writeInt(0);r.saveTo(output) 
// case i:InactiveConflict => output.writeInt(1);i.saveTo(output)
// }
// saveConfig(output)
output.writeObject(this)
}
finally {
output.close()
}
}
def isConflictSolvedByUser:Option[Boolean]
lazy val finalEccManipulator = finalEcc.map(_.get)
def getUserValuation = finalEccManipulator.map(_.getValuation)
// def saveConfig(o:ObjectOutputStream) {
// o.writeInt(finalConfig.size)
// for((k, v) <- finalConfig) {
// o.writeObject(k)
// o.writeObject(v)
// }
// }
}
object Conflict extends Serializable {
def load(file:String):Conflict = {
val input = new ObjectInputStream(new FileInputStream(file))
try {
// input.readInt match {
// case 0 => LoadReq(input)
// case 1 => LoadInactive(input)
// }
input.readObject.asInstanceOf[Conflict]
}
finally {
input.close
}		
}
// def loadConfig(i:ObjectInputStream):Map[String, Literal] = {
// val size = i.readInt
// (for(index <- 1 to size) yield {
// val k = i.readObject.asInstanceOf[String]
// val v = i.readObject.asInstanceOf[Literal]
// (k, v)
// }).toMap
// }
// def LoadReq(input:ObjectInputStream):RequiresConflict = {
// RequiresConflict(input.readObject.asInstanceOf[SerializedEccManipulator], 
// input.readInt, loadConfig(input))
// }

// def LoadInactive(input:ObjectInputStream):InactiveConflict = {
// InactiveConflict(input.readObject.asInstanceOf[SerializedEccManipulator], 
// input.readObject.asInstanceOf[String], loadConfig(input))
// }

}

case class RequiresConflict(ecc:SerializedEccManipulator, violatedIndex:Int, finalEcc:Option[SerializedEccManipulator] = None) 
extends Conflict(finalEcc) {
override def solve(executionTimes:Int, strategy:Strategy):Iterable[DataFix] = {
val (result, time) = ecc.get.generateFixWithTime(violatedIndex, executionTimes, strategy)
lastMilliSecond = time
result
}
def saveEcc(file:String) = {
ecc.get.save(file)
}
def saveTo(output:ObjectOutputStream) = {
output.writeObject(ecc)
output.writeInt(violatedIndex)
}
override def isConflictSolvedByUser = {
finalEccManipulator.map(!_.getUnsatisfiedConstraintIndexes.contains(violatedIndex))
}
def getEccManipulator = ecc.get
}

case class InactiveConflict(ecc:SerializedEccManipulator, id:String, finalEcc:Option[SerializedEccManipulator] = None) extends Conflict(finalEcc) {
override def solve(executionTimes:Int, strategy:Strategy):Iterable[DataFix] = {
val (result, time) = ecc.get.activateFeatureWithTime(id, executionTimes, strategy)
lastMilliSecond = time
result
}
def saveEcc(file:String) = {
ecc.get.save(file)
}
def saveTo(output:ObjectOutputStream) = {
output.writeObject(ecc)
output.writeObject(id)
}
override def isConflictSolvedByUser = finalEccManipulator.map(_.isFeatureActive(id))
}

object MainExperiment {
val executionTimes = 100

def main(args:Array[String]) {
if (args.size > 0 && args(0) == "-c") {
produceSimulatedConflicts("experiment/simulation", "experiment/conflicts/simulation/")
produceDefaultConflicts("experiment/defaults/", "experiment/conflicts/defaults/", "experiment/default-conflict-list-withoutduplicate.txt")
}
else if (args.size > 0 && args(0) == "-ct") {
checkConfigToolCoverage
}
else {
solveConflicts("experiment/conflicts/", "experiment/result/propagation/", PropagationStrategy)
solveConflicts("experiment/conflicts/", "experiment/result/elimination/", EliminationStrategy)
solveConflicts("experiment/conflicts/", "experiment/result/ignorance/", IgnoranceStrategy)
}
}

def checkConfigToolCoverage() {
import scala.xml._
val root = XML.loadFile("experiment/conflict-results-cleaned-xml-ready.xml")
// val root = XML.loadFile("experiment/test.xml")
val relativeRoot = "experiment/conflicts/"

val writer = new BufferedWriter(new FileWriter("experiment/result/configToolCoverage.csv"))
writer.write("fileName, is covered\n")

for (conflict <- root \ "conflict") {
val fileID = conflict \ "file" text
val file = relativeRoot + fileID + ".conflict"
println("processing " + file)
val conflictFile = Conflict.load(file)
writer.write(fileID + ", ")
assert(conflictFile.isConflictSolvedByUser.isDefined)
if (conflictFile.isConflictSolvedByUser == Some(true)) {
assert(conflictFile.getUserValuation.isDefined)
val valuation = conflictFile.getUserValuation.get
val fixPairs = for (change <- conflict \ "fix" \ "change") yield {
val feature:String = change \ "feature" text
val value:String = change \ "value" text
val optionValue:SingleOptionValue = 
if (value.startsWith("\"")) StringOptionValue(value.substring(1, value.length - 1)) //"
else IntOptionValue(value.toInt)
assert (conflictFile.finalEccManipulator.isDefined)
val ecc = conflictFile.finalEccManipulator.get
ecc.getFeatureFlavor(feature) match {
case Flavor.Bool =>
ecc.convertSingleOptionValueToValuation(feature, optionValue).head
case Flavor.Data =>
val result = ecc.convertSingleOptionValueToValuation(feature, optionValue).head
result
case Flavor.BoolData =>
if (value == 0 || value == 1) {
assert(optionValue.isInstanceOf[IntOptionValue])
ecc.convertSingleOptionValueToValuation(
feature, 
DoubleOptionValue(optionValue.asInstanceOf[IntOptionValue], 
IntOptionValue(0))
).head
}
else {
ecc.convertSingleOptionValueToValuation(feature, 
DoubleOptionValue(IntOptionValue(0), optionValue)).tail.head
}
}
}
val fixExpr = fixPairs.map(pair => IdentifierRef(pair._1) === pair._2).reduce[Expression](_ & _)
if (ExpressionHelper.evaluateTypeCorrectExpression(fixExpr, valuation) == BoolLiteral(true)) {
writer.write("true\n")
}
else {
writer.write("false\n")
}

}
else writer.write("not applicable\n")
}
writer.close()
}

def produceSimulatedConflicts(inputFolder:String, outputFolder:String) {
var eccpath = ""
var changesArray: List[Array[Change]] = null
createFolder(outputFolder)
for (dir <- new File(inputFolder).listFiles) {
println("\n\n[INFO] Processing "+dir.getAbsolutePath)
if(!dir.getAbsolutePath.endsWith(".svn")) {
createFolder(outputFolder+dir.getName)
// load the test files
for (file <- new File(dir.getAbsolutePath).listFiles) {
if (file.getAbsolutePath.endsWith(".ecc")) {
// load the ecc file
println("\n\n[INFO] Loading ecc: "+file.getAbsolutePath)
eccpath = file.getAbsolutePath
} 
else if (file.getAbsolutePath.endsWith(".dec")) {
// load the decision (change) file
println("\n\n[INFO] Loading decisions: "+file.getAbsolutePath)
var ecosHandler = new ECOSChangeHandler
changesArray = ecosHandler.ECOSChangeParser.getChangesFromFile(file.getAbsolutePath)
} // else unknown file type
}			


var summaryFW = createFile(outputFolder+dir.getName+File.separatorChar+"summary.txt")
writeLine(summaryFW,"============================================================================")
writeLine(summaryFW,"File: "+dir.getName)
writeLine(summaryFW,"Number of user changes: "+changesArray(0).length)
writeLine(summaryFW,"")
writeLine(summaryFW,"Legend: ")
writeLine(summaryFW,"- [!]: cannot be activated")
writeLine(summaryFW,"- [A]: activates")
writeLine(summaryFW,"- [C]: conflicts")
writeLine(summaryFW,"- [F]: fixes")
writeLine(summaryFW,"============================================================================")
var order = 0
var orderLabel = ""
val identifiedErrors = mutable.Set[Int]()
val identifiedInactives = mutable.Set[String]()
for (ca <- changesArray) {
order match {
case 0 => writeLine(summaryFW,"\r\nTop-down order") ; orderLabel = "top-down"
case 1 => writeLine(summaryFW,"\r\nBottom-up order") ; orderLabel = "bottom-up"
case 2 => writeLine(summaryFW,"\r\nRandom order") ; orderLabel = "random"
}
writeLine(summaryFW,"---------------------------------------------------------")
println(orderLabel+" ["+ca.length+"]")
var eccManipulator = new EccManipulator(eccpath)
writeLine(summaryFW,"Number of constriants:" + eccManipulator.getConstraintSize)
writeLine(summaryFW,"Number of features:" + eccManipulator.getFeatureSize)
writeLine(summaryFW,"Number of changes:" + ca.length)
var res = processChanges(
eccManipulator,
ca,
summaryFW,
outputFolder+dir.getName+File.separatorChar,
orderLabel,
identifiedErrors,
identifiedInactives,
outputFolder + dir.getName)
order = order + 1
}
closeFile(summaryFW)
}
}
}

def processChanges(eccManipulator: EccManipulator,
changes: Array[Change], 
summaryFW: FileWriter,
outputFolder: String, 
orderLabel: String,
identifiedErrors: mutable.Set[Int],
identifiedInactives: mutable.Set[String],
outputDir:String) {

type InactiveFeatureList = ListBuffer[(Change, SerializedEccManipulator)]

var inactiveFeatureList : InactiveFeatureList = new ListBuffer() //(feature_name,status) The status is
// false if the feature has been
// activated later on
var conflictMap = Map[Int,String]() //(index,constraint)
var fixesList: List[Iterable[DataFix]] = Nil
var conflictCombination: List[Int] = Nil
var fixLog = ""
var activeLog = ""

val finalEcc = {
val serialized = new SerializedEccManipulator(eccManipulator)
val cloneEcc = serialized.get
for (c <- changes) {
cloneEcc.changeFeature(c.feature, c.getOptionValue)
}
new SerializedEccManipulator(cloneEcc)
}

writeLine(summaryFW,"Execution log:")
for (index <- 0 until changes.size) {
val c = changes(index)

if(!eccManipulator.isFeatureActive(c.feature)) {
//        println("\t[!]"+c.feature)
writeLine(summaryFW,"\t[!]"+c.feature + "\t" + c.getOptionValue)
//        nInactive = nInactive + 1
inactiveFeatureList.append((c,new SerializedEccManipulator(eccManipulator)))
} else {
writeLine(summaryFW,"\t"+c.feature + "\t" + c.getOptionValue)
// println("\t"+c.feature)
eccManipulator.changeFeature(c.feature, c.getOptionValue)
}

def filterOnce(list:InactiveFeatureList) = list.filter(ift =>
if(eccManipulator.isFeatureActive(ift._1.feature)) {
writeLine(summaryFW,"\t\t [A]"+ift._1.feature)
eccManipulator.changeFeature(ift._1.feature, ift._1.getOptionValue)
if (!identifiedInactives.contains(ift._1.feature)) {
val conflict = new InactiveConflict(ift._2, ift._1.feature, Some(finalEcc))
val fileNameWithoutExtension = outputDir + "\\inactive-" + ift._1.feature
conflict.save(fileNameWithoutExtension + ".conflict")
conflict.saveEcc(fileNameWithoutExtension + ".ecc")
identifiedInactives += ift._1.feature
writeLine(summaryFW, "\t\t\t recorded as a conflict")
}
else {
writeLine(summaryFW, "\t\t\t already identified")
}
false
}
else true
)

def filter(list:InactiveFeatureList):InactiveFeatureList = {
val result = filterOnce(list)
if (result == list) result
else filter(result)
}

inactiveFeatureList = filter(inactiveFeatureList) 

lazy val serializedEcc = new SerializedEccManipulator(eccManipulator)
var unsatisfiedConstraints = eccManipulator.getUnsatisfiedConstraintIndexes
for (sci <- unsatisfiedConstraints) {
writeLine(summaryFW,"\t\t [C] ("+sci+") "+eccManipulator.getConstraint(sci).getSource())
if(!identifiedErrors.contains(sci)) {
identifiedErrors += sci 
val fileNameWithoutExtension = outputDir + "\\error-" + sci
eccManipulator.save(fileNameWithoutExtension + ".ecc")
new RequiresConflict(serializedEcc, sci, Some(finalEcc)).save(fileNameWithoutExtension + ".conflict")
writeLine(summaryFW,"\t\t\t recorded as a conflict")
}
else {
writeLine(summaryFW,"\t\t\t already identified")
}
}
}
}

def produceDefaultConflicts(inputFolder:String, outputFolder:String, filterFile:String) {
createFolder(outputFolder)


val filterReader = new BufferedReader(new FileReader(filterFile))
val includedConflicts = mutable.Set[String]()
var line = filterReader.readLine()
while(line != null) {
includedConflicts += line
line = filterReader.readLine()
}


for (file <- new File(inputFolder).listFiles
if file.getName.endsWith(".ecc")) {

val outputDir = outputFolder + file.getName + "/"
val log = new LogPrinter(outputDir + "summary.log")
log.println("Processing " + file.getAbsolutePath)

val eccManipulator = new EccManipulator(file.getAbsolutePath)
log.println("Number of Features: " + eccManipulator.getFeatureSize)
log.println("Number of Constraints: " + eccManipulator.getConstraintSize)

lazy val serializedEcc = new SerializedEccManipulator(eccManipulator)
var unsatisfiedConstraints = eccManipulator.getUnsatisfiedConstraintIndexes
for (sci <- unsatisfiedConstraints) {
log.println("\t [C] ("+sci+") " + eccManipulator.getConstraint(sci).getSource())
if (includedConflicts.contains(file.getName + "\\error-" + sci)) {
log.println("\t\tRecorded")
val fileNameWithoutExtension = outputDir + "\\error-" + sci
new RequiresConflict(serializedEcc, sci).save(fileNameWithoutExtension + ".conflict")
}
else {
log.println("\t\tFiltered out")
}
}
log.println("Number of Conflicts: " + unsatisfiedConstraints.size)
log.close
}
}

def countOperators(c:Expression):Int = {
var result = 0
rewrite{everywhere{query{
case _:BinaryExpression => result += 1
case _:Conditional => result += 1
case _:UnaryExpression => result += 1
case _:FunctionCall => result += 1
}}}(c)
result
}

def solveConflicts(inputFolder:String, outputFolder:String, strategy:Strategy) {
createFolder(outputFolder)
val log = new LogPrinter(outputFolder + "execution.log")
val conflictWriter = new BufferedWriter(new FileWriter(outputFolder + "conflicts.csv"))
conflictWriter.write("conflictID, Number Of Fixes, Number of all variables, Generation Time (ms), Cover User Change, Bring New Error\n")
val fixWriter = new BufferedWriter(new FileWriter(outputFolder + "fixes.csv"))
fixWriter.write("fixID, conflictID, Number of variables, Number of fix units, Cover User Change, Bring new conflict\n")
val unitWriter = new BufferedWriter(new FileWriter(outputFolder + "units.csv"))
unitWriter.write("unitID, fixID, conflictID, unitType, Number of variables, Number of operators\n")

for {dir <- new File(inputFolder).listFiles 
if !dir.getAbsolutePath.endsWith(".svn")
fileFolder <- dir.listFiles
conflictFile <- fileFolder.listFiles
if (conflictFile.getName.endsWith(".conflict"))}{

log.println("processing " + dir.getName + "\\" + fileFolder + "\\" + conflictFile.getName + "...")

val conflict = Conflict.load(conflictFile.getAbsolutePath)
val conflictID = dir.getName + "-" + fileFolder.getName + "-" + conflictFile.getName
val fixes = conflict.solve(executionTimes, strategy).toIndexedSeq
val optRefFixes = 
if (strategy == IgnoranceStrategy) Some(conflict.solve(1, EliminationStrategy).toSet)
else None

conflictWriter.write(conflictID + ", " + fixes.size + ", " + fixes.map(_.variables.size).sum + ", " + conflict.getLastSolvingMilliSeconds + ", ")

fixes.foreach(f => log.println("\t" + f))
def CoverChange2String(cover:Int) = cover match {
case 1 => "true"
case 0 => "not applicable"
case -1 => "false"
}
val isConflictSolvedByUser = conflict.isConflictSolvedByUser
var bringNewError = false
val coverChanges = 
if (fixes.size == 0) {
if (isConflictSolvedByUser == Some(true)) -1
else 0
}
else (for( fixID <- 0 until fixes.size ) yield {
val singleCoverChange = 
if (isConflictSolvedByUser == Some(true)) 
if (ExpressionHelper.evaluateTypeCorrectExpression(fixes(fixID).constraint, 
conflict.getUserValuation.get) == BoolLiteral(true)) 
1
else -1
else 0
fixWriter.write(fixID + ", " + conflictID + ", " + fixes(fixID).variables.size + ", " + fixes(fixID).units.size + ", " + CoverChange2String(singleCoverChange) + ",")

if (strategy != IgnoranceStrategy) fixWriter.write("false\n")
else {
val fixBringNewError = !(optRefFixes.get contains fixes(fixID))
fixWriter.write(fixBringNewError.toString + "\n")
bringNewError = bringNewError || fixBringNewError
}

val units = fixes(fixID).units.toIndexedSeq
for (unitID <- 0 until units.size)
unitWriter.write(unitID + ", " + fixID + ", " + conflictID + ", " + units(unitID).getClass.getName + ", " + units(unitID).variables.size + ", " + countOperators(units(unitID).constraint) + "\n")

singleCoverChange
}) reduce (java.lang.Math.max(_, _))
conflictWriter.write(CoverChange2String(coverChanges) + "," + bringNewError + "\n")
}
conflictWriter.close
fixWriter.close
unitWriter.close
log.close
}



def createFolder(inputFolder: String): File = {
var folder = new File(inputFolder)
if(!folder.exists)
folder.mkdirs
folder
}

def createFile(inputFile: String): FileWriter = {
var file = new File(inputFile)
if(file.exists) {
file.delete
} else {
file.createNewFile
}
var fstream = new FileWriter(file,true)
fstream
}

def writeLine(fstream: FileWriter, line: String) {
try{
fstream.write(line+"\r\n");
} catch {
case e: Exception => println("Error: " + e.getMessage());
}
}

def closeFile(fstream: FileWriter) {
fstream.close
}



}
*/
