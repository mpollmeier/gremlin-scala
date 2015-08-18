package gremlin

import java.util.function.{BiPredicate, Function => JFunction, Predicate => JPredicate}

import org.apache.tinkerpop.gremlin.process.traversal
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.structure
import shapeless._

import _root_.scala.language.implicitConversions
import _root_.scala.reflect.runtime.universe._


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
    def ---(from: ScalaVertex) = SemiEdge(from, Map("label" -> label))

    def -->(right: ScalaVertex) = SemiDoubleEdge(right, Map("label" -> label))
  }

  // not sure the best way to implement this!!!
  implicit class SemiEdgeProductFunctions[A <: Product](p: A)(implicit tag: TypeTag[A]) {
    // could we do something with the type tag determine what to do?
    /*
      (p: A) could be:
        (String, Any) = Tuple2
        ((String, Any), (String, Any)) = Tuple2
        case class which is also a Product
    */
    lazy val properties =
      if(p.isInstanceOf[(_, _)]) {
        if(p.productElement(0).isInstanceOf[(_, _)])
          p.productIterator.foldLeft(Map.empty[String, Any]) {
            (m, a) => a match {
              case (k,v) => m.updated(k.asInstanceOf[String], v)
            }
          }
        else Map(p.asInstanceOf[(String, Any)])
      }
      else ??? // this is the case class

    def ---(from: ScalaVertex) = SemiEdge(from, properties)
  }

/*
  // This is not best because if the Product is a
  implicit class SemiEdgeProductFunctions(p: Product) {
    lazy val properties = p.productIterator.foldLeft(Map[String, Any]()) {
      (m, a) => a match {
        case (k, v) => m.updated(k.asInstanceOf[String], v)
        case b: String => m.updated(b, p.productElement(1))
      }
    }

    def ---(from: ScalaVertex) = SemiEdge(from, properties)
  }
*/

/*
  implicit class SemiEdgeCcFunctions[T <: Product : Marshallable](cc: T) {
    def ---(from: ScalaVertex) = {
      val (id, label, properties) = implicitly[Marshallable[T]].fromCC(cc)
      SemiEdge(from, properties.updated("label", label))
    }
  }
*/
}
