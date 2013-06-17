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

@RunWith(classOf[JUnitRunner])
class FilterStepTest extends FunSpec with ShouldMatchers {
  val g = TinkerGraphFactory.createTinkerGraph

  describe("filter") {
    it("finds none") {
      g.V.filter { v: Vertex ⇒ false }.toList.size should be(0)
    }

    it("finds all") {
      g.V.filter { v: Vertex ⇒ true }.toList.size should be(6)
    }

    it("finds vertices with string property") {
      g.V.filter { v: Vertex ⇒ v.get[String]("lang").exists(_ == "java") }.toList.size should be(2)
    }

    it("finds vertices with int property") {
      g.V.filter { v: Vertex ⇒ v.get[Int]("age").exists(_ > 30) }.toList.size should be(2)
    }
  }

  describe("filterNot") {
    it("finds none") {
      g.V.filterNot { v: Vertex ⇒ false }.toList.size should be(6)
    }

    it("finds all") {
      g.V.filterNot { v: Vertex ⇒ true }.toList.size should be(0)
    }

    it("finds vertices with string property") {
      g.V.filterNot { v: Vertex ⇒ v.get[String]("lang").exists(_ == "java") }.toList.size should be(4)
    }

    it("finds vertices with int property") {
      g.V.filterNot { v: Vertex ⇒ v.get[Int]("age").exists(_ > 30) }.toList.size should be(4)
    }
  }

}