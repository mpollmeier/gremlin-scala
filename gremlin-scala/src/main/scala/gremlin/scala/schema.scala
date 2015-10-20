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
  import schema._

  case class StepLabel[A](name: String)
  case class StepLabelWithValue[A](label: StepLabel[A], value: A)

  object GetLabelName extends (StepLabel ~> Const[String]#λ) {
    def apply[B](label: StepLabel[B]) = label.name
  }

  // object ToString extends Poly1 {
  //   implicit def apply[Z] = at[StepLabel[Z]](_.name)
  // }
  // object GetLabelValue extends (StepLabelWithValue ~> Id) {
  //   def apply[B](label: StepLabelWithValue[B]) = label.value
  // }
}
