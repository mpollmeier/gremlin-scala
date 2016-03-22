package gremlin.scala

import shapeless.HNil
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource

case class ScalaGraphTraversalSource(traversalSource: GraphTraversalSource) {
  // start traversal with all vertices
  def V = GremlinScala[Vertex, HNil](traversalSource.V())

  // start traversal with all edges
  def E = GremlinScala[Edge, HNil](traversalSource.E())

  // start traversal with some vertices identified by given ids
  def V(vertexIds: Any*) = GremlinScala[Vertex, HNil](traversalSource.V(vertexIds.asInstanceOf[Seq[AnyRef]]: _*))

  // start traversal with some edges identified by given ids
  def E(edgeIds: Any*) = GremlinScala[Edge, HNil](traversalSource.E(edgeIds.asInstanceOf[Seq[AnyRef]]: _*))
}
