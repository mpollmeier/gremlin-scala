package gremlin.scala

import java.util
import org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality
import org.apache.tinkerpop.gremlin.structure.{Direction, T, VertexProperty}
import scala.collection.JavaConverters._

case class ScalaVertex(vertex: Vertex) extends ScalaElement[Vertex] {
  override def element = vertex

  def toCC[CC <: Product: Marshallable] =
    implicitly[Marshallable[CC]].toCC(vertex)

  override def setProperty[A](key: Key[A], value: A): Vertex = {
    element.property(key.name, value)
    vertex
  }

  def setProperties(properties: Map[Key[Any], Any]): Vertex = {
    properties.foreach { case (k, v) => setProperty(k, v) }
    vertex
  }

  def setProperties[CC <: Product: Marshallable](cc: CC): Vertex = {
    val fromCC = implicitly[Marshallable[CC]].fromCC(cc)
    fromCC.properties.foreach { case (k, v) => setProperty(Key[Any](k), v) }
    vertex
  }

  override def removeProperty(key: Key[_]): Vertex =
    removeProperty(key, Cardinality.single)

  def removeProperty(key: Key[_], cardinality: Cardinality): Vertex = {
    cardinality match {
      case Cardinality.single =>
        val p = property(key)
        if (p.isPresent) p.remove
      case Cardinality.list | Cardinality.set =>
        vertex.properties(key.name).asScala.foreach { p: VertexProperty[_] =>
          p.remove
        }
    }
    vertex
  }

  override def removeProperties(keys: Key[_]*): Vertex = {
    keys.foreach(removeProperty)
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

  /** `implicit ScalaGraph` required for configuration, e.g. when using remote graph */
  def addEdge(label: String, inVertex: Vertex, properties: KeyValue[_]*)(implicit graph: ScalaGraph): Edge =
    graph.traversal.V(vertex).addE(label, properties: _*).to(inVertex).head()

  def addEdge[CC <: Product: Marshallable](inVertex: Vertex, cc: CC): Edge = {
    val fromCC = implicitly[Marshallable[CC]].fromCC(cc)
    val idParam = fromCC.id.toSeq.flatMap(List(T.id, _))
    val params = fromCC.properties.flatMap(pair => Seq(pair._1, pair._2.asInstanceOf[AnyRef]))
    vertex.addEdge(fromCC.label, inVertex.vertex, idParam ++ params: _*)
  }

  /** `implicit ScalaGraph` required for configuration, e.g. when using remote graph */
  def <--(se: SemiEdge)(implicit graph: ScalaGraph): Edge =
    se.from.asScala().addEdge(se.label, vertex, se.properties: _*)

  /** `implicit ScalaGraph` required for configuration, e.g. when using remote graph */
  def <--(de: SemiDoubleEdge)(implicit graph: ScalaGraph): (Edge, Edge) =
    addEdge(de.label, de.right, de.properties: _*) -> de.right.asScala()
      .addEdge(de.label, vertex, de.properties: _*)

  def ---(label: String): SemiEdge = SemiEdge(vertex, label)

  def ---(label: String, properties: KeyValue[_]*): SemiEdge =
    SemiEdge(vertex, label, properties: _*)

  def ---(label: String, properties: Map[String, Any]): SemiEdge =
    SemiEdge(vertex, label, properties.map {
      case (key, value) => Key[Any](key) -> value
    }.toSeq: _*)

  def ---[CC <: Product: Marshallable](cc: CC): SemiEdge = {
    val fromCC = implicitly[Marshallable[CC]].fromCC(cc)
    SemiEdge(vertex, fromCC.label, fromCC.properties.map { r =>
      Key[Any](r._1) -> r._2
    }.toSeq: _*)
  }

  def vertices(direction: Direction, edgeLabels: String*): util.Iterator[Vertex] =
    vertex.vertices(direction, edgeLabels: _*)

  def edges(direction: Direction, edgeLabels: String*): util.Iterator[Edge] =
    vertex.edges(direction, edgeLabels: _*)

  def property[A](cardinality: Cardinality,
                  key: Key[A],
                  value: A,
                  keyValues: AnyRef*): VertexProperty[A] =
    vertex.property(cardinality, key.name, value, keyValues: _*)

  /** convenience function for `property` which normally requires to pass in key/value pairs as varargs */
  def setPropertyList[A <: AnyRef](key: String, values: List[A]): VertexProperty[A] = {
    removeProperty(Key[A](key), Cardinality.list)
    values
      .map { value =>
        vertex.property(Cardinality.list, key, value)
      }
      .lastOption
      .getOrElse(VertexProperty.empty[A])
  }
  /* TODO: looks like there's a bug in tinkerpop - passing multiple values with the same key doesn't result in all those values being set:
    values match {
      case Nil =>
        removeProperty(key)
        VertexProperty.empty[A]
      case headValue :: tailValues =>
        val varargs = tailValues.flatMap(value => List(key.name, value))
        vertex.property(Cardinality.list, key.name, headValue, varargs: _*)
    }
   */

  def setPropertyList[A <: AnyRef](key: Key[A], values: List[A]): VertexProperty[A] =
    setPropertyList(key.name, values)

  override def properties[A: DefaultsToAny]: Stream[VertexProperty[A]] =
    vertex.properties[A](keys.map(_.name).toSeq: _*).asScala.toStream

  override def properties[A: DefaultsToAny](wantedKeys: String*): Stream[VertexProperty[A]] =
    vertex.properties[A](wantedKeys: _*).asScala.toStream
}
