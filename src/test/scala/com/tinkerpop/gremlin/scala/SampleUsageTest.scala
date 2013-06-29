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
class SampleUsageTest extends FunSpec with ShouldMatchers with TestGraph {

  describe("Usage with default Tinkergraph") {
    it("finds all vertices") {
      vertices.count should be(6)
      vertices.propertyMap.toScalaList should be(List(
        Map("name" -> "lop", "lang" -> "java"),
        Map("age" -> 27, "name" -> "vadas"),
        Map("name" -> "marko", "age" -> 29),
        Map("name" -> "peter", "age" -> 35),
        Map("name" -> "ripple", "lang" -> "java"),
        Map("name" -> "josh", "age" -> 32)))
    }

    it("finds all names of vertices") {
      vertices.property("name").toScalaList should be(List("lop", "vadas", "marko", "peter", "ripple", "josh"))
    }

    it("can get a specific vertex by id and get it's properties") {
      val marko = graph.v(1)
      marko("name") should be("marko")
      marko("age") should be(29)
    }

    it("finds everybody who is over 30 years old") {
      vertices.filter { v: Vertex ⇒
        v.get[Int]("age") match {
          case Some(age) if age > 30 ⇒ true
          case _                     ⇒ false
        }
      }.propertyMap().toScalaList should be(List(
        Map("name" -> "peter", "age" -> 35),
        Map("name" -> "josh", "age" -> 32)))
    }

    it("finds who marko knows") {
      val marko = graph.v(1)
      marko.out("knows").map { _("name") }.toScalaList should be(List("vadas", "josh"))
    }

    it("finds who marko knows if a given edge property `weight` is > 0.8") {
      val marko = graph.v(1)
      marko.outE("knows").filter { e: Edge ⇒
        e.get[Float]("weight") match {
          case Some(weight) if weight > 0.8 ⇒ true
          case _                            ⇒ false
        }
      }.inV.propertyMap.toScalaList should be(List(Map("name" -> "josh", "age" -> 32)))
    }

    describe("Usage with empty Graph") {
      it("creates a vertex with properties") {
        val graph = new TinkerGraph
        val id = 42
        val vertex = graph.addV(id)
        vertex.setProperty("key", "value")

        graph.v(id)("key") should be("value")
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