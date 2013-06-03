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

import com.tinkerpop.gremlin.scala.jsr223.util.SplittingDirectory
import com.tinkerpop.gremlin.scala.jsr223.util.VirtualDirectoryWrapper
import scala.collection.mutable
import scala.tools.nsc._
import scala.reflect.internal.util.{ BatchSourceFile, SourceFile }
import scala.tools.nsc.interpreter._
import scala.tools.nsc.io.AbstractFile
import scala.tools.nsc.io.VirtualDirectory
import scala.tools.nsc.reporters.ConsoleReporter
import scala.tools.nsc.reporters.Reporter
import java.io.PrintWriter

/**
 * a compiler that keeps track of classes added to the directory
 */
class TrackingCompiler private ( /*bundleContext : BundleContext,*/
  settings: Settings, reporter: Reporter, classLoaderBuilder: () ⇒ ClassLoader,
  writtenClasses: mutable.ListBuffer[AbstractFile])
    extends GremlinScalaCompiler( /*bundleContext : BundleContext,*/
      settings: Settings, reporter: Reporter) {

  /**
   * compiles a list of classes to settings.outputDirs returning a
   * the generated AbstractFiles
   */
  def compileToDir(sources: List[Array[Char]]): List[AbstractFile] = {
    writtenClasses.clear()
    var i = 0
    val sourceFiles: List[SourceFile] = for (chars ← sources) yield {
      i = i + 1;
      new BatchSourceFile("<script" + i + ">", chars)
    }
    (new Run).compileSources(sourceFiles)
    if (reporter.hasErrors) {
      reporter.reset
      throw new CompileErrorsException;
    }
    writtenClasses.toList
  }

  /**
   * compiles a list of class sources returning a list of compiled classes
   */
  @throws(classOf[CompileErrorsException])
  def compile(sources: List[Array[Char]]): List[Class[_]] = {
    val classFiles = compileToDir(sources)
    val classLoader = classLoaderBuilder()
    val result: List[Class[_]] = for (
      classFile ← classFiles;
      if (!classFile.name.contains('$'))
    ) yield {
      val path = classFile.path
      val relevantPath = path.substring(path.indexOf('/') + 1, path.lastIndexOf('.'))
      val fqn = relevantPath.replace("/", ".")
      classLoader.loadClass(fqn)
    }
    result
  }

}

object TrackingCompiler {

  private class TrackingCompilerSplittingDirectory extends SplittingDirectory

  private def createClassLoader(dir: AbstractFile) = new AbstractFileClassLoader(dir, this.getClass.getClassLoader())

  def apply( /*bundleContext : BundleContext, */ out: PrintWriter, outputDirectoryOption: Option[AbstractFile]) = {
    val (outputDirectory, classLoaderBuilder): (AbstractFile, () ⇒ ClassLoader) = outputDirectoryOption match {
      case Some(d) ⇒ (d, () ⇒ createClassLoader(d))
      case None ⇒ {
        val d = new TrackingCompilerSplittingDirectory
        d.currentTarget = new VirtualDirectory("(memory)", None)
        def createClassLoaderAndReset() = {
          val r = createClassLoader(d.currentTarget)
          d.currentTarget = new VirtualDirectory("(memory)", None)
          r
        }
        (d, createClassLoaderAndReset _)
      }
    }

    val writtenClasses: mutable.ListBuffer[AbstractFile] = mutable.ListBuffer[AbstractFile]()
    val settings = {
      def outputListener(abtractFile: AbstractFile) {
        writtenClasses += abtractFile
      }
      val settings = new Settings
      settings.outputDirs setSingleOutput VirtualDirectoryWrapper.wrap(outputDirectory, outputListener)
      settings
    }
    new TrackingCompiler( /*bundleContext,*/
      settings,
      new ConsoleReporter(settings, null, out) {
        override def printMessage(msg: String) {
          out write msg
          out.flush()
        }
      }, classLoaderBuilder, writtenClasses)
  }
}

