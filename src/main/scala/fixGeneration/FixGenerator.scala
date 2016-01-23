package ca.uwaterloo.gsd.rangeFix
import java.io._
import collection._

trait FixGenerator {
  def fixWithIgnorance(toFix: Int):Iterable[DataFix]
  def fixWithElimination(toFix:Int, toBeEnsuredConstraints:Set[Int]):Iterable[DataFix]
  def fix(toFix:Set[Int], toBeEnsuredConstraints:Set[Int], unchangeableVars: Set[String]):Iterable[DataFix]
  def fix(toFix:Int, toBeEnsuredConstraints:Set[Int], unchangeableVars: Set[String] = Set()):Iterable[DataFix] = fix(Set(toFix), toBeEnsuredConstraints, unchangeableVars)

}

object FixGenerator {

  def create(allConstraints: IndexedSeq[Expression],
             types: Expression.Types,
             config: Map[String, Literal]
           ):FixGenerator = {
    // println("7-%d" format System.currentTimeMillis)
    val translator = new Expression2SMT(allConstraints, config, types)
    // println("8-%d" format System.currentTimeMillis)
    val smtConstraints = allConstraints.map(translator.convert)
    // println("9-%d" format System.currentTimeMillis)
    val constraint2varsMap = mutable.Map[Int, Set[String]]()
    val var2ConstraintsMap = mutable.Map[String, Set[Int]]()
    val collector = new Expression.GlobalVarCollector()
    for (i <- 0 until allConstraints.size) {
      for (v <- collector.allGlobalVars(allConstraints(i))) {
        constraint2varsMap put (i, constraint2varsMap.getOrElse(i, Set()) + v)
        var2ConstraintsMap put (v, var2ConstraintsMap.getOrElse(v, Set()) + i)
      }
    }
    // println("12-%d" format System.currentTimeMillis)
    val typeCollector = new Expression.CollectorS({
      case l: Literal => ExpressionHelper.getType(l, Map())
    })
    val constraint2TypesMap: Map[Int, Set[Type]] =
      (0 until allConstraints.size).map(i =>
        (i, typeCollector collect allConstraints(i))).toMap
    // println("13-%d" format System.currentTimeMillis)
    val smtFunctions = Expression.collectl {
      case x:UserFunctionCall => x.func.name -> translator.convertFunctionDefine(x.func)
    } (allConstraints) toMap

    new FixGeneratorImpl(allConstraints,
                         types,
                         config,
                         translator,
                         smtConstraints,
                         constraint2varsMap,
                         var2ConstraintsMap,
                         constraint2TypesMap,
                         smtFunctions
                       )
  }

  def addEqualConstraint(f:FixGenerator, l:Literal, exprIndex:Int):(FixGenerator, Int) = {
    assert(l.isInstanceOf[IntLiteral] || l.isInstanceOf[BoolLiteral] || l.isInstanceOf[EnumLiteral],String.format("type: %s",l.toString))
    val fi = f.asInstanceOf[FixGeneratorImpl]
    val fg = new FixGeneratorImpl(
      fi.allConstraints :+ (l === fi.allConstraints(exprIndex)),
      fi.types,
      fi.config,
      fi.translator,
      fi.smtConstraints :+ (fi.translator.convert(l) === fi.smtConstraints(exprIndex)),
      fi.constraint2varsMap + (fi.allConstraints.size -> fi.constraint2varsMap.getOrElse(exprIndex, Set())),
      {
        val vars = fi.constraint2varsMap.getOrElse(exprIndex, Set())
        fi.var2ConstraintsMap.map {
          case (v, indexes) => (v, if (vars contains v) indexes + fi.allConstraints.size else indexes)
        }
      },
      fi.constraint2TypesMap + (fi.allConstraints.size -> fi.constraint2TypesMap.getOrElse(exprIndex, Set())),
      fi.smtFunctions
    )
    (fg, fi.allConstraints.size)
  }
  private class FixGeneratorImpl(
    val allConstraints: IndexedSeq[Expression],
    val types: Expression.Types,
    val config: Map[String, Literal],
    val translator:Expression2SMT,
    val smtConstraints:IndexedSeq[SMTExpression],
    val constraint2varsMap:Map[Int, Set[String]],
    val var2ConstraintsMap:Map[String, Set[Int]],
    val constraint2TypesMap:Map[Int, Set[Type]],
    val smtFunctions:Map[String, SMTFuncDefine]
  ) extends FixGenerator {

    private val printDetailedTime = false
    private def printDetailedTime[T](id: String)(func: => T): T = {
      if (printDetailedTime) Timer.printTime(id)(func) else func
    }

    override def fixWithIgnorance(toFix: Int) = fix(toFix, Set[Int]())
    override def fixWithElimination(toFix: Int, toBeEnsuredConstraints: Set[Int]) = {
      val modifiableVars = constraint2varsMap.getOrElse(toFix, Set())
      val relatedConstraints = modifiableVars.map(var2ConstraintsMap).flatten.toSet & (toBeEnsuredConstraints + toFix)
      val unchangeableVars = relatedConstraints.map(constraint2varsMap).flatten.toSet -- modifiableVars
      fixImpl(modifiableVars, relatedConstraints, unchangeableVars)
    }
    private def getRelatedConstraints(constraintIndexes: Set[Int], toBeEnsuredConstraints: Set[Int]): (Set[String], Set[Int]) = {
      val allToBeEnsuredConstraints = constraintIndexes ++ toBeEnsuredConstraints
      def expandVars(allVars: Set[String], allConstraints: Set[Int], newVars: Set[String]): (Set[String], Set[Int]) = {
        val relatedConstraints = newVars.map(var2ConstraintsMap).flatten.toSet
        val newConstraints = (relatedConstraints -- allConstraints) & allToBeEnsuredConstraints
        if (newConstraints.size > 0)
          expandConstraints(allVars ++ newVars, allConstraints, newConstraints)
        else
          (allVars ++ newVars, allConstraints)
      }
      def expandConstraints(allVars: Set[String], allConstraintIndexes: Set[Int], newConstraints: Set[Int]): (Set[String], Set[Int]) = {
        val relatedVars = newConstraints.map(constraint2varsMap.getOrElse(_, Set())).flatten.toSet
        val newVars = relatedVars -- allVars
        if (newVars.size > 0)
          expandVars(allVars, allConstraintIndexes ++ newConstraints, newVars)
        else
          (allVars, allConstraintIndexes ++ newConstraints)
      }
      expandConstraints(Set(), Set(), constraintIndexes)
    }

    override def fix(toFix: Set[Int], toBeEnsuredConstraints: Set[Int], unchangeableVars: Set[String]): Iterable[DataFix] = {
      val (slicedVars, slicedConstraintIndexes) = getRelatedConstraints(toFix, toBeEnsuredConstraints)
      val slicedChangeableVars = slicedVars -- unchangeableVars
      fixImpl(slicedChangeableVars, slicedConstraintIndexes, unchangeableVars)
    }


    private def fixImpl(changeableVars: Set[String], slicedConstraintIndexes: Set[Int], unchangeableVars: Set[String]): Iterable[DataFix] = {
      val (slicedConfig, slicedSMTConfig, slicedConstraints, slicedSMTConstraints, slicedSMTTypes, slicedSMTFuncsToDecl, slicedSMTTypesToDecl) = printDetailedTime("Slicing") {
        // var tmpConstraints:List[Expression] = List[Expression]()
        // println("unchangeableVars:%s" format(unchangeableVars))
        // slicedConstraintIndexes.foreach(v=> {
        //   println("%d:%s" format(v,allConstraints(v)))
        //   slicedConstraintIndexes.foreach(u=>{
        //     if (u!=v){
        //       print("compare %d, %d:" format(u,v))
        //       if (allConstraints(u)!=allConstraints(v))
        //         println("yes")
        //       else println("no")
        //     }
        //   })
        //   tmpConstraints = allConstraints(v)::tmpConstraints
        //   })
        // println("end")
       // val slicedConstraints = tmpConstraints
        val slicedConstraints = slicedConstraintIndexes.map(allConstraints)
        import org.kiama.rewriting.Rewriter._
        // def replaceVariables: Expression => Expression = rewrite {
        //   Expression.everywheretdNoDef(rule {
        //       case IdentifierRef(x) if unchangeableVars.contains(x) => config(x)
        //     })
        // }
        // val replacedConstraints = slicedConstraints.map(replaceVariables)
        
        // val replacedConstraints = rewrite(Expression.replaceVars { x =>
        //   if (unchangeableVars.contains(x)) Some(config(x))
        //   else None
        // })(slicedConstraints)
        // val slicedSMTConstraints:Iterable[SMTExpression] =
        //   if (unchangeableVars.size == 0)
        //     slicedConstraintIndexes.map(smtConstraints)
        //   else
        //     replacedConstraints.map(translator.convert)
	// val userFunctions = Expression.collectl {
        //     case x:UserFunctionCall => translator.convertFunctionDefine(x.func)
        // } (replacedConstraints) .reverse
        val unreplacedSMTConstraints =
          slicedConstraintIndexes.map(smtConstraints)

        val unreplacedUserFunctions = Expression.sortFuncDef(slicedConstraints).map(x => smtFunctions(x.name))
          // Expression.collectl {
        //   case x:FunctionDef => smtFunctions(x.name)
        // } (slicedConstraints) .reverse

        // assert each user function is defined only once
        assert(unreplacedUserFunctions.map(_.name).toSet.size == unreplacedUserFunctions.size)

        val replaceVarRule = Expression.replaceVars { x =>
          if (!changeableVars.contains(x)) Some(translator.convert(config(x)))
          else None
        }

        
        val (slicedSMTConstraints, userFunctions) =
          rewrite(replaceVarRule)((unreplacedSMTConstraints,
                                   unreplacedUserFunctions))
        
        // var userFunctions:Set[SMTFuncDefine] = Set[SMTFuncDefine]()
        // val ruleFunctionDef= rule {
        //   case x:FunctionDef => {
        //     val smtFunDef = translator.convertFunctionDefine(x)
        //     if (! (userFunctions contains smtFunDef) ) userFunctions +=  smtFunDef
        //     x
        //   }
        // }
        // val ruleCommonRule=rule{case x => x}
        // replacedConstraints.foreach(rewrite(MyRewriter.everywheretdWithGuard(ruleFunctionDef, ruleCommonRule)))

        //assert(Expression.collectGlobalVars(slicedSMTConstraints).toSet ++ Expression.collectGlobalVars(userFunctions).toSet ==changeableVars,
        //       "vars are not properly sliced. replacedConstraints: %s \n changeableVars %s".format(
        //          Expression.collectGlobalVars(slicedSMTConstraints).toSet ++ Expression.collectGlobalVars(userFunctions).toSet,
        //          changeableVars)
        //     )
        //assert(
        //  org.kiama.rewriting.Rewriter.collects {
        //    case x: SMTVarRef => x.id
        //  }(slicedSMTConstraints) ++
        //    userFunctions.map { 
        //    case SMTFuncDefine(name, params, retType, body) =>
        //      val paramNames = params.map(_._1).toSet
        //      org.kiama.rewriting.Rewriter.collects {
        //        case SMTVarRef(id) if ! paramNames.contains(id) => id
        //      }(body)
        //  }.flatten.toSet == changeableVars)
        if (printDetailedTime) print("sliced size " + (slicedSMTConstraints.size.toDouble / allConstraints.size.toDouble * 100.0) + "%--")
        val slicedConfig = config.filterKeys(changeableVars.contains)
        val slicedSMTConfig = slicedConfig.mapValues(translator.convert).asInstanceOf[Map[String, SMTLiteral]]
        val slicedTypes = types.filterKeys(changeableVars.contains)
        val (slicedSMTTypes, slicedSMTFuncsInVars) = translator.convertTypes(slicedTypes)
        val typesInExprPair =
          slicedConstraintIndexes.map(constraint2TypesMap).flatten
            .map(t => translator.type2SMTTypeAndFunc(t)).unzip
        val slicedSMTTypesToDecl = typesInExprPair._1 ++ slicedSMTTypes.values
        val slicedSMTFuncsToDecl:Seq[SMTFuncDefine] = (slicedSMTFuncsInVars ++ typesInExprPair._2.flatten).distinct
        assert(slicedSMTFuncsToDecl.map(_.name).toSet.size == slicedSMTFuncsToDecl.size)
        (slicedConfig, slicedSMTConfig, slicedConstraints, slicedSMTConstraints, slicedSMTTypes, slicedSMTFuncsToDecl++userFunctions.asInstanceOf[Iterable[Expression]], slicedSMTTypesToDecl)
      }
      val diagnoses = printDetailedTime("Generating diagnoses") {
        SMTFixGenerator.generateSimpleDiagnoses(
          slicedSMTConfig.asInstanceOf[Map[String, SMTLiteral]],
          changeableVars,
          slicedSMTConstraints.asInstanceOf[Iterable[SMTExpression]],
          slicedSMTTypes.asInstanceOf[Map[String, SMTType]],
          slicedSMTFuncsToDecl.asInstanceOf[Seq[SMTFuncDefine]],
          slicedSMTTypesToDecl.asInstanceOf[Set[SMTType]])
      }
      val fixes = printDetailedTime("Converting to fixes") {
        def getRelatedVars(cons:Expression):Set[String] = constraint2varsMap.getOrElse(allConstraints.indexOf(cons),Set())
        //SMTFixGenerator.simpleDiagnoses2Fixes(slicedConfig, slicedConstraints, types, diagnoses,getRelatedVars)
        SMTFixGenerator.simpleDiagnoses2Fixes(config.filterKeys(id=>unchangeableVars.contains(id)||changeableVars.contains(id)), slicedConstraints.asInstanceOf[Set[Expression]], types, diagnoses,getRelatedVars)
      }
      fixes
    }
  }
}

// class FixGenerator_old(allConstraints: IndexedSeq[Expression],
//   types: Expression.Types,
//   config: Map[String, Literal]) {
//   private var printDetailedTime = false
//   private def printDetailedTime[T](id: String)(func: => T): T = {
//     if (printDetailedTime) Timer.printTime(id)(func) else func
//   }

//   private val translator = new Expression2SMT(allConstraints, config, types)
//   private val smtConstraints = allConstraints.map(translator.convert)
//   private val constraint2varsMap = mutable.Map[Int, Set[String]]()
//   private val var2ConstraintsMap = mutable.Map[String, Set[Int]]()
//   for (i <- 0 until allConstraints.size) {
//     val vars = org.kiama.rewriting.Rewriter.collects { case IdentifierRef(id) => id }(allConstraints(i))
//     for (v <- vars) {
//       constraint2varsMap put (i, constraint2varsMap.getOrElse(i, Set()) + v)
//       var2ConstraintsMap put (v, var2ConstraintsMap.getOrElse(v, Set()) + i)
//     }
//   }
//   private def getTypeInExpr(c: Expression): Set[Type] = {
//     import org.kiama.rewriting.Rewriter._
//     val allTypes = mutable.Set[Type]()
//     rewrite(everywhere {
//       query {
//         case l: Literal => allTypes += ExpressionHelper.getType(l, Map())
//       }
//     })(c)
//     allTypes
//   }
//   private val constraint2TypesMap: Map[Int, Set[Type]] = (0 until allConstraints.size).map(i => (i, getTypeInExpr(allConstraints(i)))).toMap

//   def fixWithIgnorance(toFix: Int) = fix(toFix, Set())
//   def fixWithElimination(toFix: Int, toBeEnsuredConstraints: Set[Int]) = {
//     val modifiableVars = constraint2varsMap.getOrElse(toFix, Set())
//     val relatedConstraints = modifiableVars.map(var2ConstraintsMap).flatten.toSet & (toBeEnsuredConstraints + toFix)
//     import org.kiama.rewriting.Rewriter._
//     def replaceVariables: Expression => Expression = rewrite {
//       everywhere {
//         rule {
//           case IdentifierRef(x) if !modifiableVars.contains(x) => config(x)
//         }
//       }
//     }
//     val replacedConstraints = relatedConstraints.map(allConstraints).map(replaceVariables)
//     //**hs_begin**
//     val userFunctions = collects{case x:UserFunctionCall=>translator.convertFunctionDefine(x.func)}(replacedConstraints)
//     //**hs_end
//     assert(collects { case IdentifierRef(x) => x }(replacedConstraints) == modifiableVars)
//     val replacedSMTConstraints = replacedConstraints.map(translator.convert)
//     assert(collects { case x: SMTVarRef => x.id }(replacedSMTConstraints) == modifiableVars)
//     val slicedConfig = config.filterKeys(modifiableVars.contains)
//     val slicedSMTConfig = slicedConfig.mapValues(translator.convert).asInstanceOf[Map[String, SMTLiteral]]
//     val slicedTypes = types.filterKeys(modifiableVars.contains)
//     val (slicedSMTTypes, slicedSMTFuncsInVars) = translator.convertTypes(slicedTypes)
//     val typesInExprPair = relatedConstraints.map(constraint2TypesMap).flatten.map(t => translator.type2SMTTypeAndFunc(t)).unzip
//     val slicedSMTTypesToDecl = typesInExprPair._1 ++ slicedSMTTypes.values
//     val slicedSMTFuncsToDecl = slicedSMTFuncsInVars ++ typesInExprPair._2.flatten

//     val diagnoses =
//       SMTFixGenerator.generateSimpleDiagnoses(slicedSMTConfig, modifiableVars, replacedSMTConstraints, slicedSMTTypes, slicedSMTFuncsToDecl++userFunctions, slicedSMTTypesToDecl)

//     val fixes = SMTFixGenerator.simpleDiagnoses2Fixes(slicedConfig, replacedConstraints, diagnoses)
//     fixes
//   }

//   // return value 1: related variables
//   // return value 2: the indexes of related constarints
//   private def getRelatedConstraints(constraintIndexes: Set[Int], toBeEnsuredConstraints: Set[Int]): (Set[String], Set[Int]) = {
//     val allToBeEnsuredConstraints = constraintIndexes ++ toBeEnsuredConstraints
//     def expandVars(allVars: Set[String], allConstraints: Set[Int], newVars: Set[String]): (Set[String], Set[Int]) = {
//       val relatedConstraints = newVars.map(var2ConstraintsMap).flatten.toSet
//       val newConstraints = (relatedConstraints -- allConstraints) & allToBeEnsuredConstraints
//       if (newConstraints.size > 0)
//         expandConstraints(allVars ++ newVars, allConstraints, newConstraints)
//       else
//         (allVars ++ newVars, allConstraints)
//     }
//     def expandConstraints(allVars: Set[String], allConstraintIndexes: Set[Int], newConstraints: Set[Int]): (Set[String], Set[Int]) = {
//       val relatedVars = newConstraints.map(constraint2varsMap.getOrElse(_, Set())).flatten.toSet
//       val newVars = relatedVars -- allVars
//       if (newVars.size > 0)
//         expandVars(allVars, allConstraintIndexes ++ newConstraints, newVars)
//       else
//         (allVars, allConstraintIndexes ++ newConstraints)
//     }
//     expandConstraints(Set(), Set(), constraintIndexes)
//   }

//   def fix(toFix: Int, toBeEnsuredConstraints: Set[Int], unchangeableVars: Set[String] = Set()): Iterable[DataFix] = {
//     fix(Set(toFix), toBeEnsuredConstraints, unchangeableVars)
//   }

//   def fix(toFix: Set[Int], toBeEnsuredConstraints: Set[Int], unchangeableVars: Set[String]): Iterable[DataFix] = {
//     val (slicedVars, slicedConstraintIndexes) = getRelatedConstraints(toFix, toBeEnsuredConstraints)
//     val slicedChangeableVars = slicedVars -- unchangeableVars
//     fixImpl(slicedChangeableVars, slicedConstraintIndexes, unchangeableVars)
//   }

//   private def fixImpl(slicedVars: Set[String], slicedConstraintIndexes: Set[Int], unchangeableVars: Set[String]): Iterable[DataFix] = {
//     val (slicedConfig, slicedSMTConfig, slicedConstraints, slicedSMTConstraints, slicedSMTTypes, slicedSMTFuncsToDecl, slicedSMTTypesToDecl) = printDetailedTime("Slicing") {
//       val slicedConstraints = slicedConstraintIndexes.map(allConstraints)
//       import org.kiama.rewriting.Rewriter._
//       def replaceVariables: Expression => Expression = rewrite {
//         everywhere {
//           rule {
//             case IdentifierRef(x) if unchangeableVars.contains(x) => config(x)
//           }
//         }
//       }
//       val replacedConstraints = slicedConstraints.map(replaceVariables)
//       val slicedSMTConstraints =
//         if (unchangeableVars.size == 0)
//           slicedConstraintIndexes.map(smtConstraints)
//         else
//           replacedConstraints.map(translator.convert)
//       val userFunctions = 
// 	if (unchangeableVars.size==0)
// 	  collects{case x:UserFunctionCall=>translator.convertFunctionDefine(x.func)}(slicedConstraintIndexes.map(allConstraints))
// 	else
// 	  collects{case x:UserFunctionCall=>translator.convertFunctionDefine(x.func)}(replacedConstraints)
//       assert(org.kiama.rewriting.Rewriter.collects {
//         case x: IdentifierRef => x.id
//       }(replacedConstraints) == slicedVars)
//       assert(org.kiama.rewriting.Rewriter.collects {
//         case x: SMTVarRef => x.id
//       }(slicedSMTConstraints) == slicedVars)

//       if (printDetailedTime) print("sliced size " + (replacedConstraints.size.toDouble / allConstraints.size.toDouble * 100.0) + "%--")

//       val slicedConfig = config.filterKeys(slicedVars.contains)
//       val slicedSMTConfig = slicedConfig.mapValues(translator.convert).asInstanceOf[Map[String, SMTLiteral]]
//       val slicedTypes = types.filterKeys(slicedVars.contains)
//       val (slicedSMTTypes, slicedSMTFuncsInVars) = translator.convertTypes(slicedTypes)
//       val typesInExprPair = slicedConstraintIndexes.map(constraint2TypesMap).flatten.map(t => translator.type2SMTTypeAndFunc(t)).unzip
//       val slicedSMTTypesToDecl = typesInExprPair._1 ++ slicedSMTTypes.values
//       val slicedSMTFuncsToDecl = slicedSMTFuncsInVars ++ typesInExprPair._2.flatten
//       (slicedConfig, slicedSMTConfig, replacedConstraints, slicedSMTConstraints, slicedSMTTypes, slicedSMTFuncsToDecl++userFunctions, slicedSMTTypesToDecl)
//     }
//     val diagnoses = printDetailedTime("Generating diagnoses") {
//       SMTFixGenerator.generateSimpleDiagnoses(slicedSMTConfig, slicedVars, slicedSMTConstraints, slicedSMTTypes, slicedSMTFuncsToDecl, slicedSMTTypesToDecl)
//     }

//     val fixes = printDetailedTime("Converting to fixes") {
//       SMTFixGenerator.simpleDiagnoses2Fixes(slicedConfig, slicedConstraints, diagnoses)
//     }

//     fixes
//   }
// }
