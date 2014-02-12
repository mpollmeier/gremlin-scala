package com.tinkerpop.gremlin.scala

import com.tinkerpop.gremlin.scala._
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec
import com.tinkerpop.blueprints._
import shapeless._
import GremlinScala._

class TraversalStepsTest extends GremlinSpec {

  describe("vertex steps") {
    it("gets all vertices") {
      gs.V.toList should have size (6)
      //TODO: path step
      //graph.V.path.toList foreach {_: Vertex :: HNil ⇒ [> compiles <] }
    }

    it("follows the out vertices") {
      v(1).out.property[String]("name").toSet.unroll should be(Set("vadas", "josh", "lop"))
      v(1).out(1).property[String]("name").toSet.unroll should be(Set("lop"))
    }

    //it("gets the in vertices") {
      //graph.v(3).in.property("name").toList should be(List("marko", "josh", "peter"))
      //graph.v(3).in(1).property("name").toList should be(List("marko"))
    //}

    //it("gets both in and out vertices") {
      //graph.v(4).both.property("name").toList should be(List("marko", "ripple", "lop"))
      //graph.v(4).both(1).property("name").toList should be(List("marko"))
    //}

    //TODO: illTyped
  }

  //describe("edge adjacency") {
    //it("gets all edges") {
      //graph.E.toList should have size (6)
    //}

    //it("follows out edges") {
      //graph.v(1).outE.label.toList should be(List("knows", "knows", "created"))
      //graph.v(1).outE(1).label.toList should be(List("knows"))

      //graph.v(1).outE("knows", "created").inV.name.toList should be(List("vadas", "josh", "lop"))
    //}

    //it("follows in edges") {
      //graph.v(3).inE.label.toList should be(List("created", "created", "created"))
      //graph.v(3).inE(1).label.toList should be(List("created"))
    //}

    //it("follows both edges") {
      //graph.v(4).bothE.label.toList should be(List("knows", "created", "created"))
      //graph.v(4).bothE(1).label.toList should be(List("knows"))

      //graph.v(4).bothE("created").label.toList should be(List("created", "created"))
      //graph.v(4).bothE(1, "created").label.toList should be(List("created"))
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

  describe("properties") {
    it("gets a property") {
      v(1).property[String]("name").head.get should be("marko")
      v(1).property[String]("doesnt exit").head.isPresent should be(false)
    }

    it("sets a property") {
      v(1).head.setProperty("name", "updated")
      v(1).property[String]("name").head.get should be("updated")
    }
  }

}
