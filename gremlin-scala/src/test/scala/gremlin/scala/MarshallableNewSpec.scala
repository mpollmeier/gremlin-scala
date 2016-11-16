package gremlin.scala

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import org.scalatest.WordSpec
import org.scalatest.Matchers
import shapeless.test.illTyped
import scala.meta.serialiser.mappable

/* note: to print out the generated code to the console, just define @mappable(List("_debug" -> "true")) */
object MarshallableNewSpec {
  @mappable case class CCSimple(s: String, i: Int)
  @mappable case class CCWithOption(i: Int, s: Option[String])

  @mappable(List("_label" -> "CustomLabel"))
  case class CCWithLabel(s: String)

  // case class CCWithOptionId(@id id: Option[String], s: String)
  // case class CCWithOptionIdAndOptionValueClass(@id id: Option[Int], s: String, i: MyValueClass)

  // case class MyValueClass(value: Int) extends AnyVal
  // case class CCWithValueClass(s: String, i: MyValueClass)
  // case class CCWithOptionValueClass(s: String, i: Option[MyValueClass])

  // object CCWithCompanion { def funcInCompanion: String = "function in companion object" }
  // @mappable CCWithCompanion(s: String) { def funcInCC: String = "function in case class" }

  // @label("the_label") case class CCWithLotsOfStuff(
  //   @id id: Option[String],
  //   s: String,
  //   l: Long,
  //   o: Option[String],
  //   seq: Seq[String],
  //   map: Map[String, String],
  //   nested: NestedClass
  // )

  // case class NestedClass(s: String)
  // class NoneCaseClass(s: String)
}

// TODO: replace old MarshallableSpec
class MarshallableNewSpec extends WordSpec with Matchers {
  import MarshallableNewSpec._

  "marshals / unmarshals case classes" which {

    "only have simple members" in new Fixture {
      val cc = CCSimple("text", 12)
      val v = graph +- cc

      val vl = graph.V(v.id).head
      v.label shouldBe cc.getClass.getSimpleName
      v.valueMap should contain("s" → cc.s)
      v.valueMap should contain("i" → cc.i)
      v.toEntity[CCSimple] shouldBe cc
    }

    "contain options" should {
      // Background: if we marshal Option types, the graph db needs to understand scala.Option,
      // which wouldn't make any sense. So we rather translate it to `null` if it's `None`.
      // https://github.com/mpollmeier/gremlin-scala/issues/98

      "map `Some[A]` to `A`" in new Fixture {
        val ccWithOptionSome = CCWithOption(Int.MaxValue, Some("optional value"))
        val v = graph +- ccWithOptionSome
        v.toEntity[CCWithOption] shouldBe ccWithOptionSome

        val vl = graph.V(v.id).head
        vl.value[String]("s") shouldBe ccWithOptionSome.s.get
      }

      "map `None` to `null`" in new Fixture {
        val ccWithOptionNone = CCWithOption(Int.MaxValue, None)
        val v = graph +- ccWithOptionNone
        v.toEntity[CCWithOption] shouldBe ccWithOptionNone

        val vl = graph.V(v.id).head
        vl.keys should not contain "s"  //None should be mapped to `null`
      }
    }

    "define a custom label" in new Fixture {
      val cc = CCWithLabel("some string")
      val v = graph +- cc

      val vl = graph.V(v.id).head
      v.toEntity[CCWithLabel] shouldBe cc
      v.label shouldBe "CustomLabel"
    }

    // "use @label and @id annotations" in new Fixture {
    //   val ccWithLabelAndId = CCWithLabelAndId(
    //     "some string",
    //     Int.MaxValue,
    //     Long.MaxValue,
    //     Some("option type"),
    //     Seq("test1", "test2"),
    //     Map("key1" → "value1", "key2" → "value2"),
    //     NestedClass("nested")
    //   )

    //   val v = graph + ccWithLabelAndId

    //   v.toCC[CCWithLabelAndId] shouldBe ccWithLabelAndId

    //   val vl = graph.V(v.id).head()
    //   vl.label shouldBe "the_label"
    //   vl.id shouldBe ccWithLabelAndId.id
    //   vl.valueMap should contain("s" → ccWithLabelAndId.s)
    //   vl.valueMap should contain("l" → ccWithLabelAndId.l)
    //   vl.valueMap should contain("o" → ccWithLabelAndId.o.get)
    //   vl.valueMap should contain("seq" → ccWithLabelAndId.seq)
    //   vl.valueMap should contain("map" → ccWithLabelAndId.map)
    //   vl.valueMap should contain("nested" → ccWithLabelAndId.nested)
    // }

    // "have an Option @id annotation" in new Fixture {
    //   val cc = CCWithOptionId("text", Some(12))
    //   val v = graph + cc

    //   v.toCC[CCWithOptionId] shouldBe cc

    //   val vl = graph.V(v.id).head()
    //   vl.label shouldBe cc.getClass.getSimpleName
    //   vl.id shouldBe cc.id.get
    //   vl.valueMap should contain("s" → cc.s)
    // }

    // "define their custom marshaller" in new Fixture {
    //   val ccWithOptionNone = CCWithOption(Int.MaxValue, None)

    //   val marshaller = new Marshallable[CCWithOption] {
    //     def fromCC(cc: CCWithOption) =
    //       FromCC(None, "CCWithOption", Map("i" -> cc.i, "s" → cc.s.getOrElse("undefined")))

    //     def toCC(id: AnyRef, valueMap: Map[String, Any]): CCWithOption =
    //       CCWithOption(i = valueMap("i").asInstanceOf[Int],
    //                    s = valueMap.get("s").asInstanceOf[Option[String]])
    //   }

    //   val v = graph.+(ccWithOptionNone)(marshaller)
    //   v.toCC[CCWithOption](marshaller) shouldBe CCWithOption(ccWithOptionNone.i, Some("undefined"))
    // }


    // "contain value classes" should {
    //   "unwrap a plain value class" in new Fixture {
    //     val cc = CCWithValueClass("some text", MyValueClass(42))
    //     val v = graph + cc

    //     val vl = graph.V(v.id).head
    //     vl.label shouldBe cc.getClass.getSimpleName
    //     vl.valueMap should contain("s" → cc.s)
    //     vl.valueMap should contain("i" → cc.i.value)
    //     vl.toCC[CCWithValueClass] shouldBe cc
    //   }

    //   "unwrap an optional value class" in new Fixture {
    //     val cc = CCWithOptionValueClass("some text", Some(MyValueClass(42)))
    //     val v = graph + cc

    //     val vl = graph.V(v.id).head
    //     vl.label shouldBe cc.getClass.getSimpleName
    //     vl.valueMap should contain("s" → cc.s)
    //     vl.valueMap should contain("i" → cc.i.get.value)
    //     vl.toCC[CCWithOptionValueClass] shouldBe cc
    //   }

    //   "handle None value class" in new Fixture {
    //     val cc = CCWithOptionValueClass("some text", None)
    //     val v = graph + cc

    //     val vl = graph.V(v.id).head
    //     vl.label shouldBe cc.getClass.getSimpleName
    //     vl.valueMap should contain("s" → cc.s)
    //     vl.valueMap.keySet should not contain("i")
    //     vl.toCC[CCWithOptionValueClass] shouldBe cc
    //   }
    // }

  }

  // "find vertices by label" in new Fixture {
  //   val ccSimple = CCSimple("a string", 42)
  //   val ccWithOption = CCWithOption(52, Some("other string"))
  //   val ccWithLabel = CCWithLabel("s")

  //   graph + ccSimple
  //   graph + ccWithOption
  //   graph + ccWithLabel

  //   graph.V.count.head shouldBe 3

  //   val ccSimpleVertices = graph.V.hasLabel[CCSimple].toList
  //   ccSimpleVertices should have size 1
  //   ccSimpleVertices.head.toCC[CCSimple] shouldBe ccSimple

  //   val ccWithLabelVertices = graph.V.hasLabel[CCWithLabel].toList
  //   ccWithLabelVertices should have size 1
  //   ccWithLabelVertices.head.toCC[CCWithLabel] shouldBe ccWithLabel
  // }

  // "edge" should {
  //   "update using a case-class template" in new CCEdgeUpdateFixture {
  //     graph.E(ccWithIdSet.id.get).head.updateWith(ccUpdate).toCC[CC] shouldBe ccUpdate
  //     graph.E(ccWithIdSet.id.get).head.toCC[CC] shouldBe ccUpdate
  //   }

  //   "update as a case class" in new CCEdgeUpdateFixture {
  //     graph.E(ccWithIdSet.id.get).head.updateAs[CC](_.copy(s = ccUpdate.s, i = ccUpdate.i)).toCC[CC] shouldBe ccUpdate
  //     graph.E(ccWithIdSet.id.get).head.toCC[CC] shouldBe ccUpdate
  //   }
  // }

  // "vertex" should {
  //   "update using a case-class template" in new CCVertexUpdateFixture {
  //     graph.V(ccWithIdSet.id.get).head.updateWith(ccUpdate).toCC[CC] shouldBe ccUpdate
  //     graph.V(ccWithIdSet.id.get).head.toCC[CC] shouldBe ccUpdate
  //   }

  //   "update as a case class" in new CCVertexUpdateFixture {
  //     graph.V(ccWithIdSet.id.get).head.updateAs[CC](_.copy(s = ccUpdate.s, i = ccUpdate.i)).toCC[CC] shouldBe ccUpdate
  //     graph.V(ccWithIdSet.id.get).head.toCC[CC] shouldBe ccUpdate
  //   }
  // }

  trait Fixture {
    val graph = TinkerGraph.open.asScala
  }

  // trait CCUpdateFixture[E <: Element] extends Fixture {
  //   type CC = CCWithOptionIdNested
  //   val ccInitial = CCWithOptionIdNested("string", None, MyValueClass(42))

  //   def ccWithIdSet: CC
  //   lazy val ccUpdate = ccWithIdSet.copy(s = "otherString", i = MyValueClass(7))
  // }

  // trait CCVertexUpdateFixture extends CCUpdateFixture[Vertex] {
  //   val ccWithIdSet = (graph + ccInitial).toCC[CC]
  // }

  // trait CCEdgeUpdateFixture extends CCUpdateFixture[Edge] {
  //   private val testVertex = graph + "Huh"
  //   val ccWithIdSet = testVertex.addEdge(testVertex, ccInitial).toCC[CC]
  // }

  // "can't persist a none product type (none case class or tuple)" in {
  //   illTyped {
  //     """
  //       val graph = TinkerGraph.open.asScala
  //       graph + new NoneCaseClass("test")
  //     """
  //   }
  // }
}
