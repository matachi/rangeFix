package ca.uwaterloo.gsd.rangeFix

import gsd.linux._

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import org.kiama.rewriting.Rewriter
import collection._

class KconfigTest extends FunSuite with ShouldMatchers {

  implicit def toId(s: String) = Id(s)

  test("Linux 3.0 type check") {
    val f = (getClass.getResource("../../../../2.6.30.exconfig").getFile)
    val parsedFile = KConfigParser.parseKConfigFile(f)
    val kc = new Kconfig2(parsedFile)

    val exprs = kc.varTypes.keys.toList.map(x =>
      ( x, kc.effective(x).toExpression)) ++ kc.allConstraints.map(
      x => ("global constraint", x))

    val vars = exprs.map{case (n, e) => Expression.collectGlobalVars(e)}.flatten.toSet

    assert(vars subsetOf kc.varTypes.keys.toSet)

    // load the configuration, set defaults to undefined values
    def defaults(t:Type) = t match {
      case _:EnumType => Kconfig.tristateNo
      case NumberType => IntLiteral(0)
      case StringType => StringLiteral("")
    }
    val configFile = (getClass.getResource("../../../../2.6.30.config").getFile)
    val config = new collection.mutable.HashMap[String, Literal] 
    config ++= DotConfigParser.parseFile(configFile) mapValues { _.toExpression }

    val configWithDefaults = kc.varTypes.map{case (k, t)=>(k, defaults(t))}.toMap ++ config

    // test if all experssions are type correct
    exprs.par foreach { 
      case (n, e) =>
        ExpressionHelper.evaluateTypeCorrectExpression(e, configWithDefaults)
    }

    // test if every function is defined exactly once
    val funcs = mutable.Map[String, FunctionDef]()
    val rule = Expression.everywheretdNoDef(Rewriter.rule {
      case c:UserFunctionCall =>
        val x = c.func
        if (funcs.contains(x.name)) {
          assert(funcs(x.name) eq x, "two objects created: %s, %s" format (x, funcs(x.name)))
        }
        else
          funcs += x.name -> x
        c
    })
    exprs.unzip._2.foreach(Rewriter.rewrite(rule))

    // We should have something to compare the results of the
    // translator with those of the Kconfig. The below is the first
    // try to check with the effective value is the same as those
    // stored in the configuration. It did not pass.
    
    // for((x, effective) <- kc.varTypes.keys.map(x=>(x, kc.effective(x).toExpression)).par) {
    //   if (config.contains(x)) {
    //     println(x)
    //     ExpressionHelper.evaluateTypeCorrectExpression(effective, config withDefault defaults) should equal (config(x))
    //   }
    // }


  }



  def parseExconfig(resFile: String) = {
    val exconfigFile = (getClass.getResource(resFile).getFile)
    KConfigParser.parseKConfigFile(exconfigFile)
  }

  def parsePbFile(resFile: String) = {
    val extractFile = (getClass.getResource(resFile).getFile)
    val p = new ProtoParser
    p.parseKConfigFile(extractFile)
  }

  def loadAll(exconfig:String, configFile:String):(Kconfig2, Map[String, Literal]) = {
    val parsedFile = KConfigParser.parseKConfigFile(exconfig)
    val kc = new Kconfig2(parsedFile)


    val config = DotConfigParser.parseFile(configFile) mapValues { _.toExpression }
    (kc, config)
  }


  def expandAll(e:Expression, types:Expression.Types) = {
    val a = Rewriter.rewrite(Rewriter.everywherebu(Rewriter.rule{
      case u:UserFunctionCall => Rewriter.rewrite(Rewriter.everywherebu(Rewriter.rule{
        case x@IdentifierRef(id)  =>
          val index = u.func.params.unzip._1.indexOf(id)
          if (index < 0) x
          else u.args(index)
        }))(u.func.body)
    }))(e)
    val c = ExpressionHelper.simplifyWithReplacement(a, types)
    c
  }


  test("Test individual values") {
    val (kc, config) = loadAll("testfiles/kconfig/single.exconfig", "testfiles/kconfig/single.config")
    kc.varTypes.size should equal (1)
    expandAll(kc.effective("A").toExpression, kc.varTypes) should equal (IdentifierRef("A"))
    
  }

  test("Test full kconfig file") {
    val model = "src/test/resources/2.6.30.exconfig"
    val config = "src/test/resources/2.6.30.config"
    val featureID = "I7300_IDLE"
    val value = Kconfig.tristateYes
    val loader = new KconfigLoader(model, config)
    val manager = new KconfigManager(loader)
    val result = manager.setFeature(featureID, value)
    val fixes = result.fixes
    println(fixes)
    fixes.size should equal (1)
    fixes.head.units.size should equal (2)
    fixes.head.variables should equal (Set("X86_64", "I7300_IDLE"))    

  }

}
