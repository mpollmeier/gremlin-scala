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

  class Key[A](override val key: String) extends Atom(key)

  object Label extends Atom(T.label.name)

  object Id extends Atom(T.id.name)

  object Atom {
    def apply(p: (String, Any)) = p._1 match {
      case "label" => Label
      case "id" => Id
      case a: String => new Key(a)
    }
  }

  implicit class AtomValue(p: (String, Any)) {
    def value[A] = p._2.asInstanceOf[A]
  }
}
