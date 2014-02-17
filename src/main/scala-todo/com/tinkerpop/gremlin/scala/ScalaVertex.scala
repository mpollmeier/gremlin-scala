package com.tinkerpop.gremlin.scala

import java.util.{ Map ⇒ JMap }
import com.tinkerpop.blueprints.{ Vertex, Edge, Element }
import java.lang.{ Iterable ⇒ JIterable }
import com.tinkerpop.blueprints.Direction
import com.tinkerpop.blueprints.VertexQuery
import com.tinkerpop.gremlin.java.GremlinPipeline
import shapeless._
import ElementSpecific._

//
//   /** start a pipeline from this vertex */
//   def startPipe: GremlinScalaPipeline[Vertex, Vertex] = new GremlinScalaPipeline[Vertex, Vertex].start(vertex)
// 
  /** need to extend Vertex so that we can use existing Gremlin Pipes... */
  //val element: Element = vertex
  //def addEdge(label: String, inVertex: Vertex): Edge = vertex.addEdge(label, inVertex)
  //def getEdges(direction: Direction, labels: String*): JIterable[Edge] = vertex.getEdges(direction, labels: _*)
  //def getVertices(direction: Direction, labels: String*): JIterable[Vertex] = vertex.getVertices(direction, labels: _*)
//}

/**Implicit conversions between Vertex and ScalaVertex */
object ScalaVertex {
  def apply(vertex: Vertex) = wrap(vertex)
  implicit def wrap(vertex: Vertex) = new ScalaVertex(vertex)
  implicit def unwrap(wrapper: ScalaVertex) = wrapper.vertex
}
