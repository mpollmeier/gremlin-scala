package gremlin.scala

import org.apache.tinkerpop.gremlin.structure.util.detached.DetachedFactory
import org.apache.tinkerpop.gremlin.structure.util.Attachable

object GraphHelper {

  def cloneElements(original: ScalaGraph, clone: ScalaGraph): Unit = {
    cloneElements(original.graph, clone.graph)
  }

  /**
   * make a deep clone of the graph elements that preserves ids
   */
  def cloneElements(original: Graph, clone: Graph): Unit = {
    original.vertices().forEachRemaining(v => DetachedFactory.detach(v, true).attach(Attachable.Method.create(clone)))
    original.edges().forEachRemaining(e => DetachedFactory.detach(e, true).attach(Attachable.Method.create(clone)))
  }
}
