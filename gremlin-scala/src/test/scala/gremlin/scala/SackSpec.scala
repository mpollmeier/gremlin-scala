package gremlin.scala

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory
import org.scalatest.{WordSpec, Matchers}
import scala.util.Random

/** http://tinkerpop.apache.org/docs/current/reference/#sack-step */
class SackSpec extends WordSpec with Matchers {

  "carries simple value" when {
    "using constant for initial sack" in new Fixture {
      graph.withSack(1f).V.sack.toList shouldBe List(1f, 1f, 1f, 1f,1f, 1f)
    }

    "using function for initial sack" in new Fixture {
      graph.withSack(() => 1f).V.sack.toList shouldBe List(1f, 1f, 1f, 1f,1f, 1f)

      val randomValues = graph.withSack(() => Random.nextFloat).V.sack.toList
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
    val splitOperator = { value: Float => 
      counter += 1
      value
    }

    graph.withSack(1f, splitOperator).V.out.toList
    counter shouldBe 6
    graph.withSack(() => 1f, splitOperator).V.out.toList
    counter shouldBe 12
  }

  trait Fixture {
    val graph = TinkerFactory.createModern.asScala
    val Name = Key[String]("name")
    val Lang = Key[String]("lang")
    val Weight = Key[Double]("weight")
  }
}
