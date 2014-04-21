package com.tinkerpop.gremlin.scala.transform

import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory
import com.tinkerpop.gremlin.test.ComplianceTest
import com.tinkerpop.gremlin.scala._
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec

class TraversalStepsTest extends FunSpec with ShouldMatchers with TestGraph {

  describe("vertice adjacency") {
    it("gets all vertices") {
      graph.V.toList should have size (6)
    }

    it("gets the out vertices") {
      graph.v(1).out.property("name").toSet should be(Set("vadas", "josh", "lop"))
      graph.v(1).out(1).property("name").toSet should be(Set("lop"))
    }

    it("gets the in vertices") {
      graph.v(3).in.property("name").toSet should be(Set("marko", "josh", "peter"))
      graph.v(3).in(1).property("name").toSet should be(Set("josh"))
    }

    it("gets both in and out vertices") {
      graph.v(4).both.property("name").toSet should be(Set("marko", "ripple", "lop"))
      graph.v(4).both(1).property("name").toSet should be(Set("marko"))
    }
  }

  describe("edge adjacency") {
    it("gets all edges") {
      graph.E.toSet should have size (6)
    }

    it("follows out edges") {
      graph.v(1).outE.label.toSet should be(Set("knows", "knows", "created"))
      graph.v(1).outE(1).label.toSet should be(Set("created"))

      graph.v(1).outE("knows", "created").inV.name.toSet should be(Set("vadas", "josh", "lop"))
    }

    it("follows in edges") {
      graph.v(3).inE.label.toSet should be(Set("created", "created", "created"))
      graph.v(3).inE(1).label.toSet should be(Set("created"))
    }

    it("follows both edges") {
      graph.v(4).bothE.label.toSet should be(Set("knows", "created", "created"))
      graph.v(4).bothE(1).label.toSet should be(Set("knows"))

      graph.v(4).bothE("created").label.toSet should be(Set("created", "created"))
      graph.v(4).bothE(1, "created").label.toSet should be(Set("created"))
    }
  }

  describe("edge / vertex adjacency") {
    it("follows out edges and in vertices") {
      graph.v(1).outE.inV.property("name").toSet should be(Set("vadas", "josh", "lop"))
    }

    it("follows in edges and out vertices") {
      graph.v(2).inE.outV.property("name").toSet should be(Set("marko"))
    }
  }

  describe("vertex edge label adjacency") {
    it("follows out edges by label") {
      graph.v(1).out("knows").property("name").toSet should be(Set("vadas", "josh"))
      graph.v(1).outE("knows").inV.property("name").toSet should be(Set("vadas", "josh"))

      graph.v(1).out(1, "knows").property("name").toSet should be(Set("vadas"))
      graph.v(1).outE(1, "knows").inV.property("name").toSet should be(Set("vadas"))
    }

    it("follows out edges by labels") {
      graph.v(1).out("knows", "created").property("name").toSet should be(Set("vadas", "josh", "lop"))
      graph.v(1).outE("knows", "created").inV.property("name").toSet should be(Set("vadas", "josh", "lop"))
    }

    it("follows in edges by label") {
      graph.v(3).in("created").property("name").toSet should be(Set("marko", "josh", "peter"))
      graph.v(3).in(1, "created").property("name").toSet should be(Set("josh"))

      graph.v(3).inE("created").outV.property("name").toSet should be(Set("marko", "josh", "peter"))
      graph.v(3).inE(1, "created").outV.property("name").toSet should be(Set("josh"))
    }

    it("traverses multiple steps") {
      graph.v(1).out.out().property("name").toSet should be(Set("ripple", "lop"))
      graph.v(1).out.out().out().property("name").toList should be(Nil)
    }
  }

}
