package gremlin.scala

import java.util

import org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality
import org.apache.tinkerpop.gremlin.structure.{Direction, VertexProperty, T}
import shapeless._
import scala.collection.JavaConversions._
import schema.Key

case class ScalaVertex(vertex: Vertex) extends ScalaElement[Vertex] {
  override def element = vertex

  def toCC[P <: Product : Marshallable] =
    implicitly[Marshallable[P]].toCC(vertex.id, vertex.valueMap)

  override def setProperty[A](key: Key[A], value: A): Vertex = {
    element.property(key.key, value)
    vertex
  }

  def setProperties(properties: Map[Key[Any], Any]): Vertex = {
    properties foreach { case (k, v) ⇒ setProperty(k, v) }
    vertex
  }

  override def removeProperty(key: Key[_]): Vertex = {
    val p = property(key)
    if (p.isPresent) p.remove()
    vertex
  }

  override def removeProperties(keys: Key[_]*): Vertex = {
    keys foreach removeProperty
    vertex
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
              inVertex: Vertex,
              properties: Map[Key[_], Any] = Map.empty): Edge = {
    val params = properties.toSeq.flatMap(pair ⇒ Seq(pair._1.key, pair._2.asInstanceOf[AnyRef]))
    vertex.addEdge(label, inVertex.vertex, params: _*)
  }

  def addEdge[P <: Product : Marshallable](inVertex: Vertex, cc: P): ScalaEdge = {
    val (id, label, properties) = implicitly[Marshallable[P]].fromCC(cc)
    val idParam = id.toSeq flatMap (List(T.id, _))
    val params = properties.toSeq.flatMap(pair ⇒ Seq(pair._1, pair._2.asInstanceOf[AnyRef]))
    vertex.addEdge(label, inVertex.vertex, idParam ++ params: _*)
  }

  def <--(se: SemiEdge) = se.from.asScala.addEdge(se.label, vertex, se.properties)

  def <--(de: SemiDoubleEdge): (Edge, Edge) =
    addEdge(de.label, de.right, de.properties) → de.right.asScala.addEdge(de.label, vertex, de.properties)

  def ---(label: String) = SemiEdge(vertex, label)

  def ---(label: String, properties: (Key[_], Any)*) =
    SemiEdge(vertex, label, properties.toMap)

  def ---[P <: Product : Marshallable](cc: P) = {
    val (_, label, properties) = implicitly[Marshallable[P]].fromCC(cc)
    SemiEdge(vertex, label, properties.toMap.map{case (k,v) => (Key[Any](k), v)})
  }

  override def start() = GremlinScala[Vertex, HNil](__(vertex))

  def vertices(direction: Direction, edgeLabels: String*): util.Iterator[Vertex] =
    vertex.vertices(direction, edgeLabels: _*)

  def edges(direction: Direction, edgeLabels: String*): util.Iterator[Edge] =
    vertex.edges(direction, edgeLabels: _*)

  def property[A](cardinality: Cardinality, key: Key[A], value: A, keyValues: AnyRef*): VertexProperty[A] =
    vertex.property(cardinality, key, value, keyValues: _*)

  override def properties[A: DefaultsToAny]: Stream[VertexProperty[A]] =
    vertex.properties[A](keys.map(_.key).toSeq: _*).toStream

  override def properties[A: DefaultsToAny](wantedKeys: String*): Stream[VertexProperty[A]] =
    vertex.properties[A](wantedKeys: _*).toStream
}
