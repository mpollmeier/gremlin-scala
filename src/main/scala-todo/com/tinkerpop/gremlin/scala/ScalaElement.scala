package com.tinkerpop.gremlin.scala

import com.tinkerpop.blueprints.Element
import java.util.{ Set ⇒ JSet }
import scala.reflect.ClassTag

  /** returns null if property not set*/

  /** returns Some[A] if element present and of type A, otherwise None */
  def propertyOption[A: ClassTag](name: String): Option[A] = {
    val value: A = element.getProperty(name)
    value match {
      case value: A ⇒ Option(value)
      case other    ⇒ None
    }
  }


  override def toString = element.toString
  override def hashCode: Int = id.hashCode
  override def equals(other: Any): Boolean = other match {
    case other: Element ⇒ this.id == other.getId
    case _              ⇒ false
  }
}

