package com.tinkerpop.gremlin.scala

import com.tinkerpop.blueprints._
import com.tinkerpop.gremlin.scala._
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec

class TransformStepsSpec extends FunSpec with ShouldMatchers with TestGraph {

  describe("path") {

    it("returns a list with all objects in the path") {
      graph.v(1).startPipe.out.path.toList should be(
        Seq(
          Seq(v1, v2),
          Seq(v1, v4),
          Seq(v1, v3)
        )
      )
    }
  }

}
