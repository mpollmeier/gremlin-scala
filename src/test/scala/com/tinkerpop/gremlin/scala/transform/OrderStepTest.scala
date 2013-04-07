package com.tinkerpop.gremlin.scala.transform

import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory
import com.tinkerpop.gremlin.test.ComplianceTest
import com.tinkerpop.gremlin.scala._
import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.pipes.util.structures.{ Pair ⇒ TPair }
import java.lang.{ Integer ⇒ JInteger }

class OrderStepTest extends com.tinkerpop.gremlin.test.transform.OrderStepTest {
  val g = TinkerGraphFactory.createTinkerGraph()

  def test_g_V_name_order() {
    super.test_g_V_name_order(g.V.property("name").order())
  }

  def test_g_V_name_orderXabX() {
    super.test_g_V_name_orderXabX(g.V.property("name")
      .order({ arg: TPair[String, String] ⇒ arg.getB.compareTo(arg.getA) }))
  }

  def test_g_V_orderXa_nameXb_nameX_name() {
    super.test_g_V_orderXa_nameXb_nameX_name(g.V
      .order({ arg: TPair[Vertex, Vertex] ⇒ arg.getB.get[String]("name").get.compareTo(arg.getA.get[String]("name").get) })
      .property("name").asInstanceOf[GremlinScalaPipeline[Vertex, String]])
  }

}
