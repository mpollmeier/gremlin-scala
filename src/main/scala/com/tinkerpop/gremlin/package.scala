package com.tinkerpop.gremlin

import java.util.function.{Function => JFunction}
import com.tinkerpop.gremlin.structure._

package object scala {
  implicit def toJavaFunction[A,B](f: Function1[A,B]) = new JFunction[A,B] {
    override def apply(a: A): B = f(a)
  }

  implicit def wrap(v: Vertex) = ScalaVertex(v)
  implicit def wrap(e: Edge) = ScalaEdge(e)
  implicit def wrap(g: Graph) = ScalaGraph(g)
}

