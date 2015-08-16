package gremlin.scala

import java.time.LocalDateTime

import gremlin.scala.schema._
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import org.scalatest.{FunSpec, Matchers}

class SchemaSpec extends FunSpec with Matchers{
  describe("a schema defines a sequence of Atoms that apply a value to build a Tuple2 and") {
    it("has important default atoms") {
      Label("software") shouldBe "label" -> "software"
      Id(1) shouldBe "id" -> 1

      Label("software") shouldBe Label.key -> "software"
      Id(1) shouldBe Id.key -> 1
    }

    it("has extension points to add new Atom definitions") {
      object Created extends Key[LocalDateTime]("created")
      val now = LocalDateTime.now()
      Created(now) shouldBe "created" -> now
      Created(now) shouldBe Created.key -> now

      object Name extends Key[String]("name")
      Name("Daniel") shouldBe "name" -> "Daniel"
      Name("Daniel") shouldBe Name.key -> "Daniel"

      val Software = Label("software")
      Software shouldBe Label.key -> "software"
      Software shouldBe Label.key -> Software.value
    }
  }

  describe("a schema with defined Atoms can be used in a Map") {
    val Software = Label("software")
    val Person = Label("person")
    val Paris = Label("Paris")
    val London = Label("London")
    val EuroStar = Label("eurostar")
    object Name extends Key[String]("name")
    object Created extends Key[Int]("created")
    object Type extends Key[String]("type")
    object Weight extends Key[Int]("weight")

    it("to create a Vertex in a Graph") {
      val g = TinkerGraph.open.asScala

      val v0 = g + Map(Name("blueprints"), Software, Created(2010))
      val v1 = g + Map(Created(2009), Name("gremlin"), Software)
      val v2 = g + Map(Software, Name("gremlinScala"))
      val v3 = g + Map(Name("mpollmeier"), Person)

      g.V.toList().size shouldBe 4
      g.V.hasLabel(Software.value).toList().size shouldBe 3
      g.V.hasLabel(Person.value).toList().size shouldBe 1

      g.V.has(Name.key).toList().size shouldBe 4
      g.V.has(Created.key).toList().size shouldBe 2

      g.asJava.close()
    }

    it("add bidirectional edge with syntax sugar") {
      val g = TinkerGraph.open.asScala

      val paris = g + Paris.value[String]
      val london = g + London.value[String]

      val (edgeParisToLondon, edgeLondonToParis) = paris <-- EuroStar.value[String] --> london

      edgeParisToLondon.asJava.inVertex shouldBe london.asJava
      edgeParisToLondon.asJava.outVertex shouldBe paris.asJava

      edgeLondonToParis.asJava.inVertex shouldBe paris.asJava
      edgeLondonToParis.asJava.outVertex shouldBe london.asJava
    }

    it("add edge with properties using syntax sugar") {
      val g = TinkerGraph.open.asScala

      val paris = g + Paris.value[String]
      val london = g + London.value[String]

      val e = paris --- Map(EuroStar, Type("WDiEdge"), Weight(2)) --> london

      e.asJava.inVertex shouldBe london.asJava
      e.asJava.outVertex shouldBe paris.asJava
      e.value(Type.key) shouldBe Some("WDiEdge")
      e.value(Weight.key) shouldBe Some(2)
    }

    it("to add left edge using syntax sugar") {
      val g = TinkerGraph.open.asScala

      val paris = g + Paris.value[String]
      val london = g + London.value[String]

      val e = paris <-- Map(EuroStar, Type("WDiEdge"), Weight(2)) --- london

      e.asJava.inVertex shouldBe paris.asJava
      e.asJava.outVertex shouldBe london.asJava
      e.value(Type.key) shouldBe Some("WDiEdge")
      e.value(Weight.key) shouldBe Some(2)

      g.asJava.close()
    }
  }
}
