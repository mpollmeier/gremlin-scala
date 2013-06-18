package com.tinkerpop.gremlin.scala.pipes

import scala.collection.JavaConversions._
import scala.collection.mutable
import com.tinkerpop.blueprints.Element
import com.tinkerpop.pipes.AbstractPipe
import com.tinkerpop.pipes.transform.TransformPipe

class PropertyMapPipe[S <: Element](keys: String*)
    extends AbstractPipe[S, Map[String, Any]]
    with TransformPipe[S, Map[String, Any]] {

  def processNextStart(): Map[String, Any] = {
    val element = starts.next
    def propertyMap(keys: mutable.Set[String]) = keys.map { key ⇒ (key, element.getProperty(key)) }.toMap

    keys match {
      case Nil ⇒ propertyMap(element.getPropertyKeys)
      case keys ⇒
        val set = keys.toSet
        propertyMap(element.getPropertyKeys.filter(set.contains))
    }

  }
}