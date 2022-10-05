package gremlin.scala

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class ProjectSpec extends AnyWordSpec with Matchers {

  "projecting by two traversals" in {
    val result: (java.lang.Long, java.lang.Long) =
      graph.V()
        .has(name.of("marko"))
        .project(_(By(__().outE().count())).and(By(__().inE().count())))
        .head()

    result shouldBe (3, 0)
  }

  "projecting by property and traversal" in {
    val result: List[(String, java.lang.Long)] =
      graph.V()
        .out("created")
        .project(_(By(name)).and(By(__().in("created").count())))
        .toList()

    result shouldBe List(
      ("lop", 3),
      ("lop", 3),
      ("lop", 3),
      ("ripple", 1)
    )
  }

  def graph: ScalaGraph = TinkerFactory.createModern.asScala()
  val name = Key[String]("name")

}
