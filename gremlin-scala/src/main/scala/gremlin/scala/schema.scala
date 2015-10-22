package gremlin.scala

import org.apache.tinkerpop.gremlin.structure._

import scala.language.implicitConversions
import shapeless.poly._

object schema {
  /**
    * Smaller than an Element, consisting of the particles used
    * compose a Property (or VertexProperty) of an Element (Edge or Vertex)
    */
  sealed abstract class Atom[A](val key: String) {
    def apply(n: A): (String, A) = key → n
  }

  case class Key[A](override val key: String) extends Atom[A](key)

  object Label extends Atom[String](T.label.name)

  object ID extends Atom[Any](T.id.name)

  implicit class AtomValue[A](p: (String, A)) {
    def key: String = p._1
    def value: A = p._2
  }

}

// type safety for labelled steps
object StepLabels {
  import shapeless._
  import java.util.UUID.randomUUID
  import java.util.{Map ⇒ JMap}

  case class StepLabel[A](name: String = randomUUID.toString)

  object GetLabelName extends (StepLabel ~>> String) {
    def apply[B](label: StepLabel[B]) = label.name
  }

  object combineLabelWithValue extends Poly2 {
    implicit def atLabel[A, L <: HList] = at[StepLabel[A], (L, JMap[String, Any])] {
      case (label, (acc, values)) ⇒
        (values.get(label.name).asInstanceOf[A] :: acc, values)
    }
  }
}
