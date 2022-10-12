package gremlin.scala

import org.apache.tinkerpop.gremlin.process.traversal.{Path, Traversal}
import org.apache.tinkerpop.gremlin.process.traversal.Traverser.Admin
import org.apache.tinkerpop.gremlin.process.traversal.step.TraversalParent
import org.apache.tinkerpop.gremlin.process.traversal.step.map.ScalarMapStep
import org.apache.tinkerpop.gremlin.process.traversal.step.util.AbstractStep
import org.apache.tinkerpop.gremlin.process.traversal.traverser.TraverserRequirement

import scala.jdk.CollectionConverters.*
import scala.compiletime.erasedValue
import java.util

class SelectAllStep[S, Labels <: Tuple](traversal: Traversal[_, _])
  extends ScalarMapStep[S, Labels](traversal.asAdmin)
  with TraversalParent
{
  override def clone(): SelectAllStep[S, Labels] = new SelectAllStep[S, Labels](traversal)

  override def getRequirements: util.Set[TraverserRequirement] = Set(TraverserRequirement.PATH).asJava

  protected def map(traverser: Admin[S]): Labels =
    toTuple[Labels](toList(traverser.path))

  def toList(path: Path): List[Any] = {
    val labels = path.labels
    def hasUserLabel(i: Int) = !labels.get(i).isEmpty

    (0 until path.size).filter(hasUserLabel).map(path.get[Any]).toList
  }

  inline private def toTuple[T <: Tuple](path: List[?]): T =
    Tuple.fromArray(path.toArray).asInstanceOf[T]
//    inline erasedValue[T] match
//      case _: EmptyTuple => EmptyTuple.asInstanceOf[T]
//      case _: (th *: tt) =>
//        inline path match
//          case Nil => sys.error("Ran out of values too soon") // EmptyTuple.asInstanceOf[T] //TODO: this should probably error
//          case h :: t => (h.asInstanceOf[th] *: toTuple[tt](t)).asInstanceOf[T]

}
