package com.tinkerpop.gremlin.scala

import com.tinkerpop.blueprints.Element
import java.util.{ Map ⇒ JMap }
import scala.language.dynamics
import scala.reflect.runtime.universe._
import scala.reflect.runtime.{ universe ⇒ ru }
import scala.reflect.ClassTag
import scala.reflect.classTag

abstract class ScalaElement(val element: Element) extends Dynamic {
  def id: Any = element.getId

  def apply(key: String): Any = element.getProperty(key)
  def selectDynamic(key: String): Any = apply(key)

  /**
   * returns Some[A] if element present and of type A, otherwise None
   */
  def property[A: ClassTag](name: String): Option[A] = {
    val value: A = element.getProperty(name)
    value match {
      case value: A ⇒ Option(value)
      case other    ⇒ None
    }
  }

  override def toString = element.toString

}
