package com.tinkerpop.gremlin.scala

import com.tinkerpop.blueprints.{ Graph, Vertex, Edge }

/**Adds convenience methods to [com.tinkerpop.blueprints.Graph] */
class ScalaGraph(val graph: Graph) {
  /**Returns all vertices. */
  def V: GremlinScalaPipeline[Vertex, Vertex] = //TODO: get rid of these casts
    new GremlinScalaPipeline[Graph, Vertex].start(graph).V(graph).asInstanceOf[GremlinScalaPipeline[Vertex, Vertex]]

  /**Returns all edges. */
  def E: GremlinScalaPipeline[Edge, Edge] = //TODO: get rid of these casts
    new GremlinScalaPipeline[Graph, Edge].start(graph).E(graph).asInstanceOf[GremlinScalaPipeline[Edge, Edge]]

  /**Returns the vertices with the specified IDs. */
  def V(ids: Any*): Iterable[ScalaVertex] = ids.map(id ⇒ ScalaVertex(graph.getVertex(id)))

  /**Returns the edges with the specified IDs. */
  def E(ids: Any*): Iterable[ScalaEdge] = ids.map(id ⇒ ScalaEdge(graph.getEdge(id)))

  /**Returns the vertex with the specified ID. */
  def v(id: Any): ScalaVertex = graph.getVertex(id)

  /**Returns the edge with the specified ID. */
  def e(id: Any): ScalaEdge = graph.getEdge(id)

  def -> : GremlinScalaPipeline[Graph, Graph] =
    new GremlinScalaPipeline[Graph, Graph].start(graph).asInstanceOf[GremlinScalaPipeline[Graph, Graph]];

  /** add vertex */
  def addV() = graph.addVertex(null)
  def addV(id: Any) = graph.addVertex(id)

  /** add edge */
  def addE(id: Any, out: ScalaVertex, in: ScalaVertex, label: String) = graph.addEdge(id, out, in, label)
  def addE(out: ScalaVertex, in: ScalaVertex, label: String) = graph.addEdge(null, out, in, label)
}

/**Implicit conversions between [[com.tinkerpop.blueprints.Graph]] and [[com.tinkerpop.gremlin.scala.ScalaGraph]]. */
object ScalaGraph {
  implicit def wrap(graph: Graph) = new ScalaGraph(graph)
  implicit def unwrap(wrapper: ScalaGraph) = wrapper.graph
}
