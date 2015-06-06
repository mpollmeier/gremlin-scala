package gremlin.scala

import org.scalatest.matchers.ShouldMatchers

class FilterSpec extends TestBase {

  it("filters") {
    gs.V
      .filter { _.valueOrElse("age", default = 0) > 30 }
      .values[String]("name").toSet should be(Set("josh", "peter"))
  }

  describe("dedup") {
    it("dedups") {
      v(1).out.in.dedup.toList should be(v(1).out.in.toSet.toList)
    }

    it("dedups by a given uniqueness function") {
      v(1).out.in
        .dedup().by(_.property[String]("lang").orElse(null))
        .values[String]("name").toList should be(List("marko"))
    }
  }

  //TODO redo with where step
  // describe("except") {
  //   it("emits everything but a given object") {
  //     v(1).out.except(v(2).vertex).values[String]("name")
  //       .toSet should be(Set("lop", "josh"))
  //   }

  //   it("emits everything but an 'except' list") { 
  //     v(1).out.except(List(v(2).vertex)).values[String]("name")
  //       .toSet should be(Set("lop", "josh"))
  //   }

  //   it("emits everything unless the vertex is in a given aggregate variable") {
  //     v(1).out.aggregate("x")
  //       .out.exceptVar("x")
  //       .values[String]("name").toSet should be (Set("ripple"))
  //   }

  //   it("emits everything unless a property is in a given aggregate variable") {
  //     v(1).out
  //       .aggregate("x").by(_.value[String]("name"))
  //       .out.values[String]("name").exceptVar("x")
  //       .toSet should be (Set("ripple"))
  //   }
  // }
}
