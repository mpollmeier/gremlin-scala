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

  def property[A](key: schema.Key[A]): Property[A] = element.property[A](key.key)

  def properties[A: DefaultsToAny]: Stream[Property[A]]

  def properties[A: DefaultsToAny](keys: String*): Stream[Property[A]]

  // note: this may throw an IllegalStateException - better use `Property`
  def value[A: DefaultsToAny](key: String): A =
    element.value[A](key)

  // typesafe version of `value. have to call it `value2` because of a scala compiler bug :(
  // https://issues.scala-lang.org/browse/SI-9523
  def value2[A](key: schema.Key[A]): A =
    element.value[A](key.key)

  // note: this may throw an IllegalStateException - better use `Property`
  def values[A: DefaultsToAny](keys: String*): Iterator[A] =
    element.values[A](keys: _*)

  def valueMap[A: DefaultsToAny]: Map[String, A] = valueMap[A](keys.toSeq: _*)

  def valueMap[A: DefaultsToAny](keys: String*): Map[String, A] =
    (properties[A](keys: _*) map (p ⇒ (p.key, p.value))).toMap

}
