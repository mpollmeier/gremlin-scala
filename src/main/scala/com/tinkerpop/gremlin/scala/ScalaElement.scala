package com.tinkerpop.gremlin.scala

import com.tinkerpop.blueprints.Element
import java.util.{ Map ⇒ JMap }

abstract class ScalaElement(val element: Element) {

  def get[T](key: String): Option[T] = Option(element.getProperty(key).asInstanceOf[T])

  def id: Any = element.getId

  def property(name: String): Option[Any] = Option(element.getProperty(name))
  def apply(key: String): Any = element.getProperty(key)
}
