package gremlin.scala

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import org.scalatest.{ FunSpec, Matchers }

class ArrowSyntaxSpec extends FunSpec with Matchers {

  it("add edge with syntax sugar") {
    val graph = TinkerGraph.open.asScala

    val paris = graph.addVertex("Paris")
    val london = graph.addVertex("London")

    val e = paris --- "eurostar" --> london

    e.inVertex shouldBe london
    e.outVertex shouldBe paris
  }

  it("add edge with case class") {
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

  it("add bidirectional edge with case class") {
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

  it("add left edge with case class") {
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

  it("add bidirectional edge with syntax sugar") {
    val graph = TinkerGraph.open.asScala

    val paris = graph.addVertex("Paris")
    val london = graph.addVertex("London")

    val (edgeParisToLondon, edgeLondonToParis) = paris <-- "eurostar" --> london

    edgeParisToLondon.inVertex shouldBe london
    edgeParisToLondon.outVertex shouldBe paris

    edgeLondonToParis.inVertex shouldBe paris
    edgeLondonToParis.outVertex shouldBe london
  }

  it("add edge with properties using syntax sugar") {
    val graph = TinkerGraph.open.asScala

    val paris = graph.addVertex("Paris")
    val london = graph.addVertex("London")
    val Type = Key[String]("type")
    val Weight = Key[Int]("weight")

    val e = paris --- ("eurostar", Type → "WDiEdge", Weight → 2) --> london

    e.inVertex shouldBe london
    e.outVertex shouldBe paris
    e.value2(Type) shouldBe "WDiEdge"
    e.value2(Weight) shouldBe 2
  }

  it("add left edge with properties using syntax sugar", org.scalatest.Tag("foo")) {
    val graph = TinkerGraph.open.asScala
    val Type = Key[String]("type")

    val paris = graph.addVertex("Paris")
    val london = graph.addVertex("London")

    val e = paris <-- ("eurostar", Type → "WDiEdge") --- london

    e.inVertex shouldBe paris
    e.outVertex shouldBe london
    e.value2(Type) shouldBe "WDiEdge"
  }
}
