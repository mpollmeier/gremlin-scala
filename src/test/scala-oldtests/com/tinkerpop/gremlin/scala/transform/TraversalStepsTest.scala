package com.tinkerpop.gremlin.scala.transform

import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory
import com.tinkerpop.gremlin.test.ComplianceTest
import com.tinkerpop.gremlin.scala._
import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TraversalStepsTest extends FunSpec with ShouldMatchers with TestGraph {

  describe("vertice adjacency") {
    it("gets all vertices") {
      graph.V.toScalaList should have size (6)
    }

    it("gets the out vertices") {
      graph.v(1).out.property("name").toScalaList should be(List("vadas", "josh", "lop"))
      graph.v(1).out(1).property("name").toScalaList should be(List("vadas"))
    }

    it("gets the in vertices") {
      graph.v(3).in.property("name").toScalaList should be(List("marko", "josh", "peter"))
      graph.v(3).in(1).property("name").toScalaList should be(List("marko"))
    }

    it("gets both in and out vertices") {
      graph.v(4).both.property("name").toScalaList should be(List("marko", "ripple", "lop"))
      graph.v(4).both(1).property("name").toScalaList should be(List("marko"))
    }
  }

  describe("edge adjacency") {
    it("gets all edges") {
      graph.E.toScalaList should have size (6)
    }

    it("follows out edges") {
      graph.v(1).outE.label.toScalaList should be(List("knows", "knows", "created"))
      graph.v(1).outE(1).label.toScalaList should be(List("knows"))

      graph.v(1).outE("knows", "created").inV.name.toScalaList should be(List("vadas", "josh", "lop"))
    }

    it("follows in edges") {
      graph.v(3).inE.label.toScalaList should be(List("created", "created", "created"))
      graph.v(3).inE(1).label.toScalaList should be(List("created"))
    }

    it("follows both edges") {
      graph.v(4).bothE.label.toScalaList should be(List("knows", "created", "created"))
      graph.v(4).bothE(1).label.toScalaList should be(List("knows"))

      graph.v(4).bothE("created").label.toScalaList should be(List("created", "created"))
      graph.v(4).bothE(1, "created").label.toScalaList should be(List("created"))
    }
  }

  describe("edge / vertex adjacency") {
    it("follows out edges and in vertices") {
      graph.v(1).outE.inV.property("name").toScalaList should be(List("vadas", "josh", "lop"))
    }

    it("follows in edges and out vertices") {
      graph.v(2).inE.outV.property("name").toScalaList should be(List("marko"))
    }
  }

  describe("vertex edge label adjacency") {
    it("follows out edges by label") {
      graph.v(1).out("knows").property("name").toScalaList should be(List("vadas", "josh"))
      graph.v(1).outE("knows").inV.property("name").toScalaList should be(List("vadas", "josh"))

      graph.v(1).out(1, "knows").property("name").toScalaList should be(List("vadas"))
      graph.v(1).outE(1, "knows").inV.property("name").toScalaList should be(List("vadas"))
    }

    it("follows out edges by labels") {
      graph.v(1).out("knows", "created").property("name").toScalaList should be(List("vadas", "josh", "lop"))
      graph.v(1).outE("knows", "created").inV.property("name").toScalaList should be(List("vadas", "josh", "lop"))
    }

    it("follows in edges by label") {
      graph.v(3).in("created").property("name").toScalaList should be(List("marko", "josh", "peter"))
      graph.v(3).in(1, "created").property("name").toScalaList should be(List("marko"))

      graph.v(3).inE("created").outV.property("name").toScalaList should be(List("marko", "josh", "peter"))
      graph.v(3).inE(1, "created").outV.property("name").toScalaList should be(List("marko"))
    }

    it("traverses multiple steps") {
      graph.v(1).out.out.property("name").toScalaList should be(List("ripple", "lop"))
      graph.v(1).out.out.out.property("name").toScalaList should be(Nil)
    }
  }

}
