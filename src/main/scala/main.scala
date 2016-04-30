package ca.uwaterloo.gsd.rangeFix
import java.io._
import collection._
import scala.collection.mutable.{ListBuffer, ArrayBuffer}

object Main {
  var executionTimes = 1
  var printDetailedTime = true
  var measureTime = true

  def printDetailedTime[T](id: String)(func: => T): T = {
    if (printDetailedTime) Timer.printTime(id)(func) else func
  }

  def measureTime[T](func: => T): T = {
    if (measureTime) {
      val result = Timer.measureTime {
        val result = func
        for (i <- 1 until executionTimes)
        func
        result
      }
      println("Execution Time: " + (Timer.lastExecutionMillis / executionTimes) + "ms from " + executionTimes + " executions")
      result
    } else func
  }

  def printTime(milliSeconds:Long) {
    if (measureTime)
      println("Execution Time: %d ms from %d executions.".format(milliSeconds, executionTimes))
  }

  def findArgIndex(args: Seq[String], tgts: Iterable[String]): Int = {
    for (t <- tgts) {
      val i = args.indexOf(t)
      if (i >= 0) return i
    }
    return -1
  }

  // deal with "-verbose" and "-time".
  // only execute once in "verbose" mode, otherwise too many logs are
  // printed
  def dealWithVerboseTime(cArgs:mutable.ListBuffer[String]) {
    if (cArgs.contains("-verbose") | cArgs.contains("-v"))
      printDetailedTime = true
    else {
        val timeIndex = findArgIndex(cArgs, List("-time", "-t"))
        if (timeIndex >= 0) {
          measureTime = true
          val numberPattern = "\\d+".r
          if (cArgs.size > timeIndex + 1 &&
              numberPattern.findFirstIn(cArgs(timeIndex + 1)) ==
              Some(cArgs(timeIndex + 1))) {
            executionTimes = cArgs(timeIndex + 1).toInt
            cArgs.remove(timeIndex + 1)
          }
          cArgs.remove(timeIndex)
        }
      }
  }

  
  
}

// Summary of arguments
// -verbose or -v: print detailed execution time
// -set [f] [v] or -s [f] [v]: set a config f to value v
// -time [n] or -t [n]: execute the generation n times to get the average execution time
object KconfigMain {
  import Main._

  def main(args: Array[String]) {

    if (!new java.io.File(CompilationOptions.Z3_PATH).exists()) {
      println("Cannot find Z3.")
      return
    }

    val cArgs = mutable.ListBuffer() ++ args    

    dealWithVerboseTime(cArgs)

    if (cArgs.size < 4) {
      println("Parameters: <exconfig> <config> <optionName> <value>\nwhere value can be \"yes\", \"mod\", \"no\" or any number.\nOptional parameters:\n\t -verbose or -v: print detailed execution time\n\t-time [n] or -t [n]: execute the generation n times to get the average execution time\n")
      return
    }

    val model = cArgs(0)
    val file = cArgs(1)
    val options = ArrayBuffer[(String, Literal)]()
    var i: Int = 2
    while (i < cArgs.size) {
      options += extractOptionNameAndValue(cArgs, i)
      i += 2
    }

    try {
      // load main file
      print("Loading file...")
      val loader = new KconfigLoader(model, file)
      println("Loaded.")
      print("Preparing the fix generator...")
      val manager = new KconfigManager(loader, executionTimes)
      println("done.")
      println("Computing fixes...")
      for ((id, value) <- options)
        manager.setFeature(id, value)
      val result = manager.getFixes()
      val fixes = result.fixes
      printTime(result.milliseconds)

      // print fixes
      if (fixes.size == 0)
        println("It is not possible to change the config.")
      else
        fixes.foreach(f => println("\t" + f))
      
    } catch {
      case e: Exception => e.printStackTrace()
    }
  }

  def extractOptionNameAndValue(cArgs: ListBuffer[String], position: Int): (String, Literal) = {
    val id = cArgs(position)
    val valueStr = cArgs(position + 1)
    val value = valueStr toLowerCase match {
      case "yes" => Kconfig.tristateYes
      case "no" => Kconfig.tristateNo
      case "mode" => Kconfig.tristateMod
      case x@_ => IntLiteral(x.toInt)
    }
    (id, value)
  }
}

object EccMain {
  import Main._


  // Execution form: 
  // Summary of arguments
  // -verbose or -v: print detailed execution time
  // -time [n] or -t [n]: execute the generation n times to get the average execution time
  // -annotation [path] or -ann [path]: specify the path to the annotation file to fix type error in eCos file
  // -activate [f] or -a [f]: activate a feature with name f in ecc
  // TODO:
  // * apply the fix after user choice
  // * set Z3 path to a suitable location
  def main(args: Array[String]) {

    if (!new java.io.File(CompilationOptions.Z3_PATH).exists()) {
      println("Cannot find Z3.")
      return
    }

    val cArgs = mutable.ListBuffer() ++ args

    // deal with "-verbose" and "-time"
    dealWithVerboseTime(cArgs)

    // deal with "-annotation" which specifies the path of the annotation file for eccFile
    val annotationIndex = findArgIndex(cArgs, List("-annotation", "-ann"))
    if (annotationIndex >= 0) {
      globalAnnotationPath = cArgs(annotationIndex + 1)
      cArgs.remove(annotationIndex)
      cArgs.remove(annotationIndex)
    }

    // load main file
    val file = cArgs.filter(!_.startsWith("-")).head
    val loader = new EccLoader(file, globalAnnotationPath)

    // If the execution is to activate a feature, activate it.
    try {
      val activateIndex = { val a = cArgs.indexOf("-activate"); if (a < 0) cArgs.indexOf("-a") else a }
      if (activateIndex >= 0) {
        val id = cArgs(activateIndex + 1)
        activateSimple(loader, id)
        return
      }

      // otherwise check errors for an ecc file and fix interacctively.
      interactiveSimple(loader)
    } catch {
      case e: Exception => e.printStackTrace()
    }
  }

  def interactiveSimple(loader: EccLoader) {
    val eccManipulator = new EccManager(loader, executionTimes)
    val satisfiedConstraints = eccManipulator.getSatisfiedConstraintIndexes
    val unsatisfiedConstraints = eccManipulator.getUnsatisfiedConstraintIndexes
    
    if (unsatisfiedConstraints.size == 0) {
      println("This file contains no conflict.")
      return
    }
    var i = 0
    for (sci <- unsatisfiedConstraints) {
      i += 1
      println(i.toString + ". " + eccManipulator.getConstraint(sci).getSource() + " is violated.")
    }

    // fix each conflict
    i = 0
    for (sci <- unsatisfiedConstraints) {
      val result = eccManipulator.generateFix(sci)
      val fixes = result.fixes
      printTime(result.milliseconds)

      // print fixes
      i += 1
      println("Fixes for " + i.toString)
      if (fixes.size == 0)
        println("\tNo fix can be found")
      else
        fixes.foreach(f => println("\t" + f))

    }

  }

  def activateSimple(loader: EccLoader, id: String) {
    val eccManipulator = new EccManager(loader, executionTimes)
    // val satisfiedConstraints = eccManipulator.getSatisfiedConstraintIndexes
    // val unsatisfiedConstraints =
    //   eccManipulator.getUnsatisfiedConstraintIndexes
    val optActiveIndex = eccManipulator.getActiveConstraintIndex(id)
    if (optActiveIndex.isEmpty) {
      println("Feature " + id + " cannot be found.")
      return
    }
    if (eccManipulator.isNodeActive(id)) {
      println("Feature " + id + " is already active.")
      return
    }

    val result = eccManipulator.activateFeature(id)
    val fixes = result.fixes
    printTime(result.milliseconds)

    // print fixes
    if (fixes.size == 0)
      println("It is not possible to activate this feature.")
    else
      fixes.foreach(f => println("\t" + f))

  }

  var globalAnnotationPath = "testfiles/realworld/allModels.annotation"

}
