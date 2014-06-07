package com.tinkerpop.gremlin.scala

import shapeless._
import ops.hlist._
import scala.collection.JavaConversions._
import com.tinkerpop.gremlin.structure._
import com.tinkerpop.gremlin.process.T

trait ScalaElement[ElementType <: Element] {
  def element: ElementType
  def start(): GremlinScala[ElementType :: HNil, ElementType]

  def id: AnyRef = element.id
  def label(): String = element.label

  def property[A](key: String): Property[A] = element.property[A](key)
  def keys(): Set[String] = element.keys.toSet
  def properties: Map[String, Any] = element.properties.toMap mapValues (_.value)
  def setProperty(key: String, value: Any): Unit = element.property(key, value)
  def setProperties(properties: Map[String, Any]): Unit =
    properties foreach { case (k,v) ⇒ setProperty(k,v) }
  def removeProperty(key: String): Unit = {
    val p = property(key)
    if(p.isPresent) p.remove
  }

  /** note: this may throw an IllegalStateException!
    * in scala exceptions are typically discouraged in situations like this...
    * `value` is only provided so that we are on par with Gremlin Groovy */
  def getValue[A](key: String): A = element.value[A](key)
  def getValueWithDefault[A](key: String, default: A): A = property[A](key).orElse(default)

  def remove(): Unit = element.remove()


  //duplicated from pipeline so that we can quickly start a pipeline from an element
  //TODO: add other steps

  def filter(p: ElementType ⇒ Boolean) = start().filter(p)
  def has(key: String) = start().has(key)
  def has(key: String, value: Any) = start().has(key, value)
  def has(key: String, t: T, value: Any) = start().has(key, t, value)
}

case class ScalaVertex(vertex: Vertex) extends ScalaElement[Vertex] {
  override def element = vertex

  def out() = GremlinScala[Vertex :: Vertex :: HNil, Vertex](vertex.out())
  def out(labels: String*) = GremlinScala[Vertex :: Vertex :: HNil, Vertex](vertex.out(labels: _*))
  def out(branchFactor: Int, labels: String*) = GremlinScala[Vertex :: Vertex :: HNil, Vertex](vertex.out(branchFactor, labels: _*))

  def outE() = GremlinScala[Vertex :: Edge :: HNil, Edge](vertex.outE())
  def outE(labels: String*) = GremlinScala[Vertex :: Edge :: HNil, Edge](vertex.outE(labels: _*))
  def outE(branchFactor: Int, labels: String*) = GremlinScala[Vertex :: Edge :: HNil, Edge](vertex.outE(branchFactor, labels: _*))

  def in() = GremlinScala[Vertex :: Vertex :: HNil, Vertex](vertex.in())
  def in(labels: String*) = GremlinScala[Vertex :: Vertex :: HNil, Vertex](vertex.in(labels: _*))
  def in(branchFactor: Int, labels: String*) = GremlinScala[Vertex :: Vertex :: HNil, Vertex](vertex.in(branchFactor, labels: _*))

  def inE() = GremlinScala[Vertex :: Edge :: HNil, Edge](vertex.inE())
  def inE(labels: String*) = GremlinScala[Vertex :: Edge :: HNil, Edge](vertex.inE(labels: _*))
  def inE(branchFactor: Int, labels: String*) = GremlinScala[Vertex :: Edge :: HNil, Edge](vertex.inE(branchFactor, labels: _*))

  def both() = GremlinScala[Vertex :: Vertex :: HNil, Vertex](vertex.both())
  def both(labels: String*) = GremlinScala[Vertex :: Vertex :: HNil, Vertex](vertex.both(labels: _*))
  def both(branchFactor: Int, labels: String*) = GremlinScala[Vertex :: Vertex :: HNil, Vertex](vertex.both(branchFactor, labels: _*))

  def bothE() = GremlinScala[Vertex :: Edge :: HNil, Edge](vertex.bothE())
  def bothE(labels: String*) = GremlinScala[Vertex :: Edge :: HNil, Edge](vertex.bothE(labels: _*))
  def bothE(branchFactor: Int, labels: String*) = GremlinScala[Vertex :: Edge :: HNil, Edge](vertex.bothE(branchFactor, labels: _*))

  def addEdge(label: String, inVertex: ScalaVertex, properties: Map[String, Any] = Map.empty): ScalaEdge = {
    val e = ScalaEdge(vertex.addEdge(label, inVertex.vertex))
    e.setProperties(properties)
    e
  }


  def start() = GremlinScala[Vertex :: HNil, Vertex](vertex.start)
}

case class ScalaEdge(edge: Edge) extends ScalaElement[Edge] {
  override def element = edge

  def start() = GremlinScala[Edge :: HNil, Edge](edge.start)

  //TODO: wait until this is consistent in T3 between Vertex and Edge
  //currently Vertex.outE returns a GraphTraversal, Edge.inV doesnt quite exist
  //def inV() = GremlinScala[Edge :: Vertex :: HNil, Vertex](edge.inV())
}

