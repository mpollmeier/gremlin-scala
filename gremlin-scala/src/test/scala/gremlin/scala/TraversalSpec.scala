package gremlin.scala

import org.apache.tinkerpop.gremlin.process.traversal.Order
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import org.scalatest.{WordSpec, Matchers}
import shapeless.test.illTyped
import collection.JavaConversions._

class TraversalSpec extends WordSpec with Matchers {

  "vertex steps" can {
    "get all vertices" in new Fixture {
      graph.V.toList should have size 6
    }

    "follow the out vertices" in new Fixture {
      graph.V(1).out.values[String]("name").toSet should be(Set("vadas", "josh", "lop"))
      graph.V(1).out("knows").values[String]("name").toSet should be(Set("vadas", "josh"))

      graph.V(1).out.out.values[String]("name").toSet should be(Set("ripple", "lop"))
      graph.V(1).out.out("created").values[String]("name").toSet should be(Set("ripple", "lop"))
    }

    "follow the in vertices" in new Fixture {
      graph.V(3).in.values[String]("name").toSet should be(Set("marko", "josh", "peter"))
      graph.V(3).in("created").values[String]("name").toSet should be(Set("marko", "josh", "peter"))

      graph.V(3).in.in.values[String]("name").toSet should be(Set("marko"))
      graph.V(3).in.in("knows").values[String]("name").toSet should be(Set("marko"))
    }

    "follow both in and out vertices" in new Fixture {
      graph.V(4).both.values[String]("name").toSet should be(Set("marko", "ripple", "lop"))
      graph.V(4).both("knows").values[String]("name").toSet should be(Set("marko"))

      graph.V(4).both.both.values[String]("name").toSet should be(Set("marko", "lop", "peter", "josh", "vadas"))
      graph.V(4).both.both("knows").values[String]("name").toSet should be(Set("josh", "vadas"))
    }

    "follow out edges" in new Fixture {
      graph.V(1).outE.toSet map (_.label) should be(Set("knows", "created"))
      graph.V(1).outE("knows").toSet map (_.label) should be(Set("knows"))

      graph.V(1).out.outE.toSet map (_.label) should be(Set("created"))
      graph.V(1).out.outE("created").toSet map (_.label) should be(Set("created"))
    }

    "follow in edges" in new Fixture {
      graph.V(3).inE.toSet map (_.label) should be(Set("created"))
      graph.V(3).inE("created").toSet map (_.label) should be(Set("created"))

      graph.V(3).in.inE.toSet map (_.label) should be(Set("knows"))
      graph.V(3).in.inE("knows").toSet map (_.label) should be(Set("knows"))
    }

    "follow both edges" in new Fixture {
      graph.V(4).bothE.toSet map (_.label) should be(Set("created", "knows"))
      graph.V(4).bothE("knows").toSet map (_.label) should be(Set("knows"))

      graph.V(4).in.bothE.toSet map (_.label) should be(Set("knows", "created"))
      graph.V(4).in.bothE("knows").toSet map (_.label) should be(Set("knows"))
    }

    "does not allow edge steps" in new Fixture {
      illTyped { """graph.V(1).inV""" }
      illTyped { """graph.V(1).out.inV""" }
    }
  }

  "edge steps" can {
    "get all edges" in new Fixture {
      graph.E.toList should have size 6
    }

    //   "follow in vertex" in new Fixture {
    //     //TODO: wait until this is consistent in T3 between Vertex and Edge
    //     //currently Vertex.outE returns a Traversal, Edge.inV doesnt quite exist
    //     //e(7).inV//.out.values[String]("name").toSet should be(Set("vadas", "josh", "lop"))
    //     //graph.V(1).out("knows").values[String]("name").toSet should be(Set("vadas", "josh"))
    //     //graph.V(1).out(1, "knows").values[String]("name").toSet should be(Set("vadas"))

    //     //graph.V(1).out.out.values[String]("name").toSet should be(Set("ripple", "lop"))
    //     //graph.V(1).out.out("created").values[String]("name").toSet should be(Set("ripple", "lop"))
    //     //graph.V(1).out.out(1, "created").values[String]("name").toSet should be(Set("lop"))
    //   }

    //"does not allow vertex steps" in new Fixture {
    //illTyped {"""graph.V(1).inV"""}
    //TODO: all vertex steps: out, outE, in, inE, both
    //}
  }

  "head" can {
    "get the first element" in new Fixture {
      graph.V.values[String]("name").head shouldBe "marko"
    }

    "throw an exception if there is no result" in new Fixture {
      intercept[NoSuchElementException] {
        graph.V.filter(_ ⇒ false).values[String]("name").head
      }
    }
  }

  "headOption" can {
    "get the first element" in new Fixture {
      graph.V.values[String]("name").headOption shouldBe Some("marko")
    }

    "return None if there is no result" in new Fixture {
      graph.V.filter(_ ⇒ false).values[String]("name").headOption shouldBe None
    }
  }

  "value gets values" in new Fixture {
    graph.V.value(Age).toSet shouldBe Set(27, 29, 32, 35)
  }

  "order" can {
    "sort by natural order" in new Fixture {
      graph.V.values[Int]("age").order.toList shouldBe List(27, 29, 32, 35)
    }

    "sort by provided comparator" in new Fixture {
      graph.V.values[Int]("age").order.by(_ < _).toList shouldBe List(27, 29, 32, 35)
      graph.V.values[Int]("age").order.by(_ > _).toList shouldBe List(35, 32, 29, 27)
    }
  }

  "map" can {
    "transform the latest step" in new Fixture {
      graph.V.map(_.label).toList shouldBe graph.V.label.toList
    }

    "infer the right types" in new Fixture {
      val labels: List[String] = graph.V.map(_.label).toList
    }

    "support for comprehension" in new Fixture {
      val labels = for {
        vertex ← graph.V
      } yield vertex.label

      labels.toSet shouldBe graph.V.label.toSet
    }
  }

  "flatMap" can {
    "transform the latest step" in new Fixture {
      val v1outEdges = graph.V(1).outE.toList
      v1outEdges should have length 3

      graph.V(1).flatMap(_.outE).toList shouldBe v1outEdges
    }

    "infers the right types" in new Fixture {
      val edges: List[Edge] = graph.V(1).flatMap(_.outE).toList
    }

    "support for comprehension" in new Fixture {
      val edgeLabels = for {
        vertex ← graph.V
        edge ← vertex.outE
      } yield edge.label

      edgeLabels.toSet shouldBe graph.E.label.toSet
    }

    "doesn't compile for bad traversals" in new Fixture {
      graph.V(1).flatMap(_.outE) //compiles fine
      illTyped { """graph.GRAPH.V(1).flatMap(_.inV)""" } //verify doesn't compile
    }
  }

  "limit in nested traversals" in {
    val graph = TinkerGraph.open.asScala
    val person = "person"
    val likes = "likes"
    val name = Key[String]("name")
    val weight = Key[Float]("weight")

    val scala = graph + "scala"
    val groovy = graph + "groovy"
    val michael = graph + (person, name -> "michael")
    val marko = graph + (person, name -> "marko")

    michael --- (likes, weight -> 3) --> groovy
    michael --- (likes, weight -> 5) --> scala
    marko --- (likes, weight -> 4) --> groovy
    marko --- (likes, weight -> 3) --> scala

    val traversal = for {
      person <- graph.V.hasLabel(person)
      favorite <- person.outE(likes).order.by("weight", Order.decr).limit(1).inV
    } yield (person.value2(name), favorite.label)

    val favorites = traversal.toList.toMap
    favorites shouldBe Map(
      "michael" -> "scala",
      "marko" -> "groovy"
    )
  }

  "collect" in new Fixture {
    val ages: Set[Int] = graph.V.valueOption(Age).collect {
      case Some(age) => age
    }.toSet

    ages shouldBe Set(27,29,32,35)
  }

  "find all edges between two vertices" in {
    val graph = TinkerGraph.open.asScala

    val v0 = graph + "v0"
    val v1 = graph + "v1"
    val v2 = graph + "v2"

    v0 --- "e0" --> v1
    v0 <-- "e1" --- v1
    v0 <-- "e2" --> v2

    graph.E.count.head shouldBe 4
    val v0v1Edges = v0.bothE.filter(_.bothVertices.contains(v1)).label.toSet
    v0v1Edges shouldBe Set("e0", "e1")
  }

  trait Fixture {
    val graph = TinkerFactory.createModern.asScala
    val Name = Key[String]("name")
    val Age = Key[Int]("age")
  }
}
