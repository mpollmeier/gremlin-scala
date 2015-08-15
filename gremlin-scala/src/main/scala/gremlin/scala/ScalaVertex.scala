package gremlin.scala

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__
import shapeless._

case class ScalaVertex(vertex: Vertex) extends ScalaElement[Vertex] {
  override def element = vertex

  def toCC[T <: Product: Marshallable] = implicitly[Marshallable[T]].toCC(vertex.label, vertex.valueMap())

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
    if (p.isPresent) p.remove()
    this
  }

  def out() = start().out()
  def out(labels: String*) = start().out(labels: _*)

  def outE() = start().outE()
  def outE(labels: String*) = start().outE(labels: _*)

  def in() = start().in()
  def in(labels: String*) = start().in(labels: _*)

  def inE() = start().inE()
  def inE(labels: String*) = start().inE(labels: _*)

  def both() = start().both()
  def both(labels: String*) = start().both(labels: _*)

  def bothE() = start().bothE()
  def bothE(labels: String*) = start().bothE(labels: _*)

  def addEdge(label: String,
              inVertex: ScalaVertex,
              properties: Map[String, Any] = Map.empty): ScalaEdge = {
    val e = vertex.addEdge(label, inVertex.vertex).asScala
    e.setProperties(properties)
    e
  }

  def addEdge[T <: Product: Marshallable](inVertex: ScalaVertex, cc: T): ScalaEdge = {
    val (label, properties) = implicitly[Marshallable[T]].fromCC(cc)
    val e = vertex.addEdge(label, inVertex.vertex).asScala
    e.setProperties(properties)
    e
  }

  def <--(se: SemiEdge) = se.from.addEdge(se.label, this, se.properties)

  def <--(de: SemiDoubleEdge) = (
    this.addEdge(de.label, de.right, de.properties),
    de.right.addEdge(de.label, this, de.properties)
    )

  def ---(label: String, properties: Map[String, Any] = Map.empty) =
    SemiEdge(this, label, properties.toMap)

  def ---[T <: Product: Marshallable](cc: T) = {
    val (label, properties) = implicitly[Marshallable[T]].fromCC(cc)
    SemiEdge(this, label, properties)
  }

  def start() = GremlinScala[Vertex, HNil](__.__(vertex))
}
