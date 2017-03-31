package gremlin.scala

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource
import scala.collection.JavaConversions._
import shapeless._

trait ScalaElement[ElementType <: Element] {
  def element: ElementType

  def graph: ScalaGraph = element.graph

  /** start a new traversal from this element */
  def start(): GremlinScala[ElementType, HNil] = __(element)

  /** start a new traversal from this element and configure it */
  def start(configure: TraversalSource => TraversalSource): GremlinScala[ElementType, HNil] =
    GremlinScala[ElementType, HNil](
      configure(TraversalSource(element.graph))
      .underlying.inject(element)
    )

  def id[A: DefaultsToAny]: A = element.id.asInstanceOf[A]

  def label: String = element.label

  def keys: Set[Key[Any]] = element.keys.toSet.map(Key.apply[Any])

  def setProperty[A](key: Key[A], value: A): ElementType

  def removeProperty(key: Key[_]): ElementType

  def removeProperties(keys: Key[_]*): ElementType

  def property[A](key: Key[A]): Property[A] = element.property[A](key.name)

  def properties[A: DefaultsToAny]: Stream[Property[A]]

  def properties[A: DefaultsToAny](keys: String*): Stream[Property[A]]

  // note: this may throw an IllegalStateException - better use `valueOption` or `Property`
  def value[A: DefaultsToAny](key: String): A =
    element.value[A](key)

  // typesafe version of `value. have to call it `value2` because of a scala compiler bug :(
  // https://issues.scala-lang.org/browse/SI-9523
  def value2[A](key: Key[A]): A =
    element.value[A](key.name)

  def valueOption[A: DefaultsToAny](key: String): Option[A] =
    element.property[A](key).toOption

  def valueOption[A](key: Key[A]): Option[A] =
    element.property[A](key.name).toOption

  // note: this may throw an IllegalStateException - better use `Property`
  def values[A: DefaultsToAny](keys: String*): Iterator[A] =
    element.values[A](keys: _*)

  def valueMap[A: DefaultsToAny]: Map[String, A] = valueMap[A](keys.toSeq.map(_.name): _*)

  def valueMap[A: DefaultsToAny](keys: String*): Map[String, A] =
    (properties[A](keys: _*) map (p ⇒ (p.key, p.value))).toMap

  def toCC[CC <: Product: Marshallable]: CC

  def updateWith[CC <: Product: Marshallable](update: CC): ElementType = {
    val propMap = implicitly[Marshallable[CC]].fromCC(update).valueMap
    this.valueMap.keySet.diff(propMap.keySet) foreach { key =>
      val prop = element.property(key)
      if (prop.isPresent) prop.remove()
    }
    propMap foreach {case (key, value) => element.property(key, value)}

    element
  }

  def updateAs[CC <: Product: Marshallable](f: CC => CC): ElementType = updateWith(f(toCC[CC]))

}
