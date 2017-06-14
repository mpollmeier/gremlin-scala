package gremlin.scala.dsl

import gremlin.scala._
import shapeless._
import shapeless.ops.hlist.Tupler
import scala.collection.mutable
import shapeless.ops.hlist.Prepend

/** root type for all domain types */
trait DomainRoot extends Product {
  // type Underlying
}

/** just a helper trait for extracting type members for Steps */
trait StepsRoot {
  type EndDomain0
  type EndGraph0
  def raw: GremlinScala[EndGraph0, _]
}

class Steps[EndDomain, EndGraph, LabelsDomain <: HList, LabelsGraph <: HList](val raw: GremlinScala[EndGraph, LabelsGraph])(
  implicit converter: Converter.Aux[EndDomain, EndGraph]) extends StepsRoot {
  type EndDomain0 = EndDomain
  type EndGraph0 = EndGraph

  /* executes traversal and converts results into cpg domain type */
  def toList(): List[EndDomain] = raw.toList.map(converter.toDomain)
  def toSet(): Set[EndDomain] = raw.toSet.map(converter.toDomain)
  def head(): EndDomain = converter.toDomain(raw.head)
  def headOption(): Option[EndDomain] = raw.headOption.map(converter.toDomain)

  def dedup[NewSteps]()(implicit constr: Constructor.Aux[EndDomain, LabelsDomain, EndGraph, LabelsGraph, NewSteps]): NewSteps =
    constr(raw.dedup())

  /* access all gremlin-scala methods that don't modify the EndGraph type, e.g. `has` */
  /* TODO: track/use NewLabelsGraph as given by `fun` */
  def onRaw[NewSteps](fun: GremlinScala[EndGraph, LabelsGraph] => GremlinScala[EndGraph, LabelsGraph])(
    implicit constr: Constructor.Aux[EndDomain, LabelsDomain, EndGraph, LabelsGraph, NewSteps]): NewSteps =
    constr(fun(raw))

  /* TODO: track/use NewLabelsGraph as given by `fun` */
  def map[NewEndDomain, NewEndGraph, NewSteps <: StepsRoot](fun: EndDomain ⇒ NewEndDomain)(
    implicit
    newConverter: Converter.Aux[NewEndDomain, NewEndGraph],
    constr: Constructor.Aux[NewEndDomain, LabelsDomain, NewEndGraph, LabelsGraph, NewSteps]): NewSteps =
      constr {
        raw.map { endGraph: EndGraph =>
          newConverter.toGraph(fun(converter.toDomain(endGraph)))
        }
      }

  /* TODO: track/use NewLabelsGraph as given by `fun` */
  def flatMap[NewSteps <: StepsRoot](fun: EndDomain ⇒ NewSteps)(
    implicit
    constr: Constructor.Aux[NewSteps#EndDomain0, LabelsDomain, NewSteps#EndGraph0, LabelsGraph, NewSteps],
    newConverter: Converter[NewSteps#EndDomain0]
  ): NewSteps =
      constr {
        raw.flatMap { endGraph: EndGraph =>
          val newSteps: NewSteps = fun(converter.toDomain(endGraph))
          newSteps.raw.asInstanceOf[GremlinScala[NewSteps#EndGraph0, LabelsGraph]]
          // not sure why I need the cast here - should be safe though
        }
      }

  def filter[NewSteps](predicate: this.type => Steps[_, _, _, _])(
    implicit constr: Constructor.Aux[EndDomain, LabelsDomain, EndGraph, LabelsGraph, NewSteps]): NewSteps = {
    val rawWithFilter: GremlinScala[EndGraph, LabelsGraph] =
      raw.filter{ gs =>
        predicate(
          constr(gs.asInstanceOf[GremlinScala[EndGraph, LabelsGraph]]).asInstanceOf[this.type]
          /* TODO: remove casts */
        ).raw
      }
    constr(rawWithFilter)
  }

  def filterNot[NewSteps](predicate: this.type => Steps[_, _, _, _])(
    implicit constr: Constructor.Aux[EndDomain, LabelsDomain, EndGraph, LabelsGraph, NewSteps]): NewSteps = {
    val rawWithFilter: GremlinScala[EndGraph, LabelsGraph] =
      raw.filterNot{ gs =>
        predicate(
          constr(gs.asInstanceOf[GremlinScala[EndGraph, LabelsGraph]]).asInstanceOf[this.type]
          /* TODO: remove casts */
        ).raw
      }
    constr(rawWithFilter)
  }

  // labels the current step and preserves the type - use together with `select` step
  def as[NewLabelsDomain <: HList, NewLabelsGraph <: HList, NewSteps](stepLabel: String)(
    implicit prependGraph: Prepend.Aux[LabelsGraph, EndGraph :: HNil, NewLabelsGraph],
    prependDomain: Prepend.Aux[LabelsDomain, EndDomain :: HNil, NewLabelsDomain],
    constr: Constructor.Aux[EndDomain, NewLabelsDomain, EndGraph, NewLabelsGraph, NewSteps]): NewSteps =
    constr(raw.as(stepLabel))

  def select[LabelsGraphTuple, LabelsDomainTuple](
    implicit graphTupler: Tupler.Aux[LabelsGraph, LabelsGraphTuple],
    domainTupler: Tupler.Aux[LabelsDomain, LabelsDomainTuple],
    conv: Converter.Aux[LabelsDomainTuple,LabelsGraphTuple]) = 
    new Steps[LabelsDomainTuple, LabelsGraphTuple, LabelsDomain, LabelsGraph](raw.select())

  def select2[LabelsGraph1 <: HList, LabelsGraphTuple, LabelsDomainTuple](
    implicit
      conv1: Converter.Aux[LabelsDomain, LabelsGraph1],
      tupler1: Tupler.Aux[LabelsGraph1, LabelsGraphTuple],
      tupler2: Tupler.Aux[LabelsDomain, LabelsDomainTuple],
      conv2: Converter.Aux[LabelsDomainTuple, LabelsGraphTuple]
  ) = new Steps[LabelsDomainTuple, LabelsGraphTuple, LabelsDomain, LabelsGraph1](
    raw.asInstanceOf[GremlinScala[EndGraph, LabelsGraph1]].select()
  )

  def select3[LabelsGraph1 <: HList](
    implicit conv1: Converter.Aux[LabelsDomain, LabelsGraph1]
  ): LabelsGraph1 = ???

  def select4[LabelsGraph1 <: HList, LabelsGraphTuple](
    implicit conv1: Converter.Aux[LabelsDomain, LabelsGraph1],
    tupler1: Tupler.Aux[LabelsGraph1, LabelsGraphTuple]
  ): LabelsGraphTuple = ???


  override def toString = s"${getClass.getSimpleName}($raw)"

  // def or(traversals: (Self => Steps[_])*) : Self = {
  //   val foo = traversals.map(
  //     trav => { gs : GremlinScala[Vertex, HNil] => trav(construct(gs)).raw } )
  //   construct(raw.or(foo :_*))
  // }
}

/* Root class for all your vertex based DSL steps
 * TODO: add support for using Edge instead of Vertex?
 */
class NodeSteps[EndDomain <: DomainRoot, LabelsDomain <: HList, LabelsGraph <: HList](override val raw: GremlinScala[Vertex, LabelsGraph])(
  implicit marshaller: Marshallable[EndDomain]) extends Steps[EndDomain, Vertex, LabelsDomain, LabelsGraph](raw)(
  Converter.forDomainNode[EndDomain](marshaller, raw.traversal.asAdmin.getGraph.get)) {

  /** Aggregate all objects at this point into the given collection, e.g. `mutable.ArrayBuffer.empty[EndDomain]`
    * Uses eager evaluation (as opposed to `store`() which lazily fills a collection)
    */
  def aggregate[NewSteps](into: mutable.Buffer[EndDomain])(
    implicit constr: Constructor.Aux[EndDomain, LabelsDomain, Vertex, LabelsGraph, NewSteps]): NewSteps =
    constr(
      raw.sideEffect{ v: Vertex =>
        into += v.toCC[EndDomain]
      }
    )

  def filterOnEnd[NewSteps](predicate: EndDomain => Boolean)(
    implicit constr: Constructor.Aux[EndDomain, LabelsDomain, Vertex, LabelsGraph, NewSteps]): NewSteps =
    constr(
      raw.filterOnEnd { v: Vertex =>
        predicate(v.toCC[EndDomain])
      }
    )
}
