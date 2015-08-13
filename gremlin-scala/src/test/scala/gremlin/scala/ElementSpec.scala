package gremlin.scala

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import org.scalatest.matchers.ShouldMatchers
import org.apache.tinkerpop.gremlin.structure.T

class ElementSpec extends TestBase {

  describe("properties") {
    it("gets properties") {
      v(1).keys shouldBe Set("name", "age")
      v(1).property[String]("name").value should be("marko")
      v(1).property[String]("doesnt exit").isPresent shouldBe false
      v(1).propertyMap() shouldBe Map("name" -> "marko", "age" -> 29)
      v(1).propertyMap("name", "age") shouldBe Map("name" -> "marko", "age" -> 29)
      v(1).properties("name", "age") should have length 2
      v(1).properties() should have length 2

      e(7).keys shouldBe Set("weight")
      e(7).property[Float]("weight").value shouldBe 0.5
      e(7).property[Float]("doesnt exit").isPresent shouldBe false
      e(7).propertyMap("weight") shouldBe Map("weight" -> 0.5)
    }

    it("sets a property") {
      v(1).setProperty("vertexProperty", "updated")
      v(1).property[String]("vertexProperty").value shouldBe "updated"

      e(7).setProperty("edgeProperty", "updated")
      e(7).property[String]("edgeProperty").value shouldBe "updated"
    }

    it("removes a property") {
      v(1).setProperty("vertexProperty1", "updated")
      v(1).removeProperty("vertexProperty1")
      v(1).removeProperty("doesnt exist")
      v(1).property[String]("vertexProperty1").isPresent shouldBe false

      e(7).setProperty("edgeProperty", "updated")
      e(7).removeProperty("edgeProperty")
      e(7).removeProperty("doesnt exist")
      e(7).property[String]("edgeProperty").isPresent shouldBe false
    }
  }

  describe("values") {
    it("gets a value") {
      v(1).value[String]("name") shouldBe Some("marko")
      v(1).value[String]("doesn't exist") shouldBe None
      v(1).getValue[String]("name") shouldBe "marko"
      e(7).value[Float]("weight") shouldBe Some(0.5)
    }

    it("falls back to default value if value doesnt exist") {
      v(1).valueOrElse("doesnt exist", "blub") shouldBe "blub"
      e(7).valueOrElse("doesnt exist", 0.8) shouldBe 0.8
    }

    it("returns None if it doesn't exist") {
      v(1).value[String]("doesnt exit") shouldBe None
      e(7).value[Float]("doesnt exit") shouldBe None
    }
  }

  describe("id, equality and hashCode") {
    it("has an id") {
      v(1).id shouldBe 1
      e(7).id shouldBe 7
    }

    it("equals") {
      v(1) == v(1) shouldBe true
      v(1) == v(2) shouldBe false
    }

    it("uses the right hashCodes") {
      v(1).hashCode shouldBe v(1).hashCode
      v(1).hashCode should not be v(2).hashCode

      Set(v(1)) contains v(1) shouldBe true
      Set(v(1)) contains v(2) shouldBe false
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
      val v1 = graph ++ label1
      val v2 = graph ++ (label2, "testkey" -> "testValue")

      graph.V.has(T.label, label1).head() shouldBe v1.vertex
      graph.V.has(T.label, label2).head() shouldBe v2.vertex
      graph.V.has(T.label, label2).head().value[String]("testkey") shouldBe "testValue"
    }

    it("adds a vertex and edges with a given label with syntactic sugar") {
      val g = TinkerGraph.open.asScala
      val label1 = "label1"
      val label2 = "label2"
      val testLabel = "testLabel"
      (g ++ label1) --- testLabel --> (g ++ (label2, "testkey" -> "testValue"))

      g.V.has(T.label, label1).head().label() shouldBe label1
      g.V.has(T.label, label2).head().label() shouldBe label2
      g.V.has(T.label, label2).head().value[String]("testkey") shouldBe "testValue"
      g.V.has(T.label, label1).head().outE().head().label() shouldBe testLabel
      g.V.has(T.label, label1).head().out(testLabel).head().label() shouldBe label2
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

      val e = v1.addEdge("testLabel", v2, Map("testKey" -> "testValue"))
      e.label shouldBe "testLabel"
      e.propertyMap("testKey") shouldBe Map("testKey" -> "testValue")
      v1.outE().head should be(e.edge)
      v1.out("testLabel").head shouldBe v2.vertex
    }

    it("removes elements") {
      val graph = TinkerGraph.open.asScala
      val v = graph.addVertex()
      v.remove()
      graph.V.toList() shouldBe empty
    }
  }
}

