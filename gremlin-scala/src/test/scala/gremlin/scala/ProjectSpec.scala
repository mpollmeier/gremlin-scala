package gremlin.scala

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory
import org.scalatest.{Matchers, WordSpec}

class ProjectSpec extends WordSpec with Matchers {

  "projecting by one thing" in {
    // val result: String = //fails
    // val result: (String) = //fails
    val result: Tuple1[String] =
      // val result =
      graph.V
        .has(name.of("marko"))
        .project(_(By(name)))
        .head
    println(result) //(marko)
    println(result.getClass) // scala.Tuple1
  }

  "projecting by two traversals" in {
    val result: (java.lang.Long, java.lang.Long) =
      graph.V
        .has(name.of("marko"))
        .project(_(By(__.outE.count)).and(By(__.inE.count)))
        .head

    result shouldBe (3, 0)
  }

  "projecting by property and traversal" in {
    val result: List[(String, java.lang.Long)] =
      graph.V
        .out("created")
        .project(_(By(name)).and(By(__.in("created").count)))
        .toList

    result shouldBe List(
      ("lop", 3),
      ("lop", 3),
      ("lop", 3),
      ("ripple", 1)
    )
  }

  def graph: ScalaGraph = TinkerFactory.createModern.asScala
  val name = Key[String]("name")

}
