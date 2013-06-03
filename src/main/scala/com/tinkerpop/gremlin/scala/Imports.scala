package com.tinkerpop.gremlin.scala

import scala.collection.JavaConversions._
import com.tinkerpop.gremlin.{ Imports ⇒ GremlinImports }

object Imports {
  def forConsole = GremlinImports.getImports.map(
    _.replace("static ", "")
      .replace("*", "_")
      .replace("$", ".")) :+ "com.tinkerpop.gremlin.scala._"

  def forRexster = forConsole.filter { imp ⇒
    rexsterNotAvailable.forall(!imp.startsWith(_))
  }

  val rexsterNotAvailable = List(
    "com.tinkerpop.blueprints.oupls.sail",
    "com.tinkerpop.blueprints.impls.orient.batch",
    "com.tinkerpop.gremlin.pipes")

}