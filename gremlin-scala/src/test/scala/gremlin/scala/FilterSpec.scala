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
    val g = TinkerGraph.open.asScala
    g + ("software", Name → "blueprints", Created → 2010)

    g.V
      .has(Name → "blueprints")
      .head <-- "dependsOn" --- (g + ("software", Name → "gremlin", Created → 2009))
    g.V
      .has(Name → "gremlin")
      .head <-- "dependsOn" --- (g + ("software", Name → "gremlinScala"))
    g.V
      .has(Name → "gremlinScala")
      .head <-- "createdBy" --- (g + ("person", Name → "mpollmeier"))

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

  trait Fixture {
    val graph = TinkerFactory.createClassic.asScala
  }
}
