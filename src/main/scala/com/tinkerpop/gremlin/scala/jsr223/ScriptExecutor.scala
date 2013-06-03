package com.tinkerpop.gremlin.scala.jsr223

import java.io.FileReader
import java.io.Reader
import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory
import javax.script.ScriptEngineManager
import com.tinkerpop.gremlin.scala.Gremlin
import java.io.File
import java.io.FileWriter
import scala.collection.mutable
import scala.collection.JavaConversions._
import javax.script.Compilable

object ScriptExecutor extends App {
  val factory = new ScriptEngineManager
  val engine = factory.getEngineByName("gremlin-scala")

  println(engine.eval("42") + "\n")
  println(engine.eval("val a = 42") + "\n")
  println(engine.eval("a + 42") + "\n")
}