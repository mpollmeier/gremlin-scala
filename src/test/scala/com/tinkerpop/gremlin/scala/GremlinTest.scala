package com.tinkerpop.gremlin.scala

import com.tinkerpop.gremlin.structure._
import com.tinkerpop.tinkergraph.TinkerFactory
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec
import shapeless._

trait TestGraph {
  val graph = TinkerFactory.createClassic()
  def gs: ScalaGraph = GremlinScala.of(graph)
  def v(i: Int) = gs.v(i:Integer)
  def e(i: Int) = gs.e(i:Integer)

  def print(gs: GremlinScala[_,_]) = println(gs.toList)
}

trait GremlinSpec extends FunSpec with ShouldMatchers with TestGraph {
  implicit class Properties[A](set: Traversable[Property[A]]) {
    def unroll(): Traversable[A] = set map (_.get)
  }
}



//TODO
  //describe("ScalaElement equality and hashCode are based on their id") {
    //it("equals") {
      //graph.v(1) == graph.v(1) should be(true)
      //graph.v(1) == graph.v(2) should be(false)
    //}

    //it("uses the right hashCodes") {
      //graph.v(1).hashCode should be(graph.v(1).hashCode)
      //graph.v(1).hashCode should not be (graph.v(2).hashCode)

      //Set(graph.v(1)) contains (graph.v(1)) should be(true)
      //Set(graph.v(1)) contains (graph.v(2)) should be(false)
    //}
  //}

