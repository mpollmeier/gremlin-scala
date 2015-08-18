package gremlin.scala

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import org.scalatest.FunSpec
import org.scalatest.Matchers
import shapeless.test.illTyped


case class CCWithoutLabelOrId(s: String,
                              i: Int)

case class CCWithOptionId(s: String,
                          @id id: Option[Int])

case class CCWithLabel(s: String,
                       l: Long,
                       o: Option[String],
                       seq: Seq[String],
                       map: Map[String, String],
                       nested: NestedClass)

@label("the label")
case class CCWithLabelAndId(s: String,
                            @id id: Int,
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
  val example = CCWithLabelAndId(
    "some string",
    Int.MaxValue,
    Long.MaxValue,
    Some("option type"),
    Seq("test1", "test2"),
    Map("key1" -> "value1", "key2" -> "value2"),
    NestedClass("nested")
  )

  it("saves a case class as a vertex with a @label and @id annotation") {
    val graph = TinkerGraph.open.asScala
    val v = graph.addVertex(example)

    val vl = graph.V(v.id).head()
    vl.label shouldBe "the label"
    vl.id shouldBe example.id
    vl.valueMap should contain("s" → example.s)
    vl.valueMap should contain("l" → example.l)
    vl.valueMap should contain("o" → example.o)
    vl.valueMap should contain("seq" → example.seq)
    vl.valueMap should contain("map" → example.map)
    vl.valueMap should contain("nested" → example.nested)
  }

  it("load a vertex into a case class") {
    val graph = TinkerGraph.open.asScala

    val v = graph.addVertex(example)
    v.toCC[CCWithLabelAndId] shouldBe example
  }

  it("saves a case class with Option @id annotation") {
    val graph = TinkerGraph.open.asScala
    val cc = CCWithOptionId("text", Some(12))
    val v = graph.addVertex(cc)

    val vl = graph.V(v.id).head()
    vl.label shouldBe cc.getClass.getSimpleName
    vl.id shouldBe cc.id.get
    vl.valueMap should contain("s" → cc.s)
  }

  it("load a vertex into a case class with Option @id annotation") {
    val graph = TinkerGraph.open.asScala
    val cc = CCWithOptionId("text", Some(12))
    val v = graph.addVertex(cc)

    v.toCC[CCWithOptionId] shouldBe cc
  }

  it("saves a case class without annotation as a vertex") {
    val graph = TinkerGraph.open.asScala
    val cc = CCWithoutLabelOrId("text", 12)
    val v = graph.addVertex(cc)

    val vl = graph.V(v.id).head()
    vl.label shouldBe cc.getClass.getSimpleName
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
}
