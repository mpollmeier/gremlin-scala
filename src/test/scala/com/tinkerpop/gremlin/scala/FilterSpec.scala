package com.tinkerpop.gremlin.scala

import org.scalatest.matchers.ShouldMatchers

class FilterSpec extends TestBase {

  describe("filter") {
    it("filters") {
      //TODO: always convert to ScalaVertex?
      gs.V
        .filter { v: Vertex => ScalaVertex(v).value("age", default = 0) > 30 }
        .value[String]("name").toSet should be(Set("josh", "peter"))
    }
  }
}
