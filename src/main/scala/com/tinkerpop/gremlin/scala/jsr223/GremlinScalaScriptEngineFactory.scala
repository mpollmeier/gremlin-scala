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
import javax.script.{ScriptEngineFactory => JavaxEngineFactory, Compilable, 
					 CompiledScript, ScriptEngine, AbstractScriptEngine, Bindings,
					 SimpleBindings, ScriptException}
import scala.actors.DaemonActor
import scala.tools.nsc.interpreter._
import scala.tools.nsc.io.VirtualDirectory

//TODO have the interpretatin function back running, consider using http://code.google.com/p/scalascriptengine
class GremlinScalaScriptEngineFactory() extends  JavaxEngineFactory /*with BundleListener*/  {

	private var factory: InterpreterFactory = null
	private var compilerService: CompilerService = null
	var _interpreter : IMain = null
	//private var bundleContext: BundleContext = null
	def interpreter = {
		if (_interpreter == null) {
			this.synchronized {
				if (_interpreter == null) {
					_interpreter = factory.createInterpreter(new PrintWriter(System.out))
				}
			}
		}
		_interpreter
	}


	//methods from GremlinScalaScriptEngineFactory
	override def getEngineName() = "Scala Scripting Engine for OSGi"
	override def getEngineVersion() = "0.3/scala 2.10.0"
	override def getExtensions() = java.util.Collections.singletonList("scala")
	override def getMimeTypes() = java.util.Collections.singletonList("application/x-scala")
	override def getNames() = java.util.Collections.singletonList("scala")
	override def getLanguageName() = "Scala"
	override def getLanguageVersion ="2.10.0"
	override def getParameter(key : String) = {
		key match {
			case ScriptEngine.ENGINE => getEngineName
			case ScriptEngine.ENGINE_VERSION => getEngineVersion
			case ScriptEngine.NAME => getNames.get(0)
			case ScriptEngine.LANGUAGE => getLanguageName
			case ScriptEngine.LANGUAGE_VERSION => getLanguageVersion
			case _ => null
		}
	}
	override def getMethodCallSyntax(obj : String,
									 m : String,
									 args : String*) = {
		obj+"."+m+"("+args.mkString(",")+")"
	}
	override def getOutputStatement(toDisplay : String) = "println(\""+toDisplay+"\")"
	override def getProgram(statements : String*) = statements.mkString("\n")
	override def getScriptEngine : ScriptEngine = GremlinScalaScriptEngine

	def bindInterpreterFactory(f: InterpreterFactory) = {
		factory = f
	}

	def unbindInterpreterFactory(f: InterpreterFactory) = {
		factory = null
		_interpreter = null
	}

	/** Inner object as it accesse interpreter
	 */
	object GremlinScalaScriptEngine extends AbstractScriptEngine() with Compilable {
		override def eval(script : Reader, context : ScriptContext) : Object = {
			val scriptStringWriter = new StringWriter()
			var ch = script.read
			while (ch != -1) {
				scriptStringWriter.write(ch)
				ch = script.read
			}
			eval(scriptStringWriter.toString, context)
		}

		val interpreterAction = new DaemonActor {
			def act() {
				//not using loop { react {, as this method doesn't seem to guarantee
				//asynchronous execution
				//also using react with a final invocation of act() different exception from interprter.bind have been seen
				while(true) {
					receive {
						case (script : String, context : ScriptContext) => {
							//not yet threadsafe, but the test isn't failing
							//should pass jobs to actor that guarantees they are executed sequentially
							//and binding to not inferfere
							try {
								val jTypeMap : java.util.Map[String, java.lang.reflect.Type] =
								new java.util.HashMap[String, java.lang.reflect.Type]()
									val valueMap = new java.util.HashMap[String, Any]()
								import _root_.scala.collection.JavaConversions._
								for (scope <- context.getScopes;
									 if (context.getBindings(scope.intValue) != null);
									 entry <- context.getBindings(scope.intValue)) {
									interpreter.bind(entry._1,
													 getAccessibleClass(entry._2.getClass).getName, entry._2)
								}
							  interpreter.interpret("val zzScriptResult = {"+script+"}")
							  println("visible stuff:")
							  System.out.println("stuff stuff stuff");
								println("interpreter.valueOfTerm(\"zzScriptResult\"): "+interpreter.valueOfTerm("zzScriptResult"))
							  interpreter.visibleTermNames.foreach(println)
								if (interpreter.reporter.hasErrors) {
									throw new ScriptException("some error","script-file",1)
								}
								sender ! interpreter.valueOfTerm("zzScriptResult")
							} catch {
								case e => sender ! GremlinScalaScriptEngineFactory.ActorException(e)
							}
						}
						case GremlinScalaScriptEngineFactory.RefreshInterpreter => {
								_interpreter = null;
						}
					}
				}
			}
		}
		interpreterAction.start()


		override def eval(script : String, context : ScriptContext) : Object = {
			/*val timeout = 180*1000
			interpreterAction !? (timeout, (script, context)) match {
				case Some(x : Object) => x
				case x => throw new RuntimeException("Timeout executing script")
			}*/
			interpreterAction !? ((script, context)) match {
				case GremlinScalaScriptEngineFactory.ActorException(e) => throw e
				case x : Object => x match {
				  case Some(y:Object) => y
				  case None => null
				}
			}
		}
		
		override def getFactory() = GremlinScalaScriptEngineFactory.this
		override def createBindings() : Bindings = new SimpleBindings

		override def compile(script: Reader): CompiledScript = {
			val scriptStringWriter = new StringWriter()
			var ch = script.read
			while (ch != -1) {
				scriptStringWriter.write(ch)
				ch = script.read
			}
			compile(scriptStringWriter.toString)
		}

		
		var classCounter = 0

		val virtualDirectory = new VirtualDirectory("(memory)", None)
		var msgWriter = new StringWriter


		
		override def compile(script: String): CompiledScript = {
			try {
				AccessController.doPrivileged(new PrivilegedAction[CompiledScript]() {
					override def run() =  {
						val objectName = "CompiledScript"+classCounter
						classCounter += 1
						val classCode = "class " + objectName + """ {
							|	def run($: Map[String, Object]) = {
							|""".stripMargin + script +"""
							|	}
							|}""".stripMargin
							val sources: List[Array[Char]] = List(classCode.toCharArray)
						val clazz = try {
							compilerService.compile(sources)(0)
						} catch {
							case e: CompileErrorsException => throw new ScriptException(e.getMessage, "script", -1);
							case e => throw e
						}
						val scriptObject = clazz.newInstance()

						new CompiledScript() {
							override def eval(context: ScriptContext) = {

								var map = Map[String, Object]()
								import _root_.scala.collection.JavaConversions._
								for (	scope <- context.getScopes;
										if (context.getBindings(scope.intValue) != null);
										entry <- context.getBindings(scope.intValue)) {
									map = map + (entry._1 -> entry._2)
								}
								val runMethod = clazz.getMethod("run", classOf[Map[String, Object]])
								try {
									runMethod.invoke(scriptObject, map)
								} catch {
									case e: InvocationTargetException => {
										throw e.getCause
									}
								}
							}
							override def getEngine = GremlinScalaScriptEngine.this
						}
					}
				})
			} catch {
				case e: PrivilegedActionException => throw e.getCause
			}
		}

		

		

		/**
		 * returns an accessible class or interface that is implemented by class,
		 * is doesn't look for superinterfaces of implement interfaces
		 */
		private def getAccessibleClass(clazz : Class[_]) : Class[_] = {
			if(isAccessible(clazz)) {
				return clazz
			} else {
				val foo : Class[_] = clazz.getInterfaces()(0)
				for (implementedInterface <- clazz.getInterfaces()) {
					if (isAccessible(implementedInterface)) return implementedInterface
				}
			}
			getAccessibleSuperClass(clazz)
		}

		private def getAccessibleSuperClass(clazz : Class[_]) : Class[_] = {
			val superClass = clazz.getSuperclass
			if (superClass == null) {
				throw new RuntimeException("No upper class to be checked for accessibility for "+clazz)
			}
			if (isAccessible(superClass)) {
				superClass
			} else {
				getAccessibleSuperClass(superClass)
			}
		}

		private def isAccessible(clazz : Class[_])  = {
			try {
				Class.forName(clazz.getName)
				true
			} catch {
				case e: Exception => false
			}
		}
	}
}

object GremlinScalaScriptEngineFactory {
	case class ActorException(e: Throwable);
	case object RefreshInterpreter;
}