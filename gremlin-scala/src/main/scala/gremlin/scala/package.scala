package gremlin

import java.util.function.{
  BiConsumer,
  BiPredicate,
  BiFunction,
  BinaryOperator,
  Consumer,
  Function => JFunction,
  Predicate => JPredicate,
  Supplier,
  UnaryOperator
}
import org.apache.tinkerpop.gremlin.process.traversal
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.structure
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
  type P[A] = traversal.P[A]

  implicit class GraphAsScala[G <: Graph](g: G) {
    def asScala() = ScalaGraph(g)
  }

  implicit class GraphAsJava(g: ScalaGraph) {
    def asJava() = g.graph
  }

  implicit class EdgeAsScala(e: Edge) {
    def asScala() = ScalaEdge(e)
  }

  implicit class EdgeAsJava(e: ScalaEdge) {
    def asJava() = e.edge
  }

  implicit class VertexAsScala(e: Vertex) {
    def asScala() = ScalaVertex(e)
  }

  implicit class VertexAsJava(v: ScalaVertex) {
    def asJava() = v.vertex
  }

  implicit class PropertyOps[A](property: Property[A]) {
    def toOption: Option[A] =
      if (property.isPresent) Some(property.value)
      else None
  }

  // to create a new anonymous traversal, e.g. `__.outE`
  def __[A](): GremlinScala.Aux[A, HNil] =
    GremlinScala[A, HNil](org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.start[A]())

  def __[A](starts: A*): GremlinScala.Aux[A, HNil] =
    GremlinScala[A, HNil](
      org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__
        .__[A](starts: _*))

  implicit def asScalaVertex(v: Vertex): ScalaVertex = ScalaVertex(v)

  implicit def asScalaEdge(e: Edge): ScalaEdge = ScalaEdge(e)

  implicit def asScalaGraph(g: Graph): ScalaGraph = ScalaGraph(g)

  implicit def asGremlinScala[A](traversal: GraphTraversal[_, A]): GremlinScala.Aux[A, HNil] =
    GremlinScala[A, HNil](traversal)

  implicit def toSupplier[A](f: () => A): Supplier[A] = new Supplier[A] {
    override def get(): A = f()
  }

  implicit def toConsumer[A](f: A => Unit): Consumer[A] = new Consumer[A] {
    override def accept(a: A): Unit = f(a)
  }

  implicit def toJavaFunction[A, B](f: A => B): JFunction[A, B] =
    new JFunction[A, B] {
      override def apply(a: A): B = f(a)
    }

  implicit def toJavaUnaryOperator[A](f: A => A): UnaryOperator[A] =
    new UnaryOperator[A] {
      override def apply(a: A): A = f(a)
    }

  implicit def toJavaBinaryOperator[A](f: (A, A) => A): BinaryOperator[A] =
    new BinaryOperator[A] {
      override def apply(a1: A, a2: A): A = f(a1, a2)
    }

  implicit def toJavaBiFunction[A, B, C](f: (A, B) => C): BiFunction[A, B, C] =
    new BiFunction[A, B, C] {
      override def apply(a: A, b: B): C = f(a, b)
    }

  implicit def toJavaBiConsumer[A, B](f: (A, B) => Unit): BiConsumer[A, B] =
    new BiConsumer[A, B] {
      override def accept(a: A, b: B): Unit = f(a, b)
    }

  implicit def toJavaPredicate[A](f: A => Boolean): JPredicate[A] =
    new JPredicate[A] {
      override def test(a: A): Boolean = f(a)
    }

  implicit def toJavaBiPredicate[A, B](predicate: (A, B) => Boolean): BiPredicate[A, B] =
    new BiPredicate[A, B] {
      def test(a: A, b: B) = predicate(a, b)
    }

  implicit def liftTraverser[A, B](fun: A => B): Traverser[A] => B =
    (t: Traverser[A]) => fun(t.get)

  // Marshalling implicits
  implicit class GremlinScalaVertexFunctions(val gs: GremlinScala[Vertex]) {

    /**
      * Load a vertex values into a case class
      */
    def toCC[CC <: Product: Marshallable] = gs.map(_.toCC[CC])
  }

  implicit class GremlinScalaEdgeFunctions(val gs: GremlinScala[Edge]) {

    /**
      * Load a edge values into a case class
      */
    def toCC[CC <: Product: Marshallable] = gs.map(_.toCC[CC])
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
  ](labelAndValuesAsTuple: LabelAndValuesAsTuple)(
      implicit toHList: ToHList.Aux[LabelAndValuesAsTuple, LabelAndValues],
      startsWithLabel: IsHCons.Aux[LabelAndValues, Lbl, KeyValues], // first element has to be a Label
      keyValueToList: ToTraversable.Aux[KeyValues, List, KeyValue[_]] // all other elements have to be KeyValue[_]
  ) {
    lazy val labelAndValues = labelAndValuesAsTuple.toHList
    lazy val label: String = labelAndValues.head
    lazy val keyValues: KeyValues = labelAndValues.tail
    lazy val properties: List[KeyValue[_]] = keyValues.toList

    def ---(from: Vertex) = SemiEdge(from, label, properties: _*)
    def -->(right: Vertex) = SemiDoubleEdge(right, label, properties: _*)
  }
}
