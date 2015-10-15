package gremlin.scala

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import org.scalatest.matchers.ShouldMatchers
import org.apache.tinkerpop.gremlin.structure.T

class ElementSpec extends TestBase {

  describe("properties") {
    it("gets properties") {
      v1.keys shouldBe Set("name", "age")
      v1.property[String]("name").value should be("marko")
      v1.property[String]("doesnt exit").isPresent shouldBe false
      v1.valueMap shouldBe Map("name" → "marko", "age" → 29)
      v1.valueMap("name", "age") shouldBe Map("name" → "marko", "age" → 29)
      v1.properties("name", "age").length shouldBe 2
      v1.properties.length shouldBe 2

      e7.keys shouldBe Set("weight")
      e7.property[Float]("weight").value shouldBe 0.5
      e7.property[Float]("doesnt exit").isPresent shouldBe false
      e7.valueMap("weight") shouldBe Map("weight" → 0.5)
    }

    it("maps properties to scala.Option") {
      v1.property[String]("name").toOption should be(Some("marko"))
      e7.property[Float]("weight").toOption shouldBe Some(0.5)
    }

    it("sets a property") {
      v1.setProperty("vertexProperty", "updated")
      v1.property[String]("vertexProperty").value shouldBe "updated"

      e7.setProperty("edgeProperty", "updated")
      e7.property[String]("edgeProperty").value shouldBe "updated"
    }

    it("removes a property") {
      v1.setProperty("vertexProperty1", "updated")
      v1.removeProperty("vertexProperty1")
      v1.removeProperty("doesnt exist")
      v1.property[String]("vertexProperty1").isPresent shouldBe false

      e7.setProperty("edgeProperty", "updated")
      e7.removeProperty("edgeProperty")
      e7.removeProperty("doesnt exist")
      e7.property[String]("edgeProperty").isPresent shouldBe false
    }
  }

  describe("values") {
    it("gets a value") {
      v1.value[String]("name") shouldBe Some("marko")
      v1.value[String]("doesn't exist") shouldBe None
      v1.getValue[String]("name") shouldBe "marko"
      e7.value[Float]("weight") shouldBe Some(0.5)
    }

    it("falls back to default value if value doesnt exist") {
      v1.valueOrElse("doesnt exist", "blub") shouldBe "blub"
      e7.valueOrElse("doesnt exist", 0.8) shouldBe 0.8
    }

    it("returns None if it doesn't exist") {
      v1.value[String]("doesnt exit") shouldBe None
      e7.value[Float]("doesnt exit") shouldBe None
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
      v2.setProperty("testkey", "testValue")

      graph.v(v1.id) shouldBe Some(v1)
      graph.v(v2.id).get.property[String]("testkey").value shouldBe "testValue"
      graph.V.toList() should have size 2
    }

    it("adds a vertex with a given label") {
      val graph = TinkerGraph.open.asScala
      val label1 = "label1"
      val label2 = "label2"
      val v1 = graph.addVertex(label1)
      val v2 = graph.addVertex(label2, Map("testkey" → "testValue"))

      graph.V.has(T.label, label1).head() shouldBe v1.vertex
      graph.V.has(T.label, label2).head() shouldBe v2.vertex
      graph.V.has(T.label, label2).head().value[String]("testkey") shouldBe "testValue"
    }

    it("adds a vertex with a given label with syntactic sugar") {
      val graph = TinkerGraph.open.asScala
      val label1 = "label1"
      val label2 = "label2"

      val v1 = graph + label1
      val v2 = graph + (label2, "testkey" → "testValue")

      graph.V.hasLabel(label1).head() shouldBe v1.vertex
      graph.V.hasLabel(label2).head() shouldBe v2.vertex
      graph.V.hasLabel(label2).head().value[String]("testkey") shouldBe "testValue"

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

      val e = v1.asScala.addEdge("testLabel", v2, Map("testKey" → "testValue"))
      e.label shouldBe "testLabel"
      e.valueMap("testKey") shouldBe Map("testKey" → "testValue")
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
}

