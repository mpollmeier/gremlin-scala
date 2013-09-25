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
class SimplePathTest extends FunSpec with ShouldMatchers with TestGraph {

  it("simplifies the path by removing cycles") {
    graph.v(1).out.in.simplePath.toScalaList.size should be(2)
  }

}
