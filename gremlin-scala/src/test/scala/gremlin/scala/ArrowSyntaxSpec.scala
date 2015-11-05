package gremlin.scala

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import org.scalatest.{WordSpec, Matchers}

class ArrowSyntaxSpec extends WordSpec with Matchers {

  "adding edge with syntax sugar" in {
    val graph = TinkerGraph.open.asScala

    val paris = graph.addVertex("Paris")
    val london = graph.addVertex("London")

    val e = paris --- "eurostar" --> london

    e.inVertex shouldBe london
    e.outVertex shouldBe paris
  }

  "adding edge with case class" in {
    val graph = TinkerGraph.open.asScala

    val paris = graph.addVertex("Paris")
    val london = graph.addVertex("London")

    val e = paris --- CCWithLabelAndId(
      "some string",
      Int.MaxValue,
      Long.MaxValue,
      Some("option type"),
      Seq("test1", "test2"),
      Map("key1" → "value1", "key2" → "value2"),
      NestedClass("nested")
    ) --> london

    e.inVertex shouldBe london
    e.outVertex shouldBe paris
  }

  "adding bidirectional edge with case class" in {
    val graph = TinkerGraph.open.asScala

    val paris = graph.addVertex("Paris")
    val london = graph.addVertex("London")

    val (e0, e1) = paris <-- CCWithLabel(
      "some string",
      Long.MaxValue,
      Some("option type"),
      Seq("test1", "test2"),
      Map("key1" → "value1", "key2" → "value2"),
      NestedClass("nested")
    ) --> london

    e0.inVertex shouldBe london
    e0.outVertex shouldBe paris

    e1.inVertex shouldBe paris
    e1.outVertex shouldBe london
  }

  "adding left edge with case class" in {
    val graph = TinkerGraph.open.asScala

    val paris = graph.addVertex("Paris")
    val london = graph.addVertex("London")

    val e = paris <-- CCWithLabelAndId(
      "some string",
      Int.MaxValue,
      Long.MaxValue,
      Some("option type"),
      Seq("test1", "test2"),
      Map("key1" → "value1", "key2" → "value2"),
      NestedClass("nested")
    ) --- london

    e.inVertex shouldBe paris
    e.outVertex shouldBe london
  }

  "adding bidirectional edge with syntax sugar" in {
    val graph = TinkerGraph.open.asScala

    val paris = graph.addVertex("Paris")
    val london = graph.addVertex("London")

    val (edgeParisToLondon, edgeLondonToParis) = paris <-- "eurostar" --> london

    edgeParisToLondon.inVertex shouldBe london
    edgeParisToLondon.outVertex shouldBe paris

    edgeLondonToParis.inVertex shouldBe paris
    edgeLondonToParis.outVertex shouldBe london
  }

  "adding edge with properties using syntax sugar" in {
    val graph = TinkerGraph.open.asScala

    val paris = graph.addVertex("Paris")
    val london = graph.addVertex("London")
    val Type = Key[String]("type")

    val e = paris --- ("eurostar", Type → "WDiEdge") --> london

    e.inVertex shouldBe london
    e.outVertex shouldBe paris
    e.value2(Type) shouldBe "WDiEdge"
  }

  "adding left edge with properties using syntax sugar" in {
    val graph = TinkerGraph.open.asScala
    val Type = Key[String]("type")

    val paris = graph.addVertex("Paris")
    val london = graph.addVertex("London")

    val e = paris <-- ("eurostar", Type → "WDiEdge") --- london

    e.inVertex shouldBe paris
    e.outVertex shouldBe london
    e.value2(Type) shouldBe "WDiEdge"
  }

  "multiple properties" in {
    val graph = TinkerGraph.open.asScala
    val Type = Key[String]("type")
    val Weight = Key[Int]("weight")

    val paris = graph.addVertex("Paris")
    val london = graph.addVertex("London")
    // TODO: get same behaviour for all cases
    val e1 = paris --- ("eurostar", Type → "value1", Weight → 1) --> london
    val e2 = paris <-- ("eurostar", (Type → "value2", Weight → 2)) --- london
    // val e3 = paris <-- ("eurostar", (Type → "value3", Weight → 3)) --> london

    e1.value2(Type) shouldBe "value1"
    e1.value2(Weight) shouldBe 1
    e2.value2(Type) shouldBe "value2"
    e2.value2(Weight) shouldBe 2
    // e3.value2(Type) shouldBe "value3"
    // e3.value2(Weight) shouldBe 3
  }
}
