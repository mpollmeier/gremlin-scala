package com.tinkerpop.gremlin.scala.branch

import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory
import com.tinkerpop.gremlin.test.ComplianceTest
import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.gremlin.scala._
import com.tinkerpop.pipes.Pipe
import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class IfThenElseStepTest extends FunSpec with ShouldMatchers with TestGraph {

  it("gets josh's age, otherwise the name") {
    graph.v(1).out.ifThenElse(
      { v: Vertex ⇒ v("name") == "josh" },
      { v: Vertex ⇒ v("age") },
      { v: Vertex ⇒ v("name") })
      .toScalaList should be(List("vadas", 32, "lop"))
  }

}