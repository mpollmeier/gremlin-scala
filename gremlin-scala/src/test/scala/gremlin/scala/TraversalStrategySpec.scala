package gremlin.scala

import java.util
import java.util.concurrent.CompletableFuture

import org.apache.tinkerpop.gremlin.process.remote.RemoteConnection
import org.apache.tinkerpop.gremlin.process.remote.traversal.{
  AbstractRemoteTraversal,
  DefaultRemoteTraverser,
  RemoteTraversal
}
import org.apache.tinkerpop.gremlin.structure.{Vertex => TVertex}
import org.apache.tinkerpop.gremlin.process.traversal.{Bytecode, Step, TraversalSideEffects}
import org.apache.tinkerpop.gremlin.process.traversal.Traverser.Admin
import org.apache.tinkerpop.gremlin.structure.util.empty.EmptyGraph
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Await
import scala.concurrent.duration.{FiniteDuration, MILLISECONDS}
import scala.util.Random

class TraversalStrategySpec extends AnyWordSpec with Matchers {

  "sack step".can {

    /** http://tinkerpop.apache.org/docs/current/reference/#sack-step */
    "carry simple value" when {
      "using constant for initial sack" in new Fixture {
        graph.configure(_.withSack(1d)).V().sack().toList() shouldBe List(1d, 1d, 1d, 1d, 1d, 1d)
      }

      "using function for initial sack" in new Fixture {
        graph.configure(_.withSack(() => 1d)).V().sack().toList() shouldBe List(1d, 1d, 1d, 1d, 1d, 1d)

        val randomValues =
          graph.configure(_.withSack(() => Random.nextDouble())).V().sack().toList()
        randomValues.toSet.size shouldBe 6
      }
    }

    "transform the sack on the go" in new Fixture {
      val result = graph
        .configure(_.withSack(1d))
        .V()
        .repeat {
          _.outE().sack { (curr: Double, edge) =>
            curr * edge.value2(Weight)
          }.inV()
        }
        .times(2)
        .sack()
        .toSet()

      result shouldBe Set(1d, 0.4d)
    }

    "be modulated with by operator" when {
      "modulating by property" in new Fixture {
        val result = graph
          .configure(_.withSack(1d))
          .V(1)
          .outE()
          .sack(multiply, By(Weight))
          .inV()
          .sack()
          .toSet()

        result shouldBe Set(0.4d, 0.5d, 1d)
      }

      "modulating by traversal" in new Fixture {
        val result = graph
          .configure(_.withSack(1d))
          .V(1)
          .outE()
          .sack(multiply, By(__().value(Weight)))
          .inV()
          .sack()
          .toSet()

        result shouldBe Set(0.4d, 0.5d, 1d)
      }
      def multiply(a: Double, b: Double): Double = a * b
    }

    "use provided split operator when cloning sack" in new Fixture {
      var counter = 0
      val identityWithCounterIncrease = { (value: Double) =>
        counter += 1
        value
      }

      graph
        .configure(_.withSack(1d, splitOperator = identityWithCounterIncrease))
        .V()
        .out()
        .toList()
      counter shouldBe 6
    }

    "use provided merge operator when bulking sack" in new Fixture {
      val sum = (f1: Double, f2: Double) => f1 + f2
      graph
        .configure(_.withSack(1d, mergeOperator = sum))
        .V(1)
        .out("knows")
        .in("knows")
        .sack()
        .toList() shouldBe List(2d, 2d)
      // without `sum` would be List(1d, 1d)
    }

    "be configured when starting from an element" when {
      "on a vertex" in new Fixture {
        val v1: Vertex = graph.V(1).head()
        v1.start(_.withSack(1d)).outE(Knows).sack().toList() shouldBe List(1d, 1d)
      }

      "on an edge" in new Fixture {
        val e7: Edge = graph.E(7).head()
        e7.start(_.withSack(1d)).outV().outE(Knows).sack().toList() shouldBe List(1d, 1d)
      }
    }
  }

  "withRemote" should {
    "use RemoteConnection" in new RemoteGraphFixture {
      // Execute a traversal with provided vertices
      val result = remoteGraph.V().toList()
      result shouldEqual mockVertices
    }

    "support promise" in new RemoteGraphFixture {
      // Execute a traversal using promise with the provided vertices
      val future = remoteGraph.V().promise()
      val result = Await.result(future, FiniteDuration(1000, MILLISECONDS))
      result shouldEqual mockVertices
    }
  }

  trait Fixture {
    val graph = TinkerFactory.createModern.asScala()
    val Name = Key[String]("name")
    val Lang = Key[String]("lang")
    val Weight = Key[Double]("weight")
    val Knows = "knows"
  }

  trait RemoteGraphFixture {
    val graph = EmptyGraph.instance().asScala()

    // effectively a g.V() bytecode
    val expectedBytecode: Bytecode = new Bytecode()
    expectedBytecode.addStep("V")

    // data to return
    val mockVertices = List[TVertex](
      DetachedVertex(id = 1: Integer, label = "person"),
      DetachedVertex(id = 2: Integer, label = "person")
    )

    // Create a future that completes immediately and provides a remote traversal providing vertices
    val vertexResult = new CompletableFuture[RemoteTraversal[_ <: Any, TVertex]]

    // Stub out a remote connection that responds to g.V() with 2 vertices
    val connection = new RemoteConnection {
      override def submitAsync[E](bytecode: Bytecode): CompletableFuture[RemoteTraversal[_, E]] =
        if (bytecode == expectedBytecode) vertexResult.asInstanceOf[CompletableFuture[RemoteTraversal[_, E]]]
        else ???
      override def close(): Unit = ()
    }

    val remoteGraph = graph.configure(_.withRemote(connection))

    val traversal = new AbstractRemoteTraversal[Int, TVertex] {
      val it = mockVertices.iterator

      override def nextTraverser: Admin[TVertex] =
        new DefaultRemoteTraverser[Vertex](it.next(), 1)

      override def getSideEffects: TraversalSideEffects =
        null // not necessary for this test

      override def next(): TVertex = nextTraverser().get()

      override def hasNext: Boolean = it.hasNext

      override def getSteps: util.List[Step[_, _]] = super.getSteps
    }
    vertexResult.complete(traversal)

  }
}
