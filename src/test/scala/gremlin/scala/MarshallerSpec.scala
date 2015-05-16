package gremlin.scala

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers

case class ExampleClass(s: String, i: Int, l: Long, o: Option[String])

class MarshallerSpec extends FunSpec with ShouldMatchers {
  val example = ExampleClass("some string", Int.MaxValue, Long.MaxValue, Some("option type"))

  it("saves a case class as a vertex") {
    val graph = TinkerGraph.open
    val gs = GremlinScala(graph)

    val v = gs.save(example)

    v.valueMap should contain ("s" → example.s)
    v.valueMap should contain ("i" → example.i)
    v.valueMap should contain ("l" → example.l)
    v.valueMap should contain ("o" → example.o.get)

    v shouldBe ScalaVertex(gs.V.toList.head)
  }

  it("converts a Vertex into a case class") {
    val graph = TinkerGraph.open
    val gs = GremlinScala(graph)

    val v = gs.save(example)

    v.start.load[ExampleClass].head shouldBe example
  }
}
