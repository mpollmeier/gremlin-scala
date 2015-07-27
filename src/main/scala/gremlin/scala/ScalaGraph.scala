package gremlin.scala

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import shapeless._
import shapeless.labelled.FieldType
import scala.reflect.ClassTag
import scala.reflect.runtime.universe._
import scala.collection.JavaConverters._

case class ScalaGraph(graph: Graph) {

  def addVertex() = ScalaVertex(graph.addVertex())
  def addVertex(label: String) = ScalaVertex(graph.addVertex(label))
  def addVertex(properties: Map[String, Any]): ScalaVertex = {
    val v = addVertex()
    v.setProperties(properties)
    v
  }
  def addVertex(label: String, properties: Map[String, Any]): ScalaVertex = {
    val v = addVertex(label)
    v.setProperties(properties)
    v
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

  // save an object's values into a new vertex
  def save[A](cc: A)(implicit ctmr: CcToMapRec[A]): ScalaVertex = {
    val params = ctmr(cc)
    addVertex().setProperties(params)
  }
}
