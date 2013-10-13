package com.tinkerpop.gremlin.scala.filter

import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import com.tinkerpop.gremlin.scala.TestGraph
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RetainStepTest extends FunSpec with ShouldMatchers with TestGraph {

  it("retains everything except what is in the supplied collection") {
    val retainList = List(graph.v(1), graph.v(2), graph.v(3))
    graph.V.retain(retainList).toList should be(List(graph.v(3), graph.v(2), graph.v(1)))
  }

  ignore("retains everything except what is in named step") {
    //    not currently supported because RetainFilterPipe uses ElementHelper.areEqual to compare two elements, which compares if the classes are equal.
    //   *  I'll open a pull request to fix that in blueprints shortly...
  }

}