package com.tinkerpop.gremlin

import java.util.function.{BiPredicate, Function => JFunction, Predicate => JPredicate}
import com.tinkerpop.gremlin.process.graph.GraphTraversal
import com.tinkerpop.gremlin.scala.GremlinScala._
import shapeless._
import _root_.scala.language.implicitConversions
import _root_.scala.reflect.runtime.universe._

package object scala {
  type Vertex = structure.Vertex
  type Edge = structure.Edge
  type Element = structure.Element
  type Graph = structure.Graph
  type Property[A] = structure.Property[A]
  type Traverser[A] = process.Traverser[A]

  implicit def wrap(v: Vertex): ScalaVertex = ScalaVertex(v)
  implicit def wrap(e: Edge): ScalaEdge = ScalaEdge(e)
  implicit def wrap(g: Graph): ScalaGraph = ScalaGraph(g)
  implicit def wrap[A](traversal: GraphTraversal[_, A]): GremlinScala[A, HNil] = GremlinScala[A, HNil](traversal)
  implicit def unwrap(g: ScalaGraph): Graph = g.graph

  implicit def toElementSteps[End <: Element, Labels <: HList](gremlinScala: GremlinScala[End, Labels]): GremlinElementSteps[End, Labels] =
    new GremlinElementSteps(gremlinScala)

  implicit def toVertexSteps[End <: Vertex, Labels <: HList](gremlinScala: GremlinScala[End, Labels]): GremlinVertexSteps[End, Labels] =
    new GremlinVertexSteps(gremlinScala)

  implicit def toEdgeSteps[End <: Edge, Labels <: HList](gremlinScala: GremlinScala[End, Labels]): GremlinEdgeSteps[End, Labels] =
    new GremlinEdgeSteps(gremlinScala)

  //TODO make vertexSteps extend elementSteps and return VertexSteps here
  implicit def toElementSteps(v: ScalaVertex): GremlinElementSteps[Vertex, HNil] = v.start()

  implicit def toElementSteps(e: ScalaEdge): GremlinElementSteps[Edge, HNil] = e.start()

  implicit def toJavaFunction[A, B](f: A => B): JFunction[A, B] with Object {def apply(a: A): B} = new JFunction[A, B] {
    override def apply(a: A): B = f(a)
  }

  implicit def toJavaPredicate[A](f: A => Boolean): JPredicate[A] with Object {def test(a: A): Boolean} = new JPredicate[A] {
    override def test(a: A): Boolean = f(a)
  }

  //converts e.g. `(i: Int, s: String) ⇒ true` into a BiPredicate
  implicit def toJavaBiPredicate[A, B](predicate: (A, B) ⇒ Boolean): BiPredicate[A, B] with Object {def test(a: A, b: B): Boolean} =
    new BiPredicate[A, B] {
      def test(a: A, b: B) = predicate(a, b)
    }

  implicit def liftTraverser[A, B](fun: A ⇒ B): Traverser[A] ⇒ B = { t: Traverser[A] ⇒ fun(t.get) }

  implicit class GremlinScalaVertexFunctions(gs: GremlinScala[Vertex, _]) {
    def load[A: TypeTag] = gs.map[A] { case vertex =>
      val mirror = runtimeMirror(getClass.getClassLoader)
      val classA = typeOf[A].typeSymbol.asClass
      val classMirror = mirror.reflectClass(classA)
      val constructor = typeOf[A].declaration(nme.CONSTRUCTOR).asMethod

      val params = constructor.paramss.head map {
        case field if field.name.decoded == "id" => vertex.id.toString
        case field if field.typeSignature.typeSymbol.fullName == typeOf[Option.type].typeSymbol.fullName =>
          Option(vertex.valueOrElse(field.name.decoded, null))
        case field => vertex.valueOrElse(field.name.decoded, null)
      }

      val constructorMirror = classMirror.reflectConstructor(constructor)
      constructorMirror(params: _*).asInstanceOf[A]
    }
  }
}
