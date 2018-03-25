package gremlin.scala

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import org.scalatest.{Matchers, WordSpec}

class GraphHelperSpec extends WordSpec with Matchers {

  "deep clone graph" in {
    val original = TinkerGraph.open.asScala
    val testProperty = Key[String]("testProperty")

    {
      implicit val graph: ScalaGraph = original
      val marko = original + "marko"
      val stephen = original + "stephen"
      marko --- "knows" --> stephen
    }

    {
      implicit val clone: ScalaGraph = GraphHelper.cloneElements(original, TinkerGraph.open.asScala)
      val stephen = clone.V.hasLabel("stephen").head
      val michael = clone + "michael"
      michael --- "knows" --> stephen
      clone.V.property(testProperty, "someValue").iterate

      // original graph should be unchanged
      original.V.count.head shouldBe 2
      original.E.count.head shouldBe 1
      original.V.has(testProperty).count.head shouldBe 0

      // cloned graph should contain old and new elements and properties
      clone.V.count.head shouldBe 3
      clone.E.count.head shouldBe 2
      clone.V.has(testProperty).count.head shouldBe 3
    }
  }

}
