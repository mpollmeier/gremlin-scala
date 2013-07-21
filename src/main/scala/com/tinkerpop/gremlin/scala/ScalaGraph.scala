package com.tinkerpop.gremlin.scala

import com.tinkerpop.blueprints.Graph

class ScalaGraph(val graph: Graph) {
  /** iterate all vertices */
  def V: GremlinScalaPipeline[ScalaVertex, ScalaVertex] = new GremlinScalaPipeline[ScalaGraph, ScalaVertex].V(graph)

  /** iterate all edges */
  def E: GremlinScalaPipeline[ScalaEdge, ScalaEdge] = new GremlinScalaPipeline[ScalaGraph, ScalaEdge].E(graph)

  /**Returns the vertices with the specified IDs. */
  def V(ids: Any*): Iterable[ScalaVertex] = ids.map(id ⇒ ScalaVertex(graph.getVertex(id)))

  /**Returns the edges with the specified IDs. */
  def E(ids: Any*): Iterable[ScalaEdge] = ids.map(id ⇒ ScalaEdge(graph.getEdge(id)))

  /**Returns the vertex with the specified ID. */
  def v(id: Any): ScalaVertex = graph.getVertex(id)

  /**Returns the edge with the specified ID. */
  def e(id: Any): ScalaEdge = graph.getEdge(id)

  def -> : GremlinScalaPipeline[ScalaGraph, ScalaGraph] =
    new GremlinScalaPipeline[ScalaGraph, ScalaGraph].start(new ScalaGraph(graph))

  /** add vertex; id defaults to null which will generate a random id*/
  def addV(id: Any = null) = graph.addVertex(id)

  /** add edge; id defaults to null which will generate a random id */
  def addE(out: ScalaVertex, in: ScalaVertex, label: String, id: Any = null) = graph.addEdge(id, out.vertex, in.vertex, label)
}

/**Implicit conversions between [[com.tinkerpop.blueprints.Graph]] and [[com.tinkerpop.gremlin.scala.ScalaGraph]]. */
object ScalaGraph {
  implicit def wrap(graph: Graph) = new ScalaGraph(graph)
  implicit def unwrap(wrapper: ScalaGraph) = wrapper.graph
}
