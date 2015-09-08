package gremlin.scala

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__
import shapeless._
import scala.collection.JavaConversions._

case class ScalaEdge(edge: Edge) extends ScalaElement[Edge] {
  override def element = edge

  override def setProperty(key: String, value: Any): ScalaEdge = {
    element.property(key, value)
    this
  }

  def setProperties(properties: Map[String, Any]): ScalaEdge = {
    properties foreach { case (k, v) ⇒ setProperty(k, v) }
    this
  }

  def setProperties[T <: Product: Marshallable](cc: T): ScalaEdge = {
    val (_, _, properties) = implicitly[Marshallable[T]].fromCC(cc)
    setProperties(properties)
    this
  }

  override def removeProperty(key: String): ScalaEdge = {
    val p = property(key)
    if (p.isPresent) p.remove()
    this
  }

  override def removeProperties(keys: String*): ScalaEdge = {
    keys foreach removeProperty
    this
  }

  def toCC[T <: Product: Marshallable] =
    implicitly[Marshallable[T]].toCC(edge.id, edge.valueMap)

  override def start() = GremlinScala[Edge, HNil](__.__(edge))

  override def properties[A: DefaultsToAny]: Stream[Property[A]] =
    edge.properties[A](keys.toSeq: _*).toStream

  override def properties[A: DefaultsToAny](wantedKeys: String*): Stream[Property[A]] =
    edge.properties[A](wantedKeys: _*).toStream

  //TODO: wait until this is consistent in T3 between Vertex and Edge
  //currently Vertex.outE returns a GraphTraversal, Edge.inV doesnt quite exist
  //def inV() = GremlinScala[Vertex, HNil](edge.inV())
}
