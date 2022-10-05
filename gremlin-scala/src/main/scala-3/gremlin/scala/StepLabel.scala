package gremlin.scala

import java.util.UUID.randomUUID
import java.util.{Map => JMap}
import scala.annotation.implicitNotFound
import shapeless.{HList, HNil, Poly2}
import shapeless.poly._

// type safety for labelled steps
case class StepLabel[A](name: String = randomUUID.toString)

object StepLabel {

  object GetLabelName extends (StepLabel ~>> String) {
    def apply[B](label: StepLabel[B]) = label.name
  }

  object combineLabelWithValue extends Poly2 {
    implicit def atLabel[A, L <: HList] =
      at[StepLabel[A], (L, JMap[String, Any])] {
        case (label, (acc, values)) =>
          (values.get(label.name).asInstanceOf[A] :: acc, values)
      }
  }

  trait ExtractLabelType[A] {
    type Out
  }

  object ExtractLabelType extends LowPriorityExtractLabelTypeImplicits {
    @implicitNotFound(
      "Unable to find implicit for extracting LabelType of StepLabel `${A}`. "
        + "We probably need to add an implicit def to `LowPriorityExtractLabelTypeImplicits`")
    type Aux[A, Out0] = ExtractLabelType[A] { type Out = Out0 }
  }

  trait LowPriorityExtractLabelTypeImplicits {
    implicit def forSingle[A] = new ExtractLabelType[StepLabel[A]] {
      type Out = A
    }

    implicit def forHNil = new ExtractLabelType[HNil] { type Out = HNil }

    implicit def forHList[H, T <: HList, HOut, TOut <: HList](
        implicit hExtractLabelType: ExtractLabelType.Aux[H, HOut],
        tExtractLabelType: ExtractLabelType.Aux[T, TOut])
      : ExtractLabelType.Aux[H :: T, HOut :: TOut] =
      new ExtractLabelType[H :: T] { type Out = HOut :: TOut }
  }
}
