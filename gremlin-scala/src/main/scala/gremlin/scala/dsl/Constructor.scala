package gremlin.scala.dsl

import gremlin.scala._
import shapeless._
import shapeless.ops.hlist.Tupler
import shapeless.ops.product.ToHList

trait Constructor[DomainType] {
  type GraphType
  type StepsType
  def apply(raw: GremlinScala[GraphType, HNil]): StepsType
}
object Constructor {
  type Aux[DomainType, GraphTypeOut, StepsTypeOut] = Constructor[DomainType] {
    type GraphType = GraphTypeOut
    type StepsType = StepsTypeOut
  }

  implicit val forString = new Constructor[String] {
    type GraphType = String
    type StepsType = Steps[String, String]
    def apply(raw: GremlinScala[GraphType, HNil]): Steps[String, String] = new Steps[String, String](raw)
  }

  implicit val forInteger = new Constructor[Integer] {
    type GraphType = Integer
    type StepsType = Steps[Integer, Integer]
    def apply(raw: GremlinScala[GraphType, HNil]): Steps[Integer, Integer] = new Steps[Integer, Integer](raw)
  }

  implicit val forBoolean = new Constructor[Boolean] {
    type GraphType = Boolean
    type StepsType = Steps[Boolean, Boolean]
    def apply(raw: GremlinScala[GraphType, HNil]): Steps[Boolean, Boolean] = new Steps[Boolean, Boolean](raw)
  }

  def forDomainNode[DomainType <: DomainRoot, StepsTypeOut <: NodeSteps[DomainType]](
    constr: GremlinScala[Vertex, HNil] => StepsTypeOut) = new Constructor[DomainType] {
    type GraphType = Vertex
    type StepsType = StepsTypeOut

    def apply(raw: GremlinScala[GraphType, HNil]): StepsTypeOut = constr(raw)
  }

  implicit val forHNil = new Constructor[HNil] {
    type GraphType = HNil
    type StepsType = Steps[HNil, HNil]
    def apply(raw: GremlinScala[HNil, HNil]) = new Steps[HNil, HNil](raw)
  }

  implicit def forHList[
    H,
    HGraphType,
    HStepsType,
    T <: HList,
    TGraphType <: HList,
    TStepsType](
    implicit
    hConstr: Constructor.Aux[H, HGraphType, HStepsType],
    tConstr: Constructor.Aux[T, TGraphType, TStepsType],
    converter: Converter.Aux[H :: T, HGraphType :: TGraphType]) =
      new Constructor[H :: T] {
        type GraphType = HGraphType :: TGraphType
        type StepsType = Steps[H :: T, HGraphType :: TGraphType]
        def apply(raw: GremlinScala[GraphType, HNil]): StepsType =
          new Steps[H :: T, HGraphType :: TGraphType](raw)
    }

  // for all Products, e.g. tuples, case classes etc
  implicit def forGeneric[
    T, Repr <: HList,
    GraphTypeHList <: HList,
    GraphTypeTuple <: Product,
    StepsType0 <: StepsRoot,
    EndDomainHList <: HList,
    EndDomainTuple <: Product
  ](implicit
    gen: Generic.Aux[T, Repr],
    constr: Constructor.Aux[Repr, GraphTypeHList, StepsType0],  
    graphTypeTupler: Tupler.Aux[GraphTypeHList, GraphTypeTuple], 
    eq: StepsType0#EndDomain0 =:= EndDomainHList,
    tupler: Tupler.Aux[EndDomainHList, EndDomainTuple],
    converter: Converter.Aux[T, GraphTypeTuple]) =
    new Constructor[T] {
      type GraphType = GraphTypeTuple
      type StepsType = Steps[T, GraphType]
      def apply(raw: GremlinScala[GraphType, HNil]): StepsType =
        new Steps[T, GraphType](raw)
    }
}
