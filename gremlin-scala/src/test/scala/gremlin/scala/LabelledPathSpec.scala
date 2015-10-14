package gremlin.scala

import org.scalatest.matchers.ShouldMatchers
import shapeless._

class LabelledPathSpec extends TestBase {

  describe("type safety for labelled steps") {

    it("includes labelled steps") {
      val path: List[Edge :: HNil] =
        v(1).outE.as("a").labelledPath.toList

      path should be(List(
        e(9).edge :: HNil,
        e(7).edge :: HNil,
        e(8).edge :: HNil
      ))
    }

    it("supports multiple steps") {
      val path: List[Vertex :: Edge :: HNil] =
        v(1).asScala.as("a").outE.as("b").labelledPath.toList

      path should be(List(
        v(1).vertex :: e(9).edge :: HNil,
        v(1).vertex :: e(7).edge :: HNil,
        v(1).vertex :: e(8).edge :: HNil
      ))
    }

    it("doesn't include unlabelled steps") {
      val path: List[Edge :: HNil] =
        v(1).outE.as("b").labelledPath.toList

      path should be(List(
        e(9).edge :: HNil,
        e(7).edge :: HNil,
        e(8).edge :: HNil
      ))
    }

    it("works without labelled steps") {
      val path: List[HNil] =
        v(1).outE.inV.labelledPath.toList

      path should be(List(
        HNil,
        HNil,
        HNil
      ))
    }

    it("supports arbitrary classes") {
      val path: List[Vertex :: Vertex :: String :: HNil] =
        v(1).asScala.as("a").out.as("b").values[String]("name").as("c").labelledPath.toList

      path should be(List(
        v(1).vertex :: v(3).vertex :: "lop" :: HNil,
        v(1).vertex :: v(2).vertex :: "vadas" :: HNil,
        v(1).vertex :: v(4).vertex :: "josh" :: HNil
      ))
    }
  }

}
