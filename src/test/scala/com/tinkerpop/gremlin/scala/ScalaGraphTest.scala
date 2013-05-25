package com.tinkerpop.gremlin.scala

import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory
import com.tinkerpop.gremlin.scala.ScalaVertex._
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.tinkerpop.blueprints._
import com.tinkerpop.blueprints.impls.tg.TinkerGraph
import scala.collection.JavaConversions._
import scala.collection.mutable.Map

@RunWith(classOf[JUnitRunner])
class ScalaGraphTest extends FunSpec with ShouldMatchers {

  describe("ScalaGraph") {
    val graph: ScalaGraph = TinkerGraphFactory.createTinkerGraph

    it("knows all vertices") {
      println(graph.V.has("name", "marko", "peter").toList)
    }
  }
}