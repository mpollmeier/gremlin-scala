package gremlin.scala

import gremlin.scala.StepLabels.StepLabel
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import org.scalatest.Matchers
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers
import shapeless._

class SelectSpec extends WordSpec with Matchers {

  "select for all labelled steps" should {
    def g: ScalaGraph[TinkerGraph] = TinkerFactory.createModern.asScala
    val a = StepLabel[Vertex]()
    val b = StepLabel[Edge]()
    val c = StepLabel[Double]()

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

}
