package gremlin.scala.dsl

import gremlin.scala._
import shapeless._
import shapeless.ops.hlist.Tupler
import shapeless.ops.product.ToHList

trait Converter[DomainType] {
  type GraphType
  def toGraph(domainType: DomainType): GraphType
  def toDomain(graphType: GraphType): DomainType
}

object Converter extends LowPriorityConverterImplicits {
  type Aux[DomainType, Out0] = Converter[DomainType] { type GraphType = Out0 }
  type Identity[T] = Aux[T, T]
}

trait LowPriorityConverterImplicits extends LowestPriorityConverterImplicits {
  /* need to explicitly create these for the base types, otherwise it there would
   * be ambiguous implicits (given Converter.forDomainNode) */

  implicit def forUnit: Converter.Identity[Unit] = identityConverter[Unit]
  implicit val forString: Converter.Identity[Label] = identityConverter[String]
  implicit val forInt: Converter.Identity[Int] = identityConverter[Int]
  implicit val forLong: Converter.Identity[Long] = identityConverter[Long]
  implicit val forDouble: Converter.Identity[Double] = identityConverter[Double]
  implicit val forFloat: Converter.Identity[Float] = identityConverter[Float]
  implicit val forBoolean: Converter.Identity[Boolean] = identityConverter[Boolean]
  implicit val forInteger: Converter.Identity[Integer] = identityConverter[Integer]
  implicit val forJLong: Converter.Identity[java.lang.Long] = identityConverter[java.lang.Long]
  implicit val forJDouble: Converter.Identity[java.lang.Double] = identityConverter[java.lang.Double]
  implicit val forJFloat: Converter.Identity[java.lang.Float] = identityConverter[java.lang.Float]
  implicit val forJBoolean: Converter.Identity[java.lang.Boolean] = identityConverter[java.lang.Boolean]
  def identityConverter[A]: Converter.Identity[A] = new Converter[A] {
    type GraphType = A
    def toGraph(value: A): A = value
    def toDomain(value: A): A = value
  }

  implicit def forDomainNode[DomainType <: DomainRoot](
      implicit marshaller: Marshallable[DomainType],
      graph: Graph
  ): Converter.Aux[DomainType, Vertex] = new Converter[DomainType] {
    type GraphType = Vertex
    def toDomain(v: Vertex): DomainType = marshaller.toCC(v)
    def toGraph(dt: DomainType): Vertex = AnonymousVertex(dt)
  }

  implicit def forList[A, AGraphType](
    implicit aConverter: Converter.Aux[A, AGraphType]
  ): Converter.Aux[List[A], List[AGraphType]] =
    new Converter[List[A]] {
      type GraphType = List[AGraphType]
      def toDomain(aGraphs: List[AGraphType]): List[A] =
        aGraphs.map(aConverter.toDomain)
      def toGraph(as: List[A]): List[AGraphType] = as.map(aConverter.toGraph)
    }

  implicit def forSet[A, AGraphType](
    implicit aConverter: Converter.Aux[A, AGraphType]
  ): Converter.Aux[Set[A], Set[AGraphType]] =
    new Converter[Set[A]] {
      type GraphType = Set[AGraphType]
      def toDomain(aGraphs: Set[AGraphType]): Set[A] =
        aGraphs.map(aConverter.toDomain)
      def toGraph(as: Set[A]): Set[AGraphType] = as.map(aConverter.toGraph)
    }

  implicit val forHNil: Converter.Aux[HNil, HNil] = new Converter[HNil] {
    type GraphType = HNil
    def toGraph(value: HNil) = HNil
    def toDomain(value: GraphType) = HNil
  }

  implicit def forHList[H, HGraphType, T <: HList, TGraphType <: HList](
      implicit
      hConverter: Converter.Aux[H, HGraphType],
      tConverter: Converter.Aux[T, TGraphType]
  ): Converter.Aux[H :: T, HGraphType :: TGraphType] =
    new Converter[H :: T] {
      type GraphType = HGraphType :: TGraphType

      def toGraph(values: H :: T): GraphType = values match {
        case h :: t => hConverter.toGraph(h) :: tConverter.toGraph(t)
      }

      def toDomain(values: GraphType): H :: T = values match {
        case h :: t => hConverter.toDomain(h) :: tConverter.toDomain(t)
      }
    }
}

trait LowestPriorityConverterImplicits {
  // for all Products, e.g. tuples, case classes etc
  implicit def forGeneric[T, Repr <: HList, GraphType <: HList, GraphTypeTuple <: Product](
      implicit
      gen: Generic.Aux[T, Repr],
      converter: Converter.Aux[Repr, GraphType],
      tupler: Tupler.Aux[GraphType, GraphTypeTuple],
      toHList: ToHList.Aux[GraphTypeTuple, GraphType]
  ): Converter.Aux[T, GraphTypeTuple] =
    new Converter[T] {
      type GraphType = GraphTypeTuple

      def toGraph(value: T): GraphTypeTuple =
        tupler(converter.toGraph(gen.to(value)))

      def toDomain(value: GraphTypeTuple): T =
        gen.from(converter.toDomain(toHList(value)))
    }
}
