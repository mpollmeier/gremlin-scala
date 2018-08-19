package gremlin.scala.dsl

import gremlin.scala._
import gremlin.scala.StepLabel.{combineLabelWithValue, GetLabelName}
import java.util.{Map => JMap}
import scala.collection.mutable
import shapeless._
import shapeless.ops.hlist.{IsHCons, Mapper, Prepend, RightFolder, ToTraversable, Tupler}
import shapeless.ops.product.ToHList

/** root type for all domain types */
trait DomainRoot extends Product {
  // type Underlying
}

/** just a helper trait for extracting type members for Steps */
trait StepsRoot {
  type EndDomain0
  type EndGraph0
  def raw: GremlinScala[EndGraph0]
}

class Steps[EndDomain, EndGraph, Labels <: HList](val raw: GremlinScala[EndGraph])(
    implicit converter: Converter.Aux[EndDomain, EndGraph])
    extends StepsRoot {
  type EndDomain0 = EndDomain
  type EndGraph0 = EndGraph

  /* executes traversal and converts results into cpg domain type */
  def toList(): List[EndDomain] = raw.toList.map(converter.toDomain)
  def toSet(): Set[EndDomain] = raw.toSet.map(converter.toDomain)
  def head(): EndDomain = converter.toDomain(raw.head)
  def headOption(): Option[EndDomain] = raw.headOption.map(converter.toDomain)
  override def clone() = new Steps[EndDomain, EndGraph, Labels](raw.clone())

  def dedup(): Steps[EndDomain, EndGraph, Labels] =
    new Steps[EndDomain, EndGraph, Labels](raw.dedup())

  /* access all gremlin-scala methods that don't modify the EndGraph type, e.g. `has` */
  /* TODO: track/use NewLabelsGraph as given by `fun` */
  def onRaw(
      fun: GremlinScala[EndGraph] => GremlinScala[EndGraph]): Steps[EndDomain, EndGraph, Labels] =
    new Steps[EndDomain, EndGraph, Labels](fun(raw))

  /* TODO: track/use NewLabelsGraph as given by `fun` */
  def map[NewEndDomain, NewEndGraph, NewSteps <: StepsRoot](fun: EndDomain => NewEndDomain)(
      implicit
      newConverter: Converter.Aux[NewEndDomain, NewEndGraph],
      constr: Constructor.Aux[NewEndDomain, Labels, NewEndGraph, NewSteps]): NewSteps =
    constr {
      raw.map { endGraph: EndGraph =>
        newConverter.toGraph(fun(converter.toDomain(endGraph)))
      }
    }

  /* TODO: track/use NewLabelsGraph as given by `fun` */
  def flatMap[NewSteps <: StepsRoot](fun: EndDomain => NewSteps)(
      implicit
      constr: Constructor.Aux[NewSteps#EndDomain0, Labels, NewSteps#EndGraph0, NewSteps],
      newConverter: Converter[NewSteps#EndDomain0]
  ): NewSteps =
    constr {
      raw.flatMap { endGraph: EndGraph =>
        val newSteps: NewSteps = fun(converter.toDomain(endGraph))
        newSteps.raw.asInstanceOf[GremlinScala[NewSteps#EndGraph0]]
      // not sure why I need the cast here - should be safe though
      }
    }

  def filter(predicate: Steps[EndDomain, EndGraph, Labels] => Steps[_, _, _])
    : Steps[EndDomain, EndGraph, Labels] = {
    val rawWithFilter: GremlinScala[EndGraph] =
      raw.filter { gs =>
        predicate(
          new Steps[EndDomain, EndGraph, Labels](gs)
        ).raw
      }
    new Steps[EndDomain, EndGraph, Labels](rawWithFilter)
  }

  def filterNot(predicate: Steps[EndDomain, EndGraph, Labels] => Steps[_, _, _])
    : Steps[EndDomain, EndGraph, Labels] = {
    val rawWithFilter: GremlinScala[EndGraph] =
      raw.filterNot { gs =>
        predicate(
          new Steps[EndDomain, EndGraph, Labels](gs)
        ).raw
      }
    new Steps[EndDomain, EndGraph, Labels](rawWithFilter)
  }

  // labels the current step and preserves the type - use together with `select` step
  def as[NewLabels <: HList](stepLabel: String)(
      implicit prependDomain: Prepend.Aux[Labels, EndDomain :: HNil, NewLabels])
    : Steps[EndDomain, EndGraph, NewLabels] =
    new Steps[EndDomain, EndGraph, NewLabels](
      raw.asInstanceOf[GremlinScala.Aux[EndGraph, HNil]].as(stepLabel))

  def as[NewLabels <: HList](stepLabel: StepLabel[EndDomain])(
      implicit prependDomain: Prepend.Aux[Labels, EndDomain :: HNil, NewLabels])
    : Steps[EndDomain, EndGraph, NewLabels] =
    new Steps[EndDomain, EndGraph, NewLabels](
      raw.asInstanceOf[GremlinScala.Aux[EndGraph, HNil]].as(stepLabel.name))

  // select all labels
  def select[LabelsGraph <: HList, LabelsGraphTuple, LabelsTuple]()(
      implicit
      conv1: Converter.Aux[Labels, LabelsGraph],
      tupler1: Tupler.Aux[LabelsGraph, LabelsGraphTuple],
      tupler2: Tupler.Aux[Labels, LabelsTuple],
      conv2: Converter.Aux[LabelsTuple, LabelsGraphTuple]
  ) = new Steps[LabelsTuple, LabelsGraphTuple, Labels](
    raw.asInstanceOf[GremlinScala.Aux[EndGraph, LabelsGraph]].select()
  )

  // select one specific label
  def select[Label, LabelGraph](label: StepLabel[Label])(
      implicit conv1: Converter.Aux[Label, LabelGraph]) =
    new Steps[Label, LabelGraph, Labels](raw.select(StepLabel[LabelGraph](label.name)))

  // select multiple specific labels
  def select[StepLabelsTuple <: Product,
             StepLabels <: HList,
             H0,
             T0 <: HList,
             SelectedTypes <: HList,
             SelectedTypesTuple <: Product,
             SelectedGraphTypesTuple <: Product,
             LabelNames <: HList,
             Z](stepLabelsTuple: StepLabelsTuple)(
      implicit toHList: ToHList.Aux[StepLabelsTuple, StepLabels],
      hasOne: IsHCons.Aux[StepLabels, H0, T0],
      hasTwo: IsHCons[T0], // witnesses that labels has > 1 elements
      extractLabelType: StepLabel.ExtractLabelType.Aux[StepLabels, SelectedTypes],
      tupler: Tupler.Aux[SelectedTypes, SelectedTypesTuple],
      conv: Converter.Aux[SelectedTypesTuple, SelectedGraphTypesTuple],
      stepLabelToString: Mapper.Aux[GetLabelName.type, StepLabels, LabelNames],
      trav: ToTraversable.Aux[LabelNames, List, String],
      folder: RightFolder.Aux[StepLabels,
                              (HNil, JMap[String, Any]),
                              combineLabelWithValue.type,
                              (SelectedTypes, Z)]
  ): Steps[SelectedTypesTuple, SelectedGraphTypesTuple, Labels] = {
    val stepLabels: StepLabels = toHList(stepLabelsTuple)
    val labels: List[String] = stepLabels.map(GetLabelName).toList
    val label1 = labels.head
    val label2 = labels.tail.head
    val remainder = labels.tail.tail

    val selectTraversal =
      raw.traversal.select[Any](label1, label2, remainder: _*)
    val newRaw: GremlinScala[SelectedGraphTypesTuple] =
      GremlinScala(selectTraversal).map { selectValues =>
        val resultTuple = stepLabels.foldRight((HNil: HNil, selectValues))(combineLabelWithValue)
        val values: SelectedTypes = resultTuple._1
        tupler(values)
          .asInstanceOf[SelectedGraphTypesTuple] //dirty but does the trick
      }

    new Steps[SelectedTypesTuple, SelectedGraphTypesTuple, Labels](newRaw)
  }

  override def toString = s"${getClass.getSimpleName}($raw)"
}

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
