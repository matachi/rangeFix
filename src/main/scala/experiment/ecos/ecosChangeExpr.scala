package ca.uwaterloo.gsd.rangeFix

import util.parsing.input.PagedSeqReader
import collection.immutable.PagedSeq
import util.parsing.combinator._
import scala.util.Random._
import collection.mutable.Stack

/**
 * Created by IntelliJ IDEA.
 * User: ahubaux
 * Date: 13/09/11
 * Time: 17:56
 * To change this template use File | Settings | File Templates.
 */

/*
 * Type declaration
 */

abstract class ecosChangeExpr
// BASE TYPES
case class eLit (st : String) extends ecosChangeExpr
case class eBooldata (bd : String) extends ecosChangeExpr // bd will be either 0 or 1
case class eDecision (st1 : ecosChangeExpr, st2: ecosChangeExpr) extends ecosChangeExpr
case class eChange (label: ecosChangeExpr, decision: ecosChangeExpr) extends ecosChangeExpr
case class eSkip () extends ecosChangeExpr
case class eChanges (changes: List[ecosChangeExpr]) extends ecosChangeExpr

class Change(fName: String, c1: String, c2: String) extends Serializable {
  import java.text.{NumberFormat, ParsePosition}

  val feature = fName
  val change1 = c1
  val change2 = c2

  override def toString = "[Feature: "+feature+" - Changes: "+change1+" ; "+change2+"]"
  
  // taken from: http://rosettacode.org/wiki/Determine_if_a_string_is_numeric#Scala
  private def isNumeric(input: String): Boolean = {
    val formatter = NumberFormat.getInstance
    val pos = new ParsePosition(0)
    formatter.parse(input, pos)
    input.length == pos.getIndex // valid if parse position is at end of string
  }
  
  private def checkString(input: String): String = {
    if(input.startsWith("\"")) {
      input.drop(1)
    }
    if(input.endsWith("\"")) {
      input.dropRight(1)
    }
    input
  }


  private def encodeOptionValue(input: String): SingleOptionValue = {
    if(isNumeric(input)) {
      new IntOptionValue(input.toInt)
    } else if (input.startsWith("0x")) {
      new IntOptionValue(java.lang.Long.parseLong(input.substring(2), 16))
    } else {
      new StringOptionValue(checkString(input))
    }
  }
  
  def getOptionValue():OptionValue = {
      if(change1!=null && change2!=null){
        // this is a booldata
        new DoubleOptionValue(IntOptionValue(change1.toInt),encodeOptionValue(change2))
      } 
	  else {
		assert(change1!=null)
        encodeOptionValue(change1)
      }
  }

}

class ECOSChangeHandler {

  object ECOSChangeParser extends JavaTokenParsers with ImplicitConversions{
    var changesBottomUp : Stack[Change] = new Stack()


      /*
       * Pseudo-BNF grammar (whitespaces and tabulations are ignored here for simplicity)
       *
       * eol = \n | \r\n
       * lit = [_0-9a-zA-Z-,:'`=!/&.+*|?<>()@$%#\^\[\]\\\"]*
       * label = [_0-9a-zA-Z]*
       * booldata = 0|1
       * emptyString = \"\"
       *
       * changelist ::= line*
       *
       * line ::=    change
       *           | eol
       *
       * change ::= label eol oldval eol newval eol
       *
       * oldval ::= ">" decision
       * newval ::= "<" decision
       *
       * decison ::=    "#" lit
       *              | "user_value" lit
       *              | "user_value" booldata lit
       *              | "inferred_value" lit
       *              | "inferred_value" booldata lit
       *
       */

      override protected val whiteSpace = """[ \t]+""".r
      lazy val eol = "\n" | "\r\n"    // carriage return or line feed
      lazy val lit = ("""[ _0-9a-zA-Z-;,:'`=!/&.{}+*|?<>()@$%#\^\[\]\\\"]*""").r ^^ eLit
      lazy val label = ("""[_0-9a-zA-Z]*""").r ^^ eLit
      lazy val booldata = ("""[0][ ]""").r ^^ eBooldata | ("""[1][ ]""").r ^^ eBooldata
     // lazy val ws = ("""[ ]""").r ^^^ eSkip()

      lazy val changeList = rep(line) ^^ eChanges

      lazy val line: Parser[ecosChangeExpr] = change |
                                              eol ^^^ eSkip()

      lazy val change: Parser[ecosChangeExpr] = label~eol~oldval~eol~newval<~eol ^^ { case l~ee~o~eee~n => eChange(l,n) }

      lazy val oldval: Parser[ecosChangeExpr] = ">" ~> decision
      lazy val newval: Parser[ecosChangeExpr] = "<" ~> decision

      lazy val decision: Parser[ecosChangeExpr] =
        "#"~>lit ^^ { case v => eDecision(null,null) } |
        "user_value" ~> booldata ~ lit ^^ { case bd~v => eDecision(bd,v) } |
        "user_value" ~> lit ^^ { case v => eDecision(v,null) } |
        "inferred_value"~> booldata ~ lit ^^ { case bd~v => eDecision(bd,v) } |
        "inferred_value" ~>lit ^^ { case v => eDecision(v,null) }

    /*
     * PARSING FUNCTIONS
     */

    def parseString(input : String) =
      parseAll(changeList, input) match {
        case Success(result,_) => result
        case x => sys.error(x.toString)
      }

    def parseFile(file : String) =
      parseAll(changeList, new PagedSeqReader(PagedSeq fromFile file)) match {
        case Success(result,_) => result ; //println("SUCCESS \t")
        case x => println("ERROR"); sys.error(x.toString)
      }

    /*
     * EVALUATION FUNCTION
     */



    private def eval(cList: ecosChangeExpr) : List[String] = {
      var changeList: List[String] = Nil
      cList match {
          case eLit(content) => if(content.trim.length>0) content.trim() :: Nil else null
          case eBooldata(content) => content.trim() :: Nil
          case eDecision(st1,st2)  =>
            var d1 = eval(st1)
            var d2 = eval(st2)
            if(d1!=null) {
              if (d2!=null)  {
                changeList = d2.head :: changeList
              }
              changeList = d1.head :: changeList
            } else {
              changeList = Nil
            }
            changeList
          case eChange(label,dec) =>
            var feature = eval(label).head
            var decisions = eval(dec)
            if (decisions.length==1){
              var change = new Change(feature,decisions.head,null)
              changesBottomUp.push(change)
            } else if (decisions.length==2) {
              var change = new Change(feature,decisions(0),decisions(1))
              changesBottomUp.push(change)
            }
            null
          case eChanges(changeList) =>  for (c <- changeList) eval(c) ; null
          case _ => null
        }
    }

    def getChangesFromFile(file: String): List[Array[Change]] = {
      var changesArray: List[Array[Change]] = Nil
      val result = parseFile(file)
      eval(result)

      var changesBottomUpArray = new Array[Change](changesBottomUp.length)
      var changesTopDownArray = new Array[Change](changesBottomUp.length)
      var changesRandomArray  = new Array[Change](changesBottomUp.length)
      var i = 0
      var j = changesBottomUp.length-1
      for (c <- changesBottomUp) {
        changesBottomUpArray(i) = c
        changesTopDownArray(j) = c
        changesRandomArray(i) = c
        i = i + 1
        j = j - 1
      }

      changesRandomArray = shuffle(changesRandomArray)

      changesArray = changesRandomArray :: changesArray
      changesArray = changesBottomUpArray :: changesArray
      changesArray = changesTopDownArray :: changesArray

      changesArray
    }

    // Fisher-Yates shuffle, see: http://en.wikipedia.org/wiki/Fisher–Yates_shuffle
    def shuffle[T](array: Array[T]): Array[T] = {
        val rnd = new java.util.Random(array.size)
        for (n <- Iterator.range(array.length - 1, 0, -1)) {
                val k = rnd.nextInt(n + 1)
                val t = array(k); array(k) = array(n); array(n) = t
        }
        return array
    }
  }
}