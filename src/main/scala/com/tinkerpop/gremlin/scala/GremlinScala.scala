package com.tinkerpop.gremlin.scala

import shapeless._
import ops.hlist._
import scala.collection.JavaConversions._
import com.tinkerpop.gremlin._
import com.tinkerpop.blueprints._

case class GremlinScala[Types <: HList, End](pipeline: Pipeline[_, End]) {
  def toList(): List[End] = pipeline.toList.toList
  def head(): End = toList.head

  //TODO: provide own PropertyPipe that converts from blueprints property to Option?
  //def property
  def property[A](key: String)(implicit p:Prepend[Types, Property[A]::HNil]) =
    GremlinScala[p.Out, Property[A]](pipeline.property[A](key))
}

object GremlinScala {
  def of(graph: Graph): GremlinScala[Graph :: HNil, Graph] = {
    val gremlin = GremlinJ.of(graph).asInstanceOf[GremlinJ[Graph, Graph]]
    GremlinScala[Graph :: HNil, Graph](gremlin)
  }

  implicit class GremlinEdgeSteps[Types <: HList, End <: Edge](gremlinScala: GremlinScala[Types, End])
    extends GremlinScala[Types, End](gremlinScala.pipeline) {

    def inV(implicit p:Prepend[Types, Vertex::HNil]) = 
      GremlinScala[p.Out, Vertex](pipeline.inV)
  }

  implicit class GremlinVertexSteps[Types <: HList, End <: Vertex](gremlinScala: GremlinScala[Types, End])
    extends GremlinScala[Types, End](gremlinScala.pipeline) {

    def outE(implicit p:Prepend[Types, Edge::HNil]) = 
      GremlinScala[p.Out, Edge](pipeline.outE())

    def out(implicit p:Prepend[Types, Vertex::HNil]) = 
      GremlinScala[p.Out, Vertex](pipeline.out())
  }

  implicit class GremlinGraphSteps[Types <: HList, End <: Graph](gremlinScala: GremlinScala[Types, End])
    extends GremlinScala[Types, End](gremlinScala.pipeline) {

    /** get vertices by id */
    def v(ids: AnyRef*)(implicit p:Prepend[Types, Vertex::HNil]) =
      GremlinScala[p.Out, Vertex](pipeline.v(ids: _*))

    /** get all vertices */
    def V(implicit p:Prepend[Types, Vertex::HNil]) =
      GremlinScala[p.Out, Vertex](pipeline.V)
  }
}
