package gremlin.scala.dsl

import gremlin.scala._
import shapeless._

/** root type for all domain types */
trait DomainRoot extends Product {
  // type Underlying
}

/** just a helper trait for extracting type members for Steps */
trait StepsRoot {
  type EndDomain0
  type EndGraph0
  def raw: GremlinScala[EndGraph0, HNil]
}

class Steps[EndDomain, EndGraph](val raw: GremlinScala[EndGraph, HNil])(
  implicit converter: Converter.Aux[EndDomain, EndGraph]) extends StepsRoot {
  type EndDomain0 = EndDomain
  type EndGraph0 = EndGraph

  /* executes traversal and converts results into cpg domain type */
  def toList(): List[EndDomain] = raw.toList.map(converter.toDomain)
  def toSet(): Set[EndDomain] = raw.toSet.map(converter.toDomain)
  def head(): EndDomain = converter.toDomain(raw.head)
  def headOption(): Option[EndDomain] = raw.headOption.map(converter.toDomain)

  def map[NewEndDomain, NewEndGraph, NewSteps <: StepsRoot](fun: EndDomain ⇒ NewEndDomain)(
    implicit
    newConverter: Converter.Aux[NewEndDomain, NewEndGraph],
    constr: Constructor.Aux[NewEndDomain, NewEndGraph, NewSteps]): NewSteps =
      constr {
        raw.map { endGraph: EndGraph =>
          newConverter.toGraph(fun(converter.toDomain(endGraph)))
        }
      }

  def flatMap[NewSteps <: StepsRoot](fun: EndDomain ⇒ NewSteps)(
    implicit
    constr: Constructor.Aux[NewSteps#EndDomain0, NewSteps#EndGraph0, NewSteps],
    newConverter: Converter[NewSteps#EndDomain0]
  ): NewSteps =
      constr {
        raw.flatMap { endGraph: EndGraph =>
          val newSteps: NewSteps = fun(converter.toDomain(endGraph))
          newSteps.raw.asInstanceOf[GremlinScala[NewSteps#EndGraph0, HNil]]
          // not sure why I need the cast here - should be safe though
        }
      }
}

/* Root class for all your vertex based DSL steps
 * basically just a shortcut for getting the implicits/types right
 * TODO: add support for as/select - currently always HNil
 * TODO: add support for using Edge instead of Vertex?
 */
class NodeSteps[EndDomain <: DomainRoot](override val raw: GremlinScala[Vertex, HNil])(
  implicit marshaller: Marshallable[EndDomain]) extends Steps[EndDomain, Vertex](raw)(
  Converter.forDomainNode[EndDomain](marshaller, raw.traversal.asAdmin.getGraph.get)
)
