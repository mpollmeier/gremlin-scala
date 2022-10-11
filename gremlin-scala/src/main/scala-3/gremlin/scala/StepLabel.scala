package gremlin.scala

import java.util.UUID.randomUUID
import java.util.{Map => JMap}
import scala.annotation.implicitNotFound


// type safety for labelled steps
case class StepLabel[A](name: String = randomUUID.toString) {
  def getLabelValueFromMap(values: JMap[String, Any]): A = values.get(name).asInstanceOf[A]
}

object StepLabel {

  inline def extractLabelNames[T <: NonEmptyTuple](inline tup: T): List[String] =
    inline tup match {
      case (h: StepLabel[_]) *: EmptyTuple => h.name :: Nil
      case (h: StepLabel[_]) *: (t: NonEmptyTuple) => h.name :: extractLabelNames(t)
      case _ => compiletime.error("Not a tuple of StepLabels")
    }

  inline transparent def extractValues[T <: Tuple](inline tup: T, values: JMap[String, Any]): Tuple =
    inline tup match {
      case (h: StepLabel[a]) *: t => values.get(h.name).asInstanceOf[a] *: extractValues(t, values)
      case EmptyTuple => EmptyTuple
      case _ => compiletime.error("Not a tuple of StepLabels")
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
