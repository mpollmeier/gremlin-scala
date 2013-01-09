package com.tinkerpop.gremlin.scala.transform

import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory
import com.tinkerpop.gremlin.test.ComplianceTest
import com.tinkerpop.gremlin.scala._
import com.tinkerpop.blueprints.Vertex
import java.lang.{Integer => JInteger}

class OrderStepTest extends com.tinkerpop.gremlin.test.transform.OrderStepTest {
  val g = TinkerGraphFactory.createTinkerGraph()

  override def testCompliance() {
    ComplianceTest.testCompliance(this.getClass)
  }

  def test_g_V_name_order() {
    super.test_g_V_name_order(g.V.property("name").order())
  }
  
  def test_g_V_name_orderXabX() {
	  super.test_g_V_name_orderXabX(g.V.property("name")
	           .order({arg: com.tinkerpop.pipes.util.structures.Pair[String,String] => 
	           		arg.getB.compareTo(arg.getA).asInstanceOf[JInteger]}))
  }
  
  def test_g_V_orderXa_nameXb_nameX_name() {
	  super.test_g_V_orderXa_nameXb_nameX_name(g.V
	           .order({arg: com.tinkerpop.pipes.util.structures.Pair[Vertex,Vertex] => 
	           		arg.getB.getProperty("name").asInstanceOf[String]
	           			.compareTo(arg.getA.getProperty("name").asInstanceOf[String])
	           			.asInstanceOf[JInteger]}).property("name"))
  }

}
