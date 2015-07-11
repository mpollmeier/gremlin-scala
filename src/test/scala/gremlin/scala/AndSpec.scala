package gremlin.scala

class AndSpec extends TestBase {

  describe("and steps") {

    it("returns a vertex if both conditions are met") {
      val x = v(1)

      x.and(
        x.out().has("name", "lop"),
        x.out().has("name", "vadas")
      ).values[String]("name").toSet should be(Set("marko"))
    }

    it("returns empty set if one of the conditions isn't met") {
      val x = v(1)

      x.and(
        x.out().has("name", "lop"),
        x.out().has("name", "foo") // unmet condition
      ).values[String]("name").toSet should be(Set())
    }

  }
}
