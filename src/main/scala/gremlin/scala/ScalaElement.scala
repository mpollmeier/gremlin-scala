package gremlin.scala

import scala.collection.JavaConversions._
import org.apache.tinkerpop.gremlin.structure.T
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__
import shapeless._
import scala.language.postfixOps

trait ScalaElement[ElementType <: Element] {
  def element: ElementType
  def start(): GremlinScala[ElementType, HNil]

  def id: AnyRef = element.id
  def label(): String = element.label

  def keys(): Set[String] = element.keys.toSet

  def property[A](key: String): Property[A] = element.property[A](key)

  def properties(wantedKeys: String*): Seq[Property[Any]] = {
    val requiredKeys = if(!wantedKeys.isEmpty) wantedKeys else keys
    requiredKeys map property[Any] toSeq
  }

  def propertyMap(wantedKeys: String*): Map[String, Any] = {
    val requiredKeys = if(!wantedKeys.isEmpty) wantedKeys else keys
    requiredKeys map { key ⇒ (key, getValue(key)) } toMap
  }

  // note: this may throw an IllegalStateException - better use `value`
  def getValue[A](key: String): A = element.value[A](key)

  def value[A](key: String): Option[A] = {
    val p = property[A](key)
    if (p.isPresent) Some(p.value)
    else None
  }

  def valueMap(): Map[String, Any] =
    keys map { key ⇒ (key, getValue(key)) } toMap

  def valueOrElse[A](key: String, default: ⇒ A): A = property[A](key).orElse(default)

  def remove(): Unit = element.remove()
}

case class ScalaVertex(vertex: Vertex) extends ScalaElement[Vertex] {
  override def element = vertex

  def setProperty(key: String, value: Any): ScalaVertex = {
    element.property(key, value)
    this
  }

  def setProperties(properties: Map[String, Any]): ScalaVertex = {
    properties foreach { case (k, v) ⇒ setProperty(k, v) }
    this
  }

  def removeProperty(key: String): ScalaVertex = {
    val p = property(key)
    if (p.isPresent) p.remove
    this
  }

  def out() = start.out()
  def out(labels: String*) = start.out(labels: _*)

  def outE() = start.outE()
  def outE(labels: String*) = start.outE(labels: _*)

  def in() = start.in()
  def in(labels: String*) = start.in(labels: _*)

  def inE() = start.inE()
  def inE(labels: String*) = start.inE(labels: _*)

  def both() = start.both()
  def both(labels: String*) = start.both(labels: _*)

  def bothE() = start.bothE()
  def bothE(labels: String*) = start.bothE(labels: _*)

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

  def start() = GremlinScala[Vertex, HNil](__.__(vertex))
}

case class ScalaEdge(edge: Edge) extends ScalaElement[Edge] {
  override def element = edge

  def setProperty(key: String, value: Any): ScalaEdge = {
    element.property(key, value)
    this
  }

  def setProperties(properties: Map[String, Any]): ScalaEdge = {
    properties foreach { case (k, v) ⇒ setProperty(k, v) }
    this
  }

  def removeProperty(key: String): ScalaEdge = {
    val p = property(key)
    if (p.isPresent) p.remove
    this
  }

  def start() = GremlinScala[Edge, HNil](__.__(edge))

  //TODO: wait until this is consistent in T3 between Vertex and Edge
  //currently Vertex.outE returns a GraphTraversal, Edge.inV doesnt quite exist
  //def inV() = GremlinScala[Vertex, HNil](edge.inV())
}
