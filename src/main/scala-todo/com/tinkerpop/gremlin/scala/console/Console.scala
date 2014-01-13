package com.tinkerpop.gremlin.scala.console

import scala.tools.nsc.Settings
import scala.tools.nsc.interpreter.ILoop
import com.tinkerpop.gremlin.scala.Imports
import scala.tools.nsc.interpreter.ReplReporter

/**
 * http://www.scala-lang.org/archives/downloads/distrib/files/nightly/docs/compiler/scala/tools/nsc/interpreter/package.html
 * http://www.michaelpollmeier.com/create-your-custom-scala-repl/
 */
object Console extends App {
  val settings = new Settings
  settings.usejavacp.value = true
  settings.deprecation.value = true

  new GremlinILoop().process(settings)
}

class GremlinILoop extends ILoop {
  override def prompt = "gremlin> "

  addThunk {
    intp.beQuietDuring {
      intp.addImports(Imports.asList: _*)
    }
  }

  override def printWelcome() {
    echo("\n" +
      "         \\,,,/\n" +
      "         (o o)\n" +
      "-----oOOo-(_)-oOOo-----")
  }

  var gremlinIntp: GremlinInterpreter = _
  override def createInterpreter() {
    if (addedClasspath != "")
      settings.classpath.append(addedClasspath)
    gremlinIntp = new GremlinInterpreter
    intp = gremlinIntp
  }

  override def command(line: String): Result = {
    val result = super.command(line)
    if (result.keepRunning && result.lineToRecord.isDefined)
      printLastValue
    result
  }

  /**Prints the last value by expanding its elements if it's iterator-like or collection-like. */
  def printLastValue = gremlinIntp.lastValue match {
    case Some(value) ⇒ for (v ← toIterator(value)) out.println("==> " + v)
    case _           ⇒
  }

  /**Coerces the specified value into an iterator. */
  def toIterator(value: Any): Iterator[Any] = {
    import scala.collection.JavaConverters._
    value match {
      case t: Traversable[Any]      ⇒ t.toIterator
      case a: Array[_]              ⇒ a.toIterator
      case i: java.lang.Iterable[_] ⇒ i.asScala.toIterator
      case i: java.util.Iterator[_] ⇒ i.asScala
      case m: java.util.Map[_, _]   ⇒ m.asScala.toIterator
      case _                        ⇒ Iterator.single(value)
    }
  }

  class GremlinInterpreter extends ILoopInterpreter {
    def prevRequest: Option[Request] = Option(lastRequest)

    /**Returns the last value evaluated by this interpreter. See https://issues.scala-lang.org/browse/SI-4899 */
    def lastValue: Option[AnyRef] = prevRequest flatMap (_.lineRep.callOpt("$result"))
  }

}

