package gremlin.scala

import java.lang.{Long ⇒ JLong, Double ⇒ JDouble}
import java.util.function.{Predicate ⇒ JPredicate, Consumer ⇒ JConsumer}
import java.util.{Comparator, List ⇒ JList, Map ⇒ JMap, Collection ⇒ JCollection, Iterator ⇒ JIterator}
import java.util.stream.{Stream ⇒ JStream}

import collection.JavaConversions._
import org.apache.tinkerpop.gremlin.process.traversal.Order
import org.apache.tinkerpop.gremlin.process.traversal.Pop
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.process.traversal.step.util.BulkSet
import org.apache.tinkerpop.gremlin.process.traversal.{P, Path, Scope, Traversal}
import org.apache.tinkerpop.gremlin.structure.{T, Direction}
import shapeless.{HList, HNil, ::}
import shapeless.ops.hlist.{IsHCons, Mapper, Prepend, RightFolder, ToTraversable, Tupler}
import shapeless.ops.product.ToHList
import shapeless.syntax.std.product.productOps
import scala.language.existentials
import StepLabel.{combineLabelWithValue, GetLabelName}

case class GremlinScala[End, Labels <: HList](traversal: GraphTraversal[_, End]) {
  def toStream(): JStream[End] = traversal.toStream

  def toList(): List[End] = traversal.toList.toList

  def toSet(): Set[End] = traversal.toList.toSet

  // unsafe! this will throw a runtime exception if there is no element. better use `headOption`
  def head(): End = toList.head

  def headOption(): Option[End] = toList.headOption

  def exists(): Boolean = headOption.isDefined

  // execute pipeline - applies all side effects
  def iterate() = {
    traversal.iterate()
    GremlinScala[End, Labels](traversal)
  }

  def cap(sideEffectKey: String, sideEffectKeys: String*) =
    GremlinScala[End, Labels](traversal.cap(sideEffectKey, sideEffectKeys: _*))

  def option[A](optionTraversal: GremlinScala[End, HNil] ⇒ GremlinScala[A, _]) = {
    val t = optionTraversal(start).traversal.asInstanceOf[Traversal[End, A]]
    GremlinScala[End, Labels](traversal.option(t))
  }

  def option[A, M](pickToken: M, optionTraversal: GremlinScala[End, HNil] ⇒ GremlinScala[A, _]) = {
    val t = optionTraversal(start).traversal.asInstanceOf[Traversal[End, A]]
    GremlinScala[End, Labels](traversal.option(pickToken, t))
  }

  def filter(p: End ⇒ Boolean) = GremlinScala[End, Labels](traversal.filter(new JPredicate[Traverser[End]] {
    override def test(h: Traverser[End]): Boolean = p(h.get)
  }))

  def filterWithTraverser(p: Traverser[End] ⇒ Boolean) = GremlinScala[End, Labels](traversal.filter(new JPredicate[Traverser[End]] {
    override def test(h: Traverser[End]): Boolean = p(h)
  }))

  def collect[A](pf: PartialFunction[End, A]): GremlinScala[A, Labels] =
    filter(pf.isDefinedAt).map(pf)

  def count() = GremlinScala[JLong, Labels](traversal.count())

  def count(scope: Scope) = GremlinScala[JLong, Labels](traversal.count(scope))

  def map[A](fun: End ⇒ A) = GremlinScala[A, Labels](traversal.map[A] { t: Traverser[End] ⇒ fun(t.get) })

  def mapWithTraverser[A](fun: Traverser[End] ⇒ A) =
    GremlinScala[A, Labels](traversal.map[A](fun))

  def flatMap[A](fun: End ⇒ GremlinScala[A, _]): GremlinScala[A, Labels] =
    GremlinScala[A, Labels](
      traversal.flatMap[A] { t: Traverser[End] ⇒
        fun(t.get).toList.toIterator: JIterator[A]
      }
    )

  def flatMapWithTraverser[A](fun: Traverser[End] ⇒ GremlinScala[A, _]) =
    GremlinScala[A, Labels](
      traversal.flatMap[A] { e: Traverser[End] ⇒
        fun(e).toList.toIterator: JIterator[A]
      }
    )

  def path() = GremlinScala[Path, Labels](traversal.path())

  // select all labelled steps - see `as` step and `SelectSpec`
  def select[LabelsTuple]()(implicit tupler: Tupler.Aux[Labels, LabelsTuple]) =
    GremlinScala[LabelsTuple, Labels](traversal.asAdmin.addStep(new SelectAllStep[End, Labels, LabelsTuple](traversal)))

  def select[A](stepLabel: StepLabel[A]) = GremlinScala[A, Labels](traversal.select(stepLabel.name))

  /* Select values from the traversal based on some given StepLabels (must be a tuple of `StepLabel`)
   *
   *  Lot's of type level magic here to make this work...
   *   * takes a tuple (with least two elements) whose elements are all StepLabel[_]
   *   * converts it to an HList
   *   * get's the actual values from the Tinkerpop3 java select as a Map[String, Any]
   *   * uses the types from the StepLabels to get the values from the Map (using a type level fold)
   */
  def select[
    StepLabelsAsTuple <: Product,
    StepLabels <: HList,
    H0, T0 <: HList,
    LabelNames <: HList,
    TupleWithValue,
    Values <: HList, Z,
    ValueTuples](stepLabelsTuple: StepLabelsAsTuple)(
    implicit toHList: ToHList.Aux[StepLabelsAsTuple,StepLabels],
    hasOne: IsHCons.Aux[StepLabels, H0, T0], hasTwo: IsHCons[T0], // witnesses that stepLabels has > 1 elements
    stepLabelToString: Mapper.Aux[GetLabelName.type, StepLabels, LabelNames],
    trav: ToTraversable.Aux[LabelNames, List, String],
    folder: RightFolder.Aux[StepLabels, (HNil, JMap[String, Any]), combineLabelWithValue.type, (Values, Z)],
    tupler: Tupler.Aux[Values, ValueTuples]
  ): GremlinScala[ValueTuples, Labels] = {
    val stepLabels: StepLabels = stepLabelsTuple.toHList
    val labels: List[String] = stepLabels.map(GetLabelName).toList
    val label1 = labels.head
    val label2 = labels.tail.head
    val remainder = labels.tail.tail

    val selectTraversal = traversal.select[Any](label1, label2, remainder: _*)
    GremlinScala(selectTraversal).map { selectValues ⇒
      val resultTuple = stepLabels.foldRight((HNil: HNil, selectValues))(combineLabelWithValue)
      val values: Values = resultTuple._1
      tupler(values)
    }
  }

  // TODO: remove once by/cap etc. are ported over to type safe model using StepLabel
  def select[A: DefaultsToAny](selectKey: String) = GremlinScala[A, Labels](traversal.select(selectKey))

  // TODO: remove once by/cap etc. are ported over to type safe model using StepLabel
  def select[A: DefaultsToAny](pop: Pop, selectKey: String) = GremlinScala[A, Labels](traversal.select(pop, selectKey))

  // TODO: remove once by/cap etc. are ported over to type safe model using StepLabel
  def select(selectKey1: String, selectKey2: String, otherSelectKeys: String*) =
    GremlinScala[JMap[String, Any], Labels](traversal.select(selectKey1, selectKey2, otherSelectKeys: _*))

  // TODO: remove once by/cap etc. are ported over to type safe model using StepLabel
  def select(pop: Pop, selectKey1: String, selectKey2: String, otherSelectKeys: String*) =
    GremlinScala[JMap[String, Any], Labels](traversal.select(pop, selectKey1, selectKey2, otherSelectKeys: _*))

  def order() = GremlinScala[End, Labels](traversal.order())

  def order(scope: Scope) = GremlinScala[End, Labels](traversal.order(scope))

  def simplePath() = GremlinScala[End, Labels](traversal.simplePath())

  def cyclicPath() = GremlinScala[End, Labels](traversal.cyclicPath())

  def sample(amount: Int) = GremlinScala[End, Labels](traversal.sample(amount))

  def sample(scope: Scope, amount: Int) = GremlinScala[End, Labels](traversal.sample(scope, amount))

  /* removes elements/properties from the graph */
  def drop() = GremlinScala[End, Labels](traversal.drop())

  def dedup(dedupLabels: String*) = GremlinScala[End, Labels](traversal.dedup(dedupLabels: _*))

  def dedup(scope: Scope, dedupLabels: String*) = GremlinScala[End, Labels](traversal.dedup(scope, dedupLabels: _*))

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
  def as(name: String, moreNames: String*)(implicit p: Prepend[Labels, End :: HNil]) =
    GremlinScala[End, p.Out](traversal.as(name, moreNames: _*))

  def as(stepLabel: StepLabel[End])(implicit p: Prepend[Labels, End :: HNil]) =
    GremlinScala[End, p.Out](traversal.as(stepLabel.name))

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

  def group[A: DefaultsToAny]() = GremlinScala[JMap[String, A], Labels](traversal.group())

  def group[A <: AnyRef](byTraversal: End ⇒ A) =
    GremlinScala[JMap[A, BulkSet[End]], Labels](traversal.group().by(byTraversal))

  def group(sideEffectKey: String) = GremlinScala[End, Labels](traversal.group(sideEffectKey))

  def groupCount() = GremlinScala[JMap[End, JLong], Labels](traversal.groupCount())

  // note that groupCount is a side effect step, other than the 'count' step..
  // https://groups.google.com/forum/#!topic/gremlin-users/5wXSizpqRxw
  def groupCount(sideEffectKey: String) = GremlinScala[End, Labels](traversal.groupCount(sideEffectKey))

  def profile() = GremlinScala[End, Labels](traversal.profile)

  def sack[A]() = GremlinScala[A, Labels](traversal.sack[A])

  def barrier() = GremlinScala[End, Labels](traversal.barrier())

  def barrier(maxBarrierSize: Int) = GremlinScala[End, Labels](traversal.barrier(maxBarrierSize))

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

  // provide arbitrary Traversal, e.g. by using `__.outE`
  // can't help much with the types as `by` can be used to address previously labelled steps, not just the last one
  def by(byTraversal: Traversal[_, _]) = GremlinScala[End, Labels](traversal.by(byTraversal))

  def by(byTraversal: Traversal[_, _], order: Order) = GremlinScala[End, Labels](traversal.by(byTraversal, order))

  def by(order: Order) = GremlinScala[End, Labels](traversal.by(order))

  def `match`[A](traversals: Traversal[End, _]*) =
    GremlinScala[JMap[String, A], Labels](
      traversal.`match`(traversals: _*)
    )

  def unfold[A]() = GremlinScala[A, Labels](traversal.unfold())

  def fold() = GremlinScala[JList[End], Labels](traversal.fold())

  def sum() = GremlinScala[JDouble, Labels](traversal.sum())

  def sum(scope: Scope) = GremlinScala[JDouble, Labels](traversal.sum(scope))

  def mean() = GremlinScala[JDouble, Labels](traversal.mean())

  def mean(scope: Scope) = GremlinScala[JDouble, Labels](traversal.mean(scope))

  def inject(injections: End*) = GremlinScala[End, Labels](traversal.inject(injections: _*))

  def emit() = GremlinScala[End, Labels](traversal.emit())

  def emit(emitTraversal: GremlinScala[End, HNil] ⇒ GremlinScala[End, _]) =
    GremlinScala[End, Labels](traversal.emit(emitTraversal(start).traversal))

  def emitWithTraverser(predicate: Traverser[End] ⇒ Boolean) = GremlinScala[End, Labels](traversal.emit(predicate))

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

  def until(untilTraversal: GremlinScala[End, HNil] ⇒ GremlinScala[End, _]) =
    GremlinScala[End, Labels](traversal.until(untilTraversal(start).traversal))

  def untilWithTraverser(predicate: Traverser[End] ⇒ Boolean) =
    GremlinScala[End, Labels](traversal.until(predicate))

  def times(maxLoops: Int) = GremlinScala[End, Labels](traversal.times(maxLoops))

  def tree(sideEffectKey: String) = GremlinScala[End, Labels](traversal.tree(sideEffectKey))

  def is(value: AnyRef) = GremlinScala[End, Labels](traversal.is(value))

  def is(predicate: P[End]) = GremlinScala[End, Labels](traversal.is(predicate))

  def not(notTraversal: GremlinScala[End, HNil] ⇒ GremlinScala[_, _]) =
    GremlinScala[End, Labels](traversal.not(notTraversal(start).traversal))

  def where(predicate: P[String]) = GremlinScala[End, Labels](traversal.where(predicate))

  def where(startKey: String, predicate: P[String]) = GremlinScala[End, Labels](traversal.where(startKey, predicate))

  def where(whereTraversal: GremlinScala[End, HNil] ⇒ GremlinScala[_, _]) =
    GremlinScala[End, Labels](traversal.where(whereTraversal(start).traversal))

  // would rather use asJavaCollection, but unfortunately there are some casts to java.util.List in the tinkerpop codebase...
  protected def toJavaList[A](i: Iterable[A]): JList[A] = i.toList

  protected def start[A] = GremlinScala[A, HNil](__[A]())
}

class GremlinElementSteps[End <: Element, Labels <: HList](gremlinScala: GremlinScala[End, Labels])
    extends GremlinScala[End, Labels](gremlinScala.traversal) {

  def properties(keys: String*) =
    GremlinScala[Property[Any], Labels](traversal.properties(keys: _*)
      .asInstanceOf[GraphTraversal[_, Property[Any]]])

  def propertyMap(keys: String*) =
    GremlinScala[JMap[String, Any], Labels](traversal.propertyMap(keys: _*))

  def key() = GremlinScala[String, Labels](traversal.key)

  def value[A](key: Key[A]) =
    GremlinScala[A, Labels](traversal.values[A](key.value))

  def value[A](key: String) =
    GremlinScala[A, Labels](traversal.values[A](key))

  def valueOption[A](key: Key[A]): GremlinScala[Option[A], Labels] =
    this.properties(key.value).map(_.toOption.asInstanceOf[Option[A]])

  def valueOption[A](key: String): GremlinScala[Option[A], Labels] =
    this.properties(key).map(_.toOption.asInstanceOf[Option[A]])

  def values[A](key: String*) =
    GremlinScala[A, Labels](traversal.values[A](key: _*))

  def valueMap(keys: String*) =
    GremlinScala[JMap[String, AnyRef], Labels](traversal.valueMap(keys: _*))

  def has(key: Key[_]) = GremlinScala[End, Labels](traversal.has(key.value))

  def has[A](key: Key[A], value: A) = GremlinScala[End, Labels](traversal.has(key.value, value))

  def has[A](p: KeyValue[A]) = GremlinScala[End, Labels](traversal.has(p.key.value, p.value))

  def has[A](p: (Key[A], A)) = GremlinScala[End, Labels](traversal.has(p._1.value, p._2))

  def has[A](key: Key[A], predicate: P[A]) = GremlinScala[End, Labels](traversal.has(key.value, predicate))

  def has(accessor: T, value: Any) = GremlinScala[End, Labels](traversal.has(accessor, value))

  def has[A](accessor: T, predicate: P[A]) = GremlinScala[End, Labels](traversal.has(accessor, predicate))

  // A: type of the property value
  def has[A, B](key: Key[A], propertyTraversal: GremlinScala[A, HNil] ⇒ GremlinScala[B, _]) =
    GremlinScala[End, Labels](traversal.has(key.value, propertyTraversal(start).traversal))

  def has[A](label: String, key: Key[A], value: A) =
    GremlinScala[End, Labels](traversal.has(label, key.value, value))

  def has[A](label: String, key: Key[A], predicate: P[A]) =
    GremlinScala[End, Labels](traversal.has(label, key.value, predicate))

  def hasId(ids: AnyRef*) = GremlinScala[End, Labels](traversal.hasId(ids: _*))

  def hasLabel(labels: String*) = GremlinScala[End, Labels](traversal.hasLabel(labels: _*))

  def hasKey(keys: Key[_]*) = GremlinScala[End, Labels](traversal.hasKey(keys.map(_.value): _*))

  def hasValue(values: String*) = GremlinScala[End, Labels](traversal.hasValue(values: _*))

  def hasNot(key: Key[_]) = GremlinScala[End, Labels](traversal.hasNot(key.value))

  def hasNot[A](key: KeyValue[A]) = GremlinScala[End, Labels](traversal.not(__.has(key.key.value, key.value)))

  def hasNot[A](key: Key[A], value: A) = GremlinScala[End, Labels](traversal.not(__.has(key.value, value)))

  def and(traversals: (GremlinScala[End, HNil] ⇒ GremlinScala[End, _])*) =
    GremlinScala[End, Labels](traversal.and(traversals.map {
      _(start).traversal
    }: _*))

  def or(traversals: (GremlinScala[End, HNil] ⇒ GremlinScala[End, _])*) =
    GremlinScala[End, Labels](traversal.or(traversals.map {
      _(start).traversal
    }: _*))

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

  def inV() = GremlinScala[Vertex, Labels](traversal.inV)

  def outV() = GremlinScala[Vertex, Labels](traversal.outV)

  def bothV() = GremlinScala[Vertex, Labels](traversal.bothV())

  def otherV() = GremlinScala[Vertex, Labels](traversal.otherV())
}

class GremlinNumberSteps[End <: Number, Labels <: HList](gremlinScala: GremlinScala[End, Labels])
    extends GremlinScala[End, Labels](gremlinScala.traversal) {

  def max() = GremlinScala[End, Labels](traversal.max[End]())

  def max(scope: Scope) = GremlinScala[End, Labels](traversal.max(scope))

  def min() = GremlinScala[End, Labels](traversal.min[End]())

  def min(scope: Scope) = GremlinScala[End, Labels](traversal.min(scope))
}

