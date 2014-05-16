package com.tinkerpop.gremlin.scala

import com.tinkerpop.blueprints._
import java.util.{ Map ⇒ JMap, HashMap ⇒ JHashMap, Collection ⇒ JCollection }
import java.lang.{ Boolean ⇒ JBoolean, Iterable ⇒ JIterable }
import com.tinkerpop.gremlin.Tokens
import com.tinkerpop.pipes._
import com.tinkerpop.pipes.branch.LoopPipe.LoopBundle
import com.tinkerpop.pipes.util.structures.{ Tree, Table, Row, Pair ⇒ TPair }
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
import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.reflect.{ classTag, ClassTag }

class GremlinScalaPipeline[S, E] extends Pipeline[S, E] with Dynamic {
  def id: GremlinScalaPipeline[S, Object] = addPipe(new IdPipe)

  def label: GremlinScalaPipeline[S, String] = addPipe(new LabelPipe)

  def out: GremlinScalaPipeline[S, Vertex] = out()
  def out(labels: String*): GremlinScalaPipeline[S, Vertex] = out(branchFactor = Int.MaxValue, labels: _*)
  def out(branchFactor: Int, labels: String*): GremlinScalaPipeline[S, Vertex] =
    addVertexPipe(branchFactor, Direction.OUT, labels: _*)

  def outE: GremlinScalaPipeline[S, Edge] = outE()
  def outE(labels: String*): GremlinScalaPipeline[S, Edge] = outE(branchFactor = Int.MaxValue, labels: _*)
  def outE(branchFactor: Int, labels: String*): GremlinScalaPipeline[S, Edge] =
    addVertexPipe(branchFactor, Direction.OUT, labels: _*)

  def outV: GremlinScalaPipeline[S, Vertex] = addPipe(new OutVertexPipe)

  def in: GremlinScalaPipeline[S, Vertex] = in()
  def in(labels: String*): GremlinScalaPipeline[S, Vertex] = in(branchFactor = Int.MaxValue, labels: _*)
  def in(branchFactor: Int, labels: String*): GremlinScalaPipeline[S, Vertex] =
    addVertexPipe(branchFactor, Direction.IN, labels: _*)

  def inE: GremlinScalaPipeline[S, Edge] = inE()
  def inE(labels: String*): GremlinScalaPipeline[S, Edge] = inE(branchFactor = Int.MaxValue, labels: _*)
  def inE(branchFactor: Int, labels: String*): GremlinScalaPipeline[S, Edge] =
    addVertexPipe(branchFactor, Direction.IN, labels: _*)

  def inV: GremlinScalaPipeline[S, Vertex] = addPipe(new InVertexPipe)

  def addVertexPipe[A <: Element: ClassTag](branchFactor: Int, direction: Direction, labels: String*): GremlinScalaPipeline[S, A] = {
    val clazz = classTag[A].runtimeClass.asInstanceOf[Class[A]]
    addPipe(new VertexQueryPipe(clazz, direction, null, null, branchFactor, 0, Integer.MAX_VALUE, labels: _*))
  }

  def V(graph: Graph): GremlinScalaPipeline[Vertex, Vertex] =
    manualStart(graph.getVertices)

  def E(graph: Graph): GremlinScalaPipeline[Edge, Edge] =
    manualStart(graph.getEdges)

  /** Check if the element has a property with provided key */
  def has(key: String): GremlinScalaPipeline[S, E] =
    has(key, Tokens.T.neq, null)

  /** Check if the element has a property with provided key/value */
  def has(key: String, value: Any): GremlinScalaPipeline[S, E] =
    has(key, Tokens.T.eq, value)

  /** Check if the element does not have a property with provided key. */
  def hasNot(key: String): GremlinScalaPipeline[S, E] =
    has(key, Tokens.T.eq, null)

  /** Check if the element does not have a property with provided key/value */
  def hasNot(key: String, value: Any): GremlinScalaPipeline[S, E] =
    has(key, Tokens.T.neq, value)

  /** Check if the element has a property with provided key/value that matches the given comparison token */
  def has(key: String, comparison: Tokens.T, value: Any): GremlinScalaPipeline[S, E] =
    has(key, Tokens.mapPredicate(comparison), value)

  /** Check if the element has a property with provided key/value that matches the given predicate */
  def has(key: String, predicate: Predicate, value: Any): GremlinScalaPipeline[S, E] = {
    val pipeline = key match {
      case Tokens.ID    ⇒ addPipe(new IdFilterPipe(predicate, value))
      case Tokens.LABEL ⇒ addPipe(new LabelFilterPipe(predicate, value))
      case _            ⇒ addPipe[Nothing](new PropertyFilterPipe(key, predicate, value))
    }
    pipeline.asInstanceOf[GremlinScalaPipeline[S, E]]
  }

  /** checks if a given property is in an interval (startValue: inclusive, endValue: exclusive)  */
  def interval(key: String, startValue: Comparable[_], endValue: Comparable[_]): GremlinScalaPipeline[S, Element] =
    addPipe(new IntervalFilterPipe[Element](key, startValue, endValue))

  def both(labels: String*): GremlinScalaPipeline[S, Vertex] = both(Int.MaxValue, labels: _*)
  def both(branchFactor: Int, labels: String*): GremlinScalaPipeline[S, Vertex] =
    addPipe(new VertexQueryPipe(classOf[Vertex], Direction.BOTH, null, null, branchFactor, 0, Integer.MAX_VALUE, labels: _*))

  def bothE(labels: String*): GremlinScalaPipeline[S, Edge] = bothE(Int.MaxValue, labels: _*)
  def bothE(branchFactor: Int, labels: String*): GremlinScalaPipeline[S, Edge] =
    addPipe(new VertexQueryPipe(classOf[Edge], Direction.BOTH, null, null, branchFactor, 0, Integer.MAX_VALUE, labels: _*))

  def bothV: GremlinScalaPipeline[S, Vertex] = addPipe(new BothVerticesPipe)

  def propertyMap(keys: String*): GremlinScalaPipeline[S, Map[String, Any]] = addPipe(new PropertyMapPipe(keys: _*))
  def propertyMap: GremlinScalaPipeline[S, Map[String, Any]] = propertyMap()

  def property[F](key: String): GremlinScalaPipeline[S, F] = addPipe(new PropertyPipe(key, false))

  /** Gets the objects on each step on the path through the pipeline as lists. Example:
   *  graph.v(1).out.path
   *  ==>[v[1], v[2]]
   *  ==>[v[1], v[4]]
   *  ==>[v[1], v[3]]
   */
  def path: GremlinScalaPipeline[S, Seq[_]] = addPipe(new PathPipe[Any]).map(_.toSeq)

  /** Gets the objects on each named step on the path through the pipeline as rows. */
  def select: GremlinScalaPipeline[S, Row[_]] =
    addPipe(new SelectPipe(null, FluentUtility.getAsPipes(this)))

  /** Gets the objects for given named steps on the path through the pipeline as rows. */
  def select(steps: String*): GremlinScalaPipeline[S, Row[_]] =
    addPipe(new SelectPipe(steps, FluentUtility.getAsPipes(this)))

  ////////////////////
  /// BRANCH PIPES ///
  ////////////////////
  /** Copies incoming object to internal pipes. */
  def copySplit(pipes: Pipe[E, _]*): GremlinScalaPipeline[S, _] = addPipe(new CopySplitPipe(pipes))

  def exhaustMerge: GremlinScalaPipeline[S, _] =
    addPipe(new ExhaustMergePipe(FluentUtility.getPreviousPipe(this).asInstanceOf[MetaPipe].getPipes))

  /** Used in combination with a copySplit, merging the parallel traversals in a round-robin fashion. */
  def fairMerge: GremlinScalaPipeline[S, _] =
    addPipe(new FairMergePipe(FluentUtility.getPreviousPipe(this).asInstanceOf[MetaPipe].getPipes))

  def ifThenElse(ifFunction: E ⇒ Boolean, thenFunction: E ⇒ _, elseFunction: E ⇒ _): GremlinScalaPipeline[S, _] =
    addPipe(new IfThenElsePipe(
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
    addPipe(new LoopPipe(new Pipeline(FluentUtility.removePreviousPipes(this, namedStep)), whileFunction))

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
    addPipe(new LoopPipe(new Pipeline(FluentUtility.removePreviousPipes(this, namedStep)), whileFun, emit))

  ////////////////////
  /// FILTER PIPES ///
  ////////////////////
  /** Takes a collection of pipes and emits incoming objects that are true for all of the pipes. */
  def and(pipes: Pipe[E, _]*): GremlinScalaPipeline[S, E] = addPipe(new AndFilterPipe[E](pipes: _*))

  def back(namedStep: String): GremlinScalaPipeline[S, Any] =
    addPipe(new BackFilterPipe(new Pipeline(FluentUtility.removePreviousPipes(this, namedStep))))

  def back(numberStep: Int): GremlinScalaPipeline[S, Any] = addPipe(new BackFilterPipe(new Pipeline(FluentUtility.removePreviousPipes(this, numberStep))))

  def dedup: GremlinScalaPipeline[S, E] = addPipe(new DuplicateFilterPipe[E])

  /** only emits the object if the object generated by its function hasn't been seen before. */
  def dedup(dedupFunction: E ⇒ _): GremlinScalaPipeline[S, E] = addPipe(new DuplicateFilterPipe[E](dedupFunction))

  /** emits everything except what is in the supplied collection. */
  def except(iterable: Iterable[E]): GremlinScalaPipeline[S, E] = addPipe(new ExceptFilterPipe[E](iterable))

  /** emits everything except what is in the results of a named step.
   *
   *  not currently supported because ExceptFilterPipe uses ElementHelper.areEqual to compare two elements, which compares if the classes are equal.
   *  I'll open a pull request to fix that in blueprints shortly...
   */
  def except(namedSteps: String*): GremlinScalaPipeline[S, E] = throw new NotImplementedError("not currently supported")

  /** retains everything that is in the supplied collection. */
  def retain(iterable: Iterable[E]): GremlinScalaPipeline[S, E] = addPipe(new RetainFilterPipe[E](iterable))

  /** retains everything that is in the results of a named step.
   *
   *  not currently supported because RetainFilterPipe uses ElementHelper.areEqual to compare two elements, which compares if the classes are equal.
   *  I'll open a pull request to fix that in blueprints shortly...
   */
  def retain(namedSteps: String*): GremlinScalaPipeline[S, E] = throw new NotImplementedError("not currently supported")

  def filter(f: E ⇒ Boolean): GremlinScalaPipeline[S, E] = addPipe(new FilterFunctionPipe[E](f))
  def filterNot(f: E ⇒ Boolean): GremlinScalaPipeline[S, E] = addPipe(new FilterFunctionPipe[E]({ e: E ⇒ !f(e) }))

  def or(pipes: Pipe[E, _]*): GremlinScalaPipeline[S, E] = addPipe(new OrFilterPipe[E](pipes: _*))

  def random(bias: Double): GremlinScalaPipeline[S, E] = addPipe(new RandomFilterPipe[E](bias))

  /** only emit a given range of elements */
  def range(low: Int, high: Int): GremlinScalaPipeline[S, E] = addPipe(new RangeFilterPipe[E](low, high))

  /** simplifies the path by removing cycles */
  def simplePath: GremlinScalaPipeline[S, E] = addPipe(new CyclicPathFilterPipe[E])

  /** Adds input into buffer greedily - it will exhaust all the items that come to it from previous steps before emitting the next element.
   *  Note that this is a side effect step: the input will just flow through to the next step, but you can use `cap` to get the buffer into the pipeline.
   *  @see example in SideEffectTest
   */
  def aggregate(buffer: mutable.Buffer[E]): GremlinScalaPipeline[S, E] = addPipe(new AggregatePipe[E](buffer))

  /** Like aggregate, but applies `fun` to each element prior to adding it to the Buffer */
  def aggregate[F](buffer: mutable.Buffer[F])(fun: E ⇒ F): GremlinScalaPipeline[S, E] =
    addPipe(new AggregatePipe[E](buffer, fun))

  /** Emits input, but adds input to collection. This is a lazy step, i.e. it adds it to the buffer as the elements are being traversed.  */
  def store(buffer: mutable.Buffer[E]): GremlinScalaPipeline[S, E] = addPipe(new StorePipe[E](buffer))

  /** Like store , but applies `fun` to each element prior to adding it to the Buffer */
  def store[F](buffer: mutable.Buffer[F], fun: E ⇒ F): GremlinScalaPipeline[S, E] =
    addPipe(new StorePipe[E](buffer, fun))

  /** Behaves similar to `back` except that it does not filter. It will go down a particular path and back up to where it left off.
   *  As such, its useful for yielding a side-effect down a particular branch.
   */
  def optional(namedStep: String): GremlinScalaPipeline[S, _] =
    addPipe(new OptionalPipe(new Pipeline(FluentUtility.removePreviousPipes(this, namedStep))))

  /** Groups input by given keyFunction greedily - it will exhaust all the items that come to it from previous steps before emitting the next element.
   *  Note that this is a side effect step: the input will just flow through to the next step, but you can use `cap` to get the buffer into the pipeline.
   *  @see example in SideEffectTest
   */
  def groupBy[K, V](map: JMap[K, JCollection[V]] = new JHashMap[K, JCollection[V]])(
    keyFunction: E ⇒ K, valueFunction: E ⇒ V): GremlinScalaPipeline[S, E] =
    addPipe(
      new GroupByPipe(
        map.asInstanceOf[JMap[K, JCollection[Any]]],
        keyFunction,
        new ScalaPipeFunction(valueFunction).asInstanceOf[ScalaPipeFunction[E, Any]])
    )

  /** counts each traversed object and stores it in a map */
  def groupCount: GremlinScalaPipeline[S, E] = addPipe(new GroupCountPipe)

  /** Emits input, but calls a side effect closure on each input. */
  def sideEffect[F](sideEffectFunction: E ⇒ F): GremlinScalaPipeline[S, E] =
    addPipe(new SideEffectFunctionPipe(FluentUtility.prepareFunction(asMap, sideEffectFunction))).asInstanceOf[GremlinScalaPipeline[S, E]]

  /** Emit input, but stores the tree formed by the traversal as a map. */
  def tree: GremlinScalaPipeline[S, E] = addPipe(new TreePipe)

  ///////////////////////
  /// TRANSFORM PIPES ///
  ///////////////////////

  /** Emit the incoming vertex, but have other vertex provide an incoming and outgoing edge to incoming vertex. */
  def linkBoth(label: String, other: Vertex): GremlinScalaPipeline[S, Vertex] = 
    addPipe(new LinkPipe(Direction.BOTH, label, other))

  /** Emit the incoming vertex, but have other vertex provide an incoming and outgoing edge to incoming vertex. */
  def linkOut(label: String, other: Vertex): GremlinScalaPipeline[S, Vertex] = 
    addPipe(new LinkPipe(Direction.OUT, label, other))

  /** Emit the incoming vertex, but have other vertex provide an incoming and outgoing edge to incoming vertex. */
  def linkIn(label: String, other: Vertex): GremlinScalaPipeline[S, Vertex] = 
    addPipe(new LinkPipe(Direction.IN, label, other))

  /** Remembers a particular mapping from input to output. Long or expensive expressions with no side effects can use this step
   *  to remember a mapping, which helps reduce load when previously processed objects are passed into it.
   *  For situations where memoization may consume large amounts of RAM, consider using an embedded key-value store like JDBM or
   *  some other persistent Map implementation.
   */
  def memoize(namedStep: String): GremlinScalaPipeline[S, E] =
    addPipe(new MemoizePipe(new Pipeline(FluentUtility.removePreviousPipes(this, namedStep))))

  /** Add a ShufflePipe to the end of the Pipeline.
   *  All the objects previous to this step are aggregated in a greedy fashion, their order randomized and emitted
   *  as a List.
   */
  def shuffle: GremlinScalaPipeline[S, List[E]] = addPipe(new ShufflePipe)

  /** All the objects previous to this step are aggregated in a greedy fashion and emitted as a List.
   *  Normally they would be traversed over lazily.
   *  Gather/Scatter is good for breadth-first traversals where the gather closure filters out unwanted elements at the current radius.
   *  @see https://github.com/tinkerpop/gremlin/wiki/Depth-First-vs.-Breadth-First
   *
   *  Note: Gremlin-Groovy comes with an overloaded gather pipe that takes a function to
   *  transform the last step. You can achieve the same by just appending a map step.
   */
  def gather: GremlinScalaPipeline[S, List[E]] = addPipe(new GatherPipe[E]) map (_.toList)

  /** This will unroll any iterator/iterable/map that is provided to it.
   *  Gather/Scatter is good for breadth-first traversals where the gather closure filters out unwanted elements at the current radius.
   *  @see https://github.com/tinkerpop/gremlin/wiki/Depth-First-vs.-Breadth-First
   *
   *  Note: only for one level - it will not unroll an iterator within an iterator.
   */
  def scatter: GremlinScalaPipeline[S, _] = {
    import com.tinkerpop.gremlin.scala.pipes.ScatterPipe
    addPipe(new ScatterPipe)
  }

  /** emits the side-effect of the previous pipe (e.g. groupBy) - and not the values that flow through it.
   *  If you use it, this normally is the last step. @see examples in SideEffectTest
   */
  def cap: GremlinScalaPipeline[S, _] = {
    val sideEffectPipe = FluentUtility.removePreviousPipes(this, 1).get(0).asInstanceOf[SideEffectPipe[S, _]]
    addPipe(new SideEffectCapPipe(sideEffectPipe))
  }

  /** map objects over a given function
   *  aliases: transform (standard gremlin) and ∘ (category theory)
   */
  def map[F](function: E ⇒ F): GremlinScalaPipeline[S, F] = {
    val pipeFunction = new ScalaPipeFunction(function)
    addPipe(new TransformFunctionPipe(pipeFunction))
  }
  def ∘[F](function: E ⇒ F): GremlinScalaPipeline[S, F] = map(function)
  def transform[F](function: E ⇒ F): GremlinScalaPipeline[S, F] = map(function)

  def order: GremlinScalaPipeline[S, E] = addPipe(new OrderPipe)
  def order(by: Order = Order.INCR): GremlinScalaPipeline[S, E] = addPipe(new OrderPipe(by))

  def order(compare: (E, E) ⇒ Int): GremlinScalaPipeline[S, E] = {
    val compareFun: PipeFunction[TPair[E, E], Integer] =
      new ScalaPipeFunction({ pair: TPair[E, E] ⇒ compare(pair.getA, pair.getB) })
    addPipe(new OrderPipe(compareFun))
  }

  ////////////////////////
  /// utility methods ///
  ///////////////////////

  def as(name: String): GremlinScalaPipeline[S, E] = {
    val pipeline = addPipe(new AsPipe(name, FluentUtility.removePreviousPipes(this, 1).get(0)))
    asMap.refresh()
    pipeline.asInstanceOf[GremlinScalaPipeline[S, E]]
  }

  def start(startObject: S): GremlinScalaPipeline[S, S] = {
    addPipe(new StartPipe[S](startObject))
    FluentUtility.setStarts(this, startObject)
    this.asInstanceOf[GremlinScalaPipeline[S, S]]
  }

  /** run through pipeline and get results as List */
  def toList[_](): List[E] = iterableAsScalaIterable(this).toList

  /** run through pipeline and get results as Set */
  def toSet[_](): Set[E] = iterableAsScalaIterable(this).toSet

  /** run through pipeline and get results as Stream */
  def toStream(): Stream[E] = iterableAsScalaIterable(this).toStream

  /** Completely drain the pipeline of its objects - useful when a sideEffect of the pipeline is desired */
  override def iterate() = PipeHelper.iterate(this)

  def addPipe[T](pipe: Pipe[_ <: Any, T]): GremlinScalaPipeline[S, T] = {
    super.addPipe(pipe)
    this.asInstanceOf[GremlinScalaPipeline[S, T]]
  }

  private def manualStart[T](start: JIterable[T]): GremlinScalaPipeline[T, T] = {
    val pipe = addPipe(new StartPipe[S](start))
    FluentUtility.setStarts(this, start)
    pipe.asInstanceOf[GremlinScalaPipeline[T, T]]
  }

  implicit def boolean2BooleanFn(fn: E ⇒ Boolean)(e: E): JBoolean = fn(e)
  def selectDynamic[F](field: String): GremlinScalaPipeline[S, F] = property(field)

  protected def asMap = new AsMap(this)
}
