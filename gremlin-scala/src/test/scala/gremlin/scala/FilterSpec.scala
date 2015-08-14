package gremlin.scala

import org.apache.tinkerpop.gremlin.structure.T
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import org.scalatest.matchers.ShouldMatchers

class FilterSpec extends TestBase {

  it("filters") {
    graph.V
      .filter { _.valueOrElse("age", default = 0) > 30 }
      .values[String]("name").toSet should be(Set("josh", "peter"))
  }

  it("has") {
    graph.V.has("age", 35).value[String]("name").toSet shouldBe Set("peter")
  }

  it("has - sugar") {
    val person = "person"
    val software = "software"
    val blueprints = "name" -> "blueprints"
    val gremlin = "name" -> "gremlin"
    val gremlinScala = "name" -> "gremlin-scala"
    val mpollmeier = "name" -> "mpollmeier"
    val _2010 = "created" -> 2010
    val _2009 = "created" -> 2009
    val dependsOn = "dependsOn"
    val createdBy = "createdBy"

    val g = TinkerGraph.open.asScala
    g + (software, blueprints, _2010)
    g.V.has(blueprints).head <-- dependsOn --- (g + (software, gremlin, _2009))
    g.V.has(gremlin).head <-- dependsOn --- (g + (software, gremlinScala))
    g.V.has(gremlinScala).head <-- createdBy --- (g + (person, mpollmeier))

    g.V.toList().size shouldBe 4
    g.V.has(T.label, software).toList().size shouldBe 3
    g.V.has(T.label, person).toList().size shouldBe 1

    g.E.toList().size shouldBe 3
    g.E.has(T.label, dependsOn).toList().size shouldBe 2
    g.E.has(T.label, createdBy).toList().size shouldBe 1
  }

  it("hasNot") {
    graph.V.hasNot("age", 35).value[String]("name").toSet shouldBe Set("lop", "marko", "josh", "vadas", "ripple")
  }

  describe("dedup") {
    it("dedups") {
      v(1).out.in.dedup().toList should be(v(1).out.in.toSet.toList)
    }

    // TODO: fix
    ignore("dedups by a given uniqueness function", org.scalatest.Tag("foo")) {
      v(1).out.in
        .dedup().by(_.property[String]("lang").orElse(null))
        .values[String]("name").toList should be(List("marko"))
    }
  }

  //TODO redo with where step
  // describe("except") {
  //   it("emits everything but a given object") {
  //     v(1).out.except(v(2).vertex).values[String]("name")
  //       .toSet should be(Set("lop", "josh"))
  //   }

  //   it("emits everything but an 'except' list") { 
  //     v(1).out.except(List(v(2).vertex)).values[String]("name")
  //       .toSet should be(Set("lop", "josh"))
  //   }

  //   it("emits everything unless the vertex is in a given aggregate variable") {
  //     v(1).out.aggregate("x")
  //       .out.exceptVar("x")
  //       .values[String]("name").toSet should be (Set("ripple"))
  //   }

  //   it("emits everything unless a property is in a given aggregate variable") {
  //     v(1).out
  //       .aggregate("x").by(_.value[String]("name"))
  //       .out.values[String]("name").exceptVar("x")
  //       .toSet should be (Set("ripple"))
  //   }
  // }
}
