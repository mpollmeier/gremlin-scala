package com.tinkerpop.gremlin.scala

import com.tinkerpop.gremlin.java.GremlinPipeline
import com.tinkerpop.blueprints._
import com.tinkerpop.pipes._
import com.tinkerpop.pipes.branch.LoopPipe.LoopBundle
import java.util.{ Map ⇒ JMap, HashMap ⇒ JHashMap, List ⇒ JList, Iterator ⇒ JIterator, Collection ⇒ JCollection, ArrayList ⇒ JArrayList }
import java.lang.{ Boolean ⇒ JBoolean, Integer ⇒ JInteger, Iterable ⇒ JIterable }
import com.tinkerpop.gremlin.Tokens
import com.tinkerpop.pipes.util.structures.{ Tree, Table, Row, Pair ⇒ TPair }
import com.tinkerpop.pipes.transform.TransformPipe
import com.tinkerpop.pipes.transform.TransformPipe.Order
import com.tinkerpop.pipes.util.structures.{ Pair ⇒ TinkerPair }
import com.tinkerpop.pipes.filter._
import com.tinkerpop.pipes.transform._
import com.tinkerpop.pipes.branch._
import com.tinkerpop.pipes.util._
import com.tinkerpop.pipes.sideeffect._
import com.tinkerpop.gremlin.scala.pipes.PropertyMapPipe
import com.tinkerpop.pipes.util.structures.AsMap
import scala.language.dynamics
import scala.collection.convert.wrapAsJava
import com.tinkerpop.pipes.transform.InVertexPipe
import scala.collection.JavaConversions._
import scala.collection.mutable

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

  /** checks if a given property is in an interval (startValue: inclusive, endValue: exclusive)  */
  def interval[_](key: String, startValue: Comparable[_], endValue: Comparable[_]): GremlinScalaPipeline[S, ScalaElement] =
    addPipe2(new IntervalFilterPipe[ScalaElement](key, startValue, endValue))

  override def both(labels: String*): GremlinScalaPipeline[S, Vertex] = super.both(labels: _*)
  override def bothE(labels: String*): GremlinScalaPipeline[S, Edge] = super.bothE(labels: _*)
  override def bothV: GremlinScalaPipeline[S, Vertex] = super.bothV

  override def idEdge(graph: Graph): GremlinScalaPipeline[S, Edge] = super.idEdge(graph)
  override def id: GremlinScalaPipeline[S, Object] = super.id
  override def idVertex(graph: Graph): GremlinScalaPipeline[S, Vertex] = super.idVertex(graph)

  override def in(labels: String*): GremlinScalaPipeline[S, Vertex] = super.in(labels: _*)
  override def inE(labels: String*): GremlinScalaPipeline[S, Edge] = super.inE(labels: _*)
  override def inV: GremlinScalaPipeline[S, Vertex] = super.inV
  override def outE(labels: String*): GremlinScalaPipeline[S, Edge] = super.outE(labels: _*)
  override def out(labels: String*): GremlinScalaPipeline[S, Vertex] = super.out(labels: _*)
  override def outV: GremlinScalaPipeline[S, Vertex] = super.outV

  override def label: GremlinScalaPipeline[S, String] = addPipe2(new LabelPipe)

  def propertyMap(keys: String*): GremlinScalaPipeline[S, Map[String, Any]] = addPipe2(new PropertyMapPipe(keys: _*))
  def propertyMap: GremlinScalaPipeline[S, Map[String, Any]] = propertyMap()

  def property[F](key: String): GremlinScalaPipeline[S, F] = addPipe2(new PropertyPipe(key, false))

  ////////////////////
  /// BRANCH PIPES ///
  ////////////////////
  /** Copies incoming object to internal pipes. */
  override def copySplit(pipes: Pipe[E, _]*): GremlinScalaPipeline[S, _] = addPipe2(new CopySplitPipe(pipes))

  override def exhaustMerge: GremlinScalaPipeline[S, _] =
    addPipe2(new ExhaustMergePipe(FluentUtility.getPreviousPipe(this).asInstanceOf[MetaPipe].getPipes))

  /** Used in combination with a copySplit, merging the parallel traversals in a round-robin fashion. */
  override def fairMerge: GremlinScalaPipeline[S, _] =
    addPipe2(new FairMergePipe(FluentUtility.getPreviousPipe(this).asInstanceOf[MetaPipe].getPipes))

  def ifThenElse(ifFunction: E ⇒ Boolean, thenFunction: E ⇒ _, elseFunction: E ⇒ _): GremlinScalaPipeline[S, _] =
    addPipe2(new IfThenElsePipe(
      new ScalaPipeFunction(ifFunction),
      new ScalaPipeFunction(thenFunction),
      new ScalaPipeFunction(elseFunction)
    ))

  /** Add a LoopPipe to the end of the Pipeline.
   *  Looping is useful for repeating a section of a pipeline.
   *  The provided whileFunction determines when to drop out of the loop.
   *  The whileFunction is provided a LoopBundle object which contains the object in loop along with other useful metadata.
   *
   *  @param namedStep     the name of the step to loop back to
   *  @param whileFunction whether or not to continue looping on the current object
   */
  def loop(namedStep: String, whileFunction: LoopBundle[E] ⇒ Boolean): GremlinScalaPipeline[S, E] =
    addPipe2(new LoopPipe(new Pipeline(FluentUtility.removePreviousPipes(this, namedStep)), whileFunction))

  /** Add a LoopPipe to the end of the Pipeline.
   *  Looping is useful for repeating a section of a pipeline.
   *  The provided whileFunction determines when to drop out of the loop.
   *  The provided emitFunction can be used to emit objects that are still going through a loop.
   *  The whileFunction and emitFunctions are provided a LoopBundle object which contains the object in loop along with other useful metadata.
   *
   *  @param namedStep     the number of steps to loop back to
   *  @param whileFun whether or not to continue looping on the current object
   *  @param emit whether or not to emit the current object (irrespective of looping)
   */
  def loop(namedStep: String,
           whileFun: LoopBundle[E] ⇒ Boolean,
           emit: LoopBundle[E] ⇒ Boolean): GremlinScalaPipeline[S, E] =
    addPipe2(new LoopPipe(new Pipeline(FluentUtility.removePreviousPipes(this, namedStep)), whileFun, emit))

  ////////////////////
  /// FILTER PIPES ///
  ////////////////////
  /** Takes a collection of pipes and emits incoming objects that are true for all of the pipes. */
  override def and(pipes: Pipe[E, _]*): GremlinScalaPipeline[S, E] = addPipe2(new AndFilterPipe[E](pipes: _*))

  override def back(namedStep: String): GremlinScalaPipeline[S, Any] =
    addPipe2(new BackFilterPipe(new Pipeline(FluentUtility.removePreviousPipes(this, namedStep))))

  override def dedup: GremlinScalaPipeline[S, E] = addPipe2(new DuplicateFilterPipe[E])

  /** only emits the object if the object generated by its function hasn't been seen before. */
  def dedup(dedupFunction: E ⇒ _): GremlinScalaPipeline[S, E] = addPipe2(new DuplicateFilterPipe[E](dedupFunction))

  /** emits everything except what is in the supplied collection. */
  def except(iterable: Iterable[E]): GremlinScalaPipeline[S, E] = addPipe2(new ExceptFilterPipe[E](iterable))

  /** emits everything except what is in the results of a named step.
   *
   *  not currently supported because ExceptFilterPipe uses ElementHelper.areEqual to compare two elements, which compares if the classes are equal.
   *  I'll open a pull request to fix that in blueprints shortly...
   */
  override def except(namedSteps: String*): GremlinScalaPipeline[S, E] = throw new NotImplementedError("not currently supported")

  /** retains everything that is in the supplied collection. */
  def retain(iterable: Iterable[E]): GremlinScalaPipeline[S, E] = addPipe2(new RetainFilterPipe[E](iterable))

  /** retains everything that is in the results of a named step.
   *
   *  not currently supported because RetainFilterPipe uses ElementHelper.areEqual to compare two elements, which compares if the classes are equal.
   *  I'll open a pull request to fix that in blueprints shortly...
   */
  override def retain(namedSteps: String*): GremlinScalaPipeline[S, E] = throw new NotImplementedError("not currently supported")

  def filter(f: E ⇒ Boolean): GremlinScalaPipeline[S, E] = addPipe2(new FilterFunctionPipe[E](f))
  def filterPF(f: PartialFunction[E, Boolean]): GremlinScalaPipeline[S, E] = addPipe2(new FilterFunctionPipe[E](f))
  def filterNot(f: E ⇒ Boolean): GremlinScalaPipeline[S, E] = addPipe2(new FilterFunctionPipe[E]({ e: E ⇒ !f(e) }))
  def filterNotPF(f: PartialFunction[E, Boolean]): GremlinScalaPipeline[S, E] = addPipe2(new FilterFunctionPipe[E]({ e: E ⇒ !f(e) }))

  override def or(pipes: Pipe[E, _]*): GremlinScalaPipeline[S, E] = addPipe2(new OrFilterPipe[E](pipes: _*))

  def random(bias: Double): GremlinScalaPipeline[S, E] = addPipe2(new RandomFilterPipe[E](bias))

  /** only emit a given range of elements */
  override def range(low: Int, high: Int): GremlinScalaPipeline[S, E] = addPipe2(new RangeFilterPipe[E](low, high))

  /** simplifies the path by removing cycles */
  override def simplePath: GremlinScalaPipeline[S, E] = addPipe2(new CyclicPathFilterPipe[E])

  /** Adds input into buffer greedily - it will exhaust all the items that come to it from previous steps before emitting the next element.
   *  Note that this is a side effect step: the input will just flow through to the next step, but you can use `cap` to get the buffer into the pipeline.
   *  @see example in SideEffectTest
   */
  def aggregate(buffer: mutable.Buffer[E]): GremlinScalaPipeline[S, E] = addPipe2(new AggregatePipe[E](buffer))

  /** Like aggregate, but applies `fun` to each element prior to adding it to the Buffer */
  def aggregate[F](buffer: mutable.Buffer[F])(fun: E ⇒ F): GremlinScalaPipeline[S, E] =
    addPipe2(new AggregatePipe[E](buffer, new ScalaPipeFunction(fun)))

  /** Emits input, but adds input to collection. This is a lazy step, i.e. it adds it to the buffer as the elements are being traversed.  */
  def store(buffer: mutable.Buffer[E]): GremlinScalaPipeline[S, E] = addPipe2(new StorePipe[E](buffer))

  /** Like store , but applies `fun` to each element prior to adding it to the Buffer */
  def store[F](buffer: mutable.Buffer[F], fun: E ⇒ F): GremlinScalaPipeline[S, E] =
    addPipe2(new StorePipe[E](buffer, new ScalaPipeFunction(fun)))

  override def optional(namedStep: String): GremlinScalaPipeline[S, _] =
    addPipe2(new OptionalPipe(new Pipeline(FluentUtility.removePreviousPipes(this, namedStep))))

  /** Groups input by given keyFunction greedily - it will exhaust all the items that come to it from previous steps before emitting the next element.
   *  Note that this is a side effect step: the input will just flow through to the next step, but you can use `cap` to get the buffer into the pipeline.
   *  @see example in SideEffectTest
   */
  def groupBy[K, V](map: JMap[K, JCollection[Any]] = new JHashMap)(keyFunction: E ⇒ K, valueFunction: E ⇒ V): GremlinScalaPipeline[S, E] =
    addPipe2(
      new GroupByPipe(
        map,
        new ScalaPipeFunction(keyFunction),
        new ScalaPipeFunction(valueFunction).asInstanceOf[ScalaPipeFunction[E, Any]])
    )

  override def groupCount(map: JMap[_, Number], keyFunction: PipeFunction[_, _]): GremlinScalaPipeline[S, E] = super.groupCount(map, keyFunction)
  override def groupCount(keyFunction: PipeFunction[_, _]): GremlinScalaPipeline[S, E] = super.groupCount(keyFunction)
  override def groupCount(map: JMap[_, Number]): GremlinScalaPipeline[S, E] = super.groupCount(map)
  override def groupCount: GremlinScalaPipeline[S, E] = super.groupCount()

  def sideEffect[F](sideEffectFunction: E ⇒ F): GremlinScalaPipeline[S, F] = {
    val sideEffectPipe = new ScalaPipeFunction(sideEffectFunction)
    addPipe2(new SideEffectFunctionPipe(FluentUtility.prepareFunction(this.asMap, sideEffectPipe))).asInstanceOf[GremlinScalaPipeline[S, F]]
  }

  override def table(table: Table, stepNames: JCollection[String], columnFunctions: PipeFunction[_, _]*): GremlinScalaPipeline[S, E] =
    super.table(table, stepNames, columnFunctions: _*)

  override def table(table: Table, columnFunctions: PipeFunction[_, _]*): GremlinScalaPipeline[S, E] = super.table(table, columnFunctions: _*)
  override def table(columnFunctions: PipeFunction[_, _]*): GremlinScalaPipeline[S, E] = super.table(columnFunctions: _*)
  override def table(table: Table): GremlinScalaPipeline[S, E] = super.table(table)
  override def table: GremlinScalaPipeline[S, E] = super.table()

  override def tree(tree: Tree[_], branchFunctions: PipeFunction[_, _]*): GremlinScalaPipeline[S, E] = super.tree(tree, branchFunctions: _*)
  override def tree(branchFunctions: PipeFunction[_, _]*): GremlinScalaPipeline[S, E] = super.tree(branchFunctions: _*)

  /** Add a OrderMapPipe to the end of the Pipeline
   *  Given a Map as an input, the map is first ordered and then the keys are emitted in the order.
   *
   *  @param order if the values implement Comparable, then a increment or decrement sort is usable
   *  @return the extended Pipeline
   */
  override def orderMap(by: Tokens.T): GremlinScalaPipeline[S, _] = super.orderMap(by)

  /** Add a OrderMapPipe to the end of the Pipeline
   *  Given a Map as an input, the map is first ordered and then the keys are emitted in the order.
   *
   *  @param order if the values implement Comparable, then a increment or decrement sort is usable
   *  @return the extended Pipeline
   */
  override def orderMap(by: TransformPipe.Order): GremlinScalaPipeline[S, _] = super.orderMap(by)

  /** Add a OrderMapPipe to the end of the Pipeline
   *  Given a Map as an input, the map is first ordered and then the keys are emitted in the order.
   *
   *  @param compareFunction a function to compare to map entries
   *  @return the extended Pipeline
   */
  def orderMap(by: TinkerPair[JMap.Entry[_, _], JMap.Entry[_, _]] ⇒ Integer): GremlinScalaPipeline[S, _] =
    super.orderMap(new ScalaPipeFunction(by))

  /** Add a LinkPipe to the end of the Pipeline.
   *  Emit the incoming vertex, but have other vertex provide an outgoing edge to incoming vertex.
   *
   *  @param label     the edge label
   *  @param namedStep the step name that has the other vertex to link to
   *  @return the extended Pipeline
   */
  override def linkOut(label: String, namedStep: String): GremlinScalaPipeline[S, Vertex] =
    super.linkOut(label, namedStep)

  /** Add a LinkPipe to the end of the Pipeline.
   *  Emit the incoming vertex, but have other vertex provide an incoming edge to incoming vertex.
   *
   *  @param label     the edge label
   *  @param namedStep the step name that has the other vertex to link to
   *  @return the extended Pipeline
   */
  override def linkIn(label: String, namedStep: String): GremlinScalaPipeline[S, Vertex] =
    super.linkIn(label, namedStep)

  /** Add a LinkPipe to the end of the Pipeline.
   *  Emit the incoming vertex, but have other vertex provide an incoming and outgoing edge to incoming vertex.
   *
   *  @param label     the edge label
   *  @param namedStep the step name that has the other vertex to link to
   *  @return the extended Pipeline
   */
  override def linkBoth(label: String, namedStep: String): GremlinScalaPipeline[S, Vertex] =
    super.linkBoth(label, namedStep)

  /** Add a LinkPipe to the end of the Pipeline.
   *  Emit the incoming vertex, but have other vertex provide an outgoing edge to incoming vertex.
   *
   *  @param label the edge label
   *  @param other the other vertex
   *  @return the extended Pipeline
   */
  override def linkOut(label: String, other: Vertex): GremlinScalaPipeline[S, Vertex] =
    super.linkOut(label, other)

  /** Add a LinkPipe to the end of the Pipeline.
   *  Emit the incoming vertex, but have other vertex provide an incoming edge to incoming vertex.
   *
   *  @param label the edge label
   *  @param other the other vertex
   *  @return the extended Pipeline
   */
  override def linkIn(label: String, other: Vertex): GremlinScalaPipeline[S, Vertex] =
    super.linkIn(label, other)

  /** Add a LinkPipe to the end of the Pipeline.
   *  Emit the incoming vertex, but have other vertex provide an incoming and outgoing edge to incoming vertex.
   *
   *  @param label the edge label
   *  @param other the other vertex
   *  @return the extended Pipeline
   */
  override def linkBoth(label: String, other: Vertex): GremlinScalaPipeline[S, Vertex] =
    super.linkBoth(label, other)

  ///////////////////////
  /// TRANSFORM PIPES ///
  ///////////////////////
  override def memoize(namedStep: String): GremlinScalaPipeline[S, E] = super.memoize(namedStep)
  override def memoize(namedStep: String, map: JMap[_, _]): GremlinScalaPipeline[S, E] = super.memoize(namedStep, map)

  override def path(pathFunctions: PipeFunction[_, _]*): GremlinScalaPipeline[S, JList[_]] = super.path(pathFunctions: _*)

  override def select: GremlinScalaPipeline[S, Row[_]] = super.select()
  override def select(stepFunctions: PipeFunction[_, _]*): GremlinScalaPipeline[S, Row[_]] = super.select(stepFunctions: _*)
  override def select(stepNames: JCollection[String], stepFunctions: PipeFunction[_, _]*): GremlinScalaPipeline[S, Row[_]] =
    super.select(stepNames, stepFunctions: _*)

  /** Add a ShufflePipe to the end of the Pipeline.
   *  All the objects previous to this step are aggregated in a greedy fashion, their order randomized and emitted
   *  as a List.
   */
  def shuffle[_]: GremlinScalaPipeline[S, List[E]] = addPipe2(new ShufflePipe)

  /** All the objects previous to this step are aggregated in a greedy fashion and emitted as a List.
   *  Normally they would be traversed over lazily.
   *  Gather/Scatter is good for breadth-first traversals where the gather closure filters out unwanted elements at the current radius.
   *  @see https://github.com/tinkerpop/gremlin/wiki/Depth-First-vs.-Breadth-First
   *
   *  Note: Gremlin-Groovy comes with an overloaded gather pipe that takes a function to
   *  transform the last step. You can achieve the same by just appending a map step.
   */
  def gather[_]: GremlinScalaPipeline[S, List[E]] = addPipe2(new GatherPipe[E]) map (_.toList)

  /** This will unroll any iterator/iterable/map that is provided to it.
   *  Gather/Scatter is good for breadth-first traversals where the gather closure filters out unwanted elements at the current radius.
   *  @see https://github.com/tinkerpop/gremlin/wiki/Depth-First-vs.-Breadth-First
   *
   *  Note: only for one level - it will not unroll an iterator within an iterator.
   */
  override def scatter: GremlinScalaPipeline[S, _] = {
    import com.tinkerpop.gremlin.scala.pipes.ScatterPipe
    addPipe2(new ScatterPipe)
  }

  /** emits the side-effect of the previous pipe (e.g. groupBy) - and not the values that flow through it.
   *  If you use it, this normally is the last step. @see examples in SideEffectTest
   *  //TODO: reimplement, use proper types
   */
  override def cap: GremlinScalaPipeline[S, _] = {
    val sideEffectPipe = FluentUtility.removePreviousPipes(this, 1).get(0).asInstanceOf[SideEffectPipe[S, _]]
    addPipe2(new SideEffectCapPipe(sideEffectPipe))
  }

  def map[F](function: E ⇒ F): GremlinScalaPipeline[S, F] = super.transform(new ScalaPipeFunction(function))
  def transform[F](function: E ⇒ F): GremlinScalaPipeline[S, F] = map(function)

  override def order: GremlinScalaPipeline[S, E] = addPipe2(new OrderPipe)
  override def order(by: Order = Order.INCR): GremlinScalaPipeline[S, E] = addPipe2(new OrderPipe(by))

  def order(compare: (E, E) ⇒ Int): GremlinScalaPipeline[S, E] = {
    val compareFun: PipeFunction[TPair[E, E], Integer] =
      new ScalaPipeFunction({ pair: TPair[E, E] ⇒ compare(pair.getA, pair.getB) })
    addPipe2(new OrderPipe(compareFun))
  }

  //////////////////////
  /// UTILITY PIPES ///
  //////////////////////

  override def as(name: String): GremlinScalaPipeline[S, E] = {
    val pipeline = addPipe2(new AsPipe(name, FluentUtility.removePreviousPipes(this, 1).get(0)))
    this.asMap.refresh()
    pipeline.asInstanceOf[GremlinScalaPipeline[S, E]]
  }
  override def start(startObject: S): GremlinScalaPipeline[S, S] = {
    addPipe2(new StartPipe[S](startObject))
    FluentUtility.setStarts(this, startObject)
    this.asInstanceOf[GremlinScalaPipeline[S, S]]
  }

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

  //TODO: remove once we don't extend GremlinPipeline any more
  implicit private def scalaPipeline[A, B](pipeline: GremlinPipeline[A, B]): GremlinScalaPipeline[A, B] =
    pipeline.asInstanceOf[GremlinScalaPipeline[A, B]]

  implicit def boolean2BooleanFn(fn: E ⇒ Boolean)(e: E): JBoolean = fn(e)
  def selectDynamic[F](field: String): GremlinScalaPipeline[S, F] = property(field)

}
