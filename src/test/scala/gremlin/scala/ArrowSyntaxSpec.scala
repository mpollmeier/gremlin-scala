package gremlin.scala

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import org.scalatest.FunSpec
import org.scalatest.Matchers
import shapeless.HNil

class ArrowSyntaxSpec extends FunSpec with Matchers {

  it("add edge with syntax sugar") {
    val graph: Graph = TinkerGraph.open

    val paris = graph.addVertex("Paris")
    val london = graph.addVertex("London")

    val e = paris -- "eurostar" --> london

    e.outVertex shouldBe paris
    e.inVertex shouldBe london
  }

  it("add edge with properties using syntax sugar") {
    val graph: Graph = TinkerGraph.open

    val paris = graph.addVertex("Paris")
    val london = graph.addVertex("London")

    val e = paris -- ("eurostar", "type" -> "WDiEdge", "weight" -> 2) --> london

    e.outVertex shouldBe paris
    e.inVertex shouldBe london
    e.value("type") shouldBe Some("WDiEdge")
    e.value("weight") shouldBe Some(2)
  }

  it("should support bidirectional connections") {
    val graph: Graph = TinkerGraph.open

    val jupiter = graph.addVertex("jupiter")
    val neptune = graph.addVertex("neptune")

    val (e0, e1) = jupiter <--> "brother" <--> neptune

    e0.outVertex shouldBe jupiter
    e0.inVertex shouldBe neptune

    e1.outVertex shouldBe neptune
    e1.inVertex shouldBe jupiter
  }

  it("should support bidirectional connections with properties") {
    val graph: Graph = TinkerGraph.open

    val jupiter = graph.addVertex("jupiter")
    val neptune = graph.addVertex("neptune")

    val (e0, e1) = jupiter <--> ("brother", "type" -> "WDiEdge", "weight" -> 2) <--> neptune

    e0.value("type") shouldBe Some("WDiEdge")
    e0.value("weight") shouldBe Some(2)

    e1.value("type") shouldBe Some("WDiEdge")
    e1.value("weight") shouldBe Some(2)
  }
}
