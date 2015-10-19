package gremlin.scala

import org.apache.commons.configuration.Configuration
import org.apache.tinkerpop.gremlin.process.computer.GraphComputer
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.structure.Graph.Variables
import org.apache.tinkerpop.gremlin.structure.{Transaction, T}
import shapeless._
import scala.collection.JavaConversions._

case class ScalaGraph[G <: Graph](graph: G) {

  def addVertex(label: String): Vertex = graph.addVertex(label)

  def addVertex(): Vertex = graph.addVertex()

  def addVertex(label: String, properties: (String, Any)*): Vertex = {
    val labelParam = Seq(T.label, label)
    val params = properties.flatMap(pair ⇒ Seq(pair._1, pair._2.asInstanceOf[AnyRef]))
    graph.addVertex(labelParam ++ params: _*)
  }

  def addVertex(properties: (String, Any)*): Vertex = {
    val params = properties.flatMap(pair ⇒ Seq(pair._1, pair._2.asInstanceOf[AnyRef]))
    graph.addVertex(params: _*)
  }

  def addVertex(label: String, properties: Map[String, Any]): Vertex =
    addVertex(label, properties.toSeq: _*)

  def addVertex(properties: Map[String, Any]): Vertex =
    addVertex(properties.toSeq: _*)

  /**
    * Save an object's values into a new vertex
    * @param cc The case class to persist as a vertex
    */
  def addVertex[P <: Product: Marshallable](cc: P): Vertex = {
    val (id, label, properties) = implicitly[Marshallable[P]].fromCC(cc)
    val idParam = id.toSeq flatMap (List(T.id, _))
    val labelParam = Seq(T.label, label)
    val params = properties.toSeq.flatMap(pair ⇒ Seq(pair._1, pair._2.asInstanceOf[AnyRef]))
    graph.addVertex(idParam ++ labelParam ++ params: _*)
  }

  def +(label: String): Vertex = addVertex(label)

  def +(label: String, properties: (Key[_], Any)*): Vertex =
    addVertex(label, properties.toMap.map { case (k, v) ⇒ (k.value, v) })

  // get vertex by id
  def v(id: Any): Option[Vertex] =
    graph.traversal.V(id.asInstanceOf[AnyRef]).headOption

  // get edge by id
  def e(id: Any): Option[Edge] =
    graph.traversal.E(id.asInstanceOf[AnyRef]).headOption

  // start traversal with all vertices 
  def V = GremlinScala[Vertex, HNil](graph.traversal.V().asInstanceOf[GraphTraversal[_, Vertex]])

  // start traversal with all edges
  def E = GremlinScala[Edge, HNil](graph.traversal.E().asInstanceOf[GraphTraversal[_, Edge]])

  // start traversal with some vertices identified by given ids 
  def V(vertexIds: Any*) =
    GremlinScala[Vertex, HNil](graph.traversal.V(vertexIds.asInstanceOf[Seq[AnyRef]]: _*)
      .asInstanceOf[GraphTraversal[_, Vertex]])

  // start traversal with some edges identified by given ids 
  def E(edgeIds: Any*) =
    GremlinScala[Edge, HNil](graph.traversal.E(edgeIds.asInstanceOf[Seq[AnyRef]]: _*)
      .asInstanceOf[GraphTraversal[_, Edge]])

  def edges(edgeIds: Any*): Iterator[Edge] =
    graph.edges(edgeIds.asInstanceOf[Seq[AnyRef]])

  def vertices(vertexIds: Any*): Iterator[Vertex] =
    graph.vertices(vertexIds.asInstanceOf[Seq[AnyRef]])

  def tx(): Transaction = graph.tx()

  def variables(): Variables = graph.variables()

  def configuration(): Configuration = graph.configuration()

  def compute[C <: GraphComputer](graphComputerClass: Class[C]): C =
    graph.compute(graphComputerClass)

  def compute(): GraphComputer = graph.compute()

  def close(): Unit = graph.close()

  def transactional[R](work: Graph ⇒ R) = graph.tx.submit(work)
}
