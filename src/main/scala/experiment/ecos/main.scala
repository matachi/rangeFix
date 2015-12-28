package ca.uwaterloo.gsd.rangeFix

//import root_.ca.uwaterloo.gsd.rangeFix.Change
import java.text.{NumberFormat, ParsePosition}
import scala.collection.mutable.Map
import java.io.{FileWriter, File}
import collection.mutable.ListBuffer
import org.kiama.rewriting.Rewriter._

/**
 * Created by IntelliJ IDEA.
 * User: ahubaux
 * Date: 13/09/11
 * Time: 17:54
 * To change this template use File | Settings | File Templates.
 */

object MainEcosExperiment {

  private var conflictCombinations: List[List[Int]] = Nil

  def createFolder(inputFolder: String): File = {
    var folder = new File(inputFolder)
    if(folder.exists) {
      folder.delete
    } else {
      folder.mkdir
    }
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

  // taken from: http://rosettacode.org/wiki/Determine_if_a_string_is_numeric#Scala
  def isNumeric(input: String): Boolean = {
    val formatter = NumberFormat.getInstance
    val pos = new ParsePosition(0)
    formatter.parse(input, pos)
    input.length == pos.getIndex // valid if parse position is at end of string
  }

  def checkString(input: String): String = {
    if(input.startsWith("\"")) {
      input.drop(1)
    }
    if(input.endsWith("\"")) {
      input.dropRight(1)
    }
    input
  }

  def encodeOptionValue(input: String): SingleOptionValue = {
    if(isNumeric(input)) {
      return new IntOptionValue(input.toInt)
    } else if (input.startsWith("0x")) {
      return new IntOptionValue(java.lang.Long.parseLong(input.substring(2), 16))
    } else {
      return new StringOptionValue(checkString(input))
    }
    return null
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

  def processChanges(eccManipulator: EccManipulator,changes: Array[Change], summaryFW: FileWriter,
                     outputFolder: String, orderLabel: String): String = {
//    var nInactive = 0
//    var nConflicts = 0

    var inactiveFeatureList : ListBuffer[(Change,Boolean)] = new ListBuffer() //(feature_name,status) The status is
                                                                             // false if the feature has been
                                                                             // activated later on
    var conflictMap = Map[Int,String]() //(index,constraint)
    var fixesList: List[Iterable[DataFix]] = Nil
    var eccChange: OptionValue = null
    var conflictCombination: List[Int] = Nil
    var fixLog = ""
    var activeLog = ""

    writeLine(summaryFW,"Execution log:")
    for (c <- changes) {
      if(c.change1!=null && c.change2!=null){
        // this is a booldata
        eccChange = new DoubleOptionValue(IntOptionValue(c.change1.toInt),encodeOptionValue(c.change2))
      } else if (c.change1!=null) {
        eccChange = encodeOptionValue(c.change1)
      }

      if(!eccManipulator.isFeatureActive(c.feature)) {
//        println("\t[!]"+c.feature)
        writeLine(summaryFW,"\t[!]"+c.feature)
//        nInactive = nInactive + 1
        inactiveFeatureList.append((c,true))
      } else {
        writeLine(summaryFW,"\t"+c.feature)
       // println("\t"+c.feature)
        eccManipulator.changeFeature(c.feature,eccChange)
      }

      var i = 0
      for (ift <- inactiveFeatureList) {
        if(ift._2 == true && eccManipulator.isFeatureActive(ift._1.feature)) {
       //   println("\t\t [A]"+ift._1.feature)
          writeLine(summaryFW,"\t\t [A]"+ift._1.feature)
          if(ift._1.change1!=null && ift._1.change2!=null){
            eccChange = new DoubleOptionValue(IntOptionValue(ift._1.change1.toInt),encodeOptionValue(ift._1.change2))
          } else if (ift._1.change1!=null) {
            eccChange = encodeOptionValue(ift._1.change1)
          }
          eccManipulator.changeFeature(ift._1.feature,eccChange)
          inactiveFeatureList(i) = (ift._1,false)
        }
        i += 1
      }

      var unsatisfiedConstraints = eccManipulator.getUnsatisfiedConstraintIndexes


      for (sci <- unsatisfiedConstraints) {
        if(!conflictMap.contains(sci)) {
          conflictMap += (sci -> eccManipulator.getConstraint(sci).getSource())
          conflictCombination = sci.toInt :: conflictCombination
          conflictCombination = conflictCombination.sortWith(_ > _)
//          println("\t\t [C] ("+sci+") "+eccManipulator.getConstraint(sci).getSource())
          writeLine(summaryFW,"\t\t [C] ("+sci+") "+eccManipulator.getConstraint(sci).getSource())
         // nConflicts = nConflicts + 1
          var newConflictCombination = true
          for (cc <- conflictCombinations) {
            if(cc.equals(conflictCombination)) {
              newConflictCombination = false
            }
          }
          if(newConflictCombination) {
            var fileLabel = outputFolder+"conflict"
            var fixList =  conflictCombination+"\r\n"
  //          println("\t\t [F] "+conflictCombination)
            writeLine(summaryFW,"\t\t [F] "+conflictCombination)
            for (con <- conflictCombination) {
              fileLabel = fileLabel+"-"+con
              var fix = eccManipulator.generateFix(con, PropagationStrategy)
              fixList = "\t"+ c.feature +"\r\n\t\t"+ conflictCombination +"\r\n\t\t"+fix+"\r\n"
              fixesList = fix :: fixesList
              writeLine(summaryFW,"\t\t\t ("+con+") "+fix)
            }
            eccManipulator.save(fileLabel+".ecc")
            conflictCombinations = conflictCombination :: conflictCombinations
            fixLog = fixLog + fixList
          }
        }
      }
    }

    writeLine(summaryFW,"\r\nTotal number of inactive features to activate: "+inactiveFeatureList.size)
    writeLine(summaryFW,"\r\nInactive feature conflicts:")

    var inactivatedFeatures = 0
    for (ift <- inactiveFeatureList) {
      if(ift._2==true) {
        activeLog = activeLog + "\t" + ift._1.feature+"\r\n\t\t"+eccManipulator.activateFeature(ift._1.feature, PropagationStrategy)+ "\r\n"
        inactivatedFeatures = inactivatedFeatures + 1
      }
    }
    writeLine(summaryFW,activeLog)
    writeLine(summaryFW,"\r\nTotal number of conflicts: "+conflictMap.size)

    // Stats on fixes

    var nFixes: Double = 0
    var totalNComplexUnitsFix: Double = 0
    var totalNVariablesFix: Double = 0
    var avgNVariablesFix: Double = 0
    var maxNVariablesFix = 0
    var avgNComplexUnitsFix: Double = 0
    var totalNOperatorsCU: Double = 0
    var avgNOperatorsCU: Double = 0
    var maxNOperatorsCU = 0

    if(fixesList.size>0) {
      for (fi <- fixesList) {
        var nVar = 0
        var nCU = 0
        var nOp = 0
        if(fi.size>0) {
         for(f <- fi) {
           nVar = nVar + f.variables.size
           if (nVar>1) {
             nCU = nCU + 1
             nOp = countOperators(f.constraint)
             if (nOp>maxNOperatorsCU) maxNOperatorsCU = nOp
             totalNOperatorsCU = totalNOperatorsCU + nOp
           }
           if (f.variables.size>maxNVariablesFix) maxNVariablesFix = f.variables.size
         }
         nFixes = nFixes + fi.size
         totalNVariablesFix = totalNVariablesFix + nVar
         totalNComplexUnitsFix = totalNComplexUnitsFix + nCU
        }
      }
      avgNVariablesFix = totalNVariablesFix / nFixes
      avgNComplexUnitsFix = totalNComplexUnitsFix / nFixes
      if(totalNComplexUnitsFix>0) avgNOperatorsCU = totalNOperatorsCU / totalNComplexUnitsFix
    }

    // Aggregate and return stats on changes

    return outputFolder+" ; "+orderLabel+" ; "+changes.length+" ; "+inactiveFeatureList.size+" ; "+inactivatedFeatures+
      " ; "+conflictMap.size+" ; "+nFixes+" ; "+ avgNVariablesFix+" ; "+maxNVariablesFix+" ; "+avgNComplexUnitsFix+
      "; "+totalNComplexUnitsFix+" ; "+avgNOperatorsCU+" ; "+maxNOperatorsCU

  }

  def main (args: Array[String]) {

    var eccpath = ""
   // var eccManipulator: EccManipulator = null
    var changesArray: List[Array[Change]] = null
    val inputFolder = args(0)
    var outputFolder = args(1)
    if(!outputFolder.endsWith(File.separatorChar.toString)) outputFolder = outputFolder + File.separatorChar

    var finalSummaryFW = createFile(outputFolder+"summary.csv")
    writeLine(finalSummaryFW,"File name ; Order ; Number of user changes ; Number of inactive features ; " +
      "Number of inactived features ; Number of conflicts ; Number of fixes ; Average number of variables in fix ; " +
      "Maximum number of variables in fix ; Percentage of complex units ; " +
      "Total number of complex conflict units ; Average number of operators per complex unit ; " +
      "Maximum number of operators per complex unit")

    // iterate through the test directory
    for (dir <- new File(inputFolder).listFiles) {
	    println("\n\n[INFO] Processing "+dir.getAbsolutePath)
      createFolder(outputFolder+dir.getName)
      // load the test files
      for (file <- new File(dir.getAbsolutePath).listFiles) {
        if (file.getAbsolutePath.endsWith(".ecc")) {
          // load the ecc file
          println("\n\n[INFO] Loading ecc: "+file.getAbsolutePath)
          eccpath = file.getAbsolutePath
        } else if (file.getAbsolutePath.endsWith(".dec")) {
          // load the decision (change) file
          println("\n\n[INFO] Loading decisions: "+file.getAbsolutePath)
          var ecosHandler = new ECOSChangeHandler
          changesArray = ecosHandler.ECOSChangeParser.getChangesFromFile(file.getAbsolutePath)
        } // else unknown file type
      }
      // execute the changes
      if(!dir.getAbsolutePath.endsWith(".svn")) {
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
        conflictCombinations = Nil
        for (ca <- changesArray) {
          order match {
            case 0 => writeLine(summaryFW,"\r\nTop-down order") ; orderLabel = "top-down"
            case 1 => writeLine(summaryFW,"\r\nBottom-up order") ; orderLabel = "bottom-up"
            case 2 => writeLine(summaryFW,"\r\nRandom order") ; orderLabel = "random"
          }
          writeLine(summaryFW,"---------------------------------------------------------")
          println(orderLabel+" ["+ca.length+"]")
          var eccManipulator = new EccManipulator(eccpath)
          var res = processChanges(eccManipulator,ca,summaryFW,outputFolder+dir.getName+File.separatorChar,orderLabel)
          writeLine(finalSummaryFW,res)
          order = order + 1
        }
        closeFile(summaryFW)
      }
    }
    closeFile(finalSummaryFW)
  }
}

