package com.tinkerpop.gremlin.scala

import com.tinkerpop.blueprints.{ Vertex, Edge }

/**Adds convenience methods to [[com.tinkerpop.blueprints.Edge]]. */
class ScalaEdge(val edge: Edge) extends ScalaElement(edge) {
  def inV: GremlinScalaPipeline[Edge, Vertex] = new GremlinScalaPipeline[Edge, Vertex].start(edge).inV
  def outV: GremlinScalaPipeline[Edge, Vertex] = new GremlinScalaPipeline[Edge, Vertex].start(edge).outV
  def bothV: GremlinScalaPipeline[Edge, Vertex] = new GremlinScalaPipeline[Edge, Vertex].start(edge).bothV

  def -> : GremlinScalaPipeline[Edge, Edge] = new GremlinScalaPipeline[Edge, Edge].start(edge)
}

/**Implicit conversions between [[com.tinkerpop.blueprints.Edge]] and [[com.tinkerpop.gremlin.scala.ScalaEdge]]. */
object ScalaEdge {
  def apply(edge: Edge) = wrap(edge)
  implicit def wrap(edge: Edge) = new ScalaEdge(edge)
  implicit def unwrap(wrapper: ScalaEdge) = wrapper.edge
}
