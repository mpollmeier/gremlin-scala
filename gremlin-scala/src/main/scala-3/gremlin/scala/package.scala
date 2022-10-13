package gremlin

import java.util.function.{BiConsumer, BiFunction, BiPredicate, BinaryOperator, Consumer, Supplier, UnaryOperator, Function as JFunction, Predicate as JPredicate}
import org.apache.tinkerpop.gremlin.process.traversal
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.structure

import _root_.scala.language.implicitConversions
import _root_.scala.annotation.targetName

package object scala {
  type Vertex = structure.Vertex
  type Edge = structure.Edge
  type Element = structure.Element
  type Graph = structure.Graph
  type Property[A] = structure.Property[A]
  type Traverser[A] = traversal.Traverser[A]
  type Label = String
  type P[A] = traversal.P[A]

  extension(g: Graph) def asScala() = ScalaGraph(g)
  extension(g: ScalaGraph) def asJava() = g.graph
  extension(e: Edge) def asScala() = ScalaEdge(e)
  extension(e: ScalaEdge) def asJava() = e.edge
  extension(e: Vertex) def asScala() = ScalaVertex(e)
  extension(v: ScalaVertex) def asJava() = v.vertex

  extension[A](property: Property[A])
    def toOption: Option[A] =
      if (property.isPresent) Some(property.value)
      else None

  // to create a new anonymous traversal, e.g. `__.outE`
  def __[A](): GremlinScala.Aux[A, EmptyTuple] =
    GremlinScala[A, EmptyTuple](org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.start[A]())

  def __[A](starts: A*): GremlinScala.Aux[A, EmptyTuple] =
    GremlinScala[A, EmptyTuple](
      org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__
        .__[A](starts: _*))

  given Conversion[Vertex, ScalaVertex] = ScalaVertex(_)
  given Conversion[Edge, ScalaEdge] = ScalaEdge(_)
  given Conversion[Graph, ScalaGraph] = ScalaGraph(_)
  given [A]: Conversion[GraphTraversal[_, A], GremlinScala.Aux[A, EmptyTuple]] with
    def apply(traversal: GraphTraversal[_, A]) = GremlinScala[A, EmptyTuple](traversal)

  given[A]: Conversion[() => A, Supplier[A]] with
    def apply(fn: () => A) = () => fn()

  given[A]: Conversion[A => Unit, Consumer[A]] with
    def apply(fn: A => Unit) = (a: A) => fn(a)

  given toJavaFunction[A,B]: Conversion[A => B, JFunction[A, B]] with
    def apply(fn: A => B) = (a: A) => fn(a)

  given[A]: Conversion[A => A, UnaryOperator[A]] with
    def apply(fn: A => A) = (a: A) => fn(a)

  given[A]: Conversion[(A,A) => A, BinaryOperator[A]] with
    def apply(fn: (A,A) => A) = (a1: A, a2: A) => fn(a1, a2)

  given[A, B, C]: Conversion[(A,B) => C, BiFunction[A, B, C]] with
    def apply(fn: (A,B) => C) = (a: A, b: B) => fn(a, b)

  given toJavaBiConsumer[A, B]: Conversion[(A, B) => Unit, BiConsumer[A, B]] with
    def apply(fn: (A, B) => Unit) = (a: A, b: B) => fn(a, b)

  given[A]: Conversion[A => Boolean, JPredicate[A]] with
    def apply(fn: A => Boolean) = (a: A) => fn(a)

  given toJavaBiPredicate[A, B]: Conversion[(A,B) => Boolean, BiPredicate[A, B]] with
    def apply(fn: (A,B) => Boolean) = (a: A, b: B) => fn(a,b)

  given liftTraverser[A, B]: Conversion[A => B, Traverser[A] => B] with
    def apply(fn: A => B) = (t: Traverser[A]) => fn(t.get)


  // Marshalling implicits
  extension (gs: GremlinScala[Vertex])
    @targetName("vertexToCC") def toCC[CC <: Product: Marshallable] = gs.map(_.toCC[CC])

  extension (gs: GremlinScala[Edge])
    @targetName("edgeToCC") def toCC[CC <: Product : Marshallable] = gs.map(_.toCC[CC])

  // Arrow syntax implicits
  extension(label: Label)
    def ---(from: Vertex) = SemiEdge(from, label)
    def -->(right: Vertex) = SemiDoubleEdge(right, label)

//  implicit class SemiEdgeProductFunctions[
//      LabelAndValuesAsTuple <: Product,
//      LabelAndValues <: Tuple,
//      Lbl <: String,
//      KeyValues <: Tuple
//  ](labelAndValuesAsTuple: LabelAndValuesAsTuple)(
//      implicit toHList: ToHList.Aux[LabelAndValuesAsTuple, LabelAndValues],
//      startsWithLabel: IsHCons.Aux[LabelAndValues, Lbl, KeyValues], // first element has to be a Label
//      keyValueToList: ToTraversable.Aux[KeyValues, List, KeyValue[_]] // all other elements have to be KeyValue[_]
//  ) {
//    lazy val labelAndValues = labelAndValuesAsTuple.toHList
//    lazy val label: String = labelAndValues.head
//    lazy val keyValues: KeyValues = labelAndValues.tail
//    lazy val properties: List[KeyValue[_]] = keyValues.toList
//
//    def ---(from: Vertex) = SemiEdge(from, label, properties: _*)
//    def -->(right: Vertex) = SemiDoubleEdge(right, label, properties: _*)
//  }
}
