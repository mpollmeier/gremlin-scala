package com.tinkerpop.gremlin.scala

import shapeless._
import ops.hlist._
import com.tinkerpop.gremlin.process._
import com.tinkerpop.gremlin.process.graph.map.MapStep
import com.tinkerpop.gremlin.process.util.PathConsumer

class TypedPathStep[S, Types <: HList](traversal: Traversal[_,_]) extends MapStep[S, Types](traversal) with PathConsumer {

  this.setFunction { holder: Traverser[S] =>
    toHList(toList(holder.getPath)): Types
  }

  def toList(path: Path) =
    (for (i <- 0 until path.size) yield path.get[Any](i)).toList

  private def toHList[T <: HList](path: List[_]): T =
    if(path.length == 0)
      HNil.asInstanceOf[T]
    else
      (path.head :: toHList[IsHCons[T]#T](path.tail)).asInstanceOf[T]
}
