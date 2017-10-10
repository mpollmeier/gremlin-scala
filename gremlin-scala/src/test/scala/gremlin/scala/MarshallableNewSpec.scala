package gremlin.scala

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import org.scalatest.{Matchers, WordSpec}
import scala.meta.serialiser.{FromMap, mappable, ToMap}
import scala.util.Try
import scala.collection.JavaConverters._

/* note: to print out the generated code to the console, just define @mappable(List("_debug" -> "true")) */
class MarshallableNewSpec extends WordSpec with Matchers {
  "marshals / unmarshals case classes" which {

    @mappable case class CCSimple(s: String, i: Int)
    "only have simple members" in {
      val graph = TinkerGraph.open.asScala
      val cc = CCSimple("text", 12)
      val v = graph +- cc

      val vl = graph.V(v.id).head
      v.label shouldBe cc.getClass.getSimpleName
      v.valueMap should contain("s" → cc.s)
      v.valueMap should contain("i" → cc.i)
      v.asEntity[CCSimple] shouldBe cc
    }

    "contain options" should {
      // Background: if we marshal Option types, the graph db needs to understand scala.Option,
      // which wouldn't make any sense. So we rather translate it to `null` if it's `None`.
      // https://github.com/mpollmeier/gremlin-scala/issues/98

      @mappable case class CCWithOption(i: Int, s: Option[String])
      "map `Some[A]` to `A`" in {
        val graph = TinkerGraph.open.asScala
        val ccWithOptionSome = CCWithOption(Int.MaxValue, Some("optional value"))
        val v = graph +- ccWithOptionSome
        v.asEntity[CCWithOption] shouldBe ccWithOptionSome

        val vl = graph.V(v.id).head
        vl.value[String]("s") shouldBe ccWithOptionSome.s.get
      }

      "map `None` to `null`" in {
        val graph = TinkerGraph.open.asScala
        val ccWithOptionNone = CCWithOption(Int.MaxValue, None)
        val v = graph +- ccWithOptionNone
        v.asEntity[CCWithOption] shouldBe ccWithOptionNone

        val vl = graph.V(v.id).head
        vl.keys should not contain "s"  //None should be mapped to `null`
      }
    }

    @mappable(Map("_label" -> "CustomLabel"))
    case class CCWithLabel(s: String)
    "define a custom label" in {
      val graph = TinkerGraph.open.asScala
      val cc = CCWithLabel("some string")
      val v = graph +- cc

      val vl = graph.V(v.id).head
      v.asEntity[CCWithLabel] shouldBe cc
      v.label shouldBe "CustomLabel"
    }

    @mappable case class CCWithVertex(underlying: Option[Vertex] = None, s: String) extends WithVertex[CCWithVertex] {
      override def withVertex(vertex: Vertex) = this.copy(underlying = Some(vertex))
    }
    "contain the underlying vertex as a member (when given a lens)" in {
      val graph = TinkerGraph.open.asScala
      val cc = CCWithVertex(s = "some string")
      val v = graph +- cc

      // now the underlying vertex should be set
      val serialised = v.asEntity[CCWithVertex]
      serialised.underlying.get shouldBe v
      serialised.s shouldBe cc.s
    }

    "define a custom marshaller" in {
      case class CCWithOption(i: Int, s: Option[String])

      implicit val toMap = new ToMap[CCWithOption] {
        override def apply(cc: CCWithOption) = Map("i" -> cc.i, "s" -> cc.s.getOrElse("undefined"))
      }
      implicit val fromMap = new FromMap[CCWithOption] {
        override def apply(keyValues: Map[String, Any]) =
          Try {
            CCWithOption(i = keyValues("i").asInstanceOf[Int],
                         s = keyValues.get("s").asInstanceOf[Option[String]])
          }
      }

      val graph = TinkerGraph.open.asScala
      val cc = CCWithOption(Int.MaxValue, None)
      val v = graph +- cc
      v.asEntity[CCWithOption] shouldBe CCWithOption(Int.MaxValue, Some("undefined"))
    }
  }

  "vertex" should {

    @mappable case class CCSimple(s: String, i: Int)
    "update using a new instance" in {
      val graph = TinkerGraph.open.asScala
      val initial = CCSimple("initial", 1)
      graph +- initial

      val update = CCSimple("updated", 2)
      graph.V.head.updateWith1(update)
      graph.V.head.asEntity[CCSimple] shouldBe update
    }

    "update using lambda" in {
      val graph = TinkerGraph.open.asScala
      val initial = CCSimple("initial", 1)
      graph +- initial

      val update = CCSimple("updated", 2)
      graph.V.head.updateAs1[CCSimple](_ => update)
      graph.V.head.asEntity[CCSimple] shouldBe update
    }
  }

}
