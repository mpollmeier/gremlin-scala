package com.tinkerpop.gremlin.scala.filter

import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory
import com.tinkerpop.gremlin.test.ComplianceTest
import com.tinkerpop.blueprints._
import com.tinkerpop.gremlin.scala._
import com.tinkerpop.pipes.Pipe
import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner
import com.tinkerpop.gremlin.Tokens.T._

@RunWith(classOf[JUnitRunner]) /** @see http://gremlindocs.com/#filter/and */
class AndStepTest extends FunSpec with ShouldMatchers with TestGraph {

  it("finds edges that are true for all of the pipes") {
    val pipe1 = ->[Edge].has("weight", gt, 0.4f)
    val pipe2 = ->[Edge].has("weight", lt, 0.8f)
    graph.v(1).outE.and(pipe1, pipe2).id.toList should be(List("7"))
  }

}
