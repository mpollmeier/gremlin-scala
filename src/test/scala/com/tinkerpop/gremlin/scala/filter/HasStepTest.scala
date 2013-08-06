package com.tinkerpop.gremlin.scala.filter

import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory
import com.tinkerpop.gremlin.test.ComplianceTest
import com.tinkerpop.gremlin.scala._
import com.tinkerpop.gremlin.Tokens.T
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec
import com.tinkerpop.gremlin.Tokens

@RunWith(classOf[JUnitRunner])
class HasStepTest extends FunSpec with ShouldMatchers {

  val g = TinkerGraphFactory.createTinkerGraph()

  describe("has steps") {

    it("finds by property") {
      //      val a = g.V.has("name", "marko").toList
      //      val a = g.V.hasXXX("name", Tokens.T.eq, "marko").toList
      //      println(a)
      //      println(b)
      //      assertEquals(pipe.next().getProperty("name"), "marko");
      //        assertFalse(pipe.hasNext());
    }

    //TODO works with int? is AnyRef?
    ignore("finds by id") {}
    ignore("finds by label") {}
  }

  //  def test_g_V_hasXname_markoX() {
  //    super.test_g_V_hasXname_markoX(g.V.has("name", "marko"));
  //  }
  //
  //  def test_g_V_hasXname_blahX() {
  //    super.test_g_V_hasXname_blahX(g.V.has("name", "blah"));
  //  }
  //
  //  def test_g_v1_out_hasXid_2X() {
  //    super.test_g_v1_out_hasXid_2X(g.v(1).out.has("id", "2"));
  //  }
  //
  //  def test_g_V_hasXage_gt_30X() {
  //    super.test_g_V_hasXage_gt_30X(g.V.has("age", T.gt, 30));
  //  }
}
