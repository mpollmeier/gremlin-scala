package com.tinkerpop.gremlin.scala

import com.tinkerpop.blueprints.Element
import java.util.{ Set ⇒ JSet }
import scala.language.dynamics
import scala.reflect.ClassTag

abstract class ScalaElement(val element: Element) extends Element with Dynamic {
  def id: Any = getId

  def apply(key: String): Any = getProperty(key)
  def selectDynamic(key: String): Any = getProperty(key)

  /** returns Some[A] if element present and of type A, otherwise None */
  def property[A: ClassTag](name: String): Option[A] = {
    val value: A = element.getProperty(name)
    value match {
      case value: A ⇒ Option(value)
      case other    ⇒ None
    }
  }

  override def toString = element.toString

  /** need to implement Element so that we can use existing Gremlin Pipes... */
  def getId(): Object = element.getId
  def getProperty[T](key: String): T = element.getProperty(key)
  def getPropertyKeys: java.util.Set[String] = element.getPropertyKeys
  def remove(): Unit = element.remove()
  def removeProperty[T](key: String): T = element.removeProperty(key)
  def setProperty(key: String, value: Any): Unit = element.setProperty(key, value)
}
