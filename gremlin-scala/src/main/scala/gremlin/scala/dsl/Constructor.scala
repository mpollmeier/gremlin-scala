package gremlin.scala.dsl

import gremlin.scala._
import shapeless._
import shapeless.ops.hlist.Tupler
import shapeless.ops.product.ToHList

trait Constructor[DomainType, LabelsDomain <: HList] {
  type GraphType
  type StepsType
  def apply(raw: GremlinScala[GraphType, _]): StepsType
}

object Constructor extends LowPriorityConstructorImplicits {
  type Aux[DomainType, LabelsDomain <: HList, GraphTypeOut, StepsTypeOut] = Constructor[DomainType, LabelsDomain] {
    type GraphType = GraphTypeOut
    type StepsType = StepsTypeOut
  }
}

trait LowPriorityConstructorImplicits extends LowestPriorityConstructorImplicits {

  implicit def forSimpleType[A, LabelsDomain <: HList](implicit converter: Converter.Aux[A, A]) =
    new Constructor[A, LabelsDomain] {
      type GraphType = A
      type StepsType = Steps[A, A, LabelsDomain]
      def apply(raw: GremlinScala[GraphType, _]) = new Steps[A, A, LabelsDomain](raw)
    }

  def forDomainNode[
    DomainType <: DomainRoot,
    LabelsDomain <: HList,
    StepsTypeOut <: NodeSteps[DomainType, LabelsDomain]](
    constr: GremlinScala[Vertex, _] => StepsTypeOut) = new Constructor[DomainType, LabelsDomain] {
    type GraphType = Vertex
    type StepsType = StepsTypeOut

    def apply(raw: GremlinScala[GraphType, _]): StepsTypeOut = constr(raw)
  }

  implicit def forList[
    A,
    AGraphType,
    LabelsDomain <: HList,
    AStepsType](implicit aConverter: Converter.Aux[A, AGraphType]) = new Constructor[List[A], LabelsDomain] {
    type GraphType = List[AGraphType]
    type StepsType = Steps[List[A], List[AGraphType], LabelsDomain]
    def apply(raw: GremlinScala[GraphType, _]) =
      new Steps[List[A], List[AGraphType], LabelsDomain](raw)
  }

  implicit def forSet[
    A,
    AGraphType,
    LabelsDomain <: HList,
    AStepsType](implicit aConverter: Converter.Aux[A, AGraphType]) = new Constructor[Set[A], LabelsDomain] {
    type GraphType = Set[AGraphType]
    type StepsType = Steps[Set[A], Set[AGraphType], LabelsDomain]
    def apply(raw: GremlinScala[GraphType, _]) =
      new Steps[Set[A], Set[AGraphType], LabelsDomain](raw)
  }

  implicit val forHNil = new Constructor[HNil, HNil] {
    type GraphType = HNil
    type StepsType = Steps[HNil, HNil, HNil]
    def apply(raw: GremlinScala[HNil, _]) = new Steps[HNil, HNil, HNil](raw)
  }

  implicit def forHList[
    H,
    HGraphType,
    LabelsDomain <: HList,
    HStepsType,
    T <: HList,
    TGraphType <: HList,
    TStepsType](
    implicit
    hConstr: Constructor.Aux[H, LabelsDomain, HGraphType, HStepsType],
    tConstr: Constructor.Aux[T, LabelsDomain, TGraphType, TStepsType],
    converter: Converter.Aux[H :: T, HGraphType :: TGraphType]) =
      new Constructor[H :: T, LabelsDomain] {
        type GraphType = HGraphType :: TGraphType
        type StepsType = Steps[H :: T, HGraphType :: TGraphType, LabelsDomain]
        def apply(raw: GremlinScala[GraphType, _]): StepsType =
          new Steps[H :: T, HGraphType :: TGraphType, LabelsDomain](raw)
    }
}

trait LowestPriorityConstructorImplicits {
  // for all Products, e.g. tuples, case classes etc
  implicit def forGeneric[
    T, Repr <: HList,
    GraphTypeHList <: HList,
    GraphTypeTuple <: Product,
    LabelsDomain <: HList,
    StepsType0 <: StepsRoot,
    EndDomainHList <: HList,
    EndDomainTuple <: Product
  ](implicit
    gen: Generic.Aux[T, Repr],
    constr: Constructor.Aux[Repr, LabelsDomain, GraphTypeHList, StepsType0],  
    graphTypeTupler: Tupler.Aux[GraphTypeHList, GraphTypeTuple], 
    eq: StepsType0#EndDomain0 =:= EndDomainHList,
    tupler: Tupler.Aux[EndDomainHList, EndDomainTuple],
    converter: Converter.Aux[T, GraphTypeTuple]) =
    new Constructor[T, LabelsDomain] {
      type GraphType = GraphTypeTuple
      type StepsType = Steps[T, GraphType, LabelsDomain]
      def apply(raw: GremlinScala[GraphType, _]): StepsType =
        new Steps[T, GraphType, LabelsDomain](raw)
    }
}
