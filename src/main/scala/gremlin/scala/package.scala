package gremlin

import java.util.function.{ Function ⇒ JFunction, Predicate ⇒ JPredicate, BiPredicate }

import gremlin.scala.GremlinScala._
import org.apache.tinkerpop.gremlin.structure
import org.apache.tinkerpop.gremlin.process.traversal
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import shapeless._
import shapeless.ops.hlist._
import _root_.scala.language.implicitConversions
import _root_.scala.reflect.runtime.universe._

package object scala {
  type Vertex = structure.Vertex
  type Edge = structure.Edge
  type Element = structure.Element
  type Graph = structure.Graph
  type Property[A] = structure.Property[A]
  type Traverser[A] = traversal.Traverser[A]

  implicit def wrap(v: Vertex) = ScalaVertex(v)
  implicit def wrap(e: Edge) = ScalaEdge(e)
  implicit def wrap(g: Graph) = ScalaGraph(g)
  implicit def wrap[A](traversal: GraphTraversal[_,A]) = GremlinScala[A, HNil](traversal)

  implicit def toElementSteps[End <: Element, Labels <: HList](gremlinScala: GremlinScala[End, Labels]) =
    new GremlinElementSteps(gremlinScala)

  implicit def toVertexSteps[End <: Vertex, Labels <: HList](gremlinScala: GremlinScala[End, Labels]) =
    new GremlinVertexSteps(gremlinScala)

  implicit def toEdgeSteps[End <: Edge, Labels <: HList](gremlinScala: GremlinScala[End, Labels]) =
    new GremlinEdgeSteps(gremlinScala)

  //TODO make vertexSteps extend elementSteps and return VertexSteps here
  implicit def toElementSteps(v: ScalaVertex): GremlinElementSteps[Vertex, HNil] = v.start

  implicit def toElementSteps(e: ScalaEdge): GremlinElementSteps[Edge, HNil] = e.start

  implicit def toJavaFunction[A, B](f: Function1[A, B]) = new JFunction[A, B] {
    override def apply(a: A): B = f(a)
  }

  implicit def toJavaPredicate[A](f: Function1[A, Boolean]) = new JPredicate[A] {
    override def test(a: A): Boolean = f(a)
  }

  //converts e.g. `(i: Int, s: String) ⇒ true` into a BiPredicate
  implicit def toJavaBiPredicate[A, B](predicate: (A, B) ⇒ Boolean) =
    new BiPredicate[A, B] {
      def test(a: A, b: B) = predicate(a, b)
    }

  implicit def liftTraverser[A, B](fun: A ⇒ B): Traverser[A] ⇒ B =
    { t: Traverser[A] ⇒ fun(t.get) }

  implicit class GremlinScalaVertexFunctions(gs: GremlinScala[Vertex, _]) {

    // load a vertex values into a case class
    def load[A: TypeTag] = gs.map[A] { case vertex =>
      val mirror = runtimeMirror(getClass.getClassLoader)
      val classA = typeOf[A].typeSymbol.asClass
      val classMirror = mirror.reflectClass(classA)
      val constructor = typeOf[A].declaration(nme.CONSTRUCTOR).asMethod

      val params = constructor.paramss.head map {
        case field if field.name.decodedName.toString == "id" => vertex.id.toString
        case field if field.typeSignature.typeSymbol.fullName == typeOf[Option.type].typeSymbol.fullName =>
          Option(vertex.valueOrElse(field.name.decoded.toString, null))
        case field => vertex.valueOrElse(field.name.decoded.toString, null)
      }

      val constructorMirror = classMirror.reflectConstructor(constructor)
      constructorMirror(params: _*).asInstanceOf[A]

      // TODO: when we don't need to support scala 2.10 any more, change to:
      // val mirror = runtimeMirror(getClass.getClassLoader)
      // val classA = typeOf[A].typeSymbol.asClass
      // val classMirror = mirror.reflectClass(classA)
      // val constructor = typeOf[A].decl(termNames.CONSTRUCTOR).asMethod

      // val params = constructor.paramLists.head map {
      //   case field if field.name.decodedName.toString == "id" => vertex.id.toString
      //   case field if field.typeSignature.typeSymbol.fullName == typeOf[Option.type].typeSymbol.fullName =>
      //     Option(vertex.valueOrElse(field.name.decodedName.toString, null))
      //   case field => vertex.valueOrElse(field.name.decodedName.toString, null)
      // }

      // val constructorMirror = classMirror.reflectConstructor(constructor)
      // constructorMirror(params: _*).asInstanceOf[A]
    }
  }
}
