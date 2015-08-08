package gremlin.scala

import gremlin.scala.ClassExampleWithLabel
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import org.scalatest.FunSpec
import org.scalatest.Matchers

class ArrowSyntaxSpec extends FunSpec with Matchers {

  it("add edge with syntax sugar") {
    val graph: Graph = TinkerGraph.open

    val paris = graph.addVertex("Paris")
    val london = graph.addVertex("London")

    val e = paris -- "eurostar" -> london

    e.inVertex shouldBe london
    e.outVertex shouldBe paris
  }

  it("") {
    val graph: Graph = TinkerGraph.open

    val paris = graph.addVertex("Paris")
    val london = graph.addVertex("London")

    val e = paris -- ClassExampleWithLabel(
      "some string",
      Int.MaxValue,
      Long.MaxValue,
      Some("option type"),
      Seq("test1", "test2"),
      Map("key1" -> "value1", "key2" -> "value2"),
      NestedClass("nested")
    ) -> london

    e.inVertex shouldBe london
    e.outVertex shouldBe paris
  }
}
