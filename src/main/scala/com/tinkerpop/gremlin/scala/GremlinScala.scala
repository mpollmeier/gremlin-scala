package com.tinkerpop.gremlin.scala

import shapeless._
import ops.hlist._
import scala.collection.JavaConversions._
import com.tinkerpop.gremlin._
import com.tinkerpop.gremlin.structure._
import com.tinkerpop.gremlin.process._

case class GremlinScala[Types <: HList, End](pipeline: Traversal[_, End]) {
  def toList(): List[End] = pipeline.toList.toList
  def toSet(): Set[End] = pipeline.toList.toSet
  def head(): End = toList.head

  def property[A](key: String)(implicit p:Prepend[Types, Property[A]::HNil]) =
    GremlinScala[p.Out, Property[A]](pipeline.property[A](key))
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

  def in() = GremlinScala[Vertex :: HNil, Vertex](vertex.in())
  def in(branchFactor: Int) = GremlinScala[Vertex :: HNil, Vertex](vertex.in(branchFactor))

  def both() = GremlinScala[Vertex :: HNil, Vertex](vertex.both())
  def both(branchFactor: Int) = GremlinScala[Vertex :: HNil, Vertex](vertex.both(branchFactor))
}

object GremlinScala {
  def of(graph: Graph): ScalaGraph = ScalaGraph(graph)

  implicit class GremlinVertexSteps[Types <: HList, End <: Vertex](gremlinScala: GremlinScala[Types, End])
    extends GremlinScala[Types, End](gremlinScala.pipeline) {

    def out()(implicit p:Prepend[Types, Vertex::HNil]) =
      GremlinScala[p.Out, Vertex](pipeline.out())
    def out(branchFactor: Int)(implicit p:Prepend[Types, Vertex::HNil]) =
      GremlinScala[p.Out, Vertex](pipeline.out(branchFactor))

    //def outE()(implicit p:Prepend[Types, Edge::HNil]) =
      //GremlinScala[p.Out, Edge](pipeline.outE())
    //def outE(branchFactor: Int)(implicit p:Prepend[Types, Edge::HNil]) = 
      //GremlinScala[p.Out, Edge](pipeline.outE(branchFactor))

    def in()(implicit p:Prepend[Types, Vertex::HNil]) =
      GremlinScala[p.Out, Vertex](pipeline.in())
    def in(branchFactor: Int)(implicit p:Prepend[Types, Vertex::HNil]) =
      GremlinScala[p.Out, Vertex](pipeline.in(branchFactor))

    def both()(implicit p:Prepend[Types, Vertex::HNil]) =
      GremlinScala[p.Out, Vertex](pipeline.both())
    def both(branchFactor: Int)(implicit p:Prepend[Types, Vertex::HNil]) =
      GremlinScala[p.Out, Vertex](pipeline.both(branchFactor))
  }

  implicit class GremlinEdgeSteps[Types <: HList, End <: Edge](gremlinScala: GremlinScala[Types, End])
    extends GremlinScala[Types, End](gremlinScala.pipeline) {

    //def inV(implicit p:Prepend[Types, Vertex::HNil]) = 
      //GremlinScala[p.Out, Vertex](pipeline.inV)
  }

}
