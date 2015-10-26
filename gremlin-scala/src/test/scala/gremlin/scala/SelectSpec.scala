package gremlin.scala

// import gremlin.scala.StepLabels.StepLabel
import org.apache.tinkerpop.gremlin.tinkergraph.structure.{TinkerFactory, TinkerGraph}
import org.scalatest.{Matchers, WordSpec}
import shapeless._

class SelectSpec extends WordSpec with Matchers {
  def g: ScalaGraph[TinkerGraph] = TinkerFactory.createModern.asScala

  "select" when {
    val a = StepLabel[Vertex]()
    val b = StepLabel[Edge]()
    val c = StepLabel[Double]()

    "selecting all labelled steps" should {
      "support single label" in {
        val path: List[Edge :: HNil] =
          g.V(1).outE.as(b).select.toList

        path(0) shouldBe g.E(9).head :: HNil
        path(1) shouldBe g.E(7).head :: HNil
        path(2) shouldBe g.E(8).head :: HNil
      }

      "support multiple label" in {
        val path: List[Vertex :: Edge :: HNil] =
          g.V(1).as(a).outE.as(b).select.toList

        val v1 = g.V(1).head
        path(0) shouldBe v1 :: g.E(9).head :: HNil
        path(1) shouldBe v1 :: g.E(7).head :: HNil
        path(2) shouldBe v1 :: g.E(8).head :: HNil
      }

      "works without labelled steps" in {
        val path: List[HNil] =
          g.V(1).outE.inV.select.toList

        path(0) shouldBe HNil
        path(1) shouldBe HNil
        path(2) shouldBe HNil
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
        val result: Vertex :: Edge :: HNil =
          traversal.select(a :: b :: HNil).head

        result shouldBe v1 :: e9 :: HNil
      }

      "derive types for as/select with three labels" in {
        val result: Vertex :: Edge :: Double :: HNil =
          traversal.select(a :: b :: c :: HNil).head

        result shouldBe v1 :: e9 :: 0.4 :: HNil
      }
    }
  }

}
