package com.tinkerpop.gremlin.scala

import java.lang.{ Long ⇒ JLong }
import java.util.{ Comparator, List ⇒ JList }
import scala.collection.JavaConversions._

import com.tinkerpop.gremlin._
import com.tinkerpop.gremlin.process._
import com.tinkerpop.gremlin.process.graph.GraphTraversal
import com.tinkerpop.gremlin.process.T
import com.tinkerpop.gremlin.structure._
import com.tinkerpop.gremlin.util.function.SConsumer
import com.tinkerpop.gremlin.util.function.SPredicate
import shapeless._
import shapeless.ops.hlist._

case class GremlinScala[Types <: HList, End](traversal: GraphTraversal[_, End]) {
  def toSeq(): Seq[End] = traversal.toList.toSeq
  def toList(): List[End] = traversal.toList.toList
  def toSet(): Set[End] = traversal.toList.toSet
  def head(): End = toList.head
  def headOption(): Option[End] = Option(head)
  /** execute pipeline - applies all side effects */
  def iterate() = {
    traversal.iterate()
    GremlinScala[Types, End](traversal)
  }

  def property[A](key: String)(implicit p: Prepend[Types, Property[A] :: HNil]) =
    GremlinScala[p.Out, Property[A]](traversal.property[A](key))

  def value[A](key: String)(implicit p: Prepend[Types, A :: HNil]) =
    GremlinScala[p.Out, A](traversal.value[A](key))
  def value[A](key: String, default: A)(implicit p: Prepend[Types, A :: HNil]) =
    GremlinScala[p.Out, A](traversal.value[A](key, default))

  def has(key: String) = GremlinScala[Types, End](traversal.has(key))
  def has(key: String, value: Any) = GremlinScala[Types, End](traversal.has(key, value))
  def has(key: String, t: T, value: Any) = GremlinScala[Types, End](traversal.has(key, t, value))
  def has(key: String, t: T, seq: Seq[_]) = GremlinScala[Types, End](traversal.has(key, t, asJavaCollection(seq)))

  def hasNot(key: String) = GremlinScala[Types, End](traversal.hasNot(key))

  def filter(p: End ⇒ Boolean) = GremlinScala[Types, End](traversal.filter(new SPredicate[Traverser[End]] {
    override def test(h: Traverser[End]): Boolean = p(h.get)
  }))

  def count()(implicit p: Prepend[Types, JLong :: HNil]) = GremlinScala[p.Out, JLong](traversal.count())

  def map[A](fun: Traverser[End] ⇒ A)(implicit p: Prepend[Types, A :: HNil]) =
    GremlinScala[p.Out, A](traversal.map[A](fun))

  def path()(implicit p: Prepend[Types, Types :: HNil]) =
    GremlinScala[p.Out, Types](traversal.addStep(new TypedPathStep[End, Types](traversal)))

  def select()(implicit p: Prepend[Types, Path :: HNil]) = GremlinScala[p.Out, Path](traversal.select())

  def select(asLabels: Seq[String])(implicit p: Prepend[Types, Path :: HNil]) = GremlinScala[p.Out, Path](traversal.select(asLabels: JList[String]))

  def order() = GremlinScala[Types, End](traversal.order())
  def order(lessThan: (End, End) ⇒ Boolean) =
    GremlinScala[Types, End](traversal.order(new Comparator[Traverser[End]]() {
      override def compare(a: Traverser[End], b: Traverser[End]) =
        if (lessThan(a.get, b.get)) -1
        else 0
    }))

  def shuffle() = GremlinScala[Types, End](traversal.shuffle())

  def simplePath() = GremlinScala[Types, End](traversal.simplePath())
  def cyclicPath() = GremlinScala[Types, End](traversal.cyclicPath())

  def dedup() = GremlinScala[Types, End](traversal.dedup())
  def dedup[A](uniqueFun: End ⇒ A) = GremlinScala[Types, End](traversal.dedup(uniqueFun))

  def aggregate(variable: String) = GremlinScala[Types, End](traversal.aggregate(variable))
  def aggregate[A](variable: String, fun: End ⇒ A) = GremlinScala[Types, End](traversal.aggregate(variable, fun))

  def except(someObject: End) = GremlinScala[Types, End](traversal.except(someObject))
  def except(list: Iterable[End]) = GremlinScala[Types, End](traversal.except(list))
  /** not named `except` because type End could be String */
  def exceptVar(variable: String) = GremlinScala[Types, End](traversal.except(variable))

  /* startValue: greaterThanEqual
   * endValue: less than */
  def interval[A, B](key: String, startValue: Comparable[A], endValue: Comparable[B]) =
    GremlinScala[Types, End](traversal.interval(key, startValue, endValue))

  /** keeps element on a probabilistic base - probability range: 0.0 (keep none) - 1.0 - keep all */
  def random(probability: Double) = GremlinScala[Types, End](traversal.random(probability))

  def range(low: Int, high: Int) = GremlinScala[Types, End](traversal.range(low, high))

  def retain(variable: String) = GremlinScala[Types, End](traversal.retain(variable))
  def retainOne(retainObject: End) = GremlinScala[Types, End](traversal.retain(retainObject))
  def retainAll(retainCollection: Seq[End]) = GremlinScala[Types, End](traversal.retain(retainCollection))

  def as(name: String) = GremlinScala[Types, End](traversal.as(name))
  def back[A](to: String)(implicit p: Prepend[Types, A :: HNil]) = GremlinScala[p.Out, A](traversal.back[A](to))

  def label()(implicit p: Prepend[Types, String :: HNil]) = GremlinScala[p.Out, String](traversal.label())

  def sideEffect(traverse: Traverser[End] ⇒ Any) = {
    val consumer = new SConsumer[Traverser[End]] {
      override def accept(t: Traverser[End]) = traverse
    }
    GremlinScala[Types, End](traversal.sideEffect(consumer))
  }
  //def sideEffect(consumer: SConsumer[Traverser[End]]) = GremlinScala[Types, End](traversal.sideEffect(consumer))
}

case class ScalaGraph(graph: Graph) extends AnyVal {
  def addVertex(): ScalaVertex = ScalaVertex(graph.addVertex())
  def addVertex(id: AnyRef): ScalaVertex = addVertex(id, Map.empty)
  def addVertex(id: AnyRef, properties: Map[String, Any]): ScalaVertex = {
    val v = ScalaVertex(graph.addVertex(Element.ID, id))
    v.setProperties(properties)
    v
  }

  /** get vertex by id */
  def v(id: AnyRef): Option[ScalaVertex] = graph.v(id) match {
    case v: Vertex ⇒ Some(ScalaVertex(v))
    case _         ⇒ None
  }

  /** get edge by id */
  def e(id: AnyRef): Option[ScalaEdge] = graph.e(id) match {
    case e: Edge ⇒ Some(ScalaEdge(e))
    case _       ⇒ None
  }

  /** get all vertices */
  def V() = GremlinScala[Vertex :: HNil, Vertex](graph.V.asInstanceOf[GraphTraversal[_, Vertex]])
  /** get all edges */
  def E() = GremlinScala[Edge :: HNil, Edge](graph.E.asInstanceOf[GraphTraversal[_, Edge]])
}

object GremlinScala {
  def of(graph: Graph): ScalaGraph = ScalaGraph(graph)

  class GremlinVertexSteps[Types <: HList, End <: Vertex](gremlinScala: GremlinScala[Types, End])
      extends GremlinScala[Types, End](gremlinScala.traversal) {

    def out()(implicit p: Prepend[Types, Vertex :: HNil]) = GremlinScala[p.Out, Vertex](traversal.out())
    def out(labels: String*)(implicit p: Prepend[Types, Vertex :: HNil]) = GremlinScala[p.Out, Vertex](traversal.out(labels: _*))
    def out(branchFactor: Int, labels: String*)(implicit p: Prepend[Types, Vertex :: HNil]) = GremlinScala[p.Out, Vertex](traversal.out(branchFactor, labels: _*))

    def outE()(implicit p: Prepend[Types, Edge :: HNil]) = GremlinScala[p.Out, Edge](traversal.outE())
    def outE(labels: String*)(implicit p: Prepend[Types, Edge :: HNil]) = GremlinScala[p.Out, Edge](traversal.outE(labels: _*))
    def outE(branchFactor: Int, labels: String*)(implicit p: Prepend[Types, Edge :: HNil]) = GremlinScala[p.Out, Edge](traversal.outE(branchFactor, labels: _*))

    def in()(implicit p: Prepend[Types, Vertex :: HNil]) = GremlinScala[p.Out, Vertex](traversal.in())
    def in(labels: String*)(implicit p: Prepend[Types, Vertex :: HNil]) = GremlinScala[p.Out, Vertex](traversal.in(labels: _*))
    def in(branchFactor: Int, labels: String*)(implicit p: Prepend[Types, Vertex :: HNil]) = GremlinScala[p.Out, Vertex](traversal.in(branchFactor, labels: _*))

    def inE()(implicit p: Prepend[Types, Edge :: HNil]) = GremlinScala[p.Out, Edge](traversal.inE())
    def inE(labels: String*)(implicit p: Prepend[Types, Edge :: HNil]) = GremlinScala[p.Out, Edge](traversal.inE(labels: _*))
    def inE(branchFactor: Int, labels: String*)(implicit p: Prepend[Types, Edge :: HNil]) = GremlinScala[p.Out, Edge](traversal.inE(branchFactor, labels: _*))

    def both()(implicit p: Prepend[Types, Vertex :: HNil]) = GremlinScala[p.Out, Vertex](traversal.both())
    def both(labels: String*)(implicit p: Prepend[Types, Vertex :: HNil]) = GremlinScala[p.Out, Vertex](traversal.both(labels: _*))
    def both(branchFactor: Int, labels: String*)(implicit p: Prepend[Types, Vertex :: HNil]) = GremlinScala[p.Out, Vertex](traversal.both(branchFactor, labels: _*))

    def bothE()(implicit p: Prepend[Types, Edge :: HNil]) = GremlinScala[p.Out, Edge](traversal.bothE())
    def bothE(labels: String*)(implicit p: Prepend[Types, Edge :: HNil]) = GremlinScala[p.Out, Edge](traversal.bothE(labels: _*))
    def bothE(branchFactor: Int, labels: String*)(implicit p: Prepend[Types, Edge :: HNil]) = GremlinScala[p.Out, Edge](traversal.bothE(branchFactor, labels: _*))
  }

  class GremlinEdgeSteps[Types <: HList, End <: Edge](gremlinScala: GremlinScala[Types, End])
      extends GremlinScala[Types, End](gremlinScala.traversal) {

    def inV(implicit p: Prepend[Types, Vertex :: HNil]) =
      GremlinScala[p.Out, Vertex](traversal.inV)

    def outV(implicit p: Prepend[Types, Vertex :: HNil]) =
      GremlinScala[p.Out, Vertex](traversal.outV)
  }
}
