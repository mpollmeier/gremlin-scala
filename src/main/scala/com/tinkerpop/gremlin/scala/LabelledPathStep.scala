package com.tinkerpop.gremlin.scala

import collection.JavaConversions._
import com.tinkerpop.gremlin.process._
import com.tinkerpop.gremlin.process.graph.marker.PathConsumer
import com.tinkerpop.gremlin.process.graph.step.map.MapStep
import shapeless._
import shapeless.ops.hlist._

// class TypedPathStep[S, Types <: HList](traversal: Traversal[_,_]) extends MapStep[S, Types](traversal) with PathConsumer {
//
//   this.setFunction { traverser: Traverser[S] ⇒
//     toHList(toList(traverser.getPath)): Types
//   }
//
//   def toList(path: Path) =
//     (for (i <- 0 until path.size) yield path.get[Any](i)).toList
//
//   private def toHList[T <: HList](path: List[_]): T =
//     if(path.length == 0)
//       HNil.asInstanceOf[T]
//     else
//       (path.head :: toHList[IsHCons[T]#T](path.tail)).asInstanceOf[T]
// }

class LabelledPathStep[S, Types <: HList](traversal: Traversal[_, _]) extends MapStep[S, Types](traversal) with PathConsumer {

  this.setFunction { traverser: Traverser[S] ⇒
    toHList(toList(traverser.getPath)): Types
  }

  def toList(path: Path): List[Any] = {
    val labels = path.getLabels
    (0 until path.size) filterNot (i ⇒ labels(i).isEmpty) map path.get[Any] toList
  }

  private def toHList[T <: HList](path: List[_]): T =
    if (path.length == 0)
      HNil.asInstanceOf[T]
    else
      (path.head :: toHList[IsHCons[T]#T](path.tail)).asInstanceOf[T]
}
