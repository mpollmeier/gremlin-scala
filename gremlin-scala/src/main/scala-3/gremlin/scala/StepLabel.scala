package gremlin.scala

import java.util.UUID.randomUUID
import java.util.{Map => JMap}
import scala.annotation.implicitNotFound


// type safety for labelled steps
case class StepLabel[A](name: String = randomUUID.toString) {
  def getLabelValueFromMap(values: JMap[String, Any]): A = values.get(name).asInstanceOf[A]
}

object StepLabel {

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
    given forSingle[A]: ExtractLabelType[StepLabel[A]] =
      new ExtractLabelType[StepLabel[A]] { type Out = A }

    given forEmptyTuple: ExtractLabelType[EmptyTuple] =
      new ExtractLabelType[EmptyTuple] { type Out = EmptyTuple }

    given forTuple[H, T <: Tuple, HOut, TOut <: Tuple](
      using
      hExtractLabelType: ExtractLabelType.Aux[H, HOut],
      tExtractLabelType: ExtractLabelType.Aux[T, TOut]
    ): ExtractLabelType.Aux[H *: T, HOut *: TOut] =
      new ExtractLabelType[H *: T] { type Out = HOut *: TOut }
  }
}
