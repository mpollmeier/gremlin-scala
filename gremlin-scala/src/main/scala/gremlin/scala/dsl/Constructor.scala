package gremlin.scala.dsl

import gremlin.scala._
import shapeless._
import shapeless.ops.hlist.Tupler
import shapeless.ops.product.ToHList

trait Constructor[DomainType, Labels <: HList] {
  type GraphType
  type StepsType
  def apply(raw: GremlinScala[GraphType]): StepsType
}

object Constructor extends LowPriorityConstructorImplicits {
  type Aux[DomainType, Labels <: HList, GraphTypeOut, StepsTypeOut] = Constructor[DomainType, Labels] {
    type GraphType = GraphTypeOut
    type StepsType = StepsTypeOut
  }
}

trait LowPriorityConstructorImplicits extends LowestPriorityConstructorImplicits {

  implicit def forSimpleType[A, Labels <: HList](implicit converter: Converter.Aux[A, A]) =
    new Constructor[A, Labels] {
      type GraphType = A
      type StepsType = Steps[A, A, Labels]
      def apply(raw: GremlinScala[GraphType]) = new Steps[A, A, Labels](raw)
    }

  def forDomainNode[
    DomainType <: DomainRoot,
    Labels <: HList,
    StepsTypeOut <: NodeSteps[DomainType, Labels]](
    constr: GremlinScala[Vertex] => StepsTypeOut) = new Constructor[DomainType, Labels] {
    type GraphType = Vertex
    type StepsType = StepsTypeOut

    def apply(raw: GremlinScala[GraphType]): StepsTypeOut = constr(raw)
  }

  implicit def forList[
    A,
    AGraphType,
    Labels <: HList,
    AStepsType](implicit aConverter: Converter.Aux[A, AGraphType]) = new Constructor[List[A], Labels] {
    type GraphType = List[AGraphType]
    type StepsType = Steps[List[A], List[AGraphType], Labels]
    def apply(raw: GremlinScala[GraphType]) =
      new Steps[List[A], List[AGraphType], Labels](raw)
  }

  implicit def forSet[
    A,
    AGraphType,
    Labels <: HList,
    AStepsType](implicit aConverter: Converter.Aux[A, AGraphType]) = new Constructor[Set[A], Labels] {
    type GraphType = Set[AGraphType]
    type StepsType = Steps[Set[A], Set[AGraphType], Labels]
    def apply(raw: GremlinScala[GraphType]) =
      new Steps[Set[A], Set[AGraphType], Labels](raw)
  }

  implicit val forHNil = new Constructor[HNil, HNil] {
    type GraphType = HNil
    type StepsType = Steps[HNil, HNil, HNil]
    def apply(raw: GremlinScala[HNil]) = new Steps[HNil, HNil, HNil](raw)
  }

  implicit def forHList[
    H,
    HGraphType,
    Labels <: HList,
    HStepsType,
    T <: HList,
    TGraphType <: HList,
    TStepsType](
    implicit
    hConstr: Constructor.Aux[H, Labels, HGraphType, HStepsType],
    tConstr: Constructor.Aux[T, Labels, TGraphType, TStepsType],
    converter: Converter.Aux[H :: T, HGraphType :: TGraphType]) =
      new Constructor[H :: T, Labels] {
        type GraphType = HGraphType :: TGraphType
        type StepsType = Steps[H :: T, HGraphType :: TGraphType, Labels]
        def apply(raw: GremlinScala[GraphType]): StepsType =
          new Steps[H :: T, HGraphType :: TGraphType, Labels](raw)
    }
}

trait LowestPriorityConstructorImplicits {
  // for all Products, e.g. tuples, case classes etc
  implicit def forGeneric[
    T, Repr <: HList,
    GraphTypeHList <: HList,
    GraphTypeTuple <: Product,
    Labels <: HList,
    StepsType0 <: StepsRoot,
    EndDomainHList <: HList,
    EndDomainTuple <: Product
  ](implicit
    gen: Generic.Aux[T, Repr],
    constr: Constructor.Aux[Repr, Labels, GraphTypeHList, StepsType0],  
    graphTypeTupler: Tupler.Aux[GraphTypeHList, GraphTypeTuple], 
    eq: StepsType0#EndDomain0 =:= EndDomainHList,
    tupler: Tupler.Aux[EndDomainHList, EndDomainTuple],
    converter: Converter.Aux[T, GraphTypeTuple]) =
    new Constructor[T, Labels] {
      type GraphType = GraphTypeTuple
      type StepsType = Steps[T, GraphType, Labels]
      def apply(raw: GremlinScala[GraphType]): StepsType =
        new Steps[T, GraphType, Labels](raw)
    }
}
