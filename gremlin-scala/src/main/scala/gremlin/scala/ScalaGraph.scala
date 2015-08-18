package gremlin.scala

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.structure.T
import shapeless._

case class ScalaGraph[G <: Graph](graph: G) {

  def addVertex(): ScalaVertex = graph.addVertex()

  def addVertex(label: String): ScalaVertex = graph.addVertex(label)

  def addVertex(properties: Map[String, Any]): ScalaVertex = {
    graph.addVertex(properties.toSeq.flatMap {
      case (k, v) =>
      val label = T.label.name
      val id = T.id.name
      k match {
        case `label` => Seq(T.label, v.asInstanceOf[AnyRef])
        case `id` => Seq(T.id, v.asInstanceOf[AnyRef])
        case _ => Seq(k, v.asInstanceOf[AnyRef])
      }
    }: _*)
  }

  def addVertex(label: String, properties: Map[String, Any]): ScalaVertex =
    addVertex(label).setProperties(properties)

  /**
   * Save an object's values into a new vertex
   *
   * @param cc The case class to persist as a vertex
   * @tparam P
   * @return
   */
  def addVertex[P <: Product : Marshallable](cc: P): ScalaVertex = {
    val (id, label, properties) = implicitly[Marshallable[P]].fromCC(cc)
    val idParam = id.toSeq flatMap (List(T.id, _))
    val params = properties.toSeq.flatMap(pair => Seq(pair._1, pair._2.asInstanceOf[AnyRef]))
    graph.addVertex(idParam ++ (T.label +: label +: params): _*)
  }

  def +(label: String): ScalaVertex = addVertex(label)

  def +(properties: (String, Any)*): ScalaVertex = addVertex(properties.toMap)

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

}
