package gremlin.scala

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory
import org.scalatest.{WordSpec, Matchers}
import scala.util.Random

class BranchSpec extends WordSpec with Matchers {

  "choose is a special (simple) version of branch for boolean logic" in new Fixture {
    /* TODO: choose should take a traversal => Boolean */
    graph.V.hasLabel("person")
      .choose(
        _.value2(Age) <= 30,
        onTrue = _.in(),
        onFalse = _.out()
      ).value(Name)
      .toSet shouldBe Set("marko", "ripple", "lop", "lop")
  }

  // "" in new Fixture {
  //   // graph.withSack(1d).V.sack.toList shouldBe List(1d, 1d, 1d, 1d,1d, 1d)
  // }

  trait Fixture {
    val graph = TinkerFactory.createModern.asScala
    val Age = Key[Int]("age")

    val Name = Key[String]("name")
    val Height = Key[Int]("height")
    val Shoesize = Key[Int]("shoesize")
  }
}
