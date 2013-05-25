package com.tinkerpop.gremlin.scala.jsr223

import java.io.FileReader
import java.io.Reader
import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory
import javax.script.ScriptEngineManager
import com.tinkerpop.gremlin.scala.Gremlin
import java.io.File
import java.io.FileWriter
import com.googlecode.scalascriptengine.ScalaScriptEngine

object ScriptExecutor extends App {

  //  val scriptFile = "script.scala"
  //  val script = """println("Hello, World")"""

  //  new FileWriter(scriptFile).write(script)
  val testscripts = "src/test/resources/jsr223"
  val engine = ScalaScriptEngine.onChangeRefresh(new File(testscripts))

  while (true) {
    engine.refresh
    val script = engine.newInstance[ScalaScript]("GremlinScript")
    println(script.result)
    Thread.sleep(1000)
  }

  //  File content:
  //  trait Script extends Script {
  //    def result = TinkerGraphFactory.createTinkerGraph
  //  }

  //  trait GremlinScalaScript {
  //    def execute(script)
  //  }

  //  engine.
  //  eval(scriptFile)
  //  val manager = new ScriptEngineManager
  //  println(manager.getEngineFactories)

  //  val graph = TinkerGraphFactory.createTinkerGraph
  //  val manager = new ScriptEngineManager
  //  val engine = manager.getEngineByName(Gremlin.language)

  //  def evaluate(reader: Reader, args: List[String]) {

  //  }

  //  args.toList match {
  //    case Nil            ⇒ System.err.println("Usage: <path_to_scala_script> <arg1> <arg2> ...")
  //    case script :: args ⇒ evaluate(new FileReader(script), args)
  //  }

  //        //GremlinJS.load();
  //
  //        //GremlinJSPipeline p = new GremlinJSPipeline(graph);
  //        //factory.put("g", p);
  //        // evaluate JavaScript code from String
  //        //engine.eval("print('Hello, World')");
  //        //GremlinJS.load();
  //        final Bindings bindings = engine.createBindings();
  //        bindings.put("g", new GremlinJSPipeline(graph));
  //        bindings.put("T", new TokenT());
  //        if (arguments.size() > 0) {
  //            for (int i = 0; i < arguments.size(); i++) {
  //                bindings.put("a" + (i + 1), arguments.get(i));
  //            }
  //        }
  //        try {
  //            engine.eval(reader, bindings);
  //        } catch (Exception e) {
  //            System.err.println(e.getMessage());
  //        }
  //    }

}