package com.tinkerpop.gremlin.scala

import scala.collection.JavaConversions._

import com.tinkerpop.gremlin.process.T
import com.tinkerpop.gremlin.process.Traverser
import com.tinkerpop.gremlin.structure._
import shapeless._

trait ScalaElement[ElementType <: Element] {
  def element: ElementType
  def start(): GremlinScala[ElementType :: HNil, ElementType]

  def id: AnyRef = element.id
  def label(): String = element.label

  def keys(): Set[String] = element.keys.toSet

  def property[A](key: String): Property[A] = element.property[A](key)

  def properties(keys: String*): Seq[Property[Any]] = 
    element.iterators
      .properties[Any](keys: _*)
      .asInstanceOf[java.util.Iterator[Property[Any]]]
      .toSeq

  def propertyMap(): Map[String, Any] =
    keys map { key ⇒ (key, value(key)) } toMap

  def setProperty(key: String, value: Any): Unit = element.property(key, value)
  def setProperties(properties: Map[String, Any]): Unit =
    properties foreach { case (k, v) ⇒ setProperty(k, v) }
  def removeProperty(key: String): Unit = {
    val p = property(key)
    if (p.isPresent) p.remove
  }

  def setHiddenProperty(key: String, value: Any): Unit = element.property(Graph.Key.hide(key), value)

  def hiddenProperties(keys: String*): Seq[Property[Any]] = 
    element.iterators
      .hiddens[Any](keys: _*)
      .asInstanceOf[java.util.Iterator[Property[Any]]]
      .toSeq

  def hiddenKeys: Set[String] = element.hiddenKeys.toSet

  /**
   * note: this may throw an IllegalStateException!
   * in scala exceptions are typically discouraged in situations like this...
   * `value` is only provided so that we are on par with Gremlin Groovy
   */
  def value[A](key: String): A = element.value[A](key)
  def valueMap(): Map[String, Any] =
    keys map { key ⇒ (key, value(key)) } toMap

  def valueWithDefault[A](key: String, default: A): A = property[A](key).orElse(default)

  def remove(): Unit = element.remove()

  //duplicated from pipeline so that we can quickly start a pipeline from an element
  //TODO: can we do the same with an automatic conversion?
  //TODO: add other steps

  def filter(p: ElementType ⇒ Boolean) = start.filter(p)
  def has(key: String) = start.has(key)
  def has(key: String, value: Any) = start.has(key, value)
  def has(key: String, t: T, value: Any) = start.has(key, t, value)
  def hasNot(key: String) = start.hasNot(key)
  def as(name: String) = start.as(name)
  def mapWithTraverser[A](fun: Traverser[ElementType] ⇒ A) = start.mapWithTraverser(fun)
  def map[A](fun: ElementType ⇒ A) = start.map(fun)
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

  def addEdge(label: String, inVertex: ScalaVertex, properties: Map[String, Any]): ScalaEdge = {
    val e = ScalaEdge(vertex.addEdge(label, inVertex.vertex))
    e.setProperties(properties)
    e
  }

  def addEdge(id: AnyRef, label: String, inVertex: ScalaVertex, properties: Map[String, Any]): ScalaEdge = {
    val e = ScalaEdge(vertex.addEdge(label, inVertex.vertex, Element.ID, id))
    e.setProperties(properties)
    e
  }

  def `with`[A <: AnyRef, B <: AnyRef](tuples: (A, B)*) = start.`with`(tuples: _*)

  def start() = GremlinScala[Vertex :: HNil, Vertex](vertex.start)
}

case class ScalaEdge(edge: Edge) extends ScalaElement[Edge] {
  override def element = edge

  def `with`[A <: AnyRef, B <: AnyRef](tuples: (A, B)*) = start.`with`(tuples: _*)

  def start() = GremlinScala[Edge :: HNil, Edge](edge.start)

  //TODO: wait until this is consistent in T3 between Vertex and Edge
  //currently Vertex.outE returns a GraphTraversal, Edge.inV doesnt quite exist
  //def inV() = GremlinScala[Edge :: Vertex :: HNil, Vertex](edge.inV())
}

