package ca.uwaterloo.gsd.rangeFix
import java.io.OutputStreamWriter
import java.lang.Runtime
import java.io.InputStreamReader
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.Writer
import java.io.FileWriter
import scala.collection._


trait TraceWriter {
  def write(content:String)
  def flush()
}

class EnabledTraceWriter(w:Writer) extends TraceWriter {
  def write(content:String) = {
    val f = new FileWriter(TraceWriter.fileName, true)
    f.write(content)
    f.close
    w.write(content)
  }
  
  def flush() = w.flush()
}

class DisabledTraceWriter(w:Writer) extends TraceWriter {
  def write(content:String) = { w.write(content) }
  def flush() = w.flush()
}

object TraceWriter {
  var fileName:String = "temp.txt"
  var traceEnabled = false
}

class Z3 {
  val fixedParams = Array(CompilationOptions.Z3_PATH, "-in", "-smt2")
  val parameters = if (CompilationOptions.THREAD_NUMBER == 1) fixedParams else fixedParams ++ List("PAR_NUM_THREADS=" + CompilationOptions.THREAD_NUMBER)
  val p = Runtime.getRuntime().exec(parameters)
  val orgWriter = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()))
  val writer = if (TraceWriter.traceEnabled) new EnabledTraceWriter(orgWriter) else new DisabledTraceWriter(orgWriter)
  val reader = new BufferedReader(new InputStreamReader(p.getInputStream()))

  def declareVariables(vars: Iterable[(String, SMTType)]) {
    assert(reader.ready() == false, reader.readLine)
    for (v <- vars) {
      assert(reader.ready() == false, reader.readLine)
      writer.write("(declare-const ")
      writer.write(v._1 + " "
		   + v._2.toString);
      writer.write(")\n")
      assert(reader.ready() == false, reader.readLine)
    }
    // writer.flush()
    assert(reader.ready() == false, reader.readLine)
  }
  
  
  // assuming no string
  def assertConstraint(constraint: SMTExpression) {
    assertConstraint(constraint.toString)
  }

  def assertConstraint(constraint: String) {
    assert(reader.ready() == false, reader.readLine)
    // println("### asserting ### " + constraint)
    writer.write("(assert " + constraint + ")\n")
    assert(reader.ready() == false, constraint + " => " + reader.readLine())
    
  }
  
  def assertNamedConstraint(constraint:SMTExpression, name:String) {
    assertNamedConstraint(constraint.toString, name)
  }

  
  def assertNamedConstraint(constraint:String, name:String) {
    assert(reader.ready() == false, reader.readLine)
    writer.write("(assert (! " + constraint + " :named " + name + "))\n")
    // writer.flush()
    assert(reader.ready() == false)
  }
  
  def declareTypes(types: Iterable[SMTType]) {
    assert(reader.ready() == false, reader.readLine)
    val texts = types.map(_.toDeclaration).filter(_ != "")
    if (texts.size == 0) return
    writer.write("(declare-datatypes () (" + texts.reduceLeft(_ + _) + "))\n")
    // writer.flush()
    assert(reader.ready() == false)
  }
  
  def declareFunc(func: SMTFuncDefine) {
    assert(reader.ready() == false, reader.readLine)
    writer.write(func.toDefString + "\n")
    // writer.flush()
    assert(reader.ready() == false, reader.readLine)
    
  }
  

  def push() {
    assert(reader.ready() == false, reader.readLine)
    writer.write("(push)\n")
    // writer.flush()
    assert(reader.ready() == false)
  }

  def pop() {
    assert(reader.ready() == false, reader.readLine)
    writer.write("(pop)\n")
    // writer.flush()
    assert(reader.ready() == false, reader.readLine)
  }
  
  def checkSat(): Boolean = {
    assert(reader.ready() == false, reader.readLine)
    writer.write("(check-sat)\n")
    writer.flush()
    val line = reader.readLine();
    assert(reader.ready() == false, reader.readLine)
    if (line == "sat") true 
    else {
      assert(line == "unsat", line)
      false
    }
  }
  
  def enableUnsatCore() {
    assert(reader.ready() == false, reader.readLine)
    writer.write("(set-option :produce-unsat-cores true)\n")
    // writer.flush()
    assert(reader.ready() == false)
  }
  
  def getMinimalUnsatCore(vars: Iterable[String]): Option[Traversable[String]] = 
  {
    val core = getUnsatCore(vars)
    if (core.isDefined) {
      var toTest = core.get
      val newCore = mutable.ListBuffer[String]()
      while (toTest.size > 0) {
	push()
	try {
	  // assert vars except toTest.head
	  def assertVar(v:String) { assertNamedConstraint(v, exprNamePrefix + v) }
	  newCore foreach assertVar
	  toTest.tail foreach assertVar
	  
	  // if toTest.head is part of the minimal core
	  if (checkSat) newCore += toTest.head
	  
	  // remove toTest.head
	  toTest = toTest.tail
	}
	finally {
	  pop()
	}
      }
      Some(newCore)
    }
    else core
  }
  
  val exprNamePrefix = "__ex__"
  def getUnsatCore(vars: Iterable[String]): Option[Traversable[String]] = 
  {
    assert(reader.ready() == false, reader.readLine)
    push()
    try {
      for (v <- vars)
      assertNamedConstraint(v, exprNamePrefix + v)
      if (checkSat) {
	None
      }
      else {
	val result = getUnsatCore()
	result.map(_.map(name => {assert(name.size > exprNamePrefix.size, name); name.substring(exprNamePrefix.size)}))
      }
    }
    finally {
      pop()
      assert(reader.ready() == false, reader.readLine)
    }
  }

  // None means sat
  def getUnsatCore(): Option[Traversable[String]] = {
    assert(reader.ready() == false, reader.readLine)
    // println("### get-core ### " + varstr)
    writer.write("(get-unsat-core)\n")
    writer.flush()
    val line = reader.readLine()
    assert(reader.ready() == false, reader.readLine)
    val pattern = "(error \"line \\d+ column \\d+: unsat core is not available\")".r
    // println("### result ### " + line)
    if (pattern.findFirstIn(line) == line) return None
    assert(line.length() >= 2, line)
    assert(!line.startsWith("(error "), line)
    val trimmedLine = line.substring(1, line.length - 1)
    if (trimmedLine.size == 0) Some(List()) else Some(trimmedLine.split(" "))
  }	 

  //wj begin

  
  def getValValueMap(): (String,String) = {
    assert(reader.ready() == false, reader.readLine)
    writer.write("(get-model)\n")
    writer.flush()
    var strline:StringBuffer=new StringBuffer(80)
    var flag=true
    var braceNum=0
    var group=0
    while(flag)
    {
      var character=reader.read()
      if(character.toChar=='(')
	braceNum=braceNum+1
      else if(character.toChar==')')
	     {
          braceNum=braceNum-1
          if(braceNum==0)//括号匹配完成，读取输入结束
            {  
            reader.read()
            reader.read()//两个read将换行符和回车符读进来
            flag=false
          }
          else if(braceNum==1)
                 group=group+1
        }
      if(character!=13&&character!=10)//非换行符非回车符
        {
	  strline.append(character.toChar)
	}
    }
    val line=strline.toString() 
    assert(reader.ready() == false, reader.readLine)
          (line.substring(9, line.length - 1),group.toString)

  }	
  //wj end
  
  def exit() {
    writer.write("(exit)\n")
    try{
      writer.flush()
    }
    catch{
      case _ => //may have been closed
    }
    p.waitFor()
  }
}
