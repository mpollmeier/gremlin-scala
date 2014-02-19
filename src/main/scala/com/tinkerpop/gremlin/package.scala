package com.tinkerpop.gremlin

import shapeless._
import ops.hlist._
import java.util.function.{Function => JFunction}
import com.tinkerpop.gremlin.scala.GremlinScala._

package object scala {
  type Vertex = structure.Vertex
  type Edge = structure.Edge
  type Graph = structure.Graph
  implicit def toJavaFunction[A,B](f: Function1[A,B]) = new JFunction[A,B] {
    override def apply(a: A): B = f(a)
  }

  implicit def wrap(v: Vertex) = ScalaVertex(v)
  implicit def wrap(e: Edge) = ScalaEdge(e)
  implicit def wrap(g: Graph) = ScalaGraph(g)

  implicit def toVertexSteps[Types <: HList, End <: Vertex](gremlinScala: GremlinScala[Types, End]) =
    new GremlinVertexSteps(gremlinScala)
  implicit def toEdgeSteps[Types <: HList, End <: Edge](gremlinScala: GremlinScala[Types, End]) =
    new GremlinEdgeSteps(gremlinScala)

}

