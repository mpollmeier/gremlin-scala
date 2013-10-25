package com.tinkerpop.gremlin.scala.transform

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import com.tinkerpop.gremlin.scala.ScalaVertex
import com.tinkerpop.gremlin.scala.TestGraph
import com.tinkerpop.gremlin.Tokens
import com.tinkerpop.pipes.transform.TransformPipe.Order

class GatherStepTest extends FunSpec with ShouldMatchers with TestGraph {

  it("gathers elements up to that step") {
    val pipe = graph.v(1).out.gather
    val list = pipe.next()
    pipe.hasNext should be(false)

    val ids = list.map(_.getId).map(_.asInstanceOf[String])
    ids.sorted should be(List("2", "3", "4"))
  }

}
