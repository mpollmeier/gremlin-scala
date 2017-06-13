package gremlin.scala.dsl

import gremlin.scala._
import shapeless._
import shapeless.ops.hlist.Tupler
import shapeless.ops.product.ToHList

trait Constructor[DomainType, LabelsDomain <: HList] {
  type GraphType
  type LabelsGraph <: HList
  type StepsType
  def apply(raw: GremlinScala[GraphType, LabelsGraph]): StepsType
}

object Constructor extends LowPriorityConstructorImplicits {
  type Aux[DomainType, LabelsDomain <: HList, GraphTypeOut, LabelsGraphOut <: HList, StepsTypeOut] = Constructor[DomainType, LabelsDomain] {
    type GraphType = GraphTypeOut
    type LabelsGraph = LabelsGraphOut 
    type StepsType = StepsTypeOut
  }
}

trait LowPriorityConstructorImplicits extends LowestPriorityConstructorImplicits {

  /* TODO: derive LabelsGraph via implicit: labelsConverter: Converter.Aux[LabelsDomain, LabelsGraph] */
  def forBaseType[A, LabelsDomain <: HList, LabelsGraph1 <: HList](implicit converter: Converter.Aux[A, A]) = new Constructor[A, LabelsDomain] {
    type GraphType = A
    type LabelsGraph = LabelsGraph1
    type StepsType = Steps[A, A, LabelsDomain, LabelsGraph]
    def apply(raw: GremlinScala[GraphType, LabelsGraph]) = new Steps[A, A, LabelsDomain, LabelsGraph](raw)
  }

  implicit def forUnit[LabelsDomain <: HList, LabelsGraph <: HList] = forBaseType[Unit, LabelsDomain, LabelsGraph]
  implicit def forString[LabelsDomain <: HList, LabelsGraph <: HList] = forBaseType[String, LabelsDomain, LabelsGraph]
  implicit def forInt[LabelsDomain <: HList, LabelsGraph <: HList] = forBaseType[Int, LabelsDomain, LabelsGraph]
  implicit def forDouble[LabelsDomain <: HList, LabelsGraph <: HList] = forBaseType[Double, LabelsDomain, LabelsGraph]
  implicit def forFloat[LabelsDomain <: HList, LabelsGraph <: HList] = forBaseType[Float, LabelsDomain, LabelsGraph]
  implicit def forBoolean[LabelsDomain <: HList, LabelsGraph <: HList] = forBaseType[Boolean, LabelsDomain, LabelsGraph]
  implicit def forInteger[LabelsDomain <: HList, LabelsGraph <: HList] = forBaseType[Integer, LabelsDomain, LabelsGraph]
  implicit def forJDouble[LabelsDomain <: HList, LabelsGraph <: HList] = forBaseType[java.lang.Double, LabelsDomain, LabelsGraph]
  implicit def forJFloat[LabelsDomain <: HList, LabelsGraph <: HList] = forBaseType[java.lang.Float, LabelsDomain, LabelsGraph]

  def forDomainNode[
    DomainType <: DomainRoot,
    LabelsDomain <: HList,
    LabelsGraph1 <: HList,
    StepsTypeOut <: NodeSteps[DomainType, LabelsDomain, LabelsGraph1]](
    constr: GremlinScala[Vertex, LabelsGraph1] => StepsTypeOut) = new Constructor[DomainType, LabelsDomain] {
    type GraphType = Vertex
    type StepsType = StepsTypeOut
    type LabelsGraph = LabelsGraph1

    def apply(raw: GremlinScala[GraphType, LabelsGraph]): StepsTypeOut = constr(raw)
  }

  // implicit def forList[
  //   A,
  //   AGraphType,
  //   LabelsDomain <: HList,
  //   LabelsGraph <: HList,
  //   AStepsType](implicit aConverter: Converter.Aux[A, AGraphType]) = new Constructor[List[A]] {
  //   type GraphType = List[AGraphType]
  //   type StepsType = Steps[List[A], List[AGraphType], List[LabelsDomain], List[LabelsGraph]]
  //   def apply(raw: GremlinScala[GraphType, List[LabelsGraph]]) =
  //     new Steps[List[A], List[AGraphType], List[LabelsDomain], List[LabelsGraph]](raw)
  // }

  // implicit def forSet[
  //   A,
  //   AGraphType,
  //   LabelsDomain <: HList,
  //   LabelsGraph <: HList,
  //   AStepsType](implicit aConverter: Converter.Aux[A, AGraphType]) = new Constructor[Set[A]] {
  //   type GraphType = Set[AGraphType]
  //   type StepsType = Steps[Set[A], Set[AGraphType], Set[LabelsDomain], Set[LabelsGraph]]
  //   def apply(raw: GremlinScala[GraphType, Set[LabelsGraph]]) =
  //     new Steps[Set[A], Set[AGraphType], Set[LabelsDomain], Set[LabelsGraph]](raw)
  // }

  implicit val forHNil = new Constructor[HNil, HNil] {
    type GraphType = HNil
    type LabelsGraph = HNil
    type StepsType = Steps[HNil, HNil, HNil, HNil]
    def apply(raw: GremlinScala[HNil, HNil]) = new Steps[HNil, HNil, HNil, HNil](raw)
  }

  implicit def forHList[
    H,
    HGraphType,
    HLabelsDomain <: HList,
    HLabelsGraph <: HList,
    HStepsType,
    T <: HList,
    TGraphType <: HList,
    TLabelsDomain <: HList,
    TLabelsGraph <: HList,
    TStepsType](
    implicit
    hConstr: Constructor.Aux[H, HLabelsDomain, HGraphType, HLabelsGraph, HStepsType],
    tConstr: Constructor.Aux[T, TLabelsDomain, TGraphType, TLabelsGraph, TStepsType],
    converter: Converter.Aux[H :: T, HGraphType :: TGraphType]) =
      new Constructor[H :: T, HLabelsDomain :: TLabelsDomain] {
        type GraphType = HGraphType :: TGraphType
        type LabelsGraph = HLabelsGraph :: TLabelsGraph
        type StepsType = Steps[H :: T, HGraphType :: TGraphType, HLabelsDomain :: TLabelsDomain, HLabelsGraph :: TLabelsGraph]
        def apply(raw: GremlinScala[GraphType, HLabelsGraph :: TLabelsGraph]): StepsType =
          new Steps[H :: T, HGraphType :: TGraphType, HLabelsDomain :: TLabelsDomain, HLabelsGraph :: TLabelsGraph](raw)
    }
}

trait LowestPriorityConstructorImplicits {
  // for all Products, e.g. tuples, case classes etc
  // implicit def forGeneric[
  //   T, Repr <: HList,
  //   GraphTypeHList <: HList,
  //   GraphTypeTuple <: Product,
  //   StepsType0 <: StepsRoot,
  //   EndDomainHList <: HList,
  //   EndDomainTuple <: Product
  // ](implicit
  //   gen: Generic.Aux[T, Repr],
  //   constr: Constructor.Aux[Repr, GraphTypeHList, StepsType0],  
  //   graphTypeTupler: Tupler.Aux[GraphTypeHList, GraphTypeTuple], 
  //   eq: StepsType0#EndDomain0 =:= EndDomainHList,
  //   tupler: Tupler.Aux[EndDomainHList, EndDomainTuple],
  //   converter: Converter.Aux[T, GraphTypeTuple]) =
  //   new Constructor[T] {
  //     type GraphType = GraphTypeTuple
  //     type StepsType = Steps[T, GraphType]
  //     def apply(raw: GremlinScala[GraphType, _]): StepsType =
  //       new Steps[T, GraphType](raw)
  //   }
}
