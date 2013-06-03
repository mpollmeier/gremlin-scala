package com.tinkerpop.gremlin.scala.jsr223

import java.io.FileReader
import java.io.Reader
import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory
import javax.script.ScriptEngineManager
import com.tinkerpop.gremlin.scala.Gremlin
import java.io.File
import java.io.FileWriter
import com.googlecode.scalascriptengine.ScalaScriptEngine
import scala.collection.mutable
import scala.collection.JavaConversions._
import javax.script.Compilable
import org.apache.clerezza.scala.scripting.ScriptEngineFactory
import org.apache.felix.scr.annotations.Service
import org.osgi.util.tracker.ServiceTracker

object ScriptExecutor extends App {
  //  val scriptFile = "src/test/resources/jsr223/Script.scala"
  //  val script = """println("Hello, World")"""

  //  new FileWriter(scriptFile).write(script) //and flush
  //  val testscripts = "src/test/resources/jsr223"
  //  val engine = ScalaScriptEngine.onChangeRefresh(new File(testscripts))
  //  val vals = mutable.Map.empty[String, Any]
  //  vals("a") = 0
  //
  //  while (true) {
  //    engine.refresh
  //    val script = engine.constructors[ScalaScript]("Script").newInstance(vals)
  //    val result = script.result
  //
  //    println(result)
  //    vals("a") = result
  //
  //    Thread.sleep(1000)
  //  }

  val factory = new ScriptEngineManager
  val engine = factory.getEngineByName("scala")
  println(engine.eval("val a = 42"))
  //  a.compile("val a = 42")

}