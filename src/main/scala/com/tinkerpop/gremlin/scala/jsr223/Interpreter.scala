package com.tinkerpop.gremlin.scala.jsr223

import scala.collection.JavaConversions._
import scala.tools.nsc._
import scala.tools.nsc.interpreter._
import java.io.PrintWriter
import scala.tools.nsc.reporters.Reporter
import com.tinkerpop.gremlin.scala.Imports

class Interpreter(out: PrintWriter) extends IMain(new Settings, out) {
  beQuietDuring {
    addImports(Imports.forRexster: _*)
  }

  override lazy val classLoader = new AbstractFileClassLoader(virtualDirectory, getClass.getClassLoader)

  override protected def newCompiler(settings: Settings, reporter: Reporter) = {
    settings.outputDirs.setSingleOutput(virtualDirectory)
    settings.usejavacp.value = true
    new Global(settings, reporter) with ReplGlobal
  }

}

