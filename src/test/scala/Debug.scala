package ca.uwaterloo.gsd.rangeFix
import gsd.linux._

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import org.kiama.rewriting.Rewriter

//<to be deleted>

//<deleted>
class DebugSuite extends FunSuite with ShouldMatchers with BeforeAndAfterEach {
  test("nested function") {
    val model = "src/test/resources/2.6.30.exconfig"
    val config = "src/test/resources/2.6.30.config"
    val featureID = "I7300_IDLE"
    val value = Kconfig.tristateYes
    val loader = new KconfigLoader(model, config)
    val manager = new KconfigManager(loader)
    val result = manager.setFeature(featureID, value)
    val fixes = result.fixes
    fixes.size should equal (1)
    fixes.head.units.size should equal (2)
    fixes.head.variables should equal (Set("X86_64", "I7300_IDLE"))    

  }
}
