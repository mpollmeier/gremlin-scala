package gremlin.scala

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal

/**
  * by step can be used in combination with all sorts of other steps, e.g. group, order, dedup, sack, ...
  * http://tinkerpop.apache.org/docs/current/reference/#by-step
  */
trait By[A] {
  def apply[End](traversal: GraphTraversal[_, End]): GraphTraversal[_, End]
}

object by {
  def apply[A](key: Key[A]) = new By[A] {
    override def apply[End](traversal: GraphTraversal[_, End]) = traversal.by(key.name)
  }
}
