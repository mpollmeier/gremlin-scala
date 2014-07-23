package com.tinkerpop.gremlin.scala

import com.tinkerpop.blueprints.Element
import java.util.{ Set ⇒ JSet }
import scala.language.dynamics
import scala.reflect.ClassTag
import scala.annotation.implicitNotFound

trait ScalaElement extends Element with Dynamic {
  def element: Element

  def apply(key: String): Any = getProperty(key)
  def selectDynamic(key: String): Any = getProperty(key)

  /** returns Some[A] if element present and of type A, otherwise None */
  @implicitNotFound( msg = "No ClassTag available for ${A}" )
  def property[A: ClassTag](name: String): Option[A] = {
    val value: A = element.getProperty(name)
    value match {
      case value: A ⇒ Option(value)
      case other    ⇒ None
    }
  }

  def id: Any = getId

  /** need to extend Element so that we can use existing Gremlin Pipes... */
  def getId(): Object = element.getId
  def getProperty[T](key: String): T = element.getProperty(key)
  def getPropertyKeys: java.util.Set[String] = element.getPropertyKeys
  def remove(): Unit = element.remove()
  def removeProperty[T](key: String): T = element.removeProperty(key)
  def setProperty(key: String, value: Any): Unit = element.setProperty(key, value)

  override def toString = element.toString
  override def hashCode: Int = id.hashCode
  override def equals(other: Any): Boolean = other match {
    case other: Element ⇒ this.id == other.getId
    case _              ⇒ false
  }
}
