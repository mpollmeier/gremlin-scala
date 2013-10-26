package com.tinkerpop.gremlin.scala.transform

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers

import com.tinkerpop.gremlin.scala.ScalaVertex
import com.tinkerpop.gremlin.scala.TestGraph


import scala.collection.JavaConversions._;

import com.tinkerpop.gremlin.Tokens
import com.tinkerpop.pipes.transform.TransformPipe.Order
import com.tinkerpop.blueprints.Vertex

class ScatterStepTest extends FunSpec with ShouldMatchers with TestGraph {

  it("uses scatter to unroll elements that have been gathered before") {
    val pipe = graph.v(1).out.map(_.getProperty[String]("name")).gather.scatter

    val first = pipe.next()
    first should be("vadas")
    pipe.hasNext should be(true)

    val second = pipe.next()
    second should be("josh")
    pipe.hasNext should be(true)

    val third = pipe.next()
    third should be("lop")
    pipe.hasNext should be(false)
  }

}
