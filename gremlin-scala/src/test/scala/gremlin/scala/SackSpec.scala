package gremlin.scala

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory
import org.scalatest.{WordSpec, Matchers}
import scala.util.Random

class SackSpec extends WordSpec with Matchers {

  "carries simple value" when {
    "providing constant" in new Fixture {
      graph.withSack(1f).V.sack.toList shouldBe List(1f, 1f, 1f, 1f,1f, 1f)
    }

    "providing function" in new Fixture {
      graph.withSack(() => 1f).V.sack.toList shouldBe List(1f, 1f, 1f, 1f,1f, 1f)

      val randomValues = graph.withSack(() => Random.nextFloat).V.sack.toList
      randomValues.toSet.size shouldBe 6
    }
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
