package com.tinkerpop.gremlin.scala

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import scala.collection.JavaConversions._

import shapeless._
import ops.hlist._

class TypedPipelineSpec extends FunSpec with ShouldMatchers {

  it("uses T3") {
    //import com.tinkerpop.gremlin.structure._
    import com.tinkerpop.gremlin._
    //import com.tinkerpop.gremlin.pipes.map.PathPipe
    import com.tinkerpop.blueprints._
    import com.tinkerpop.tinkergraph.TinkerFactory
    import java.util.function.{Function => JFunction}
    import java.util.{List => JList, Iterator => JIterator}
    import shapeless.test.illTyped

    implicit def toJavaFunction[A,B](f: Function1[A,B]) = new JFunction[A,B] {
      override def apply(a: A): B = f(a)
    }

    //path concept: don't use standard path, but hlist
    //problem: uses simplepath which doesnt support path - what's different?
    //TODO: get path step so that it uses the pathoptimizer... so that it doesnt complain any more?
      //Gremlin: PathOptimizer
    // alternative: do what gremlin does manually
    //TODO: provide overridden constructor that takes Gremlin instead of Pipeline that creates a dummy step
    //Alternative: GremlinScala.of(graph)

    case class GremlinScala[Types <: HList, End](pipeline: Pipeline[_, End]) {
      def toList(): List[End] = pipeline.toList.toList
      def as(name: String) = GremlinScala[Types, End](pipeline.as(name))

      def back[E](to: String)(implicit p:Prepend[Types, E::HNil]) =
        GremlinScala[p.Out, E](pipeline.back(to).asInstanceOf[Pipeline[_,E]])

      //def path(implicit p:Prepend[Types, Types::HNil]): GremlinScala[p.Out, Types] =
        //GremlinScala[p.Out, Types](pipeline.addPipe(new PathPipe(pipeline, null).asInstanceOf[Pipeline[End, Types]]))
        //GremlinScala[p.Out, Types](pipeline.path().asInstanceOf[Pipeline[End, Types]])
        //addPipe(new com.tinkerpop.gremlin.pipes.map.PathPipe[End](pipeline))
      //def path(implicit p:Prepend[Types, Types::HNil]): GremlinScala[p.Out, Types] =
        //addPipe(new MyPathPipe3[End, Types](pipeline))
        //doesnt work

        //doesnt work
      //def path2 = GremlinScala[Types, End](
        //pipeline.addPipe(new MyPathPipe3[End, Types](pipeline)))

      ////works - where's the difference?
      //def path3 = pipeline.path()

      ////works - difference must be PathPipe and my PathPipe - copy it
      //def path4:Pipeline[Vertex, Vertex] = pipeline.addPipe(
        //new PathPipe[End](pipeline))

      ////doesnt work - holder is simpleholder for some reason... whats different?
      //def path5:Pipeline[Vertex, Vertex] = pipeline.addPipe(
        //new MyPathPipe[End](pipeline))

      ////works
      //def path6:Pipeline[Vertex, Vertex] = pipeline.addPipe(
        //new MyPathPipe2[End](pipeline))

      ////works
      //def path7:Pipeline[Vertex, Vertex] = pipeline.addPipe(
        //new MyPathPipe4[End](pipeline))

      //def path8:Pipeline[Vertex, Vertex] = pipeline.addPipe(
        //new MyPathPipe8[End, Types](pipeline))

      def addPipe[E](pipe: Pipe[End, E])(implicit p:Prepend[Types, E::HNil]) =
        GremlinScala[p.Out, E](pipeline.addPipe(pipe))
    }

    //class MyPathPipe2[S](pipeline: Pipeline[_,S]) extends PathPipe[S](pipeline)
    //class MyPathPipe4[S](pipeline: Pipeline[_,S]) extends PathPipe[S](pipeline) {
      //this.setFunction { h: Holder[_] =>
        //println(s"holder: $h, ${h.getPath()}")
        ////HNil.asInstanceOf[E] }
        //h.getPath
      //}
    //}
    //class MyPathPipe8[S, E <: HList](pipeline: Pipeline[_,S]) extends PathPipe[S](pipeline) {
      //this.setFunction { h: Holder[_] =>
        //println(s"holder: $h, ${h.getPath()}")
        ////HNil.asInstanceOf[E] }
        //h.getPath
        ////HNil.asInstanceOf[E] }
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
    //class MyPathPipe[S](pipeline: Pipeline[_,S]) extends MapPipe[S, Path](pipeline) {
      //this.setFunction { h:Any =>
      ////this.setFunction { h: Holder[_] =>
        ////println(s"holder: $h, ${h.getPath()}")
        //////HNil.asInstanceOf[E] }
        ////h.getPath
        //val path = new Path()
        //println(h)
        //h match {
          //case h: Holder[_] ⇒  println(s"bla ${h.getPath}")
        //}
        ////val a = h.getPath
        //path
      //}
    //}

    implicit class GremlinEdgeSteps[Types <: HList, End <: Edge](gremlinScala: GremlinScala[Types, End])
      extends GremlinScala[Types, End](gremlinScala.pipeline) {

      def inV(implicit p:Prepend[Types, Vertex::HNil]) = GremlinScala[p.Out, Vertex](pipeline.inV)
    }

    implicit class GremlinVertexSteps[Types <: HList, End <: Vertex](gremlinScala: GremlinScala[Types, End])
      extends GremlinScala[Types, End](gremlinScala.pipeline) {

      def outE(implicit p:Prepend[Types, Edge::HNil]) = GremlinScala[p.Out, Edge](pipeline.outE())
    }

    //class MyPathPipe3[S, E <: HList](pipeline: Pipeline[_,S]) extends MapPipe[S, E](pipeline) {
      ////this.setFunction {h: Holder[_] => h.getPath.asInstanceOf[E] }
      //this.setFunction { h: Holder[_] => 
        ////println(pipeline.getClass)
        //println(s"holder: $h, ${h.getPath()}")
        //HNil.asInstanceOf[E] }

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

    val graph: Graph = TinkerFactory.createClassic()
    def gremlin: GremlinJ[_, Vertex] = GremlinJ.of(graph).asInstanceOf[GremlinJ[_, Vertex]]
    def gs = GremlinScala[Vertex :: HNil, Vertex](gremlin.v(1:Integer))

    //print(gs.outE)
    //print(gs.outE.inV)
    //print(gs.as("x").outE.back[Vertex]("x"))

    //println("XXXXXXXX")
    //println(Gremlin.of(graph).v(1:Integer).value("name").path().toList)
    //println(gremlin.v(1:Integer).value("name").path().toList)
    //println(gs.pipeline.value("name").path().toList)
    //println(gs.outE.inV.pipeline.path().toList)
    //println(gs.outE.inV.path8.toList)
    //println("XXXXXXXX")
    //gs.path.toList foreach println
    //gs.path.toList foreach { l: Vertex :: HNil ⇒ println(l) }
    //gs.outE.path.toList foreach { l: Vertex :: Edge :: HNil ⇒ println(l) }

    //// verify that these do not compile
    illTyped {""" gs.inV """}
    illTyped {""" gs.outE.inV.inV """}
    illTyped {""" gs.outE.back[Edge]("x").outE """}

    def print(gremlin: GremlinScala[_,_]): Unit = {
      println("----------results---------")
      gremlin.toList foreach println
    }
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
