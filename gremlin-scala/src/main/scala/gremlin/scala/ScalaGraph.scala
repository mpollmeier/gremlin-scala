package gremlin.scala

import org.apache.commons.configuration.Configuration
import org.apache.tinkerpop.gremlin.process.computer.GraphComputer
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.structure.Graph.Variables
import org.apache.tinkerpop.gremlin.structure.{Transaction, T}
import shapeless._
import scala.collection.JavaConversions._

case class ScalaGraph[G <: Graph](graph: G) {

  def addVertex(label: String): ScalaVertex = graph.addVertex(label)

  def addVertex(): ScalaVertex = graph.addVertex()

  def addVertex(label: String, properties: (String, Any)*): ScalaVertex = {
    val labelParam = Seq(T.label, label)
    val params = properties.flatMap(pair => Seq(pair._1, pair._2.asInstanceOf[AnyRef]))
    graph.addVertex(labelParam ++ params: _*)
  }

  def addVertex(properties: (String, Any)*): ScalaVertex = {
    val params = properties.flatMap(pair => Seq(pair._1, pair._2.asInstanceOf[AnyRef]))
    graph.addVertex(params: _*)
  }

  def addVertex(label: String, properties: Map[String, Any]): ScalaVertex =
    addVertex(label, properties.toSeq: _*)

  def addVertex(properties: Map[String, Any]): ScalaVertex =
    addVertex(properties.toSeq: _*)

  /**
   * Save an object's values into a new vertex
   * @param cc The case class to persist as a vertex
   */
  def addVertex[P <: Product : Marshallable](cc: P): ScalaVertex = {
    val (id, label, properties) = implicitly[Marshallable[P]].fromCC(cc)
    val idParam = id.toSeq flatMap (List(T.id, _))
    val labelParam = Seq(T.label, label)
    val params = properties.toSeq.flatMap(pair => Seq(pair._1, pair._2.asInstanceOf[AnyRef]))
    graph.addVertex(idParam ++ labelParam ++ params: _*)
  }

  def +(label: String): ScalaVertex = addVertex(label)

  def +(label: String, properties: (String, Any)*): ScalaVertex = addVertex(label, properties.toMap)

  // get vertex by id
  def v(id: AnyRef): Option[ScalaVertex] =
    graph.traversal.V(id).headOption map ScalaVertex.apply

  // get edge by id
  def e(id: AnyRef): Option[ScalaEdge] =
    graph.traversal.E(id).headOption map ScalaEdge.apply

  // start traversal with all vertices 
  def V = GremlinScala[Vertex, HNil](graph.traversal.V().asInstanceOf[GraphTraversal[_, Vertex]])

  // start traversal with all edges
  def E = GremlinScala[Edge, HNil](graph.traversal.E().asInstanceOf[GraphTraversal[_, Edge]])

  // start traversal with some vertices identified by given ids 
  def V(vertexIds: AnyRef*) = GremlinScala[Vertex, HNil](graph.traversal.V(vertexIds: _*).asInstanceOf[GraphTraversal[_, Vertex]])

  // start traversal with some edges identified by given ids 
  def E(edgeIds: AnyRef*) = GremlinScala[Edge, HNil](graph.traversal.E(edgeIds: _*).asInstanceOf[GraphTraversal[_, Edge]])

  def edges(edgeIds: AnyRef*): Iterator[ScalaEdge] = graph.edges(edgeIds) map (_.asScala)

  def vertices(vertexIds: AnyRef*): Iterator[ScalaVertex] = graph.vertices(vertexIds) map (_.asScala)

  def tx(): Transaction = graph.tx()

  def variables(): Variables = graph.variables()

  def configuration(): Configuration = graph.configuration()

  def compute[C <: GraphComputer](graphComputerClass: Class[C]): C = graph.compute(graphComputerClass)

  def compute(): GraphComputer = graph.compute()

  def close(): Unit = graph.close()
}
