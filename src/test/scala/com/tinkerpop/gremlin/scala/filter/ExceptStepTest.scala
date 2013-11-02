package com.tinkerpop.gremlin.scala.filter

import scala.collection.mutable
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import com.tinkerpop.gremlin.scala.TestGraph
import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.gremlin.scala._

class ExceptStepTest extends FunSpec with ShouldMatchers with TestGraph {

  it("emits everything except what is in the supplied collection") {
    val excludeList = List(graph.v(1), graph.v(2), graph.v(3))
    graph.V.except(excludeList).toList should be(List(graph.v(6), graph.v(5), graph.v(4)))
  }

  it("works nicely with aggregate") {
    val buffer = mutable.Buffer.empty[Vertex]
    val result = graph.v(1).out.aggregate(buffer).in.except(buffer).toList.toSet

    result.contains(graph.v(1)) should be(true)
    result.contains(graph.v(6)) should be(true)
    List(2, 3, 4) foreach { i ⇒
      buffer.toSet.contains(graph.v(i)) should be(true)
    }
  }

  ignore("emits everything except what is in named step") {
    //    not currently supported because ExceptFilterPipe uses ElementHelper.areEqual to compare two elements, which compares if the classes are equal.
    //   *  I'll open a pull request to fix that in blueprints shortly...

    // prerequisite tests
    //g.V.has('age',T.lt,30).as('x').out('created').in('created').except('x')
    //    print(graph.V.has("age", lt, 30))
    //    print(graph.V.has("age", lt, 30).out("created").in("created"))
    //    println("""graph.V.has("age", lt, 30).as("x").out("created").in("created").except("x")""")
    //    print(graph.V.has("age", lt, 30).as("x").out("created").in("created").except("x"))

    //    val v3 = graph.v(3)
    //    graph.v(1).out.toList.toSet.contains(v3) should be(true)
    //    graph.v(1).out.out.toList.toSet.contains(v3) should be(true)
    //
    //    print(graph.v(1).out)
    //    print(graph.v(1).out.out)
    //    println("""graph.v(1).out.as("1").out.except("1")""")
    //    print(graph.v(1).out.as("1").out.except("1"))
    //
    //    val namedStep = "contains v[3]"
    //    graph.v(1).out.as(namedStep).out.except(namedStep).toList.toSet.contains(v3) should be(false)

  }

  ignore("emits everything except what is in named step - doesn't work") {
    //    not currently supported because ExceptFilterPipe uses ElementHelper.areEqual to compare two elements, which compares if the classes are equal.
    //   *  I'll open a pull request to fix that in blueprints shortly...

    // prerequisite tests
    val v3 = graph.v(3)
    graph.v(1).out.toList.toSet.contains(v3) should be(true)
    graph.v(1).out.out.toList.toSet.contains(v3) should be(true)

    //    print(graph.v(1).out)
    //    print(graph.v(1).out.out)
    //    println("""graph.v(1).out.as("1").out.except("1")""")
    //    print(graph.v(1).out.as("1").out.except("1"))

    val namedStep = "contains v[3]"
    graph.v(1).out.as(namedStep).out().except(namedStep).toList.toSet.contains(v3) should be(false)

  }

}