package gremlin.scala

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory
import org.scalatest.{WordSpec, Matchers}
import scala.util.Random

class TraversalStrategySpec extends WordSpec with Matchers {

  "sack step" can {
    /** http://tinkerpop.apache.org/docs/current/reference/#sack-step */

    "carry simple value" when {
      "using constant for initial sack" in new Fixture {
        graph.configure(_.withSack(1d)).V.sack.toList shouldBe List(1d, 1d, 1d, 1d,1d, 1d)
      }

      "using function for initial sack" in new Fixture {
        graph.configure(_.withSack(() => 1d)).V.sack.toList shouldBe List(1d, 1d, 1d, 1d,1d, 1d)

        val randomValues = graph.configure(_.withSack(() => Random.nextDouble)).V.sack.toList
        randomValues.toSet.size shouldBe 6
      }
    }

    "transform the sack on the go" in new Fixture {
      val result = graph.configure(_.withSack(1d)).V.repeat{
        _.outE
        .sack{(curr: Double, edge) => curr * edge.value2(Weight)}
        .inV
      }.times(2).sack().toSet

      result shouldBe Set(1d, 0.4d)
    }

    "use provided split operator when cloning sack" in new Fixture {
      var counter = 0
      val identityWithCounterIncrease = { value: Double => 
        counter += 1
        value
      }

      graph.configure(_.withSack(1d, splitOperator = identityWithCounterIncrease)).V.out.toList
      counter shouldBe 6
    }

    "use provided merge operator when bulking sack" in new Fixture {
      val sum = (f1: Double, f2: Double) => f1 + f2
      graph.configure(_.withSack(1d, mergeOperator = sum))
        .V(1).out("knows").in("knows").sack
        .toList shouldBe List(2d, 2d)
      // without `sum` would be List(1d, 1d)
    }

    "be configured when starting from an element" when {
      "on a vertex" in new Fixture {
        val v1: Vertex = graph.V(1).head
        v1.start(_.withSack(1d)).outE(Knows).sack.toList shouldBe List(1d, 1d)
      }

      "on an edge" in new Fixture {
        val e7: Edge = graph.E(7).head
        e7.start(_.withSack(1d)).outV.outE(Knows).sack.toList shouldBe List(1d, 1d)
      }
    }
  }

  trait Fixture {
    val graph = TinkerFactory.createModern.asScala
    val Name = Key[String]("name")
    val Lang = Key[String]("lang")
    val Weight = Key[Double]("weight")
    val Knows = "knows"
  }
}
