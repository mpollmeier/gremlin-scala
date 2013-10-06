package com.tinkerpop.gremlin.scala

import com.tinkerpop.gremlin.Tokens
import com.tinkerpop.pipes.PipeFunction

class ScalaPipeFunction[S, E](fun: S ⇒ E) extends PipeFunction[S, E] {
  override def compute(argument: S) = fun(argument)
}
