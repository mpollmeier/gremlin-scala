package com.tinkerpop.gremlin.scala.jsr223

import javax.script._
import java.io.Reader
import scala.annotation.tailrec

class GremlinScalaScriptEngine extends AbstractScriptEngine {
  private val manager = new ScriptEngineManager
  private val engine = manager.getEngineByName("scala")
  lazy val factory = new GremlinScalaScriptEngineFactory

  def eval(script: String, context: ScriptContext) = engine.eval(script, context)
  def eval(reader: Reader, context: ScriptContext) = eval(readFully(reader), context)
  def createBindings(): Bindings = new SimpleBindings
  def getFactory() = factory

  private def readFully(reader: Reader): String = {
    val arr = new Array[Char](8192)
    @tailrec
    def go(acc: StringBuilder): String = {
      if (reader.read(arr, 0, 8192) > 0)
        go(acc.append(arr))
      else acc.toString
    }
    go(new StringBuilder)
  }
}