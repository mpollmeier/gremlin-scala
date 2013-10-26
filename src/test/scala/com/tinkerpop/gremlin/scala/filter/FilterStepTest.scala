package com.tinkerpop.gremlin.scala.filter

import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory
import com.tinkerpop.gremlin.test.ComplianceTest
import com.tinkerpop.blueprints.Vertex
import java.lang.{ Integer ⇒ JInteger }
import com.tinkerpop.gremlin.scala._
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec
import com.tinkerpop.blueprints.Edge

class FilterStepTest extends FunSpec with ShouldMatchers with TestGraph {
  describe("filter") {
    it("finds none") {
      vertices.filter { _ ⇒ false }.toList.size should be(0)
    }

    it("finds all") {
      vertices.filter { _ ⇒ true }.toList.size should be(6)
    }

    it("finds vertices with string property") {
      vertices.filter { _.property[String]("lang").exists(_ == "java") }.toList.size should be(2)
    }

    it("finds vertices with int property") {
      vertices.filter { _.property[Integer]("age").exists(_ > 30) }.toList.size should be(2)
    }

    it("finds that v1.out has id=2") {
      // tinkergraph uses strings for ids
      graph.v(1).out.filter { _.id == "2" }.toList.size should be(1)
    }

    it("finds 'created' edges") {
      edges.filter { _.label == "created" }.toList.size should be(4)
    }

  }

  describe("filterNot") {
    it("finds none") {
      vertices.filterNot { _ ⇒ false }.toList.size should be(6)
    }

    it("finds all") {
      vertices.filterNot { _ ⇒ true }.toList.size should be(0)
    }

    it("finds vertices with string property") {
      vertices.filterNot { _.property[String]("lang").exists(_ == "java") }.toList.size should be(4)
    }

    it("finds vertices with int property") {
      vertices.filterNot { _.property[Integer]("age").exists(_ > 30) }.toList.size should be(4)
    }

    it("finds that v1.out has two vertices whose id is not 2") {
      // tinkergraph uses strings for ids
      graph.v(1).out.filterNot { _.id == "2" }.toList.size should be(2)
    }

    it("finds edges other than 'created'") {
      edges.filterNot { _.label == "created" }.toList.size should be(2)
    }

  }

}
