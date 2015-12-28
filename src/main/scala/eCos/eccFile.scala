package ca.uwaterloo.gsd.rangeFix
import scala.collection.JavaConversions._
import scala.collection._
import java.io._

case class PackageRef(name:String, ver:String)

case class EccFile(packages:Iterable[PackageRef], values:Map[String, OptionValue]) {
	def this(packages:java.util.Collection[PackageRef], values:java.util.Map[String, OptionValue]) =
		this(collectionAsScalaIterable(packages), mapAsScalaMap(values))
	
}

trait OptionValue
trait SingleOptionValue extends OptionValue
case class IntOptionValue(v:Long) extends SingleOptionValue
case class RealOptionValue(v:Double) extends SingleOptionValue
case class StringOptionValue(v:String) extends SingleOptionValue
case object NoneOptionValue extends OptionValue
case class DoubleOptionValue(bool:IntOptionValue, data:SingleOptionValue) extends OptionValue

// type corrected configuration

abstract class ConfigValue extends Serializable {
	def save(o:ObjectOutputStream) = {
		this match {
			case SingleConfigValue(l) => 
				o.writeInt(0)
				o.writeObject(l)
			case DoubleConfigValue(b, l) => 
				o.writeInt(1)
				o.writeObject(b)
				o.writeObject(l)
			case NoneConfigValue => o.writeInt(2)
		}
	}
}
object ConfigValue {
	def load(i:ObjectInputStream):ConfigValue = {
		i.readInt match {
			case 0 => SingleConfigValue(i.readObject.asInstanceOf[Literal])
			case 1 => 
				val b = i.readObject().asInstanceOf[BoolLiteral]
				val l = i.readObject().asInstanceOf[Literal]
				DoubleConfigValue(b, l)
			case 2 => 
				NoneConfigValue
		}
	}
}
case class SingleConfigValue(l:Literal) extends ConfigValue
case class DoubleConfigValue(b:BoolLiteral, l:Literal) extends ConfigValue
case object NoneConfigValue extends ConfigValue