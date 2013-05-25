package com.tinkerpop.gremlin.scala.jsr223

import javax.script.ScriptEngine
import javax.script.ScriptEngineFactory
import javax.script.ScriptEngineManager
import scala.collection.JavaConversions._
import com.tinkerpop.gremlin.scala.Gremlin

class GremlinScalaScriptEngineFactory extends ScriptEngineFactory {
  def getEngineName() = Gremlin.language
  def getEngineVersion() = Gremlin.version
  def getExtensions() = List("scala")
  def getMimeTypes() = List("plain")
  def getNames() = List(Gremlin.language)
  def getLanguageName() = Gremlin.language
  def getLanguageVersion() = Gremlin.version

  def getParameter(key: String) = key match {
    case ScriptEngine.ENGINE           ⇒ getEngineName
    case ScriptEngine.ENGINE_VERSION   ⇒ getEngineVersion
    case ScriptEngine.NAME             ⇒ getEngineName
    case ScriptEngine.LANGUAGE         ⇒ getLanguageName
    case ScriptEngine.LANGUAGE_VERSION ⇒ getLanguageVersion
  }

  def getMethodCallSyntax(obj: String, m: String, args: String*) = "check out the wiki, sorry ;)"
  def getOutputStatement(toDisplay: String) = s"println $toDisplay"
  def getProgram(statements: String*) = statements.reduce(_ + "\n" + _)
  def getScriptEngine() = new GremlinScalaScriptEngine
}