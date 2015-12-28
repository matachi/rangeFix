package ca.uwaterloo.gsd.rangeFix
import java.lang.management.ManagementFactory
import com.sun.management.OperatingSystemMXBean

object Timer {
  val processors = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors()
  def printTime[T](msg:String)(func: =>T):T = {
    print(msg + "...")
    val start = java.lang.System.currentTimeMillis()
    val startCPUTime =  ManagementFactory.getOperatingSystemMXBean().asInstanceOf[OperatingSystemMXBean].getProcessCpuTime()
    val result = func
    val end = java.lang.System.currentTimeMillis()
    val endCPUTime = ManagementFactory.getOperatingSystemMXBean().asInstanceOf[OperatingSystemMXBean].getProcessCpuTime()
    println((end - start).toString + "ms (CPU: " + (endCPUTime - startCPUTime) / 1000000 + "ms)")
    result
  }

  def measureTime[T](times:Int)(func: =>T):T = {
    val start = java.lang.System.currentTimeMillis()
    val result = func
    for (i <- 1 until times) func
    val end = java.lang.System.currentTimeMillis()
    lastExecutionMillis = (end - start) / times
    result
  }

  def measureTime[T](func: =>T):T = {
    measureTime(1)(func)
  }

  var lastExecutionMillis = 0L
}
