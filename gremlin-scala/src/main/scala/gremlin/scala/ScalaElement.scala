package gremlin.scala

import scala.collection.JavaConversions._
import shapeless._

trait ScalaElement[ElementType <: Element] {
  def element: ElementType

  def start(): GremlinScala[ElementType, HNil]

  def id: AnyRef = element.id

  def label: String = element.label

  def keys: Set[String] = element.keys.toSet

  def property[A](key: String): Property[A] = element.property[A](key)

  def properties(wantedKeys: String*): Seq[Property[Any]] = {
    val requiredKeys = if (wantedKeys.nonEmpty) wantedKeys else keys
    requiredKeys map property[Any]
  }.toSeq

  def propertyMap(wantedKeys: String*): Map[String, Any] = {
    val requiredKeys = if (wantedKeys.nonEmpty) wantedKeys else keys
    requiredKeys map { key => (key, getValue(key)) }
  }.toMap

  // note: this may throw an IllegalStateException - better use `value`
  def getValue[A](key: String): A = element.value[A](key)

  def value[A](key: String): Option[A] = {
    val p = property[A](key)
    if (p.isPresent) Some(p.value)
    else None
  }

  def valueMap(): Map[String, Any] = (keys map { key => (key, getValue(key)) }).toMap

  def valueOrElse[A](key: String, default: => A): A = property[A](key).orElse(default)

  def remove(): Unit = element.remove()
}
