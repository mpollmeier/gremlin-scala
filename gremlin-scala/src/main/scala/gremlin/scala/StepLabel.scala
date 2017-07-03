package gremlin.scala

import java.util.UUID.randomUUID
import java.util.{Map ⇒ JMap}
import scala.annotation.implicitNotFound
import shapeless._
import shapeless.poly._

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

  trait ExtractLabelType[A] {
    type Out
  }

  object ExtractLabelType extends LowPriorityExtractLabelTypeImplicits {
    @implicitNotFound("Unable to find implicit for extracting LabelType of StepLabel `${A}`. "
     + "We probably need to add an implicit def to `LowPriorityExtractLabelTypeImplicits`")
    type Aux[A, Out0] = ExtractLabelType[A] { type Out = Out0 }
  }

  trait LowPriorityExtractLabelTypeImplicits {
    implicit def forSingle[A] = new ExtractLabelType[StepLabel[A]] { type Out = A }

    implicit def forHNil = new ExtractLabelType[HNil] { type Out = HNil }

    implicit def forHList[H, T <: HList, HOut, TOut <: HList](
      implicit hExtractLabelType: ExtractLabelType.Aux[H, HOut],
      tExtractLabelType: ExtractLabelType.Aux[T, TOut]): ExtractLabelType.Aux[H :: T, HOut :: TOut] =
      new ExtractLabelType[H :: T] { type Out = HOut :: TOut }
  }

  import gremlin.scala.dsl._
  trait ToGraph[A] {
    type Out
    def apply(a: A): Out
  }

  object ToGraph extends LowPriorityToGraphImplicits {
    // @implicitNotFound("Unable to find implicit for converting type StepLabel[${A}] into a StepLabel for the corresponding graph type. "
    //  +"We probably need to add an implicit def to `LowPriorityToGraphImplicits`")
    type Aux[A, Out0] = ToGraph[A] { type Out = Out0 }
  }

  trait LowPriorityToGraphImplicits {
    implicit def forSingle[DomainType, GraphType](
      implicit conv: Converter.Aux[DomainType, GraphType]) = new ToGraph[StepLabel[DomainType]] {
      type Out = StepLabel[GraphType]
      def apply(label: StepLabel[DomainType]) = StepLabel[GraphType](label.name)
    }

    implicit def forHNil = new ToGraph[HNil] {
      type Out = HNil
      def apply(labels: HNil) = HNil
    }

    implicit def forHList[HDomainType, HGraphType, T <: HList, TOut <: HList]
    //   implicit hConv: Converter.Aux[HDomainType, HGraphType],
    : ToGraph.Aux[StepLabel[HDomainType] :: T, StepLabel[HGraphType] :: TOut] = ???

    // implicit def forHList[H, T <: HList, HOut, TOut <: HList](
    //   implicit hToGraph: ToGraph.Aux[H, HOut],
    //   tToGraph: ToGraph.Aux[T, TOut]
    //   // hConv: Converter.Aux[H, HOut],
    //   // tConv: Converter.Aux[T, TOut]
    // ): ToGraph.Aux[StepLabel[H :: T, HOut :: TOut] =
    //   new ToGraph[H :: T] {
    //     type Out = HOut :: TOut
    //     def apply(labels: H :: T): HOut :: TOut = ???
    //   }

    // implicit def forHList[H, T <: HList, HOut, TOut <: HList](
    //   implicit hToGraph: ToGraph.Aux[H, HOut],
    //   tToGraph: ToGraph.Aux[T, TOut]
    //   // hConv: Converter.Aux[H, HOut],
    //   // tConv: Converter.Aux[T, TOut]
    // ): ToGraph.Aux[H :: T, HOut :: TOut] =
    //   new ToGraph[H :: T] {
    //     type Out = HOut :: TOut
    //     def apply(labels: H :: T): HOut :: TOut = ???
    //   }
  }

  trait Wrap[A] {
    type Out
  }

  object Wrap extends LowPriorityWrapImplicits {
    @implicitNotFound("Unable to find implicit for wrapping type `${A}` into a StepLabel. We probably"
     + " need to add an implicit def to `LowPriorityWrapImplicits`")
    type Aux[A, Out0] = Wrap[A] { type Out = Out0 }
  }

  trait LowPriorityWrapImplicits {
    implicit def forSingle[A] = new Wrap[A] { type Out = StepLabel[A] }

    implicit def forHNil = new Wrap[HNil] { type Out = HNil }

    implicit def forHList[H, T <: HList, HOut, TOut <: HList](
      implicit hWrap: Wrap.Aux[H, HOut], tWrap: Wrap.Aux[T, TOut]): Wrap.Aux[H :: T, HOut :: TOut] =
      new Wrap[H :: T] { type Out = HOut :: TOut }
  }
}
