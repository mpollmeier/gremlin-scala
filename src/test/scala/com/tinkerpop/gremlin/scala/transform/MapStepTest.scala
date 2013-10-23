package com.tinkerpop.gremlin.scala.transform

import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import com.tinkerpop.gremlin.scala.TestGraph
import org.scalatest.junit.JUnitRunner
import com.tinkerpop.gremlin.scala._

@RunWith(classOf[JUnitRunner])
class MapStepTest extends FunSpec with ShouldMatchers with TestGraph {

  it("maps the label of an edge to it's length") {
    edges.label.map { _.size }.toList should be(List(7, 5, 7, 5, 7, 7))
    edges.label.transform { _.size }.toList should be(List(7, 5, 7, 5, 7, 7))
  }

  it("maps the age property of all vertices") {
    vertices.property("age").map { age: Integer ⇒ age * 2 }.toList should be(List(54, 58, 70, 64))
  }

  it("gets the name and the age as tuples") {
    vertices.map { v ⇒ (v("name"), v("age")) }.toList should be(List(
      ("lop", null),
      ("vadas", 27),
      ("marko", 29),
      ("peter", 35),
      ("ripple", null),
      ("josh", 32)))
  }

}
