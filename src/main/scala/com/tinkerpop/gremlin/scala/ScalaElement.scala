package com.tinkerpop.gremlin.scala

import com.tinkerpop.blueprints.Element
import java.util.{ Map ⇒ JMap }

abstract class ScalaElement(val element: Element) {

  def get[T](key: String): Option[T] = Option(element.getProperty(key).asInstanceOf[T])

  def id: Any = element.getId

  //TODO remove getProperty, rename property to getProperty
  def getProperty(key: String): AnyRef = element.getProperty(key)
  def apply(key: String): AnyRef = getProperty(key)
}
