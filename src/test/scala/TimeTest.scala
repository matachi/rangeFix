package ca.uwaterloo.gsd.rangeFix
import org.scalatest._
import org.scalatest.matchers.ShouldMatchers

object TestHelper{
  def loadModelFile(model:String, file:String, printDetail:Boolean=true):KconfigLoader={
    if (printDetail)
      println("load file: %s, %s" format(model, file))
    val loader = new KconfigLoader(model, file)
    if (printDetail)
      println("loaded!")
    return loader
  }
}
class TimeTest extends FunSuite with ShouldMatchers{
  test("bloody test")
  {
    val path = "./experiment/KConfig/"
    
    /*
     val configPath = path+"configuration_files/astlinux_2.6.17_281.config"
     val secconfigPath = path+"configuration_files/astlinux_2.6.17_297.config"
     val loader = TestHelper.loadModelFile(path+"constraint_files/2.6.17.exconfig", configPath)*/
    //Exception
    

    
    
    /*val configPath = path+"configuration_files/astlinux_2.6.17_297.config"
    val secconfigPath = path+"configuration_files/astlinux_2.6.17_308.config"
    val loader = TestHelper.loadModelFile(path+"constraint_files/2.6.17.exconfig", configPath)*/
    //Exception
    
    

    
    /*
     val configPath = path+"configuration_files/astlinux_2.6.17_308.config"
     val secconfigPath = path+"configuration_files/astlinux_2.6.17_311.config"
     val loader = TestHelper.loadModelFile(path+"constraint_files/2.6.17.exconfig", configPath)
     //HZ变量在SMTFixGenerator.generateSimpleDiagnoses里diagnoses = (new HSDAG[String]()).getDiagnoses(getMinimalCore, true)出错
     */


    
    /*
     val configPath = path+"configuration_files/astlinux_2.6.17_311.config"
     val secconfigPath = path+"configuration_files/astlinux_2.6.17_315.config"
     val loader = TestHelper.loadModelFile(path+"constraint_files/2.6.17.exconfig", configPath)
     //HZ变量在SMTFixGenerator.generateSimpleDiagnoses里diagnoses = (new HSDAG[String]()).getDiagnoses(getMinimalCore, true)出错
     */


    
    /*
     val configPath = path+"configuration_files/astlinux_2.6.17_315.config"
     val secconfigPath = path+"configuration_files/astlinux_2.6.17_500.config"
     val loader = TestHelper.loadModelFile(path+"constraint_files/2.6.17.exconfig", configPath)
     //FS_POSIK_ACL变量在SMTFixGenerator.generateSimpleDiagnoses里diagnoses = (new HSDAG[String]()).getDiagnoses(getMinimalCore, true)出错
     */


    /*
     val configPath = path+"configuration_files/astlinux_2.6.17_500.config"
     val secconfigPath = path+"configuration_files/astlinux_2.6.17_572.config"
     val loader = TestHelper.loadModelFile(path+"constraint_files/2.6.17.exconfig", configPath)
     //okay
     */


    
    /*
     val configPath = path+"configuration_files/CdMa-HeRoC-2.6.29_2.6.29_1.config"
     val secconfigPath = path+"configuration_files/CdMa-HeRoC-2.6.29_2.6.29_2.config"
     val loader = TestHelper.loadModelFile(path+"constraint_files/2.6.29.exconfig", configPath)
     //MSM_AMSS_VERSION变量在ConfigManager类里setFeature里throw new java.lang.IllegalArgumentException("  feature cannot be found.")
     //去除该变量后
     //okay
     */
    
    /*
     val configPath = path+"configuration_files/crux-arm_2.6.19_1.config"
     val secconfigPath = path+"configuration_files/crux-arm_2.6.19_2.config"
     val loader = TestHelper.loadModelFile(path+"constraint_files/2.6.19.exconfig", configPath)
     //okay
     */
    

    
    
     val configPath = path+"configuration_files/crux-arm_2.6.19_2.config"
     val secconfigPath = path+"configuration_files/crux-arm_2.6.19_3.config"
     val loader = TestHelper.loadModelFile(path+"constraint_files/2.6.19.exconfig", configPath)
     //okay
    
    
    

    /*
     val configPath = path+"configuration_files/lustre-release_2.6.32_3.config"
     val secconfigPath = path+"configuration_files/lustre-release_2.6.32_4.config"
     val loader = TestHelper.loadModelFile(path+"constraint_files/2.6.32.exconfig", configPath)
     //HAVE_IRQ_WORK变量在ConfigManager类里setFeature里throw new java.lang.IllegalArgumentException("  feature cannot be found.")
     //去除不在_effectiveMap的变量后，asseration failed
     */

    /*
     val configPath = path+"configuration_files/lustre-release_2.6.32_4.config"
     val secconfigPath = path+"configuration_files/lustre-release_2.6.32_5.config"
     val loader = TestHelper.loadModelFile(path+"constraint_files/2.6.32.exconfig", configPath)
     //okay
     */

    /*
     val configPath = path+"configuration_files/lustre-release-1_2.6.32_2.config"
     val secconfigPath = path+"configuration_files/lustre-release-1_2.6.32_3.config"
     val loader = TestHelper.loadModelFile(path+"constraint_files/2.6.32.exconfig", configPath)
     //BH_LRU_SIZE变量是唯一要修改的变量
     //但不在_effectiveMap里，所以去除该变量，没有要修改的变量
     //okay
     */

    /*
     val configPath = path+"configuration_files/nascc_2.6.20.4_122.config"
     val secconfigPath = path+"configuration_files/nascc_2.6.20.4_134.config"
     val loader = TestHelper.loadModelFile(path+"constraint_files/2.6.20.exconfig", configPath)
     //LEGACY_PTY_COUNT_default unknown   个人推测整数处理是不是有问题
     */

    /*
     val configPath = path+"configuration_files/pdaxroom_2.6.32_orig.config"
     val secconfigPath = path+"configuration_files/pdaxroom_2.6.32_1.config"
     val loader = TestHelper.loadModelFile(path+"constraint_files/2.6.32.exconfig", configPath)
     //Exception
     */

    /*
     val configPath = path+"configuration_files/runnix_2.6.20_runnix_50.config"
     val secconfigPath = path+"configuration_files/runnix_2.6.20_runnix_72.config"
     val loader = TestHelper.loadModelFile(path+"constraint_files/2.6.20.exconfig", configPath)
     //okay
     */

    
    /* val configPath = path+"configuration_files/tuxbox_2.6.12_org.config"
     val secconfigPath = path+"configuration_files/tuxbox_2.6.12_1.config"
     val loader = TestHelper.loadModelFile(path+"constraint_files/2.6.12.exconfig", configPath)*/
    //okay
    


    
    var mycompare=new myNewCompareFile(loader,configPath,secconfigPath)
    var modifyMap=mycompare.getResult()
    var oldLoader=loader
    var totalExcutionTime = 0L
    var excutionTime = 0L
    var successCt = 0
    var totalCt = 0
    var fixesCt = 0
    var fixesUnit =0
    var unitVars =0
    var fixRt:FixGenResult = null
    var i = modifyMap.keySet.iterator//迭代器
    while(i.hasNext){//遍历 
      var str=i.next
      if(oldLoader.getVarType(str) != StringType) {
        import java.lang.Runnable
        import java.lang.Thread
        val manager = new KconfigManager(oldLoader, 1);
        var flag:Boolean = false
        val task = new Runnable{
          override def run()={
            fixRt = Timer.measureTime(manager.setFeature (str, modifyMap(str)))
            excutionTime = Timer.lastExecutionMillis
            flag = true
          }
        }
        val thread = new Thread(task)
        thread.start()
        thread.join(20000)
        thread.stop
        totalCt+=1
        if (flag){
          successCt += 1
          totalExcutionTime += excutionTime
          fixesCt += fixRt.fixes.size
          fixRt.fixes.foreach(fix=>{
            fixesUnit += fix.units.size
            fix.units.foreach(unit=> unitVars += unit.variables.size)
          })
          oldLoader.modifyConfiguration(Map(str->modifyMap(str)))
        }
      }         
    }
    if(totalCt>0)
      printf("total: %d, sucess:%d, %f\n", totalCt, successCt, successCt.toFloat/totalCt.toFloat)
    if(successCt>0)
      {
      printf("total time: %s ms, average time:%s ms\n", totalExcutionTime.toString, (totalExcutionTime/successCt).toString())
      printf("total fixes: %d, average:%f\n", fixesCt, fixesCt.toFloat/successCt.toFloat)
    }
    if(fixesCt>0)
      printf("total unit: %d, average:%f per fix\n", fixesUnit, fixesUnit.toFloat/fixesCt.toFloat )
    if(fixesUnit>0)
      printf("total variables:%d average:%f per unit\n", unitVars, unitVars.toFloat/fixesUnit.toFloat)
  }
}
