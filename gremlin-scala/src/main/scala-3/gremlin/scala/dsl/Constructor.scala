package gremlin.scala.dsl

import gremlin.scala._
import scala.util.NotGiven

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

  inline given forSimpleType[A, Labels <: Tuple](using Converter.Aux[A, A]): Constructor[A, Labels] =
    new Constructor[A, Labels] {
      type GraphType = A
      type StepsType = Steps[A, A, Labels]
      def apply(raw: GremlinScala[GraphType]) = Steps[A, A, Labels](raw)
    }

  inline def forDomainNode[
    DomainType <: DomainRoot,
    Labels <: Tuple,
    StepsTypeOut <: NodeSteps[DomainType, Labels]
  ](constr: GremlinScala[Vertex] => StepsTypeOut) =
    new Constructor[DomainType, Labels] {
      type GraphType = Vertex
      type StepsType = StepsTypeOut

      def apply(raw: GremlinScala[GraphType]): StepsTypeOut = constr(raw)
    }

  inline given forList[A, AGraphType, Labels <: Tuple](
    using
    Tuple.Union[Labels] <:< StepLabel[_],
    Converter.Aux[A, AGraphType]
  ): Constructor[List[A], Labels] =
    new Constructor[List[A], Labels] {
      type GraphType = List[AGraphType]
      type StepsType = Steps[List[A], List[AGraphType], Labels]
      def apply(raw: GremlinScala[GraphType]) = Steps[List[A], List[AGraphType], Labels](raw)
    }

  inline given forSet[A, AGraphType, Labels <: Tuple](
    using
    Tuple.Union[Labels] <:< StepLabel[_],
    Converter.Aux[A, AGraphType]
  ): Constructor[Set[A], Labels] =
    new Constructor[Set[A], Labels] {
      type GraphType = Set[AGraphType]
      type StepsType = Steps[Set[A], Set[AGraphType], Labels]
      def apply(raw: GremlinScala[GraphType]) = Steps[Set[A], Set[AGraphType], Labels](raw)
    }

  inline given forEmptyTuple: Constructor[EmptyTuple, EmptyTuple] =
    new Constructor[EmptyTuple, EmptyTuple] {
      type GraphType = EmptyTuple
      type StepsType = Steps[EmptyTuple, EmptyTuple, EmptyTuple]
      def apply(raw: GremlinScala[EmptyTuple]) = new Steps[EmptyTuple, EmptyTuple, EmptyTuple](raw)
    }

  inline given forTuple[
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
    Converter.Aux[H *: T, HGraphType *: TGraphType],
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

//  inline given forTuple[
//    Tup <: Tuple,
//    GraphType <: Tuple,
//    Labels <: Tuple,
//  ](using
//    Converter.Aux[Tup, GraphType],
//  ): Constructor.Aux[
//    Tup,
//    Labels,
//    GraphType,
//    Steps[Tup, GraphType, Labels]
//  ] =
//    new Constructor[Tup, Labels] {
//      type GraphType = GraphType
//      type StepsType = Steps[Tup, GraphType, Labels]
//
//      def apply(raw: GremlinScala[GraphType]): StepsType =
//        new Steps[Tup, GraphType, Labels](raw)
//    }
}

trait LowestPriorityConstructorImplicits {

  // for all Products, e.g. tuples, case classes etc
  inline given forProduct[Prod <: Product, Labels <: Tuple]
    (using NotGiven[Prod <:< Tuple])
    (using m: scala.deriving.Mirror.ProductOf[Prod])
    (using cons: Constructor[m.MirroredElemTypes, Labels])
    (using cons.StepsType <:< StepsRoot)
    (using Converter.Aux[Prod, cons.GraphType])
  : Constructor[Prod, Labels] =
    new Constructor[Prod, Labels] {
      type GraphType = cons.GraphType
      type StepsType = Steps[Prod, GraphType, Labels]
      def apply(raw: GremlinScala[GraphType]): StepsType = new Steps[Prod, GraphType, Labels](raw)
    }
}
