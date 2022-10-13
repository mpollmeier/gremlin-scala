package gremlin.scala

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import shapeless.test.illTyped

class SchemaSpec extends AnyWordSpec with Matchers {

  "a schema with defined Keys".can {
    val Software = "software"
    val Person = "person"
    val Paris = "Paris"
    val London = "London"
    val EuroStar = "eurostar"
    object Name extends Key[String]("name")
    object Created extends Key[Int]("created")
    object Type extends Key[String]("type")
    object Weight extends Key[Int]("weight")

    "create vertices" in {
      implicit val graph: ScalaGraph = TinkerGraph.open.asScala()

      val v0 = graph + (Software, Name -> "blueprints", Created -> 2010)
      val v1 = graph + (Software, Created -> 2009, Name -> "gremlin")
      val v2 = graph + (Software, Name -> "gremlinScala")
      val v3 = graph + (Person, Name -> "mpollmeier")

      graph.V().toList().size shouldBe 4
      graph.V().hasLabel(Software).toList().size shouldBe 3
      graph.V().hasLabel(Person).toList().size shouldBe 1

      graph.V().has(Name).toList().size shouldBe 4
      graph.V().has(Created).toList().size shouldBe 2

      graph.asJava().close()
    }

    "read type safe properties" when {
      "using `value`" should {
        "support vertices" in new Fixture {
          val name = paris.value2(Name)
          val someString: String = name
          illTyped { //to ensure that there is no implicit conversion to make the above work
            """
            val i: Integer = paris.value2(Name)
          """
          }
        }

        "support edges" in new Fixture {
          val distance = rail.value2(Distance)
          val someInt: Int = distance
          illTyped { //to ensure that there is no implicit conversion to make the above work
            """
            val i: String = v.value2(Distance)
          """
          }
        }

        "support traversal" in new Fixture {
          val name = paris.out(EuroStar).value(Name).head()
          val someString: String = name //no implicit conversion, it already is a String

          val distance = paris.outE(EuroStar).value(Distance).head()
          val someInt: Int = distance //no implicit conversion, it already is an Int
        }
      }

      "using `property`" should {
        "support vertices" in new Fixture {
          val name = paris.property(Name)
          val someString: Property[String] = name
          illTyped { //to ensure that there is no implicit conversion to make the above work
            """
            val i: Property[Integer] = paris.property(Name)
          """
          }
        }

        "support edges" in new Fixture {
          val distance = rail.property(Distance)
          val someInt: Property[Int] = distance //no implicit conversion, it already is an Int
          illTyped { //to ensure that there is no implicit conversion to make the above work
            """
            val i: Property[String] = v.property(Distance)
          """
          }
        }
      }

      trait Fixture {
        val City = "city"
        object Name extends Key[String]("name")
        object Population extends Key[Int]("population")
        object Distance extends Key[Int]("distance")

        implicit val graph = TinkerGraph.open.asScala()
        val paris = graph + (City, Name -> "paris")
        val london = graph + (City, Name -> "london")
        val rail = paris --- (EuroStar, Distance -> 495) --> london
      }
    }
  }
}
