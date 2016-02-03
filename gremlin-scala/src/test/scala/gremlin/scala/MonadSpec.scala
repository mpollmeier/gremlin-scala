package gremlin.scala

import org.apache.tinkerpop.gremlin.structure.VertexProperty
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import org.scalatest.{WordSpec, Matchers}
import scala.collection.JavaConversions._
import shapeless._

class MonadSpec extends WordSpec with Matchers {

  "obeys the monad laws - simple case" in {
    // based on examples in http://devth.com/2015/monad-laws-in-scala/

    val graph = TinkerGraph.open.asScala

    def f(x: Int): GremlinScala[Int, HNil] = if (x < 10) __[Int]() else __(x * 2)
    def g(x: Int): GremlinScala[Int, HNil] = if (x > 50) __(x + 1) else __[Int]()

    withClue("left identity") {
      val a = 30
      val lhs = __(a).flatMap(f).head
      val rhs = f(a).head
      lhs shouldBe 60
      lhs shouldBe rhs
    }

    withClue("right identity") {
      def m = __(30)
      val lhs = m.flatMap { x: Int ⇒ __(x) }.head
      lhs shouldBe 30
      val rhs = m.head
      lhs shouldBe rhs
    }

    withClue("associativity") {
      def m = __(30)
      val lhs = m.flatMap(f).flatMap(g).head
      lhs shouldBe 61

      val rhs = m.flatMap(x ⇒ f(x).flatMap(g)).head
      rhs shouldBe 61

      lhs shouldBe rhs
    }
  }

  "obeys monad laws - complex case" in {
    val g = TinkerGraph.open()

    val TMSID = Key[String]("tmsID")
    val Hobby = Key[String]("hobby")
    val NumSiblings = Key[Int]("numSiblings")

    g + ("hobby", TMSID → "1234", Hobby → "stable mucking")
    g + ("sibs", TMSID → "1234", NumSiblings → 3)

    // val vs = g.V.has(TMSID).toList
    // val ps = GremlinScala(__(vs: _*)).properties().toSet
    // println(ps)
    // g + ("dummy", ps.toList.map(p => KeyValue(Key(p.key), p.value)): _*)
    // GremlinScala(__(vs: _*)).drop()

    // for {
    //   v <- g.V.has(TMSID)
    //   // ps <- v.start.properties()
    //   //   // _  = g + ("dummy", ps.toList.map(p => KeyValue(Key(p.key), p.value)): _*)
    //   //   // println(ps)
    // } yield {
    //   val a: Int = v
    // }

    // for {
    // v <- g.V.has(TMSID)
    // ps <- v.start.properties()
    //   // _  = g + ("dummy", ps.toList.map(p => KeyValue(Key(p.key), p.value)): _*)
    //   // println(ps)
    // } yield (v.id, ps)

    // TODO: remove group step?
    // TODO: remove TMSID property?
    // TODO: simplify wrap/__/union
    val traversal = for {
      vs ← g.V.has(TMSID).group { v ⇒ v.property[String]("tmsID") }.map(_.values.flatten)
      // vs ← g.V.has(TMSID)//.map(_.values())
      _ = println(s"*** vs == $vs ***")
      ps = vs.foldLeft(Set.empty[VertexProperty[_]]) {
        case (acc, v) ⇒ acc ++ v.properties().toSet
      }
      // ps = vs.start.foldLeft(Set.empty[VertexProperty[_]]) {
      //   case (acc, v) ⇒ acc ++ v.properties().toSet
      // }
      // _ = g + ("dummy", ps.toList.map(p ⇒ KeyValue(Key(p.key), p.value)): _*)
      // d ← vs.foldLeft(__[Vertex]) { (acc, v) ⇒ acc.union(g.V(v.id)) }.drop
    } yield ()

    traversal.iterate()
    // println(traversal.toList)
    // println(traversal.toList.map(_.toList))

    // println("V count after: " + g.V.count.head)
    // println("V valueMap after: " + g.V.has(TMSID).valueMap().toList)

    val ourVertexes = g.V.has(TMSID, "1234").toList
    // val properties: List[Any] = ourVertex.values(ourVertex.keys.toList: _*).toList

    // println(s"*** OURVERTEXES == ${ourVertexes} ***")
    // println(s"*** PROPERTIES.SIZE == ${properties.size} ***")
    // println(s"*** PROPERTIES == ${properties} ***")
    // assert(g.V.count.head == 1)
    // val result = ourVertexes.size == 1 &&
    // properties.size == 3 &&
    // properties.contains("stable mucking") &&
    // properties.contains(3) &&
    // properties.contains("1234")
    g.V.count.head shouldBe 1
  }
}
