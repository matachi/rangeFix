package ca.uwaterloo.gsd.rangeFix
import java.io._
import scala.xml.XML
object kconfigtemptest{
import collection.mutable
import collection._
import ConditionalCompilation._

var flag = false
var configuration:Map[String, SMTLiteral] = null 
var varSet:Set[String]=null 
var constraints: Iterable[SMTExpression]=null
var 			      types:Map[String, SMTType] =null 
var 			      funcsToDeclare:Seq[SMTFuncDefine]=null
var 			      optTypesToDeclare:Set[SMTType] = null
}
case class SingleLog(modiFeature:Option[Tuple3[String,Literal,Boolean]],changes:Map[String,Literal]){}
class KConfigChangeLogger{
  var logs:IndexedSeq[SingleLog] = IndexedSeq[SingleLog]()
  def  addLog(log:SingleLog){
    logs = logs :+ log
  }
  def writeLogs(fileName:String){
    val file = new File(fileName)
    if (file.exists())
      file.delete()
    val out = new ObjectOutputStream(new FileOutputStream(fileName))
    try {
      writeLogs(out)
    }
    finally{
      out.close()
    }
  }
  def loadLogs(fileName:String){
    val in = new ObjectInputStream(new FileInputStream(fileName))
    try{
      loadLogs(in)
    }
    finally{
      in.close()
    }
  }
  def writeLogs(o:ObjectOutputStream){
    o.writeInt(logs.size)
    logs.foreach(log=>{
      if (!log.modiFeature.isDefined)
        o.writeInt(0)
      else {
        o.writeInt(1)
        o.writeObject(log.modiFeature.get._1)
        o.writeObject(log.modiFeature.get._2)
        o.writeBoolean(log.modiFeature.get._3)
      }
      o.writeInt(log.changes.size)
      log.changes.foreach(change=>{
        o.writeObject(change._1)
        o.writeObject(change._2)
      })
    })
  }
  def loadLogs(input:ObjectInputStream){
    logs = IndexedSeq[SingleLog]()
    val logSize = input.readInt
    (0 until logSize).foreach(i=>{
      val modiTag = input.readInt
      val m =
        if (modiTag == 0)
          None
        else {
          assert(modiTag==1)
          val id = input.readObject.asInstanceOf[String]
          val v = input.readObject.asInstanceOf[Literal]
          val c = input.readBoolean
          Some(id,v,c)
        }
      var changes:Map[String,Literal] = Map[String,Literal]()
      val changeSize = input.readInt
      (0 until changeSize).foreach(j=>{
        val id = input.readObject.asInstanceOf[String]
        val v = input.readObject.asInstanceOf[Literal]
        changes = changes + (id->v)
      })
      logs = logs :+ SingleLog(m, changes)
    })
  }
}

object KConfigExperimentConfiguation{
val pathConstraints = "./experiment/KConfig/constraint_files/"
val pathConfigurations = "./experiment/KConfig/configuration_files/"
val pathResults = "./experiment/KConfig/results_nonminimal/"
val pairComparations = List(
  ("2.6.17.exconfig","astlinux_2.6.17_281.config","astlinux_2.6.17_297.config"),
  ("2.6.17.exconfig","astlinux_2.6.17_297.config","astlinux_2.6.17_308.config"),
  ("2.6.17.exconfig","astlinux_2.6.17_308.config","astlinux_2.6.17_311.config"),
  ("2.6.17.exconfig","astlinux_2.6.17_311.config","astlinux_2.6.17_315.config"),
  ("2.6.17.exconfig","astlinux_2.6.17_315.config","astlinux_2.6.17_500.config"),
  ("2.6.17.exconfig","astlinux_2.6.17_500.config","astlinux_2.6.17_572.config"),
  ("2.6.29.exconfig","CdMa-HeRoC-2.6.29_2.6.29_1.config","CdMa-HeRoC-2.6.29_2.6.29_2.config"),
  ("2.6.19.exconfig","crux-arm_2.6.19_1.config","crux-arm_2.6.19_2.config"),
  ("2.6.19.exconfig","crux-arm_2.6.19_2.config","crux-arm_2.6.19_3.config"),
  ("2.6.32.exconfig","pdaxroom_2.6.32_orig.config","pdaxroom_2.6.32_1.config"),
  ("2.6.20.exconfig","runnix_2.6.20_runnix_50.config","runnix_2.6.20_runnix_72.config")
)
}
case class ExperimentSingleData(value:Literal, finished:Boolean, cover:Boolean, fixResult: Option[FixGenResult]){}
object KConfigExperiment{
  var experimentStrategy:Strategy = PropagationStrategy
  def main(args: Array[String]){
    if (args.size>0 && args(0)=="-c")
      produceConflicts()
    else if (args.size>0 && args(0)=="-e"){
      runFullExperiment(true,true)
      runFullExperiment(true,false)
      //runFullExperiment(false,true)
    }
    else if (args.size>0 && args(0)=="-t"){
      testNewAlgorithm()
    }
  }
  def testNewAlgorithm(){
    import KConfigExperimentConfiguation._
    
    pairComparations.foreach(pair=>{ 
      val model = pair._1
      val origFile=pair._2
      val currentFile=pair._3
      val logger = new KConfigChangeLogger
      var loader = new KconfigLoader(pathConstraints+model,pathConfigurations+origFile)
      logger.loadLogs("./experiment/KConfig/conflicts/"+origFile+"/log.logger")
      logger.logs.foreach(log=>{
        if (log.modiFeature.isDefined) {
          println("[%s]:%s".format(origFile,log.modiFeature.get._1))
          val manager = new KconfigManager(loader)
          SMTFixGenerator.useAlgorithm = true
          SMTFixGenerator.divideUnits = true
          SMTFixGenerator.testNewAlgorithm = true
          manager.setFeature(log.modiFeature.get._1,log.modiFeature.get._2)
        }
        loader.modifyConfiguration(log.changes)
      })
    })
  }
  def produceConflicts(){
    SMTFixGenerator.useAlgorithm = true
    SMTFixGenerator.divideUnits= true
    import KConfigExperimentConfiguation._
    pairComparations.foreach(pair=>{
      println("process %s" format pair._2)
      val basePath = "./experiment/KConfig/conflicts/"+pair._2
      createFolder(basePath)
      val summaryFileWriter = new FileWriter(basePath+"/summary.txt")
      val logger = try{
        generateLogs(pathConstraints+pair._1,pathConfigurations+pair._2,pathConfigurations+pair._3,summaryFileWriter)
      }
      finally{
        summaryFileWriter.close() 
      }
      logger.writeLogs(basePath+"/log.logger")
    })
  }
  def fixBool(id:String, value:Literal, loader:KconfigLoader):(String, Literal)= loader.getVarType(id) match{
      case None => (id,value)
      case _ => loader.getVarType(id).get match{
        case BoolType => value match {
          case Kconfig.tristateYes | Kconfig.tristateMod=> (id, BoolLiteral(true))
          case Kconfig.tristateNo  => (id, BoolLiteral(false))
          case _ => (id, value)
        }
        case _ => (id, value)
      }
  }
  def generateLogs(model:String, originalFile:String, currentFile:String, summaryWriter:FileWriter):KConfigChangeLogger= {
    summaryWriter.write("model:%s, orginal file:%s, current file:%s\n".format(model,originalFile,currentFile))
    val logger = new KConfigChangeLogger
    var mapResults:Map[String,ExperimentSingleData] = Map[String, ExperimentSingleData]()
    var loader:KconfigLoader = new KconfigLoader(model, originalFile)
    var currentLoader = new KconfigLoader(model,currentFile)
    var mycompare=new myNewCompareFile(loader,originalFile,currentFile)
    var currentEvaluation = currentLoader.valuation.map(a=>fixBool(a._1, a._2, currentLoader))
    var alreadyModificated = Map[String, Literal]()
    val mapModifications = scala.util.Random.shuffle(mycompare.getResult().map(a=>fixBool(a._1, a._2, loader)).toTraversable)
    mapModifications.foreach( modification=> {
      println("consider %s" format modification._1)
      if (alreadyModificated.contains(modification._1)){
        summaryWriter.write("%s already changed before\n" format modification._1)
      }
      else if (loader.getVarType(modification._1)==None){
        summaryWriter.write("varieble %s not found\n" format modification._1)
      }
      else {
        val id:String = modification._1
        val value:Literal = modification._2
        var changes = Map[String, Literal]()
        if (!value.isInstanceOf[Literal] || value.isInstanceOf[StringLiteral]){
          summaryWriter.write("change %s to %s is not supported\n".format(id,value.toString))
          changes = changes + (id->value)
          logger.addLog(SingleLog(None, changes))
        }
        else {
          import java.lang.Runnable
          import java.lang.Thread
          var flag:Boolean = false
          var fixRlt:FixGenResult = null
          val manager = new KconfigManager(loader, 1)
          val task = new Runnable{
            override def run()={
                fixRlt = manager.setFeature(id, value)
                flag = true
            }
          }
          val thread = new Thread(task)
          thread.setName("setFeature")
          thread.start()
          thread.join(20000)
          thread.stop
          if (flag) {
            summaryWriter.write("%s changed to %s results: %s\n".format(id,value.toString,fixRlt.toString))
            val isConflict = fixRlt.fixes.size match{
              case 0=>false
              case 1=>{
                val fix = fixRlt.fixes.toIndexedSeq(0)
                fix.units.size match{
                  case 0=>false
                  case 1=> fix.units.toIndexedSeq(0) match {
                    case x:AssignmentUnit=> (x.variable != id || x.expr != value)
                    case _=>true
                  }
                  case _ =>true
                }
              }
              case _=>true
            }
            val rlt = fixRlt.fixes.find(fix=>{ // find a fix that cover the user change
              fix.units.forall(unit=>{
                unit match {
                  case u:AssignmentUnit => currentEvaluation(u.variable) == u.expr
                  case u:RangeUnit => {
                    import org.kiama.rewriting.Rewriter._
                    ExpressionHelper.simplify(
                      rewrite(everywheretd(rule{ case IdentifierRef(id) => currentEvaluation(id) })) (u.constraint)
                    ) == BoolLiteral(true)
                  }
                  case _ =>false
                }
              })
            })
            if (rlt!=None){
              rlt.get.units.foreach(unit=>{
                unit.variables.foreach(id => changes += (id -> currentEvaluation(id)))
              })
              summaryWriter.write("\t cover user change ")
            }
            else summaryWriter.write("\t not cover user change ")
            if (!changes.contains(id))
              changes = changes + (id->value)
            if (isConflict){
                summaryWriter.write("marked as conflict\n")
                logger.addLog(SingleLog(Some((id,value,rlt.isDefined)),changes))
              }
            else
              {summaryWriter.write("is not a conflict\n")
                logger.addLog(SingleLog(None,changes))
             }
          }
          else{
            changes = changes + (id->value)
            summaryWriter.write("%s changed to %s not finished\n".format(id,value.toString))
            logger.addLog(SingleLog(None,changes))
          }
        }
        summaryWriter.write("do changes:%s\n" format changes.toString())
        alreadyModificated  = alreadyModificated ++ changes
        assert(changes==logger.logs(logger.logs.size-1).changes,"changes:/n%s/n%s/n".format(changes,logger.logs(logger.logs.size-1)))
        loader.modifyConfiguration(changes)
      }
    })
    summaryWriter.write("total user change :%s\n conflict:%s\n".format(logger.logs.size.toString, logger.logs.filter(_.modiFeature.isDefined).size.toString))
    logger
  }
  
  def createFolder(name:String){
    val file = new File(name)
    if (file.exists && !file.isDirectory)
      file.delete
    if (!file.exists())
      file.mkdirs()
  }
  
  def runFullExperiment(useAl:Boolean, divideUn:Boolean){
    SMTFixGenerator.useAlgorithm = useAl
    SMTFixGenerator.divideUnits = divideUn
    val fileName = {if (useAl) "newAlgorithm_" else "oldAlgorithm_"}+ {if(divideUn) "divided" else "nondivided"}
    import KConfigExperimentConfiguation._
    createFolder(pathResults+fileName)
    pairComparations.foreach(pair=>{
      val basePath = "./experiment/KConfig/conflicts/"+pair._2
      var pairFileElem = <File origFile={pair._2} currFile={pair._3}></File>
      val logger = new KConfigChangeLogger
      logger.loadLogs(basePath+"/log.logger")
      val results = solveConflictsFromLogger(pathConstraints+pair._1,pathConfigurations+pair._2,logger)
      results.foreach(rlt=>{
        val resultElem = if (rlt._2.finished){
          val fixResult = rlt._2.fixResult.get
          var re:scala.xml.Elem = <Modification varName={rlt._1} value={rlt._2.value.toString} finish="true" cover={rlt._2.cover.toString} time={fixResult.milliseconds.toString}> </Modification>
          fixResult.fixes.foreach(fix=>{
            var fixElem:scala.xml.Elem = <Fix />
            fix.units.foreach(unit=>{
              var unitElem = <Unit varNum={unit.variables.size.toString} constraint={unit.constraint.toString} />
              fixElem = fixElem.copy(child=fixElem.child :+ unitElem)
            })
            re = re.copy(child = re.child :+ fixElem)
          })
          re
        }
        else
          <Modification varName={rlt._1} value={rlt._2.value.toString} finish="false"/>
        pairFileElem = pairFileElem.copy(child=pairFileElem.child :+ resultElem)
      })
      val xmlFile = new File (pathResults+fileName+"/"+pair._2+".xml")
      if (xmlFile.exists)
          xmlFile.delete
        scala.xml.XML.save(xmlFile.getAbsolutePath(), pairFileElem, "UTF-8")
    })
  }
  
  def solveConflictsFromLogger(model:String, originalFile:String, logger:KConfigChangeLogger):Map[String,ExperimentSingleData]= {
    var mapResults:Map[String,ExperimentSingleData] = Map[String, ExperimentSingleData]()
    var loader:KconfigLoader = new KconfigLoader(model, originalFile)
    var endFlag:Boolean = false
    logger.logs.foreach( log => {
      if (log.modiFeature.isDefined){//  && (log.modiFeature.get._1=="CRYPTO_MD5")) {
        val message = "[%s,%s]".format(if (SMTFixGenerator.useAlgorithm) "new algorithm" else "old algorithm", if (SMTFixGenerator.divideUnits) "divide" else "nondivide")
        val id:String = log.modiFeature.get._1
        val value:Literal = log.modiFeature.get._2
        println("%s%s->%s".format(message,id,value.toString))
        val cover = log.modiFeature.get._3
        var fixRlt:FixGenResult = null
        val finishFlag = {
          var flag:Boolean = false
          val manager = new KconfigManager(loader, 1)
          val task = new Runnable{
            override def run()={
              fixRlt= manager.setFeature(id, value)
              flag = true
            }
          }
          val thread = new Thread(task)
          thread.setName("setFeature")
          thread.start()
          thread.join(20000)
          thread.stop
          flag
        }
        if (!finishFlag){
          mapResults += (id->ExperimentSingleData(value, false, cover, None))
        }
        else{
          val manager = new KconfigManager(loader,100)
          fixRlt = manager.setFeature(id, value)
          println(fixRlt)
          mapResults += (id->ExperimentSingleData(value,true, cover, Some(fixRlt)))
        }
      }
      loader.modifyConfiguration(log.changes)
    })
    mapResults
  }
}
