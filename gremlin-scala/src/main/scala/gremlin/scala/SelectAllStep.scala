package gremlin.scala

import org.apache.tinkerpop.gremlin.process.traversal.{Path, Traversal}
import org.apache.tinkerpop.gremlin.process.traversal.Traverser.Admin
import org.apache.tinkerpop.gremlin.process.traversal.step.TraversalParent
import org.apache.tinkerpop.gremlin.process.traversal.step.map.ScalarMapStep
import org.apache.tinkerpop.gremlin.process.traversal.traverser.TraverserRequirement

import scala.collection.JavaConverters._
import shapeless._
import shapeless.ops.hlist._

class SelectAllStep[S, Labels <: HList, LabelsTuple](traversal: Traversal[_, _])(
    implicit tupler: Tupler.Aux[Labels, LabelsTuple])
    extends ScalarMapStep[S, LabelsTuple](traversal.asAdmin)
    with TraversalParent {

  override def getRequirements = Set(TraverserRequirement.PATH).asJava

  protected def map(traverser: Admin[S]): LabelsTuple = {
    val labels: Labels = toHList(toList(traverser.path))
    tupler(labels)
  }

  def toList(path: Path): List[Any] = {
    val labels = path.labels
    def hasUserLabel(i: Int) = !labels.get(i).isEmpty

    (0 until path.size).filter(hasUserLabel).map(path.get[Any]).toList
  }

  private def toHList[T <: HList](path: List[_]): T =
    if (path.isEmpty)
      HNil.asInstanceOf[T]
    else
      (path.head :: toHList[IsHCons[T]#T](path.tail)).asInstanceOf[T]
}
