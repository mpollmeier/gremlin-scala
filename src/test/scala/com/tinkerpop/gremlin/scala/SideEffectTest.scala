package com.tinkerpop.gremlin.scala

import com.tinkerpop.blueprints._
import com.tinkerpop.gremlin.scala._
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec
import com.tinkerpop.gremlin.Tokens.T._
import scala.collection.JavaConversions._
import scala.collection.mutable
import java.util.{ Map ⇒ JMap, HashMap ⇒ JHashMap, Collection ⇒ JCollection }
import com.tinkerpop.pipes.util.structures.Tree

class SideEffectTest extends FunSpec with ShouldMatchers with TestGraph {

  describe("sideEffect") {
    it("executes a side effect") {
      var youngest = Int.MaxValue

      graph.V.has("age").sideEffect { vertex ⇒
        val age = vertex.getProperty[Integer]("age")
        if (age < youngest) youngest = age
      }.toList

      youngest should be(27)
    }
  }

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
      graph.v(1).out.aggregate(buffer)(getName).iterate()

      buffer.toSet should be(Set("vadas", "josh", "lop"))
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

    it("applies a transformation to each element before filling the buffer") {
      val buffer = mutable.Buffer.empty[String]
      graph.v(1).out.store[String](buffer, getName).iterate()

      buffer.toSet should be(Set("vadas", "josh", "lop"))
    }
  }

  describe("optional") {
    //TODO: fix - standard pipe doesn't seem to work...?
    ignore("adds the output of a named step to the current step") {
      val namedStep = "named step"
      val overThirty = (v: Vertex) ⇒ v.getProperty[Int]("age") > 30

      graph.V.filter(overThirty).toList should be(List(v6, v4))

      graph.V.filter(overThirty).as(namedStep).out.optional(namedStep).toList should be(
        List(v6, v4, v3, v5, v3)
      )
    }
  }

  describe("groupBy") {
    it("groups tinkerpop team by age range") {
      val ageMap = new JHashMap[String, JCollection[String]]
      graph.V.groupBy(ageMap)(keyFunction = ageRange, valueFunction = getName).iterate()

      val result: Map[String, JCollection[String]] = ageMap.toMap
      result(underThirty).toSet should be(Set("vadas", "marko"))
      result(overThirty).toSet should be(Set("peter", "josh"))
      result(unknown).toSet should be(Set("lop", "ripple"))
    }
  }

  describe("tree") {
    it("stores the tree formed by the traversal as a map") {
      val tree = graph.v(1).out.out.tree.cap.toList.head.asInstanceOf[Tree[Vertex]]
      tree.keys should be(Set(v1))
      tree.toString should be("{v[1]={v[4]={v[3]={}, v[5]={}}}}")
      //TODO: reimplement with proper types and proper test
    }
  }

  describe("groupCount") {
    it("counts each traversed object and stores it in a map") {
      val counts: mutable.Map[Vertex, Int] = graph.V.out("created").groupCount.cap.toList.head.asInstanceOf[java.util.HashMap[Vertex, Int]]
      counts should be(
        mutable.Map(
          (v3, 3),
          (v5, 1))
      )
    }
  }

  def getName(v: Vertex) = v.getProperty[String]("name")
  def getAge(v: Vertex) = v.getProperty[Integer]("age")

  val underThirty = "under thirty"
  val overThirty = "over thirty"
  val unknown = "unknown"

  def ageRange(v: Vertex): String =
    v.property[Integer]("age") match {
      case Some(age) if (age < 30)  ⇒ underThirty
      case Some(age) if (age >= 30) ⇒ overThirty
      case _                        ⇒ unknown
    }
}
