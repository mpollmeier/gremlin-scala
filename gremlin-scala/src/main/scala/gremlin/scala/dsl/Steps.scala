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

class Steps[EndDomain, EndGraph, Labels <: HList](val raw: GremlinScala[EndGraph, _])(
  implicit converter: Converter.Aux[EndDomain, EndGraph]) extends StepsRoot {
  type EndDomain0 = EndDomain
  type EndGraph0 = EndGraph

  /* executes traversal and converts results into cpg domain type */
  def toList(): List[EndDomain] = raw.toList.map(converter.toDomain)
  def toSet(): Set[EndDomain] = raw.toSet.map(converter.toDomain)
  def head(): EndDomain = converter.toDomain(raw.head)
  def headOption(): Option[EndDomain] = raw.headOption.map(converter.toDomain)

  def dedup[NewSteps]()(implicit constr: Constructor.Aux[EndDomain, Labels, EndGraph, NewSteps]): NewSteps =
    constr(raw.dedup())

  /* access all gremlin-scala methods that don't modify the EndGraph type, e.g. `has` */
  /* TODO: track/use NewLabelsGraph as given by `fun` */
  def onRaw[NewSteps](fun: GremlinScala[EndGraph, _] => GremlinScala[EndGraph, _])(
    implicit constr: Constructor.Aux[EndDomain, Labels, EndGraph, NewSteps]): NewSteps =
    constr(fun(raw))

  /* TODO: track/use NewLabelsGraph as given by `fun` */
  def map[NewEndDomain, NewEndGraph, NewSteps <: StepsRoot](fun: EndDomain ⇒ NewEndDomain)(
    implicit
    newConverter: Converter.Aux[NewEndDomain, NewEndGraph],
    constr: Constructor.Aux[NewEndDomain, Labels, NewEndGraph, NewSteps]): NewSteps =
      constr {
        raw.map { endGraph: EndGraph =>
          newConverter.toGraph(fun(converter.toDomain(endGraph)))
        }
      }

  def flatMap[NewSteps <: StepsRoot](fun: Steps[EndDomain, EndGraph] ⇒ NewSteps)(
    implicit
    constr: Constructor.Aux[NewSteps#EndDomain0, Labels, NewSteps#EndGraph0, NewSteps],
    newConverter: Converter[NewSteps#EndDomain0]
  ): NewSteps =
      constr {
        raw.flatMap { gs: GremlinScala[EndGraph, shapeless.HNil] =>
          val gs = GremlinScala[EndGraph, HNil](org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.start[EndGraph]())
          val steps = new Steps[EndDomain, EndGraph](gs)
          val newSteps: NewSteps = fun(steps)
          newSteps.raw.asInstanceOf[GremlinScala[NewSteps#EndGraph0, HNil]]
          // not sure why I need the cast here - should be safe though
        }
      }

  def filter[NewSteps](predicate: this.type => Steps[_, _, _])(
    implicit constr: Constructor.Aux[EndDomain, Labels, EndGraph, NewSteps]): NewSteps = {
    val rawWithFilter: GremlinScala[EndGraph, _] =
      raw.filter{ gs =>
        predicate(
          constr(gs.asInstanceOf[GremlinScala[EndGraph, _]]).asInstanceOf[this.type]
          /* TODO: remove casts */
        ).raw
      }
    constr(rawWithFilter)
  }

  def filterNot[NewSteps](predicate: this.type => Steps[_, _, _])(
    implicit constr: Constructor.Aux[EndDomain, Labels, EndGraph, NewSteps]): NewSteps = {
    val rawWithFilter: GremlinScala[EndGraph, _] =
      raw.filterNot{ gs =>
        predicate(
          constr(gs.asInstanceOf[GremlinScala[EndGraph, _]]).asInstanceOf[this.type]
          /* TODO: remove casts */
        ).raw
      }
    constr(rawWithFilter)
  }

  // labels the current step and preserves the type - use together with `select` step
  def as[NewLabels <: HList, NewSteps](stepLabel: String)(
    implicit prependDomain: Prepend.Aux[Labels, EndDomain :: HNil, NewLabels],
    constr: Constructor.Aux[EndDomain, NewLabels, EndGraph, NewSteps]): NewSteps =
    constr(raw.asInstanceOf[GremlinScala[EndGraph, HNil]].as(stepLabel))

  def select[LabelsGraph <: HList, LabelsGraphTuple, LabelsTuple](
    implicit
      conv1: Converter.Aux[Labels, LabelsGraph],
      tupler1: Tupler.Aux[LabelsGraph, LabelsGraphTuple],
      tupler2: Tupler.Aux[Labels, LabelsTuple],
      conv2: Converter.Aux[LabelsTuple, LabelsGraphTuple]
  ) = new Steps[LabelsTuple, LabelsGraphTuple, Labels](
    raw.asInstanceOf[GremlinScala[EndGraph, LabelsGraph]].select()
  )

  override def toString = s"${getClass.getSimpleName}($raw)"
}

/* Root class for all your vertex based DSL steps
 * TODO: add support for using Edge instead of Vertex?
 */
class NodeSteps[EndDomain <: DomainRoot, Labels <: HList](override val raw: GremlinScala[Vertex, _])(
  implicit marshaller: Marshallable[EndDomain]) extends Steps[EndDomain, Vertex, Labels](raw)(
  Converter.forDomainNode[EndDomain](marshaller, raw.traversal.asAdmin.getGraph.get)) {

  /** Aggregate all objects at this point into the given collection, e.g. `mutable.ArrayBuffer.empty[EndDomain]`
    * Uses eager evaluation (as opposed to `store`() which lazily fills a collection)
    */
  def aggregate[NewSteps](into: mutable.Buffer[EndDomain])(
    implicit constr: Constructor.Aux[EndDomain, Labels, Vertex, NewSteps]): NewSteps =
    constr(
      raw.sideEffect{ v: Vertex =>
        into += v.toCC[EndDomain]
      }
    )

  def filterOnEnd[NewSteps](predicate: EndDomain => Boolean)(
    implicit constr: Constructor.Aux[EndDomain, Labels, Vertex, NewSteps]): NewSteps =
    constr(
      raw.filterOnEnd { v: Vertex =>
        predicate(v.toCC[EndDomain])
      }
    )
}

/* TODO: removeme */
object Usage {
  val graph: ScalaGraph = ???
  // val gs: GremlinScala[Vertex, HNil] = ???
  // val x: GremlinScala[Edge, HNil] = gs.flatMap { v =>
  //   v.outE
  // }
  // new Fixture {
  //   val CoDeveloper = "co-developer"
  //   val coDevelopers: GremlinScala[Vertex, _] = for {
  //     v1 ← graph.V(1)
  //     coDeveloper ← v1.out(Created).in(Created).filter(_.is(P.neq(v1)))
  //   } yield coDeveloper

  //   val a: GremlinScala[Vertex, _] = for {
  //     v1 ← graph.V(1)
  //   } yield v1

  //   // val x: Int = coDevelopers.toList
  //   // coDevelopers.toList.foreach { case (v1: Vertex, coDeveloper: Vertex) =>
  //   //   v1 --- CoDeveloper --> coDeveloper
  //   // }
  // }

  trait Fixture {
    val graph: ScalaGraph = ???
    val Name = Key[String]("name")
    val Nickname = Key[String]("nickname")
    val Lang = Key[String]("lang")
    val Age = Key[Int]("age")
    val StartTime = Key[Int]("startTime")
    val Knows = "knows"
    val Person = "person"
    val Created = "created"
  }
}
