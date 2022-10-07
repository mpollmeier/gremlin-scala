package gremlin.scala.dsl

import gremlin.scala._
import gremlin.scala.StepLabel.{combineLabelWithValue, GetLabelName}
import java.util.{Map => JMap}
import java.util.stream.{Stream => JStream}
import scala.collection.mutable
import compiletime.ops.int.*

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

class Steps[EndDomain, EndGraph, Labels <: Tuple](val raw: GremlinScala[EndGraph])(
  using val converter: Converter.Aux[EndDomain, EndGraph]
) extends StepsRoot {
  type EndDomain0 = EndDomain
  type EndGraph0 = EndGraph

  /* executes traversal and converts results into cpg domain type */
  def toList(): List[EndDomain] = raw.toList().map(converter.toDomain)
  def toStream(): JStream[EndDomain] = raw.toStream().map { (end: EndGraph) => converter.toDomain(end) }
  def toSet(): Set[EndDomain] = raw.toSet().map(converter.toDomain)
  def iterate(): Unit = raw.iterate()
  def exec(): Unit = iterate()

  /**
    Execute the traversal and convert it to a mutable buffer
    */
  def toBuffer(): mutable.Buffer[EndDomain] = toList().toBuffer

  def head(): EndDomain = converter.toDomain(raw.head())
  def headOption(): Option[EndDomain] = raw.headOption().map(converter.toDomain)
  def isDefined: Boolean = headOption().isDefined

  /**
    * shortcut for `toList`
    */
  def l: List[EndDomain] = toList()

  /**
    Alias for `toStream`
    */
  def s(): JStream[EndDomain] = toStream()

  /**
    * print the results to stdout
    */
  def p(): List[String] = {
    l.map {
      case vertex: Vertex => {
        val label = vertex.label
        val id = vertex.id().toString
        val keyValPairs = vertex.valueMap.toList
          .filter(x => x._2.toString != "")
          .sortBy(_._1)
          .map(x => x._1 + ": " + x._2)
        s"($label,$id): " + keyValPairs.mkString(", ")
      }
      case elem => elem.toString
    }
  }

  def count(): Long =
    raw.count().head()

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
    using
    newConverter: Converter.Aux[NewEndDomain, NewEndGraph],
    constr: Constructor.Aux[NewEndDomain, Labels, NewEndGraph, NewSteps]
  ): NewSteps =
    constr {
      raw.map { (endGraph: EndGraph) =>
        newConverter.toGraph(fun(converter.toDomain(endGraph)))
      }
    }

  /* TODO: track/use NewLabelsGraph as given by `fun` */
  def flatMap[NewSteps <: StepsRoot](fun: EndDomain => NewSteps)(
    using
    constr: Constructor.Aux[NewSteps#EndDomain0, Labels, NewSteps#EndGraph0, NewSteps],
    newConverter: Converter[NewSteps#EndDomain0]
  ): NewSteps =
    constr {
      raw.flatMap { (endGraph: EndGraph) =>
        val newSteps: NewSteps = fun(converter.toDomain(endGraph))
        newSteps.raw.asInstanceOf[GremlinScala[NewSteps#EndGraph0]]
      // not sure why I need the cast here - should be safe though
      }
    }

  def filter(
    predicate: Steps[EndDomain, EndGraph, Labels] => Steps[_, _, _]
  ): Steps[EndDomain, EndGraph, Labels] = {
    val rawWithFilter: GremlinScala[EndGraph] =
      raw.filter { gs =>
        predicate(
          new Steps[EndDomain, EndGraph, Labels](gs)
        ).raw
      }
    new Steps[EndDomain, EndGraph, Labels](rawWithFilter)
  }

  def filterNot(
    predicate: Steps[EndDomain, EndGraph, Labels] => Steps[_, _, _]
  ): Steps[EndDomain, EndGraph, Labels] = {
    val rawWithFilter: GremlinScala[EndGraph] =
      raw.filterNot { gs =>
        predicate(
          new Steps[EndDomain, EndGraph, Labels](gs)
        ).raw
      }
    new Steps[EndDomain, EndGraph, Labels](rawWithFilter)
  }

  // labels the current step and preserves the type - use together with `select` step
  def as(stepLabel: String): Steps[EndDomain, EndGraph, Tuple.Append[Labels, EndDomain]] =
    Steps[EndDomain, EndGraph, Tuple.Append[Labels, EndDomain]](
      raw.asInstanceOf[GremlinScala.Aux[EndGraph, EmptyTuple]].as(stepLabel)
    )

  def as(stepLabel: StepLabel[EndDomain]): Steps[EndDomain, EndGraph, Tuple.Append[Labels, EndDomain]] =
    new Steps[EndDomain, EndGraph, Tuple.Append[Labels, EndDomain]](
      raw.asInstanceOf[GremlinScala.Aux[EndGraph, EmptyTuple]].as(stepLabel.name))

  // select all labels
  def select[LabelsGraph]()(using conv: Converter.Aux[Labels, LabelsGraph]) =
    Steps[Labels, LabelsGraph, Labels](
      raw.asInstanceOf[GremlinScala.Aux[EndGraph, LabelsGraph]].select()
    )

  // select one specific label
  def select[Label, LabelGraph](label: StepLabel[Label])(
    using conv1: Converter.Aux[Label, LabelGraph]
  ) =
    Steps[Label, LabelGraph, Labels](raw.select(StepLabel[LabelGraph](label.name)))

  def getLabelValueFromMap[A](sl: StepLabel[A], values: JMap[String, Any]): A =
    values.get(sl.name).asInstanceOf[A]

  // select multiple specific labels
  def select[
    StepLabels <: NonEmptyTuple,
    SelectedTypes <: NonEmptyTuple,
    SelectedGraphTypes <: NonEmptyTuple,
    LabelNames <: Tuple
  ](stepLabels: StepLabels)(
    using
    Tuple.Union[StepLabels] <:< StepLabel[_],
    Tuple.Size[StepLabels] <= 2,
  )(
    using
    extractLabelType: StepLabel.ExtractLabelType.Aux[StepLabels, SelectedTypes],
    conv: Converter.Aux[SelectedTypes, SelectedGraphTypes],
  ): Steps[SelectedTypes, SelectedGraphTypes, Labels] = {
    val labels: List[String] = stepLabels.map(_.name).toList()
    val label1 = labels.head
    val label2 = labels.tail.head
    val remainder = labels.tail.tail

    val selectTraversal = raw.traversal.select[Any](label1, label2, remainder: _*)
    val newRaw: GremlinScala[SelectedGraphTypes] =
      GremlinScala(selectTraversal).map { selectValues =>
        val values: SelectedTypes = stepLabels map {
          case sl: StepLabel[a] => getLabelValueFromMap[a](sl, selectValues)
        }
        values.asInstanceOf[SelectedGraphTypes] //dirty but does the trick
      }

    new Steps[SelectedTypes, SelectedGraphTypes, Labels](newRaw)
  }

  /**
    Repeat the given traversal. This step can be combined with the until and emit steps to
    provide a termination and emit criteria.
    */
  def repeat[NewEndDomain >: EndDomain](
    repeatTraversal: Steps[EndDomain, EndGraph, EmptyTuple] => Steps[NewEndDomain, EndGraph, _]
  )(
    using newConverter: Converter.Aux[NewEndDomain, EndGraph]
  ): Steps[NewEndDomain, EndGraph, Labels] =
    new Steps[NewEndDomain, EndGraph, Labels](
      raw.repeat { rawTraversal =>
        repeatTraversal(
          new Steps[EndDomain, EndGraph, EmptyTuple](rawTraversal)
        ).raw
      }
    )

  /**
    Termination criteria for a repeat step.
    If used before the repeat step it as "while" characteristics.
    If used after the repeat step it as "do-while" characteristics
    */
  def until(
    untilTraversal: Steps[EndDomain, EndGraph, EmptyTuple] => Steps[_, _, _]
  ): Steps[EndDomain, EndGraph, Labels] =
    Steps[EndDomain, EndGraph, Labels](
      raw.until { rawTraversal =>
        untilTraversal(
          new Steps[EndDomain, EndGraph, EmptyTuple](rawTraversal)
        ).raw
      }
    )

  /**
    * Modifier for repeat steps. Configure the amount of times the repeat traversal is
    * executed.
    */
  def times(maxLoops: Int): Steps[EndDomain, EndGraph, Labels] =
    Steps[EndDomain, EndGraph, Labels](raw.times(maxLoops))

  /**
    Emit is used with the repeat step to emit the elements of the repeatTraversal after each
    iteration of the repeat loop.
    */
  def emit(): Steps[EndDomain, EndGraph, Labels] =
    Steps[EndDomain, EndGraph, Labels](raw.emit())

  /**
    Emit is used with the repeat step to emit the elements of the repeatTraversal after each
    iteration of the repeat loop.
    The emitTraversal defines under which condition the elements are emitted.
    */
  def emit(
    emitTraversal: Steps[EndDomain, EndGraph, EmptyTuple] => Steps[_, _, _]
  ): Steps[EndDomain, EndGraph, Labels] =
    Steps[EndDomain, EndGraph, Labels](
      raw.emit { rawTraversal =>
        emitTraversal(
          new Steps[EndDomain, EndGraph, EmptyTuple](rawTraversal)
        ).raw
      }
    )

  /**
    * The or step is a filter with multiple `or` related filter traversals.
    */
  def or(
    orTraversals: (Steps[EndDomain, EndGraph, EmptyTuple] => Steps[_, _, _])*
  ): Steps[EndDomain, EndGraph, Labels] = {
    val rawOrTraversals = orTraversals.map {
      orTraversal => (rawTraversal: GremlinScala[EndGraph]) =>
        orTraversal(
          new Steps[EndDomain, EndGraph, EmptyTuple](
            rawTraversal.asInstanceOf[GremlinScala.Aux[EndGraph, EmptyTuple]])
        ).raw
    }

    new Steps[EndDomain, EndGraph, Labels](
      raw.or(rawOrTraversals: _*)
    )
  }

  /**
    * The and step is a filter with multiple `and` related filter traversals.
    */
  def and(
    andTraversals: (Steps[EndDomain, EndGraph, EmptyTuple] => Steps[_, _, _])*
  ): Steps[EndDomain, EndGraph, Labels] = {
    val rawAndTraversals = andTraversals.map {
      andTraversal => (rawTraversal: GremlinScala[EndGraph]) =>
        andTraversal(
          new Steps[EndDomain, EndGraph, EmptyTuple](
            rawTraversal.asInstanceOf[GremlinScala.Aux[EndGraph, EmptyTuple]])
        ).raw
    }

    new Steps[EndDomain, EndGraph, Labels](
      raw.and(rawAndTraversals: _*)
    )
  }

  /**
    * Step that orders nodes according to f.
    * */
  def orderBy[A](fun: EndDomain => A): Steps[EndDomain, EndGraph, Labels] =
    new Steps[EndDomain, EndGraph, Labels](raw.order(By { (v: EndGraph) =>
      fun(converter.toDomain(v))
    }))

  override def toString = s"${getClass.getSimpleName}($raw)"
}
