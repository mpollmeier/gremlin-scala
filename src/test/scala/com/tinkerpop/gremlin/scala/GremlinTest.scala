package com.tinkerpop.gremlin.scala

import com.tinkerpop.blueprints._
import com.tinkerpop.tinkergraph.TinkerFactory
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec
import shapeless._

trait TestGraph {
  val graph = TinkerFactory.createClassic()
  def gs: GremlinScala[Graph :: HNil, Graph] = GremlinScala.of(graph)

  def print(gs: GremlinScala[_,_]) = println(gs.toList)
}

trait GremlinSpec extends FunSpec with ShouldMatchers with TestGraph

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

