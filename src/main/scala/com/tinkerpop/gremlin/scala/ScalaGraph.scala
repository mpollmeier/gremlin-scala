package com.tinkerpop.gremlin.scala

import com.tinkerpop.blueprints._

class ScalaGraph(val graph: Graph) {
  /** iterate all vertices */
  def V: GremlinScalaPipeline[Vertex, Vertex] = new GremlinScalaPipeline[ScalaGraph, Vertex].V(graph)

  /** iterate all edges */
  def E: GremlinScalaPipeline[Edge, Edge] = new GremlinScalaPipeline[ScalaGraph, Edge].E(graph)

  /**Returns the vertices with the specified IDs. */
  def V(ids: Any*): Iterable[Vertex] = ids.map(graph.getVertex)

  /**Returns the edges with the specified IDs. */
  def E(ids: Any*): Iterable[Edge] = ids.map(graph.getEdge)

  /**Returns the vertex with the specified ID. */
  def v(id: Any): Vertex = graph.getVertex(id)

  /**Returns the edge with the specified ID. */
  def e(id: Any): ScalaEdge = graph.getEdge(id)

  def -> : GremlinScalaPipeline[ScalaGraph, ScalaGraph] =
    new GremlinScalaPipeline[ScalaGraph, ScalaGraph].start(ScalaGraph(graph))

  /** add vertex; id defaults to null which will generate a random id*/
  def addV(id: Any = null) = graph.addVertex(id)

  /** add edge; id defaults to null which will generate a random id */
  def addE(out: Vertex, in: Vertex, label: String, id: Any = null) = graph.addEdge(id, out.vertex, in.vertex, label)
}

/**Implicit conversions between [[com.tinkerpop.blueprints.Graph]] and [[com.tinkerpop.gremlin.scala.ScalaGraph]]. */
object ScalaGraph {
  def apply(graph: Graph) = wrap(graph)
  implicit def wrap(graph: Graph) = new ScalaGraph(graph)
  implicit def unwrap(wrapper: ScalaGraph) = wrapper.graph
}
