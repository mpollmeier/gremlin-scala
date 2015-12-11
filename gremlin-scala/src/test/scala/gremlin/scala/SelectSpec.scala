package gremlin.scala

import org.apache.tinkerpop.gremlin.tinkergraph.structure.{TinkerFactory, TinkerGraph}
import org.scalatest.{Matchers, WordSpec}
import java.lang.{Double ⇒ JDouble, Long ⇒ JLong}

class SelectSpec extends WordSpec with Matchers {
  def graph = TinkerFactory.createModern.asScala

  "selecting all labelled steps" should {
    "support single label" in {
      val path: List[Tuple1[Edge]] =
        graph.V(1).outE.as("a").select.toList

      path(0)._1 shouldBe graph.E(9).head
      path(1)._1 shouldBe graph.E(7).head
      path(2)._1 shouldBe graph.E(8).head
    }

    "support multiple label" in {
      val path: List[(Vertex, Edge)] =
        graph.V(1).as("a").outE.as("b").select.toList

      val v1 = graph.V(1).head
      path(0) shouldBe ((v1, graph.E(9).head))
      path(1) shouldBe ((v1, graph.E(7).head))
      path(2) shouldBe ((v1, graph.E(8).head))
    }

    "works without labelled steps" in {
      val path: List[Unit] =
        graph.V(1).outE.inV.select.toList

      path(0) shouldBe (())
      path(1) shouldBe (())
      path(2) shouldBe (())
    }
  }

  "selecting one or more specific labels" should {
    val a = StepLabel[Vertex]()
    val b = StepLabel[Edge]()
    val c = StepLabel[Double]()

    val v1 = graph.V(1).head
    val e9 = graph.E(9).head
    def traversal = graph.V(1).as(a).outE("created").as(b).value(TestGraph.Weight).as(c)
    "derive types for a simple as/select" in {
      val result: Vertex =
        traversal.select(a).head

      result shouldBe v1
    }

    "derive types for as/select with two labels" in {
      val result: (Vertex, Edge) =
        traversal.select((a, b)).head

      result shouldBe ((v1, e9))
    }

    "derive types for as/select with three labels" in {
      val result: (Vertex, Edge, Double) =
        traversal.select((a, b, c)).head

      result shouldBe ((v1, e9, 0.4))
    }
  }

  "resets labels on ReducingBarrier steps" should {
    "work for `mean`" in {
        graph.V.as("a").outE("created")
          .value(TestGraph.Weight)
          .mean.as("b")
          .select()
          .head._1 shouldBe 0.49999999999999994
    }

    "work for `count`" in {
      graph.V.as("a")
        .out("created")
        .count().as("b")
        .select().head._1 shouldBe 4
    }

    "work for `max`" in {
      // val x: Number = 5
      // val t = graph.V.as("a").outE("created")
      //   .value(TestGraph.Weight)
      // .map(_.toInt)
      // .max.as("b")
      //   .toList.foreach println

      // new GremlinNumberSteps(t)


        // .select()
        // .head._1 shouldBe 0.49999999999999994
    }
  }
}
