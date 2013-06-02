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


import scala.tools.nsc.interpreter._;
import java.io.PrintWriter

class InterpreterFactory() {
	
	/*protected var bundles: Array[Bundle] = null
	protected var bundleContext : BundleContext = null;

	def activate(componentContext: ComponentContext)= {
		bundleContext = componentContext.getBundleContext
	}

	def deactivate(componentContext: ComponentContext) = {
		bundleContext = null
	}*/

	def createInterpreter(out: PrintWriter) : IMain = {
		val i = new GremlinScalaInterpreter(/*bundleContext,*/ out)
		i
	}

	
}
