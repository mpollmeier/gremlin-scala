package gremlin.scala

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class MonadSpec extends AnyWordSpec with Matchers {

  "obeys the monad laws" in {
    // based on examples in http://devth.com/2015/monad-laws-in-scala/

    val graph = TinkerGraph.open.asScala()

    def f(x: Int): GremlinScala[Int] = if (x < 10) __[Int]() else __(x * 2)
    def g(x: Int): GremlinScala[Int] = if (x > 50) __(x + 1) else __[Int]()

    withClue("left identity") {
      val a = 30
      val lhs = __(a).flatMap(f).head()
      val rhs = f(a).head()
      lhs shouldBe 60
      lhs shouldBe rhs
    }

    withClue("right identity") {
      def m = __(30)
      val lhs = m.flatMap { x: Int =>
        __(x)
      }.head()
      lhs shouldBe 30
      val rhs = m.head()
      lhs shouldBe rhs
    }

    withClue("associativity") {
      def m = __(30)
      val lhs = m.flatMap(f).flatMap(g).head()
      lhs shouldBe 61

      val rhs = m.flatMap(x => f(x).flatMap(g)).head()
      rhs shouldBe 61

      lhs shouldBe rhs
    }
  }
}
