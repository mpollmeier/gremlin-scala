package com.tinkerpop.gremlin.scala

import org.scalatest.matchers.ShouldMatchers

class FilterSpec extends TestBase {

  it("filters") {
    //TODO: always convert to ScalaVertex/Edge where possible?
    gs.V
      .filter { v: Vertex => ScalaVertex(v).value("age", default = 0) > 30 }
      .value[String]("name").toSet should be(Set("josh", "peter"))
  }

  describe("dedup") {
    it("dedups") {
      v(1).out.in.dedup.toList should be(v(1).out.in.toSet.toList)
    }

    it("dedups by a given uniqueness function") {
      v(1).out.in
        .dedup(v => ScalaVertex(v).property[String]("lang").orElse(null))
        .value[String]("name").toList should be(List("josh"))
    }
  }

  describe("except") {
    it("emits everything but a given object") {
      v(1).out.except(v(2).vertex).value[String]("name")
        .toSet should be(Set("lop", "josh"))
    }

    it("emits everything but an 'except' list") { 
      v(1).out.except(List(v(2).vertex)).value[String]("name")
        .toSet should be(Set("lop", "josh"))
    }

    it("emits everything unless the vertex is in a given aggregate variable") {
      v(1).out.aggregate("x")
        .out.exceptVar("x")
        .value[String]("name").toSet should be (Set("ripple"))
    }

    it("emits everything unless a property is in a given aggregate variable") {
      v(1).out
        .aggregate("x", v => ScalaVertex(v).value[String]("name"))
        .out.value[String]("name").exceptVar("x")
        .toSet should be (Set("ripple"))
    }
  }
}
