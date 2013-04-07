package com.tinkerpop.gremlin.scala

import com.tinkerpop.gremlin.java.GremlinPipeline
import com.tinkerpop.blueprints._
import com.tinkerpop.pipes.{ PipeFunction, Pipe }
import com.tinkerpop.pipes.util.{ FluentUtility, StartPipe }
import com.tinkerpop.pipes.branch.LoopPipe.LoopBundle
import java.util.{ Map ⇒ JMap, List ⇒ JList, Iterator ⇒ JIterator, Collection ⇒ JCollection, ArrayList ⇒ JArrayList }
import java.lang.{ Boolean ⇒ JBoolean, Integer ⇒ JInteger }
import com.tinkerpop.gremlin.Tokens
import com.tinkerpop.pipes.util.structures.{ Tree, Table, Row, Pair ⇒ TPair }
import com.tinkerpop.pipes.transform.TransformPipe
import com.tinkerpop.pipes.util.structures.{ Pair ⇒ TinkerPair }

class GremlinScalaPipeline[S, E] extends GremlinPipeline[S, E] {

  def out: GremlinScalaPipeline[S, Vertex] = super.out()
  def in: GremlinScalaPipeline[S, Vertex] = super.in()
  def path: GremlinScalaPipeline[S, JList[_]] = super.path()

  def V(graph: Graph): GremlinScalaPipeline[Vertex, Vertex] = manualStart(graph.getVertices)
  def E(graph: Graph): GremlinScalaPipeline[Edge, Edge] = manualStart(graph.getEdges)

  def has[F <: Element, T](key: String, value: T): GremlinScalaPipeline[S, F] =
    super.has(key, value).asInstanceOf[GremlinScalaPipeline[S, F]]

  def has[F <: Element, T](key: String, comparison: Tokens.T, value: T): GremlinScalaPipeline[S, F] =
    super.has(key, comparison, value).asInstanceOf[GremlinScalaPipeline[S, F]]

  def hasNot[F <: Element, T](key: String, value: T): GremlinScalaPipeline[S, F] =
    super.hasNot(key, value).asInstanceOf[GremlinScalaPipeline[S, F]]

  def hasNot[F <: Element, T](key: String, comparison: Tokens.T, value: T): GremlinScalaPipeline[S, F] =
    super.hasNot(key, comparison, value).asInstanceOf[GremlinScalaPipeline[S, F]]

  override def bothE(labels: String*): GremlinScalaPipeline[S, Edge] = super.bothE(labels: _*)
  override def both(labels: String*): GremlinScalaPipeline[S, Vertex] = super.both(labels: _*)
  override def bothV: GremlinScalaPipeline[S, Vertex] = super.bothV

  override def idEdge(graph: Graph): GremlinScalaPipeline[S, Edge] = super.idEdge(graph)
  override def id: GremlinScalaPipeline[S, Object] = super.id
  override def idVertex(graph: Graph): GremlinScalaPipeline[S, Vertex] = super.idVertex(graph)

  override def inE(labels: String*): GremlinScalaPipeline[S, Edge] = super.inE(labels: _*)
  override def in(labels: String*): GremlinScalaPipeline[S, Vertex] = super.in(labels: _*)
  override def inV: GremlinScalaPipeline[S, Vertex] = super.inV
  override def outE(labels: String*): GremlinScalaPipeline[S, Edge] = super.outE(labels: _*)
  override def out(labels: String*): GremlinScalaPipeline[S, Vertex] = super.out(labels: _*)
  override def outV: GremlinScalaPipeline[S, Vertex] = super.outV

  override def label: GremlinScalaPipeline[S, String] = super.label

  //TODO use scala Map here?
  def map[F <: Element](keys: String*): GremlinScalaPipeline[S, JMap[String, Object]] = super.map(keys: _*)

  def property[F <: Element](key: String) = super.property(key).asInstanceOf[GremlinScalaPipeline[S, F]]

  override def step[F](pipe: Pipe[E, F]): GremlinScalaPipeline[S, F] = super.step(pipe)
  def step[F](f: JIterator[E] ⇒ F): GremlinScalaPipeline[S, F] =
    super.step(new ScalaPipeFunction(f)).asInstanceOf[GremlinScalaPipeline[S, F]]

  ////////////////////
  /// BRANCH PIPES ///
  ////////////////////
  override def copySplit(pipes: Pipe[E, _]*): GremlinScalaPipeline[S, _] = super.copySplit(pipes: _*)

  override def exhaustMerge: GremlinScalaPipeline[S, _] = super.exhaustMerge

  override def fairMerge: GremlinScalaPipeline[S, _] = super.fairMerge

  def ifThenElse(ifFunction: E ⇒ JBoolean, thenFunction: E ⇒ _, elseFunction: E ⇒ _): GremlinScalaPipeline[S, _] =
    super.ifThenElse(new ScalaPipeFunction(ifFunction), new ScalaPipeFunction(thenFunction), new ScalaPipeFunction(elseFunction))

  def loop(numberedStep: Int, whileFunction: LoopBundle[E] ⇒ JBoolean): GremlinScalaPipeline[S, E] =
    super.loop(numberedStep, new ScalaPipeFunction(whileFunction))

  def loop(namedStep: String, whileFunction: LoopBundle[E] ⇒ JBoolean): GremlinScalaPipeline[S, E] =
    super.loop(namedStep, new ScalaPipeFunction(whileFunction))

  def loop(numberedStep: Int, whileFunction: LoopBundle[E] ⇒ JBoolean, emitFunction: LoopBundle[E] ⇒ JBoolean): GremlinScalaPipeline[S, E] =
    super.loop(numberedStep, new ScalaPipeFunction(whileFunction), new ScalaPipeFunction(emitFunction))

  def loop(namedStep: String, whileFunction: LoopBundle[E] ⇒ JBoolean, emitFunction: LoopBundle[E] ⇒ JBoolean): GremlinScalaPipeline[S, E] =
    super.loop(namedStep, new ScalaPipeFunction(whileFunction), new ScalaPipeFunction(emitFunction))

  ////////////////////
  /// FILTER PIPES ///
  ////////////////////
  override def and(pipes: Pipe[E, _]*): GremlinScalaPipeline[S, E] = super.and(pipes: _*)

  override def back(numberedStep: Int): GremlinScalaPipeline[S, _] = super.back(numberedStep)
  override def back(namedStep: String): GremlinScalaPipeline[S, _] = super.back(namedStep)

  override def dedup: GremlinScalaPipeline[S, E] = super.dedup()
  def dedup(dedupFunction: E ⇒ _): GremlinScalaPipeline[S, E] = super.dedup(new ScalaPipeFunction(dedupFunction))

  override def except(collection: JCollection[E]): GremlinScalaPipeline[S, E] = super.except(collection)

  def filter(filterFunction: E ⇒ JBoolean): GremlinScalaPipeline[S, E] = super.filter(new ScalaPipeFunction(filterFunction))

  override def or(pipes: Pipe[E, _]*): GremlinScalaPipeline[S, E] = super.or(pipes: _*)

  def random(bias: Double): GremlinScalaPipeline[S, E] = super.random(bias)

  override def range(low: Int, high: Int): GremlinScalaPipeline[S, E] = super.range(low, high)

  override def retain(collection: JCollection[E]): GremlinScalaPipeline[S, E] = super.retain(collection)

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

  override def optional(numberedStep: Int): GremlinScalaPipeline[S, _] = super.optional(numberedStep)
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

  ///////////////////////
  /// TRANSFORM PIPES ///
  ///////////////////////
  override def gather: GremlinScalaPipeline[S, JList[_]] = super.gather()
  def gather(function: JList[_] ⇒ JList[_]): GremlinScalaPipeline[S, JList[_]] =
    super.gather(new ScalaPipeFunction(function)).asInstanceOf[GremlinScalaPipeline[S, JList[_]]]

  override def memoize(namedStep: String): GremlinScalaPipeline[S, E] = super.memoize(namedStep)
  override def memoize(numberedStep: Int): GremlinScalaPipeline[S, E] = super.memoize(numberedStep)
  override def memoize(namedStep: String, map: JMap[_, _]): GremlinScalaPipeline[S, E] = super.memoize(namedStep, map)
  override def memoize(numberedStep: Int, map: JMap[_, _]): GremlinScalaPipeline[S, E] = super.memoize(numberedStep, map)

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

  def transform[T](function: E ⇒ T): GremlinScalaPipeline[S, T] = super.transform(new ScalaPipeFunction(function))
  def apply[T](function: E ⇒ T): GremlinScalaPipeline[S, T] = transform(function)

  def order(compareFunction: PipeFunction[TPair[E, E], Int]): GremlinScalaPipeline[S, E] =
    super.order({ x: TPair[E, E] ⇒ compareFunction.compute(x).asInstanceOf[JInteger] })

  override def order: GremlinScalaPipeline[S, E] = super.order()
  override def order(by: Tokens.T) = super.order(by)
  override def order(by: TransformPipe.Order): GremlinScalaPipeline[S, E] = super.order(by)

  //////////////////////
  /// UTILITY PIPES ///
  //////////////////////

  override def as(name: String): GremlinScalaPipeline[S, E] = super.as(name)
  override def start(starts: S): GremlinScalaPipeline[S, S] = super.start(starts)

  private def manualStart[T](start: Any): GremlinScalaPipeline[T, T] = {
    val pipe = this.add(new StartPipe[S](start))
    FluentUtility.setStarts(this, start)
    pipe.asInstanceOf[GremlinScalaPipeline[T, T]]
  }

  implicit private def scalaPipeline[A, B](pipeline: GremlinPipeline[A, B]): GremlinScalaPipeline[A, B] =
    pipeline.asInstanceOf[GremlinScalaPipeline[A, B]]
}
