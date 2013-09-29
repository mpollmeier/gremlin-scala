package com.tinkerpop.gremlin.scala

import com.tinkerpop.blueprints._
import com.tinkerpop.gremlin.scala._
import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec
import com.tinkerpop.gremlin.Tokens.T._
import org.scalatest.junit.JUnitRunner
import scala.collection.JavaConversions._
import scala.collection.mutable

@RunWith(classOf[JUnitRunner])
class SideEffectTest extends FunSpec with ShouldMatchers with TestGraph {

  describe("Aggregate") {
    it("fills a buffer greedily") {
      val buffer = mutable.Buffer.empty[Vertex]
      graph.v(1).out.aggregate(buffer).iterate()
      List(2, 3, 4) foreach { i ⇒
        buffer.toSet.contains(graph.v(i)) should be(true)
      }
    }

    it("works nicely with except") {
      val buffer = mutable.Buffer.empty[Vertex]
      val result = graph.v(1).out.aggregate(buffer).in.except(buffer).toScalaList.toSet

      result.contains(graph.v(1)) should be(true)
      result.contains(graph.v(6)) should be(true)
      List(2, 3, 4) foreach { i ⇒
        buffer.toSet.contains(graph.v(i)) should be(true)
      }
    }
  }

}
