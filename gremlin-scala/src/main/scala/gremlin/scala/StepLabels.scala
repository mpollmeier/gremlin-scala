package gremlin.scala

import shapeless.poly._

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
