package gremlin.scala

import org.apache.tinkerpop.gremlin.tinkergraph.structure.{TinkerFactory, TinkerGraph}
import org.scalatest.{Matchers, WordSpec}
import shapeless._

class SelectSpec extends WordSpec with Matchers {
  def g = TinkerFactory.createModern.asScala

  "select" when {
    val a = StepLabel[Vertex]()
    val b = StepLabel[Edge]()
    val c = StepLabel[Double]()

    "selecting all labelled steps" should {
      "support single label" in {
        val path: List[Tuple1[Edge]] =
          g.V(1).outE.as(b).select.toList

        path(0) shouldBe Tuple1(g.E(9).head)
        path(1) shouldBe Tuple1(g.E(7).head)
        path(2) shouldBe Tuple1(g.E(8).head)
      }

      "support multiple label" in {
        val path: List[(Vertex, Edge)] =
          g.V(1).as(a).outE.as(b).select.toList

        val v1 = g.V(1).head
        path(0) shouldBe ((v1, g.E(9).head))
        path(1) shouldBe ((v1, g.E(7).head))
        path(2) shouldBe ((v1, g.E(8).head))
      }

      "works without labelled steps" in {
        val path: List[Unit] =
          g.V(1).outE.inV.select.toList

        path(0) shouldBe (())
        path(1) shouldBe (())
        path(2) shouldBe (())
      }
    }

    "selecting one or more specific labels" should {
      val v1 = g.V(1).head
      val e9 = g.E(9).head
      def traversal = g.V(1).as(a).outE("created").as(b).value("weight").as(c)
      "derive types for a simple as/select" in {
        val result: Vertex =
          traversal.select(a).head

        result shouldBe v1
      }

      "derive types for as/select with two labels" in {
        val result: (Vertex, Edge) =
          traversal.select(a :: b :: HNil).head

        result shouldBe ((v1, e9))
      }

      "derive types for as/select with three labels" in {
        val result: (Vertex, Edge, Double) =
          traversal.select(a :: b :: c :: HNil).head

        result shouldBe ((v1, e9, 0.4))
      }
    }
  }


}
