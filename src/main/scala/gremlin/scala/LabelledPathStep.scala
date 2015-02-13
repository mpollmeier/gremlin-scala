package gremlin.scala

import collection.JavaConversions._
import com.tinkerpop.gremlin.process._
import com.tinkerpop.gremlin.process.graph.traversal.step.map.MapStep
import com.tinkerpop.gremlin.process.traverser.TraverserRequirement
import shapeless._
import shapeless.ops.hlist._

class LabelledPathStep[S, Labels <: HList](traversal: Traversal[_, _]) extends MapStep[S, Labels](traversal.asAdmin) {

  override def getRequirements = Set(
    TraverserRequirement.PATH,
    TraverserRequirement.PATH_ACCESS
  )

  this.setFunction { traverser: Traverser[S] ⇒
    toHList(toList(traverser.path)): Labels
  }

  def toList(path: Path): List[Any] = {
    val labels = path.labels
    def hasUserLabel(i: Int) = !labels(i).isEmpty

    (0 until path.size) filter hasUserLabel map path.get[Any] toList
  }

  private def toHList[T <: HList](path: List[_]): T =
    if (path.length == 0)
      HNil.asInstanceOf[T]
    else
      (path.head :: toHList[IsHCons[T]#T](path.tail)).asInstanceOf[T]
}
