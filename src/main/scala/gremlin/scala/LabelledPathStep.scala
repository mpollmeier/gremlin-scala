package gremlin.scala

import collection.JavaConversions._
import org.apache.tinkerpop.gremlin.process.traversal.Path
import org.apache.tinkerpop.gremlin.process.traversal.Traversal
import org.apache.tinkerpop.gremlin.process.traversal.traverser.TraverserRequirement
import org.apache.tinkerpop.gremlin.process.traversal
import org.apache.tinkerpop.gremlin.process.traversal.step.map.MapStep
import org.apache.tinkerpop.gremlin.process.traversal.step.TraversalParent
import shapeless._
import shapeless.ops.hlist._
import scala.language.postfixOps

class LabelledPathStep[S, Labels <: HList](traversal: Traversal[_, _]) extends MapStep[S, Labels](traversal.asAdmin) with TraversalParent {

  override def getRequirements = Set(
    TraverserRequirement.PATH,
    TraverserRequirement.PATH_ACCESS
  )

  override def map(traverser: org.apache.tinkerpop.gremlin.process.traversal
.Traverser.Admin[S]): Labels =
    toHList(toList(traverser.path))

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
