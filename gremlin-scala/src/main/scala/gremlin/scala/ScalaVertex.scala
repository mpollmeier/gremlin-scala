package gremlin.scala

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__
import shapeless._

case class ScalaVertex(vertex: Vertex) extends ScalaElement[Vertex] {
  override def element = vertex

  def --(label: String) = SemiEdge(vertex, label)

  def toCC[T <: Product: Mappable] = implicitly[Mappable[T]].fromMap(vertex.valueMap())

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

  // if you want to specify the vertex id, just provide `T.id -> YourId` as a property
  def addEdge(label: String,
              inVertex: ScalaVertex,
              properties: Map[String, Any] = Map.empty): ScalaEdge = {
    val e = ScalaEdge(vertex.addEdge(label, inVertex.vertex))
    e.setProperties(properties)
    e
  }

  def start() = GremlinScala[Vertex, HNil](__.__(vertex))
}
