package com.tinkerpop.gremlin.scala

import com.tinkerpop.blueprints._
import com.tinkerpop.gremlin.scala._
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec
import com.tinkerpop.pipes.util.structures.Row
import scala.collection.JavaConversions._
import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory

class TransformStepsSpec extends FunSpec with ShouldMatchers with TestGraph {

  describe("path") {
    it("returns a list with all objects in the path") {
      graph.v(1).startPipe.out.path.toSet should be(
        Set(
          Seq(v1, v2),
          Seq(v1, v4),
          Seq(v1, v3)
        )
      )
    }
  }

  describe("select") {
    val step1 = "step 1"
    val step2 = "step 2"

    it("returns a list with objects of named steps") {
      graph.v(1).startPipe.out.as(step1).out.as(step2).select.toSet should be(
        Set(
          new Row(List(v4, v5), List(step1, step2)),
          new Row(List(v4, v3), List(step1, step2))
        )
      )
    }

    it("only includes the provided named steps") {
      graph.v(1).startPipe.out.as(step1).out.as(step2).select(step2).toSet should be(
        Set(
          new Row(List(v5), List(step2)),
          new Row(List(v3), List(step2))
        )
      )
    }
  }

  describe("linkBoth [In/Out]") {

    trait Scenario {
      val graph: ScalaGraph = TinkerGraphFactory.createTinkerGraph
      val marko = graph.v(1)
    }

    it("links vertices in BOTH directions") {
      new Scenario {
        graph.V.except(List(marko)).linkBoth("connected",marko).iterate()
        marko.out("connected").count should be(5)
        marko.in("connected").count should be(5)
      }
    }

    it("links vertices in OUT direction") {
      new Scenario {
        graph.V.except(List(marko)).linkOut("connected",marko).iterate()
        marko.in("connected").count should be(5)
      }
    }

    it("links vertices in IN direction") {
      new Scenario {
        graph.V.except(List(marko)).linkIn("connected",marko).iterate()
        marko.out("connected").count should be(5)
      }
    }

  }

}
