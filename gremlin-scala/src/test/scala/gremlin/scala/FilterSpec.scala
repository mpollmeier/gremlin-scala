package gremlin.scala

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import TestGraph._
import org.scalatest.Matchers
import org.scalatest.WordSpec

class FilterSpec extends WordSpec with Matchers {

  "filter" in new Fixture {
    graph.V
      .filter(_.value(Age).is(P.gt(30)))
      .value(Name)
      .toSet should be(Set("josh", "peter"))
  }

  "filterNot" in new Fixture {
    graph.V
      .filterNot(_.value(Age).is(P.gt(30)))
      .value(Name)
      .toSet should be(Set("lop", "marko", "vadas", "ripple"))
  }

  "filter on end type" in new Fixture {
    graph.V
      .filterOnEnd(_.property(Age).orElse(0) > 30)
      .value(Name)
      .toSet should be(Set("josh", "peter"))
  }

  "has" in new Fixture {
    graph.V.has(Age, 35).value(Name).toSet shouldBe Set("peter")
  }

  "has - sugar" in new Fixture {
    implicit val g = TinkerGraph.open.asScala
    g + ("software", Name -> "blueprints", Created -> 2010)

    g.V
      .has(Name -> "blueprints")
      .head <-- "dependsOn" --- (g + ("software", Name -> "gremlin", Created -> 2009))
    g.V
      .has(Name -> "gremlin")
      .head <-- "dependsOn" --- (g + ("software", Name -> "gremlinScala"))
    g.V
      .has(Name -> "gremlinScala")
      .head <-- "createdBy" --- (g + ("person", Name -> "mpollmeier"))

    g.V.toList().size shouldBe 4
    g.V.hasLabel("software").toList().size shouldBe 3
    g.V.hasLabel("person").toList().size shouldBe 1

    g.E.toList().size shouldBe 3
    g.E.hasLabel("dependsOn").toList().size shouldBe 2
    g.E.hasLabel("createdBy").toList().size shouldBe 1

    g.asJava.close()
  }

  "hasNot" in new Fixture {
    graph.V.hasNot(Age, 35).value(Name).toSet shouldBe Set("lop",
                                                           "marko",
                                                           "josh",
                                                           "vadas",
                                                           "ripple")
  }

  "coin all" in new Fixture {
    graph.V.coin(1.0d).value(Name).toSet shouldBe Set("lop",
                                                      "marko",
                                                      "josh",
                                                      "vadas",
                                                      "ripple",
                                                      "peter")
  }

  "coin nothing" in new Fixture {
    graph.V.coin(0.0d).value(Name).toSet shouldBe Set()
  }

  "dedup success" in new Fixture {
    val a = StepLabel[Edge]()
    val b = StepLabel[Vertex]()

    graph.V.outE.as(a).inV.as(b)
      .select(a).select(b).order(By(Name))
      .value(Name)
      .dedup()
      .toList shouldBe List("josh", "lop", "ripple", "vadas")
  }

  "drop success" in new Fixture {
    graph.V.outE.drop().toSet shouldBe Set()
  }


  "is usage" in new Fixture {
    graph.V.value(Age).is(P.lte(30)).toSet shouldBe Set(27, 29)
  }

  "range success" in new Fixture {
    val markoVertexId = 1
    graph.V(markoVertexId).out("knows").out("created").range(0, 1).value(Name).toSet should contain oneOf("lop", "ripple")
  }

  "simple path" in new Fixture {
    val markoVertexId = 1
    graph.V(markoVertexId).out("created").in("created").simplePath.value(Name).toSet shouldBe Set("josh", "peter")
  }

  "tail" in new Fixture {
    graph.V.value(Name).order.tail(2).toSet shouldBe Set("ripple", "vadas")
  }

  "where" in new Fixture {
    val a = StepLabel[Vertex]()
    graph.V.as(a).out("created").where(_.as(a).value(Name).is("josh")).in("created").value(Name).toSet shouldBe Set("marko", "josh", "peter")
  }

  trait Fixture {
    val graph = TinkerFactory.createClassic.asScala
  }
}
