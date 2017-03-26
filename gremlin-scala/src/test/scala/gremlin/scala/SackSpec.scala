package gremlin.scala

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory
import org.scalatest.{WordSpec, Matchers}

class SackSpec extends WordSpec with Matchers {

  "carries constant value" in new Fixture {
    graph.withSack(1f).V.sack.toList shouldBe List(1f, 1f, 1f, 1f,1f, 1f)
  }

  trait Fixture {
    val graph = TinkerFactory.createModern.asScala
    // val Name = Key[String]("name")
    // val Nickname = Key[String]("nickname")
    // val Lang = Key[String]("lang")
    // val Age = Key[Int]("age")
    // val Person = "person"
    // val Created = "created"
  }
}
