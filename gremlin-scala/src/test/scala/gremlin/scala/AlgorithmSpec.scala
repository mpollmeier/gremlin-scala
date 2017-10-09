package gremlin.scala

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import org.scalatest.{WordSpec, Matchers}

/* demo common algorithms */
class AlgorithmSpec extends WordSpec with Matchers {

  "directed acyclic graphs" can {
    "be detected" in {
      // to start with, there's no cycles
      val graph = TinkerGraph.open.asScala
      val vA = graph + "a"
      val vB = graph + "b"
      val vC = graph + "c"
      val vD = graph + "d"
      vA --- "next" --> vB
      vA --- "next" --> vD
      vC --- "next" --> vA
      vC --- "next" --> vD

      isCyclic(graph) shouldBe false

      //make it cyclic
      vB --- "next" --> vC
      isCyclic(graph) shouldBe true
    }

    def isCyclic(graph: ScalaGraph): Boolean = {
      val paths = graph.V.as("a")
        .repeat(_.out.simplePath)
        .emit
        .where(_.out.as("a"))
        .toList

      paths.size > 0
    }
  }

}
