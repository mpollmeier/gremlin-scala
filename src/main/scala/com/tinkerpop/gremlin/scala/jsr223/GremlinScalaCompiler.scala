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


import scala.tools.nsc.Global
import scala.tools.nsc.Settings
import scala.tools.nsc.interpreter.ReplGlobal
import tools.nsc.reporters.{Reporter, ConsoleReporter}
import scala.reflect.internal.util.{Position, NoPosition, FakePos}
import java.io.{PrintWriter, StringWriter, File}
//import org.slf4j.scala._

/*
 * unfortunately there seems to be no way to change the classpath, so this doesn't
 * listen to BundleEvents
 * TODO: check if this is still true with Scala 2.20
 */
class GremlinScalaCompiler( settings: Settings, reporter: Reporter)
		extends Global(settings, reporter) with ReplGlobal /*with Logging*/ { self =>

  //TODO: need to set classpaths
  /*override lazy val platform: ThisPlatform = {
    new { val global: self.type = self } with JavaPlatform {
      override lazy val classPath = {
        createClassPath[AbstractFile](super.classPath)
      }
    }
  }

  override def classPath = platform.classPath

  def createClassPath[T](original: ClassPath[T]) = {
    
    var result = ListBuffer(original)
    for (bundle <- bundleContext.getBundles; if bundle.getResource("/") != null) {
      try {
        val files = BundleClassPathBuilder.fromBundle(bundle)
        files.foreach(file => {
          //debug("Adding bundle " + file + " to the Scala compiler classpath")
          result += original.context.newClassPath(file)
        })
      } catch {
        case e: Exception => logger.debug(e.toString)
      }

    }
    new MergedClassPath(result.toList.reverse, original.context)
  }*/

}


