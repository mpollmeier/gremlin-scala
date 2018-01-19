package gremlin.scala

import org.apache.tinkerpop.gremlin.structure.util.detached.DetachedFactory
import org.apache.tinkerpop.gremlin.structure.util.Attachable
import scala.collection.JavaConverters._

object GraphHelper {

  def cloneElements(original: ScalaGraph, clone: ScalaGraph): ScalaGraph = {
    cloneElements(original.graph, clone.graph).asScala
  }

  /**
    * make a deep clone of the graph elements that preserves ids
    */
  def cloneElements(original: Graph, clone: Graph): Graph = {
    original
      .vertices()
      .asScala
      .foreach(v =>
        DetachedFactory.detach(v, true).attach(Attachable.Method.create(clone)))
    original
      .edges()
      .asScala
      .foreach(e =>
        DetachedFactory.detach(e, true).attach(Attachable.Method.create(clone)))
    clone
  }
}
