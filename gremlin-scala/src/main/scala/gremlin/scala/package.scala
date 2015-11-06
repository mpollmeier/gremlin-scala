package gremlin

import java.util.function.{BiPredicate, Function ⇒ JFunction, Predicate ⇒ JPredicate}

import org.apache.tinkerpop.gremlin.process.traversal
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.structure
import org.apache.tinkerpop.gremlin.structure.VertexProperty
import shapeless._
import shapeless.ops.hlist.IsHCons
import shapeless.ops.hlist.{IsHCons, ToTraversable}
import shapeless.ops.product.ToHList
import shapeless.syntax.std.product.productOps
import _root_.scala.language.implicitConversions

package object scala {
  type Vertex = structure.Vertex
  type Edge = structure.Edge
  type Element = structure.Element
  type Graph = structure.Graph
  type Property[A] = structure.Property[A]
  type Traverser[A] = traversal.Traverser[A]
  type Label = String

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
  implicit class SemiEdgeFunctions(label: Label) {
    def ---(from: Vertex) = SemiEdge(from, label)

    def -->(right: Vertex) = SemiDoubleEdge(right, label)
  }

  implicit class SemiEdgeProductFunctions[
    LabelAndValuesAsTuple <: Product,
    LabelAndValues <: HList,
    Lbl <: String,
    KeyValues <: HList
  ](labelAndValuesAsTuple: LabelAndValuesAsTuple)
  (implicit toHList: ToHList.Aux[LabelAndValuesAsTuple,LabelAndValues],
   startsWithLabel: IsHCons.Aux[LabelAndValues, Lbl, KeyValues], // first element has to be a Label
   keyValueToList: ToTraversable.Aux[KeyValues, List, KeyValue[_]] // all other elements have to be KeyValue[_]
  ) {
    lazy val labelAndValues = labelAndValuesAsTuple.toHList
    lazy val label: String = labelAndValues.head
    lazy val keyValues: KeyValues = labelAndValues.tail
    lazy val properties: List[KeyValue[_]] = keyValues.toList

    def ---(from: Vertex) = SemiEdge(from, label, properties)
    def -->(right: Vertex) = SemiDoubleEdge(right, label, properties)
  }

  // TODO: get back to work? conflicts with other SemiEdgeProductFunctions...
  // implicit class SemiEdgeCcFunctions[T <: Product: Marshallable](cc: T) {
  //   def ---(from: Vertex) = {
  //     val fromCC = implicitly[Marshallable[T]].fromCC(cc)
  //     SemiEdge(from, fromCC.label, fromCC.valueMap.map { r ⇒ Key[Any](r._1) → r._2 }.toSeq)
  //   }

  //   def -->(from: Vertex) = {
  //     val fromCC = implicitly[Marshallable[T]].fromCC(cc)
  //     SemiDoubleEdge(from, fromCC.label, fromCC.valueMap.map { r ⇒ Key[Any](r._1) → r._2 }.toSeq)
  //   }
  // }

}
