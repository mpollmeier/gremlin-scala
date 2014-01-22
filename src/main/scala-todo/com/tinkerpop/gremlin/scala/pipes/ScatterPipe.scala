package com.tinkerpop.gremlin.scala.pipes

import scala.collection.JavaConversions._;

import com.tinkerpop.pipes.util.PipeHelper
import com.tinkerpop.pipes.transform.TransformPipe
import com.tinkerpop.pipes.AbstractPipe

/**
 * Just like com.tinkerpop.pipes.transform.ScatterPipe, but for Scala Traversables
 * TODO: port to more idiomatic Scala...
 */
class ScatterPipe[S, E] extends AbstractPipe[S, E] with TransformPipe[S, E] {
  var tempIterator: Iterator[E] = PipeHelper.emptyIterator.toIterator

  override def processNextStart(): E = {
    if (tempIterator.hasNext) {
      tempIterator.next()
    } else {
      starts.next() match {
        case t: Traversable[E] ⇒
          tempIterator = t.toIterator
          processNextStart()
        case other ⇒ other.asInstanceOf[E]
      }
    }
  }

  override def reset() = {
    tempIterator = PipeHelper.emptyIterator.toIterator
    super.reset()
  }
}

