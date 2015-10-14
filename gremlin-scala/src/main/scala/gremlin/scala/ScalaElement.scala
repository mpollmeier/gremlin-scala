package gremlin.scala

import scala.collection.JavaConversions._
import shapeless._

trait ScalaElement[ElementType <: Element] {
  def element: ElementType

  def graph: ScalaGraph[Graph] = element.graph

  def start(): GremlinScala[ElementType, HNil]

  def id[A: DefaultsToAny]: A = element.id.asInstanceOf[A]

  def label: String = element.label

  def keys: Set[String] = element.keys.toSet

  def setProperty(key: String, value: Any): ElementType

  def removeProperty(key: String): ElementType

  def removeProperties(keys: String*): ElementType

  def property[A: DefaultsToAny](key: String): Property[A] = element.property[A](key)

  def properties[A: DefaultsToAny]: Stream[Property[A]]

  def properties[A: DefaultsToAny](keys: String*): Stream[Property[A]]

  def valueMap[A: DefaultsToAny]: Map[String, A] = valueMap[A](keys.toSeq: _*)

  def valueMap[A: DefaultsToAny](keys: String*): Map[String, A] =
    (properties[A](keys: _*) map (p ⇒ (p.key, p.value))).toMap

  // note: this may throw an IllegalStateException - better use `value`
  def getValue[A](key: String): A = element.value[A](key)

  def value[A](key: String): Option[A] = {
    val p = property[A](key)
    if (p.isPresent) Some(p.value)
    else None
  }

  def valueOrElse[A](key: String, default: ⇒ A): A =
    property[A](key).orElse(default)

  def remove(): Unit = element.remove()
}
