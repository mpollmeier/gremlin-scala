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

import java.security.AccessController
import java.security.Permission
import java.security.PrivilegedExceptionAction
import java.security.PrivilegedActionException
import scala.tools.nsc.io.AbstractFile
import scala.tools.nsc.io.VirtualDirectory
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.io.PrintWriter

/**
 * Copied and adapted from clerazza scala script engine: https://github.com/apache/clerezza
 */
class CompileErrorsException(message: String) extends Exception(message) {
  def this() = this(null)
}

class CompilePermission extends Permission("Compile Permssion") {
  def getActions() = ""
  def implies(p: Permission) = p.isInstanceOf[CompilePermission]
  override def equals(o: Any): Boolean = o.isInstanceOf[CompilePermission]
  override def hashCode = classOf[CompilePermission].hashCode
}

class CompilerService {

  protected val sharedVirtualDirectory = new VirtualDirectory("(memory)", None)
  protected var currentSharedCompilerOutputStream: OutputStream = null
  protected val splipptingOutputStream = new OutputStream() {
    def write(b: Int) {
      if (currentSharedCompilerOutputStream == null) {
        throw new IOException("no currentSharedCompilerOutputStream set")
      }
      currentSharedCompilerOutputStream.write(b)
    }
  }
  protected val splittingPrintWriter = new PrintWriter(splipptingOutputStream, true)

  protected var currentSharedCompiler: TrackingCompiler = null;
  protected def sharedCompiler = {
    if (currentSharedCompiler == null) {
      synchronized {
        if (currentSharedCompiler == null) {
          currentSharedCompiler = TrackingCompiler( /*bundleContext,*/ splittingPrintWriter, None)
          //createCompiler(splittingPrintWriter, sharedVirtualDirectory)
        }
      }
    }
    currentSharedCompiler
  }

  def createCompiler(out: PrintWriter, outputSirectory: AbstractFile): TrackingCompiler = {
    TrackingCompiler(out, Some(outputSirectory))
  }

  def compile(sources: Array[Array[Char]]): Array[Class[_]] = {
    compile(sources.toList).toArray
  }

  def compile(sources: List[Array[Char]]): List[Class[_]] = {
    AccessController.checkPermission(new CompilePermission)
    sharedCompiler.synchronized {
      AccessController.doPrivileged[List[Class[_]]](
        new PrivilegedExceptionAction[List[Class[_]]] {
          def run(): List[Class[_]] = {
            val baos = new ByteArrayOutputStream
            currentSharedCompilerOutputStream = baos
            try {
              sharedCompiler.compile(sources)
            } catch {
              case c: CompileErrorsException ⇒ throw new CompileErrorsException(
                new String(baos.toByteArray, "utf-8"))
              case e: Throwable ⇒ throw e
            } finally {
              currentSharedCompilerOutputStream = null
            }
          }
        })
    }
  }

  /**
   * compiles a set of sources with a dedicated compiler
   */
  def compileIsolated(sources: List[Array[Char]]): List[Class[_]] = {
    val virtualDirectory = new VirtualDirectory("(memory)", None)
    compileIsolated(sources, virtualDirectory)
  }

  def compileIsolated(sources: List[Array[Char]], outputDirectory: AbstractFile): List[Class[_]] = {
    AccessController.checkPermission(new CompilePermission)
    sharedCompiler.synchronized {
      try {
        AccessController.doPrivileged[List[Class[_]]](
          new PrivilegedExceptionAction[List[Class[_]]] {
            def run() = {
              val out = new ByteArrayOutputStream
              val printWriter = new PrintWriter(out)
              val compiler = createCompiler(printWriter, outputDirectory)
              try {
                compiler.compile(sources)
              } catch {
                case c: CompileErrorsException ⇒ throw new CompileErrorsException(new String(out.toByteArray, "utf-8"))
                case e: Throwable              ⇒ throw e
              }
            }
          })
      }
    }
  }

  def compileToDir(sources: List[Array[Char]], outputDirectory: AbstractFile): List[AbstractFile] = {
    AccessController.checkPermission(new CompilePermission)
    sharedCompiler.synchronized {
      try {
        AccessController.doPrivileged[List[AbstractFile]](
          new PrivilegedExceptionAction[List[AbstractFile]] {
            def run() = {
              val out = new ByteArrayOutputStream
              val printWriter = new PrintWriter(out)
              val compiler = createCompiler(printWriter, outputDirectory)
              try {
                compiler.compileToDir(sources)
              } catch {
                case c: CompileErrorsException ⇒ throw new CompileErrorsException(new String(out.toByteArray, "utf-8"))
                case e: Throwable              ⇒ throw e
              }
            }
          })
      }
    }
  }

}
