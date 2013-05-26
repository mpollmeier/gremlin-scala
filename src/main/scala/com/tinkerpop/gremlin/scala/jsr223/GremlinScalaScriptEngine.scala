package com.tinkerpop.gremlin.scala.jsr223

import javax.script._
import java.io.Reader
import scala.annotation.tailrec
import com.tinkerpop.gremlin.scala.Gremlin
import java.io.File
import scala.collection.JavaConversions._
import java.io.FileWriter
import com.googlecode.scalascriptengine.ScalaScriptEngine
import scala.collection.mutable

object GremlinScalaScriptEngine {
  val ScalaLine = """\w*\s*(\w?)\s*=?\s*(.*)""".r
}

class GremlinScalaScriptEngine(workDir: String = "work") extends AbstractScriptEngine {
  import GremlinScalaScriptEngine._
  private lazy val factory = new GremlinScalaScriptEngineFactory
  val dir = s"$workDir/jsr223"
  val scriptDir = new File(dir)
  scriptDir.mkdirs()

  val engine = ScalaScriptEngine.onChangeRefresh(scriptDir)
  val vals = mutable.Map.empty[String, Any]

  def eval(script: String, context: ScriptContext) = {
    Option(context.getBindings(ScriptContext.ENGINE_SCOPE)) map { bindings ⇒
      bindings.keySet foreach { key ⇒
        println(s"engine scope: $key = ${bindings.get(key)}")
      }
    }

    try {
      writeScript(script)
      engine.refresh
      val scalaScript = engine.constructors[ScalaScript]("Script").newInstance(vals)
      val result = scalaScript.result

      /** jsr223 forces us to return an object... ;( */
      result.asInstanceOf[Object]
    } catch {
      case e: Throwable ⇒ "that didn't work ;("
    }
  }

  def parseLine(line: String) = line.trim match {
    case ScalaLine(returnName, command) ⇒
      if ("".equals(returnName))
        ParseResult(None, command.trim)
      else
        ParseResult(Some(returnName.trim), command.trim)
  }

  def writeScript(script: String) {
    val file = new FileWriter(s"$dir/Script.scala")
    file.write(fullScript(script))

    file.flush()
    file.close()
  }

  def fullScript(script: String) = """
import com.tinkerpop.gremlin.scala.jsr223.ScalaScript
import com.tinkerpop.blueprints.impls.tg._
import scala.collection.mutable
class Script(vals: mutable.Map[String, Any]) extends ScalaScript {""" +
    s""" //val a  = vals("a").asInstanceOf[Int]
          def result = $script
       }"""

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

/**
 * implemented by script file
 */
trait ScalaScript {
  def result: Any
}

case class ParseResult(returnName: Option[String], command: String)