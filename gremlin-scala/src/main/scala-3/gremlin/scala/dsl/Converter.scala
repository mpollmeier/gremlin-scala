package gremlin.scala.dsl

import gremlin.scala.*

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

  inline given forUnit: Converter.Identity[Unit] = identityConverter[Unit]
  inline given forString: Converter.Identity[Label] = identityConverter[String]
  inline given forInt: Converter.Identity[Int] = identityConverter[Int]
  inline given forLong: Converter.Identity[Long] = identityConverter[Long]
  inline given forDouble: Converter.Identity[Double] = identityConverter[Double]
  inline given forFloat: Converter.Identity[Float] = identityConverter[Float]
  inline given forBoolean: Converter.Identity[Boolean] = identityConverter[Boolean]
  inline given forInteger: Converter.Identity[Integer] = identityConverter[Integer]
  inline given forJLong: Converter.Identity[java.lang.Long] = identityConverter[java.lang.Long]
  inline given forJDouble: Converter.Identity[java.lang.Double] = identityConverter[java.lang.Double]
  inline given forJFloat: Converter.Identity[java.lang.Float] = identityConverter[java.lang.Float]
  inline given forJBoolean: Converter.Identity[java.lang.Boolean] = identityConverter[java.lang.Boolean]

  inline def identityConverter[A]: Converter.Identity[A] = new Converter[A] {
    type GraphType = A
    def toGraph(value: A): A = value
    def toDomain(value: A): A = value
  }

  inline given forDomainNode[DomainType <: DomainRoot](
    using
    marshaller: Marshallable[DomainType],
    graph: Graph
  ): Converter.Aux[DomainType, Vertex] = new Converter[DomainType] {
    type GraphType = Vertex
    def toDomain(v: Vertex): DomainType = marshaller.toCC(v)
    def toGraph(dt: DomainType): Vertex = AnonymousVertex(dt)
  }

  inline given forList[A, AGraphType](
    using aConverter: Converter.Aux[A, AGraphType]
  ): Converter.Aux[List[A], List[AGraphType]] =
    new Converter[List[A]] {
      type GraphType = List[AGraphType]
      def toDomain(aGraphs: List[AGraphType]): List[A] =
        aGraphs.map(aConverter.toDomain)
      def toGraph(as: List[A]): List[AGraphType] = as.map(aConverter.toGraph)
    }

  inline given forSet[A, AGraphType](
    using aConverter: Converter.Aux[A, AGraphType]
  ): Converter.Aux[Set[A], Set[AGraphType]] =
    new Converter[Set[A]] {
      type GraphType = Set[AGraphType]
      def toDomain(aGraphs: Set[AGraphType]): Set[A] =
        aGraphs.map(aConverter.toDomain)
      def toGraph(as: Set[A]): Set[AGraphType] = as.map(aConverter.toGraph)
    }

  inline given forEmptyTuple: Converter.Aux[EmptyTuple, EmptyTuple] = new Converter[EmptyTuple] {
    type GraphType = EmptyTuple
    def toGraph(value: EmptyTuple) = EmptyTuple
    def toDomain(value: GraphType) = EmptyTuple
  }

  inline given forTuples[H, T <: Tuple, HGraphType, TGraphType <: Tuple](
    using
    hConverter: Converter.Aux[H, HGraphType],
    tConverter: Converter.Aux[T, TGraphType]
  ): Converter.Aux[H *: T, HGraphType *: TGraphType] =
    new Converter[H *: T] {
      type GraphType = HGraphType *: TGraphType

      def toGraph(values: H *: T): GraphType = values match {
        case h *: t => hConverter.toGraph(h) *: tConverter.toGraph(t)
      }

      def toDomain(values: GraphType): H *: T = values match {
        case h *: t => hConverter.toDomain(h) *: tConverter.toDomain(t)
      }
    }
}

trait LowestPriorityConverterImplicits {
  // for all Products, e.g. tuples, case classes etc
  inline given forProduct[Prod <: Product, ProdGraphType <: Tuple](
    using m: scala.deriving.Mirror.ProductOf[Prod]
  )(
    using converter: Converter.Aux[m.MirroredElemTypes, ProdGraphType],
  ): Converter.Aux[Prod, ProdGraphType] =
    new Converter[Prod] {
      type GraphType = ProdGraphType

      def toGraph(value: Prod): GraphType =
        converter.toGraph(Tuple.fromProductTyped(value))

      def toDomain(value: GraphType): Prod =
        m.fromProduct(converter.toDomain(value))
    }
}
