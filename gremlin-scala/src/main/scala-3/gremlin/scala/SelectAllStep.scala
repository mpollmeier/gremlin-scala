package gremlin.scala

import org.apache.tinkerpop.gremlin.process.traversal.{Path, Traversal}
import org.apache.tinkerpop.gremlin.process.traversal.Traverser.Admin
import org.apache.tinkerpop.gremlin.process.traversal.step.TraversalParent
import org.apache.tinkerpop.gremlin.process.traversal.step.map.ScalarMapStep
import org.apache.tinkerpop.gremlin.process.traversal.traverser.TraverserRequirement

import scala.jdk.CollectionConverters._
import scala.compiletime.erasedValue
import java.util

class SelectAllStep[S, Labels <: Tuple](traversal: Traversal[_, _])
  extends ScalarMapStep[S, Labels](traversal.asAdmin)
  with TraversalParent
{

  override def getRequirements: util.Set[TraverserRequirement] = Set(TraverserRequirement.PATH).asJava

  protected def map(traverser: Admin[S]): Labels =
    toTuple[Labels](toList(traverser.path))

  def toList(path: Path): List[Any] = {
    val labels = path.labels
    def hasUserLabel(i: Int) = !labels.get(i).isEmpty

    (0 until path.size).filter(hasUserLabel).map(path.get[Any]).toList
  }

  inline private def toTuple[T <: Tuple](path: List[_]): T =
    inline erasedValue[T] match
      case _: (th *: tt) =>
        inline path match
          case Nil => EmptyTuple.asInstanceOf[T] //TODO: this should probably error
          case (h: th) :: t => (h *: toTuple[tt](t)).asInstanceOf[T]
      case _: EmptyTuple => EmptyTuple.asInstanceOf[T]

}
