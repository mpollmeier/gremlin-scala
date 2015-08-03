package gremlin.scala

import com.thinkaurelius.titan.core.{TitanFactory, TitanGraph}
import com.thinkaurelius.titan.example.GraphOfTheGodsFactory
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import org.scalatest.FunSpec
import org.scalatest.Matchers
import shapeless.test.illTyped

case class ExampleClass(@label1 s: String,
                        i: Int,
                        l: Long,
                        o: Option[String],
                        seq: Seq[String],
                        map: Map[String, String],
                        nested: NestedClass) {
  def rendomDef = ???
}

case class NestedClass(s: String)

class NoneCaseClass(s: String)

class MarshallerSpec extends FunSpec with Matchers {
  val example = ExampleClass(
    "some string",
    Int.MaxValue,
    Long.MaxValue,
    Some("option type"),
    Seq("test1", "test2"),
    Map("key1" -> "value1", "key2" -> "value2"),
    NestedClass("nested")
  )

  it("saves a case class as a vertex") {
    val graph = TinkerGraph.open
    val v = graph.addCC(example)

    val vl = graph.V(v.id).head()
    vl.valueMap should contain("s" → example.s)
    vl.valueMap should contain("i" → example.i)
    vl.valueMap should contain("l" → example.l)
    vl.valueMap should contain("o" → example.o)
    vl.valueMap should contain("seq" → example.seq)
    vl.valueMap should contain("map" → example.map)
    vl.valueMap should contain("nested" → example.nested)
  }

  it("converts a Vertex into a case class") {
    val graph = TinkerGraph.open

    val v = graph.addCC(example)
    v.toCC[ExampleClass] shouldBe example
  }

  it("can't persist a none product type (none case class or tuple)") {
    illTyped {
      """
        val graph = TinkerGraph.open
        graph.addCC(new NoneCaseClass("test"))
      """
    }
  }
}
