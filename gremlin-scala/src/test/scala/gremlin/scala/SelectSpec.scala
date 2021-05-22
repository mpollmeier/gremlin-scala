package gremlin.scala

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import shapeless.{::, HNil}
import java.util.{Collection => JCollection, Map => JMap}
import scala.collection.JavaConverters._

class SelectSpec extends AnyWordSpec with Matchers {
  def graph = TinkerFactory.createModern.asScala()

  "selecting all labelled steps" should {
    "support single label" in {
      val path: List[Tuple1[Edge]] =
        graph.V(1).outE().as("a").select().toList()

      path(0)._1 shouldBe graph.E(9).head()
      path(1)._1 shouldBe graph.E(7).head()
      path(2)._1 shouldBe graph.E(8).head()
    }

    "support multiple label" in {
      val path: List[(Vertex, Edge)] =
        graph.V(1).as("a").outE().as("b").select().toList()

      val v1 = graph.V(1).head()
      path(0) shouldBe ((v1, graph.E(9).head()))
      path(1) shouldBe ((v1, graph.E(7).head()))
      path(2) shouldBe ((v1, graph.E(8).head()))
    }

    "works without labelled steps" in {
      val path: List[Unit] =
        graph.V(1).outE().inV().select().toList()

      path(0) shouldBe (())
      path(1) shouldBe (())
      path(2) shouldBe (())
    }
  }

  "selecting one or more specific labels" should {
    val a = StepLabel[Vertex]()
    val b = StepLabel[Edge]()
    val c = StepLabel[Double]()

    val v1 = graph.V(1).head()
    val e9 = graph.E(9).head()

    def newTraversal: GremlinScala.Aux[Double, Vertex :: Edge :: Double :: HNil] =
      graph.V(1).as(a).outE("created").as(b).value(TestGraph.Weight).as(c)

    "derive types for a simple as/select" in {
      val traversal = newTraversal
      val result: Vertex =
        traversal.select(a).head()

      result shouldBe v1
    }

    "derive types for as/select with two labels" in {
      val traversal = newTraversal
      val result: (Vertex, Edge) =
        traversal.select((a, b)).head()

      result shouldBe ((v1, e9))
    }

    "derive types for as/select with three labels" in {
      val traversal = newTraversal
      val result: (Vertex, Edge, Double) =
        traversal.select((a, b, c)).head()

      result shouldBe ((v1, e9, 0.4))
    }
  }

  "resets labels on ReducingBarrier steps" should {
    "work for `mean`" in {
      graph
        .V()
        .as("a")
        .outE("created")
        .value(TestGraph.Weight)
        .mean()
        .as("b")
        .select()
        .head()
        ._1 shouldBe 0.49999999999999994
    }

    "work for `count`" in {
      graph
        .V()
        .as("a")
        .out("created")
        .count()
        .as("b")
        .select()
        .head()
        ._1 shouldBe 4
    }

    "work for `max`" in {
      graph
        .V()
        .as("a")
        .outE("created")
        .value(TestGraph.Weight)
        .max()
        .as("b")
        .select()
        .head()
        ._1 shouldBe 1.0
    }

    "work for `min`" in {
      graph
        .V()
        .as("a")
        .outE("created")
        .value(TestGraph.Weight)
        .min()
        .as("b")
        .select()
        .head()
        ._1 shouldBe 0.2
    }

    "work for `sum`" in {
      val sum = graph
        .V()
        .as("a")
        .outE("created")
        .value(TestGraph.Weight)
        .sum()
        .as("b")
        .select()
        .head()
        ._1

      (sum: Double) shouldBe 2d +- 0.1d
    }
  }

  "select column" should {
    "extract keys from map" in {
      val result: collection.Set[String] = graph
        .V()
        .hasLabel("software")
        .group(By(__().value(Key[String]("name"))), By(__().in("created")))
        .selectKeys
        .head()
        .asScala
      result shouldBe Set("ripple", "lop")
    }

    "extract keys from map entry" in {
      val result: List[String] = graph
        .V()
        .hasLabel("software")
        .group(By(__().value(Key[String]("name"))), By(__().in("created")))
        .unfold[JMap.Entry[String, JCollection[Vertex]]]()
        .selectKeys
        .toList()
      result shouldBe List("ripple", "lop")
    }

    "extract keys from path" in {
      val result: Seq[collection.Set[String]] = graph
        .V(1)
        .as("a")
        .outE()
        .as("b")
        .path()
        .selectKeys
        .head()
        .asScala
        .toList
        .map(_.asScala)
      result shouldBe List(Set("a"), Set("b"))
    }

    "extract values from map" in {
      val result: Iterable[String] = graph
        .V()
        .hasLabel("software")
        .group(By(Key[String]("lang")), By(Key[String]("name")))
        .selectValues
        .head()
        .asScala
        .flatMap(_.asScala)
      result shouldBe List("lop", "ripple")
    }

    "extract values from map entry" in {
      val result: List[String] = graph
        .V()
        .hasLabel("software")
        .group(By(__().value(Key[String]("name"))),
               By(__().in("created").value(Key[String]("name"))))
        .unfold[JMap.Entry[String, String]]()
        .selectValues
        .toList()

      result shouldBe List("josh", "marko")
    }

    "extract values from path" in {
      val result: java.util.List[Any] = graph
        .V(1)
        .as("a")
        .outE()
        .as("b")
        .path()
        .selectValues
        .head()
      result.get(0).toString shouldBe "v[1]"
      result.get(1).toString shouldBe "e[9][1-created->3]"
    }
  }
}
