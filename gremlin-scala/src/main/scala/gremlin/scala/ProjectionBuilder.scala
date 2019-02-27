package gremlin.scala

import java.util.{Map => JMap, UUID}
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import shapeless.ops.tuple.{Prepend => TuplePrepend}
import shapeless.syntax.std.tuple._

class ProjectionBuilder[T <: Product] private[gremlin] (
    labels: Seq[String],
    addBy: GraphTraversal[_, JMap[String, Any]] => GraphTraversal[_, JMap[String, Any]],
    buildResult: JMap[String, Any] => T) {

  def apply[U, TR <: Product](by: By[U])(
      implicit prepend: TuplePrepend.Aux[T, Tuple1[U], TR]): ProjectionBuilder[TR] = {
    val label = UUID.randomUUID().toString
    new ProjectionBuilder[TR](labels :+ label,
                              addBy.andThen(by.apply),
                              map => buildResult(map) :+ map.get(label).asInstanceOf[U])
  }

  def and[U, TR <: Product](by: By[U])(
      implicit prepend: TuplePrepend.Aux[T, Tuple1[U], TR]): ProjectionBuilder[TR] = apply(by)

  private[gremlin] def build(g: GremlinScala[_]): GremlinScala[T] = {
    GremlinScala(addBy(g.traversal.project(labels.head, labels.tail: _*))).map(buildResult)
  }
}

object ProjectionBuilder {
  def apply() = new ProjectionBuilder[Nil.type](Nil, scala.Predef.identity, _ => Nil)
}
