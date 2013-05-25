package com.tinkerpop.gremlin.scala.jsr223

import java.io.FileReader
import java.io.Reader
import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory
import javax.script.ScriptEngineManager

object ScriptExecutor extends App {
  //  args.toList match {
  //    case Nil            ⇒ System.err.println("Usage: <path_to_scala_script> <arg1> <arg2> ...")
  //    case script :: args ⇒ evaluate(new FileReader(script), args)
  //  }

  val graph = TinkerGraphFactory.createTinkerGraph
  val manager = new ScriptEngineManager
  val engine = manager.getEngineByName("scala")

  // evalute scala code from string
  engine.eval("""println("Hello, World")""")

  //  def evaluate(reader: Reader, args: List[String]) {

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