package gremlin.scala.dsl

import gremlin.scala._

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

  given def forUnit: Converter.Identity[Unit] = identityConverter[Unit]
  given val forString: Converter.Identity[Label] = identityConverter[String]
  given val forInt: Converter.Identity[Int] = identityConverter[Int]
  given val forLong: Converter.Identity[Long] = identityConverter[Long]
  given val forDouble: Converter.Identity[Double] = identityConverter[Double]
  given val forFloat: Converter.Identity[Float] = identityConverter[Float]
  given val forBoolean: Converter.Identity[Boolean] = identityConverter[Boolean]
  given val forInteger: Converter.Identity[Integer] = identityConverter[Integer]
  given val forJLong: Converter.Identity[java.lang.Long] = identityConverter[java.lang.Long]
  given val forJDouble: Converter.Identity[java.lang.Double] = identityConverter[java.lang.Double]
  given val forJFloat: Converter.Identity[java.lang.Float] = identityConverter[java.lang.Float]
  given val forJBoolean: Converter.Identity[java.lang.Boolean] = identityConverter[java.lang.Boolean]

  def identityConverter[A]: Converter.Identity[A] = new Converter[A] {
    type GraphType = A
    def toGraph(value: A): A = value
    def toDomain(value: A): A = value
  }

  given forDomainNode[DomainType <: DomainRoot](
    using marshaller: Marshallable[DomainType],
    graph: Graph
  ): Converter.Aux[DomainType, Vertex] = new Converter[DomainType] {
    type GraphType = Vertex
    def toDomain(v: Vertex): DomainType = marshaller.toCC(v)
    def toGraph(dt: DomainType): Vertex = AnonymousVertex(dt)
  }

  given forList[A, AGraphType](
    using aConverter: Converter.Aux[A, AGraphType]
  ): Converter.Aux[List[A], List[AGraphType]] =
    new Converter[List[A]] {
      type GraphType = List[AGraphType]
      def toDomain(aGraphs: List[AGraphType]): List[A] =
        aGraphs.map(aConverter.toDomain)
      def toGraph(as: List[A]): List[AGraphType] = as.map(aConverter.toGraph)
    }

  given forSet[A, AGraphType](
    using aConverter: Converter.Aux[A, AGraphType]
  ): Converter.Aux[Set[A], Set[AGraphType]] =
    new Converter[Set[A]] {
      type GraphType = Set[AGraphType]
      def toDomain(aGraphs: Set[AGraphType]): Set[A] =
        aGraphs.map(aConverter.toDomain)
      def toGraph(as: Set[A]): Set[AGraphType] = as.map(aConverter.toGraph)
    }

  given forHNil: Converter.Aux[EmptyTuple, EmptyTuple] = new Converter[EmptyTuple] {
    type GraphType = EmptyTuple
    def toGraph(value: EmptyTuple) = EmptyTuple
    def toDomain(value: GraphType) = EmptyTuple
  }

  given forHList[H, HGraphType, T <: Tuple, TGraphType <: Tuple](
      using
      hConverter: Converter.Aux[H, HGraphType],
      tConverter: Converter.Aux[T, TGraphType]
  ): Converter.Aux[H *: T, HGraphType *: TGraphType] =
    new Converter[H *: T] {
      type GraphType = HGraphType *: TGraphType

      def toGraph(values: H *: T): GraphType = values match {
        case h :: t => hConverter.toGraph(h) :: tConverter.toGraph(t)
      }

      def toDomain(values: GraphType): H *: T = values match {
        case h :: t => hConverter.toDomain(h) :: tConverter.toDomain(t)
      }
    }
}

trait LowestPriorityConverterImplicits {
  // for all Products, e.g. tuples, case classes etc
  given forGeneric[T, Repr <: Tuple, GraphType <: Tuple, GraphTypeTuple <: Product](
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
