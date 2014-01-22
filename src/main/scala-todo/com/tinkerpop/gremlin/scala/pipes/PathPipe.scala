package com.tinkerpop.gremlin.scala.pipes

import scala.collection.JavaConversions._
import com.tinkerpop.pipes.transform.TransformPipe
import com.tinkerpop.pipes.AbstractPipe
import com.tinkerpop.pipes.Pipe
import java.util.{List ⇒ JList, Iterator ⇒ JIterator}
import shapeless._
import ops.hlist._


class PathPipe[S, E <: HList] extends AbstractPipe[S, E] with TransformPipe[S, E] {
  override def setStarts(starts: JIterator[S]): Unit = {
    super.setStarts(starts)
    this.enablePath(true)
  }

  override def processNextStart(): E = starts match {
    case starts: Pipe[_,_] ⇒ 
      starts.next()
      val path: JList[_] = starts.getCurrentPath
      toHList[E](path.toList)
  }

  def toHList[T <: HList](path: List[_]): T = 
    if(path.length == 0)
      HNil.asInstanceOf[T]
    else
      (path.head :: toHList[IsHCons[T]#T](path.tail)).asInstanceOf[T]
}

