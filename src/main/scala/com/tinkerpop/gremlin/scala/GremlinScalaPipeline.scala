package com.tinkerpop.gremlin.scala

import com.tinkerpop.gremlin.java.GremlinPipeline
import com.tinkerpop.blueprints._
import com.tinkerpop.pipes._
import com.tinkerpop.pipes.branch.LoopPipe.LoopBundle
import java.util.{ Map ⇒ JMap, List ⇒ JList, Iterator ⇒ JIterator, Collection ⇒ JCollection, ArrayList ⇒ JArrayList }
import java.lang.{ Boolean ⇒ JBoolean, Integer ⇒ JInteger, Iterable ⇒ JIterable }
import com.tinkerpop.gremlin.Tokens
import com.tinkerpop.pipes.util.structures.{ Tree, Table, Row, Pair ⇒ TPair }
import com.tinkerpop.pipes.transform.TransformPipe
import com.tinkerpop.pipes.util.structures.{ Pair ⇒ TinkerPair }
import scala.collection.JavaConversions._
import com.tinkerpop.pipes.filter._
import com.tinkerpop.pipes.transform._
import com.tinkerpop.pipes.branch._
import com.tinkerpop.pipes.util._
import com.tinkerpop.gremlin.scala.pipes.PropertyMapPipe
import com.tinkerpop.pipes.util.structures.AsMap
import scala.language.dynamics
import scala.collection.convert.wrapAsJava
import com.tinkerpop.pipes.transform.InVertexPipe

class GremlinScalaPipeline[S, E] extends GremlinPipeline[S, E] with Dynamic {
  def out: GremlinScalaPipeline[S, Vertex] = super.out()
  def in: GremlinScalaPipeline[S, Vertex] = super.in()
  def path: GremlinScalaPipeline[S, JList[_]] = super.path()

  def V(graph: Graph): GremlinScalaPipeline[ScalaVertex, ScalaVertex] = {
    val vertices = graph.getVertices.iterator.map { v ⇒ ScalaVertex(v) }
    val jIterator: JIterable[ScalaVertex] = wrapAsJava.asJavaIterable(vertices.toIterable)
    manualStart(jIterator)
  }

  def E(graph: Graph): GremlinScalaPipeline[ScalaEdge, ScalaEdge] = {
    val edges = graph.getEdges.iterator.map { e ⇒ ScalaEdge(e) }
    val jIterator: JIterable[ScalaEdge] = wrapAsJava.asJavaIterable(edges.toIterable)
    manualStart(jIterator)
  }

  /** Check if the element has a property with provided key */
  def has[_](key: String): GremlinScalaPipeline[S, E] =
    has(key, Tokens.T.neq, null)

  /** Check if the element has a property with provided key/value */
  def has[_](key: String, value: Any): GremlinScalaPipeline[S, E] =
    has(key, Tokens.T.eq, value)

  /** Check if the element does not have a property with provided key. */
  def hasNot[_](key: String): GremlinScalaPipeline[S, E] =
    has(key, Tokens.T.eq, null)

  /** Check if the element does not have a property with provided key/value */
  def hasNot[_](key: String, value: Any): GremlinScalaPipeline[S, E] =
    has(key, Tokens.T.neq, value)

  /** Check if the element has a property with provided key/value that matches the given comparison token */
  def has[_](key: String, comparison: Tokens.T, value: Any): GremlinScalaPipeline[S, E] =
    has(key, Tokens.mapPredicate(comparison), value)

  /** Check if the element has a property with provided key/value that matches the given predicate */
  def has[_](key: String, predicate: Predicate, value: Any): GremlinScalaPipeline[S, E] = {
    val pipeline = key match {
      case Tokens.ID    ⇒ addPipe2(new IdFilterPipe(predicate, value))
      case Tokens.LABEL ⇒ addPipe2(new LabelFilterPipe(predicate, value))
      case _            ⇒ addPipe2(new PropertyFilterPipe(key, predicate, value))
    }
    pipeline.asInstanceOf[GremlinScalaPipeline[S, E]]
  }

  override def bothE(labels: String*): GremlinScalaPipeline[S, Edge] = super.bothE(labels: _*)
  override def both(labels: String*): GremlinScalaPipeline[S, Vertex] = super.both(labels: _*)
  override def bothV: GremlinScalaPipeline[S, Vertex] = super.bothV

  override def idEdge(graph: Graph): GremlinScalaPipeline[S, Edge] = super.idEdge(graph)
  override def id: GremlinScalaPipeline[S, Object] = super.id
  override def idVertex(graph: Graph): GremlinScalaPipeline[S, Vertex] = super.idVertex(graph)

  override def inE(labels: String*): GremlinScalaPipeline[S, Edge] = super.inE(labels: _*)
  override def in(labels: String*): GremlinScalaPipeline[S, Vertex] = super.in(labels: _*)
  override def inV: GremlinScalaPipeline[S, Vertex] = super.inV
  //def inV: GremlinScalaPipeline[S, ScalaVertex] = addPipe2(new InVertexPipe().asInstanceOf[Pipe[_, ScalaVertex]])
  override def outE(labels: String*): GremlinScalaPipeline[S, Edge] = super.outE(labels: _*)
  override def out(labels: String*): GremlinScalaPipeline[S, Vertex] = super.out(labels: _*)
  override def outV: GremlinScalaPipeline[S, Vertex] = super.outV

  override def label: GremlinScalaPipeline[S, String] = super.label

  def propertyMap(keys: String*): GremlinScalaPipeline[S, Map[String, Any]] = addPipe2(new PropertyMapPipe(keys: _*))
  def propertyMap: GremlinScalaPipeline[S, Map[String, Any]] = propertyMap()

  def property[F](key: String): GremlinScalaPipeline[S, F] = addPipe2(new PropertyPipe(key, false))

  override def step[F](pipe: Pipe[E, F]): GremlinScalaPipeline[S, F] = super.step(pipe)
  def step[F](f: JIterator[E] ⇒ F): GremlinScalaPipeline[S, F] =
    super.step(new ScalaPipeFunction(f)).asInstanceOf[GremlinScalaPipeline[S, F]]

  ////////////////////
  /// BRANCH PIPES ///
  ////////////////////
  /** Copies incoming object to internal pipes. */
  override def copySplit(pipes: Pipe[E, _]*): GremlinScalaPipeline[S, _] = super.copySplit(pipes: _*)

  override def exhaustMerge: GremlinScalaPipeline[S, _] = super.exhaustMerge

  /** Used in combination with a copySplit, merging the parallel traversals in a round-robin fashion. */
  override def fairMerge: GremlinScalaPipeline[S, _] = super.fairMerge

  def ifThenElse(ifFunction: E ⇒ Boolean, thenFunction: E ⇒ _, elseFunction: E ⇒ _): GremlinScalaPipeline[S, _] =
    addPipe2(new IfThenElsePipe(
      new ScalaPipeFunction(ifFunction),
      new ScalaPipeFunction(thenFunction),
      new ScalaPipeFunction(elseFunction)
    ))

  /**
   * Add a LoopPipe to the end of the Pipeline.
   * Looping is useful for repeating a section of a pipeline.
   * The provided whileFunction determines when to drop out of the loop.
   * The whileFunction is provided a LoopBundle object which contains the object in loop along with other useful metadata.
   *
   * @param namedStep     the name of the step to loop back to
   * @param whileFunction whether or not to continue looping on the current object
   */
  def loop(namedStep: String, whileFunction: LoopBundle[E] ⇒ Boolean): GremlinScalaPipeline[S, E] =
    addPipe2(new LoopPipe(new Pipeline(FluentUtility.removePreviousPipes(this, namedStep)), whileFunction))

  /**
   * Add a LoopPipe to the end of the Pipeline.
   * Looping is useful for repeating a section of a pipeline.
   * The provided whileFunction determines when to drop out of the loop.
   * The provided emitFunction can be used to emit objects that are still going through a loop.
   * The whileFunction and emitFunctions are provided a LoopBundle object which contains the object in loop along with other useful metadata.
   *
   * @param namedStep     the number of steps to loop back to
   * @param whileFun whether or not to continue looping on the current object
   * @param emit whether or not to emit the current object (irrespective of looping)
   */
  def loop(namedStep: String,
           whileFun: LoopBundle[E] ⇒ Boolean,
           emit: LoopBundle[E] ⇒ Boolean): GremlinScalaPipeline[S, E] =
    addPipe2(new LoopPipe(new Pipeline(FluentUtility.removePreviousPipes(this, namedStep)), whileFun, emit))

  ////////////////////
  /// FILTER PIPES ///
  ////////////////////
  override def and(pipes: Pipe[E, _]*): GremlinScalaPipeline[S, E] = super.and(pipes: _*)

  override def back(namedStep: String): GremlinScalaPipeline[S, _] = super.back(namedStep)

  override def dedup: GremlinScalaPipeline[S, E] = super.dedup()
  def dedup(dedupFunction: E ⇒ _): GremlinScalaPipeline[S, E] = super.dedup(new ScalaPipeFunction(dedupFunction))

  override def except(collection: JCollection[E]): GremlinScalaPipeline[S, E] = super.except(collection)
  override def except(namedSteps: String*): GremlinScalaPipeline[S, E] = super.except(namedSteps: _*)

  def filter(f: E ⇒ Boolean): GremlinScalaPipeline[S, E] = addPipe2(new FilterFunctionPipe[E](f))
  def filterPF(f: PartialFunction[E, Boolean]): GremlinScalaPipeline[S, E] = addPipe2(new FilterFunctionPipe[E](f))
  def filterNot(f: E ⇒ Boolean): GremlinScalaPipeline[S, E] = addPipe2(new FilterFunctionPipe[E]({ e: E ⇒ !f(e) }))
  def filterNotPF(f: PartialFunction[E, Boolean]): GremlinScalaPipeline[S, E] = addPipe2(new FilterFunctionPipe[E]({ e: E ⇒ !f(e) }))

  override def or(pipes: Pipe[E, _]*): GremlinScalaPipeline[S, E] = super.or(pipes: _*)

  def random(bias: Double): GremlinScalaPipeline[S, E] = super.random(bias)

  override def range(low: Int, high: Int): GremlinScalaPipeline[S, E] = super.range(low, high)

  override def retain(collection: JCollection[E]): GremlinScalaPipeline[S, E] = super.retain(collection)
  override def retain(namedSteps: String*): GremlinScalaPipeline[S, E] = super.retain(namedSteps: _*)

  override def simplePath: GremlinScalaPipeline[S, E] = super.simplePath()

  /////////////////////////
  /// SIDE-EFFECT PIPES ///
  /////////////////////////
  override def aggregate(aggregate: JCollection[E]): GremlinScalaPipeline[S, E] = super.aggregate(aggregate)
  override def aggregate(): GremlinScalaPipeline[S, E] = super.aggregate(new JArrayList[E]())

  def aggregate(aggregate: JCollection[_], aggregateFunction: E ⇒ _): GremlinScalaPipeline[S, E] =
    super.aggregate(aggregate, new ScalaPipeFunction(aggregateFunction))

  def aggregate(aggregateFunction: E ⇒ _): GremlinScalaPipeline[S, E] =
    super.aggregate(new JArrayList[Object](), new ScalaPipeFunction(aggregateFunction))

  override def optional(namedStep: String): GremlinScalaPipeline[S, _] = super.optional(namedStep)

  override def groupBy(map: JMap[_, JList[_]], keyFunction: PipeFunction[_, _], valueFunction: PipeFunction[_, _]): GremlinScalaPipeline[S, E] =
    super.groupBy(map, keyFunction, valueFunction)

  override def groupBy(keyFunction: PipeFunction[_, _], valueFunction: PipeFunction[_, _]): GremlinScalaPipeline[S, E] =
    super.groupBy(keyFunction, valueFunction)

  override def groupBy(keyFunction: PipeFunction[_, _], valueFunction: PipeFunction[_, _], reduceFunction: PipeFunction[_, _]): GremlinScalaPipeline[S, E] =
    super.groupBy(keyFunction, valueFunction, reduceFunction)

  override def groupBy(reduceMap: JMap[_, _], keyFunction: PipeFunction[_, _], valueFunction: PipeFunction[_, _], reduceFunction: PipeFunction[_, _]): GremlinScalaPipeline[S, E] =
    super.groupBy(reduceMap, keyFunction, valueFunction, reduceFunction)

  override def groupCount(map: JMap[_, Number], keyFunction: PipeFunction[_, _], valueFunction: PipeFunction[TPair[_, Number], Number]): GremlinScalaPipeline[S, E] =
    super.groupCount(map, keyFunction, valueFunction)

  override def groupCount(keyFunction: PipeFunction[_, _], valueFunction: PipeFunction[TPair[_, Number], Number]): GremlinScalaPipeline[S, E] =
    super.groupCount(keyFunction, valueFunction)

  override def groupCount(map: JMap[_, Number], keyFunction: PipeFunction[_, _]): GremlinScalaPipeline[S, E] = super.groupCount(map, keyFunction)
  override def groupCount(keyFunction: PipeFunction[_, _]): GremlinScalaPipeline[S, E] = super.groupCount(keyFunction)
  override def groupCount(map: JMap[_, Number]): GremlinScalaPipeline[S, E] = super.groupCount(map)
  override def groupCount: GremlinScalaPipeline[S, E] = super.groupCount()

  def sideEffect(sideEffectFunction: E ⇒ _): GremlinScalaPipeline[S, E] = super.sideEffect(new ScalaPipeFunction(sideEffectFunction))

  override def store(storage: JCollection[E]): GremlinScalaPipeline[S, E] = super.store(storage)
  override def store(): GremlinScalaPipeline[S, E] = super.store(new JArrayList[E]())
  def store(storage: JCollection[_], storageFunction: E ⇒ _): GremlinScalaPipeline[S, E] = super.aggregate(storage, new ScalaPipeFunction(storageFunction))
  def store(storageFunction: E ⇒ _): GremlinScalaPipeline[S, E] = super.store(new JArrayList[Object](), new ScalaPipeFunction(storageFunction))

  override def table(table: Table, stepNames: JCollection[String], columnFunctions: PipeFunction[_, _]*): GremlinScalaPipeline[S, E] =
    super.table(table, stepNames, columnFunctions: _*)

  override def table(table: Table, columnFunctions: PipeFunction[_, _]*): GremlinScalaPipeline[S, E] = super.table(table, columnFunctions: _*)
  override def table(columnFunctions: PipeFunction[_, _]*): GremlinScalaPipeline[S, E] = super.table(columnFunctions: _*)
  override def table(table: Table): GremlinScalaPipeline[S, E] = super.table(table)
  override def table: GremlinScalaPipeline[S, E] = super.table()

  override def tree(tree: Tree[_], branchFunctions: PipeFunction[_, _]*): GremlinScalaPipeline[S, E] = super.tree(tree, branchFunctions: _*)
  override def tree(branchFunctions: PipeFunction[_, _]*): GremlinScalaPipeline[S, E] = super.tree(branchFunctions: _*)

  /**
   * Add a OrderMapPipe to the end of the Pipeline
   * Given a Map as an input, the map is first ordered and then the keys are emitted in the order.
   *
   * @param order if the values implement Comparable, then a increment or decrement sort is usable
   * @return the extended Pipeline
   */
  override def orderMap(by: Tokens.T): GremlinScalaPipeline[S, _] = super.orderMap(by)

  /**
   * Add a OrderMapPipe to the end of the Pipeline
   * Given a Map as an input, the map is first ordered and then the keys are emitted in the order.
   *
   * @param order if the values implement Comparable, then a increment or decrement sort is usable
   * @return the extended Pipeline
   */
  override def orderMap(by: TransformPipe.Order): GremlinScalaPipeline[S, _] = super.orderMap(by)

  /**
   * Add a OrderMapPipe to the end of the Pipeline
   * Given a Map as an input, the map is first ordered and then the keys are emitted in the order.
   *
   * @param compareFunction a function to compare to map entries
   * @return the extended Pipeline
   */
  def orderMap(by: TinkerPair[JMap.Entry[_, _], JMap.Entry[_, _]] ⇒ Integer): GremlinScalaPipeline[S, _] =
    super.orderMap(new ScalaPipeFunction(by))

  /**
   * Add a LinkPipe to the end of the Pipeline.
   * Emit the incoming vertex, but have other vertex provide an outgoing edge to incoming vertex.
   *
   * @param label     the edge label
   * @param namedStep the step name that has the other vertex to link to
   * @return the extended Pipeline
   */
  override def linkOut(label: String, namedStep: String): GremlinScalaPipeline[S, Vertex] =
    super.linkOut(label, namedStep)

  /**
   * Add a LinkPipe to the end of the Pipeline.
   * Emit the incoming vertex, but have other vertex provide an incoming edge to incoming vertex.
   *
   * @param label     the edge label
   * @param namedStep the step name that has the other vertex to link to
   * @return the extended Pipeline
   */
  override def linkIn(label: String, namedStep: String): GremlinScalaPipeline[S, Vertex] =
    super.linkIn(label, namedStep)

  /**
   * Add a LinkPipe to the end of the Pipeline.
   * Emit the incoming vertex, but have other vertex provide an incoming and outgoing edge to incoming vertex.
   *
   * @param label     the edge label
   * @param namedStep the step name that has the other vertex to link to
   * @return the extended Pipeline
   */
  override def linkBoth(label: String, namedStep: String): GremlinScalaPipeline[S, Vertex] =
    super.linkBoth(label, namedStep)

  /**
   * Add a LinkPipe to the end of the Pipeline.
   * Emit the incoming vertex, but have other vertex provide an outgoing edge to incoming vertex.
   *
   * @param label the edge label
   * @param other the other vertex
   * @return the extended Pipeline
   */
  override def linkOut(label: String, other: Vertex): GremlinScalaPipeline[S, Vertex] =
    super.linkOut(label, other)

  /**
   * Add a LinkPipe to the end of the Pipeline.
   * Emit the incoming vertex, but have other vertex provide an incoming edge to incoming vertex.
   *
   * @param label the edge label
   * @param other the other vertex
   * @return the extended Pipeline
   */
  override def linkIn(label: String, other: Vertex): GremlinScalaPipeline[S, Vertex] =
    super.linkIn(label, other)

  /**
   * Add a LinkPipe to the end of the Pipeline.
   * Emit the incoming vertex, but have other vertex provide an incoming and outgoing edge to incoming vertex.
   *
   * @param label the edge label
   * @param other the other vertex
   * @return the extended Pipeline
   */
  override def linkBoth(label: String, other: Vertex): GremlinScalaPipeline[S, Vertex] =
    super.linkBoth(label, other)

  ///////////////////////
  /// TRANSFORM PIPES ///
  ///////////////////////
  override def gather: GremlinScalaPipeline[S, JList[_]] = super.gather()
  def gather(function: JList[_] ⇒ JList[_]): GremlinScalaPipeline[S, _] = super.gather(new ScalaPipeFunction(function))

  override def memoize(namedStep: String): GremlinScalaPipeline[S, E] = super.memoize(namedStep)
  override def memoize(namedStep: String, map: JMap[_, _]): GremlinScalaPipeline[S, E] = super.memoize(namedStep, map)

  override def path(pathFunctions: PipeFunction[_, _]*): GremlinScalaPipeline[S, JList[_]] = super.path(pathFunctions: _*)

  override def select: GremlinScalaPipeline[S, Row[_]] = super.select()
  override def select(stepFunctions: PipeFunction[_, _]*): GremlinScalaPipeline[S, Row[_]] = super.select(stepFunctions: _*)
  override def select(stepNames: JCollection[String], stepFunctions: PipeFunction[_, _]*): GremlinScalaPipeline[S, Row[_]] =
    super.select(stepNames, stepFunctions: _*)

  /**
   * Add a ShufflePipe to the end of the Pipeline.
   * All the objects previous to this step are aggregated in a greedy fashion, their order randomized and emitted
   * as a List.
   *
   * @return the extended Pipeline
   */
  override def shuffle: GremlinScalaPipeline[S, JList[_]] = super.shuffle()

  override def scatter: GremlinScalaPipeline[S, _] = super.scatter()

  override def cap: GremlinScalaPipeline[S, _] = super.cap()

  def map[T](function: E ⇒ T): GremlinScalaPipeline[S, T] = super.transform(new ScalaPipeFunction(function))
  def transform[T](function: E ⇒ T): GremlinScalaPipeline[S, T] = transform(function)

  def order(compareFunction: PipeFunction[TPair[E, E], Int]): GremlinScalaPipeline[S, E] =
    super.order({ x: TPair[E, E] ⇒ new JInteger(compareFunction.compute(x)) })

  override def order: GremlinScalaPipeline[S, E] = super.order()
  override def order(by: Tokens.T) = super.order(by)
  override def order(by: TransformPipe.Order): GremlinScalaPipeline[S, E] = super.order(by)

  //////////////////////
  /// UTILITY PIPES ///
  //////////////////////

  override def as(name: String): GremlinScalaPipeline[S, E] = super.as(name)
  override def start(starts: S): GremlinScalaPipeline[S, S] = super.start(starts)

  def toScalaList(): List[E] = iterableAsScalaIterable(this).toList

  private def manualStart[T](start: JIterable[_]): GremlinScalaPipeline[T, T] = {
    val pipe = addPipe2(new StartPipe[S](start))
    FluentUtility.setStarts(this, start)
    pipe.asInstanceOf[GremlinScalaPipeline[T, T]]
  }

  def addPipe2[T](pipe: Pipe[_, T]): GremlinScalaPipeline[S, T] = {
    addPipe(pipe)
    this.asInstanceOf[GremlinScalaPipeline[S, T]]
  }

  //TODO: remove
  implicit private def scalaPipeline[A, B](pipeline: GremlinPipeline[A, B]): GremlinScalaPipeline[A, B] =
    pipeline.asInstanceOf[GremlinScalaPipeline[A, B]]

  implicit def boolean2BooleanFn(fn: E ⇒ Boolean)(e: E): JBoolean = fn(e)
  def selectDynamic[F](field: String): GremlinScalaPipeline[S, F] = property(field)

}
