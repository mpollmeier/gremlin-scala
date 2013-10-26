package com.tinkerpop.gremlin.scala.filter

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import com.tinkerpop.gremlin.scala.TestGraph

class RangeStepTest extends FunSpec with ShouldMatchers with TestGraph {

  it("emits only a given range of elements") {
    graph.v(1).out.range(0, 1).toScalaList.size should be(2)
  }
}