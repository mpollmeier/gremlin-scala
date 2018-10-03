package gremlin.scala

import org.apache.commons.configuration.Configuration
import org.apache.tinkerpop.gremlin.process.computer.GraphComputer
import org.apache.tinkerpop.gremlin.structure.Graph.Variables
import org.apache.tinkerpop.gremlin.structure.{T, Transaction}
import shapeless._

object ScalaGraph {
  def apply(graph: Graph): ScalaGraph =
    ScalaGraph(TraversalSource(graph))
}

case class ScalaGraph(traversalSource: TraversalSource) {
  lazy val traversal = traversalSource
  lazy val graph = traversalSource.graph

  def configure(conf: TraversalSource => TraversalSource) =
    ScalaGraph(conf(TraversalSource(graph)))

  def addVertex(): Vertex =
    traversalSource.underlying.addV().next

  def addVertex(label: String): Vertex =
    traversalSource.underlying.addV(label).next

  def addVertex(properties: (String, Any)*): Vertex = {
    val traversal = traversalSource.underlying.addV()
    properties.foreach { case (key, value) => traversal.property(key, value) }
    traversal.next
  }

  def addVertex(label: String, properties: (String, Any)*): Vertex = {
    val traversal = traversalSource.underlying.addV(label)
    properties.foreach { case (key, value) => traversal.property(key, value) }
    traversal.next
  }

  def addVertex(label: String, properties: Map[String, Any]): Vertex =
    addVertex(label, properties.toSeq: _*)

  def addVertex(properties: Map[String, Any]): Vertex =
    addVertex(properties.toSeq: _*)

  /**
    * Save an object's values as a new vertex
    * Note: `@id` members cannot be set for all graphs (e.g. remote graphs), so it is ignored here generally
    */
  def addVertex[CC <: Product: Marshallable](cc: CC): Vertex = {
    val fromCC = implicitly[Marshallable[CC]].fromCC(cc)
    addVertex(fromCC.label, fromCC.properties: _*)
  }

  def +[CC <: Product: Marshallable](cc: CC): Vertex = addVertex(cc)

  def +(label: String): Vertex = addVertex(label)

  def +(label: String, properties: KeyValue[_]*): Vertex =
    addVertex(label, properties.map(v => (v.key.name, v.value)).toMap)

  /** start a traversal with `addV` */
  def addV(): GremlinScala.Aux[Vertex, HNil] =
    traversalSource.addV()

  /** start a traversal with `addV` */
  def addV(label: String): GremlinScala.Aux[Vertex, HNil] =
    traversalSource.addV(label)

  /** start a traversal with `addV` */
  def addE(label: String): GremlinScala.Aux[Edge, HNil] =
    traversalSource.addE(label)

  /** start a traversal with given `starts`` */
  def inject[S](starts: S*): GremlinScala.Aux[S, HNil] =
    traversalSource.inject(starts: _*)

  /** start traversal with all vertices */
  def V(): GremlinScala.Aux[Vertex, HNil] =
    traversalSource.V()

  /** start traversal with all edges */
  def E(): GremlinScala.Aux[Edge, HNil] =
    traversalSource.E()

  /** start traversal with some vertices identified by given ids */
  def V(vertexIds: Any*): GremlinScala.Aux[Vertex, HNil] =
    traversalSource.V(vertexIds: _*)

  /** start traversal with some edges identified by given ids */
  def E(edgeIds: Any*): GremlinScala.Aux[Edge, HNil] =
    traversalSource.E(edgeIds: _*)

  def tx(): Transaction = graph.tx()

  def variables(): Variables = graph.variables()

  def configuration(): Configuration = graph.configuration()

  def compute[C <: GraphComputer](graphComputerClass: Class[C]): C =
    graph.compute(graphComputerClass)

  def compute(): GraphComputer = graph.compute()

  def close(): Unit = graph.close()

  /* TODO: reimplement with createThreadedTx, if the underlying graph supports it */
  // def transactional[R](work: Graph => R) = graph.tx.submit(work)
}
