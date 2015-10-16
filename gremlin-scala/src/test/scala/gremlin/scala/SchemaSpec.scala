package gremlin.scala

import java.time.LocalDateTime

import gremlin.scala.schema._
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import org.scalatest.{WordSpec, Matchers}
import collection.JavaConversions._

class SchemaSpec extends WordSpec with Matchers {
  "a schema with a sequence of Atoms that apply a value to build a Tuple2" can {
    "use some default atoms" in {
      Label("software") shouldBe "label" → "software"
      ID(1) shouldBe "id" → 1

      Label("software") shouldBe Label.key → "software"
      ID(1) shouldBe ID.key → 1
    }

    "use extension points to add new Atom definitions" in {
      object Created extends Key[LocalDateTime]("created")
      val now = LocalDateTime.now()
      Created(now) shouldBe "created" → now
      Created(now) shouldBe Created.key → now

      object Name extends Key[String]("name")
      Name("Daniel") shouldBe "name" → "Daniel"
      Name("Daniel") shouldBe Name.key → "Daniel"

      val Software = Label("software")
      Software shouldBe Label.key → "software"
      Software shouldBe Label.key → Software.value
    }
  }

  "a schema with defined Atoms" can {
    val Software = Label("software").value
    val Person = Label("person").value
    val Paris = Label("Paris").value
    val London = Label("London").value
    val EuroStar = Label("eurostar").value
    object Name extends Key[String]("name")
    object Created extends Key[Int]("created")
    object Type extends Key[String]("type")
    object Weight extends Key[Int]("weight")

    "create vertices" in {
      val g = TinkerGraph.open.asScala

      val v0 = g + (Software, Name("blueprints"), Created(2010))
      val v1 = g + (Software, Created(2009), Name("gremlin"))
      val v2 = g + (Software, Name("gremlinScala"))
      val v3 = g + (Person, Name("mpollmeier"))

      g.V.toList().size shouldBe 4
      g.V.hasLabel(Software).toList().size shouldBe 3
      g.V.hasLabel(Person).toList().size shouldBe 1

      g.V.has(Name.key).toList().size shouldBe 4
      g.V.has(Created.key).toList().size shouldBe 2

      g.asJava.close()
    }

    "add bidirectional edges" in {
      val g = TinkerGraph.open.asScala

      val paris = g + Paris
      val london = g + London

      val (edgeParisToLondon, edgeLondonToParis) = paris <-- EuroStar --> london

      edgeParisToLondon.inVertex shouldBe london
      edgeParisToLondon.outVertex shouldBe paris

      edgeLondonToParis.inVertex shouldBe paris
      edgeLondonToParis.outVertex shouldBe london
    }

    "add edges with properties" in {
      val g = TinkerGraph.open.asScala

      val paris = g + Paris
      val london = g + London

      val e = paris --- (EuroStar, Type("WDiEdge"), Weight(2)) --> london

      e.inVertex shouldBe london
      e.outVertex shouldBe paris
      e.value[String](Type.key) shouldBe "WDiEdge"
      e.value[Int](Weight.key) shouldBe 2
    }

    "add left edge with just a Label" in {
      val g = TinkerGraph.open.asScala

      val paris = g + Paris
      val london = g + London

      val e = paris <-- EuroStar --- london

      e.inVertex shouldBe paris
      e.outVertex shouldBe london

      g.close()
    }

    "add left edge with Label and Name" in {
      val g = TinkerGraph.open.asScala

      val paris = g + Paris
      val london = g + London

      val e = paris <-- (EuroStar, Name("test")) --- london

      e.inVertex shouldBe paris
      e.outVertex shouldBe london

      g.close()
    }

    "add left edge with Label, Weight and Name" in {
      val g = TinkerGraph.open.asScala

      val paris = g + Paris
      val london = g + London

      val e = paris <-- (EuroStar, (Weight(99), Name("test"))) --- london

      e.inVertex shouldBe paris
      e.outVertex shouldBe london

      g.asJava.close()
    }
  }
}
