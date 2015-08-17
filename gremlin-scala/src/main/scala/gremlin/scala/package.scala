package gremlin

import java.util.function.{Function ⇒ JFunction, Predicate ⇒ JPredicate, BiPredicate}

import org.apache.tinkerpop.gremlin.structure
import org.apache.tinkerpop.gremlin.process.traversal
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.structure
import shapeless._
import _root_.scala.language.implicitConversions

package object scala {
  type Vertex = structure.Vertex
  type Edge = structure.Edge
  type Element = structure.Element
  type Graph = structure.Graph
  type Property[A] = structure.Property[A]
  type Traverser[A] = traversal.Traverser[A]

  implicit class GraphAsScala[G <: Graph](g: G) {
    def asScala = ScalaGraph(g)
  }

  implicit class GraphAsJava[T <: structure.Graph](g: ScalaGraph[T]) {
    def asJava = g.graph
  }

  implicit class EdgeAsScala(e: Edge) {
    def asScala = ScalaEdge(e)
  }

  implicit class EdgeAsJava(e: ScalaEdge) {
    def asJava = e.edge
  }

  implicit class VertexAsScala(e: Vertex) {
    def asScala = ScalaVertex(e)
  }

  implicit class VertexAsJava(v: ScalaVertex) {
    def asJava = v.vertex
  }

  implicit def wrap(v: Vertex) = ScalaVertex(v)

  implicit def wrap(e: Edge) = ScalaEdge(e)

  implicit def wrap(g: Graph) = ScalaGraph(g)

  implicit def wrap[A](traversal: GraphTraversal[_, A]) = GremlinScala[A, HNil](traversal)

  implicit def toElementSteps[End <: Element, Labels <: HList](gremlinScala: GremlinScala[End, Labels]) =
    new GremlinElementSteps(gremlinScala)

  implicit def toVertexSteps[End <: Vertex, Labels <: HList](gremlinScala: GremlinScala[End, Labels]) =
    new GremlinVertexSteps(gremlinScala)

  implicit def toEdgeSteps[End <: Edge, Labels <: HList](gremlinScala: GremlinScala[End, Labels]) =
    new GremlinEdgeSteps(gremlinScala)

  //TODO make vertexSteps extend elementSteps and return VertexSteps here
  implicit def toElementSteps(v: ScalaVertex): GremlinElementSteps[Vertex, HNil] = v.start()

  implicit def toElementSteps(e: ScalaEdge): GremlinElementSteps[Edge, HNil] = e.start()

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
    (t: Traverser[A]) ⇒ fun(t.get)

  // Marshalling implicits
  implicit class GremlinScalaVertexFunctions(gs: GremlinScala[Vertex, _]) {
    /**
     * Load a vertex values into a case class
     */
    def toCC[T <: Product : Marshallable] = gs map (_.toCC[T])
  }

  implicit class GremlinScalaEdgeFunctions(gs: GremlinScala[Edge, _]) {
    /**
     * Load a edge values into a case class
     */
    def toCC[T <: Product : Marshallable] = gs map (_.toCC[T])
  }

  // Arrow syntax implicits
  implicit class SemiEdgeFunctions(label: String) {
    def ---(from: ScalaVertex) = SemiEdge(from, label)

    def -->(right: ScalaVertex) = SemiDoubleEdge(right, label)
  }

  implicit class SemiEdgePropertiesFunctions(labelProperties: (String, Map[String, Any])) {
    private val (label, properties) = labelProperties

    def ---(from: ScalaVertex) = SemiEdge(from, label, properties)
  }

  implicit class SemiEdgeMapFunctions(properties: Map[String, Any]) {
    def ---(from: ScalaVertex) = SemiEdge(from, properties("label").asInstanceOf[String], properties)
  }

  implicit class SemiEdgeCcFunctions[T <: Product : Marshallable](cc: T) {
    def ---(from: ScalaVertex) = {
      val (id, label, properties) = implicitly[Marshallable[T]].fromCC(cc)
      SemiEdge(from, label, properties)
    }
  }
}
