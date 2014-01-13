package com.tinkerpop.gremlin.scala

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import scala.collection.JavaConversions._

import shapeless._
import ops.hlist._
//import syntax.std.traversable._

class TypedPipelineSpec extends FunSpec with ShouldMatchers {

  it("using a simple wrapper that maintains the right order of the types by appending") {
    import com.tinkerpop.blueprints.Edge
    import com.tinkerpop.blueprints.Vertex
    import com.tinkerpop.pipes.Pipe
    import com.tinkerpop.pipes.AbstractPipe
    import com.tinkerpop.pipes.transform.TransformPipe
    import com.tinkerpop.gremlin.java.GremlinPipeline
    import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory
    import java.util.{List ⇒ JList, Iterator ⇒ JIterator}
    import shapeless.test.illTyped

    case class GremlinScala[End, Types <: HList](gremlin: GremlinPipeline[_, End]) {
      def toList(): List[End] = gremlin.toList.toList
      def as(name: String) = GremlinScala[End, Types](gremlin.as(name))
      def back[E](to: String)(implicit p:Prepend[Types, E::HNil]) = 
        GremlinScala[E, p.Out](gremlin.back(to).asInstanceOf[GremlinPipeline[_,E]])

      def path(implicit p:Prepend[Types, Types::HNil]): GremlinScala[Types, p.Out] =
        addPipe(new PathPipe[End, Types])

      def addPipe[E](pipe: Pipe[End, E])(implicit p:Prepend[Types, E::HNil]) = 
        GremlinScala[E, p.Out](gremlin.add(pipe))
    }

    implicit class GremlinEdgeSteps[End <: Edge, Types <: HList](gremlinScala: GremlinScala[End,Types])
      extends GremlinScala[End, Types](gremlinScala.gremlin) {

      def inV(implicit p:Prepend[Types, Vertex::HNil]) = GremlinScala[Vertex, p.Out](gremlin.inV)
    }

    implicit class GremlinVertexSteps[End <: Vertex, Types <: HList](gremlinScala: GremlinScala[End,Types])
      extends GremlinScala[End, Types](gremlinScala.gremlin) {

      def outE(implicit p:Prepend[Types, Edge::HNil]) = GremlinScala[Edge, p.Out](gremlin.outE())
    }

    class PathPipe[S, E <: HList] extends AbstractPipe[S, E] with TransformPipe[S, E] {
      override def setStarts(starts: JIterator[S]): Unit = {
        super.setStarts(starts)
        this.enablePath(true)
      }

      override def processNextStart(): E = starts match {
        case starts: Pipe[_,_] ⇒ 
          starts.next()
          val path: JList[_] = starts.getCurrentPath
          toHList[E](path.toList)
      }

      def toHList[T <: HList](path: List[_]): T = 
        if(path.length == 0)
          HNil.asInstanceOf[T]
        else
          (path.head :: toHList[IsHCons[T]#T](path.tail)).asInstanceOf[T]
    }

    val graph = TinkerGraphFactory.createTinkerGraph
    def vertexPipeline = new GremlinPipeline[Unit, Vertex](graph.v(1))
    def vertexGremlin = GremlinScala[Vertex, Vertex :: HNil](vertexPipeline)

    print(vertexGremlin.outE)
    print(vertexGremlin.outE.inV)
    print(vertexGremlin.as("x").outE.back[Vertex]("x"))

    vertexGremlin.path.toList foreach { l: Vertex :: HNil ⇒ println(l) }
    vertexGremlin.outE.path.toList foreach { l: Vertex :: Edge :: HNil ⇒ println(l) }

    // verify that these do not compile
    illTyped {""" vertexGremlin.inV """}
    illTyped {""" vertexGremlin.outE.inV.inV """}
    illTyped {""" vertexGremlin.outE.back[Edge]("x").outE """}
    illTyped {""" edgeGremlin.outE """}
    illTyped {""" edgeGremlin.inV.outE.outE """}

    def print(gremlin: GremlinScala[_,_]): Unit = {
      println("----------results---------")
      gremlin.toList foreach println
    }
  }


  ignore("keeps the types in right order so we don't have to reverse them") {
    class Pipeline[End, Types <: HList] {

      def toList: List[End] = ???

      def intStep(implicit p:Prepend[Types, Int :: HNil]): Pipeline[Int, p.Out] = ???
      def stringStep(implicit p:Prepend[Types, String :: HNil]): Pipeline[String, p.Out] 
        = addStep[String]

      def path(implicit p:Prepend[Types, Types :: HNil]): Pipeline[Types, p.Out] = ???

      def addStep[E](implicit p:Prepend[Types, E :: HNil]): Pipeline[E, p.Out] = ???
    }

    val p = new Pipeline[String, String :: HNil]
    val p1: Pipeline[Int, String :: Int :: HNil] = p.intStep
    p1.path.toList.foreach {_: String :: Int :: HNil ⇒ }

    p.stringStep.intStep.path.toList.foreach { _: String :: String :: Int :: HNil ⇒ }
  }

  ignore("demonstrates the problem for shapeless-dev") {
    // type Head is the first entry of the HList L - one could aswell get that vis IsHCons[L], but this is simpler for now
    class Pipeline[Head, L <: HList](implicit val R:Reverse[L]) {

      def reifyR[L <: HList](implicit R:Reverse[L]): Reverse[L]{type Out = R.Out} = R
      type L1 = L

      //type LRev = R.Out
      type LRev
      //val head = implicitly[IsHCons[L]]
      //type Head = head.H
      val rev = reifyR[L]
      //type LRev = rev.Out


      // get a list of all Head elements
      def iterate: List[Head] = ???

      // the Head type is the reverse of L
      def allPreviousStepsAsListInReverseOrder: Pipeline[LRev, LRev :: L]{type LRev = rev.Out} = ???

      def allPreviousStepsAsList: Pipeline[L, L :: L] = ???
    }

    val p = new Pipeline[String, String :: Int :: HNil]
    def reifyR[L <: HList](implicit R:Reverse[L]): Reverse[L]{type Out = R.Out} = R
    val rev = reifyR[p.L1]
    //val rev = reifyR[String :: Int :: HNil]
    type LR2 = rev.Out
    type LR = Int :: String :: HNil
    implicitly[LR2 =:= LR]
    //implicitly[p.LRev =:= LR] //does not compile

    p.iterate foreach {_: String ⇒ }
    p.allPreviousStepsAsList.iterate foreach {_: String :: Int :: HNil ⇒ }
    //p.allPreviousStepsAsListInReverseOrder.iterate foreach {_: Int :: String :: HNil ⇒ }
    //p.allPreviousStepsAsListInReverseOrder.iterate foreach {_: p.LRev ⇒ }
  }

  ignore("is a simple wrapper around Pipeline that holds the types") {
    import com.tinkerpop.blueprints.Edge
    import com.tinkerpop.blueprints.Vertex
    import com.tinkerpop.pipes.Pipe
    import com.tinkerpop.pipes.AbstractPipe
    import com.tinkerpop.pipes.transform.TransformPipe
    import com.tinkerpop.gremlin.java.GremlinPipeline
    import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory
    import java.util.{List ⇒ JList, Iterator ⇒ JIterator}
    import shapeless.test.illTyped

    // H is the out type of the _last_ pipe, i.e. the result type of the whole pipeline
    case class GremlinScala[H, T <: HList]
      (gremlin: GremlinPipeline[_, H])
      (implicit val R:Reverse[H::T]) {
      type CurrentTypes = H :: T
      //val reifyR: Reverse[H::T]{type Out = R.Out} = R
      //type CurrentTypesReversed = Reverse[CurrentTypes]#Out
      //def reifyR[L <: HList](implicit R:Reverse[L]): Reverse[L]{type Out = R.Out} = R
      //def reifyR: Reverse[CurrentTypes]{type Out = R.Out} = R
      //val rev = reifyR
      //type CurrentTypesReversed = rev.Out
      //type CurrentTypesReversed = reifyR.Out
      type CurrentTypesReversed = R.Out

      def toList(): List[H] = gremlin.toList.toList
      //def as(name: String) = GremlinScala[H, T](gremlin.as(name))
      //def back[S](to: String) = GremlinScala[S, CurrentTypes](gremlin.back(to).asInstanceOf[GremlinPipeline[_,S]])

      //def path: GremlinScala[CurrentTypes, CurrentTypes] = addPipe(new PathPipe[H, CurrentTypes])
      def path(implicit R3: Reverse[CurrentTypesReversed::CurrentTypes]): GremlinScala[CurrentTypesReversed, CurrentTypes] = addPipe(new PathPipe[H, CurrentTypesReversed])
      //def pathBoring = GremlinScala[JList[_], CurrentTypes](gremlin.path())

      def addPipe[E](pipe: Pipe[H, E])(implicit R2:Reverse[E::CurrentTypes]) = GremlinScala[E, CurrentTypes](gremlin.add(pipe))
    }

    //implicit class GremlinEdgeSteps[H <: Edge, T <: HList](gremlinScala: GremlinScala[H, _])
      //extends GremlinScala[H, T](gremlinScala.gremlin) {

      //def inV = GremlinScala[Vertex, CurrentTypes](gremlin.inV)
    //}

    //implicit class GremlinVertexSteps[H <: Vertex, T <: HList](gremlinScala: GremlinScala[H, _])
      //extends GremlinScala[H, T](gremlinScala.gremlin) {

      //def outE = GremlinScala[Edge, CurrentTypes](gremlin.outE())
    //}

    class PathPipe[S, E <: HList] extends AbstractPipe[S, E] with TransformPipe[S, E] {
      override def setStarts(starts: JIterator[S]): Unit = {
        super.setStarts(starts)
        this.enablePath(true)
      }

      override def processNextStart(): E = starts match {
        case starts: Pipe[_,_] ⇒ 
          starts.next()
          val path: JList[_] = starts.getCurrentPath
          toHList[E](path.toList)
      }

      def toHList[T <: HList](path: List[_]): T = 
        if(path.length == 0)
          HNil.asInstanceOf[T]
        else
          (path.head :: toHList[IsHCons[T]#T](path.tail)).asInstanceOf[T]
    }

    val graph = TinkerGraphFactory.createTinkerGraph
    def vertexPipeline = new GremlinPipeline[Unit, Vertex](graph.v(1))
    def vertexGremlin = GremlinScala[Vertex, HNil](vertexPipeline)

    //print(vertexGremlin.outE)
    //print(vertexGremlin.outE.inV)
    //print(vertexGremlin.as("x").outE.back[Vertex]("x"))

    //vertexGremlin.path.toList foreach { l: Vertex :: HNil ⇒ println(l) }
    //vertexGremlin.outE.path.toList foreach { l: Edge :: Vertex :: _ ⇒ println(l) }
    //attention: the HList is reversed in it's types... I'm working on that... and then the next line should work
    //vertexGremlin.outE.path.toList foreach { l: Vertex :: Edge :: _ ⇒ println(l) }

    // these do not compile (and they shouldn't!)
    //illTyped {""" vertexGremlin.inV """}
    //illTyped {""" vertexGremlin.outE.inV.inV """}
    //illTyped {""" vertexGremlin.outE.back[Edge]("x").outE """}
    //illTyped {""" edgeGremlin.outE """}
    //illTyped {""" edgeGremlin.inV.outE.outE """}

    def print(gremlin: GremlinScala[_,_]): Unit = {
      println("----------results---------")
      gremlin.toList foreach println
      //println("--------------------------")
    }
  }

  ignore("fifth try: pipes as hlist") {
    trait Element { def id: Int }
    case class Vertex(id: Int, outE: Iterator[Edge]) extends Element
    case class Edge(id: Int, label: String, inV: Vertex) extends Element

    trait Pipe[-I,O] { def iter: Iterator[O] }
    case class InVPipe(starts: Iterator[Edge]) extends Pipe[Edge, Vertex] {
      val iter = starts map { _.inV }
    }
    case class OutEPipe(starts: Iterator[Vertex]) extends Pipe[Vertex, Edge] {
      val iter = starts flatMap { _.outE }
    }
    case class LabelPipe(starts: Iterator[Edge]) extends Pipe[Edge, String] {
      val iter = starts map { _.label }
    }
    case class IdPipe(starts: Iterator[Element]) extends Pipe[Element, Int] {
      val iter = starts map { _.id }
    }

    // H is the out type of the _last_ pipe, i.e. the result type of the whole pipeline!
    case class Pipeline[H, T <: HList](pipes: Pipe[_, H] :: T) {
      type CurrentPipes = Pipe[_, H] :: T
      def toList: List[H] = pipes.head.iter.toList
    }
    implicit class EdgeSteps[H <: Edge, T <: HList](pipeline: Pipeline[H, T]) extends Pipeline[H, T](pipeline.pipes) {
      def inV: Pipeline[Vertex, CurrentPipes] = addPipe(pipes, InVPipe)
      def label: Pipeline[String, CurrentPipes] = addPipe(pipes, LabelPipe)
    }
    implicit class VertexSteps[H <: Vertex, T <: HList](pipeline: Pipeline[H, T]) extends Pipeline[H, T](pipeline.pipes) {
      def outE: Pipeline[Edge, Pipe[_, H] :: T] = addPipe(pipes, OutEPipe)
    }

    def startPipeline[H,T](pipe: Pipe[H,T]) = Pipeline(pipe :: HNil)
    //H1: the old end type of the pipeline
    //H2: the new end type of the pipeline
    def addPipe[H2, H1, T <: HList](
      pipes: Pipe[_, H1] :: T,
      pipeConstr: Iterator[H1] ⇒ Pipe[H1, H2]): Pipeline[H2, Pipe[_, H1] :: T] = {
        val next = pipeConstr(pipes.head.iter)
        Pipeline(next :: pipes)
    }


    /* test setup:
     *     |e1-|
     * v1->|   |->v2
     *     |e2-|
     */
    def v(id: Int): Vertex = id match {
      case 1 ⇒ Vertex(id, outE = List(e(1), e(2)).iterator)
      case 2 ⇒ Vertex(id, outE = Nil.iterator)
    }
    def e(id: Int): Edge = id match {
      case 1|2 ⇒ Edge(id, label = s"label:$id", inV = v(2))
    }
    def edgeStartPipe = new Pipe[Nothing, Edge] {
      val iter = List(e(1)).iterator
    }
    def vertexStartPipe = new Pipe[Nothing, Vertex] {
      val iter = List(v(1)).iterator
    }
    def edgePipeline = startPipeline(edgeStartPipe)
    def vertexPipeline = startPipeline(vertexStartPipe)

    // these compile
    edgePipeline.inV
    vertexPipeline.outE       
    vertexPipeline.outE.inV
    vertexPipeline.outE.label 

    //println(vertexPipeline.toList)
    //println(vertexPipeline.outE.toList)
    //println(vertexPipeline.outE.inV.toList)
    //println(vertexPipeline.outE.inV.outE.toList)

    // these don't compile - and they shouldn't ;)
    //vertexPipeline.inV
    //edgePipeline.inV.inV    
    //vertexPipeline.outE.outE
    //vertexPipeline.label
  }

  ignore("forth try: Pipeline as HList") {
    //case class Pipeline[H, T <: HList](pipes :Pipe[_, H]::T) {
      //def path: H::T = {
        //val iter1 = pipes(0).testObjects. :: HNil
        //combine iterators to streams:
        //???
      //}

      // testObjects is only passed in to simplify things for now
      //def out[E](testObjects: List[E]) = Pipeline(Pipe(testObjects) :: pipes)
      //def path: HList[A] = List(1, "one").toHList[A].get//OrElse(HNil)
      //def toList = pipes.head
      //def getHead1[B <: HList :IsHCons](pipes: B) = pipes.head
      //T3 semantics: Pipline extends Pipe
    //}

    //val emptyPipe = Pipe(Nil)
    //val start = Pipeline(emptyPipe :: HNil)
    //val strings = List[String]("one", "two")
    //val stringPipe = Pipe(strings)
    //val ints = List[Int](1,2)
    //val intPipe = Pipe(ints)
    //val pipeline = Pipeline(stringPipe :: HNil)
    //val pipeline = Pipeline(strings :: ints :: HNil)
    //val pipeline = start.out(strings)
    //val path: String :: HNil  = pipeline.path
    // val path1 = path.head
    // val path2 = path.tail.head
    // assert(path1 == strings(0) :: HNil)
    // assert(path2 == strings(1) :: HNil)
    // assert(path1 == strings(0) :: ints(0) :: HNil)
    // assert(path2 == strings(1) :: ints(1) :: HNil)

    //val headPipe: Pipe[String] = getHead(pipeline.pipes)
    //val headPipe2: Pipe[String] = pipeline.toList


    /** TODOs:
    path with one pipe: replace ??? with impl
    path with two pipes
      Pipe[Graph,Vertex] :: Pipe[Vertex,Edge] :: Pipe[Edge,Vertex]
    Pipe needs to hold two types: I/O
    out: make work again
    out: append to hlist, not prepend
    out: use peano types to stop compiler if types of pipes don't fit together
    let path produce the content lazily, not eagerly
      use sink and producer? iteratees? scalaz?
    zip pipes to get real path instead of dummy list
    reverse types and pipes on each step? flatten hlist type?
      https://groups.google.com/forum/#!searchin/shapeless-dev/append/shapeless-dev/gOXAbvGqEv8/hgqZmqmiLDAJ
    pipe: extend some tinkerpop type?
    have three types of pipelines? VertexPipeline (that contains outE etc.), EdgePipeline (that contains inV etc.) and Pipeline that contains common stuff like filter?
      shouldn't be necessary if the compiler detects that inV.inV doesn't work
    */

  }

  ignore("third try: nested Pipes") {
    //problem: cannot nest types other than vertex/edge easily as InVPipe and OutEPipe are explicitly bound to these types
    //easy: Filter, label, ...
    //hard: path, back, ...

    case class Vertex(id: Int, outE: Iterator[Edge])
    case class Edge(id: Int, inV: Vertex)

    trait Pipe[I,O] {
      //def starts: Iterator[I]
      def iter: Iterator[O]
      def next: O = {
        println(s"pipe.next: $iter.next")
        iter.next
      }
      def hasNext = {
        println(s"pipe.hasNext: $iter.hasNext")
        iter.hasNext
      }
    }

    case class InVPipe(prev: Pipe[_, Edge]) extends Pipe[Edge, Vertex] {
      val iter = prev.iter map { _.inV }
    }
    case class OutEPipe(prev: Pipe[_, Vertex]) extends Pipe[Vertex, Edge] {
      val iter = prev.iter flatMap { _.outE }
    }

    def v(id: Int): Vertex = id match {
      case 1 ⇒ Vertex(id, outE = List(e(1), e(2)).iterator)
      case 2 ⇒ Vertex(id, outE = Nil.iterator)
    }
    def e(id: Int): Edge = id match {
      case 1|2 ⇒ Edge(id, inV = v(2))
    }

    val startPipe = new Pipe[Nothing, Vertex] {
      val iter = List(v(1)).iterator
    }
    val outE = OutEPipe(startPipe)
    val inV = InVPipe(outE)

    //startPipe.iter foreach println
    //outE.iter foreach println
    //inV.iter foreach println
  }

  ignore("second try") {
    type Vertex = Int
    type Edge = Float
    type Graph = String

    trait Pipe[S,+E] {}

    trait Pipeline[+T] {
      type head <: Pipe[_,T]
      type tail <: Pipeline[T]
    }

    trait NilPipeline extends Pipeline[Any] {
      override def toString = "NilPipeline"
    }
    object NilPipeline extends NilPipeline

    trait StartPipe[E] extends Pipe[Graph, E] {
      override def toString = "StartPipe"
    }
    trait VertexPipe[S] extends Pipe[S, Vertex] {
      override def toString = "VertexPipe"
    }
    trait EdgePipe[S] extends Pipe[S, Edge] {
      override def toString = "EdgePipe"
    }

    case class ::[S, E](head: Pipe[S,E], tail: Pipeline[E]) extends Pipeline[S] {
      override def toString = s"$head :: $tail"
    }

    val startPipe = new StartPipe[Vertex]{} //Pipe[Graph, Vertex]
    val edgePipe = new EdgePipe[Vertex]{} //Pipe[Vertex, Edge]
    val p1 = ::(startPipe, NilPipeline)
    val p2 = ::(edgePipe, p1)
    println(p1)
    println(p2)

    // TODOs:
    //ensure cannot combine wrong pipes
    //dummy version of Pipe and Pipeline methods
}


  ignore("first try") {
    import com.tinkerpop.blueprints.Vertex

    sealed trait Pipe {
      type Head // <: Pipeline //idea: have Pipeline[Vertex]?
      type Tail <: Pipe

      def get: Head
    }

    final case class VertexPipe extends Pipe {
      type Head = Vertex
      //def out: TypedPipe[S, Vertex] = ???
      //def outE: TypedPipe[S, Edge] = ???
      override def get = ???
    }

    sealed class PipeNil extends Pipe{
      type Head = Unit
      type Tail = PipeNil

      override def get = Unit
    }

    sealed trait HList {
      type Head
      type Tail <: HList
    }

    sealed class HNil extends HList {
      type Head = Nothing
      type Tail = HNil
      def ::[T](v : T) = HCons(v, this)
    }
    case object HNil extends HNil

    final case class HCons[H, T <: HList](head : H, tail : T) extends HList {
      type Head = H
      type Tail = T
      def ::[T](v : T) = HCons(v, this)
      
      //type Fun[T] = H => tail.Fun[T]
      //def apply[T](f: Fun[T]): T = tail( f(head) )

      //override def toString = head + " :: " + tail
    }

    val p = new VertexPipe
    println(p)
  }

}
