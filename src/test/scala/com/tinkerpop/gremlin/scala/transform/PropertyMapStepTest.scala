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

@RunWith(classOf[JUnitRunner])
class PropertyMapStepTest extends FunSpec with ShouldMatchers with TestGraph {

  it("maps the properties of one vertex") {
    graph.v(1).->.propertyMap.toList.iterator.next should be(
      Map("name" -> "marko", "age" -> 29))
  }

  it("maps the properties of everybody who knows marko") {
    List.fromIterator(graph.v(1).out("knows").propertyMap.toList.iterator) should be(
      List(
        Map("name" -> "vadas", "age" -> 27),
        Map("name" -> "josh", "age" -> 32))
    )
  }

  it("maps the properties for a given key for one vertex") {
    graph.v(1).->.propertyMap("name", "doesnt exit").toList.iterator.next should be(
      Map("name" -> "marko"))
  }

}
