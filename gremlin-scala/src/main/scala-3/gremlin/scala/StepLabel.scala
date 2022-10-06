package gremlin.scala

import java.util.UUID.randomUUID
import java.util.{Map => JMap}
import scala.annotation.implicitNotFound


// type safety for labelled steps
case class StepLabel[A](name: String = randomUUID.toString)

object StepLabel {

  val getLabelName: [B] => StepLabel[B] => String =
    [B] => (label: StepLabel[B]) => label.name

  type ValueMap = JMap[String, Any]

  val getLabelValueFromMap: [B] => (StepLabel[B], ValueMap) => B =
    [B] => (label: StepLabel[B], values: ValueMap) => values.get(label.name).asInstanceOf[B]

//  val combineLabelWithValue:
//    [A, L <: Tuple] => StepLabel[A] => L => ValueMap => (L, ValueMap) =
//    [A,L] =>
//
//    extends Poly2 {
//    implicit def atLabel[A, L <: Tuple] =
//      at[StepLabel[A], (L, JMap[String, Any])] {
//        case (label, (acc, values)) =>
//          (values.get(label.name).asInstanceOf[A] :: acc, values)
//      }


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
      ExtractLabelType[StepLabel[A]] { type Out = A }

    given forHNil: ExtractLabelType[EmptyTuple] =
      ExtractLabelType[EmptyTuple] { type Out = EmptyTuple }

    given forHList[
      H,
      T <: Tuple,
      HOut,
      TOut <: Tuple
    ](
      using
      hExtractLabelType: ExtractLabelType.Aux[H, HOut],
      tExtractLabelType: ExtractLabelType.Aux[T, TOut]
    ): ExtractLabelType.Aux[H *: T, HOut *: TOut] =
      ExtractLabelType[H *: T] { type Out = HOut *: TOut }
  }
}
