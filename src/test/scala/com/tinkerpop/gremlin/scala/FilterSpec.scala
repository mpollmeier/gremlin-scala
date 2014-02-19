package com.tinkerpop.gremlin.scala

import org.scalatest.matchers.ShouldMatchers

class FilterSpec extends TestBase {

  it("filters") {
    //TODO: always convert to ScalaVertex?
    gs.V
      .filter { v: Vertex => ScalaVertex(v).value("age", default = 0) > 30 }
      .value[String]("name").toSet should be(Set("josh", "peter"))
  }

  it("dedups") {
    v(1).out.in.dedup.toList should be(v(1).out.in.toSet.toList)
  }
}
