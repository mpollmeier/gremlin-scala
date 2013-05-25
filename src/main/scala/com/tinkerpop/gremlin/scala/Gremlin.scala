package com.tinkerpop.gremlin.scala

import com.tinkerpop.gremlin.Tokens

object Gremlin {

  def version(): String = {
    Tokens.VERSION
  }

  def language(): String = {
    "gremlin-scala"
  }

}