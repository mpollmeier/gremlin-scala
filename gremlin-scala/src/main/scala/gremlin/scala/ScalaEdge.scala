package gremlin.scala

import shapeless._
import scala.collection.JavaConversions._
import schema.Key

case class ScalaEdge(edge: Edge) extends ScalaElement[Edge] {
  override def element = edge

  override def setProperty[A](key: Key[A], value: A): Edge = {
    element.property(key.value, value)
    edge
  }

  def setProperties(properties: Map[Key[Any], Any]): Edge = {
    properties foreach { case (k, v) ⇒ setProperty(k, v) }
    edge
  }

  def setProperties[T <: Product: Marshallable](cc: T): Edge = {
    val (_, _, properties) = implicitly[Marshallable[T]].fromCC(cc)
    properties foreach { case (k, v) ⇒ element.property(k, v) }
    edge
  }

  override def removeProperty(key: Key[_]): Edge = {
    val p = property(key)
    if (p.isPresent) p.remove()
    edge
  }

  override def removeProperties(keys: Key[_]*): Edge = {
    keys foreach removeProperty
    edge
  }

  def toCC[T <: Product: Marshallable] =
    implicitly[Marshallable[T]].toCC(edge.id, edge.valueMap)

  override def start() = GremlinScala[Edge, HNil](__(edge))

  override def properties[A: DefaultsToAny]: Stream[Property[A]] =
    edge.properties[A](keys.map(_.value).toSeq: _*).toStream

  override def properties[A: DefaultsToAny](wantedKeys: String*): Stream[Property[A]] =
    edge.properties[A](wantedKeys: _*).toStream

  //TODO: wait until this is consistent in T3 between Vertex and Edge
  //currently Vertex.outE returns a GraphTraversal, Edge.inV doesnt quite exist
  //def inV() = GremlinScala[Vertex, HNil](edge.inV())
}
