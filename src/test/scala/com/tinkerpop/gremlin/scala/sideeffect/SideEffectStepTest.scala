package com.tinkerpop.gremlin.scala.sideeffect

import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory
import com.tinkerpop.gremlin.test.ComplianceTest
import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.gremlin.scala._
import com.tinkerpop.pipes.Pipe
import junit.framework.Assert._

/** @author Marko A. Rodriguez (http://markorodriguez.com)
 */

class SideEffectStepTest extends com.tinkerpop.gremlin.test.sideeffect.SideEffectStepTest {

  val g = TinkerGraphFactory.createTinkerGraph();

  //  def test_g_v1_sideEffectXstore_aX_propertyXnameX() {
  //    var a = g.getVertex(2)
  //    super.test_g_v1_sideEffectXstore_aX_propertyXnameX(g.v(1).->.sideEffect { v: Vertex ⇒ a = v }.property("name").asInstanceOf[Pipe[Vertex, String]])
  //    assertEquals(a.id, g.v(1).id)
  //  }
  //
  //  def test_g_v1_out_sideEffectXincr_cX_propertyXnameX() {
  //    var c = 0;
  //    super.test_g_v1_out_sideEffectXincr_cX_propertyXnameX(g.v(1).->.out.sideEffect { v: Vertex ⇒ c = c + 1 }.property("name").asInstanceOf[Pipe[Vertex, String]]);
  //    assertEquals(c, 3);
  //  }
  //
  //  def test_g_v1_out_sideEffectXfalseX_propertyXnameX() {
  //    super.test_g_v1_out_sideEffectXfalseX_propertyXnameX(g.v(1).->.out.sideEffect { v: Vertex ⇒ false }.property("name").asInstanceOf[Pipe[Vertex, String]]);
  //  }

}

