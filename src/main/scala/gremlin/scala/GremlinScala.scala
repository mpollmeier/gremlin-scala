package gremlin.scala

import java.lang.{ Long ⇒ JLong, Double => JDouble }
import java.util.function.{ Predicate ⇒ JPredicate, Consumer ⇒ JConsumer, BiPredicate, Supplier }
import java.util.{ Comparator, List ⇒ JList, Map ⇒ JMap, Collection ⇒ JCollection, Iterator ⇒ JIterator }

import gremlin.scala.GremlinScala

import collection.JavaConversions._
import collection.mutable
import org.apache.tinkerpop.gremlin.process.traversal.Order
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__
import org.apache.tinkerpop.gremlin.process.traversal.{ P, Path, Scope, Traversal }
import org.apache.tinkerpop.gremlin.structure.{ T, Direction }
import shapeless.{ HList, HNil, :: }
import shapeless.ops.hlist.Prepend
import scala.language.existentials

case class GremlinScala[End, Labels <: HList](traversal: GraphTraversal[_, End]) {
  def toStream(): Stream[End] = traversal.toStream
  def toList(): List[End] = traversal.toList.toList
  def toSet(): Set[End] = traversal.toList.toSet
  def head(): End = toList.head
  def headOption(): Option[End] = toList.headOption

  // execute pipeline - applies all side effects
  def iterate() = {
    traversal.iterate()
    GremlinScala[End, Labels](traversal)
  }

  def cap(sideEffectKey: String, sideEffectKeys: String*) =
    GremlinScala[End, Labels](traversal.cap(sideEffectKey, sideEffectKeys: _*))

  //TODO: rename to option?
  // def optionTraversal[A](optionTraversal: GremlinScala[End, HNil] => GremlinScala[A, _]) =
  //   GremlinScala[End, Labels](traversal.option(optionTraversal(start).traversal))
  //   GremlinScala[End, Labels](traversal.option[A](optionTraversal(start).traversal))
    // GremlinScala[End, Labels](traversal.by(byTraversal(start).traversal))
  // def byTraversal[A](byTraversal: GremlinScala[End, HNil] ⇒ GremlinScala[A, _]) =
  //   GremlinScala[End, Labels](traversal.by(byTraversal(start).traversal))

  def filter(p: End ⇒ Boolean) = GremlinScala[End, Labels](traversal.filter(new JPredicate[Traverser[End]] {
    override def test(h: Traverser[End]): Boolean = p(h.get)
  }))

  def filterWithTraverser(p: Traverser[End] ⇒ Boolean) = GremlinScala[End, Labels](traversal.filter(new JPredicate[Traverser[End]] {
    override def test(h: Traverser[End]): Boolean = p(h)
  }))

  def count() = GremlinScala[JLong, Labels](traversal.count())
  def count(scope: Scope) = GremlinScala[JLong, Labels](traversal.count(scope))

  def map[A](fun: End ⇒ A) = GremlinScala[A, Labels](traversal.map[A] { t: Traverser[End] ⇒ fun(t.get) })

  def mapWithTraverser[A](fun: Traverser[End] ⇒ A) =
    GremlinScala[A, Labels](traversal.map[A](fun))

  def flatMap[A](fun: End ⇒ Iterable[A]) =
    GremlinScala[A, Labels](
      traversal.flatMap[A] { t: Traverser[End] ⇒
        fun(t.get).toIterator: JIterator[A]
      }
    )

  def flatMapWithTraverser[A](fun: Traverser[End] ⇒ Iterable[A]) =
    GremlinScala[A, Labels](
      traversal.flatMap[A] { e: Traverser[End] ⇒
        fun(e).toIterator: JIterator[A]
      }
    )

  def path() = GremlinScala[Path, Labels](traversal.path())

  // like path, but type safe and contains only the labelled steps - see `as` step and `LabelledPathSpec` 
  def labelledPath() = GremlinScala[Labels, Labels](traversal.asAdmin.addStep(new LabelledPathStep[End, Labels](traversal)))

  def select() = GremlinScala[JMap[String, End], Labels](traversal.select())

  def select[A, B](label: String) = GremlinScala[B, Labels](traversal.select(label))

  def select(stepLabels: Seq[String]) =
    GremlinScala[JMap[String, End], Labels](traversal.select(stepLabels: _*))

  def order() = GremlinScala[End, Labels](traversal.order())
  def order(scope: Scope) = GremlinScala[End, Labels](traversal.order(scope))

  def simplePath() = GremlinScala[End, Labels](traversal.simplePath())
  def cyclicPath() = GremlinScala[End, Labels](traversal.cyclicPath())

  def sample(amount: Int) = GremlinScala[End, Labels](traversal.sample(amount))
  def sample(scope: Scope, amount: Int) = GremlinScala[End, Labels](traversal.sample(scope, amount))

  def drop() = GremlinScala[End, Labels](traversal.drop())

  def dedup() = GremlinScala[End, Labels](traversal.dedup())

  // keeps element on a probabilistic base - probability range: 0.0 (keep none) - 1.0 - keep all 
  def coin(probability: Double) = GremlinScala[End, Labels](traversal.coin(probability))

  def range(low: Int, high: Int) = GremlinScala[End, Labels](traversal.range(low, high))
  def range(scope: Scope, low: Int, high: Int) =
    GremlinScala[End, Labels](traversal.range(scope, low, high))

  def limit(limit: Long) = GremlinScala[End, Labels](traversal.limit(limit))
  def limit(scope: Scope, limit: Long) = GremlinScala[End, Labels](traversal.limit(scope, limit))

  def tail() = GremlinScala[End, Labels](traversal.tail())
  def tail(limit: Long) = GremlinScala[End, Labels](traversal.tail(limit))
  def tail(scope: Scope, limit: Long) = GremlinScala[End, Labels](traversal.tail(scope, limit))

  // labels the current step and preserves the type - see `labelledPath` steps 
  def as(name: String)(implicit p: Prepend[Labels, End :: HNil]) = GremlinScala[End, p.Out](traversal.as(name))

  def label() = GremlinScala[String, Labels](traversal.label())

  def id() = GremlinScala[AnyRef, Labels](traversal.id())

  def identity() = GremlinScala[End, Labels](traversal.identity())

  def to(direction: Direction, edgeLabels: String*) =
    GremlinScala[Vertex, Labels](traversal.to(direction, edgeLabels: _*))

  def sideEffect(fun: End ⇒ Any) =
    GremlinScala[End, Labels](traversal.sideEffect(
      new JConsumer[Traverser[End]] {
        override def accept(t: Traverser[End]) = fun(t.get)
      }
    ))

  def sideEffectWithTraverser(fun: Traverser[End] ⇒ Any) =
    GremlinScala[End, Labels](traversal.sideEffect(
      new JConsumer[Traverser[End]] {
        override def accept(t: Traverser[End]) = fun(t)
      }
    ))

  def subgraph(sideEffectKey: String) = GremlinScala[Edge, Labels](traversal.subgraph(sideEffectKey))

  def aggregate(sideEffectKey: String) = GremlinScala[End, Labels](traversal.aggregate(sideEffectKey))

  def group[A, B]() = GremlinScala[JMap[A, B], Labels](traversal.group())

  def group(sideEffectKey: String) = GremlinScala[End, Labels](traversal.group(sideEffectKey))

  def groupCount[A]() = GremlinScala[JMap[A, JLong], Labels](traversal.groupCount())

  // note that groupCount is a side effect step, other than the 'count' step..
  // https://groups.google.com/forum/#!topic/gremlin-users/5wXSizpqRxw
  def groupCount(sideEffectKey: String) = GremlinScala[End, Labels](traversal.groupCount(sideEffectKey))

  def profile() = GremlinScala[End, Labels](traversal.profile)

  def sack[A]() = GremlinScala[A, Labels](traversal.sack[A])

  // by steps can be used in combination with all sorts of other steps, e.g. group, order, dedup, ...
  def by() = GremlinScala[End, Labels](traversal.by())

  def by[A <: AnyRef](funProjection: End ⇒ A) = GremlinScala[End, Labels](traversal.by(funProjection))

  def by(tokenProjection: T) = GremlinScala[End, Labels](traversal.by(tokenProjection))

  def by(elementPropertyKey: String) = GremlinScala[End, Labels](traversal.by(elementPropertyKey))

  def by(elementPropertyKey: String, order: Order) = GremlinScala[End, Labels](traversal.by(elementPropertyKey, order))

  def by(lessThan: (End, End) ⇒ Boolean) =
    GremlinScala[End, Labels](traversal.by(new Comparator[End]() {
      override def compare(a: End, b: End) =
        if (lessThan(a, b)) -1
        else 0
    }))

  //TODO: rename to by (without P)
  // type A is when the element property resolves to
  // e.g. if the property "name" resolves to a String you gotta supply [String] there...
  def byP[A](elementPropertyKey: String, lessThan: (A, A) ⇒ Boolean) =
    GremlinScala[End, Labels](traversal.by(elementPropertyKey, new Comparator[A]() {
      override def compare(a: A, b: A) =
        if (lessThan(a, b)) -1
        else 0
    }))

  def by(byTraversal: Traversal[_, _]) = GremlinScala[End, Labels](traversal.by(byTraversal))

  def by(order: Order) = GremlinScala[End, Labels](traversal.by(order))

  //TODO: rename to by
  def byTraversal[A](byTraversal: GremlinScala[End, HNil] ⇒ GremlinScala[A, _]) =
    GremlinScala[End, Labels](traversal.by(byTraversal(start).traversal))

  //TODO: rename to by 
  def byTraversal[A](byTraversal: GremlinScala[End, HNil] ⇒ GremlinScala[A, _], order: Order) =
    GremlinScala[End, Labels](traversal.by(byTraversal(start).traversal, order))

  def `match`[A](startLabel: String, traversals: Seq[GremlinScala[_, _]]) =
    GremlinScala[JMap[String, A], Labels](
      traversal.`match`(
        startLabel,
        traversals map (_.traversal): _*
      )
    )

  def unfold[A]() = GremlinScala[A, Labels](traversal.unfold())

  def fold() = GremlinScala[JList[End], Labels](traversal.fold())

  def sum() = GremlinScala[JDouble, Labels](traversal.sum())
  def sum(scope: Scope) = GremlinScala[JDouble, Labels](traversal.sum(scope))

  def max[A <: Number]() = GremlinScala[A, Labels](traversal.max())
  def max[A <: Number](scope: Scope) = GremlinScala[A, Labels](traversal.max(scope))

  def min[A <: Number]() = GremlinScala[A, Labels](traversal.min())
  def min[A <: Number](scope: Scope) = GremlinScala[A, Labels](traversal.min(scope))

  def mean() = GremlinScala[JDouble, Labels](traversal.mean())
  def mean(scope: Scope) = GremlinScala[JDouble, Labels](traversal.mean(scope))

  def inject(injections: End*) = GremlinScala[End, Labels](traversal.inject(injections: _*))

  def emit() = GremlinScala[End, Labels](traversal.emit())

  def emit(predicate: Traverser[End] ⇒ Boolean) = GremlinScala[End, Labels](traversal.emit(predicate))

  def branch(fun: End ⇒ Iterable[String]) =
    GremlinScala[End, Labels](traversal.branch { t: Traverser[End] ⇒
      fun(t.get): JCollection[String]
    })

  def branchWithTraverser(fun: Traverser[End] ⇒ Iterable[String]) =
    GremlinScala[End, Labels](traversal.branch { t: Traverser[End] ⇒
      fun(t): JCollection[String]
    })

  def union(traversals: GremlinScala[End, _]*) =
    GremlinScala[End, Labels](traversal.union(traversals map (_.traversal): _*))

  // repeats the provided anonymous traversal which starts at the current End
  // best combined with `times` or `until` step
  // e.g. gs.V(1).repeat(_.out).times(2)
  def repeat(repeatTraversal: GremlinScala[End, HNil] ⇒ GremlinScala[End, _]) =
    GremlinScala[End, Labels](traversal.repeat(repeatTraversal(start).traversal))

  def until(predicate: Traverser[End] ⇒ Boolean) =
    GremlinScala[End, Labels](traversal.until(predicate))

  def times(maxLoops: Int) = GremlinScala[End, Labels](traversal.times(maxLoops))

  def tree(sideEffectKey: String) = GremlinScala[End, Labels](traversal.tree(sideEffectKey))

  def is(value: AnyRef) = GremlinScala[End, Labels](traversal.is(value))

  def is(predicate: P[End]) = GremlinScala[End, Labels](traversal.is(predicate))

  def where(predicate: P[End]) = GremlinScala[End, Labels](traversal.where(predicate))

  def where(scope: Scope, predicate: P[End]) = GremlinScala[End, Labels](traversal.where(scope, predicate))

  def where(scope: Scope, startKey: String, predicate: P[End]) =
    GremlinScala[End, Labels](traversal.where(scope, startKey, predicate))

  def where(whereTraversal: GremlinScala[End, HNil] ⇒ GremlinScala[_, _]) =
    GremlinScala[End, Labels](traversal.where(whereTraversal(start).traversal))

  def where(scope: Scope, whereTraversal: GremlinScala[End, HNil] ⇒ GremlinScala[_, _]) =
    GremlinScala[End, Labels](traversal.where(scope, whereTraversal(start).traversal))

  // would rather use asJavaCollection, but unfortunately there are some casts to java.util.List in the tinkerpop codebase...
  protected def toJavaList[A](i: Iterable[A]): JList[A] = i.toList

  protected def start[A] = GremlinScala[A, HNil](__.start[A]())
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

    def key() = GremlinScala[String, Labels](traversal.key)

    def value[A](key: String) =
      GremlinScala[A, Labels](traversal.values[A](key))

    def values[A](key: String*) =
      GremlinScala[A, Labels](traversal.values[A](key: _*))

    def valueMap(keys: String*) =
      GremlinScala[JMap[String, AnyRef], Labels](traversal.valueMap(keys: _*))

    def has(key: String) = GremlinScala[End, Labels](traversal.has(key))

    def has(key: String, value: Any) = GremlinScala[End, Labels](traversal.has(key, value))

    def has(accessor: T, value: Any) = GremlinScala[End, Labels](traversal.has(accessor, value))

    // A: type of the property value
    def has[A, B](key: String, propertyTraversal: GremlinScala[A, HNil] ⇒ GremlinScala[B, _]) =
      GremlinScala[End, Labels](traversal.has(key, propertyTraversal(start).traversal))

    /* there can e.g. be one of:
     * `(i: Int, s: String) ⇒ true` - there is an implicit conversion to BiPredicate in package.scala
     * org.apache.tinkerpop.gremlin.structure.Compare.{eq, gt, gte, lt, lte, ...}
     * org.apache.tinkerpop.gremlin.structure.Contains.{in, nin, ...}
     */
    // def has(key: String, predicate: BiPredicate[_, _], value: Any) =
    //   GremlinScala[End, Labels](traversal.has(key, predicate, value))

    // def has(key: String, predicate: BiPredicate[_, _], value: Seq[_]) =
    //   GremlinScala[End, Labels](traversal.has(key, predicate, toJavaList(value)))

    // def has(accessor: T, predicate: BiPredicate[_, _], value: Any) =
    //   GremlinScala[End, Labels](traversal.has(accessor, predicate, value))

    // def has(accessor: T, predicate: BiPredicate[_, _], value: Seq[_]) =
    //   GremlinScala[End, Labels](traversal.has(accessor, predicate, toJavaList(value)))

    def has(label: String, key: String, value: Any) =
      GremlinScala[End, Labels](traversal.has(label, key, value))

    // def has(label: String, key: String, value: Seq[_]) =
    //   GremlinScala[End, Labels](traversal.has(label, key, toJavaList(value)))

    // def has(label: String, key: String, predicate: BiPredicate[_, _], value: Any) =
    //   GremlinScala[End, Labels](traversal.has(label, key, predicate, value))

    // def has(label: String, key: String, predicate: BiPredicate[_, _], value: Seq[_]) =
    //   GremlinScala[End, Labels](traversal.has(label, key, predicate, toJavaList(value)))

    def hasId(ids: AnyRef*) = GremlinScala[End, Labels](traversal.hasId(ids: _*))

    def hasLabel(labels: String*) = GremlinScala[End, Labels](traversal.hasLabel(labels: _*))

    def hasKey(keys: String*) = GremlinScala[End, Labels](traversal.hasKey(keys: _*))

    def hasValue(values: String*) = GremlinScala[End, Labels](traversal.hasValue(values: _*))

    def hasNot(key: String) = GremlinScala[End, Labels](traversal.hasNot(key))

    def hasNot(key: String, value: Any) = GremlinScala[End, Labels](traversal.where(P.not(__.has[End](key, value))))

    def and(traversals: (GremlinScala[End, _] ⇒ GremlinScala[End, _])*) =
      GremlinScala[End, Labels](traversal.and(traversals.map { _(start).traversal }: _*))

    def or(traversals: (GremlinScala[End, _] ⇒ GremlinScala[End, _])*) =
      GremlinScala[End, Labels](traversal.or(traversals.map { _(start).traversal }: _*))

    def local[A](localTraversal: GremlinScala[End, HNil] ⇒ GremlinScala[A, _]) =
      GremlinScala[A, Labels](traversal.local(localTraversal(start).traversal))

    def timeLimit(millis: Long) = GremlinScala[End, Labels](traversal.timeLimit(millis))

    def store(sideEffectKey: String) = GremlinScala[End, Labels](traversal.store(sideEffectKey))
  }

  class GremlinVertexSteps[End <: Vertex, Labels <: HList](gremlinScala: GremlinScala[End, Labels])
      extends GremlinScala[End, Labels](gremlinScala.traversal) {

    def out() = GremlinScala[Vertex, Labels](traversal.out())
    def out(labels: String*) = GremlinScala[Vertex, Labels](traversal.out(labels: _*))

    def outE() = GremlinScala[Edge, Labels](traversal.outE())
    def outE(labels: String*) = GremlinScala[Edge, Labels](traversal.outE(labels: _*))

    def in() = GremlinScala[Vertex, Labels](traversal.in())
    def in(labels: String*) = GremlinScala[Vertex, Labels](traversal.in(labels: _*))

    def inE() = GremlinScala[Edge, Labels](traversal.inE())
    def inE(labels: String*) = GremlinScala[Edge, Labels](traversal.inE(labels: _*))

    def both() = GremlinScala[Vertex, Labels](traversal.both())
    def both(labels: String*) = GremlinScala[Vertex, Labels](traversal.both(labels: _*))

    def bothE() = GremlinScala[Edge, Labels](traversal.bothE())
    def bothE(labels: String*) = GremlinScala[Edge, Labels](traversal.bothE(labels: _*))
  }

  class GremlinEdgeSteps[End <: Edge, Labels <: HList](gremlinScala: GremlinScala[End, Labels])
      extends GremlinScala[End, Labels](gremlinScala.traversal) {

    def inV = GremlinScala[Vertex, Labels](traversal.inV)

    def outV = GremlinScala[Vertex, Labels](traversal.outV)

    def bothV() = GremlinScala[Vertex, Labels](traversal.bothV())

    def otherV() = GremlinScala[Vertex, Labels](traversal.otherV())
  }
}
