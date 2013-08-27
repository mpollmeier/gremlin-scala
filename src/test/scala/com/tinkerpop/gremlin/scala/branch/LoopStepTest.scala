package com.tinkerpop.gremlin.scala.branch

import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory
import com.tinkerpop.gremlin.test.ComplianceTest
import com.tinkerpop.pipes.branch.LoopPipe.LoopBundle
import com.tinkerpop.gremlin.scala._
import com.tinkerpop.pipes.Pipe
import com.tinkerpop.blueprints.{ Graph, Vertex }
import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class LoopStepTest extends FunSpec with ShouldMatchers with TestGraph {

  describe("looping") {
    it("jumps back to named step and loops twice") {
      graph.v(1).startPipe.as("here").out.loop("here", { lb: LoopBundle[Vertex] ⇒ lb.loops < 3 })
        .name.toScalaList should be(List("ripple", "lop"))
    }

    it("optionally takes an emit function that decides if the current object gets emitted or not - that could get emitted multiple times") {
      graph.v(1).startPipe.as("here").out.loop(
        namedStep = "here",
        whileFun = { lb: LoopBundle[Vertex] ⇒ lb.loops < 3 },
        emit = { lb: LoopBundle[Vertex] ⇒ ScalaVertex(lb.getObject).name == "lop" })
        .name.toScalaList should be(List("lop", "lop"))
    }

  }

}