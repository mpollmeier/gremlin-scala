package com.tinkerpop.gremlin.scala.filter

import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import com.tinkerpop.gremlin.Tokens
import com.tinkerpop.gremlin.Tokens.T
import com.tinkerpop.gremlin.scala.TestGraph
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class IntervalStepTest extends FunSpec with ShouldMatchers with TestGraph {

  it("finds everybody between 20 and 30 years old") {
    graph.V.interval("age", 20, 30).toScalaList.size should be(2)
  }

}
