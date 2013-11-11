package com.tinkerpop.gremlin.scala

import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import com.tinkerpop.blueprints._
import com.tinkerpop.blueprints.impls.tg.TinkerGraph
import scala.collection.JavaConversions._

import shapeless._
import syntax.std.traversable._
import ops.traversable.FromTraversable
import ops.hlist._

class TypedPipelineSpec extends FunSpec with ShouldMatchers with TestGraph {

  def getHead[A <: HList :IsHCons](pipes: A) = pipes.head

  describe("Pipeline[A<:HList]") {
    it("works", org.scalatest.Tag("foo")) {

      case class Pipeline[A <: HList](pipes : A)(implicit val ev: IsHCons[A]) {
        // A: Vertex :: Edge :: Vertex :: HNil
        // Pipe[Graph,Vertex] :: Pipe[Vertex,Edge] :: Pipe[Edge,Vertex]
        def out[E](testObjects: List[E]) = Pipeline(Pipe(testObjects) :: pipes)

        //def path: HList[A] = List(1, "one").toHList[A].get//OrElse(HNil)

        def toList = {
          pipes.head//.testObjects
        }
      }

      case class Pipe[E](testObjects: List[E]) {
        type T = E
        val testIter = testObjects.iterator
        def next = testIter.next
        def hasNext = testIter.hasNext
      }

      object EmptyPipe extends Pipe(Nil)
      val start = Pipeline[Pipe[Nothing] :: HNil](EmptyPipe :: HNil)
      val testObjects1 = List[String]("edge1", "edge2")
      val testObjects2 = List[Int](1,2)
      val pipeline = start.out(testObjects1)//.out(testObjects2)
      val headPipe: Pipe[String] = getHead(pipeline.pipes)
      val headPipe2: Pipe[String] = pipeline.toList //why not???

      //val p = new Pipe(testObjects1)
      //type t = p.T
      //val a: Int = pipeline.toList(0)
      //println(pipeline.path)
      //pipeline.toList should be (testObjects2)

      //val a = 1 :: "one" :: HNil
      //type A = Int :: String :: HNil
      //def blub: A#H = ???

      /** TODOs:
      get rid of E in Pipeline type arguments → ensure always NonEmptyHList → :IsHCons
        problem: in toList, how do we get the type of A.HEAD? implicitly[IsHCons[A]] doesn't help us..
      `path`
        returns List[A]
      reverse types: use stuff 
      zip pipes to get real path instead of dummy list
      refer type of pipes to A?
      reverse types and pipes on each step? flatten hlist type?
        https://groups.google.com/forum/#!searchin/shapeless-dev/append/shapeless-dev/gOXAbvGqEv8/hgqZmqmiLDAJ
      use peano types to stop compiler if types don't fit together?
      use sink and producer? iteratees? scalaz?
      */

    val l = 1 :: "one" :: HNil
    def fun[A <: HList :IsHCons](a: A) = {
      a.head
    }
    val b: Int= fun(l)
    //val c: String= fun(l)
    //println(b)
  }
}

  describe("second try") {
    it("works") {
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
}


  describe("first try") {
    it("is strongly typed") {
      val p = new VertexPipe
      println(p)
     }

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
  }

}
