package com.tinkerpop.gremlin.scala.filter

import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory
import com.tinkerpop.gremlin.test.ComplianceTest
import com.tinkerpop.blueprints.Vertex
import java.lang.{ Integer ⇒ JInteger }
import com.tinkerpop.gremlin.scala._
import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner
import com.tinkerpop.blueprints.Edge

@RunWith(classOf[JUnitRunner])
class FilterStepTest extends FunSpec with ShouldMatchers with TestGraph {
  describe("filter") {
    it("finds none") {
      vertices.filter { v: Vertex ⇒ false }.toList.size should be(0)
      vertices.filterPF { case _ ⇒ false }.toList.size should be(0)
    }

    it("finds all") {
      vertices.filter { v: Vertex ⇒ true }.toList.size should be(6)
      vertices.filterPF { case _ ⇒ true }.toList.size should be(6)
    }

    it("finds vertices with string property") {
      vertices.filter { v: Vertex ⇒ v.property[String]("lang").exists(_ == "java") }.toList.size should be(2)
      vertices.filterPF { case v: Vertex ⇒ v.property[String]("lang").exists(_ == "java") }.toList.size should be(2)
    }

    it("finds vertices with int property") {
      vertices.filter { v: Vertex ⇒ v.property[Integer]("age").exists(_ > 30) }.toList.size should be(2)
      vertices.filterPF { case v: Vertex ⇒ v.property[Integer]("age").exists(_ > 30) }.toList.size should be(2)
    }

    it("finds that v1.out has id=2") {
      // tinkergraph uses strings for ids
      graph.v(1).out.filter { v: Vertex ⇒ v.id == "2" }.toList.size should be(1)
      graph.v(1).out.filterPF { case v: Vertex ⇒ v.id == "2" }.toList.size should be(1)
    }

    it("finds 'created' edges") {
      edges.filter { e: Edge ⇒ e.getLabel == "created" }.toList.size should be(4)
      edges.filterPF { case e: Edge ⇒ e.getLabel == "created" }.toList.size should be(4)
    }

  }

  describe("filterNot") {
    it("finds none") {
      vertices.filterNot { v: Vertex ⇒ false }.toList.size should be(6)
      vertices.filterNotPF { case _ ⇒ false }.toList.size should be(6)
    }

    it("finds all") {
      vertices.filterNot { v: Vertex ⇒ true }.toList.size should be(0)
      vertices.filterNotPF { case _ ⇒ true }.toList.size should be(0)
    }

    it("finds vertices with string property") {
      vertices.filterNot { v: Vertex ⇒ v.property[String]("lang").exists(_ == "java") }.toList.size should be(4)
      vertices.filterNotPF { case v: Vertex ⇒ v.property[String]("lang").exists(_ == "java") }.toList.size should be(4)
    }

    it("finds vertices with int property") {
      vertices.filterNot { v: Vertex ⇒ v.property[Integer]("age").exists(_ > 30) }.toList.size should be(4)
      vertices.filterNotPF { case v: Vertex ⇒ v.property[Integer]("age").exists(_ > 30) }.toList.size should be(4)
    }

    it("finds that v1.out has two vertices whose id is not 2") {
      // tinkergraph uses strings for ids
      graph.v(1).out.filterNot { v: Vertex ⇒ v.id == "2" }.toList.size should be(2)
      graph.v(1).out.filterNotPF { case v: Vertex ⇒ v.id == "2" }.toList.size should be(2)
    }

    it("finds edges other than 'created'") {
      edges.filterNot { e: Edge ⇒ e.getLabel == "created" }.toList.size should be(2)
      edges.filterNotPF { case e: Edge ⇒ e.getLabel == "created" }.toList.size should be(2)
    }

  }

}