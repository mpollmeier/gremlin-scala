package com.tinkerpop.gremlin.scala

import com.tinkerpop.blueprints._
import com.tinkerpop.gremlin.scala._
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec

class TransformStepsSpec extends FunSpec with ShouldMatchers with TestGraph {

  describe("path step") {
    it("returns a list with all objects in the path") {
      val paths = graph.v(1).->.out().path.toList
      println(paths.toSeq)
    }

    //  def test_g_v1_propertyXnameX_path() {
    //    super.test_g_v1_propertyXnameX_path(g.v(1).->.property("name").path())
    //  }
    //
    //  def test_g_v1_out_pathXage__nameX() {
    //    super.test_g_v1_out_pathXage__nameX(g.v(1).out.path({ v: Vertex ⇒ v("age") }, { v: Vertex ⇒ v("name") }))
    //  }
    //
    //  def test_g_V_out_loopX1__loops_lt_3X_pathXit__name__langX() {
    //    val p = g.V.out.loop(1, { lb: LoopBundle[Vertex] ⇒ lb.loops < 3 }).path({ v: Vertex ⇒ v }, { v: Vertex ⇒ v("name") }, { v: Vertex ⇒ v("lang") })
    //    super.test_g_V_out_loopX1__loops_lt_3X_pathXit__name__langX(p)
    //  }
  }
}
