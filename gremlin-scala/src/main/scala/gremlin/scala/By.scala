package gremlin.scala

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal

/**
  * by step can be used in combination with all sorts of other steps, e.g. group, order, dedup, sack, ...
  * http://tinkerpop.apache.org/docs/current/reference/#by-step
  */
trait By[ByWhat, TraversalEnd] {
  def apply(traversal: GraphTraversal[_,TraversalEnd]): GraphTraversal[_,TraversalEnd]
}

object by {

  /* identity modulator */
  def apply[ByWhat, TraversalEnd]() = new By[ByWhat, TraversalEnd] {
    override def apply(traversal: GraphTraversal[_,TraversalEnd]) = traversal.by()
  }

  /* modulate by property */
  def apply[ByWhat, TraversalEnd](key: Key[ByWhat]) = new By[ByWhat, TraversalEnd] {
    override def apply(traversal: GraphTraversal[_,TraversalEnd]) = traversal.by(key.name)
  }

  /* modulate by traversal */
  def apply[ByWhat, TraversalEnd](byTraversal: GremlinScala[TraversalEnd, _] => GremlinScala[ByWhat, _]) = new By[ByWhat, TraversalEnd] {
    override def apply(traversal: GraphTraversal[_,TraversalEnd]) = traversal.by(byTraversal(__[TraversalEnd]).traversal)
  }
}
