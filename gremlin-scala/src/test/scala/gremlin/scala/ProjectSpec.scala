package gremlin.scala

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory
import org.scalatest.{Matchers, WordSpec}
import shapeless.HNil

class ProjectSpec extends WordSpec with Matchers {
  def graph: ScalaGraph = TinkerFactory.createModern.asScala()

  "project steps" should {
    "provide type safe result" in {
      val result = graph
        .V()
        .out("created")
        .project(_(_.value(Key[String]("name")))
          .and(_.in("created").count()))
        .toList()

      result shouldBe List(
        "lop" :: 3 :: HNil,
        "lop" :: 3 :: HNil,
        "lop" :: 3 :: HNil,
        "ripple" :: 1 :: HNil
      )
    }

    "provide other type safe result" in {
      val result = graph
        .V()
        .has(Key("name").of("marko"))
        .project(_(_.outE().count())
          .and(_.inE().count()))
        .head()

      result shouldBe (3 :: 0 :: HNil)
    }
  }
}
