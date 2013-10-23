package com.tinkerpop.gremlin.scala

import com.tinkerpop.blueprints.{ Vertex, Edge, Direction }

/**Adds convenience methods to [[com.tinkerpop.blueprints.Edge]]. */
class ScalaEdge(val edge: Edge) extends Edge with ScalaElement {
  def inV: GremlinScalaPipeline[Edge, Vertex] = new GremlinScalaPipeline[Edge, Vertex].start(ScalaEdge(edge)).inV
  def outV: GremlinScalaPipeline[Edge, Vertex] = new GremlinScalaPipeline[Edge, Vertex].start(edge).outV
  def bothV: GremlinScalaPipeline[Edge, Vertex] = new GremlinScalaPipeline[Edge, Vertex].start(edge).bothV

  def -> = startPipe
  def startPipe: GremlinScalaPipeline[Edge, Edge] = new GremlinScalaPipeline[Edge, Edge].start(edge)

  val element = edge
  /** need to extend Edge so that we can use existing Gremlin Pipes... */
  def label = getLabel
  def getLabel: String = edge.getLabel
  def getVertex(direction: Direction): Vertex = edge.getVertex(direction)

}

/**Implicit conversions between [[com.tinkerpop.blueprints.Edge]] and [[com.tinkerpop.gremlin.scala.ScalaEdge]]. */
object ScalaEdge {
  def apply(edge: Edge) = wrap(edge)
  implicit def wrap(edge: Edge) = new ScalaEdge(edge)
  implicit def unwrap(wrapper: ScalaEdge) = wrapper.edge
}
