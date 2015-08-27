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
  sealed abstract class Atom[A](val key: String) {
    def apply(n: A): (String, A) = key -> n
  }

  case class Key[A](override val key: String) extends Atom[A](key)

  object Label extends Atom[String](T.label.name)

  object ID extends Atom[Any](T.id.name)

  implicit class AtomValue[A](p: (String, A)) {
    def key: String = p._1
    def value: A = p._2
  }
}
