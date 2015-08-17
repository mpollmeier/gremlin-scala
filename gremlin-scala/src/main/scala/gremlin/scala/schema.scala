package gremlin.scala

import org.apache.tinkerpop.gremlin.structure._

import scala.language.implicitConversions

object schema {
  /**
   * Smaller than an Element, consisting of the particles used
   * compose a Property or VertexProperty of an Element (Edge or Vertex)
   *
   * @param key
   */
  sealed abstract class Atom(val key: String) {
    def apply(n: Any): (String, Any) = key -> n
  }

  case class Key[A](override val key: String) extends Atom(key)

  object Label extends Atom(T.label.name)

  object ID extends Atom(T.id.name)

  implicit class AtomValue(p: (String, Any)) {
    def value[A] = p._2.asInstanceOf[A]
  }
}
