package com.tinkerpop.gremlin.scala

import junit.framework.TestCase
import com.tinkerpop.gremlin.Tokens
import junit.framework.Assert._
import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory

class GremlinTest extends TestCase {

  def testVersion() {
    assertEquals(Gremlin.version(), Tokens.VERSION);
  }

  def testLanguage() {
    assertEquals(Gremlin.language(), "gremlin-scala");
  }

}

trait TestGraph {
  val graph = TinkerGraphFactory.createTinkerGraph
  def vertices = graph.V
  def edges = graph.E
}