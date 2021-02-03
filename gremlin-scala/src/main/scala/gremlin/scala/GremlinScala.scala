package gremlin.scala

import java.lang.{Long => JLong, Double => JDouble}
import java.util.function.{
  Predicate => JPredicate,
  Consumer => JConsumer,
  BiFunction => JBiFunction,
  Function => JFunction
}
import java.util.{
  Comparator,
  List => JList,
  Map => JMap,
  Collection => JCollection,
  Iterator => JIterator,
  Set => JSet
}
import java.util.stream.{Stream => JStream}

import collection.JavaConverters._
import gremlin.scala.StepLabel.{combineLabelWithValue, GetLabelName}
import org.apache.tinkerpop.gremlin.process.traversal.Order
import org.apache.tinkerpop.gremlin.process.traversal.Pop
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.{
  DefaultGraphTraversal,
  GraphTraversal
}
import org.apache.tinkerpop.gremlin.process.traversal.step.util.{BulkSet, Tree}
import org.apache.tinkerpop.gremlin.process.traversal.traverser.util.TraverserSet
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalExplanation
import org.apache.tinkerpop.gremlin.process.traversal.{Bytecode, Path, Scope, Traversal}
import org.apache.tinkerpop.gremlin.structure.{Column, Direction, T}
import shapeless.{::, HList, HNil}
import shapeless.ops.hlist.{IsHCons, Mapper, Prepend, RightFolder, ToTraversable, Tupler}
import shapeless.ops.product.ToHList
import shapeless.syntax.std.tuple._
import scala.concurrent.duration.FiniteDuration
import scala.reflect.runtime.{universe => ru}
import scala.collection.{immutable, mutable}
import scala.concurrent.{Future, Promise}

object GremlinScala {

  /** constructor */
  def apply[End, Labels0 <: HList](
      traversal: GraphTraversal[_, End]): GremlinScala.Aux[End, Labels0] =
    new GremlinScala[End](traversal) { type Labels = Labels0 }

  /** convenience type constructor
    * `GremlinScala[Vertex] { type Labels = HNil }` is equivalent to `GremlinScala.Aux[Vertex, HNil]` */
  type Aux[End, Labels0 <: HList] = GremlinScala[End] { type Labels = Labels0 }
}

class GremlinScala[End](val traversal: GraphTraversal[_, End]) {

  /** labels applied to this traversal using `as` step */
  type Labels <: HList

  /** alias for `toList`, because typing kills */
  def l(): List[End] = toList

  def toList(): List[End] = traversal.toList.asScala.toList

  def toIterator(): Iterator[End] = traversal.asScala

  def toJIterator(): JIterator[End] = traversal

  def toStream(): JStream[End] = traversal.toStream

  def toSet(): Set[End] = toList.toSet

  def toMap[A, B](implicit ev: End <:< (A, B)): immutable.Map[A, B] =
    toList.toMap

  def toBuffer(): mutable.Buffer[End] = traversal.toList.asScala

  /** unsafe! this will throw a runtime exception if there is no element. better use `headOption` */
  def head(): End = limit(1).toList.head

  def headOption(): Option[End] = limit(1).toList.headOption

  def explain(): TraversalExplanation = traversal.explain()

  def exists(): Boolean = headOption.isDefined
  def notExists(): Boolean = !exists()

  /** execute pipeline - applies all side effects */
  def iterate(): GremlinScala.Aux[End, Labels] = {
    traversal.iterate()
    GremlinScala[End, Labels](traversal)
  }

  override def clone() = GremlinScala[End, Labels](
    traversal match {
      // clone is protected on Traversal, but DefaultGraphTraversal makes it public
      case dgt: DefaultGraphTraversal[_, End] => dgt.clone
      // unfortunately, structural types can't be checked in pattern match
      // if more (and potentially yet unknown) Traversals need to be supported,
      // we could either use type classes or reflection: http://stackoverflow.com/a/3434804/452762
    }
  )

  def cap(sideEffectKey: String, sideEffectKeys: String*) =
    GremlinScala[End, Labels](traversal.cap(sideEffectKey, sideEffectKeys: _*))

  def cap[A](stepLabel: StepLabel[A]) =
    GremlinScala[A, Labels](traversal.cap(stepLabel.name))

  /** returns the result of the specified traversal if it yields a result else it returns the calling element, i.e. the identity(). */
  def optional(optionalTraversal: GremlinScala.Aux[End, HNil] => GremlinScala[End]) = {
    val t = optionalTraversal(start).traversal
    GremlinScala[End, Labels](traversal.optional(t))
  }

  /** returns the result of the specified traversal if it yields a result else it returns the provided default value
    *
    * note: uses coalesce internally, which is a flatMap step, which affects `as` and `traverser` behaviour */
  def optional[A](optionalTraversal: GremlinScala.Aux[End, HNil] => GremlinScala[A], default: A) =
    coalesce(optionalTraversal, _.constant(default))

  def project[A](projectKey: String,
                 otherProjectKeys: String*): GremlinScala.Aux[JMap[String, A], Labels] =
    GremlinScala[JMap[String, A], Labels](traversal.project(projectKey, otherProjectKeys: _*))

  def project[H <: Product](
      builder: ProjectionBuilder[Nil.type] => ProjectionBuilder[H]): GremlinScala[H] =
    builder(ProjectionBuilder()).build(this)

  /** You might think that predicate should be `GremlinScala[End] => GremlinScala[Boolean]`,
    * but that's not how tp3 works: e.g. `.value(Age).is(30)` returns `30`, not `true`
    */
  def filter(predicate: GremlinScala[End] => GremlinScala[_]) =
    GremlinScala[End, Labels](traversal.filter(predicate(start).traversal))

  def filterNot(predicate: GremlinScala[End] => GremlinScala[_]) =
    GremlinScala[End, Labels](traversal.filter(predicate(start).traversal.count.is(P.eq(0))))

  /** used in scala for comprehensions */
  def withFilter(predicate: GremlinScala[End] => GremlinScala[_]) =
    filter(predicate)

  def filterOnEnd(predicate: End => Boolean) = GremlinScala[End, Labels](
    traversal.filter(new JPredicate[Traverser[End]] {
      override def test(h: Traverser[End]): Boolean = predicate(h.get)
    })
  )

  def filterWithTraverser(predicate: Traverser[End] => Boolean) =
    GremlinScala[End, Labels](traversal.filter(new JPredicate[Traverser[End]] {
      override def test(h: Traverser[End]): Boolean = predicate(h)
    }))

  def collect[A](pf: PartialFunction[End, A]): GremlinScala.Aux[A, Labels] =
    filterOnEnd(pf.isDefinedAt).map(pf)

  def count() = GremlinScala[JLong, HNil](traversal.count())

  def count(scope: Scope) = GremlinScala[JLong, HNil](traversal.count(scope))

  def loops() = GremlinScala[Integer, HNil](traversal.loops())

  def getSideEffect[A](sideEffectKey: String): A = traversal.asAdmin().getSideEffects.get(sideEffectKey)

  def map[A](fun: End => A) =
    GremlinScala[A, Labels](traversal.map[A] { t: Traverser[End] =>
      fun(t.get)
    })

  def mapWithTraverser[A](fun: Traverser[End] => A) =
    GremlinScala[A, Labels](traversal.map[A](fun))

  def flatMap[A](fun: End => GremlinScala[A]): GremlinScala.Aux[A, Labels] =
    GremlinScala[A, Labels](
      traversal.flatMap[A] { t: Traverser[End] =>
        fun(t.get).toList().toIterator.asJava: JIterator[A]
      }
    )

  def flatMapWithTraverser[A](fun: Traverser[End] => GremlinScala[A]) =
    GremlinScala[A, Labels](
      traversal.flatMap[A] { e: Traverser[End] =>
        fun(e).toList().toIterator.asJava: JIterator[A]
      }
    )

  /** track every step in the traversal */
  def path() = GremlinScala[Path, Labels](traversal.path())

  /** track every step in the traversal, modulate elements in round robin fashion */
  def path(bys: By[_]*) = {
    var newTrav: GraphTraversal[_, Path] = traversal.path()
    bys.foreach { by =>
      newTrav = by(newTrav)
    }
    GremlinScala[Path, Labels](newTrav)
  }

  /** select all labelled steps - see `as` step and `SelectSpec` */
  def select[LabelsTuple]()(implicit tupler: Tupler.Aux[Labels, LabelsTuple]) =
    GremlinScala[LabelsTuple, Labels](
      traversal.asAdmin.addStep(new SelectAllStep[End, Labels, LabelsTuple](traversal)))

  def select[A](stepLabel: StepLabel[A]) =
    GremlinScala[A, Labels](traversal.select(stepLabel.name))

  /** Select values from the traversal based on some given StepLabels (must be a tuple of `StepLabel`)
    *
    *  Lot's of type level magic here to make this work...
    *   * takes a tuple (with least two elements) whose elements are all StepLabel[_]
    *   * converts it to an HList
    *   * get's the actual values from the Tinkerpop3 java select as a Map[String, Any]
    *   * uses the types from the StepLabels to get the values from the Map (using a type level fold)
    */
  def select[StepLabelsAsTuple <: Product,
             StepLabels <: HList,
             H0,
             T0 <: HList,
             LabelNames <: HList,
             TupleWithValue,
             Values <: HList,
             Z,
             ValueTuples](stepLabelsTuple: StepLabelsAsTuple)(
      implicit toHList: ToHList.Aux[StepLabelsAsTuple, StepLabels],
      hasOne: IsHCons.Aux[StepLabels, H0, T0],
      hasTwo: IsHCons[T0], // witnesses that stepLabels has > 1 elements
      stepLabelToString: Mapper.Aux[GetLabelName.type, StepLabels, LabelNames],
      trav: ToTraversable.Aux[LabelNames, List, String],
      folder: RightFolder.Aux[StepLabels,
                              (HNil, JMap[String, Any]),
                              combineLabelWithValue.type,
                              (Values, Z)],
      tupler: Tupler.Aux[Values, ValueTuples]
  ): GremlinScala.Aux[ValueTuples, Labels] = {
    val stepLabels: StepLabels = toHList(stepLabelsTuple)
    val labels: List[String] = stepLabels.map(GetLabelName).toList
    val label1 = labels.head
    val label2 = labels.tail.head
    val remainder = labels.tail.tail

    val selectTraversal = traversal.select[Any](label1, label2, remainder: _*)
    GremlinScala(selectTraversal).map { selectValues =>
      val resultTuple =
        stepLabels.foldRight((HNil: HNil, selectValues))(combineLabelWithValue)
      val values: Values = resultTuple._1
      tupler(values)
    }
  }

  // TODO: remove once by/cap etc. are ported over to type safe model using StepLabel
  def select[A: DefaultsToAny](selectKey: String) =
    GremlinScala[A, Labels](traversal.select(selectKey))

  // TODO: remove once by/cap etc. are ported over to type safe model using StepLabel
  def select[A: DefaultsToAny](pop: Pop, selectKey: String) =
    GremlinScala[A, Labels](traversal.select(pop, selectKey))

  // TODO: remove once by/cap etc. are ported over to type safe model using StepLabel
  def select(selectKey1: String, selectKey2: String, otherSelectKeys: String*) =
    GremlinScala[JMap[String, Any], Labels](
      traversal.select(selectKey1, selectKey2, otherSelectKeys: _*))

  // TODO: remove once by/cap etc. are ported over to type safe model using StepLabel
  def select(pop: Pop, selectKey1: String, selectKey2: String, otherSelectKeys: String*) =
    GremlinScala[JMap[String, Any], Labels](
      traversal.select(pop, selectKey1, selectKey2, otherSelectKeys: _*))

  /* select only the keys from a map (e.g. groupBy) - see usage examples in SelectSpec.scala */
  def selectKeys[K](implicit columnType: ColumnType.Aux[End, K, _]): GremlinScala[K] = {
    new GremlinScala[K](
      traversal
        .select(Column.keys) // The result of select(keys) may not be a collection (when applied on Map.Entry)
        .asInstanceOf[GraphTraversal[_, K]])
  }

  /* select only the values from a map (e.g. groupBy) - see usage examples in SelectSpec.scala */
  def selectValues[V](implicit columnType: ColumnType.Aux[End, _, V]): GremlinScala[V] = {
    new GremlinScala[V](
      traversal
        .select(Column.values)
        .asInstanceOf[GraphTraversal[_, V]])
  }

  @deprecated("use order(by(...))", "3.0.0.1")
  def orderBy[A <: AnyRef: Ordering](by: End => A): GremlinScala.Aux[End, Labels] =
    orderBy(by, implicitly[Ordering[A]])

  @deprecated("use order(by(...))", "3.0.0.1")
  def orderBy[A <: AnyRef](by: End => A, comparator: Comparator[A]): GremlinScala.Aux[End, Labels] =
    GremlinScala[End, Labels](
      traversal
        .order()
        .by(
          new Comparator[End] {
            override def compare(a: End, b: End) =
              comparator.compare(by(a), by(b))
          }
        )
    )

  @deprecated("use order(by(...))", "3.0.0.1")
  def orderBy(elementPropertyKey: String)(
      implicit ev: End <:< Element): GremlinScala.Aux[End, Labels] =
    GremlinScala[End, Labels](traversal.order().by(elementPropertyKey, Order.asc))

  @deprecated("use order(by(...))", "3.0.0.1")
  def orderBy(elementPropertyKey: String, comparator: Order)(
      implicit ev: End <:< Element): GremlinScala.Aux[End, Labels] =
    GremlinScala[End, Labels](traversal.order().by(elementPropertyKey, comparator))

  def order() = GremlinScala[End, Labels](traversal.order())

  @deprecated("use order(by(Order))", "3.0.0.1")
  def order(comparator: Order) =
    GremlinScala[End, Labels](traversal.order().by(comparator))

  def order(scope: Scope) =
    GremlinScala[End, Labels](traversal.order(scope).by(Order.asc))

  @deprecated("use order(by(...))", "3.0.0.1")
  def order(scope: Scope, comparator: Order = Order.asc) =
    GremlinScala[End, Labels](traversal.order(scope).by(comparator))

  /** n.b. `By` can be used in place of `OrderBy` */
  def order(orderBys: OrderBy[_]*) = {
    var newTrav: GraphTraversal[_, End] = traversal.order()
    orderBys.foreach { orderBy =>
      newTrav = orderBy(newTrav)
    }
    GremlinScala[End, Labels](newTrav)
  }

  /** n.b. `By` can be used in place of `OrderBy` */
  def order(scope: Scope, orderBys: OrderBy[_]*) = {
    var newTrav: GraphTraversal[_, End] = traversal.order(scope)
    orderBys.foreach { orderBy =>
      newTrav = orderBy(newTrav)
    }
    GremlinScala[End, Labels](newTrav)
  }

  def simplePath() = GremlinScala[End, Labels](traversal.simplePath())

  def cyclicPath() = GremlinScala[End, Labels](traversal.cyclicPath())

  def sample(amount: Int) = GremlinScala[End, Labels](traversal.sample(amount))

  def sample(scope: Scope, amount: Int) =
    GremlinScala[End, Labels](traversal.sample(scope, amount))

  /** removes elements/properties from the graph */
  def drop() = GremlinScala[End, Labels](traversal.drop())

  def dedup() = GremlinScala[End, Labels](traversal.dedup())

  def dedup(by: By[_]) = GremlinScala[End, Labels](by(traversal.dedup()))

  def dedup(dedupLabels: String*) =
    GremlinScala[End, Labels](traversal.dedup(dedupLabels: _*))

  def dedup(scope: Scope, dedupLabels: String*) =
    GremlinScala[End, Labels](traversal.dedup(scope, dedupLabels: _*))

  /** keeps element on a probabilistic base - probability range: 0.0 (keep none) - 1.0 - keep all */
  def coin(probability: Double) =
    GremlinScala[End, Labels](traversal.coin(probability))

  def range(low: Long, high: Long) =
    GremlinScala[End, Labels](traversal.range(low, high))

  def range(scope: Scope, low: Long, high: Long) =
    GremlinScala[End, Labels](traversal.range(scope, low, high))

  def limit(max: Long) = GremlinScala[End, Labels](traversal.limit(max))

  def limit(scope: Scope, max: Long) =
    GremlinScala[End, Labels](traversal.limit(scope, max))

  def timeLimit(maxTime: FiniteDuration) =
    GremlinScala[End, Labels](traversal.timeLimit(maxTime.toMillis))

  def tail() = GremlinScala[End, Labels](traversal.tail())

  def tail(limit: Long) = GremlinScala[End, Labels](traversal.tail(limit))

  def tail(scope: Scope, limit: Long) =
    GremlinScala[End, Labels](traversal.tail(scope, limit))

  /** labels the current step and preserves the type - see `select` step */
  def as(name: String, moreNames: String*)(implicit p: Prepend[Labels, End :: HNil]) =
    GremlinScala[End, p.Out](traversal.as(name, moreNames: _*))

  /** labels the current step and preserves the type - see `select` step */
  def as(stepLabel: StepLabel[End])(implicit p: Prepend[Labels, End :: HNil]) =
    GremlinScala[End, p.Out](traversal.as(stepLabel.name))

  def label() = GremlinScala[String, Labels](traversal.label())

  def id() = GremlinScala[AnyRef, Labels](traversal.id())

  def identity() = GremlinScala[End, Labels](traversal.identity())

  def sideEffect(fun: End => Any) =
    GremlinScala[End, Labels](
      traversal.sideEffect(
        new JConsumer[Traverser[End]] {
          override def accept(t: Traverser[End]) = fun(t.get)
        }
      ))

  def sideEffectWithTraverser(fun: Traverser[End] => Any) =
    GremlinScala[End, Labels](
      traversal.sideEffect(
        new JConsumer[Traverser[End]] {
          override def accept(t: Traverser[End]) = fun(t)
        }
      ))

  /** Organize objects in the stream into a Map. */
  def group[A: DefaultsToAny]() =
    GremlinScala[JMap[String, A], Labels](traversal.group())

  /** Organize objects in the stream into a Map, group keys with a modulator */
  def group[Modulated](keysBy: By[Modulated]) =
    GremlinScala[JMap[Modulated, JCollection[End]], Labels](keysBy(traversal.group()))

  /** Organize objects in the stream into a Map, group keys and values with a modulator */
  def group[ModulatedKeys, ModulatedValues](keysBy: By[ModulatedKeys],
                                            valuesBy: By[ModulatedValues]) =
    GremlinScala[JMap[ModulatedKeys, JCollection[ModulatedValues]], Labels](
      valuesBy(keysBy(traversal.group())))

  @deprecated("use group(by(...))", "3.0.0.1")
  def group[A <: AnyRef](byTraversal: End => A) =
    GremlinScala[JMap[A, BulkSet[End]], Labels](traversal.group().by(byTraversal))

  @deprecated("use group(by(...))", "3.0.0.1")
  def groupBy[A <: AnyRef](byFun: End => A): GremlinScala.Aux[JMap[A, JCollection[End]], Labels] =
    GremlinScala[JMap[A, JCollection[End]], Labels](
      traversal.group().by(byFun: JFunction[End, AnyRef]))

  @deprecated("use group(by(...))", "3.0.0.1")
  def groupBy[A <: AnyRef, B](byFun: End => A,
                              valueFun: End => B): GremlinScala.Aux[Map[A, Iterable[B]], Labels] =
    GremlinScala[JMap[A, JCollection[End]], Labels](
      traversal.group().by(byFun: JFunction[End, AnyRef])
    ).map(_.asScala.mapValues(_.asScala.map(valueFun)).toMap)

  def groupCount() =
    GremlinScala[JMap[End, JLong], Labels](traversal.groupCount())

  /** note that groupCount is a side effect step, other than the 'count' step..
    * https://groups.google.com/forum/#!topic/gremlin-users/5wXSizpqRxw */
  def groupCount(sideEffectKey: String) =
    GremlinScala[End, Labels](traversal.groupCount(sideEffectKey))

  def groupCount[Modulated](by: By[Modulated]) =
    GremlinScala[JMap[Modulated, JLong], Labels](by(traversal.groupCount()))

  def profile(sideEffectKey: String) =
    GremlinScala[End, Labels](traversal.profile(sideEffectKey))

  /** Take value out of the sack.
    * TODO: carry SackType as class type parameter (both ScalaGraph and GremlinScala) */
  def sack[SackType]() =
    GremlinScala[SackType, Labels](traversal.sack[SackType]())

  /** Modify the sack with the current End type. [SideEffect] */
  def sack[SackType](func: (SackType, End) => SackType) =
    GremlinScala[End, Labels](traversal.sack(func))

  /** sack with by modulator */
  def sack[SackType, Modulated](func: (SackType, Modulated) => SackType, by: By[Modulated]) =
    GremlinScala[End, Labels](by(traversal.sack(func)))

  def barrier() = GremlinScala[End, Labels](traversal.barrier())

  def barrier(maxBarrierSize: Int) =
    GremlinScala[End, Labels](traversal.barrier(maxBarrierSize))

  def barrier(consumer: TraverserSet[End] => Unit): GremlinScala.Aux[End, Labels] = {
    val jConsumer: JConsumer[TraverserSet[End]] = consumer //invoke implicit conversion
    val jConsumerOnAnyRef = jConsumer.asInstanceOf[JConsumer[TraverserSet[AnyRef]]] // since type isn't properly defined in j, need to cast
    GremlinScala[End, Labels](traversal.barrier(jConsumerOnAnyRef))
  }

  def barrier(consumer: JConsumer[TraverserSet[AnyRef]]): GremlinScala.Aux[End, Labels] =
    GremlinScala[End, Labels](traversal.barrier(consumer))

  // by steps can be used in combination with all sorts of other steps, e.g. group, order, dedup, ...
  @deprecated("don't use step by itself, most steps now accept a `By` instance as an argument",
              "3.0.0.1")
  def by() = GremlinScala[End, Labels](traversal.by())

  @deprecated("don't use step by itself, most steps now accept a `By` instance as an argument",
              "3.0.0.1")
  def by(comparator: Comparator[End]): GremlinScala.Aux[End, Labels] =
    GremlinScala[End, Labels](traversal.by(comparator))

  @deprecated("don't use step by itself, most steps now accept a `By` instance as an argument",
              "3.0.0.1")
  def by[A <: AnyRef](funProjection: End => A) =
    GremlinScala[End, Labels](traversal.by(funProjection))

  @deprecated("don't use step by itself, most steps now accept a `By` instance as an argument",
              "3.0.0.1")
  def by[A <: AnyRef](funProjection: End => A,
                      comparator: Comparator[A] = Order.asc): GremlinScala.Aux[End, Labels] =
    GremlinScala[End, Labels](
      traversal.by(toJavaFunction(funProjection)
                     .asInstanceOf[java.util.function.Function[_, AnyRef]],
                   comparator)
    )

  @deprecated("don't use step by itself, most steps now accept a `By` instance as an argument",
              "3.0.0.1")
  def by(tokenProjection: T) =
    GremlinScala[End, Labels](traversal.by(tokenProjection))

  @deprecated("don't use step by itself, most steps now accept a `By` instance as an argument",
              "3.0.0.1")
  def by(elementPropertyKey: String) =
    GremlinScala[End, Labels](traversal.by(elementPropertyKey))

  @deprecated("don't use step by itself, most steps now accept a `By` instance as an argument",
              "3.0.0.1")
  def by[A](elementPropertyKey: String, comparator: Comparator[A]) =
    GremlinScala[End, Labels](traversal.by(elementPropertyKey, comparator))

  @deprecated("don't use step by itself, most steps now accept a `By` instance as an argument",
              "3.0.0.1")
  def by(lessThan: (End, End) => Boolean) =
    GremlinScala[End, Labels](traversal.by(new Comparator[End]() {
      override def compare(a: End, b: End) =
        if (lessThan(a, b)) -1
        else 0
    }))

  // provide arbitrary Traversal, e.g. by using `__.outE`
  // can't help much with the types as `by` can be used to address previously labelled steps, not just the last one
  @deprecated("don't use step by itself, most steps now accept a `By` instance as an argument",
              "3.0.0.1")
  def by(byTraversal: Traversal[_, _]) =
    GremlinScala[End, Labels](traversal.by(byTraversal))

  @deprecated("don't use step by itself, most steps now accept a `By` instance as an argument",
              "3.0.0.1")
  def by[A](byTraversal: Traversal[_, A], comparator: Comparator[A]) =
    GremlinScala[End, Labels](traversal.by(byTraversal, comparator))

  def `match`[A](traversals: (GremlinScala.Aux[End, HNil] => GremlinScala[_])*) =
    GremlinScala[JMap[String, A], Labels](
      traversal.`match`(traversals.map(_.apply(__[End]()).traversal): _*)
    )

  def unfold[A]() = GremlinScala[A, Labels](traversal.unfold())

  def fold() = GremlinScala[JList[End], HNil](traversal.fold())

  def foldLeft[Z](z: Z)(op: (Z, End) => Z) =
    GremlinScala[Z, HNil](traversal.fold(z, new JBiFunction[Z, End, Z] {
      override def apply(t: Z, u: End): Z = op(t, u)
    }))

  def inject(injections: End*) =
    GremlinScala[End, Labels](traversal.inject(injections: _*))

  def store(stepLabel: StepLabel[JSet[End]]) =
    GremlinScala[End, Labels](traversal.store(stepLabel.name))

  @deprecated("use store(StepLabel) for more type safety", "3.3.3.16")
  def store(sideEffectKey: String) =
    GremlinScala[End, Labels](traversal.store(sideEffectKey))

  def aggregate(stepLabel: StepLabel[JSet[End]]) =
    GremlinScala[End, Labels](traversal.aggregate(stepLabel.name))

  @deprecated("use aggregate(StepLabel) for more type safety", "3.3.3.16")
  def aggregate(sideEffectKey: String) =
    GremlinScala[End, Labels](traversal.aggregate(sideEffectKey))

  /** modulator for repeat step - emit everything on the way */
  def emit() = GremlinScala[End, Labels](traversal.emit())

  /** modulator for repeat step - emit if emitTraversal has at least one result */
  def emit(emitTraversal: GremlinScala.Aux[End, HNil] => GremlinScala[_]) =
    GremlinScala[End, Labels](traversal.emit(emitTraversal(start).traversal))

  /** modulator for repeat step - emit depending on predicate */
  def emitWithTraverser(predicate: Traverser[End] => Boolean) =
    GremlinScala[End, Labels](traversal.emit(predicate))

  /** merges of the results of an arbitrary number of traversals.
    * supports heterogeneous queries, e.g. for the following query:
    * `g.V(1).union(_.join(_.outE).join(_.out))` the result type is derived as
    * `GremlinScala[(JList[Edge], JList[Vertex])]`
    */
  def union[EndsHList <: HList, EndsTuple](
      unionTraversals: UnionTraversals[End, HNil] => UnionTraversals[End, EndsHList])(
      implicit tupler: Tupler.Aux[EndsHList, EndsTuple]): GremlinScala.Aux[EndsTuple, Labels] = {
    // compiler cannot infer the types by itself at this point anyway, so just using `Any` here
    val unionTraversalsUntyped =
      unionTraversals(new UnionTraversals(Nil)).travsUntyped
        .asInstanceOf[Seq[GremlinScala.Aux[End, HNil] => GremlinScala[Any]]]
    val asTravs: Seq[GraphTraversal[_, Any]] = asTraversals(unionTraversalsUntyped)
    val folded: Seq[GraphTraversal[_, JList[Any]]] = asTravs.map(_.fold)
    val unionTrav: GraphTraversal[_, JList[JList[Any]]] = traversal.union(folded: _*).fold

    GremlinScala[JList[JList[Any]], Labels](unionTrav).map { results: JList[JList[Any]] =>
      // create the hlist - we know the types we will end up with: `Ends`, but they're not preserved in the tp3 (java) traversal, therefor we need to cast
      val hlist = results.asScala.toList.foldRight(HNil: HList)(_ :: _)
      tupler(hlist.asInstanceOf[EndsHList])
    }
  }

  /** merges of the results of an arbitrary number of traversals into a flat structure (i.e. no folds).   */
  def unionFlat[A](unionTraversals: (GremlinScala.Aux[End, HNil] => GremlinScala[A])*) =
    GremlinScala[A, Labels](traversal.union(asTraversals(unionTraversals): _*))

  /** evaluates the provided traversals in order and returns the first traversal that emits at least one element
    * useful e.g. for if/elseif/else semantics */
  def coalesce[A](coalesceTraversals: (GremlinScala.Aux[End, HNil] => GremlinScala[A])*)
    : GremlinScala.Aux[A, Labels] =
    GremlinScala[A, Labels](traversal.coalesce(asTraversals(coalesceTraversals): _*))

  /** special case of choose step if there's only two options - basically an if/else condition for traversals
    *
    * you might think that predicate should be `GremlinScala[End] => GremlinScala[Boolean]`,
    * but that's not how tp3 works: e.g. `.value(Age).is(30)` returns `30`, not `true`
    */
  def choose[NewEnd](predicate: GremlinScala[End] => GremlinScala[_],
                     onTrue: GremlinScala.Aux[End, HNil] => GremlinScala[NewEnd],
                     onFalse: GremlinScala.Aux[End, HNil] => GremlinScala[NewEnd])
    : GremlinScala.Aux[NewEnd, Labels] = {
    val p = predicate(start).traversal
    val t = onTrue(start).traversal
    val f = onFalse(start).traversal
    GremlinScala[NewEnd, Labels](traversal.choose(p, t, f))
  }

  /** traverser will pick first option that has a matching pickToken */
  def choose[BranchOn, NewEnd](
      on: GremlinScala[End] => GremlinScala[BranchOn],
      options: BranchOption[End, NewEnd]*): GremlinScala.Aux[NewEnd, Labels] = {
    var jTraversal: GraphTraversal[_, NewEnd] =
      traversal.choose(on(start).traversal)
    options.foreach { option =>
      /* cast needed because of the way types are defined in tp3 */
      val jTraversalOption =
        option.traversal(start).traversal.asInstanceOf[Traversal[NewEnd, _]]
      jTraversal = jTraversal.option(option.pickToken, jTraversalOption)
    }
    GremlinScala[NewEnd, Labels](jTraversal)
  }

  /** note that the traverser will go down all traversals in options if the pickToken matches
    * if you need if/then/else semantic, use `choose` instead */
  def branch[BranchOn, NewEnd](
      on: GremlinScala[End] => GremlinScala[BranchOn],
      options: BranchOption[End, NewEnd]*): GremlinScala.Aux[NewEnd, Labels] = {
    var jTraversal: GraphTraversal[_, NewEnd] =
      traversal.branch(on(start).traversal)
    options.foreach { option =>
      /* cast needed because of the way types are defined in tp3 */
      val jTraversalOption =
        option.traversal(start).traversal.asInstanceOf[Traversal[NewEnd, _]]
      jTraversal = jTraversal.option(option.pickToken, jTraversalOption)
    }
    GremlinScala[NewEnd, Labels](jTraversal)
  }

  def constant[A](value: A) = GremlinScala[A, Labels](traversal.constant(value))

  /** repeats the provided anonymous traversal which starts at the current End
    * combine with `times` or `until` step, e.g. `gs.V(1).repeat(_.out).times(2)`
    * Note: has to end on the same type (or a supertype of) the current `End`,
    * otherwise we couldn't reapply it multiple times. */
  def repeat[NewEnd >: End](repeatTraversal: GremlinScala.Aux[End, HNil] => GremlinScala[NewEnd])
    : GremlinScala.Aux[NewEnd, Labels] =
    /* gremlin-java is very restrictive here, repeat only allows traversals of the exact same type
     * this doesn't need to be this way, e.g. when extending vertex.
     * As a workaround we're casting our way around it which should be safe.
     * TODO: better solution: PR against gremlin-java */
    GremlinScala[NewEnd, Labels](
      traversal.asInstanceOf[GraphTraversal[_, NewEnd]].repeat(repeatTraversal(start).traversal))

  def until(untilTraversal: GremlinScala.Aux[End, HNil] => GremlinScala[_]) =
    GremlinScala[End, Labels](traversal.until(untilTraversal(start).traversal))

  def untilWithTraverser(predicate: Traverser[End] => Boolean) =
    GremlinScala[End, Labels](traversal.until(predicate))

  def times(maxLoops: Int) =
    GremlinScala[End, Labels](traversal.times(maxLoops))

  def tree() = GremlinScala[Tree[_], Labels](traversal.tree())

  def tree(sideEffectKey: String) =
    GremlinScala[End, Labels](traversal.tree(sideEffectKey))

  def is(value: AnyRef) = GremlinScala[End, Labels](traversal.is(value))

  def is(predicate: P[End]) = GremlinScala[End, Labels](traversal.is(predicate))

  def not(notTraversal: GremlinScala.Aux[End, HNil] => GremlinScala[_]) =
    GremlinScala[End, Labels](traversal.not(notTraversal(start).traversal))

  /** `predicate` refers to a step label */
  def where(predicate: P[String]): GremlinScala.Aux[End, Labels] =
    GremlinScala[End, Labels](traversal.where(predicate))

  /** `predicate` refers to a step label */
  def where(predicate: P[String], by: By[_]): GremlinScala.Aux[End, Labels] =
    GremlinScala[End, Labels](by(traversal.where(predicate)))

  /** `predicate` refers to a step label */
  def where(startKey: String, predicate: P[String]): GremlinScala.Aux[End, Labels] =
    GremlinScala[End, Labels](traversal.where(startKey, predicate))

  /** `predicate` refers to a step label */
  def where(startKey: String, predicate: P[String], by: By[_]): GremlinScala.Aux[End, Labels] =
    GremlinScala[End, Labels](by(traversal.where(startKey, predicate)))

  def where(whereTraversal: GremlinScala.Aux[End, HNil] => GremlinScala[_])
    : GremlinScala.Aux[End, Labels] =
    GremlinScala[End, Labels](traversal.where(whereTraversal(start).traversal))

  def addV() = GremlinScala[Vertex, Labels](traversal.addV())
  def addV(label: String) = GremlinScala[Vertex, Labels](traversal.addV(label))

  /** generic maths based on strings, see http://tinkerpop.apache.org/docs/3.3.1/reference/#math-step */
  def math(expression: String, bys: By[_]*): GremlinScala.Aux[JDouble, Labels] = {
    var newTrav: GraphTraversal[_, JDouble] = traversal.math(expression)
    bys.foreach { by =>
      newTrav = by(newTrav)
    }
    GremlinScala[JDouble, Labels](newTrav)
  }

  // ELEMENT STEPS START
  // -------------------

  /** set the property to the given value */
  def property[A](keyValue: KeyValue[A])(
      implicit ev: End <:< Element): GremlinScala.Aux[End, Labels] =
    property(keyValue.key, keyValue.value)

  /** set the property to the given value */
  def property[A](key: Key[A], value: A)(
      implicit ev: End <:< Element): GremlinScala.Aux[End, Labels] =
    GremlinScala[End, Labels](traversal.property(key.name, value))

  /** set the property to the value determined by the given traversal */
  def property[A](key: Key[A])(value: GremlinScala[End] => GremlinScala[A])(
      implicit ev: End <:< Element) =
    GremlinScala[End, Labels](traversal.property(key.name, value(start).traversal))

  def properties(keys: String*)(implicit ev: End <:< Element) =
    GremlinScala[Property[Any], Labels](
      traversal
        .properties(keys: _*)
        .asInstanceOf[GraphTraversal[_, Property[Any]]]
    )

  def propertyMap(keys: String*)(implicit ev: End <:< Element) =
    GremlinScala[JMap[String, Any], Labels](traversal.propertyMap(keys: _*))

  def key()(implicit ev: End <:< Element) =
    GremlinScala[String, Labels](traversal.key)

  def value[A](key: Key[A])(implicit ev: End <:< Element) =
    GremlinScala[A, Labels](traversal.values[A](key.name))

  def value[A](key: String)(implicit ev: End <:< Element) =
    GremlinScala[A, Labels](traversal.values[A](key))

  def valueOption[A](key: Key[A])(
      implicit ev: End <:< Element): GremlinScala.Aux[Option[A], Labels] =
    this.properties(key.name).map(_.toOption.asInstanceOf[Option[A]])

  def valueOption[A](key: String)(
      implicit ev: End <:< Element): GremlinScala.Aux[Option[A], Labels] =
    this.properties(key).map(_.toOption.asInstanceOf[Option[A]])

  def values[A](key: String*)(implicit ev: End <:< Element) =
    GremlinScala[A, Labels](traversal.values[A](key: _*))

  def valueMap(implicit ev: End <:< Element) =
    GremlinScala[JMap[AnyRef, AnyRef], Labels](traversal.valueMap())

  def valueMap(keys: String*)(implicit ev: End <:< Element) =
    GremlinScala[JMap[AnyRef, AnyRef], Labels](traversal.valueMap(keys: _*))

  /**
    * Map the {@link Element} to a {@code Map} of the property values key'd according to their {@link Property#key}.
    * For vertices, the {@code Map} will be returned with the assumption of single property values along with {@link T#id} and {@link T#label}. Prefer
    * {@link #valueMap(String...)} if multi-property processing is required. For edges, keys will include additional
    * related edge structure of {@link Direction#IN} and {@link Direction#OUT} which themselves are {@code Map}
    * instances of the particular {@link Vertex} represented by {@link T#id} and {@link T#label}.
    */
  def elementMap(keys: String*)(implicit ev: End <:< Element) =
    GremlinScala[JMap[AnyRef, AnyRef], Labels](traversal.elementMap(keys: _*))

  /**
    * Map the {@link Element} to a {@code Map} of the property values key'd according to their {@link Property#key}.
    * This step will retrieve all property values. For vertices, the {@code Map} will
    * be returned with the assumption of single property values along with {@link T#id} and {@link T#label}. Prefer
    * {@link #valueMap(String...)} if multi-property processing is required. For edges, keys will include additional
    * related edge structure of {@link Direction#IN} and {@link Direction#OUT} which themselves are {@code Map}
    * instances of the particular {@link Vertex} represented by {@link T#id} and {@link T#label}.
    */
  def elementMap(implicit ev: End <:< Element) =
    GremlinScala[JMap[AnyRef, AnyRef], Labels](traversal.elementMap())

  def has(key: Key[_])(implicit ev: End <:< Element) =
    GremlinScala[End, Labels](traversal.has(key.name))

  def has[A](key: Key[A], value: A)(implicit ev: End <:< Element) =
    GremlinScala[End, Labels](traversal.has(key.name, value))

  def has[A](keyValue: KeyValue[A])(implicit ev: End <:< Element) =
    GremlinScala[End, Labels](traversal.has(keyValue.key.name, keyValue.value))

  def has[A](p: (Key[A], A))(implicit ev: End <:< Element) =
    GremlinScala[End, Labels](traversal.has(p._1.name, p._2))

  def has[A](key: Key[A], predicate: P[A])(implicit ev: End <:< Element) =
    GremlinScala[End, Labels](traversal.has(key.name, predicate))

  def has(accessor: T, value: Any)(implicit ev: End <:< Element) =
    GremlinScala[End, Labels](traversal.has(accessor, value))

  def has[A](accessor: T, predicate: P[A])(implicit ev: End <:< Element) =
    GremlinScala[End, Labels](traversal.has(accessor, predicate))

  // A: type of the property value
  def has[A, B](key: Key[A], propertyTraversal: GremlinScala.Aux[A, HNil] => GremlinScala[B])(
      implicit ev: End <:< Element) =
    GremlinScala[End, Labels](traversal.has(key.name, propertyTraversal(start).traversal))

  def has[A](label: String, key: Key[A], value: A)(implicit ev: End <:< Element) =
    GremlinScala[End, Labels](traversal.has(label, key.name, value))

  def has[A](label: String, key: Key[A], predicate: P[A])(implicit ev: End <:< Element) =
    GremlinScala[End, Labels](traversal.has(label, key.name, predicate))

  def hasId(ids: AnyRef*)(implicit ev: End <:< Element) = {
    val list = ids.toList
    assert(list.nonEmpty, "must provide at least one id to filter on")
    GremlinScala[End, Labels](traversal.hasId(list.head, list.tail: _*))
  }

  def hasId(predicate: P[AnyRef])(implicit ev: End <:< Element) =
    GremlinScala[End, Labels](traversal.hasId(predicate))

  def hasLabel(label: String, labels: String*)(implicit ev: End <:< Element) =
    GremlinScala[End, Labels](traversal.hasLabel(label, labels: _*))

  def hasLabel[CC <: Product: ru.WeakTypeTag]()(
      implicit ev: End <:< Element): GremlinScala.Aux[End, Labels] = {
    val tpe = implicitly[ru.WeakTypeTag[CC]].tpe

    // TODO: there must be a way to avoid this...
    def unquote(s: String) = {
      val quote = "\""
      if (s.startsWith(quote) && s.endsWith(quote))
        s.substring(1, s.length - 1)
      else s
    }

    val label: String =
      tpe.typeSymbol.asClass.annotations
        .find { _.toString.startsWith("gremlin.scala.label(\"") }
        .map(_.tree.children.tail.head.toString)
        .map(unquote)
        .getOrElse(tpe.typeSymbol.name.toString)

    hasLabel(label)
  }

  def hasKey(key: Key[_], keys: Key[_]*) =
    GremlinScala[End, Labels](traversal.hasKey(key.name, keys.map(_.name): _*))

  def hasValue[A](value: A, values: AnyRef*) =
    GremlinScala[End, Labels](traversal.hasValue(value, values: _*))

  def hasNot(key: Key[_]) =
    GremlinScala[End, Labels](traversal.hasNot(key.name))

  def hasNot[A](keyValue: KeyValue[A]) =
    GremlinScala[End, Labels](traversal.not(__.traversal.has(keyValue.key.name, keyValue.value)))

  def hasNot[A](key: Key[A], value: A) =
    GremlinScala[End, Labels](traversal.not(__.traversal.has(key.name, value)))

  def hasNot[A](key: Key[A], predicate: P[A])(implicit ev: End <:< Element) =
    GremlinScala[End, Labels](traversal.not(__.traversal.has(key.name, predicate)))

  def and(traversals: (GremlinScala.Aux[End, HNil] => GremlinScala[_])*) =
    GremlinScala[End, Labels](traversal.and(traversals.map {
      _(start).traversal
    }: _*))

  def or(traversals: (GremlinScala.Aux[End, HNil] => GremlinScala[_])*) =
    GremlinScala[End, Labels](traversal.or(traversals.map {
      _(start).traversal
    }: _*))

  def local[A](localTraversal: GremlinScala.Aux[End, HNil] => GremlinScala[A])(
      implicit ev: End <:< Element) =
    GremlinScala[A, Labels](traversal.local(localTraversal(start).traversal))

  def timeLimit(millis: Long)(implicit ev: End <:< Element) =
    GremlinScala[End, Labels](traversal.timeLimit(millis))

  // ELEMENT STEPS END
  // -------------------

  // VERTEX STEPS START
  // -------------------
  def out()(implicit ev: End <:< Vertex) =
    GremlinScala[Vertex, Labels](traversal.out())

  def out(labels: String*)(implicit ev: End <:< Vertex) =
    GremlinScala[Vertex, Labels](traversal.out(labels: _*))

  def outE()(implicit ev: End <:< Vertex) =
    GremlinScala[Edge, Labels](traversal.outE())

  def outE(labels: String*)(implicit ev: End <:< Vertex) =
    GremlinScala[Edge, Labels](traversal.outE(labels: _*))

  def in()(implicit ev: End <:< Vertex) =
    GremlinScala[Vertex, Labels](traversal.in())

  def in(labels: String*)(implicit ev: End <:< Vertex) =
    GremlinScala[Vertex, Labels](traversal.in(labels: _*))

  def inE()(implicit ev: End <:< Vertex) =
    GremlinScala[Edge, Labels](traversal.inE())

  def inE(labels: String*)(implicit ev: End <:< Vertex) =
    GremlinScala[Edge, Labels](traversal.inE(labels: _*))

  def both()(implicit ev: End <:< Vertex) =
    GremlinScala[Vertex, Labels](traversal.both())

  def both(labels: String*)(implicit ev: End <:< Vertex) =
    GremlinScala[Vertex, Labels](traversal.both(labels: _*))

  def bothE()(implicit ev: End <:< Vertex) =
    GremlinScala[Edge, Labels](traversal.bothE())

  def bothE(labels: String*)(implicit ev: End <:< Vertex) =
    GremlinScala[Edge, Labels](traversal.bothE(labels: _*))

  /** may be used together with `from` / `to`, see TraversalSpec for examples */
  def addE(label: String, properties: KeyValue[_]*): GremlinScala.Aux[Edge, Labels] = {
    val newTrav = properties.foldLeft(traversal.addE(label)) { (trav, prop) =>
      trav.property(prop.key.name, prop.value)
    }
    GremlinScala[Edge, Labels](newTrav)
  }

  def addE(label: StepLabel[Vertex], properties: KeyValue[_]*): GremlinScala.Aux[Edge, Labels] =
    addE(label.name, properties: _*)

  /** modulator, use in conjunction with addE
    * http://tinkerpop.apache.org/docs/current/reference/#from-step */
  def from(vertex: Vertex): GremlinScala.Aux[End, Labels] =
    GremlinScala[End, Labels](traversal.from(vertex))

  /** modulator, use in conjunction with simplePath(), cyclicPath(), path(), and addE()
    * http://tinkerpop.apache.org/docs/current/reference/#from-step */
  def from(label: StepLabel[Vertex]): GremlinScala.Aux[End, Labels] =
    GremlinScala[End, Labels](traversal.from(label.name))

  /** modulator, use in conjunction with simplePath(), cyclicPath(), path(), and addE()
    * TODO: make this a standalone modulator like By that may only be used with the above mentioned steps
    * note: when using with addE, it only selects the first vertex!
    * http://tinkerpop.apache.org/docs/current/reference/#from-step
    * https://groups.google.com/forum/#!topic/gremlin-users/3YgKMKB4iNs */
  def from(
      fromTraversal: GremlinScala[Vertex] => GremlinScala[Vertex]): GremlinScala.Aux[End, Labels] =
    GremlinScala[End, Labels](
      traversal.from(fromTraversal(start).traversal.asInstanceOf[GraphTraversal[End, Vertex]]))

  /** modulator, use in conjunction with addE
    * http://tinkerpop.apache.org/docs/current/reference/#to-step */
  def to(vertex: Vertex): GremlinScala.Aux[End, Labels] =
    GremlinScala[End, Labels](traversal.to(vertex))

  /** modulator, use in conjunction with simplePath(), cyclicPath(), path(), and addE()
    * http://tinkerpop.apache.org/docs/current/reference/#from-step */
  def to(label: StepLabel[Vertex]): GremlinScala.Aux[End, Labels] =
    GremlinScala[End, Labels](traversal.to(label.name))

  /** modulator, use in conjunction with simplePath(), cyclicPath(), path(), and addE()
    * TODO: make this a standalone modulator like By that may only be used with the above mentioned steps
    * note: when using with addE, it only selects the first vertex!
    * http://tinkerpop.apache.org/docs/current/reference/#from-step
    * https://groups.google.com/forum/#!topic/gremlin-users/3YgKMKB4iNs */
  def to(toTraversal: GremlinScala[Vertex] => GremlinScala[Vertex]): GremlinScala.Aux[End, Labels] =
    GremlinScala[End, Labels](
      traversal.to(toTraversal(start).traversal.asInstanceOf[GraphTraversal[End, Vertex]]))

  def to(direction: Direction, edgeLabels: String*) =
    GremlinScala[Vertex, Labels](traversal.to(direction, edgeLabels: _*))

  // VERTEX STEPS END
  // -------------------

  // EDGE STEPS START
  // -------------------
  def inV()(implicit ev: End <:< Edge) =
    GremlinScala[Vertex, Labels](traversal.inV)
  def outV()(implicit ev: End <:< Edge) =
    GremlinScala[Vertex, Labels](traversal.outV)
  def bothV()(implicit ev: End <:< Edge) =
    GremlinScala[Vertex, Labels](traversal.bothV())
  def otherV()(implicit ev: End <:< Edge) =
    GremlinScala[Vertex, Labels](traversal.otherV())

  // see usage in TraversalSpec.scala
  def subgraph(stepLabel: StepLabel[Graph])(implicit ev: End <:< Edge) =
    GremlinScala[Edge, Labels](traversal.subgraph(stepLabel.name))
  // EDGE STEPS END
  // -------------------

  // NUMBER STEPS START
  // -------------------
  def max[C <: Comparable[_]]()(implicit toComparable: End => C) =
    GremlinScala[C, HNil](traversalToComparable.max[C])
  def max[C <: Comparable[_]](scope: Scope)(implicit toComparable: End => C) =
    GremlinScala[C, HNil](traversalToComparable.max[C](scope))

  def min[C <: Comparable[_]]()(implicit toComparable: End => C) =
    GremlinScala[C, HNil](traversalToComparable.min[C]())
  def min[C <: Comparable[_]](scope: Scope)(implicit toComparable: End => C) =
    GremlinScala[C, HNil](traversalToComparable.min[C](scope))

  def sum[N <: Number]()(implicit toNumber: End => N) =
    GremlinScala[N, HNil](traversalToNumber.sum())
  def sum[N <: Number](scope: Scope)(implicit toNumber: End => N) =
    GremlinScala[N, HNil](traversalToNumber.sum(scope))

  def mean[N <: Number]()(implicit toNumber: End => N) =
    GremlinScala[JDouble, HNil](traversalToNumber.mean())
  def mean[N <: Number](scope: Scope)(implicit toNumber: End => N) =
    GremlinScala[JDouble, HNil](traversalToNumber.mean(scope))

  private def traversalToNumber[N <: Number]()(implicit toNumber: End => N): GraphTraversal[_, N] =
    this.map(toNumber).traversal

  private def traversalToComparable[C <: Comparable[_]]()(implicit toComparable: End => C) =
    this.map(toComparable).traversal

  // NUMBER STEPS END
  // -------------------

  /** run pipeline asynchronously
    * note: only supported by RemoteGraphs (see `withRemote`) */
  def promise[NewEnd](onComplete: GremlinScala.Aux[End, Labels] => NewEnd): Future[NewEnd] = {
    val promise = Promise[NewEnd]
    val wrapperFun = (t: Traversal[_, _]) =>
      onComplete(GremlinScala(t.asInstanceOf[GraphTraversal[_, End]]))
    this.traversal
      .promise(wrapperFun)
      .whenComplete(toJavaBiConsumer((result: NewEnd, t: Throwable) =>
        if (t != null) promise.failure(t)
        else promise.success(result)))
    promise.future
  }

  /** convenience step for majority use case for `promise` */
  def promise(): Future[List[End]] =
    promise(_.toList)

  def V(vertexIdsOrElements: Any*)(implicit ev: End <:< Vertex): GremlinScala.Aux[Vertex, Labels] =
    GremlinScala[Vertex, Labels](traversal.V(vertexIdsOrElements.asInstanceOf[Seq[AnyRef]]: _*))

  protected def start[A] = __[A]()

  override def toString =
    traversal.toString

  def bytecode: Bytecode =
    traversal.asAdmin.getBytecode

  private def asTraversals[S, E](
      travs: Seq[GremlinScala.Aux[S, HNil] => GremlinScala[E]]): Seq[GraphTraversal[_, E]] =
    travs.map(_.apply(start).traversal)

}

trait ColumnType[FROM] {
  type KEY
  type VALUE
}

object ColumnType {
  type Aux[FROM, K, V] = ColumnType[FROM] {
    type KEY = K
    type VALUE = V
  }
  implicit def MapKeyColumn[K, V]: Aux[JMap[K, V], JSet[K], JCollection[V]] =
    new ColumnType[JMap[K, V]] {
      type KEY = JSet[K]
      type VALUE = JCollection[V]
    }
  implicit def MapEntryKeyColumn[K, V]: Aux[JMap.Entry[K, V], K, V] =
    new ColumnType[JMap.Entry[K, V]] {
      type KEY = K
      type VALUE = V
    }
  implicit val PathKeyColumn: Aux[Path, JList[JSet[String]], JList[Any]] = new ColumnType[Path] {
    type KEY = JList[JSet[String]]
    type VALUE = JList[Any]
  }
}
