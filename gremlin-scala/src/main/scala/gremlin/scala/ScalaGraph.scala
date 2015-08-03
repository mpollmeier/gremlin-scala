package gremlin.scala

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import shapeless._
import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

case class ScalaGraph(graph: Graph) {

  def addVertex(properties: Map[String, Any]): ScalaVertex = {
    val v = graph.addVertex()
    v.setProperties(properties)
    v
  }

  def addVertex(label: String, properties: Map[String, Any]): ScalaVertex = {
    val v = graph.addVertex(label)
    v.setProperties(properties)
    v
  }

  /**
   * Save an object's values into a new vertex
   *
   * @param cc The case class to persist as a vertex
   * @tparam T
   * @return
   */
  def addCC[T <: Product : Mappable](cc: T): ScalaVertex = {
    println(implicitly[Mappable[T]].toMap(cc))
    addVertex().setProperties(implicitly[Mappable[T]].toMap(cc))
  }

  // get vertex by id
  def v(id: AnyRef): Option[ScalaVertex] =
    GremlinScala(graph.traversal.V(id)).headOption map ScalaVertex.apply

  // get edge by id
  def e(id: AnyRef): Option[ScalaEdge] =
    GremlinScala(graph.traversal.E(id)).headOption map ScalaEdge.apply

  // start traversal with all vertices 
  def V = GremlinScala[Vertex, HNil](graph.traversal.V().asInstanceOf[GraphTraversal[_, Vertex]])

  // start traversal with all edges
  def E = GremlinScala[Edge, HNil](graph.traversal.E().asInstanceOf[GraphTraversal[_, Edge]])

  // start traversal with some vertices identified by given ids 
  def V(vertexIds: AnyRef*) = GremlinScala[Vertex, HNil](graph.traversal.V(vertexIds: _*).asInstanceOf[GraphTraversal[_, Vertex]])

  // start traversal with some edges identified by given ids 
  def E(edgeIds: AnyRef*) = GremlinScala[Edge, HNil](graph.traversal.E(edgeIds: _*).asInstanceOf[GraphTraversal[_, Edge]])

}
