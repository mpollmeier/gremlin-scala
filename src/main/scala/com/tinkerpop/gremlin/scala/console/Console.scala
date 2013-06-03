package com.tinkerpop.gremlin.scala.console

import scala.tools.nsc.Settings
import scala.tools.nsc.reporters.ConsoleReporter
import scala.tools.nsc.interpreter.{ ILoop, ReplReporter }
import com.tinkerpop.gremlin.scala.Imports

/**http://www.scala-lang.org/archives/downloads/distrib/files/nightly/docs/compiler/scala/tools/nsc/interpreter/package.html */
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
      intp.addImports(Imports.forConsole: _*)
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

  /**Overriden to print out the value evaluated from the specified line. */
  override def command(line: String): Result = {
    val result = super.command(line)

    //TODO handle compiler error
    //TODO handle exception
    //TODO handle something like class on multilines
    if (result.keepRunning && result.lineToRecord.isDefined)
      printLastValue

    result
  }

  /**Prints the last value by expanding its elements if it's iterator-like or collection-like. */
  def printLastValue() = gremlinIntp.lastValue match {
    case Right(value)    ⇒ for (v ← toIterator(value)) out.println("==>" + v)
    case Left(throwable) ⇒ throwable.printStackTrace(out)
  }

  /**Coerces the specified value into an iterator. */
  def toIterator(value: Any): Iterator[Any] = {
    import scala.collection.JavaConverters._
    value match {
      case t: Traversable[Any]        ⇒ t.toIterator
      case a: Array[_]                ⇒ a.toIterator
      case i: java.lang.Iterable[Any] ⇒ i.asScala.toIterator
      case i: java.util.Iterator[Any] ⇒ i.asScala
      case m: java.util.Map[Any, Any] ⇒ m.asScala.toIterator
      case _                          ⇒ Iterator.single(value)
    }
  }

  class GremlinInterpreter extends ILoopInterpreter {
    override lazy val reporter: ReplReporter = new ReplReporter(this) {
      /**Stop ReplReporter from printing to console. Instead we print in GremlinILoop.command. */
      override def printMessage(msg: String) {}
    }
    def prevRequest: Option[Request] = prevRequestList.lastOption

    /**Returns the last value evaluated by this interpreter. See https://issues.scala-lang.org/browse/SI-4899 for details. */
    def lastValue: Either[Throwable, AnyRef] =
      prevRequest.getOrElse(throw new NullPointerException()).lineRep.callEither("$result")
  }

}

