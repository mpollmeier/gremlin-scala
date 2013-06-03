/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.tinkerpop.gremlin.scala.jsr223

import java.io.PrintWriter
import java.io.Reader
import java.io.StringWriter
import java.lang.reflect.InvocationTargetException
import java.security.PrivilegedActionException
import java.security.AccessController
import java.security.PrivilegedAction
import javax.script.ScriptContext
import javax.script.{
  ScriptEngineFactory ⇒ JavaxEngineFactory,
  Compilable,
  CompiledScript,
  ScriptEngine,
  AbstractScriptEngine,
  Bindings,
  SimpleBindings,
  ScriptException
}
import scala.tools.nsc.interpreter._
import _root_.scala.collection.JavaConversions._
import com.tinkerpop.gremlin.scala.Gremlin

/**
 * Copied and adapted from clerazza scala script engine: https://github.com/apache/clerezza
 * The code isn't really idiomatic scala, but it works...
 */
class ScriptEngineFactory() extends JavaxEngineFactory {
  val interpreter = new Interpreter(new PrintWriter(System.out))
  val msgWriter = new StringWriter

  val name = "gremlin-scala"
  override def getEngineName() = name
  override def getEngineVersion() = Gremlin.version
  override def getExtensions() = List(name)
  override def getMimeTypes() = List("application/x-scala")
  override def getNames() = List(name)
  override def getLanguageName() = name
  override def getLanguageVersion = "2.10.1"
  override def getParameter(key: String) = key match {
    case ScriptEngine.ENGINE           ⇒ getEngineName
    case ScriptEngine.ENGINE_VERSION   ⇒ getEngineVersion
    case ScriptEngine.NAME             ⇒ name
    case ScriptEngine.LANGUAGE         ⇒ getLanguageName
    case ScriptEngine.LANGUAGE_VERSION ⇒ getLanguageVersion
    case _                             ⇒ null
  }

  override def getMethodCallSyntax(obj: String, m: String, args: String*) = s"$obj.$m(${args.mkString(",")})"
  override def getOutputStatement(toDisplay: String) = s"""println("$toDisplay")"""
  override def getProgram(statements: String*) = statements.mkString("\n")
  override def getScriptEngine: ScriptEngine = GremlinScalaScriptEngine

  /**
   * Inner object as it accesse interpreter
   */
  object GremlinScalaScriptEngine extends AbstractScriptEngine() with Compilable {
    override def eval(script: Reader, context: ScriptContext): Object = {
      val scriptStringWriter = new StringWriter()
      var ch = script.read
      while (ch != -1) {
        scriptStringWriter.write(ch)
        ch = script.read
      }
      eval(scriptStringWriter.toString, context)
    }

    import ScriptEngineFactory.this.interpreter.Request
    private def responseLine(request: Request): String = (request.termNames, request.getEval) match {
      case (name :: _, Some(eval)) ⇒ s"$name: ${request.typeOf(name)} = $eval"
      case (name :: _, None)       ⇒ s"$name: ${request.typeOf(name)}"
      case (Nil, _)                ⇒ ""
    }

    override def eval(script: String, context: ScriptContext): Object = {
      for (
        scope ← context.getScopes if (context.getBindings(scope.intValue) != null);
        (name, obj) ← context.getBindings(scope.intValue)
      ) interpreter.bind(name, getAccessibleClass(obj.getClass).getName, obj)

      val result = interpreter.interpret(script)
      if (interpreter.reporter.hasErrors) {
        throw new ScriptException("some error", "script-file", 1)
      }

      responseLine(interpreter.lastRequest)
    }

    override def getFactory() = ScriptEngineFactory.this
    override def createBindings(): Bindings = new SimpleBindings

    override def compile(script: Reader): CompiledScript = ???
    override def compile(script: String): CompiledScript = ???

    /**
     * returns an accessible class or interface that is implemented by class,
     * is doesn't look for superinterfaces of implement interfaces
     */
    private def getAccessibleClass(clazz: Class[_]): Class[_] =
      if (isAccessible(clazz)) {
        clazz
      } else {
        val foo: Class[_] = clazz.getInterfaces()(0)
        for (implementedInterface ← clazz.getInterfaces()) {
          if (isAccessible(implementedInterface)) return implementedInterface
        }
        getAccessibleSuperClass(clazz)
      }

    private def getAccessibleSuperClass(clazz: Class[_]): Class[_] = {
      val superClass = clazz.getSuperclass
      if (superClass == null) {
        throw new RuntimeException("No upper class to be checked for accessibility for " + clazz)
      }
      if (isAccessible(superClass))
        superClass
      else
        getAccessibleSuperClass(superClass)

    }

    private def isAccessible(clazz: Class[_]) =
      try {
        Class.forName(clazz.getName)
        true
      } catch {
        case e: Exception ⇒ false
      }
  }
}
