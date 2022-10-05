package gremlin.scala.dsl

import gremlin.scala._
import java.util.{Map => JMap}
import scala.collection.mutable

/* Root class for all your vertex based DSL steps
 * TODO: add support for using Edge instead of Vertex?
 */
class NodeSteps[EndDomain <: DomainRoot, Labels <: Tuple](override val raw: GremlinScala[Vertex])(
    using marshaller: Marshallable[EndDomain])
    extends Steps[EndDomain, Vertex, Labels](raw)(
      Converter.forDomainNode[EndDomain](marshaller, raw.traversal.asAdmin.getGraph.get)) {

  /* follow the incoming edges of the given type as long as possible */
  def walkIn(edgeType: String): GremlinScala[Vertex] =
    raw
      .repeat(_.in(edgeType))
      .until(_.in(edgeType).count().is(P.eq(0)))

  /** Aggregate all objects at this point into the given collection, e.g. `mutable.ArrayBuffer.empty[EndDomain]`
    * Uses eager evaluation (as opposed to `store`() which lazily fills a collection)
    */
  def aggregate(into: mutable.Buffer[EndDomain]): NodeSteps[EndDomain, Labels] =
    new NodeSteps[EndDomain, Labels](
      raw.sideEffect { (v: Vertex) =>
        into += v.toCC[EndDomain]
      }
    )

  def filterOnEnd(predicate: EndDomain => Boolean): NodeSteps[EndDomain, Labels] =
    new NodeSteps[EndDomain, Labels](
      raw.filterOnEnd { (v: Vertex) =>
        predicate(v.toCC[EndDomain])
      }
    )

  /** filter by id */
  def id(ids: AnyRef*): NodeSteps[EndDomain, Labels] =
    new NodeSteps[EndDomain, Labels](raw.hasId(ids: _*))

  /**
     Extend the traversal with a side-effect step, where `fun` is a
     function that performs a side effect. The function `fun` can
     access the current traversal element via the variable `_`.
    */
  def sideEffect(fun: EndDomain => Any): NodeSteps[EndDomain, Labels] =
    new NodeSteps[EndDomain, Labels](raw.sideEffect { (v: Vertex) =>
      fun(v.toCC[EndDomain])
    })

}
