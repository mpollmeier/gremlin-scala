package com.tinkerpop.gremlin.scala

import java.lang.{ Long ⇒ JLong }
import java.util.function.{ Predicate ⇒ JPredicate, Consumer ⇒ JConsumer, BiPredicate, Supplier }
import java.util.{ Comparator, List ⇒ JList, Map ⇒ JMap, Collection ⇒ JCollection }

import collection.JavaConversions._
import collection.mutable
import com.tinkerpop.gremlin._
import com.tinkerpop.gremlin.process._
import com.tinkerpop.gremlin.process.graph.GraphTraversal
import com.tinkerpop.gremlin.process.T
import com.tinkerpop.gremlin.structure._
import shapeless._
import shapeless.ops.hlist._

case class GremlinScala[End, Labels <: HList](traversal: GraphTraversal[_, End]) {
  def toSeq(): Seq[End] = traversal.toList.toSeq
  def toList(): List[End] = traversal.toList.toList
  def toSet(): Set[End] = traversal.toList.toSet
  def head(): End = toList.head
  def headOption(): Option[End] = Option(head)
  /** execute pipeline - applies all side effects */
  def iterate() = {
    traversal.iterate()
    GremlinScala[End, Labels](traversal)
  }

  def cap() = GremlinScala[End, Labels](traversal.cap())
  def cap(sideEffectKey: String) = GremlinScala[End, Labels](traversal.cap(sideEffectKey))

  def filter(p: End ⇒ Boolean) = GremlinScala[End, Labels](traversal.filter(new JPredicate[Traverser[End]] {
    override def test(h: Traverser[End]): Boolean = p(h.get)
  }))

  def count() = GremlinScala[JLong, Labels](traversal.count())

  def map[A](fun: End ⇒ A) = GremlinScala[A, Labels](traversal.map[A] { t: Traverser[End] ⇒ fun(t.get) })

  def mapWithTraverser[A](fun: Traverser[End] ⇒ A) =
    GremlinScala[A, Labels](traversal.map[A](fun))

  def path() = GremlinScala[Path, Labels](traversal.path())

  /** like path, but type safe and contains only the labelled steps - see `as` step and `LabelledPathSpec` */
  def labelledPath() = GremlinScala[Labels, Labels](traversal.addStep(new LabelledPathStep[End, Labels](traversal)))

  def select() = GremlinScala[JMap[String, End], Labels](traversal.select())

  def select(asLabels: Seq[String]) =
    GremlinScala[JMap[String, End], Labels](traversal.select(asLabels: JList[String]))

  def order() = GremlinScala[End, Labels](traversal.order())
  def order(lessThan: (End, End) ⇒ Boolean) =
    GremlinScala[End, Labels](traversal.order(new Comparator[Traverser[End]]() {
      override def compare(a: Traverser[End], b: Traverser[End]) =
        if (lessThan(a.get, b.get)) -1
        else 0
    }))

  def shuffle() = GremlinScala[End, Labels](traversal.shuffle())

  def simplePath() = GremlinScala[End, Labels](traversal.simplePath())
  def cyclicPath() = GremlinScala[End, Labels](traversal.cyclicPath())

  def dedup() = GremlinScala[End, Labels](traversal.dedup())

  def dedup[A](uniqueFun: End ⇒ A) = GremlinScala[End, Labels](traversal.dedup(liftTraverser(uniqueFun)))

  def aggregate() = GremlinScala[End, Labels](traversal.aggregate())
  def aggregate(sideEffectKey: String) = GremlinScala[End, Labels](traversal.aggregate(sideEffectKey))

  def aggregate[A](preAggregateFunction: End ⇒ A) =
    GremlinScala[End, Labels](traversal.aggregate(liftTraverser(preAggregateFunction)))

  def aggregate[A](sideEffectKey: String, preAggregateFunction: End ⇒ A) =
    GremlinScala[End, Labels](traversal.aggregate(sideEffectKey, liftTraverser(preAggregateFunction)))

  def except(someObject: End) = GremlinScala[End, Labels](traversal.except(someObject))
  def except(list: Iterable[End]) = GremlinScala[End, Labels](traversal.except(list))
  /** not named `except` because type End could be String */
  def exceptVar(variable: String) = GremlinScala[End, Labels](traversal.except(variable))

  /** keeps element on a probabilistic base - probability range: 0.0 (keep none) - 1.0 - keep all */
  def random(probability: Double) = GremlinScala[End, Labels](traversal.random(probability))

  def range(low: Int, high: Int) = GremlinScala[End, Labels](traversal.range(low, high))

  def retain(variable: String) = GremlinScala[End, Labels](traversal.retain(variable))
  def retainOne(retainObject: End) = GremlinScala[End, Labels](traversal.retain(retainObject))
  def retainAll(retainCollection: Seq[End]) = GremlinScala[End, Labels](traversal.retain(retainCollection))

  /** labels the current step and preserves the type - see `labelledPath` steps */
  def as(name: String)(implicit p: Prepend[Labels, End :: HNil]) = GremlinScala[End, p.Out](traversal.as(name))

  def back[A](to: String) = GremlinScala[A, Labels](traversal.back[A](to))

  def `with`[A](key: String, value: A) =
    GremlinScala[End, Labels](
      traversal.`with`(
        key,
        new Supplier[A] { override def get = value }
      )
    )

  def label() = GremlinScala[String, Labels](traversal.label())

  def sideEffect(traverse: Traverser[End] ⇒ Any) =
    GremlinScala[End, Labels](traversal.sideEffect(
      new JConsumer[Traverser[End]] {
        override def accept(t: Traverser[End]) = traverse(t)
      })
    )

  // note that groupCount is a side effect step, other than the 'count' step..
  // https://groups.google.com/forum/#!topic/gremlin-users/5wXSizpqRxw
  def groupCount() = GremlinScala[End, Labels](traversal.groupCount())

  def groupCount(sideEffectKey: String) = GremlinScala[End, Labels](traversal.groupCount(sideEffectKey))

  def groupCount[A](preGroupFunction: End ⇒ A) =
    GremlinScala[End, Labels](traversal.groupCount(liftTraverser(preGroupFunction)))

  def groupCount[A](sideEffectKey: String, preGroupFunction: End ⇒ A) =
    GremlinScala[End, Labels](traversal.groupCount(sideEffectKey, liftTraverser(preGroupFunction)))

  def groupBy[A](keyFunction: End ⇒ A) =
    GremlinScala[End, Labels](traversal.groupBy(liftTraverser(keyFunction)))

  def groupBy[A, B](keyFunction: End ⇒ A, valueFunction: End ⇒ B) =
    GremlinScala[End, Labels](traversal.groupBy(
      liftTraverser(keyFunction),
      liftTraverser(valueFunction)))

  def groupBy[A](sideEffectKey: String, keyFunction: End ⇒ A) =
    GremlinScala[End, Labels](traversal.groupBy(
      sideEffectKey,
      liftTraverser(keyFunction)))

  //TODO change reduceFunction to type Traversable[B] => C
  def groupBy[A, B, C](
    keyFunction: End ⇒ A,
    valueFunction: End ⇒ B,
    reduceFunction: JCollection[_] ⇒ _) =
    GremlinScala[End, Labels](traversal.groupBy(
      liftTraverser(keyFunction),
      liftTraverser(valueFunction),
      reduceFunction))

  def groupBy[A, B](sideEffectKey: String, keyFunction: End ⇒ A, valueFunction: End ⇒ B) =
    GremlinScala[End, Labels](traversal.groupBy(
      sideEffectKey,
      liftTraverser(keyFunction),
      liftTraverser(valueFunction)))

  def groupBy[A, B, C](
    sideEffectKey: String,
    keyFunction: End ⇒ A,
    valueFunction: End ⇒ B,
    reduceFunction: JCollection[_] ⇒ _) =
    GremlinScala[End, Labels](traversal.groupBy(
      sideEffectKey,
      liftTraverser(keyFunction),
      liftTraverser(valueFunction),
      reduceFunction))

  ///////////////////// BRANCH STEPS /////////////////////
  def jump(as: String) = GremlinScala[End, Labels](traversal.jump(as))

  def jump(as: String, loops: Int) = GremlinScala[End, Labels](traversal.jump(as, loops))

  def jump(as: String, ifPredicate: End ⇒ Boolean) =
    GremlinScala[End, Labels](traversal.jump(as, liftTraverser(ifPredicate)))

  def jumpWithTraverser(as: String, ifPredicate: Traverser[End] ⇒ Boolean) =
    GremlinScala[End, Labels](traversal.jump(as, ifPredicate))

  def jump(as: String, loops: Int, emitPredicate: End ⇒ Boolean) =
    GremlinScala[End, Labels](traversal.jump(
      as, loops, liftTraverser(emitPredicate)))

  def jumpWithTraverser(as: String, loops: Int, emitPredicate: Traverser[End] ⇒ Boolean) =
    GremlinScala[End, Labels](traversal.jump(as, loops, emitPredicate))

  def jump(as: String,
           ifPredicate: End ⇒ Boolean,
           emitPredicate: End ⇒ Boolean) =
    GremlinScala[End, Labels](traversal.jump(
      as,
      liftTraverser(ifPredicate),
      liftTraverser(emitPredicate)))

  def jumpWithTraverser(as: String,
                        ifPredicate: Traverser[End] ⇒ Boolean,
                        emitPredicate: Traverser[End] ⇒ Boolean) =
    GremlinScala[End, Labels](traversal.jump(as, ifPredicate, emitPredicate))
}

case class ScalaGraph(graph: Graph) extends AnyVal {
  def addVertex(): ScalaVertex = ScalaVertex(graph.addVertex())
  def addVertex(id: AnyRef): ScalaVertex = addVertex(id, Map.empty)
  def addVertex(id: AnyRef, properties: Map[String, Any]): ScalaVertex = {
    val v = ScalaVertex(graph.addVertex(T.id, id))
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
  def V = GremlinScala[Vertex, HNil](graph.V.asInstanceOf[GraphTraversal[_, Vertex]])
  /** get all edges */
  def E = GremlinScala[Edge, HNil](graph.E.asInstanceOf[GraphTraversal[_, Edge]])
}

object GS {
  // GS(graph) as a shorthand for GremlinScala(graph)
  def apply(graph: Graph) = GremlinScala(graph)
}

object GremlinScala {
  def apply(graph: Graph) = ScalaGraph(graph)

  class GremlinElementSteps[End <: Element, Labels <: HList](gremlinScala: GremlinScala[End, Labels])
      extends GremlinScala[End, Labels](gremlinScala.traversal) {

    def properties(keys: String*) =
      GremlinScala[Property[Any], Labels](traversal.properties(keys: _*)
        .asInstanceOf[GraphTraversal[_, Property[Any]]])

    def propertyMap(keys: String*) =
      GremlinScala[JMap[String, Any], Labels](traversal.propertyMap(keys: _*))

    def value[A](key: String) =
      GremlinScala[A, Labels](traversal.values[A](key))

    def values[A](key: String*) =
      GremlinScala[A, Labels](traversal.values[A](key: _*))

    def valueMap(keys: String*) =
      GremlinScala[JMap[String, AnyRef], Labels](traversal.valueMap(keys: _*))

    def has(key: String) = GremlinScala[End, Labels](traversal.has(key))

    def has(key: String, value: Any) = GremlinScala[End, Labels](traversal.has(key, value))

    def has(accessor: T, value: Any) = GremlinScala[End, Labels](traversal.has(accessor, value))

    /* there can e.g. be one of: 
     * `(i: Int, s: String) ⇒ true` - there is an implicit conversion to BiPredicate in package.scala
     * com.tinkerpop.gremlin.structure.Compare.{eq, gt, gte, lt, lte, ...}
     * com.tinkerpop.gremlin.structure.Contains.{in, nin, ...}
     */
    def has(key: String, predicate: BiPredicate[_, _], value: Any) = GremlinScala[End, Labels](traversal.has(key, predicate, value))

    def has(key: String, predicate: BiPredicate[_, _], value: Seq[_]) = GremlinScala[End, Labels](traversal.has(key, predicate, asJavaCollection(value)))

    def has(accessor: T, predicate: BiPredicate[_, _], value: Any) = GremlinScala[End, Labels](traversal.has(accessor, predicate, value))

    def has(accessor: T, predicate: BiPredicate[_, _], value: Seq[_]) = GremlinScala[End, Labels](traversal.has(accessor, predicate, asJavaCollection(value)))

    def has(label: String, key: String, value: Any) =
      GremlinScala[End, Labels](traversal.has(label, key, value))

    def has(label: String, key: String, value: Seq[_]) = GremlinScala[End, Labels](traversal.has(label, key, asJavaCollection(value)))

    def has(label: String, key: String, predicate: BiPredicate[_, _], value: Any) = GremlinScala[End, Labels](traversal.has(label, key, predicate, value))

    def has(label: String, key: String, predicate: BiPredicate[_, _], value: Seq[_]) = GremlinScala[End, Labels](traversal.has(label, key, predicate, asJavaCollection(value)))

    def hasNot(key: String) = GremlinScala[End, Labels](traversal.hasNot(key))

    /* startValue: greaterThanEqual
   * endValue: less than */
    def interval[A, B](key: String, startValue: Comparable[A], endValue: Comparable[B]) =
      GremlinScala[End, Labels](traversal.interval(key, startValue, endValue))
  }

  class GremlinVertexSteps[End <: Vertex, Labels <: HList](gremlinScala: GremlinScala[End, Labels])
      extends GremlinScala[End, Labels](gremlinScala.traversal) {

    def out() = GremlinScala[Vertex, Labels](traversal.out())
    def out(labels: String*) = GremlinScala[Vertex, Labels](traversal.out(labels: _*))
    def out(branchFactor: Int, labels: String*) = GremlinScala[Vertex, Labels](traversal.out(branchFactor, labels: _*))

    def outE() = GremlinScala[Edge, Labels](traversal.outE())
    def outE(labels: String*) = GremlinScala[Edge, Labels](traversal.outE(labels: _*))
    def outE(branchFactor: Int, labels: String*) = GremlinScala[Edge, Labels](traversal.outE(branchFactor, labels: _*))

    def in() = GremlinScala[Vertex, Labels](traversal.in())
    def in(labels: String*) = GremlinScala[Vertex, Labels](traversal.in(labels: _*))
    def in(branchFactor: Int, labels: String*) = GremlinScala[Vertex, Labels](traversal.in(branchFactor, labels: _*))

    def inE() = GremlinScala[Edge, Labels](traversal.inE())
    def inE(labels: String*) = GremlinScala[Edge, Labels](traversal.inE(labels: _*))
    def inE(branchFactor: Int, labels: String*) = GremlinScala[Edge, Labels](traversal.inE(branchFactor, labels: _*))

    def both() = GremlinScala[Vertex, Labels](traversal.both())
    def both(labels: String*) = GremlinScala[Vertex, Labels](traversal.both(labels: _*))
    def both(branchFactor: Int, labels: String*) = GremlinScala[Vertex, Labels](traversal.both(branchFactor, labels: _*))

    def bothE() = GremlinScala[Edge, Labels](traversal.bothE())
    def bothE(labels: String*) = GremlinScala[Edge, Labels](traversal.bothE(labels: _*))
    def bothE(branchFactor: Int, labels: String*) = GremlinScala[Edge, Labels](traversal.bothE(branchFactor, labels: _*))
  }

  class GremlinEdgeSteps[End <: Edge, Labels <: HList](gremlinScala: GremlinScala[End, Labels])
      extends GremlinScala[End, Labels](gremlinScala.traversal) {

    def inV = GremlinScala[Vertex, Labels](traversal.inV)

    def outV = GremlinScala[Vertex, Labels](traversal.outV)

    def bothV() = GremlinScala[Vertex, Labels](traversal.bothV())

    def otherV() = GremlinScala[Vertex, Labels](traversal.otherV())
  }
}
