package com.tinkerpop.gremlin.scala.branch

import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory
import com.tinkerpop.gremlin.test.ComplianceTest
import com.tinkerpop.gremlin.scala._
import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.pipes.Pipe
import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SplitMergeStepsTest extends FunSpec with ShouldMatchers with TestGraph {

  describe("copySplit and fairMerge") {
    it("copies incoming objects to internal pipes, then merges the parallel traversals in a round-robin fashion") {
      graph.v(1).out
        .copySplit(->[Vertex].property("name"), ->[Vertex].property("age"))
        .fairMerge
        .toScalaList should be(List("vadas", 27, "josh", 32, "lop"))
    }
  }

}
