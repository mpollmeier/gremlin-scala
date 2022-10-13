package gremlin.scala

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import gremlin.scala.*
import gremlin.scala.given

class ArrowSyntaxSpec extends AnyWordSpec with Matchers {

  "A --> B creates an edge".which {

    "has a label" in new Fixture {
      paris --- Eurostar --> london

      paris.out(Eurostar).head() shouldBe london
    }

    "has a label and one property" in new Fixture {
      paris --- (Eurostar, Name -> "alpha") --> london

      paris.out(Eurostar).head() shouldBe london
      paris.outE(Eurostar).value(Name).head() shouldBe "alpha"
    }

    "has a label and multiple properties" in new Fixture {
      paris --- (Eurostar, Name -> "alpha", Length -> 100) --> london

      paris.out(Eurostar).head() shouldBe london
      paris.outE(Eurostar).value(Name).head() shouldBe "alpha"
      paris.outE(Eurostar).value(Length).head() shouldBe 100
    }

    "has a label and multiple properties as Map " in new Fixture {
      paris --- (Eurostar, properties) --> london
      paris.out(Eurostar).head() shouldBe london
      paris.outE(Eurostar).value(Name).head() shouldBe "alpha"
      paris.outE(Eurostar).value(Length).head() shouldBe 100
    }
  }

  "A <-- B creates an edge".which {
    "has a label" in new Fixture {
      paris <-- Eurostar --- london
      london.out(Eurostar).head() shouldBe paris
    }

    "has a label and one property" in new Fixture {
      paris <-- (Eurostar, Name -> "alpha") --- london
      paris.in(Eurostar).head() shouldBe london
      paris.inE(Eurostar).value(Name).head() shouldBe "alpha"
    }

    "has a label and multiple properties" in new Fixture {
      paris <-- (Eurostar, Name -> "alpha", Length -> 100) --- london

      paris.in(Eurostar).head() shouldBe london
      paris.inE(Eurostar).value(Name).head() shouldBe "alpha"
      paris.inE(Eurostar).value(Length).head() shouldBe 100
    }

    /*   "has a label and multiple properties as Map" in new Fixture {
      paris <-- (Eurostar, properties) --- london

      paris.in(Eurostar).head shouldBe london
      paris.inE(Eurostar).value(Name).head shouldBe "alpha"
      paris.inE(Eurostar).value(Length).head shouldBe 100
    }*/
  }

  "A <--> B create edges".which {
    "have labels" in new Fixture {
      paris <-- Eurostar --> london

      paris.out(Eurostar).head() shouldBe london
      london.out(Eurostar).head() shouldBe paris
    }

    "have labels and one property" in new Fixture {
      paris <-- (Eurostar, Name -> "alpha") --> london

      paris.out(Eurostar).head() shouldBe london
      paris.outE(Eurostar).value(Name).head() shouldBe "alpha"
      paris.in(Eurostar).head() shouldBe london
      paris.inE(Eurostar).value(Name).head() shouldBe "alpha"
    }

    "have labels and multiple properties" in new Fixture {
      paris <-- (Eurostar, Name -> "alpha", Length -> 100) --> london

      paris.out(Eurostar).head() shouldBe london
      paris.outE(Eurostar).value(Name).head() shouldBe "alpha"
      paris.outE(Eurostar).value(Length).head() shouldBe 100
      paris.in(Eurostar).head() shouldBe london
      paris.inE(Eurostar).value(Name).head() shouldBe "alpha"
      paris.inE(Eurostar).value(Length).head() shouldBe 100
    }

    /*    "have labels and multiple properties as Map" in new Fixture {
      paris <-- (Eurostar, properties) --> london

      paris.out(Eurostar).head shouldBe london
      paris.outE(Eurostar).value(Name).head shouldBe "alpha"
      paris.outE(Eurostar).value(Length).head shouldBe 100
      paris.in(Eurostar).head shouldBe london
      paris.inE(Eurostar).value(Name).head shouldBe "alpha"
      paris.inE(Eurostar).value(Length).head shouldBe 100
    }*/
  }

  // TODO: case class support
  // "adding edge with case class" in {
  //   val graph = TinkerGraph.open.asScala

  //   val paris = graph.addVertex("Paris")
  //   val london = graph.addVertex("London")

  //   val e = paris --- CCWithLabelAndId(
  //     "some string",
  //     Int.MaxValue,
  //     Long.MaxValue,
  //     Some("option type"),
  //     Seq("test1", "test2"),
  //     Map("key1" -> "value1", "key2" -> "value2"),
  //     NestedClass("nested")
  //   ) --> london

  //   e.inVertex shouldBe london
  //   e.outVertex shouldBe paris
  // }

  // "adding bidirectional edge with case class" in {
  //   val graph = TinkerGraph.open.asScala

  //   val paris = graph.addVertex("Paris")
  //   val london = graph.addVertex("London")

  //   val (e0, e1) = paris <-- CCWithLabel(
  //     "some string",
  //     Long.MaxValue,
  //     Some("option type"),
  //     Seq("test1", "test2"),
  //     Map("key1" -> "value1", "key2" -> "value2"),
  //     NestedClass("nested")
  //   ) --> london

  //   e0.inVertex shouldBe london
  //   e0.outVertex shouldBe paris

  //   e1.inVertex shouldBe paris
  //   e1.outVertex shouldBe london
  // }

  // "adding left edge with case class" in {
  //   val graph = TinkerGraph.open.asScala

  //   val paris = graph.addVertex("Paris")
  //   val london = graph.addVertex("London")

  //   val e = paris <-- CCWithLabelAndId(
  //     "some string",
  //     Int.MaxValue,
  //     Long.MaxValue,
  //     Some("option type"),
  //     Seq("test1", "test2"),
  //     Map("key1" -> "value1", "key2" -> "value2"),
  //     NestedClass("nested")
  //   ) --- london

  //   e.inVertex shouldBe paris
  //   e.outVertex shouldBe london
  // }

  trait Fixture {
    implicit val graph: ScalaGraph = TinkerGraph.open.asScala()
    val paris: Vertex = graph + "Paris"
    val london = graph + "London"

    val Eurostar = "eurostar" //edge label

    val Name: Key[String] = Key[String]("name")
    val Length: Key[Int] = Key[Int]("length")

    val properties: Map[String, Any] =
      List(("name", "alpha"), ("length", 100)).toMap
  }
}
