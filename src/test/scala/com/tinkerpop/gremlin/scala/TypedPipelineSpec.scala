package com.tinkerpop.gremlin.scala

import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import com.tinkerpop.blueprints._
import com.tinkerpop.blueprints.impls.tg.TinkerGraph
import scala.collection.JavaConversions._

class TypedPipelineSpec extends FunSpec with ShouldMatchers with TestGraph {

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
