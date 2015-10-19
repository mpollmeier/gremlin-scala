package gremlin.scala

import scala.collection.JavaConversions._
import shapeless._

trait ScalaElement[ElementType <: Element] {
  def element: ElementType

  def graph: ScalaGraph[Graph] = element.graph

  def start(): GremlinScala[ElementType, HNil]

  def id[A: DefaultsToAny]: A = element.id.asInstanceOf[A]

  def label: String = element.label

  def keys: Set[Key[Any]] = element.keys.toSet.map(Key.apply[Any])

  def setProperty[A](key: Key[A], value: A): ElementType

  def removeProperty(key: Key[_]): ElementType

  def removeProperties(keys: Key[_]*): ElementType

  def property[A](key: Key[A]): Property[A] = element.property[A](key.value)

  def properties[A: DefaultsToAny]: Stream[Property[A]]

  def properties[A: DefaultsToAny](keys: String*): Stream[Property[A]]

  // note: this may throw an IllegalStateException - better use `Property`
  def value[A: DefaultsToAny](key: String): A =
    element.value[A](key)

  // typesafe version of `value. have to call it `value2` because of a scala compiler bug :(
  // https://issues.scala-lang.org/browse/SI-9523
  def value2[A](key: Key[A]): A =
    element.value[A](key.value)

  // note: this may throw an IllegalStateException - better use `Property`
  def values[A: DefaultsToAny](keys: String*): Iterator[A] =
    element.values[A](keys: _*)

  def valueMap[A: DefaultsToAny]: Map[String, A] = valueMap[A](keys.toSeq.map(_.value): _*)

  def valueMap[A: DefaultsToAny](keys: String*): Map[String, A] =
    (properties[A](keys: _*) map (p ⇒ (p.key, p.value))).toMap

}
