package gremlin.scala
import TestGraph._

class LogicalSpec extends TestBase {

  describe("and steps") {

    it("returns a vertex if both conditions are met") {
      val x = v(1).asScala

      x.start.and(
        _.out().has(Name → "lop"),
        _.out().has(Name → "vadas")
      ).values[String]("name").toSet should be(Set("marko"))
    }

    it("returns empty set if one of the conditions isn't met") {
      val x = v(1).asScala

      x.start.and(
        _.out().has(Name → "lop"),
        _.out().has(Name → "foo") // unmet condition
      ).values[String]("name").toSet should be(Set())
    }

  }

  describe("or steps") {

    it("returns a vertex if at least one condition is met") {
      val x = v(1).asScala

      x.start.or(
        _.out().has(Name → "lop"),
        _.out().has(Name → "foo") // unmet condition
      ).values[String]("name").toSet should be(Set("marko"))
    }

    it("returns empty set if none condition is met") {
      val x = v(1).asScala

      x.start.or(
        _.out().has(Name → "bar"), // unmet condition
        _.out().has(Name → "foo") // unmet condition
      ).values[String]("name").toSet should be(Set())
    }

  }

  describe("combination") {

    it("returns a vertex given and and or conditions are met") {
      val x = v(1).asScala

      x.start.or(
        _.and(
          _.out().has(Name → "lop"),
          _.out().has(Name → "vadas")
        ),
        _.out().has(Name → "foo") // unmet condition
      ).values[String]("name").toSet should be(Set("marko"))
    }

    it("returns an empty set given and and or conditions aren't met") {
      val x = v(1).asScala

      x.start.and(
        _.or(
          _.out().has(Name → "lop"),
          _.out().has(Name → "vadas")
        ),
        _.out().has(Name → "foo") // unmet condition
      ).values[String]("name").toSet should be(Set())
    }

  }

  describe("exists") {
    it("returns true if one or more elements found") {
      graph.V.exists shouldBe true
    }

    it("returns false if no elements found") {
      graph.V.filter(_ ⇒ false).exists shouldBe false
    }
  }
}
