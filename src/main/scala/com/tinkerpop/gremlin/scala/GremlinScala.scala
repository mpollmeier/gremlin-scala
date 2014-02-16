package com.tinkerpop.gremlin.scala

import shapeless._
import ops.hlist._
import scala.collection.JavaConversions._
import com.tinkerpop.gremlin._
import com.tinkerpop.gremlin.structure._
import com.tinkerpop.gremlin.process._

case class GremlinScala[Types <: HList, End](traversal: Traversal[_, End]) {
  def toList(): List[End] = traversal.toList.toList
  def toSet(): Set[End] = traversal.toList.toSet
  def head(): End = toList.head

  def property[A](key: String)(implicit p:Prepend[Types, Property[A]::HNil]) =
    GremlinScala[p.Out, Property[A]](traversal.property[A](key))
}

case class ScalaGraph(graph: Graph) {
  /** get vertex by id */
  def v(id: AnyRef) = ScalaVertex(graph.v(id))
  /** get edge by id */
  def e(id: AnyRef) = ScalaEdge(graph.e(id))

  /** get all vertices */
  def V() = GremlinScala[Vertex :: HNil, Vertex](graph.V.asInstanceOf[Traversal[_, Vertex]])
  /** get all edges */
  def E() = GremlinScala[Edge :: HNil, Edge](graph.E.asInstanceOf[Traversal[_, Edge]])
}

trait ScalaElement {
  def element: Element
  def property[A](key: String): Property[A] = element.getProperty[A](key)
  def propertyKeys(): Set[String] = element.getPropertyKeys.toSet
  def properties: Map[String, Any] = element.getProperties.toMap mapValues (_.get)
  def setProperty(key: String, value: Any): Unit = element.setProperty(key, value)
}

case class ScalaVertex(vertex: Vertex) extends ScalaElement {
  override def element = vertex

  def out() = GremlinScala[Vertex :: HNil, Vertex](vertex.out())
  def out(labels: String*) = GremlinScala[Vertex :: HNil, Vertex](vertex.out(labels: _*))
  def out(branchFactor: Int, labels: String*) = GremlinScala[Vertex :: HNil, Vertex](vertex.out(branchFactor, labels: _*))

  def outE() = GremlinScala[Edge :: HNil, Edge](vertex.outE())
  def outE(labels: String*) = GremlinScala[Edge :: HNil, Edge](vertex.outE(labels: _*))
  def outE(branchFactor: Int, labels: String*) = GremlinScala[Edge :: HNil, Edge](vertex.outE(branchFactor, labels: _*))

  def in() = GremlinScala[Vertex :: HNil, Vertex](vertex.in())
  def in(labels: String*) = GremlinScala[Vertex :: HNil, Vertex](vertex.in(labels: _*))
  def in(branchFactor: Int, labels: String*) = GremlinScala[Vertex :: HNil, Vertex](vertex.in(branchFactor, labels: _*))

  def inE() = GremlinScala[Edge :: HNil, Edge](vertex.inE())
  def inE(labels: String*) = GremlinScala[Edge :: HNil, Edge](vertex.inE(labels: _*))
  def inE(branchFactor: Int, labels: String*) = GremlinScala[Edge :: HNil, Edge](vertex.inE(branchFactor, labels: _*))

  def both() = GremlinScala[Vertex :: HNil, Vertex](vertex.both())
  def both(labels: String*) = GremlinScala[Vertex :: HNil, Vertex](vertex.both(labels: _*))
  def both(branchFactor: Int, labels: String*) = GremlinScala[Vertex :: HNil, Vertex](vertex.both(branchFactor, labels: _*))

  def bothE() = GremlinScala[Edge :: HNil, Edge](vertex.bothE())
  def bothE(labels: String*) = GremlinScala[Edge :: HNil, Edge](vertex.bothE(labels: _*))
  def bothE(branchFactor: Int, labels: String*) = GremlinScala[Edge :: HNil, Edge](vertex.bothE(branchFactor, labels: _*))
}

case class ScalaEdge(edge: Edge) extends ScalaElement {
  override def element = edge

  //TODO: wait until this is consistent in T3 between Vertex and Edge
  //currently Vertex.outE returns a Traversal, Edge.inV doesnt quite exist
  //def inV() = GremlinScala[Vertex :: HNil, Vertex](edge.inV())
}

object GremlinScala {
  def of(graph: Graph): ScalaGraph = ScalaGraph(graph)

  implicit class GremlinVertexSteps[Types <: HList, End <: Vertex](gremlinScala: GremlinScala[Types, End])
    extends GremlinScala[Types, End](gremlinScala.traversal) {

    def out()(implicit p:Prepend[Types, Vertex::HNil]) = GremlinScala[p.Out, Vertex](traversal.out())
    def out(labels: String*)(implicit p:Prepend[Types, Vertex::HNil]) = GremlinScala[p.Out, Vertex](traversal.out(labels: _*))
    def out(branchFactor: Int, labels: String*)(implicit p:Prepend[Types, Vertex::HNil]) = GremlinScala[p.Out, Vertex](traversal.out(branchFactor, labels: _*))

    def outE()(implicit p:Prepend[Types, Edge::HNil]) = GremlinScala[p.Out, Edge](traversal.outE())
    def outE(labels: String*)(implicit p:Prepend[Types, Edge::HNil]) = GremlinScala[p.Out, Edge](traversal.outE(labels: _*))
    def outE(branchFactor: Int, labels: String*)(implicit p:Prepend[Types, Edge::HNil]) = GremlinScala[p.Out, Edge](traversal.outE(branchFactor, labels: _*))

    def in()(implicit p:Prepend[Types, Vertex::HNil]) = GremlinScala[p.Out, Vertex](traversal.in())
    def in(labels: String*)(implicit p:Prepend[Types, Vertex::HNil]) = GremlinScala[p.Out, Vertex](traversal.in(labels: _*))
    def in(branchFactor: Int, labels: String*)(implicit p:Prepend[Types, Vertex::HNil]) = GremlinScala[p.Out, Vertex](traversal.in(branchFactor, labels: _*))

    def inE()(implicit p:Prepend[Types, Edge::HNil]) = GremlinScala[p.Out, Edge](traversal.inE())
    def inE(labels: String*)(implicit p:Prepend[Types, Edge::HNil]) = GremlinScala[p.Out, Edge](traversal.inE(labels: _*))
    def inE(branchFactor: Int, labels: String*)(implicit p:Prepend[Types, Edge::HNil]) = GremlinScala[p.Out, Edge](traversal.inE(branchFactor, labels: _*))

    def both()(implicit p:Prepend[Types, Vertex::HNil]) = GremlinScala[p.Out, Vertex](traversal.both())
    def both(labels: String*)(implicit p:Prepend[Types, Vertex::HNil]) = GremlinScala[p.Out, Vertex](traversal.both(labels: _*))
    def both(branchFactor: Int, labels: String*)(implicit p:Prepend[Types, Vertex::HNil]) = GremlinScala[p.Out, Vertex](traversal.both(branchFactor, labels: _*))

    def bothE()(implicit p:Prepend[Types, Edge::HNil]) = GremlinScala[p.Out, Edge](traversal.bothE())
    def bothE(labels: String*)(implicit p:Prepend[Types, Edge::HNil]) = GremlinScala[p.Out, Edge](traversal.bothE(labels: _*))
    def bothE(branchFactor: Int, labels: String*)(implicit p:Prepend[Types, Edge::HNil]) = GremlinScala[p.Out, Edge](traversal.bothE(branchFactor, labels: _*))
  }

  implicit class GremlinEdgeSteps[Types <: HList, End <: Edge](gremlinScala: GremlinScala[Types, End])
    extends GremlinScala[Types, End](gremlinScala.traversal) {

    //def inV(implicit p:Prepend[Types, Vertex::HNil]) = 
      //GremlinScala[p.Out, Vertex](traversal.inV)
  }

}
