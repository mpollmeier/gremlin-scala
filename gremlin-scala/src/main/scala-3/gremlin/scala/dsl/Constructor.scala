package gremlin.scala.dsl

import gremlin.scala._

trait Constructor[DomainType, Labels <: Tuple] {
  type GraphType
  type StepsType
  def apply(raw: GremlinScala[GraphType]): StepsType
}

object Constructor extends LowPriorityConstructorImplicits {
  type Aux[DomainType, Labels <: Tuple, GraphTypeOut, StepsTypeOut] =
    Constructor[DomainType, Labels] {
      type GraphType = GraphTypeOut
      type StepsType = StepsTypeOut
    }
}

trait LowPriorityConstructorImplicits extends LowestPriorityConstructorImplicits {

  given forSimpleType[A, Labels <: Tuple](
    using converter: Converter.Aux[A, A]
  ): Constructor[A, Labels] =
    new Constructor[A, Labels] {
      type GraphType = A
      type StepsType = Steps[A, A, Labels]
      def apply(raw: GremlinScala[GraphType]) = Steps[A, A, Labels](raw)
    }

  def forDomainNode[
    DomainType <: DomainRoot,
    Labels <: Tuple,
    StepsTypeOut <: NodeSteps[DomainType, Labels]
  ](constr: GremlinScala[Vertex] => StepsTypeOut) =
    new Constructor[DomainType, Labels] {
      type GraphType = Vertex
      type StepsType = StepsTypeOut

      def apply(raw: GremlinScala[GraphType]): StepsTypeOut = constr(raw)
    }

  given forList[A, AGraphType, Labels <: Tuple](
    using aConverter: Converter.Aux[A, AGraphType]
  ): Constructor[List[A], Labels] =
    new Constructor[List[A], Labels] {
      type GraphType = List[AGraphType]
      type StepsType = Steps[List[A], List[AGraphType], Labels]
      def apply(raw: GremlinScala[GraphType]) = Steps[List[A], List[AGraphType], Labels](raw)
    }

  given forSet[A, AGraphType, Labels <: Tuple](
    using aConverter: Converter.Aux[A, AGraphType]
  ): Constructor[Set[A], Labels] =
    new Constructor[Set[A], Labels] {
      type GraphType = Set[AGraphType]
      type StepsType = Steps[Set[A], Set[AGraphType], Labels]
      def apply(raw: GremlinScala[GraphType]) = Steps[Set[A], Set[AGraphType], Labels](raw)
    }

  given forEmptyTuple: Constructor[EmptyTuple, EmptyTuple] =
    new Constructor[EmptyTuple, EmptyTuple] {
      type GraphType = EmptyTuple
      type StepsType = Steps[EmptyTuple, EmptyTuple, EmptyTuple]
      def apply(raw: GremlinScala[EmptyTuple]) = new Steps[EmptyTuple, EmptyTuple, EmptyTuple](raw)
    }

  given forTuple[
    H,
    T <: Tuple,
    HGraphType,
    TGraphType <: Tuple,
    Labels <: Tuple,
    HStepsType,
    TStepsType
  ](using
    Constructor.Aux[H, Labels, HGraphType, HStepsType],
    Constructor.Aux[T, Labels, TGraphType, TStepsType],
    Converter.Aux[H *: T, HGraphType *: TGraphType]
  ): Constructor.Aux[
    H *: T,
    Labels,
    HGraphType *: TGraphType,
    Steps[H *: T, HGraphType *: TGraphType, Labels]
  ] =
    new Constructor[H *: T, Labels] {
      type GraphType = HGraphType *: TGraphType
      type StepsType = Steps[H *: T, HGraphType *: TGraphType, Labels]
      def apply(raw: GremlinScala[GraphType]): StepsType =
        new Steps[H *: T, HGraphType *: TGraphType, Labels](raw)
    }
}

trait LowestPriorityConstructorImplicits {
  // for all Products, e.g. tuples, case classes etc
  given forGeneric[
    T <: Tuple,
    GraphTypeTuple <: Tuple,
    Labels <: Tuple,
    StepsType0 <: StepsRoot,
    EndDomainTuple <: Tuple
  ](using
    constr: Constructor.Aux[T, Labels, GraphTypeTuple, StepsType0],
    eq: StepsType0#EndDomain0 =:= EndDomainTuple,
    converter: Converter.Aux[T, GraphTypeTuple]
  ): Constructor[T, Labels] =
    new Constructor[T, Labels] {
      type GraphType = GraphTypeTuple
      type StepsType = Steps[T, GraphType, Labels]
      def apply(raw: GremlinScala[GraphType]): StepsType = new Steps[T, GraphType, Labels](raw)
    }
}
