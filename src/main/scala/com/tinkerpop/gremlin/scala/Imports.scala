package com.tinkerpop.gremlin.scala

import scala.collection.JavaConversions._
import com.tinkerpop.gremlin.{ Imports ⇒ GremlinImports }

object Imports {
  def get = GremlinImports.getImports.map(
    _.replace("static ", "")
      .replace("*", "_")
      .replace("$", ".")) :+ "com.tinkerpop.gremlin.scala._"
}