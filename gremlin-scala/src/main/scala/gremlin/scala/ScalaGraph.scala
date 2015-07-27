package gremlin.scala

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import shapeless._
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
  def save[A: TypeTag: ClassTag](cc: A): ScalaVertex =
    addVertex().setProperties(MarshallingMacros.marshalling(cc))
//  {
//    val persistableTypes = Seq(
//      typeOf[Option.type],
//      typeOf[String],
//      typeOf[Int],
//      typeOf[Double],
//      typeOf[Float],
//      typeOf[Long],
//      typeOf[Short],
//      typeOf[Char],
//      typeOf[Byte],
//      typeOf[Seq.type],
//      typeOf[Map.type]
//    ) map (_.typeSymbol.fullName)
//
//    val mirror = runtimeMirror(getClass.getClassLoader)
//    val instanceMirror = mirror.reflect(cc)
//
//    // TODO: when we don't need to support scala 2.10 any more, change to: typeOf[A].declarations
//    val params = (typeOf[A].declarations map (_.asTerm) filter (t ⇒ t.isParamAccessor && t.isGetter) map { term ⇒
//      val termName = term.name.decodedName.toString
//      val termType = term.typeSignature.typeSymbol.fullName
//      if (!persistableTypes.contains(termType))
//        throw new IllegalArgumentException(s"The field '$termName: $termType' is not persistable.")
//
//      val fieldMirror = instanceMirror.reflectField(term)
//      termName → (term.typeSignature.typeSymbol.fullName match {
//        case t if t == typeOf[Option.type].typeSymbol.fullName ⇒
//          fieldMirror.get.asInstanceOf[Option[Any]].orNull
//        case _ ⇒ fieldMirror.get
//      })
//    } filter { case (key, value) ⇒ key != "id" && value != null }).toMap + ("label" → cc.getClass.getSimpleName)
//
//    addVertex().setProperties(params)
//  }
}
