package gremlin.scala

import java.util.UUID.randomUUID
import java.util.{Map => JMap}
import scala.annotation.implicitNotFound


// type safety for labelled steps
case class StepLabel[A](name: String = randomUUID.toString)

object StepLabel {

  inline def extractLabelNames[T <: Tuple]
    (inline tup: T)
  : List[String] =
    inline tup match {
      case EmptyTuple => Nil
      case (h: StepLabel[_]) *: t =>
        h.name :: extractLabelNames(t)
    }

  inline def extractValues[T <: Tuple](inline tup: T, values: JMap[String, Any]): TypesFromStepLabels[T] =
    untypedExtractValues[T](tup, values).asInstanceOf[TypesFromStepLabels[T]]

  private inline def untypedExtractValues[T <: Tuple](inline tup: T, values: JMap[String, Any]): Tuple =
    inline tup match {
      case EmptyTuple => EmptyTuple
      case (h: StepLabel[a]) *: t =>
        values.get(h.name).asInstanceOf[a] *: untypedExtractValues(t, values)
    }

  type TypesFromStepLabels[Tup <: Tuple] = Tup match {
    case EmptyTuple => EmptyTuple
    case StepLabel[a] *: tail => a *: TypesFromStepLabels[tail]
  }
}
