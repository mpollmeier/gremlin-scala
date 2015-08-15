package gremlin.scala

import com.thinkaurelius.titan.core.{TitanFactory, TitanGraph}
import com.thinkaurelius.titan.example.GraphOfTheGodsFactory
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import org.scalatest.FunSpec
import org.scalatest.Matchers
import shapeless.test.illTyped

case class ClassExampleWithWrongLabel(s: String,
                                      @label i: Int)

case class ClassExampleWithoutLabel(s: String,
                                    i: Int)

case class ClassExampleWithLabel(@label s: String,
                                 i: Int,
                                 l: Long,
                                 o: Option[String],
                                 seq: Seq[String],
                                 map: Map[String, String],
                                 nested: NestedClass) {
  def randomDef = ???
}

case class NestedClass(s: String)

class NoneCaseClass(s: String)

class MarshallerSpec extends FunSpec with Matchers {
  val example = ClassExampleWithLabel(
    "some string",
    Int.MaxValue,
    Long.MaxValue,
    Some("option type"),
    Seq("test1", "test2"),
    Map("key1" -> "value1", "key2" -> "value2"),
    NestedClass("nested")
  )

  it("saves a case class as a vertex with a @label annotation") {
    val graph = TinkerGraph.open.asScala
    val v = graph.addVertex(example)

    val vl = graph.V(v.id).head()
    vl.label shouldBe example.s
    vl.valueMap should contain("i" → example.i)
    vl.valueMap should contain("l" → example.l)
    vl.valueMap should contain("o" → example.o)
    vl.valueMap should contain("seq" → example.seq)
    vl.valueMap should contain("map" → example.map)
    vl.valueMap should contain("nested" → example.nested)
  }

  it("converts a Vertex into a case class") {
    val graph = TinkerGraph.open.asScala

    val v = graph.addVertex(example)
    v.toCC[ClassExampleWithLabel] shouldBe example
  }

  it("saves a case class as a vertex") {
    val graph = TinkerGraph.open.asScala
    val cc = ClassExampleWithoutLabel("text", 12)
    val v = graph.addVertex(cc)

    val vl = graph.V(v.id).head()
    vl.label shouldBe "ClassExampleWithoutLabel"
    vl.valueMap should contain("s" → cc.s)
    vl.valueMap should contain("i" → cc.i)
  }

  it("can't persist a none product type (none case class or tuple)") {
    illTyped {
      """
        val graph = TinkerGraph.open.asScala
        graph.addVertex(new NoneCaseClass("test"))
      """
    }
  }

  it("can't add a @label annotation on a none String attribute") {
    illTyped {
      """
        val graph = TinkerGraph.open.asScala
        graph.addVertex(ClassExampleWithWrongLabel("test", 42))
      """
    }
  }
}
