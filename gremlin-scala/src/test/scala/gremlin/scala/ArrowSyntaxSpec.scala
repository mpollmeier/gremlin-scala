package gremlin.scala

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import org.scalatest.{FunSpec, Matchers}

class ArrowSyntaxSpec extends FunSpec with Matchers {

  it("add edge with syntax sugar") {
    val graph = TinkerGraph.open.asScala

    val paris = graph.addVertex("Paris")
    val london = graph.addVertex("London")

    val e = paris --- "eurostar" --> london

    e.asJava.inVertex shouldBe london.asJava
    e.asJava.outVertex shouldBe paris.asJava
  }

  it("add edge with case class") {
    val graph = TinkerGraph.open.asScala

    val paris = graph.addVertex("Paris")
    val london = graph.addVertex("London")

    val e = paris --- ClassExampleWithLabel(
      "some string",
      Int.MaxValue,
      Long.MaxValue,
      Some("option type"),
      Seq("test1", "test2"),
      Map("key1" -> "value1", "key2" -> "value2"),
      NestedClass("nested")
    ) --> london

    e.asJava.inVertex shouldBe london.asJava
    e.asJava.outVertex shouldBe paris.asJava
  }

  it("add bidirectional edge with syntax sugar") {
    val graph = TinkerGraph.open.asScala

    val paris = graph.addVertex("Paris")
    val london = graph.addVertex("London")

    val (edgeParisToLondon, edgeLondonToParis) = paris <-- "eurostar" --> london

    edgeParisToLondon.asJava.inVertex shouldBe london.asJava
    edgeParisToLondon.asJava.outVertex shouldBe paris.asJava

    edgeLondonToParis.asJava.inVertex shouldBe paris.asJava
    edgeLondonToParis.asJava.outVertex shouldBe london.asJava
  }

  it("add edge with properties using syntax sugar") {
    val graph = TinkerGraph.open.asScala

    val paris = graph.addVertex("Paris")
    val london = graph.addVertex("London")

    val e = paris --- ("eurostar", Map("type" -> "WDiEdge", "weight" -> 2)) --> london

    e.asJava.inVertex shouldBe london.asJava
    e.asJava.outVertex shouldBe paris.asJava
    e.value("type") shouldBe Some("WDiEdge")
    e.value("weight") shouldBe Some(2)
  }

  it("add left edge with properties using syntax sugar") {
    val graph = TinkerGraph.open.asScala

    val paris = graph.addVertex("Paris")
    val london = graph.addVertex("London")

    val e = paris <-- ("eurostar", Map("type" -> "WDiEdge", "weight" -> 2)) --- london

    e.asJava.inVertex shouldBe paris.asJava
    e.asJava.outVertex shouldBe london.asJava
    e.value("type") shouldBe Some("WDiEdge")
    e.value("weight") shouldBe Some(2)
  }
}
