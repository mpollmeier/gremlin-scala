package gremlin.scala

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class LogicalSpec extends AnyWordSpec with Matchers {

  "choose" should {
    "provide simple version for if/else semantic" in new Fixture {
      graph.V()
        .choose(
          _.value(Age).is(P.gt(30)),
          onTrue = _.value(Height),
          onFalse = _.value(Shoesize)
        )
        .toSet() shouldBe Set(190,
                            176, // Michael and Steffi are >30 - take their height
                            5) // Karlotta is <=30 - take her shoesize
    }

    "provide if/elseif/else semantic" in new Fixture {
      // note: this is only if/elseif/else semantic because the PickToken is enforced to be unique
      graph.V()
        .choose(
          on = _.value(Age),
          BranchCase(34, _.value(Height)),
          BranchCase(32, _.value(Shoesize)),
          BranchOtherwise(_.value(YearOfBirth))
        )
        .toSet() shouldBe Set(190, // Michael is 34 - take his height
                            41, //Steffi is 32 - take her shoesize
                            2015) // Karlotta is case `Otherwise` - take her year of birth
    }
  }

  "coalesce provides if/elseif/else semantics" in new Fixture {
    graph.V()
      .value(Age)
      .coalesce(
        _.is(P.lt(31)).constant("young"),
        _.is(P.lt(34)).constant("old"),
        _.constant("very old")
      )
      .toList() shouldBe List("very old", "old", "young")
  }

  "branch" should {
    "execute all cases that match" in new Fixture {
      graph.V()
        .branch(
          on = _.value(Age),
          BranchCase(34, _.value(Height)),
          BranchCase(32, _.value(Shoesize)),
          BranchCase(1, _.value(YearOfBirth))
        )
        .toSet() shouldBe Set(190, // Michael is 34 - take his height
                            41, //Steffi is 32 - take her shoesize
                            2015) // Karlotta is 1 - take her year of birth
    }

    "allow to use `matchAll` semantics" in new Fixture {
      graph.V()
        .branch(
          on = _.value(Age),
          BranchCase(34, _.value(Height)),
          BranchMatchAll(_.value(YearOfBirth))
        )
        .toSet() shouldBe Set(190,
                            1983, // Michael's height (since he is 34) and year of birth
                            1984, //Steffi's year of birth
                            2015) // Karlotta's year of birth
    }
  }

  "and step returns results if all conditions are met" in new Fixture {
    graph.V()
      .and(
        _.label().is(Person),
        _.out().has(Name -> "Karlotta")
      )
      .value(Name)
      .toSet() shouldBe Set("Michael", "Steffi")
  }

  "or step returns results if at least one condition is met" in new Fixture {
    graph.V()
      .or(
        _.label().is("does not exist"),
        _.has(Age -> 34)
      )
      .value(Name)
      .toSet() shouldBe Set("Michael")
  }

  "exists" should {
    "return true if one or more elements found" in new Fixture {
      graph.V().exists() shouldBe true
    }

    "return false if no elements found" in new Fixture {
      graph.V().filter(_.has(Key("nonExistingProperty"))).exists() shouldBe false
    }
  }

  trait Fixture {
    implicit val graph = TinkerGraph.open.asScala()

    val Person = "person"
    val Name = Key[String]("name")
    val Age = Key[Int]("age")
    val Height = Key[Int]("height")
    val Shoesize = Key[Int]("shoesize")
    val YearOfBirth = Key[Int]("yearOfBirth")
    val StreetNumber = Key[Int]("streetNumber")
    val parentOf = "parentOf"
    val marriedTo = "marriedTo"

    val michael = graph + (Person, Name -> "Michael", Age -> 34, Height -> 190, Shoesize -> 44, YearOfBirth -> 1983, StreetNumber -> 3)
    val steffi = graph + (Person, Name -> "Steffi", Age -> 32, Height -> 176, Shoesize -> 41, YearOfBirth -> 1984, StreetNumber -> 3)
    val karlotta = graph + (Person, Name -> "Karlotta", Age -> 1, Height -> 90, Shoesize -> 5, YearOfBirth -> 2015, StreetNumber -> 3)
    michael <-- marriedTo --> steffi
    michael --- parentOf --> karlotta
    steffi --- parentOf --> karlotta
  }
}
