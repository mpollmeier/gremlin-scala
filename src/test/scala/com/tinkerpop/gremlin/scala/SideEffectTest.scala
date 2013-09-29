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

  describe("aggregate") {
    it("fills a buffer greedily") {
      val buffer = mutable.Buffer.empty[Vertex]
      graph.v(1).out.aggregate(buffer).iterate()
      List(2, 3, 4) foreach { i ⇒
        buffer.toSet.contains(graph.v(i)) should be(true)
      }
    }

    it("applies a transformation to each element before filling the buffer") {
      val buffer = mutable.Buffer.empty[String]
      def fun(v: Vertex) = v.getProperty[String]("name")

      graph.v(1).out.aggregate[String](buffer, fun).iterate()

      buffer should be(mutable.Buffer("vadas", "josh", "lop"))
    }
  }

  describe("store") {
    it("fills a buffer lazily") {
      val buffer = mutable.Buffer.empty[Vertex]
      graph.v(1).out.store(buffer).iterate()
      List(2, 3, 4) foreach { i ⇒
        buffer.toSet.contains(graph.v(i)) should be(true)
      }
    }
  }

  it("applies a transformation to each element before filling the buffer") {
    val buffer = mutable.Buffer.empty[String]
    def fun(v: Vertex) = v.getProperty[String]("name")

    graph.v(1).out.store[String](buffer, fun).iterate()

    buffer should be(mutable.Buffer("vadas", "josh", "lop"))
  }
}
