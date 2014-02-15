package com.tinkerpop.gremlin.scala

import com.tinkerpop.gremlin.scala._
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec
import com.tinkerpop.gremlin.structure._
import shapeless._
import shapeless.test.illTyped
import GremlinScala._

class TraversalStepsTest extends GremlinSpec {

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
    }
  }

  //describe("edge steps") {
    //it("gets all edges") {
      //graph.E.toList should have size (6)
    //}


    //it("does not allow vertex steps") {
      //illTyped {"""v(1).inV"""}
      //TODO: all vertex steps: out, outE, in, inE, both
    //}
  //}

  //describe("edge / vertex adjacency") {
    //it("follows out edges and in vertices") {
      //graph.v(1).outE.inV.property("name").toList should be(List("vadas", "josh", "lop"))
    //}

    //it("follows in edges and out vertices") {
      //graph.v(2).inE.outV.property("name").toList should be(List("marko"))
    //}
  //}

  //describe("vertex edge label adjacency") {
    //it("follows out edges by label") {
      //graph.v(1).out("knows").property("name").toList should be(List("vadas", "josh"))
      //graph.v(1).outE("knows").inV.property("name").toList should be(List("vadas", "josh"))

      //graph.v(1).out(1, "knows").property("name").toList should be(List("vadas"))
      //graph.v(1).outE(1, "knows").inV.property("name").toList should be(List("vadas"))
    //}

    //it("follows out edges by labels") {
      //graph.v(1).out("knows", "created").property("name").toList should be(List("vadas", "josh", "lop"))
      //graph.v(1).outE("knows", "created").inV.property("name").toList should be(List("vadas", "josh", "lop"))
    //}

    //it("follows in edges by label") {
      //graph.v(3).in("created").property("name").toList should be(List("marko", "josh", "peter"))
      //graph.v(3).in(1, "created").property("name").toList should be(List("marko"))

      //graph.v(3).inE("created").outV.property("name").toList should be(List("marko", "josh", "peter"))
      //graph.v(3).inE(1, "created").outV.property("name").toList should be(List("marko"))
    //}

    //it("traverses multiple steps") {
      //graph.v(1).out.out().property("name").toList should be(List("ripple", "lop"))
      //graph.v(1).out.out().out().property("name").toList should be(Nil)
    //}
  //}

  //describe("properties") {
    //it("gets a property") {
      //v(1).property[String]("name").head.get should be("marko")
      //v(1).property[String]("doesnt exit").head.isPresent should be(false)
    //}

    //it("sets a property") {
      //v(1).head.setProperty("name", "updated")
      //v(1).property[String]("name").head.get should be("updated")
    //}

    //it("does not allow vertex steps") {
      //illTyped {"""v(1)."""}
    //}
  //}

}
