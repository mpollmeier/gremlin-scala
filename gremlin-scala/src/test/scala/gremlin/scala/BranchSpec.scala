package gremlin.scala

import org.apache.tinkerpop.gremlin.process.traversal.P
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import org.scalatest.{WordSpec, Matchers}
import scala.util.Random

class BranchSpec extends WordSpec with Matchers {

  "choose is a special (simple) version of branch for boolean logic" in new Fixture {
    graph.V
      .choose(
        _.value(Age).is(P.gt(30)),
        onTrue = _.value(Height),
        onFalse = _.value(Shoesize)
      ).toSet shouldBe Set(
        190, 176, // Michael and Steffi are >30 - take their height
        5) // Karlotta is <=30 - take her shoesize
  }

  "branch allows for switch case logic" in new Fixture {
    graph.V
      .branch(
        on = _.value(Age),
        BranchCase(34, _.value(Height)),
        BranchCase(32, _.value(Shoesize)),
        BranchOtherwise(_.value(YearOfBirth))
      ).toSet shouldBe Set(
        190,  // Michael is 34 - take his height
        41,   //Steffi is 32 - take her shoesize
        2015) // Karlotta is the case `Otherwise` - take her year of birth
  }

  trait Fixture {
    val graph = TinkerGraph.open.asScala

    val Person = "person"
    val Name = Key[String]("name")
    val Age = Key[Int]("age")
    val Height = Key[Int]("height")
    val Shoesize = Key[Int]("shoesize")
    val YearOfBirth = Key[Int]("yearOfBirth")

    graph + (Person, Name -> "Michael",  Age -> 34, Height -> 190, Shoesize -> 44, YearOfBirth -> 1983)
    graph + (Person, Name -> "Steffi",   Age -> 32, Height -> 176, Shoesize -> 41, YearOfBirth -> 1984)
    graph + (Person, Name -> "Karlotta", Age -> 1,  Height -> 90,  Shoesize -> 5,  YearOfBirth -> 2015)
  }
}
