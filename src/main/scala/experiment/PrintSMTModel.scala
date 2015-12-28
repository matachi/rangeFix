package ca.uwaterloo.gsd.rangeFix
import java.io._
import collection._

object PrintSMTModel {
  
  def main(args:Array[String]) {
  	for (file <- new File("experiment/defaults").listFiles
		if file.getName.endsWith(".ecc")) {

		TraceWriter.traceEnabled = true
		val eccManipulator = new EccManipulator(file.getAbsolutePath)
		for (i <- eccManipulator.getUnsatisfiedConstraintIndexes) { 
			TraceWriter.fileName = "smt/" + file.getName + "-" + i.toString + ".smt"
			eccManipulator.generateFixWithUnchangeableVars(i, Set[String]())
		}
	}
  }

}