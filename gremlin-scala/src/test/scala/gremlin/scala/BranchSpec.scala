package gremlin.scala

import org.apache.tinkerpop.gremlin.process.traversal.P
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import org.scalatest.{WordSpec, Matchers}
import scala.util.Random

class BranchSpec extends WordSpec with Matchers {

  "choose is a special (simple) version of branch for boolean logic" in new Fixture {
    graph.V.hasLabel(Person)
      .choose(
        _.value(Age).is(P.gt(30)),
        onTrue = _.value(Height),
        onFalse = _.value(Shoesize)
      ).toSet shouldBe Set(
        190, 176, // Michael and Steffi are >30 - take their height
        5) // Karlotta is <=30 - take her shoesize
  }

  // "" in new Fixture {
  //   // graph.withSack(1d).V.sack.toList shouldBe List(1d, 1d, 1d, 1d,1d, 1d)
  // }

  trait Fixture {
    val graph = TinkerGraph.open.asScala

    val Person = "person"
    val Name = Key[String]("name")
    val Age = Key[Int]("age")
    val Height = Key[Int]("height")
    val Shoesize = Key[Int]("shoesize")

    graph + (Person, Name -> "Michael", Age -> 34, Height -> 190, Shoesize -> 44)
    graph + (Person, Name -> "Steffi", Age -> 32, Height -> 176, Shoesize -> 41)
    graph + (Person, Name -> "Karlotta", Age -> 1, Height -> 90, Shoesize -> 5)
  }
}
