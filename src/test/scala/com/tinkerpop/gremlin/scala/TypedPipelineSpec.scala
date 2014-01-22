package com.tinkerpop.gremlin.scala

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import scala.collection.JavaConversions._

import shapeless._
import ops.hlist._

class TypedPipelineSpec extends FunSpec with ShouldMatchers {

  it("uses T3") {
    import com.tinkerpop.gremlin._
    import com.tinkerpop.blueprints._
    import com.tinkerpop.blueprints.tinkergraph.TinkerFactory
    import java.util.{List ⇒ JList, Iterator ⇒ JIterator}
    import shapeless.test.illTyped

    //TODO: provide overridden constructor that takes Gremlin instead of Pipeline that creates a dummy step
    //Alternative: GremlinScala.of(graph)

    case class GremlinScala[End, Types <: HList](pipeline: Pipeline[_, End]) {
      def toList(): List[End] = pipeline.toList.toList
      def as(name: String) = GremlinScala[End, Types](pipeline.as(name))
      //def back[E](to: String)(implicit p:Prepend[Types, E::HNil]) = 
        //GremlinScala[E, p.Out](pipeline.back(to).asInstanceOf[GremlinPipeline[_,E]])

      //def path(implicit p:Prepend[Types, Types::HNil]): GremlinScala[Types, p.Out] =
        //addPipe(new PathPipe[End, Types])

      //def addPipe[E](pipe: Pipe[End, E])(implicit p:Prepend[Types, E::HNil]) = 
        //GremlinScala[E, p.Out](pipeline.add(pipe))
    }

    implicit class GremlinEdgeSteps[End <: Edge, Types <: HList](gremlinScala: GremlinScala[End,Types])
      extends GremlinScala[End, Types](gremlinScala.pipeline) {

      def inV(implicit p:Prepend[Types, Vertex::HNil]) = GremlinScala[Vertex, p.Out](pipeline.inV)
    }

    implicit class GremlinVertexSteps[End <: Vertex, Types <: HList](gremlinScala: GremlinScala[End,Types])
      extends GremlinScala[End, Types](gremlinScala.pipeline) {

      def outE(implicit p:Prepend[Types, Edge::HNil]) = GremlinScala[Edge, p.Out](pipeline.outE())
    }

    //class PathPipe[S, E <: HList] extends AbstractPipe[S, E] with TransformPipe[S, E] {
      //override def setStarts(starts: JIterator[S]): Unit = {
        //super.setStarts(starts)
        //this.enablePath(true)
      //}

      //override def processNextStart(): E = starts match {
        //case starts: Pipe[_,_] ⇒ 
          //starts.next()
          //val path: JList[_] = starts.getCurrentPath
          //toHList[E](path.toList)
      //}

      //def toHList[T <: HList](path: List[_]): T = 
        //if(path.length == 0)
          //HNil.asInstanceOf[T]
        //else
          //(path.head :: toHList[IsHCons[T]#T](path.tail)).asInstanceOf[T]
    //}

    val graph = TinkerFactory.createClassic()
    def gremlin: Gremlin[_, Vertex] = Gremlin.of(graph).asInstanceOf[Gremlin[_, Vertex]]
    /*def gremlin = new GremlinPipeline[Unit, Vertex](graph.getVertex(1))*/
    def gs = GremlinScala[Vertex, Vertex :: HNil](gremlin.v(1:Integer))
    println(gs.outE.toList)

    //print(vertexGremlin.outE)
    //print(vertexGremlin.outE.inV)
    //print(vertexGremlin.as("x").outE.back[Vertex]("x"))

    //vertexGremlin.path.toList foreach { l: Vertex :: HNil ⇒ println(l) }
    //vertexGremlin.outE.path.toList foreach { l: Vertex :: Edge :: HNil ⇒ println(l) }

    //// verify that these do not compile
    //illTyped {""" vertexGremlin.inV """}
    //illTyped {""" vertexGremlin.outE.inV.inV """}
    //illTyped {""" vertexGremlin.outE.back[Edge]("x").outE """}
    //illTyped {""" edgeGremlin.outE """}
    //illTyped {""" edgeGremlin.inV.outE.outE """}

    //def print(gremlin: GremlinScala[_,_]): Unit = {
      //println("----------results---------")
      //gremlin.toList foreach println
    //}
  }

 
  //it("using a simple wrapper that maintains the right order of the types by appending") {
    //import com.tinkerpop.blueprints.Edge
    //import com.tinkerpop.blueprints.Vertex
    //import com.tinkerpop.pipes.Pipe
    //import com.tinkerpop.pipes.AbstractPipe
    //import com.tinkerpop.pipes.transform.TransformPipe
    //import com.tinkerpop.gremlin.java.GremlinPipeline
    //import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory
    //import java.util.{List ⇒ JList, Iterator ⇒ JIterator}
    //import shapeless.test.illTyped

    //case class GremlinScala[End, Types <: HList](gremlin: GremlinPipeline[_, End]) {
      //def toList(): List[End] = gremlin.toList.toList
      //def as(name: String) = GremlinScala[End, Types](gremlin.as(name))
      //def back[E](to: String)(implicit p:Prepend[Types, E::HNil]) = 
        //GremlinScala[E, p.Out](gremlin.back(to).asInstanceOf[GremlinPipeline[_,E]])

      //def path(implicit p:Prepend[Types, Types::HNil]): GremlinScala[Types, p.Out] =
        //addPipe(new PathPipe[End, Types])

      //def addPipe[E](pipe: Pipe[End, E])(implicit p:Prepend[Types, E::HNil]) = 
        //GremlinScala[E, p.Out](gremlin.add(pipe))
    //}

    //implicit class GremlinEdgeSteps[End <: Edge, Types <: HList](gremlinScala: GremlinScala[End,Types])
      //extends GremlinScala[End, Types](gremlinScala.gremlin) {

      //def inV(implicit p:Prepend[Types, Vertex::HNil]) = GremlinScala[Vertex, p.Out](gremlin.inV)
    //}

    //implicit class GremlinVertexSteps[End <: Vertex, Types <: HList](gremlinScala: GremlinScala[End,Types])
      //extends GremlinScala[End, Types](gremlinScala.gremlin) {

      //def outE(implicit p:Prepend[Types, Edge::HNil]) = GremlinScala[Edge, p.Out](gremlin.outE())
    //}

    //class PathPipe[S, E <: HList] extends AbstractPipe[S, E] with TransformPipe[S, E] {
      //override def setStarts(starts: JIterator[S]): Unit = {
        //super.setStarts(starts)
        //this.enablePath(true)
      //}

      //override def processNextStart(): E = starts match {
        //case starts: Pipe[_,_] ⇒ 
          //starts.next()
          //val path: JList[_] = starts.getCurrentPath
          //toHList[E](path.toList)
      //}

      //def toHList[T <: HList](path: List[_]): T = 
        //if(path.length == 0)
          //HNil.asInstanceOf[T]
        //else
          //(path.head :: toHList[IsHCons[T]#T](path.tail)).asInstanceOf[T]
    //}

    //val graph = TinkerGraphFactory.createTinkerGraph
    //def vertexPipeline = new GremlinPipeline[Unit, Vertex](graph.getVertex(1))
    //def vertexGremlin = GremlinScala[Vertex, Vertex :: HNil](vertexPipeline)

    //print(vertexGremlin.outE)
    //print(vertexGremlin.outE.inV)
    //print(vertexGremlin.as("x").outE.back[Vertex]("x"))

    //vertexGremlin.path.toList foreach { l: Vertex :: HNil ⇒ println(l) }
    //vertexGremlin.outE.path.toList foreach { l: Vertex :: Edge :: HNil ⇒ println(l) }

    //// verify that these do not compile
    //illTyped {""" vertexGremlin.inV """}
    //illTyped {""" vertexGremlin.outE.inV.inV """}
    //illTyped {""" vertexGremlin.outE.back[Edge]("x").outE """}
    //illTyped {""" edgeGremlin.outE """}
    //illTyped {""" edgeGremlin.inV.outE.outE """}

    //def print(gremlin: GremlinScala[_,_]): Unit = {
      //println("----------results---------")
      //gremlin.toList foreach println
    //}
  //}


  //ignore("keeps the types in right order so we don't have to reverse them") {
    //class Pipeline[End, Types <: HList] {

      //def toList: List[End] = ???

      //def intStep(implicit p:Prepend[Types, Int :: HNil]): Pipeline[Int, p.Out] = ???
      //def stringStep(implicit p:Prepend[Types, String :: HNil]): Pipeline[String, p.Out] 
        //= addStep[String]

      //def path(implicit p:Prepend[Types, Types :: HNil]): Pipeline[Types, p.Out] = ???

      //def addStep[E](implicit p:Prepend[Types, E :: HNil]): Pipeline[E, p.Out] = ???
    //}

    //val p = new Pipeline[String, String :: HNil]
    //val p1: Pipeline[Int, String :: Int :: HNil] = p.intStep
    //p1.path.toList.foreach {_: String :: Int :: HNil ⇒ }

    //p.stringStep.intStep.path.toList.foreach { _: String :: String :: Int :: HNil ⇒ }
  //}

  //ignore("demonstrates the problem for shapeless-dev") {
    //// type Head is the first entry of the HList L - one could aswell get that vis IsHCons[L], but this is simpler for now
    //class Pipeline[Head, L <: HList](implicit val R:Reverse[L]) {

      //def reifyR[L <: HList](implicit R:Reverse[L]): Reverse[L]{type Out = R.Out} = R
      //type L1 = L

      ////type LRev = R.Out
      //type LRev
      ////val head = implicitly[IsHCons[L]]
      ////type Head = head.H
      //val rev = reifyR[L]
      ////type LRev = rev.Out


      //// get a list of all Head elements
      //def iterate: List[Head] = ???

      //// the Head type is the reverse of L
      //def allPreviousStepsAsListInReverseOrder: Pipeline[LRev, LRev :: L]{type LRev = rev.Out} = ???

      //def allPreviousStepsAsList: Pipeline[L, L :: L] = ???
    //}

    //val p = new Pipeline[String, String :: Int :: HNil]
    //def reifyR[L <: HList](implicit R:Reverse[L]): Reverse[L]{type Out = R.Out} = R
    //val rev = reifyR[p.L1]
    ////val rev = reifyR[String :: Int :: HNil]
    //type LR2 = rev.Out
    //type LR = Int :: String :: HNil
    //implicitly[LR2 =:= LR]
    ////implicitly[p.LRev =:= LR] //does not compile

    //p.iterate foreach {_: String ⇒ }
    //p.allPreviousStepsAsList.iterate foreach {_: String :: Int :: HNil ⇒ }
    ////p.allPreviousStepsAsListInReverseOrder.iterate foreach {_: Int :: String :: HNil ⇒ }
    ////p.allPreviousStepsAsListInReverseOrder.iterate foreach {_: p.LRev ⇒ }
  //}

}
