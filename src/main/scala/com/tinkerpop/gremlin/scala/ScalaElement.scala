package com.tinkerpop.gremlin.scala

import scala.collection.JavaConversions._

import com.tinkerpop.gremlin.process.T
import com.tinkerpop.gremlin.process.Traverser
import com.tinkerpop.gremlin.structure._
import shapeless._

trait ScalaElement[ElementType <: Element] {
  def element: ElementType
  def start(): GremlinScala[HNil, ElementType]

  def id: AnyRef = element.id
  def label(): String = element.label

  def keys(): Set[String] = element.keys.toSet
  def hiddenKeys: Set[String] = element.hiddenKeys.toSet

  def setProperty(key: String, value: Any): Unit = element.property(key, value)
  def setHiddenProperty(key: String, value: Any): Unit = element.property(Graph.Key.hide(key), value)
  def setProperties(properties: Map[String, Any]): Unit =
    properties foreach { case (k, v) ⇒ setProperty(k, v) }

  def removeProperty(key: String): Unit = {
    val p = property(key)
    if (p.isPresent) p.remove
  }

  def property[A](key: String): Property[A] = element.property[A](key)
  def hiddenProperty[A](key: String): Property[A] = element.property[A](Graph.Key.hide(key))

  def properties(wantedKeys: String*): Seq[Property[Any]] = {
    val requiredKeys = if(!wantedKeys.isEmpty) wantedKeys else keys
    requiredKeys map property[Any] toSeq
  }

  def hiddenProperties(wantedKeys: String*): Seq[Property[Any]] = {
    val requiredKeys = if(!wantedKeys.isEmpty) wantedKeys else hiddenKeys
    requiredKeys map hiddenProperty[Any] toSeq
  }

  def propertyMap(wantedKeys: String*): Map[String, Any] = {
    val requiredKeys = if(!wantedKeys.isEmpty) wantedKeys else keys
    requiredKeys map { key ⇒ (key, value(key)) } toMap
  }

  def hiddenPropertyMap(wantedKeys: String*): Map[String, Any] = {
    val requiredKeys = if(!wantedKeys.isEmpty) wantedKeys else hiddenKeys
    requiredKeys map { key ⇒ (key, hiddenValue(key)) } toMap
  }

  /**
   * note: this may throw an IllegalStateException!
   * in scala exceptions are typically discouraged in situations like this...
   * `value` is only provided so that we are on par with Gremlin Groovy
   */
  def value[A](key: String): A = element.value[A](key)
  def hiddenValue[A](key: String): A = element.value[A](Graph.Key.hide(key))

  def valueMap(): Map[String, Any] =
    keys map { key ⇒ (key, value(key)) } toMap

  def valueWithDefault[A](key: String, default: A): A = property[A](key).orElse(default)

  def remove(): Unit = element.remove()
}

case class ScalaVertex(vertex: Vertex) extends ScalaElement[Vertex] {
  override def element = vertex

  def out() = GremlinScala[HNil, Vertex](vertex.out())
  def out(labels: String*) = GremlinScala[HNil, Vertex](vertex.out(labels: _*))
  def out(branchFactor: Int, labels: String*) = GremlinScala[HNil, Vertex](vertex.out(branchFactor, labels: _*))

  def outE() = GremlinScala[HNil, Edge](vertex.outE())
  def outE(labels: String*) = GremlinScala[HNil, Edge](vertex.outE(labels: _*))
  def outE(branchFactor: Int, labels: String*) = GremlinScala[HNil, Edge](vertex.outE(branchFactor, labels: _*))

  def in() = GremlinScala[HNil, Vertex](vertex.in())
  def in(labels: String*) = GremlinScala[HNil, Vertex](vertex.in(labels: _*))
  def in(branchFactor: Int, labels: String*) = GremlinScala[HNil, Vertex](vertex.in(branchFactor, labels: _*))

  def inE() = GremlinScala[HNil, Edge](vertex.inE())
  def inE(labels: String*) = GremlinScala[HNil, Edge](vertex.inE(labels: _*))
  def inE(branchFactor: Int, labels: String*) = GremlinScala[HNil, Edge](vertex.inE(branchFactor, labels: _*))

  def both() = GremlinScala[HNil, Vertex](vertex.both())
  def both(labels: String*) = GremlinScala[HNil, Vertex](vertex.both(labels: _*))
  def both(branchFactor: Int, labels: String*) = GremlinScala[HNil, Vertex](vertex.both(branchFactor, labels: _*))

  def bothE() = GremlinScala[HNil, Edge](vertex.bothE())
  def bothE(labels: String*) = GremlinScala[HNil, Edge](vertex.bothE(labels: _*))
  def bothE(branchFactor: Int, labels: String*) = GremlinScala[HNil, Edge](vertex.bothE(branchFactor, labels: _*))

  def addEdge(label: String, inVertex: ScalaVertex, properties: Map[String, Any]): ScalaEdge = {
    val e = ScalaEdge(vertex.addEdge(label, inVertex.vertex))
    e.setProperties(properties)
    e
  }

  def addEdge(id: AnyRef, label: String, inVertex: ScalaVertex, properties: Map[String, Any]): ScalaEdge = {
    val e = ScalaEdge(vertex.addEdge(label, inVertex.vertex, T.id, id))
    e.setProperties(properties)
    e
  }

  def `with`[A <: AnyRef, B <: AnyRef](tuples: (A, B)*) = start.`with`(tuples: _*)

  def start() = GremlinScala[HNil, Vertex](vertex.start)
}

case class ScalaEdge(edge: Edge) extends ScalaElement[Edge] {
  override def element = edge

  def `with`[A <: AnyRef, B <: AnyRef](tuples: (A, B)*) = start.`with`(tuples: _*)

  def start() = GremlinScala[HNil, Edge](edge.start)

  //TODO: wait until this is consistent in T3 between Vertex and Edge
  //currently Vertex.outE returns a GraphTraversal, Edge.inV doesnt quite exist
  //def inV() = GremlinScala[HNil, Vertex](edge.inV())
}

