package gremlin.scala

import shapeless._
import shapeless.poly._
import java.util.UUID.randomUUID
import java.util.{Map ⇒ JMap}

// type safety for labelled steps
case class StepLabel[A](name: String = randomUUID.toString)

object StepLabel {

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
