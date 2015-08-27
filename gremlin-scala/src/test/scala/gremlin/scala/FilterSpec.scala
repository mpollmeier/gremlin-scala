package gremlin.scala

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph

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
    def name(n: String) = "name" -> n
    def created(n: Int) = "created" -> n
    def label(n: String) = n

    val g = TinkerGraph.open.asScala
    g + (label("software"), name("blueprints"), created(2010))
    g.V.has(name("blueprints")).head <-- "dependsOn" --- (g + (label("software"), name("gremlin"), created(2009)))
    g.V.has(name("gremlin")).head <-- "dependsOn" --- (g + (label("software"), name("gremlinScala")))
    g.V.has(name("gremlinScala")).head <-- "createdBy" --- (g + (label("person"), name("mpollmeier")))

    g.V.toList().size shouldBe 4
    g.V.hasLabel("software").toList().size shouldBe 3
    g.V.hasLabel("person").toList().size shouldBe 1

    g.E.toList().size shouldBe 3
    g.E.hasLabel("dependsOn").toList().size shouldBe 2
    g.E.hasLabel("createdBy").toList().size shouldBe 1

    g.asJava.close()
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
