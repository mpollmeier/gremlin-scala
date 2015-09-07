package gremlin.scala

import scala.collection.JavaConversions._
import shapeless._

trait ScalaElement[ElementType <: Element] {
  def element: ElementType

  def graph: ScalaGraph[Graph] = element.graph

  def start(): GremlinScala[ElementType, HNil]

  def id: AnyRef = element.id

  def label: String = element.label

  def keys: Set[String] = element.keys.toSet

  def setProperty(key: String, value: Any): Any

  def property[A](key: String): Property[A] = element.property[A](key)

  def properties[A]: Stream[Property[A]]

  def properties[A](wantedKeys: String*): Stream[Property[A]]

  def valueMap[A]: Map[String, A] = valueMap[A](keys.toSeq: _*)

  def valueMap[A](wantedKeys: String*): Map[String, A] =
    (properties[A](wantedKeys: _*) map (p => (p.key, p.value))).toMap

  // note: this may throw an IllegalStateException - better use `value`
  def getValue[A](key: String): A = element.value[A](key)

  def value[A](key: String): Option[A] = {
    val p = property[A](key)
    if (p.isPresent) Some(p.value)
    else None
  }

  def valueOrElse[A](key: String, default: => A): A =
    property[A](key).orElse(default)

  def remove(): Unit = element.remove()
}
