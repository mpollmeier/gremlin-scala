package gremlin.scala

import java.util.UUID.randomUUID
import java.util.{Map => JMap}
import scala.annotation.implicitNotFound


// type safety for labelled steps
case class StepLabel[A](name: String = randomUUID.toString)

object StepLabel {

  inline def extractLabelNames[T <: Tuple]
    (inline tup: T)
    (using Tuple.Union[T] <:< StepLabel[_])
  : List[String] =
    inline tup match {
      case EmptyTuple => Nil
      case (h: StepLabel[_]) *: t =>
        h.name :: extractLabelNames(t)
    }

  inline transparent def extractValues[T <: Tuple]
    (inline tup: T, values: JMap[String, Any])
  : Tuple =
    inline tup match {
      case EmptyTuple => EmptyTuple
      case (h: StepLabel[a]) *: t =>
        values.get(h.name).asInstanceOf[a] *: extractValues(t, values)
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
    given [A]: ExtractLabelType[StepLabel[A]] = new ExtractLabelType[StepLabel[A]] { type Out = A }

    given ExtractLabelType[EmptyTuple] = new ExtractLabelType[EmptyTuple] { type Out = EmptyTuple }

    given [H, T <: Tuple, HOut, TOut <: Tuple]
      (using ExtractLabelType.Aux[H, HOut], ExtractLabelType.Aux[T, TOut])
    : ExtractLabelType.Aux[H *: T, HOut *: TOut] =
      new ExtractLabelType[H *: T] { type Out = HOut *: TOut }
  }
}
