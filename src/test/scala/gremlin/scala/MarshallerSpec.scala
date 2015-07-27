package gremlin.scala

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import org.scalatest.FunSpec
import org.scalatest.Matchers

case class ExampleClass(s: String, i: Int, l: Long, o: Option[String], seq: Seq[String], map: Map[String, String])

case class Person(name:String, age:Int, address:Address)
case class Address(zip:Int, street:String)

class MarshallerSpec extends FunSpec with Matchers {
  val example = ExampleClass(
    "some string",
    Int.MaxValue,
    Long.MaxValue,
    Some("option type"),
    Seq("test1", "test2"),
    Map("key1" -> "value1", "key2" -> "value2")
  )

  val jack = Person("Jack", 20, Address(11111, "Jefferson"))

  it("saves a case class as a vertex") {
    val graph = TinkerGraph.open
    val gs = GremlinScala(graph)

    val v = gs.save(example)

    v.valueMap should contain ("s" → example.s)
    v.valueMap should contain ("i" → example.i)
    v.valueMap should contain ("l" → example.l)
    v.valueMap should contain ("o" → example.o)
    v.valueMap should contain ("seq" → example.seq)
    v.valueMap should contain ("map" → example.map)

    v shouldBe ScalaVertex(gs.V.toList.head)
  }

  it("converts a Vertex into a case class") {
    val graph = TinkerGraph.open
    val gs = GremlinScala(graph)

    val v = gs.save(example)
    val v2 = v.load[ExampleClass]
    example shouldBe v2.get
  }

  it("saves a nested case class"){
    val graph = TinkerGraph.open
    val gs = GremlinScala(graph)

    val v = gs.save(jack)
    v.valueMap should contain ("name" → jack.name)
    v.valueMap should contain ("age" → jack.age)
    v.valueMap should contain ("address" → Map("street" -> "Jefferson", "zip" ->11111))
  }

  it("load nested case class"){
    val graph = TinkerGraph.open
    val gs = GremlinScala(graph)

    val v = gs.save(jack)

    v.load[Person].get shouldBe jack

  }
}
