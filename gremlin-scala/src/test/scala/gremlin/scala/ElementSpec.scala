package gremlin.scala

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import org.apache.tinkerpop.gremlin.structure.T
import org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality
import scala.collection.JavaConverters._
import TestGraph._

// TODO: rewrite using new type safe steps
class ElementSpec extends TestBase {

  describe("properties") {
    it("gets properties") {
      v1.keys shouldBe Set(Key("name"), Key("age"))
      v1.property(Name).value shouldBe "marko"
      v1.property(DoesNotExist).isPresent shouldBe false
      v1.valueMap shouldBe Map("name" -> "marko", "age" -> 29)
      v1.valueMap("name", "age") shouldBe Map("name" -> "marko", "age" -> 29)
      v1.properties("name", "age").length shouldBe 2
      v1.properties.length shouldBe 2

      e7.keys shouldBe Set(Key("weight"))
      e7.property(Weight).value shouldBe 0.5
      e7.property(DoesNotExist).isPresent shouldBe false
      e7.valueMap("weight") shouldBe Map("weight" -> 0.5)
    }

    it("maps properties to scala.Option") {
      v1.property(Name).toOption should be(Some("marko"))
      e7.property(Weight).toOption shouldBe Some(0.5)
    }

    it("sets a property") {
      v1.setProperty(TestProperty, "updated")
      v1.property(TestProperty).value shouldBe "updated"

      e7.setProperty(TestProperty, "updated")
      e7.property(TestProperty).value shouldBe "updated"
    }

    /** adapted from http://tinkerpop.apache.org/docs/current/reference/#vertex-properties
      * TODO: `properties` should take `Key` as well
      */
    it("sets a property with multiple values") {
      val v = graph.addVertex((Name.name -> "marko"), (Name.name -> "marko a. rodriguez"))
      graph.V(v).properties(Name.name).count.head shouldBe 2

      v.property(Cardinality.list, Name.name, "m. a. rodriguez")
      graph.V(v).properties(Name.name).count.head shouldBe 3

      graph.V(v).properties(Name.name).hasValue("marko").count.head shouldBe 1
    }

    it("removes a property") {
      v1.setProperty(TestProperty, "updated")
      v1.removeProperty(TestProperty)
      v1.property(TestProperty).isPresent shouldBe false

      e7.setProperty(TestProperty, "updated")
      e7.removeProperty(TestProperty)
      e7.property(TestProperty).isPresent shouldBe false
    }
  }

  describe("values") {
    it("gets a value") {
      v1.value2(Name) shouldBe "marko"
      e7.value2(Weight) shouldBe 0.5
    }

    it("gets an optional value") {
      v1.valueOption(Name) shouldBe Some("marko")
      v1.valueOption(DoesNotExist) shouldBe None
    }

    it("sets an optional value") {
      v1.valueOption(TestProperty, None)
      v1.property(TestProperty).isPresent shouldBe false
      v1.valueOption(TestProperty, Some("test"))
      v1.property(TestProperty).value shouldBe "test"

      e7.valueOption(TestProperty, None)
      e7.property(TestProperty).isPresent shouldBe false
      e7.valueOption(TestProperty, Some("test"))
      e7.property(TestProperty).value shouldBe "test"
    }

    it("throws an exception if it doesn't exist") {
      intercept[IllegalStateException] { v1.value2(DoesNotExist) }
    }
  }

  describe("id, equality and hashCode") {
    it("has an id") {
      v1.id shouldBe 1
      e7.id shouldBe 7
    }

    it("equals") {
      v1 == v(1).asScala shouldBe true
      v1 == v(2).asScala shouldBe false
    }

    it("uses the right hashCodes") {
      v1.hashCode shouldBe v(1).asScala.hashCode
      v1.hashCode should not be v(2).asScala.hashCode

      Set(v1) contains v(1) shouldBe true
      Set(v1) contains v(2) shouldBe false
    }
  }

  describe("adding and removing elements") {

    it("adds a vertex") {
      val graph = TinkerGraph.open.asScala
      val v1 = graph.addVertex()
      val v2 = graph.addVertex()
      v2.setProperty(TestProperty, "testValue")

      graph.V(v1.id).head shouldBe v1
      graph.V(v2.id).head.property(TestProperty).value shouldBe "testValue"
      (graph.V.toList() should have).size(2)
    }

    it("adds a vertex with a given label") {
      val graph = TinkerGraph.open.asScala
      val label1 = "label1"
      val label2 = "label2"
      val v1 = graph.addVertex(label1)
      val v2 = graph.addVertex(label2, Map(TestProperty.name -> "testValue"))

      graph.V.has(T.label, label1).head shouldBe v1.vertex
      graph.V.has(T.label, label2).head shouldBe v2.vertex
      graph.V.has(T.label, label2).value(TestProperty).head shouldBe "testValue"
    }

    it("adds a vertex with a given label with syntactic sugar") {
      val graph = TinkerGraph.open.asScala
      val label1 = "label1"
      val label2 = "label2"

      val v1 = graph + label1
      val v2 = graph + (label2, TestProperty -> "testValue")

      graph.V.hasLabel(label1).head shouldBe v1.vertex
      graph.V.hasLabel(label2).head shouldBe v2.vertex
      graph.V.hasLabel(label2).value(TestProperty).head shouldBe "testValue"

      graph.asJava.close()
    }

    it("adds an edge") {
      val graph = TinkerGraph.open.asScala
      val v1 = graph.addVertex()
      val v2 = graph.addVertex()

      val e = v1.addEdge("testLabel", v2)
      e.label shouldBe "testLabel"
      v1.outE().head shouldBe e.edge
      v1.out("testLabel").head shouldBe v2.vertex
    }

    it("adds an edge with additional properties") {
      val graph = TinkerGraph.open.asScala
      val v1 = graph.addVertex()
      val v2 = graph.addVertex()

      val e = v1.asScala.addEdge("testLabel", v2, TestProperty -> "testValue")
      e.label shouldBe "testLabel"
      e.value2(TestProperty) shouldBe "testValue"
      e.valueMap(TestProperty.name) shouldBe Map(TestProperty.name -> "testValue")
      v1.outE().head shouldBe e.edge
      v1.out("testLabel").head shouldBe v2.vertex
    }

    it("removes elements") {
      val graph = TinkerGraph.open.asScala
      val v = graph.addVertex()
      v.remove()
      graph.V.toList() shouldBe empty
    }
  }

  def v1 = v(1).asScala
  def e7 = e(7).asScala
  val TestProperty = Key[String]("testProperty")
}
