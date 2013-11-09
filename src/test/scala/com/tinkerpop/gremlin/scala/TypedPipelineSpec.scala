package com.tinkerpop.gremlin.scala

import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import com.tinkerpop.blueprints._
import com.tinkerpop.blueprints.impls.tg.TinkerGraph
import scala.collection.JavaConversions._

class TypedPipelineSpec extends FunSpec with ShouldMatchers with TestGraph {

  describe("Pipeline[A<:HList]") {
    it("works", org.scalatest.Tag("foo")) {
      import shapeless._
      type Vertex = Int
      type Edge = Double

      case class Pipe[S,E](testObjects: List[E]) {
        val testIter = testObjects.iterator
        def next = testIter.next
        def hasNext = testIter.hasNext
      }

      case class Pipeline(pipes: HList) {
        //example for pipes: Pipe[V,E] :: Pipe[E,V] :: HNil
        def out[S,E](testObjects: List[E]) = 
          Pipeline(Pipe(testObjects) :: pipes)

        def path = pipes
        // combine all pipes.. return something that uses all the right types
        // should be List[A] for all the steps...

      }
      val start = new Pipeline(HNil)
      val testObjects1 = List[Vertex](1,2)
      val testObjects2 = List[Edge](1.0,2.0)
      val pipeline = start.out(testObjects1).out(testObjects2)
      //println(pipeline)
      println(pipeline.path)

      //case class Pipeline[A <: HList](pipes: List[Pipe[_,_]]) {
        ////example for pipes: Pipe[V,E] :: Pipe[E,V] :: HNil
        //def out[T](testObjects: List[T]) = 
          //Pipeline[Vertex :: A](Pipe(testObjects) :: pipes)

        //def path = pipes
        //// should be List[A] for all the steps...

      //}
      //val start = new Pipeline[HNil](Nil)


      // TODOs:
      //path: cast to A (<: HList)?
      //  List(1, 2, 3).toHList[Int :: Int :: Int :: HNil] 
      //  pipes.toHList[A]?
      //get actual types of pipes (S and E) → on out(...)
      //would be nice for pipes, too, but that can get casted for now
      //make pipes typesafe → HList / A? → HList[Pipe]
      //ensure cannot combine wrong pipes
      //use sink and producer? iteratees? scalaz?
      //try to get .path signature right
      //remove Pipe testObjects

      //case class Pipeline(pipes: HList) {
        ////example for pipes: Pipe[V,E] :: Pipe[E,V] :: HNil
        //def out[S,E](testObjects: List[E]) = 
          //Pipeline(Pipe(testObjects) :: pipes)

        //def path = pipes
        //// combine all pipes.. return something that uses all the right types
        //// should be List[A] for all the steps...
      //}
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
