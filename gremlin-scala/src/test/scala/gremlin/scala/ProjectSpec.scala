package gremlin.scala

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory
import org.scalatest.{Matchers, WordSpec}

class ProjectSpec extends WordSpec with Matchers {
  def graph: ScalaGraph = TinkerFactory.createModern.asScala()

  "project steps" should {
    "provide type safe result" in {
      val result = graph
        .V()
        .out("created")
        .project(_(By(Key[String]("name")))
          .and(By(__.in("created").count())))
        .toList()

      result shouldBe List(
        ("lop", 3),
        ("lop",3),
        ("lop",3),
        ("ripple", 1)
      )
    }

    "provide other type safe result" in {
      val result = graph
        .V()
        .has(Key("name").of("marko"))
        .project(_(By(__.outE().count()))
          .and(By(__.inE().count())))
        .head()

      result shouldBe (3, 0)
    }
  }
}
