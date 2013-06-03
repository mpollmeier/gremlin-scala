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

import scala.collection.JavaConversions._
import scala.tools.nsc._
import scala.tools.nsc.interpreter._
import java.io.PrintWriter
import scala.tools.nsc.reporters.Reporter
import com.tinkerpop.gremlin.scala.Imports

/**
 * Copied and adapted from clerazza scala script engine: https://github.com/apache/clerezza
 */
class GremlinScalaInterpreter(out: PrintWriter) extends IMain(new Settings, out) {

  beQuietDuring {
    addImports(Imports.get: _*)
  }

  override lazy val classLoader: AbstractFileClassLoader = {
    new AbstractFileClassLoader(virtualDirectory, this.getClass.getClassLoader())
  }
  override protected def newCompiler(settings: Settings, reporter: Reporter) = {
    settings.outputDirs setSingleOutput virtualDirectory
    new GremlinScalaCompiler(settings, reporter)
  }

}

