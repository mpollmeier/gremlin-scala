package gremlin.scala.dsl

import gremlin.scala._
import scala.collection.mutable
import shapeless.HList

/* Root class for all your vertex based DSL steps
 * TODO: add support for using Edge instead of Vertex?
 */
class NodeSteps[EndDomain <: DomainRoot, Labels <: HList](override val raw: GremlinScala[Vertex])(
    implicit marshaller: Marshallable[EndDomain])
    extends Steps[EndDomain, Vertex, Labels](raw)(
      Converter.forDomainNode[EndDomain](marshaller, raw.traversal.asAdmin.getGraph.get)) {

  /** Aggregate all objects at this point into the given collection, e.g. `mutable.ArrayBuffer.empty[EndDomain]`
    * Uses eager evaluation (as opposed to `store`() which lazily fills a collection)
    */
  def aggregate(into: mutable.Buffer[EndDomain]): NodeSteps[EndDomain, Labels] =
    new NodeSteps[EndDomain, Labels](
      raw.sideEffect { v: Vertex =>
        into += v.toCC[EndDomain]
      }
    )

  def filterOnEnd(predicate: EndDomain => Boolean): NodeSteps[EndDomain, Labels] =
    new NodeSteps[EndDomain, Labels](
      raw.filterOnEnd { v: Vertex =>
        predicate(v.toCC[EndDomain])
      }
    )
}
