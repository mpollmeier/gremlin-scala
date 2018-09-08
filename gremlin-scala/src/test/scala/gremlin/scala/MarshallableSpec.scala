package gremlin.scala

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import org.scalatest.WordSpec
import org.scalatest.Matchers
import shapeless.test.illTyped

case class CCSimple(s: String, i: Int)

case class MyValueClass(value: Int) extends AnyVal
case class CCWithValueClass(s: String, i: MyValueClass)
case class CCWithOptionValueClass(s: String, i: Option[MyValueClass])

case class CCWithOption(i: Int, s: Option[String])

case class CCWithOptionId(s: String, @id id: Option[Int])
case class CCWithOptionIdNested(s: String, @id id: Option[Int], i: MyValueClass)

@label("label_a")
case class CCWithLabel(s: String)

@label("the_label")
case class ComplexCC(
    s: String,
    l: Long,
    o: Option[String],
    seq: Seq[String],
    map: Map[String, String],
    nested: NestedClass
) { def randomDef = ??? }

case class NestedClass(s: String)

class NoneCaseClass(s: String)

class MarshallableSpec extends WordSpec with Matchers {

  "marshals / unmarshals case classes".which {

    "only have simple members" in new Fixture {
      val cc = CCSimple("text", 12)
      val v = graph + cc

      val vl = graph.V(v.id).head
      vl.label shouldBe cc.getClass.getSimpleName
      vl.valueMap should contain("s" -> cc.s)
      vl.valueMap should contain("i" -> cc.i)
    }

    "contain options" should {
      "map `Some[A]` to `A`" in new Fixture {
        val ccWithOptionSome =
          CCWithOption(Int.MaxValue, Some("optional value"))
        val v = graph + ccWithOptionSome
        v.toCC[CCWithOption] shouldBe ccWithOptionSome

        val vl = graph.V(v.id).head
        vl.value[String]("s") shouldBe ccWithOptionSome.s.get
      }

      "map `None` to `null`" in new Fixture {
        val ccWithOptionNone = CCWithOption(Int.MaxValue, None)
        val v = graph + ccWithOptionNone
        v.toCC[CCWithOption] shouldBe ccWithOptionNone

        val vl = graph.V(v.id).head
        vl.keys should not contain "s" //None should be mapped to `null`
      }

      // Background: if we marshal Option types, the graph db needs to understand scala.Option,
      // which wouldn't make any sense. So we rather translate it to `null` if it's `None`.
      // https://github.com/mpollmeier/gremlin-scala/issues/98
    }

    "contain value classes" should {
      "unwrap a plain value class" in new Fixture {
        val cc = CCWithValueClass("some text", MyValueClass(42))
        val v = graph + cc

        val vl = graph.V(v.id).head
        vl.label shouldBe cc.getClass.getSimpleName
        vl.valueMap should contain("s" -> cc.s)
        vl.valueMap should contain("i" -> cc.i.value)
        vl.toCC[CCWithValueClass] shouldBe cc
      }

      "unwrap an optional value class" in new Fixture {
        val cc = CCWithOptionValueClass("some text", Some(MyValueClass(42)))
        val v = graph + cc

        val vl = graph.V(v.id).head
        vl.label shouldBe cc.getClass.getSimpleName
        vl.valueMap should contain("s" -> cc.s)
        vl.valueMap should contain("i" -> cc.i.get.value)
        vl.toCC[CCWithOptionValueClass] shouldBe cc
      }

      "handle None value class" in new Fixture {
        val cc = CCWithOptionValueClass("some text", None)
        val v = graph + cc

        val vl = graph.V(v.id).head
        vl.label shouldBe cc.getClass.getSimpleName
        vl.valueMap should contain("s" -> cc.s)
        vl.valueMap.keySet should not contain ("i")
        vl.toCC[CCWithOptionValueClass] shouldBe cc
      }
    }

    "define their custom marshaller" in new Fixture {
      val ccWithOptionNone = CCWithOption(Int.MaxValue, None)

      val marshaller = new Marshallable[CCWithOption] {
        def fromCC(cc: CCWithOption) =
          FromCC(None, "CCWithOption", Map("i" -> cc.i, "s" -> cc.s.getOrElse("undefined")))

        def toCC(id: AnyRef, valueMap: Map[String, Any]): CCWithOption =
          CCWithOption(i = valueMap("i").asInstanceOf[Int],
                       s = valueMap.get("s").asInstanceOf[Option[String]])
      }

      val v = graph.+(ccWithOptionNone)(marshaller)
      v.toCC[CCWithOption](marshaller) shouldBe CCWithOption(ccWithOptionNone.i, Some("undefined"))
    }

    "use @label and @id annotations" in new Fixture {
      val cc = ComplexCC(
        "some string",
        Long.MaxValue,
        Some("option type"),
        Seq("test1", "test2"),
        Map("key1" -> "value1", "key2" -> "value2"),
        NestedClass("nested")
      )

      val v = graph + cc

      val vl = graph.V(v.id).head
      vl.label shouldBe "the_label"
      vl.valueMap should contain("s" -> cc.s)
      vl.valueMap should contain("l" -> cc.l)
      vl.valueMap should contain("o" -> cc.o.get)
      vl.valueMap should contain("seq" -> cc.seq)
      vl.valueMap should contain("map" -> cc.map)
      vl.valueMap should contain("nested" -> cc.nested)
    }

    "have an Option @id annotation" in new Fixture {
      val cc = CCWithOptionId(s = "text", id = Some(12))
      val v = graph + cc

      v.toCC[CCWithOptionId].s shouldBe cc.s

      val vl = graph.V(v.id).head
      vl.label shouldBe cc.getClass.getSimpleName
      vl.valueMap should contain("s" -> cc.s)
    }

  }

  "find vertices by label" in new Fixture {
    val ccSimple = CCSimple("a string", 42)
    val ccWithOption = CCWithOption(52, Some("other string"))
    val ccWithLabel = CCWithLabel("s")

    graph + ccSimple
    graph + ccWithOption
    graph + ccWithLabel

    graph.V.count.head shouldBe 3

    val ccSimpleVertices = graph.V.hasLabel[CCSimple].toList
    (ccSimpleVertices should have).size(1)
    ccSimpleVertices.head.toCC[CCSimple] shouldBe ccSimple

    val ccWithLabelVertices = graph.V.hasLabel[CCWithLabel].toList
    (ccWithLabelVertices should have).size(1)
    ccWithLabelVertices.head.toCC[CCWithLabel] shouldBe ccWithLabel
  }

  "add edges using case-class".which {
    "have no id-annotation" in new CCEdgeAddFixture {
      val ccEdgeWithLabelInitial = CCWithLabel("edge-property")

      ccVertexFrom.addEdge(ccVertexTo, ccEdgeWithLabelInitial).toCC[CCWithLabel]

      val ccEdgesWithLabel = graph.E.hasLabel[CCWithLabel].toList
      (ccEdgesWithLabel should have).size(1)
      ccEdgesWithLabel.head.toCC[CCWithLabel] shouldBe ccEdgeWithLabelInitial
    }

    "have id-annotation None" in new CCEdgeAddFixture {
      val ccEdgeWithOptionIdNoneInitial = CCWithOptionId("edge-property", None)

      val ccEdgeWithOptionIdNone = ccVertexFrom
        .addEdge(ccVertexTo, ccEdgeWithOptionIdNoneInitial)
        .toCC[CCWithOptionId]
      ccEdgeWithOptionIdNone.id should not be empty

      val ccEdgesWithOptionIdNone = graph.E.hasLabel[CCWithOptionId].toList
      (ccEdgesWithOptionIdNone should have).size(1)
      ccEdgesWithOptionIdNone.head
        .toCC[CCWithOptionId] shouldBe ccEdgeWithOptionIdNone
    }

    "have id-annotation Some(123)" in new CCEdgeAddFixture {
      val ccEdgeWithOptionIdSomeInitial =
        CCWithOptionId("edge-property", Some(123))

      val ccEdgeWithOptionIdSome = ccVertexFrom
        .addEdge(ccVertexTo, ccEdgeWithOptionIdSomeInitial)
        .toCC[CCWithOptionId]
      ccEdgeWithOptionIdSome.id shouldBe ccEdgeWithOptionIdSomeInitial.id

      val ccEdgesWithOptionIdSome = graph.E.hasLabel[CCWithOptionId].toList
      (ccEdgesWithOptionIdSome should have).size(1)
      ccEdgesWithOptionIdSome.head
        .toCC[CCWithOptionId] shouldBe ccEdgeWithOptionIdSome
    }
  }

  "edge" should {
    "update using a case-class template" in new CCEdgeUpdateFixture {
      graph
        .E(ccWithIdSet.id.get)
        .head
        .updateWith(ccUpdate)
        .toCC[CC] shouldBe ccUpdate
      graph.E(ccWithIdSet.id.get).head.toCC[CC] shouldBe ccUpdate
    }

    "update as a case class" in new CCEdgeUpdateFixture {
      graph
        .E(ccWithIdSet.id.get)
        .head
        .updateAs[CC](_.copy(s = ccUpdate.s, i = ccUpdate.i))
        .toCC[CC] shouldBe ccUpdate
      graph.E(ccWithIdSet.id.get).head.toCC[CC] shouldBe ccUpdate
    }
  }

  "vertex" should {
    "update using a case-class template" in new CCVertexUpdateFixture {
      graph
        .V(ccWithIdSet.id.get)
        .head
        .updateWith(ccUpdate)
        .toCC[CC] shouldBe ccUpdate
      graph.V(ccWithIdSet.id.get).head.toCC[CC] shouldBe ccUpdate
    }

    "update as a case class" in new CCVertexUpdateFixture {
      graph
        .V(ccWithIdSet.id.get)
        .head
        .updateAs[CC](_.copy(s = ccUpdate.s, i = ccUpdate.i))
        .toCC[CC] shouldBe ccUpdate
      graph.V(ccWithIdSet.id.get).head.toCC[CC] shouldBe ccUpdate
    }
  }

  "marshals a the end step of a traversal" in new Fixture {
    val cc1 = CCSimple("text one", 1)
    val cc2 = CCSimple("text two", 2)
    graph + cc1
    graph + cc2

    val results: Set[CCSimple] = graph.V.toCC[CCSimple].toSet
    results shouldBe Set(cc1, cc2)
  }

  trait Fixture {
    val graph = TinkerGraph.open.asScala
  }

  trait CCUpdateFixture[E <: Element] extends Fixture {
    type CC = CCWithOptionIdNested
    val ccInitial = CCWithOptionIdNested("string", None, MyValueClass(42))

    def ccWithIdSet: CC
    lazy val ccUpdate = ccWithIdSet.copy(s = "otherString", i = MyValueClass(7))
  }

  trait CCVertexUpdateFixture extends CCUpdateFixture[Vertex] {
    val ccWithIdSet = (graph + ccInitial).toCC[CC]
  }

  trait CCEdgeAddFixture extends Fixture {
    val ccVertexFrom = graph + "fromLabel"
    val ccVertexTo = graph + "toLabel"
  }

  trait CCEdgeUpdateFixture extends CCUpdateFixture[Edge] {
    private val testVertex = graph + "Huh"
    val ccWithIdSet = testVertex.addEdge(testVertex, ccInitial).toCC[CC]
  }

  "can't persist a none product type (none case class or tuple)" in {
    illTyped {
      """
        val graph = TinkerGraph.open.asScala
        graph + new NoneCaseClass("test")
      """
    }
  }
}
