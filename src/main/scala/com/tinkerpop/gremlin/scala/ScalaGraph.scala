package com.tinkerpop.gremlin.scala

import com.tinkerpop.gremlin.process.graph.GraphTraversal
import shapeless.HNil

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

case class ScalaGraph(graph: Graph) {
  def addVertex() = ScalaVertex(graph.addVertex())

  def addVertex(label: String) = ScalaVertex(graph.addVertex(label))

  def addVertex(label: String, properties: Map[String, Any]): ScalaVertex = {
    val v = addVertex(label)
    v.setProperties(properties)
    v
  }

  def save[A: TypeTag : ClassTag](cc: A): ScalaVertex = {
    val persistableType = Seq(
      typeOf[Option.type],
      typeOf[String],
      typeOf[Int],
      typeOf[Double],
      typeOf[Float],
      typeOf[Long],
      typeOf[Short],
      typeOf[Char],
      typeOf[Byte]
    ) map (_.typeSymbol.fullName)

    val mirror = runtimeMirror(getClass.getClassLoader)
    val instanceMirror = mirror.reflect(cc)

    val params = (typeOf[A].declarations map (_.asTerm) filter (t => t.isParamAccessor && t.isGetter) map { term =>
      val termName = term.name.decoded
      val termType = term.typeSignature.typeSymbol.fullName
      if (!persistableType.contains(termType))
        throw new IllegalArgumentException(s"The field '$termName: $termType' is not persistable.")

      val fieldMirror = instanceMirror.reflectField(term)
      termName -> (term.typeSignature.typeSymbol.fullName match {
        case t if t == typeOf[Option.type].typeSymbol.fullName =>
          fieldMirror.get.asInstanceOf[Option[Any]].orNull
        case _ => fieldMirror.get
      })
    } filter { case (key, value) => key != "id" && value != null}).toMap + ("label" -> cc.getClass.getSimpleName)

    addVertex().setProperties(params)
  }

  // get vertex by id
  def v(id: AnyRef): Option[ScalaVertex] =
    GremlinScala(graph.V(id)).headOption map ScalaVertex.apply

  // get edge by id
  def e(id: AnyRef): Option[ScalaEdge] =
    GremlinScala(graph.E(id)).headOption map ScalaEdge.apply

  // start traversal with all vertices
  def V = GremlinScala[Vertex, HNil](graph.V().asInstanceOf[GraphTraversal[_, Vertex]])

  // start traversal with all edges
  def E = GremlinScala[Edge, HNil](graph.E().asInstanceOf[GraphTraversal[_, Edge]])

  // start traversal with some vertices identified by given ids
  def V(vertexIds: Seq[AnyRef]) = GremlinScala[Vertex, HNil](graph.V(vertexIds: _*).asInstanceOf[GraphTraversal[_, Vertex]])

  // start traversal with some edges identified by given ids
  def E(edgeIds: Seq[AnyRef]) = GremlinScala[Edge, HNil](graph.E(edgeIds: _*).asInstanceOf[GraphTraversal[_, Edge]])
}
