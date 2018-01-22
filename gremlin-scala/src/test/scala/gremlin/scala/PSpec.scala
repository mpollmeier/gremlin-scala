package gremlin.scala

import org.apache.tinkerpop.gremlin.process.traversal.{P => JavaP}
import org.scalatest.{Matchers, WordSpec}

/* test cases copied from TP3 PTest.java */
class PSpec extends WordSpec with Matchers {

  List(
    TestCase(P.eq(0), 0, true),
    TestCase(P.eq(0), 1, false),
    TestCase(P.neq(0), 0, false),
    TestCase(P.neq(0), 1, true),
    TestCase(P.gt(0), -1, false),
    TestCase(P.gt(0), 0, false),
    TestCase(P.gt(0), 1, true),
    TestCase(P.lt(0), -1, true),
    TestCase(P.lt(0), 0, false),
    TestCase(P.lt(0), 1, false),
    TestCase(P.gte(0), -1, false),
    TestCase(P.gte(0), 0, true),
    TestCase(P.gte(0), 1, true),
    TestCase(P.lte(0), -1, true),
    TestCase(P.lte(0), 0, true),
    TestCase(P.lte(0), 1, false),
    TestCase(P.between(1, 10), 0, false),
    TestCase(P.between(1, 10), 1, true),
    TestCase(P.between(1, 10), 9, true),
    TestCase(P.between(1, 10), 10, false),
    TestCase(P.inside(1, 10), 0, false),
    TestCase(P.inside(1, 10), 1, false),
    TestCase(P.inside(1, 10), 9, true),
    TestCase(P.inside(1, 10), 10, false),
    TestCase(P.outside(1, 10), 0, true),
    TestCase(P.outside(1, 10), 1, false),
    TestCase(P.outside(1, 10), 9, false),
    TestCase(P.outside(1, 10), 10, false),
    TestCase(P.within(Set(1, 2, 3)), 0, false),
    TestCase(P.within(Set(1, 2, 3)), 1, true),
    TestCase(P.within(Set(1, 2, 3)), 10, false),
    TestCase(P.without(Set(1, 2, 3)), 0, true),
    TestCase(P.without(Set(1, 2, 3)), 1, false),
    TestCase(P.without(Set(1, 2, 3)), 10, true)
  ).map { testCase =>
    s"$testCase" in {
      testCase.predicate.test(testCase.value) shouldBe testCase.expected
    }
  }

  List(
    TestCase(P.between("m", "n").and(P.neq("marko")), "marko", false),
    TestCase(P.between("m", "n").and(P.neq("marko")), "matthias", true),
    TestCase(P.between("m", "n").or(P.eq("daniel")), "marko", true),
    TestCase(P.between("m", "n").or(P.eq("daniel")), "daniel", true),
    TestCase(P.between("m", "n").or(P.eq("daniel")), "stephen", false)
  ).map { testCase =>
    s"$testCase" in {
      testCase.predicate.test(testCase.value) shouldBe testCase.expected
    }
  }

  case class TestCase[A](predicate: JavaP[A], value: A, expected: Boolean)
}
