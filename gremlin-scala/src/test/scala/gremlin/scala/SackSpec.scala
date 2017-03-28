package gremlin.scala

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory
import org.scalatest.{WordSpec, Matchers}
import scala.util.Random

/** http://tinkerpop.apache.org/docs/current/reference/#sack-step */
class SackSpec extends WordSpec with Matchers {

  "carries simple value" when {
    "using constant for initial sack" in new Fixture {
      graph.withSack(1d).V.sack.toList shouldBe List(1d, 1d, 1d, 1d,1d, 1d)
    }

    "using function for initial sack" in new Fixture {
      graph.withSack(() => 1d).V.sack.toList shouldBe List(1d, 1d, 1d, 1d,1d, 1d)

      val randomValues = graph.withSack(() => Random.nextDouble).V.sack.toList
      randomValues.toSet.size shouldBe 6
    }
  }

  "transforming the sack on the go" in new Fixture {
    val result = graph.withSack(1d).V.repeat{
      _.outE
       .sack{(curr: Double, edge) => curr * edge.value2(Weight)}
       .inV
    }.times(2).sack().toSet

    result shouldBe Set(1d, 0.4d)
  }

  "uses provided split operator when cloning sack" in new Fixture {
    var counter = 0
    val identityWithCounterIncrease = { value: Double => 
      counter += 1
      value
    }

    graph.withSack(1d, splitOperator = identityWithCounterIncrease).V.out.toList
    counter shouldBe 6
    graph.withSack(() => 1d, splitOperator = identityWithCounterIncrease).V.out.toList
    counter shouldBe 12
  }

  /* TODO: make work and make assertions once question is answered: https://groups.google.com/forum/#!topic/gremlin-users/BVrJP2Lwwck */
  "uses provided merge operator when bulking sack" ignore new Fixture {
    val identity = {f: Double => f}
    val sum = {(f1: Double, f2: Double) => println("INVOKED"); f1 + f2 }
    val normSack = org.apache.tinkerpop.gremlin.process.traversal.SackFunctions.Barrier.normSack
    graph.withSack(1d, splitOperator = identity, mergeOperator = sum)
      .V(1).local(_.outE("knows").barrier(normSack).inV).sack
      .toList.foreach(println)
  }

  trait Fixture {
    val graph = TinkerFactory.createModern.asScala
    val Name = Key[String]("name")
    val Lang = Key[String]("lang")
    val Weight = Key[Double]("weight")
  }
}
