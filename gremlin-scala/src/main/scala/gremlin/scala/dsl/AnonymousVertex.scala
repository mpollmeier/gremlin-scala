package gremlin.scala.dsl

import gremlin.scala._
import java.util.{Iterator => JIterator}
import scala.collection.JavaConverters._

object AnonymousVertex {
  import org.apache.tinkerpop.gremlin.structure._

  def apply[CC <: Product](cc: CC)(implicit marshaller: Marshallable[CC],
                                   grph: Graph): Vertex = {
    val fromCC = marshaller.fromCC(cc)
    new Vertex {
      val graph = grph
      val id = fromCC.id
      val label = fromCC.label
      def remove(): Unit = ???
      def addEdge(label: String, inVertex: Vertex, keyValues: Object*): Edge =
        ???
      def edges(direction: Direction, edgeLabels: String*): JIterator[Edge] =
        ???
      def property[V](cardinality: VertexProperty.Cardinality,
                      key: String,
                      value: V,
                      keyValues: Object*): VertexProperty[V] = ???
      def vertices(direction: Direction,
                   edgeLabels: String*): JIterator[Vertex] = ???
      def properties[V](x$1: String*): JIterator[VertexProperty[V]] = {
        val x: Iterable[VertexProperty[V]] = fromCC.valueMap.map {
          case (ccKey, ccValue) =>
            new VertexProperty[V] {
              // Members declared in org.apache.tinkerpop.gremlin.structure.Element
              def id(): Object = ???
              def property[V](x$1: String, x$2: V): Property[V] = ???
              def remove(): Unit = ???

              // Members declared in org.apache.tinkerpop.gremlin.structure.Property
              def isPresent(): Boolean = true
              def key(): String = ccKey
              def value(): V = ccValue.asInstanceOf[V]

              // Members declared in org.apache.tinkerpop.gremlin.structure.VertexProperty
              def element(): Vertex = ???
              def properties[U](x$1: String*): JIterator[Property[U]] = ???
            }
        }
        x.iterator.asJava
      }
    }
  }
}
