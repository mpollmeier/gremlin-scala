package gremlin.scala

import java.util.function.Supplier
import org.apache.commons.configuration.Configuration
import org.apache.tinkerpop.gremlin.process.computer.GraphComputer
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.{ GraphTraversal, GraphTraversalSource }
import org.apache.tinkerpop.gremlin.structure.Graph.Variables
import org.apache.tinkerpop.gremlin.structure.{Transaction, T}
import shapeless._
import scala.collection.JavaConversions._

case class ScalaGraph(graph: Graph) {
  val traversalSource: GraphTraversalSource = graph.traversal()

  /** Make the traverser carry a local data structure.
    * See http://tinkerpop.apache.org/docs/current/reference/#sack-step */
  def withSack[A](initialValue: A): ScalaGraph =
    withNewTraversalSource(traversalSource.withSack(initialValue))

  /** Make the traverser carry a local data structure.
    * See http://tinkerpop.apache.org/docs/current/reference/#sack-step */
  def withSack[A](initialValue: () => A): ScalaGraph =
    withNewTraversalSource(traversalSource.withSack(initialValue: Supplier[A]))

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
  def addVertex[CC <: Product: Marshallable](cc: CC): Vertex = {
    val fromCC = implicitly[Marshallable[CC]].fromCC(cc)
    val idParam = fromCC.id.toSeq flatMap (List(T.id, _))
    val labelParam = Seq(T.label, fromCC.label)
    val params = fromCC.valueMap.toSeq.flatMap(pair ⇒ Seq(pair._1, pair._2.asInstanceOf[AnyRef]))
    graph.addVertex(idParam ++ labelParam ++ params: _*)
  }

  def +[CC <: Product: Marshallable](cc: CC): Vertex = addVertex(cc)

  def +(label: String): Vertex = addVertex(label)

  def +(label: String, properties: KeyValue[_]*): Vertex =
    addVertex(label, properties.map(v ⇒ (v.key.name, v.value)).toMap)

  // start traversal with all vertices 
  def V = GremlinScala[Vertex, HNil](traversalSource.V().asInstanceOf[GraphTraversal[_, Vertex]])

  // start traversal with all edges
  def E = GremlinScala[Edge, HNil](traversalSource.E().asInstanceOf[GraphTraversal[_, Edge]])

  // start traversal with some vertices identified by given ids 
  def V(vertexIds: Any*) =
    GremlinScala[Vertex, HNil](traversalSource.V(vertexIds.asInstanceOf[Seq[AnyRef]]: _*)
      .asInstanceOf[GraphTraversal[_, Vertex]])

  // start traversal with some edges identified by given ids 
  def E(edgeIds: Any*) =
    GremlinScala[Edge, HNil](traversalSource.E(edgeIds.asInstanceOf[Seq[AnyRef]]: _*)
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

  private def withNewTraversalSource(ts: GraphTraversalSource): ScalaGraph =
    new ScalaGraph(graph) {
      override val traversalSource = ts
    }
}
