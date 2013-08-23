package com.tinkerpop.gremlin.scala.filter

import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory
import com.tinkerpop.gremlin.test.ComplianceTest
import com.tinkerpop.gremlin.scala._
import com.tinkerpop.gremlin.Tokens.T
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec
import com.tinkerpop.gremlin.Tokens

@RunWith(classOf[JUnitRunner])
class HasStepTest extends FunSpec with ShouldMatchers with TestGraph {

  it("finds vertices with given property") {
    graph.V.has("age").toScalaList.size should be(4)
  }

  it("finds vertices with given property and value") {
    graph.V.has("name", "marko").toScalaList.size should be(1)
  }

  it("finds vertices without a given property") {
    graph.V.hasNot("age").toScalaList.size should be(2)
  }

  it("finds vertices without a given property and value") {
    graph.V.hasNot("name", "marko").toScalaList.size should be(5)
  }

  describe("tokens") {
    import Tokens.T

    it("finds equal") {
      graph.V.has("age", T.eq, 29).toScalaList.size should be(1)
    }

    it("finds greater than") {
      graph.V.has("age", T.gt, 29).toScalaList.size should be(2)
    }
  }

}
