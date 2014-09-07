package com.tinkerpop.gremlin

import java.util.function.{ Function ⇒ JFunction }

import com.tinkerpop.gremlin.process.Traverser
import com.tinkerpop.gremlin.scala.GremlinScala._
import com.tinkerpop.gremlin.util.function.SFunction
import com.tinkerpop.gremlin.util.function.SPredicate
import shapeless._
import shapeless.ops.hlist._

package object scala {
  type Vertex = structure.Vertex
  type Edge = structure.Edge
  type Element = structure.Element
  type Graph = structure.Graph

  implicit def wrap(v: Vertex) = ScalaVertex(v)
  implicit def wrap(e: Edge) = ScalaEdge(e)
  implicit def wrap(g: Graph) = ScalaGraph(g)
  implicit def unwrap(g: ScalaGraph) = g.graph

  implicit def toElementSteps[Types <: HList, End <: Element](gremlinScala: GremlinScala[Types, End]) =
    new GremlinElementSteps(gremlinScala)

  implicit def toVertexSteps[Types <: HList, End <: Vertex](gremlinScala: GremlinScala[Types, End]) =
    new GremlinVertexSteps(gremlinScala)

  implicit def toEdgeSteps[Types <: HList, End <: Edge](gremlinScala: GremlinScala[Types, End]) =
    new GremlinEdgeSteps(gremlinScala)

  implicit def toJavaFunction[A, B](f: Function1[A, B]) = new JFunction[A, B] {
    override def apply(a: A): B = f(a)
  }

  implicit def toSFunction[A, B](f: Function1[A, B]) = new SFunction[A, B] {
    override def apply(a: A): B = f(a)
  }

  implicit def toSPredicate[A](f: Function1[A, Boolean]) = new SPredicate[A] {
    override def test(a: A) = f(a)
  }

  implicit def liftTraverser[A, B](fun: A ⇒ B): Traverser[A] ⇒ B =
    { t: Traverser[A] ⇒ fun(t.get) }
}

