package com.tinkerpop.gremlin.scala.jsr223

import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory
import com.tinkerpop.gremlin.scala.ScalaVertex._
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.tinkerpop.blueprints._
import com.tinkerpop.blueprints.impls.tg.TinkerGraph
import scala.collection.JavaConversions._
import scala.collection.mutable.Map
import org.scalatest.mock.MockitoSugar
import javax.script.ScriptContext

@RunWith(classOf[JUnitRunner])
class ScalaScriptEngineTest extends FunSpec with ShouldMatchers with MockitoSugar {

  describe("ScriptEngine.eval") {
    val target = new GremlinScalaScriptEngine("src/test/resources")

    it("runs a simple command") {
      target.eval(""" "dummy response" """, mock[ScriptContext]) should be("dummy response")
    }

    ignore("stores values for the next execution") {}
    ignore("overrides values if reused") {}

  }

}