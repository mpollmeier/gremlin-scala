package com.tinkerpop.gremlin.scala.filter

import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory
import com.tinkerpop.gremlin.test.ComplianceTest
import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.gremlin.scala._
import com.tinkerpop.pipes.Pipe
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec

class BackStepTest extends FunSpec with ShouldMatchers with TestGraph {

  it("goes back to named step") {
    val namedStep = "named step"
    graph.v(1).startPipe.as(namedStep).out.back(namedStep).toList should be(List(v1))
  }

}
