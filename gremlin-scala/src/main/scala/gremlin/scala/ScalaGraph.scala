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
    * Save an object's values into a new vertex
    * @param cc The case class to persist as a vertex
    *
    * Note: this doesn't work with remote graphs, since remote graphs require you to use
    * traversal steps to add a vertex (e.g. addV), but there's no step to set the ID
    */
  def addVertex[CC <: Product: Marshallable](cc: CC): Vertex = {
    val fromCC = implicitly[Marshallable[CC]].fromCC(cc)
    val idParam = fromCC.id.toSeq.flatMap(List(T.id, _)) //TODO: this will break things
    val labelParam = Seq(T.label, fromCC.label)
    val params = fromCC.valueMap.toSeq.flatMap(pair => Seq(pair._1, pair._2.asInstanceOf[AnyRef]))
    graph.addVertex(idParam ++ labelParam ++ params: _*)
  }

  def +[CC <: Product: Marshallable](cc: CC): Vertex = addVertex(cc)

  def +(label: String): Vertex = addVertex(label)

  def +(label: String, properties: KeyValue[_]*): Vertex =
    addVertex(label, properties.map(v => (v.key.name, v.value)).toMap)

  /** start a traversal with `addV` */
  @deprecated("use `traversal.addV`", "3.3.1.2")
  def addV(): GremlinScala.Aux[Vertex, HNil] =
    traversalSource.addV()

  /** start a traversal with `addV` */
  @deprecated("use `traversal.addV`", "3.3.1.2")
  def addV(label: String): GremlinScala.Aux[Vertex, HNil] =
    traversalSource.addV(label)

  @deprecated("use `traversal.inject`", "3.3.1.2")
  /** start a traversal with given `starts`` */
  def inject[S](starts: S*): GremlinScala.Aux[S, HNil] =
    traversalSource.inject(starts: _*)

  /** start traversal with all vertices */
  @deprecated("use `traversal.V`", "3.3.1.2")
  def V(): GremlinScala.Aux[Vertex, HNil] =
    traversalSource.V()

  /** start traversal with all edges */
  @deprecated("use `traversal.E`", "3.3.1.2")
  def E(): GremlinScala.Aux[Edge, HNil] =
    traversalSource.E()

  /** start traversal with some vertices identified by given ids */
  @deprecated("use `traversal.V`", "3.3.1.2")
  def V(vertexIds: Any*): GremlinScala.Aux[Vertex, HNil] =
    traversalSource.V(vertexIds: _*)

  /** start traversal with some edges identified by given ids */
  @deprecated("use `traversal.E`", "3.3.1.2")
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
