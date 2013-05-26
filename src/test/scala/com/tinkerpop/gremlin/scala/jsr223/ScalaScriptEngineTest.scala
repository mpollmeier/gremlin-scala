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
import GremlinScalaScriptEngine._

@RunWith(classOf[JUnitRunner])
class ScalaScriptEngineTest extends FunSpec with ShouldMatchers with MockitoSugar {

  describe("ScriptEngine.eval") {
    val target = new GremlinScalaScriptEngine("src/test/resources")

    it("runs a simple command") {
      target.eval(""" "dummy response" """, mock[ScriptContext]) should be("dummy response")
    }

    it("parses lines in different variations") {
      //      val ScalaLine = """(val|var|def)?\s*?a\s?=""".r

      println(target.parseLine("\"dummy response\""))
      println(target.parseLine("""a = "dummy response""""))
      println(target.parseLine("""val a = "dummy response""""))

      //      target.parseLine("\"dummy response\"") should be(ParseResult(None, "\"dummy response\""))
      //      target.parseLine("""a = "dummy response"""") should be(ParseResult(Some("a"), "\"dummy response\""))
      //      target.parseLine("""val a = "dummy response"""") should be(ParseResult(Some("a"), "\"dummy response\""))
    }

    it("removes the assignment for the script") {
      target.eval(""" val a = "dummy response" """, mock[ScriptContext]) should be("dummy response")
    }

    ignore("stores values for the next execution") {}
    ignore("overrides values if reused") {}

  }

}