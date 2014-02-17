package com.tinkerpop.gremlin.scala

import com.tinkerpop.gremlin.scala._
import org.scalatest.matchers.ShouldMatchers
import com.tinkerpop.gremlin.structure._
import shapeless._
import shapeless.test.illTyped
import GremlinScala._

class TraversalStepsTest extends GremlinSpec {

  describe("properties") {
    it("gets properties") {
      v(1).propertyKeys should be (Set("name", "age"))
      v(1).property[String]("name").get should be("marko")
      v(1).property[String]("doesnt exit").isPresent should be(false)
      v(1).properties should be (Map("name" -> "marko", "age" -> 29))

      e(7).propertyKeys should be (Set("weight"))
      e(7).property[Float]("weight").get should be (0.5)
      e(7).property[Float]("doesnt exit").isPresent should be(false)
      e(7).properties should be (Map("weight" -> 0.5))
    }

    it("sets a property") {
      v(1).setProperty("vertexProperty", "updated")
      v(1).property[String]("vertexProperty").get should be("updated")

      e(7).setProperty("edgeProperty", "updated")
      e(7).property[String]("edgeProperty").get should be("updated")
    }
  }

  describe("vertex steps") {
    it("gets all vertices") {
      gs.V.toList should have size (6)
      //graph.V.path.toList foreach {_: Vertex :: HNil ⇒ [> compiles <] }
    }

    it("follows the out vertices") {
      v(1).out.property[String]("name").toSet.unroll should be(Set("vadas", "josh", "lop"))
      v(1).out("knows").property[String]("name").toSet.unroll should be(Set("vadas", "josh"))
      v(1).out(1, "knows").property[String]("name").toSet.unroll should be(Set("vadas"))

      v(1).out.out.property[String]("name").toSet.unroll should be(Set("ripple", "lop"))
      v(1).out.out("created").property[String]("name").toSet.unroll should be(Set("ripple", "lop"))
      v(1).out.out(1, "created").property[String]("name").toSet.unroll should be(Set("lop"))
    }

    it("follows the in vertices") {
      v(3).in.property[String]("name").toSet.unroll should be(Set("marko", "josh", "peter"))
      v(3).in("created").property[String]("name").toSet.unroll should be(Set("marko", "josh", "peter"))
      v(3).in(1, "created").property[String]("name").toSet.unroll should be(Set("josh"))

      v(3).in.in.property[String]("name").toSet.unroll should be(Set("marko"))
      v(3).in.in("knows").property[String]("name").toSet.unroll should be(Set("marko"))
      v(3).in.in(1).property[String]("name").toSet.unroll should be(Set("marko"))
    }

    it("follows both in and out vertices") {
      v(4).both.property[String]("name").toSet.unroll should be(Set("marko", "ripple", "lop"))
      v(4).both("knows").property[String]("name").toSet.unroll should be(Set("marko"))
      v(4).both(1, "knows").property[String]("name").toSet.unroll should be(Set("marko"))

      v(4).both.both.property[String]("name").toSet.unroll should be(Set("marko", "lop", "peter", "josh", "vadas"))
      v(4).both.both("knows").property[String]("name").toSet.unroll should be(Set("josh", "vadas"))
      v(4).both.both(1, "knows").property[String]("name").toSet.unroll should be(Set("vadas"))
    }

    it("follows out edges") {
      v(1).outE.toSet map (_.getLabel) should be(Set("knows", "created"))
      v(1).outE("knows").toSet map (_.getLabel) should be(Set("knows"))
      v(1).outE(1, "knows").toSet map (_.getLabel) should be(Set("knows"))

      v(1).out.outE.toSet map (_.getLabel) should be(Set("created"))
      v(1).out.outE("created").toSet map (_.getLabel) should be(Set("created"))
      v(1).out.outE(1, "created").toSet map (_.getLabel) should be(Set("created"))
    }

    it("follows in edges") {
      v(3).inE.toSet map (_.getLabel) should be(Set("created"))
      v(3).inE("created").toSet map (_.getLabel) should be(Set("created"))
      v(3).inE(1, "created").toSet map (_.getLabel) should be(Set("created"))

      v(3).in.inE.toSet map (_.getLabel) should be(Set("knows"))
      v(3).in.inE("knows").toSet map (_.getLabel) should be(Set("knows"))
      v(3).in.inE(1, "knows").toSet map (_.getLabel) should be(Set("knows"))
    }

    it("follows both edges") {
      v(4).bothE.toSet map (_.getLabel) should be(Set("created", "knows"))
      v(4).bothE("knows").toSet map (_.getLabel) should be(Set("knows"))
      v(4).bothE(1, "knows").toSet map (_.getLabel) should be(Set("knows"))

      v(4).in.bothE.toSet map (_.getLabel) should be(Set("knows", "created"))
      v(4).in.bothE("knows").toSet map (_.getLabel) should be(Set("knows"))
      v(4).in.bothE(1, "knows").toSet map (_.getLabel) should be(Set("knows"))
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
      //e(7).inV//.out.property[String]("name").toSet.unroll should be(Set("vadas", "josh", "lop"))
      //v(1).out("knows").property[String]("name").toSet.unroll should be(Set("vadas", "josh"))
      //v(1).out(1, "knows").property[String]("name").toSet.unroll should be(Set("vadas"))

      //v(1).out.out.property[String]("name").toSet.unroll should be(Set("ripple", "lop"))
      //v(1).out.out("created").property[String]("name").toSet.unroll should be(Set("ripple", "lop"))
      //v(1).out.out(1, "created").property[String]("name").toSet.unroll should be(Set("lop"))
    }

    //it("does not allow vertex steps") {
      //illTyped {"""v(1).inV"""}
      //TODO: all vertex steps: out, outE, in, inE, both
    //}
  }

}
