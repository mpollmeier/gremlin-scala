package gremlin.scala

import org.apache.tinkerpop.gremlin.process.traversal.{Path, Order}
import org.apache.tinkerpop.gremlin.structure.T
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import org.scalatest.{WordSpec, Matchers}
import java.util.{Map ⇒ JMap, Collection ⇒ JCollection}
import java.lang.{Long => JLong}

import scala.language.existentials
import shapeless.test.illTyped

import collection.JavaConverters._
import scala.concurrent.duration.{FiniteDuration, MILLISECONDS}

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
        graph.V.filter(_.has(Key("nonExistingProperty"))).values[String]("name").head
      }
    }
  }

  "headOption" can {
    "get the first element" in new Fixture {
      graph.V.values[String]("name").headOption shouldBe Some("marko")
    }

    "return None if there is no result" in new Fixture {
      graph.V.filter(_.has(Key("nonExistingProperty"))).values[String]("name").headOption shouldBe None
    }
  }

  "value gets values" in new Fixture {
    graph.V.value(Age).toSet shouldBe Set(27, 29, 32, 35)
  }

  "orderBy" can {
    "order by property" in new Fixture {
      graph.V.has(Age)
        .orderBy("age")
        .value(Age)
        .toList shouldBe Seq(27, 29, 32, 35)
    }

    "order by property decr" in new Fixture {
      graph.V.has(Age)
        .orderBy("age", Order.decr)
        .value(Age)
        .toList shouldBe Seq(35, 32, 29, 27)
    }

    "order by lambda" in new Fixture {
      graph.V.has(Age)
        .orderBy(_.value[Integer]("age"))
        .value(Age)
        .toList shouldBe Seq(27, 29, 32, 35)
    }

    "order by lambda decr" in new Fixture {
      graph.V.has(Age)
        .orderBy(_.value[Integer]("age"), Order.decr)
        .value(Age)
        .toList shouldBe Seq(35, 32, 29, 27)
    }

    "order non-elements" in new Fixture {
      graph.V.has(Age)
        .value[Integer]("age")
        .orderBy(x ⇒ x)
        .toList shouldBe Seq(27, 29, 32, 35)
    }

    "order non-elements decr" in new Fixture {
      graph.V.has(Age)
        .value[Integer]("age")
        .orderBy(x ⇒ -x: Integer)
        .toList shouldBe Seq(35, 32, 29, 27)
    }

    "order with sub-by" in new Fixture {
      // add two more people with Age 29
      graph + ("person", Age → 29, Name → "ZZZ")
      graph + ("person", Age → 29, Name → "aaa")

      graph.V.has(Age).has(Name)
        .order()
        .by(Age.name, Order.incr) // TODO: add by(Key[A]) step
        .by(Name.name, Order.decr)
        .value(Name)
        .toList shouldBe Seq("vadas", "marko", "aaa", "ZZZ", "josh", "peter")
    }

    // TODO: does not work because tinkerpop's Order.java enforces to be on Object, and that's because it's an enum in java can't take type parameters
    // "allow primitive types" in new Fixture {
    //     graph.V.has(Age)
    //     .orderBy(_.value[Int]("age"))
    //     .value(Age)
    //     .toList shouldBe Seq(27, 29, 32, 35)
    // }
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

    "support for comprehension" when {
      "using simple case" in new Fixture {
        val edgeLabels = for {
          vertex ← graph.V
          edge ← vertex.outE
        } yield edge.label

        edgeLabels.toSet shouldBe graph.E.label.toSet
      }

      "using slightly more complex case" in new Fixture {
        // what is the mean age of the developers for a given software?
        val softwareAndDevAges = for {
          software ← graph.V.hasLabel("software")
          meanAge ← software.in("created").value(Age).mean
        } yield (software.value2(Name), meanAge)

        softwareAndDevAges.toSet shouldBe Set(
          "lop" → 32d,
          "ripple" → 32d
        )
      }
    }

    "doesn't compile for bad traversals" in new Fixture {
      graph.V(1).flatMap(_.outE) //compiles fine
      illTyped { """graph.V(1).flatMap(_.inV)""" } //verify doesn't compile
    }
  }

  "fold" can {
    "gather elements in a list" in new Fixture {
      graph.V.has(Age)
        .orderBy("age", Order.decr)
        .value(Age)
        .fold().head().asScala.toSeq shouldBe Seq(35, 32, 29, 27)
    }

    "aggregate with arbitrary initial value and function" in new Fixture {
      graph.V.has(Age)
        .orderBy("age", Order.decr)
        .value(Age)
        .foldLeft(0)(_ + _).head() shouldBe 123

      graph.V.has(Age)
        .orderBy("age", Order.decr)
        .value(Age)
        .foldLeft("F")(_ + _ + "*").head() shouldBe "F35*32*29*27*"

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
    val michael = graph + (person, name → "michael")
    val marko = graph + (person, name → "marko")

    michael --- (likes, weight → 3) --> groovy
    michael --- (likes, weight → 5) --> scala
    marko --- (likes, weight → 4) --> groovy
    marko --- (likes, weight → 3) --> scala

    val traversal = for {
      person ← graph.V.hasLabel(person)
      favorite ← person.outE(likes).orderBy("weight", Order.decr).limit(1).inV
    } yield (person.value2(name), favorite.label)

    traversal.toMap shouldBe Map(
      "michael" → "scala",
      "marko" → "groovy"
    )
  }

  "collect" in new Fixture {
    val ages: Set[Int] = graph.V.valueOption(Age).collect {
      case Some(age) ⇒ age
    }.toSet

    ages shouldBe Set(27, 29, 32, 35)
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
    val v0v1Edges = v0.bothE.filter(_.bothV.is(v1)).label.toSet
    v0v1Edges shouldBe Set("e0", "e1")
  }

  "`drop` removes elements from the graph" in new Fixture {
    // remove all people over 30 from the graph
    graph.V.hasLabel("person").has(Age, P.gte(30)).drop.iterate

    graph.V.hasLabel("person").count.head shouldBe 2
    withClue("should remove corresponding edges as well") {
      graph.E.count.head shouldBe 2
    }
  }

  "group" should {
    // "modulate by (T)oken" in new Fixture {
    //   val results: JMap[String, JCollection[Vertex]] =
    //     graph.V.group2(by(T.label)).head

    //   results.get("software") should contain(graph.V(3).head)
    //   results.get("software") should contain(graph.V(5).head)
    //   results.get("person") should contain(graph.V(1).head)
    //   results.get("person") should contain(graph.V(2).head)
    //   results.get("person") should contain(graph.V(4).head)
    //   results.get("person") should contain(graph.V(6).head)
    // }

    "modulate by property key" in new Fixture {
      // val by2: By[] = by[Int, JMap[Int, JCollection[Vertex]]](Age) //works
      // val by2 = by[Int, JMap[Int, JCollection[Vertex]]](Age)

      val results: JMap[Int, JCollection[Vertex]] =
        graph.V.has(Age)
          // .group2[Int](by(Age)) //works
          // .group2(by[Int, JMap[Int, JCollection[Vertex]]](Age)) //works
          .group2(by(Age)) // why not?
          .head

      results.get(27) should contain(graph.V(2).head)
      results.get(29) should contain(graph.V(1).head)
      results.get(32) should contain(graph.V(4).head)
      results.get(35) should contain(graph.V(6).head)
    }

    "optionally allow to transform the values" in new Fixture {
      val results: Map[String, Iterable[String]] =
        graph.V.groupBy(_.label, _.value2(Name)).head

      results("software").toSet shouldBe Set("lop", "ripple")
      results("person").toSet shouldBe Set("marko", "vadas", "josh", "peter")
    }
  }

  "subgraph" should {
    "work in simple scenario" in new Fixture {
      val stepLabel = StepLabel[Graph]("subGraph")
      val subGraph: Graph = graph.E.hasLabel("knows")
        .subgraph(stepLabel)
        .cap(stepLabel)
        .head

      subGraph.V.count.head shouldBe 3
      subGraph.E.count.head shouldBe 2
    }

    "get all of the graph structure surrounding a single vertex" in new Fixture {
      val stepLabel = StepLabel[Graph]("subGraph")
      val subGraph: Graph = graph.V(3)
        .repeat(_.inE.subgraph(stepLabel).outV)
        .times(3)
        .cap(stepLabel).head

      subGraph.V.count.head shouldBe 4
      subGraph.E.count.head shouldBe 4
    }
  }

  "union" should {
    "work for traversals with the same end type" in new Fixture {
      val traversal: GremlinScala[Int, _] =
        graph.V(4).union(
          _.in.value(Age),
          _.in.out.value(Age)
        )

      traversal.toSet shouldBe Set(27, 29, 32)
    }

    "work for traversals with the different end types" in new Fixture {
      val traversal: GremlinScala[Any, _] = graph.V(4).union(
        _.in.value("age"),
        _.in.value("name")
      )

      traversal.toSet shouldBe Set(29, "marko")
    }
  }

  "coalesce" should {
    // Helper for testing path-based results
    def path2String(path: Path) = path.objects().asScala.map(_.toString)

    "evaluate traversals and return first with value" when {

      "relationship is first" in new Fixture {
        val traversal1 = graph.V(1)
          .coalesce(
            _.outE("knows"),
            _.outE("created")
          )
          .inV()
          .path().by("name").by(T.label)

        traversal1.toList().map(path2String) shouldBe
          Seq(Seq("marko", "knows", "vadas"), Seq("marko", "knows", "josh"))
      }

      "created is first" in new Fixture {
        val traversal2 = graph.V(1)
          .coalesce(
            _.outE("created"),
            _.outE("knows")
          )
          .inV()
          .path().by("name").by(T.label)

        traversal2.toList().map(path2String) shouldBe Seq(Seq("marko", "created", "lop"))
      }
    }

    "handle vertex-unique properties" in new Fixture {
      // Add a new property on a specific vertex and test that it is picked up first.
      graph.V(1).property(Nickname, "okram").iterate()

      val traversal3 = graph.V
        .hasLabel("person")
        .coalesce(
          _.value(Nickname),
          _.value(Name)
        )

      traversal3.toSet() shouldBe Set("okram", "vadas", "josh", "peter")
    }

    "allow constant as default" in new Fixture {
      val traversal3 = graph.V
        .coalesce(
          _.value(Age).map(_.toString),
          _.constant("ageless")
        )

      traversal3.toSet() shouldBe Set("35", "32", "27", "29", "ageless")
    }
  }

  "repeat/until step" can {
    "iterate until no moure outgoing edges" in new Fixture {
      val traversal = graph.V(1)
        .repeat(_.out)
        .until(_.outE.count.is(JLong.valueOf(0)))
        .path.by("name")

      traversal.toSet.size shouldBe 4
      // Set([marko, lop], [marko, vadas], [marko, josh, ripple], [marko, josh, lop])
    }
  }

  "where step" can {
    "limit results to find who's created more than one thing" in new Fixture {
      val traversal =  graph.V.hasLabel(Person)
        .where(_.outE(Created).count.is(P.gt(1)))

      val results = traversal.toList
      results.size shouldBe 1
      results.head.value2(Name) shouldBe "josh"
    }
  }

  "tree step" can {
    "generate a tree structure for a vertex" in {
      val graph = TinkerGraph.open()
      val vertices = (0 to 4).map(i => graph.addVertex(T.id, i.asInstanceOf[Integer]))
      val edges = Seq(1 -> 2, 1 -> 3, 3 -> 4)
        .map { case (from, to) => vertices(from).addEdge("knows", vertices(to)) }

      val tree = graph.V(1).head.start()
        .repeat(_.outE.inV).emit().tree().head

      tree.keySet().asScala shouldBe Set(vertices(1))

      val v1Edges = tree.values.asScala.head
      v1Edges.keySet.asScala shouldBe Set(edges(0), edges(1))

      val verticesUnderV1 = v1Edges.values.asScala.toSeq
      verticesUnderV1.size shouldBe 2
      verticesUnderV1(0).keySet.asScala shouldBe Set(vertices(2))
      verticesUnderV1(0).values.asScala.head shouldBe empty
      verticesUnderV1(1).keySet.asScala shouldBe Set(vertices(3))

      val v3Edges = verticesUnderV1(1).values.asScala.head
      v3Edges.keySet.asScala shouldBe Set(edges(2))

      val verticesUnderV3 = v3Edges.values.asScala.toSeq
      verticesUnderV3.size shouldBe 1
      verticesUnderV3(0).keySet.asScala shouldBe Set(vertices(4))
      verticesUnderV3(0).values.asScala.head shouldBe empty
    }
  }

  "optional step" which {
    "doesn't take a default value" should {
      "return result of optional traversal if it has one" in new Fixture {
        val results = graph.V(1).optional(_.out("knows")).value(Name).toList
        results shouldBe List("vadas", "josh")
      }

      "return identity if optional traversal doesn't find anything" in new Fixture {
        val results = graph.V(1).optional(_.out("doesnt exist")).value(Name).toList
        results shouldBe List("marko")
      }
    }

    "takes a default value" should {
      "return result of optional traversal if it has one" in new Fixture {
        val results = graph.V(1).optional(_.out("knows"), DetachedVertex()).value(Name).toList
        results shouldBe List("vadas", "josh")
      }

      "return identity if optional traversal doesn't find anything" in new Fixture {
        val results = graph.V(1).optional(_.out("doesnt exist"), DetachedVertex()).value(Name).toList
        results shouldBe List()
      }
    }
  }

  "steps to add things" can {
    "add an (unconnected) vertex for each path in the traversal" which {
      "has a label and properties" in new Fixture {
        val NewProperty = Key[String]("newProperty")

        graph.V.outE("knows").addV("newLabel").property(NewProperty, "someValue").iterate()
        graph.V.hasLabel("newLabel").count.head shouldBe 2
        graph.V.has(NewProperty → "someValue").count.head shouldBe 2
      }
    }

    "add edges" which {
      val v1Label = StepLabel[Vertex]("v1")
      val CoDeveloper = "co-developer"

      "don't use special steps" in new Fixture {
        val traversal = for {
          v1 ← graph.V(1)
          coDeveloper ← v1.out(Created).in(Created).filter(_.is(P.neq(v1)))
        } yield v1 --- CoDeveloper --> coDeveloper
        traversal.iterate()

        graph.V(1).out(CoDeveloper).value(Name).toSet shouldBe Set("josh", "peter")
      }

      "reference the `from` vertex via StepLabel" in new Fixture {
        graph.V(1).as(v1Label).out(Created).in(Created).where(P.neq(v1Label.name)).addE(CoDeveloper).from(v1Label).iterate()
        graph.V(1).out(CoDeveloper).value(Name).toSet shouldBe Set("josh", "peter")
      }

      "reference the `to` vertex via StepLabel" in new Fixture {
        graph.V(1).as(v1Label).out(Created).in(Created).where(P.neq(v1Label.name)).addE(CoDeveloper).to(v1Label).iterate()
        graph.V(1).in(CoDeveloper).value(Name).toSet shouldBe Set("josh", "peter")
      }
    }

    "use addV to start a traversal" in new Fixture {
      graph.addV(Person).property(Name, "Joseph").property(Age, 39).property(Nickname, "Joe").head

      graph.V.has(Name, "Joseph").count.head shouldBe 1
    }

    "use addV (without label) to start a traversal" in new Fixture {
      graph.addV().property(Name, "Unlabeled").head

      graph.V.has(Name, "Unlabeled").count.head shouldBe 1
    }
  }

  "local step" can {
    "limit a local subquery" in new Fixture {
      graph.V.outE.inV.count.head shouldBe 6
      graph.V.local(_.outE.limit(1)).inV.limit(3).count.head shouldBe 3
    }
  }

  "inject step" can {
    "be used to start a traversal" in new Fixture {
      graph.inject(1, 2).map(_ + 1).map(graph.V(_).value(Name).head).toList shouldEqual List("vadas", "lop")
    }

    "used to add values to traversal" in new Fixture {
      graph.V(4).out().value(Name).inject("daniel").toList shouldEqual List("daniel", "ripple", "lop")
    }
  }

  "graph step" can {
    "be used mid traversal" in new Fixture {
      val a = StepLabel[Vertex]()
      val b = StepLabel[Vertex]()
      // visit marko (a), then vadas (b) and then connect the two
      graph.V().has(Name, "marko").as(a)
        .V().has(Name, "vadas").as(b)
        .addE(Knows).from(a).to(b).property(StartTime, 2010)
        .iterate

      val names = graph.E().hasLabel(Knows).has(StartTime, 2010).bothV().value(Name).toList.sorted

      names shouldEqual List("marko", "vadas")
    }
  }

  "time limit is honored" ignore new Fixture {
    val maxTime = FiniteDuration(50, MILLISECONDS)
    val start = System.currentTimeMillis

    graph.V(1)
      .timeLimit(maxTime)
      .repeat {_.sideEffect { _ => Thread.sleep(10) }}
      .times(10)
      .toList

    val duration = System.currentTimeMillis - start
    duration should be <= maxTime.toMillis
  }

  "allows to be cloned" in new Fixture {
    val gs = graph.V(1).count
    val gsCloned = gs.clone()
    gs.head shouldBe 1
    gsCloned.head shouldBe 1
  }

  trait Fixture {
    val graph = TinkerFactory.createModern.asScala
    val Name = Key[String]("name")
    val Nickname = Key[String]("nickname")
    val Lang = Key[String]("lang")
    val Age = Key[Int]("age")
    val StartTime = Key[Int]("startTime")
    val Knows = "knows"
    val Person = "person"
    val Created = "created"
  }
}
