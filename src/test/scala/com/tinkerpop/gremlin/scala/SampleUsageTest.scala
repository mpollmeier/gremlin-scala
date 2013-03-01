package com.tinkerpop.gremlin.scala

import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory
import com.tinkerpop.gremlin.scala.ScalaVertex._
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.tinkerpop.blueprints._
import com.tinkerpop.blueprints.impls.tg.TinkerGraph

@RunWith(classOf[JUnitRunner])
class SampleUsageTest extends FunSpec with ShouldMatchers {

  describe("Usage with default Tinkergraph") {
    val graph = TinkerGraphFactory.createTinkerGraph
    def vertices = graph.V

    it("finds all vertices") {
      vertices.count should be(6)
      vertices.map.toList.toString should be(
        "[{name=lop, lang=java}, {name=vadas, age=27}, {name=marko, age=29}, " +
          "{name=peter, age=35}, {name=ripple, lang=java}, {name=josh, age=32}]")
    }

    it("can get a specific vertex by id and get it's properties") {
      val marko = graph.v(1)
      marko("name") should be("marko")
      marko("age") should be(29)
    }

    it("finds everybody who is over 30 years old") {
      vertices.filter { v: Vertex ⇒
        v.as[Int]("age") match {
          case Some(age) if age > 30 ⇒ true
          case _                     ⇒ false
        }
      }.map.toList.toString should be(
        "[{name=peter, age=35}, {name=josh, age=32}]")
    }

    it("finds who marko knows") {
      val marko = graph.v(1)
      marko.out("knows")(_("name"))
        .toList.toString should be("[vadas, josh]")
    }

    it("finds who marko knows if a given edge property `weight` is > 0.8") {
      val marko = graph.v(1)
      marko.outE("knows").filter { e: Edge ⇒
        e.as[Float]("weight") match {
          case Some(weight) if weight > 0.8 ⇒ true
          case _                            ⇒ false
        }
      }.inV.map.toList.toString should be("[{name=josh, age=32}]")
    }

    describe("Usage with empty Graph") {
      it("creates a vertex with properties") {
        val graph = new TinkerGraph
        val id = 42
        val vertex = graph.addV(id)
        vertex.setProperty("key", "value")
        graph.v(id).getProperty("key") should be("value")
      }

      it("creates vertices without specific ids") {
        val graph = new TinkerGraph
        graph.addV()
        graph.addV()
        graph.V.count should be(2)
      }

      it("creates edges between vertices") {
        val graph = new TinkerGraph
        val v1 = graph.addV()
        val v2 = graph.addV()
        graph.addE(v1, v2, "label")

        val foundVertices = v1.out("label").toList
        foundVertices.size should be(1)
        foundVertices.get(0) should be(v2)
      }
    }

  }

}