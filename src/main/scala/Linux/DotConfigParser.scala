/*package ca.uwaterloo.gsd.rangeFix

import Kconfig._

object DotConfigParser {

  val PATTERN = """CONFIG_([a-zA-Z0-9_]+)=(.+)""".r
  val STRING_PATTERN = """"([^"]*)"""".r // \" ?

  def isInt(x: String): Boolean = try {
    Integer.parseInt(x)
    true
  } catch {
    case _ => false
  }

  def isHex(s: String) = try {
      val hasPrefix = s startsWith "0x"
      java.lang.Long.parseLong(s.substring(2), 16)
      hasPrefix
    } catch {
      case _ => false
    }

  def parseFile(file: String): Map[String,TLiteral] = {
    val source = scala.io.Source.fromFile(file)
    val result: Map[String, TLiteral] = Map() ++ {
      for (line <- source.getLines if line != "" && line(0) != '#') yield {
        line match {
          case PATTERN(name, value) =>
            name -> {
              value match {
                case "y" => TYes
                case "m" => TMod
                case "n" => TNo
                case x if isInt(x) => TInt(x.toInt)
                case x if isHex(x) => TInt(java.lang.Long.parseLong(x.substring(2), 16))
				case STRING_PATTERN(x) => TString(x)
                case x => sys.error("Unmatched: " + line) //TString(x)
              }
            }
          case _ => sys.error("Unmatched: " + line)
        }
      }
    }
    source.close
    result
  }

}
*/

package ca.uwaterloo.gsd.rangeFix
import scala.collection.mutable.LinkedHashMap
import Kconfig._

object DotConfigParser {

  val PATTERN = """CONFIG_([a-zA-Z0-9_]+)=(.+)""".r
  val STRING_PATTERN = """"([^"]*)"""".r

  def isInt(x: String): Boolean = try {
    Integer.parseInt(x)
    true
  } catch {
    case _ => false
  }

  def isHex(s: String): Boolean = s.splitAt(2) match {
    case ("0x", x) => util.Try(BigInt(x, 16)).isSuccess
    case _ => false
  }

  def parseFile(file: String): Map[String,TLiteral] = {
    val source = scala.io.Source.fromFile(file)
    val result: Map[String, TLiteral] = Map() ++ {
      for (line <- source.getLines if line != "" && line(0) != '#') yield {
	    
        line match {
          case PATTERN(name, value) =>
            name -> {
              value match {
                case "y" => TYes
                case "m" => TMod
                case "n" => TNo
                case x if isInt(x) => TInt(x.toInt)
                case x if isHex(x) =>
                TInt(java.lang.Long.parseLong(
                  x.substring(2, math.min(15, x.size)),
                  16
                ))
                case STRING_PATTERN(x) => TString(x)
                case x => sys.error("Unmatched: " + line) //TString(x)
              }
            }
          case _ => sys.error("Unmatched: " + line)
        }
      }
    }
    source.close
	
    result
  }
    def myParseFile(file: String): LinkedHashMap[String,TLiteral] = {
    val source = scala.io.Source.fromFile(file)
    var result= LinkedHashMap[String, TLiteral]()
      for (line <- source.getLines if line != "" && line(0) != '#'){
	    
        line match {
          case PATTERN(name, value) =>
		     result.put(name,  {
              value match {
                case "y" => TYes
                case "m" => TMod
                case "n" => TNo
                case x if isInt(x) => TInt(x.toInt)
                case x if isHex(x) => TInt(java.lang.Long.parseLong(x.substring(2), 16))
				case STRING_PATTERN(x) => TString(x)
                case x => sys.error("Unmatched: " + line) //TString(x)
              }
            })
          case _ => sys.error("Unmatched: " + line)
        }
      }
    
    source.close
	
    result
  }

}
