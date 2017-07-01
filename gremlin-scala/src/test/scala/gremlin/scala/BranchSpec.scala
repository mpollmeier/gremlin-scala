package gremlin.scala

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import org.scalatest.{WordSpec, Matchers}

class BranchSpec extends WordSpec with Matchers {

  "choose" should {
    "provide simple version for if/else semantic" in new Fixture {
      graph.V
        .choose(
          _.value(Age).is(P.gt(30)),
          onTrue = _.value(Height),
          onFalse = _.value(Shoesize)
        ).toSet shouldBe Set(
          190, 176, // Michael and Steffi are >30 - take their height
          5) // Karlotta is <=30 - take her shoesize
    }

    "provide if/elseif/else semantic" in new Fixture {
      graph.V
        .choose(
          on = _.value(Age),
          BranchCase(34, _.value(Height)),
          BranchCase(32, _.value(Shoesize)),
          BranchOtherwise(_.value(YearOfBirth))
        ).toSet shouldBe Set(
          190,  // Michael is 34 - take his height
          41,   //Steffi is 32 - take her shoesize
          2015) // Karlotta is case `Otherwise` - take her year of birth
    }
  }

  "branch" should {
    "execute all cases that match" in new Fixture {
      graph.V
        .branch(
          on = _.value(Age),
          BranchCase(34, _.value(Height)),
          BranchCase(32, _.value(Shoesize)),
          BranchCase(1, _.value(YearOfBirth))
        ).toSet shouldBe Set(
          190,  // Michael is 34 - take his height
          41,   //Steffi is 32 - take her shoesize
          2015) // Karlotta is 1 - take her year of birth
    }

    "allow to use `matchAll` semantics" in new Fixture {
      graph.V
        .branch(
          on = _.value(Age),
          BranchCase(34, _.value(Height)),
          BranchMatchAll(_.value(YearOfBirth))
        ).toSet shouldBe Set(
          190, 1983, // Michael's height (since he is 34) and year of birth
          1984,      //Steffi's year of birth
          2015)      // Karlotta's year of birth
    }
  
  }

  trait Fixture {
    val graph = TinkerGraph.open.asScala

    val Person = "person"
    val Name = Key[String]("name")
    val Age = Key[Int]("age")
    val Height = Key[Int]("height")
    val Shoesize = Key[Int]("shoesize")
    val YearOfBirth = Key[Int]("yearOfBirth")
    val StreetNumber = Key[Int]("streetNumber")

    graph + (Person, Name -> "Michael",  Age -> 34, Height -> 190, Shoesize -> 44, YearOfBirth -> 1983, StreetNumber -> 3)
    graph + (Person, Name -> "Steffi",   Age -> 32, Height -> 176, Shoesize -> 41, YearOfBirth -> 1984, StreetNumber -> 3)
    graph + (Person, Name -> "Karlotta", Age -> 1,  Height -> 90,  Shoesize -> 5,  YearOfBirth -> 2015, StreetNumber -> 3)
  }
}
