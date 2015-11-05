package gremlin

import java.util.function.{BiPredicate, Function ⇒ JFunction, Predicate ⇒ JPredicate}

import org.apache.tinkerpop.gremlin.process.traversal
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.structure
import org.apache.tinkerpop.gremlin.structure.VertexProperty
import shapeless._
import _root_.scala.language.implicitConversions

package object scala {
  type Vertex = structure.Vertex
  type Edge = structure.Edge
  type Element = structure.Element
  type Graph = structure.Graph
  type Property[A] = structure.Property[A]
  type Traverser[A] = traversal.Traverser[A]

  implicit class KeyOps[A](key: Key[A]) {
    def →(value: A): KeyValue[A] = KeyValue(key, value)

    def ->(value: A): KeyValue[A] = KeyValue(key, value)

    def of(value: A): KeyValue[A] = KeyValue(key, value)
  }

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

  implicit class PropertyAsScala[A](property: Property[A]) {
    def toOption: Option[A] =
      if (property.isPresent) Some(property.value)
      else None
  }

  // create a new anonymous traversal, e.g. `__.outE`
  // only defined here so that user doesn't need to import it
  def __[A]() = {
    org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.start[A]()
  }

  // only defined here so that user doesn't need to import it
  def __[A](a: A) = {
    org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.__[A](a)
  }

  implicit def wrap(v: Vertex): ScalaVertex = ScalaVertex(v)

  implicit def wrap(e: Edge): ScalaEdge = ScalaEdge(e)

  implicit def wrap(g: Graph): ScalaGraph[Graph] = ScalaGraph(g)

  implicit def wrap[A](traversal: GraphTraversal[_, A]): GremlinScala[A, HNil] = GremlinScala[A, HNil](traversal)

  implicit def toElementSteps[End <: Element, Labels <: HList](gremlinScala: GremlinScala[End, Labels]): GremlinElementSteps[End, Labels] =
    new GremlinElementSteps(gremlinScala)

  implicit def toVertexSteps[End <: Vertex, Labels <: HList](gremlinScala: GremlinScala[End, Labels]): GremlinVertexSteps[End, Labels] =
    new GremlinVertexSteps(gremlinScala)

  implicit def toEdgeSteps[End <: Edge, Labels <: HList](gremlinScala: GremlinScala[End, Labels]): GremlinEdgeSteps[End, Labels] =
    new GremlinEdgeSteps(gremlinScala)

  implicit def toNumberSteps[End <: Number, Labels <: HList](gremlinScala: GremlinScala[End, Labels]): GremlinNumberSteps[End, Labels] =
    new GremlinNumberSteps(gremlinScala)

  //TODO make vertexSteps extend elementSteps and return VertexSteps here
  implicit def toElementSteps(v: ScalaVertex): GremlinElementSteps[Vertex, HNil] = v.start()

  implicit def toElementSteps(e: ScalaEdge): GremlinElementSteps[Edge, HNil] = e.start()

  implicit def toJavaFunction[A, B](f: A ⇒ B): JFunction[A, B] = new JFunction[A, B] {
    override def apply(a: A): B = f(a)
  }

  implicit def toJavaPredicate[A](f: A ⇒ Boolean): JPredicate[A] = new JPredicate[A] {
    override def test(a: A): Boolean = f(a)
  }

  //converts e.g. `(i: Int, s: String) => true` into a BiPredicate
  implicit def toJavaBiPredicate[A, B](predicate: (A, B) ⇒ Boolean): BiPredicate[A, B] =
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
    def toCC[T <: Product: Marshallable] = gs map (_.toCC[T])
  }

  implicit class GremlinScalaEdgeFunctions(gs: GremlinScala[Edge, _]) {
    /**
      * Load a edge values into a case class
      */
    def toCC[T <: Product: Marshallable] = gs map (_.toCC[T])
  }

  // Arrow syntax implicits
  implicit class SemiEdgeFunctions(label: String) {
    def ---(from: Vertex) = SemiEdge(from, label)

    def -->(right: Vertex) = SemiDoubleEdge(right, label)
  }

  implicit class SemiEdgeProductFunctions[A <: Product](t: (String, A)) {
    private val label = t._1
    private val keys = t._2

    // this is the price we pay for nice syntax a la `paris <-- ("eurostar", Name → "test") --- london`
    private lazy val properties: Seq[KeyValue[Any]] = {
      keys match {
        case k: KeyValue[_] ⇒ Seq(k.asInstanceOf[KeyValue[Any]])
        //casting to get rid of type erasure warning...
        case _ ⇒ keys.productIterator.foldLeft(Seq[KeyValue[Any]]()) { (m, prop) ⇒
          prop match {
            case (k: Key[_], v) ⇒ m :+ (k.asInstanceOf[Key[Any]] → v)
            case k: KeyValue[_] ⇒ m :+ k.asInstanceOf[KeyValue[Any]]
          }
        }
      }
    }

    def ---(from: Vertex) = SemiEdge(from, label, properties)
  }

  implicit class SemiEdgeCcFunctions[T <: Product: Marshallable](cc: T) {
    def ---(from: Vertex) = {
      val fromCC = implicitly[Marshallable[T]].fromCC(cc)
      SemiEdge(from, fromCC.label, fromCC.valueMap.map { r ⇒ Key[Any](r._1) → r._2 }.toSeq)
    }

    def -->(from: Vertex) = {
      val fromCC = implicitly[Marshallable[T]].fromCC(cc)
      SemiDoubleEdge(from, fromCC.label, fromCC.valueMap.map { r ⇒ Key[Any](r._1) → r._2 }.toSeq)
    }
  }

}
