package com.tinkerpop.gremlin.scala

import com.tinkerpop.blueprints._
import com.tinkerpop.gremlin.scala._
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec
import com.tinkerpop.gremlin.Tokens.T._
import com.tinkerpop.pipes.branch.LoopPipe.LoopBundle

class BranchStepsTest extends FunSpec with ShouldMatchers with TestGraph {

  describe("ifThenElse") {
    it("gets josh's age, otherwise the name") {
      graph.v(1).out.ifThenElse(
        { v: Vertex ⇒ v("name") == "josh" },
        { v: Vertex ⇒ v("age") },
        { v: Vertex ⇒ v("name") })
        .toScalaList should be(List("vadas", 32, "lop"))
    }
  }

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

  describe("copySplit and fairMerge") {
    it("copies incoming objects to internal pipes, then merges the parallel traversals in a round-robin fashion") {
      graph.v(1).out
        .copySplit(->[Vertex].property("name"), ->[Vertex].property("age"))
        .fairMerge
        .toScalaList should be(List("vadas", 27, "josh", 32, "lop"))
    }
  }

  describe("copySplit and exhaustMerge") {
    it("copies incoming objects to internal pipes, then merges the parallel traversals in a greedy fashion") {
      graph.v(1).out
        .copySplit(->[Vertex].property("name"), ->[Vertex].property("age"))
        .exhaustMerge
        .toScalaList should be(List("vadas", "josh", "lop", 27, 32))
    }
  }

  describe("simplePath") {
    it("simplifies the path by removing cycles") {
      graph.v(1).out.in.simplePath.toScalaList.size should be(2)
    }
  }

}
