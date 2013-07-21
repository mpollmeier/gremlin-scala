package com.tinkerpop.gremlin.scala.transform

import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory
import scala.collection.JavaConversions._
import com.tinkerpop.gremlin.test.ComplianceTest
import com.tinkerpop.gremlin.scala._
import com.tinkerpop.blueprints.Vertex
import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner
import com.tinkerpop.gremlin.scala.ScalaVertex._

@RunWith(classOf[JUnitRunner])
class MapStepTest extends FunSpec with ShouldMatchers with TestGraph {

  it("maps the label of an edge to it's length") {
    edges.label.map { _.size }.toScalaList should be(List(7, 5, 7, 5, 7, 7))
  }

  it("maps the age property of all vertices") {
    vertices.property("age").map { age: Integer ⇒ age * 2 }.toScalaList should be(List(54, 58, 70, 64))
  }

  it("gets the name and the age as tuples") {
    vertices.map { v: ScalaVertex ⇒ (v("name"), v("age")) }.toScalaList should be(List(
      ("lop", null),
      ("vadas", 27),
      ("marko", 29),
      ("peter", 35),
      ("ripple", null),
      ("josh", 32)))
  }

}
