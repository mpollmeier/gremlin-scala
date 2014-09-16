package com.tinkerpop.gremlin.scala

import org.scalatest.matchers.ShouldMatchers
import shapeless.test.illTyped

class TraversalSpec extends TestBase {

  describe("vertex steps") {
    it("gets all vertices") {
      gs.V.toList should have size (6)
      //graph.V.path.toList foreach {_: Vertex :: HNil ⇒ [> compiles <] }
    }

    it("follows the out vertices") {
      v(1).out.value[String]("name").toSet should be(Set("vadas", "josh", "lop"))
      v(1).out("knows").value[String]("name").toSet should be(Set("vadas", "josh"))

      v(1).out.out.value[String]("name").toSet should be(Set("ripple", "lop"))
      v(1).out.out("created").value[String]("name").toSet should be(Set("ripple", "lop"))
    }

    it("follows the in vertices") {
      v(3).in.value[String]("name").toSet should be(Set("marko", "josh", "peter"))
      v(3).in("created").value[String]("name").toSet should be(Set("marko", "josh", "peter"))

      v(3).in.in.value[String]("name").toSet should be(Set("marko"))
      v(3).in.in("knows").value[String]("name").toSet should be(Set("marko"))
    }

    it("follows both in and out vertices") {
      v(4).both.value[String]("name").toSet should be(Set("marko", "ripple", "lop"))
      v(4).both("knows").value[String]("name").toSet should be(Set("marko"))

      v(4).both.both.value[String]("name").toSet should be(Set("marko", "lop", "peter", "josh", "vadas"))
      v(4).both.both("knows").value[String]("name").toSet should be(Set("josh", "vadas"))
    }

    it("follows out edges") {
      v(1).outE.toSet map (_.label) should be(Set("knows", "created"))
      v(1).outE("knows").toSet map (_.label) should be(Set("knows"))

      v(1).out.outE.toSet map (_.label) should be(Set("created"))
      v(1).out.outE("created").toSet map (_.label) should be(Set("created"))
    }

    it("follows in edges") {
      v(3).inE.toSet map (_.label) should be(Set("created"))
      v(3).inE("created").toSet map (_.label) should be(Set("created"))

      v(3).in.inE.toSet map (_.label) should be(Set("knows"))
      v(3).in.inE("knows").toSet map (_.label) should be(Set("knows"))
    }

    it("follows both edges") {
      v(4).bothE.toSet map (_.label) should be(Set("created", "knows"))
      v(4).bothE("knows").toSet map (_.label) should be(Set("knows"))

      v(4).in.bothE.toSet map (_.label) should be(Set("knows", "created"))
      v(4).in.bothE("knows").toSet map (_.label) should be(Set("knows"))
    }

    it("does not allow edge steps") {
      illTyped {"""v(1).inV"""}
      illTyped {"""v(1).out.inV"""}
    }
  }

  describe("edge steps") {
    it("gets all edges") {
      gs.E.toList should have size (6)
    }
    
    it("follows in vertex") {
      //TODO: wait until this is consistent in T3 between Vertex and Edge
      //currently Vertex.outE returns a Traversal, Edge.inV doesnt quite exist
      //e(7).inV//.out.value[String]("name").toSet should be(Set("vadas", "josh", "lop"))
      //v(1).out("knows").value[String]("name").toSet should be(Set("vadas", "josh"))
      //v(1).out(1, "knows").value[String]("name").toSet should be(Set("vadas"))

      //v(1).out.out.value[String]("name").toSet should be(Set("ripple", "lop"))
      //v(1).out.out("created").value[String]("name").toSet should be(Set("ripple", "lop"))
      //v(1).out.out(1, "created").value[String]("name").toSet should be(Set("lop"))
    }

    //it("does not allow vertex steps") {
      //illTyped {"""v(1).inV"""}
      //TODO: all vertex steps: out, outE, in, inE, both
    //}
  }

  describe("common steps") {
    describe("value") {
      it("gets values") {
        gs.V.value[Int]("age").toSet should be(Set(27, 29, 32, 35))
      }

      it("gets values, defaults if not set") {
        gs.V.value[Int]("age", 99).toSet should be(Set(27, 29, 32, 35, 99))
      }
    }

    describe("order") {
      it("sorts by natural order") {
        gs.V.value[Int]("age").order.toList should be(List(27, 29, 32, 35))
      }

      it("sorts by provided comparator") {
        gs.V.value[Int]("age").order(_ < _).toList should be(List(27, 29, 32, 35))
        gs.V.value[Int]("age").order(_ > _).toList should be(List(35, 32, 29, 27))
      }
    }
  }

}
