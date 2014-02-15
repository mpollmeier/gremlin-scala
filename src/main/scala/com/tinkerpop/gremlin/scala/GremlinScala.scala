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

  /** get all vertices */
  def V() = GremlinScala[Vertex :: HNil, Vertex](graph.V.asInstanceOf[Traversal[_, Vertex]])
}

case class ScalaVertex(vertex: Vertex) {
  def out() = GremlinScala[Vertex :: HNil, Vertex](vertex.out())
  def out(branchFactor: Int) = GremlinScala[Vertex :: HNil, Vertex](vertex.out(branchFactor))

  def outE() = GremlinScala[Edge :: HNil, Edge](vertex.outE())
  def outE(branchFactor: Int) = GremlinScala[Edge :: HNil, Edge](vertex.outE(branchFactor))

  def in() = GremlinScala[Vertex :: HNil, Vertex](vertex.in())
  def in(branchFactor: Int) = GremlinScala[Vertex :: HNil, Vertex](vertex.in(branchFactor))

  def inE() = GremlinScala[Edge :: HNil, Edge](vertex.inE())
  def inE(branchFactor: Int) = GremlinScala[Edge :: HNil, Edge](vertex.inE(branchFactor))

  def both() = GremlinScala[Vertex :: HNil, Vertex](vertex.both())
  def both(branchFactor: Int) = GremlinScala[Vertex :: HNil, Vertex](vertex.both(branchFactor))

  def bothE() = GremlinScala[Edge :: HNil, Edge](vertex.bothE())
  def bothE(branchFactor: Int) = GremlinScala[Edge :: HNil, Edge](vertex.bothE(branchFactor))
}

object GremlinScala {
  def of(graph: Graph): ScalaGraph = ScalaGraph(graph)

  implicit class GremlinVertexSteps[Types <: HList, End <: Vertex](gremlinScala: GremlinScala[Types, End])
    extends GremlinScala[Types, End](gremlinScala.traversal) {

    def out()(implicit p:Prepend[Types, Vertex::HNil]) =
      GremlinScala[p.Out, Vertex](traversal.out())
    def out(branchFactor: Int)(implicit p:Prepend[Types, Vertex::HNil]) =
      GremlinScala[p.Out, Vertex](traversal.out(branchFactor))

    def outE()(implicit p:Prepend[Types, Edge::HNil]) =
      GremlinScala[p.Out, Edge](traversal.outE())
    def outE(branchFactor: Int)(implicit p:Prepend[Types, Edge::HNil]) = 
      GremlinScala[p.Out, Edge](traversal.outE(branchFactor))

    def in()(implicit p:Prepend[Types, Vertex::HNil]) =
      GremlinScala[p.Out, Vertex](traversal.in())
    def in(branchFactor: Int)(implicit p:Prepend[Types, Vertex::HNil]) =
      GremlinScala[p.Out, Vertex](traversal.in(branchFactor))

    def inE()(implicit p:Prepend[Types, Edge::HNil]) =
      GremlinScala[p.Out, Edge](traversal.inE())
    def inE(branchFactor: Int)(implicit p:Prepend[Types, Edge::HNil]) =
      GremlinScala[p.Out, Edge](traversal.inE(branchFactor))

    def both()(implicit p:Prepend[Types, Vertex::HNil]) =
      GremlinScala[p.Out, Vertex](traversal.both())
    def both(branchFactor: Int)(implicit p:Prepend[Types, Vertex::HNil]) =
      GremlinScala[p.Out, Vertex](traversal.both(branchFactor))

    def bothE()(implicit p:Prepend[Types, Edge::HNil]) =
      GremlinScala[p.Out, Edge](traversal.bothE())
    def bothE(branchFactor: Int)(implicit p:Prepend[Types, Edge::HNil]) =
      GremlinScala[p.Out, Edge](traversal.bothE(branchFactor))
  }

  implicit class GremlinEdgeSteps[Types <: HList, End <: Edge](gremlinScala: GremlinScala[Types, End])
    extends GremlinScala[Types, End](gremlinScala.traversal) {

    //def inV(implicit p:Prepend[Types, Vertex::HNil]) = 
      //GremlinScala[p.Out, Vertex](traversal.inV)
  }

}
