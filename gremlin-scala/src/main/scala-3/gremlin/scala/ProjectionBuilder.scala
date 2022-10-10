package gremlin.scala

import java.util.{Map => JMap, UUID}
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal

class ProjectionBuilder[T <: Tuple] private[gremlin] (
  labels: Seq[String],
  addBy: GraphTraversal[_, JMap[String, Any]] => GraphTraversal[_, JMap[String, Any]],
  buildResult: JMap[String, Any] => T
) {

  def apply[U, TR <: Product](by: By[U]): ProjectionBuilder[Tuple.Append[T, U]] = {
    val label = UUID.randomUUID().toString
    new ProjectionBuilder[Tuple.Append[T, U]](
      labels :+ label,
      addBy.andThen(by.apply),
      map => buildResult(map) :* map.get(label).asInstanceOf[U]
    )
  }

  def and[U](by: By[U]): ProjectionBuilder[Tuple.Append[T, U]] = apply(by)

  private[gremlin] def build(g: GremlinScala[_]): GremlinScala[T] = {
    GremlinScala(addBy(g.traversal.project(labels.head, labels.tail: _*))).map(buildResult)
  }
}

object ProjectionBuilder {
  def apply() = new ProjectionBuilder[Nil.type](Nil, scala.Predef.identity, _ => Nil)
}
