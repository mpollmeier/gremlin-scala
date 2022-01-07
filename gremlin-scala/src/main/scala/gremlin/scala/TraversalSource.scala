package gremlin.scala

import java.util.function.{BinaryOperator, Supplier, UnaryOperator}
import org.apache.commons.configuration2.Configuration
import org.apache.tinkerpop.gremlin.process.remote.RemoteConnection
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource.traversal
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource
import shapeless.HNil

object TraversalSource {
  def apply(graph: Graph): TraversalSource =
    TraversalSource(graph.traversal())
}

case class TraversalSource(underlying: GraphTraversalSource) {
  def graph: Graph = underlying.getGraph

  def addV(): GremlinScala.Aux[Vertex, HNil] =
    GremlinScala[Vertex, HNil](underlying.addV())

  def addV(label: String): GremlinScala.Aux[Vertex, HNil] =
    GremlinScala[Vertex, HNil](underlying.addV(label))

  def addE(label: String): GremlinScala.Aux[Edge, HNil] =
    GremlinScala[Edge, HNil](underlying.addE(label))

  def inject[S](starts: S*): GremlinScala.Aux[S, HNil] =
    GremlinScala[S, HNil](underlying.inject(starts: _*))

  // start traversal with all vertices
  def V(): GremlinScala.Aux[Vertex, HNil] =
    GremlinScala[Vertex, HNil](underlying.V())

  // start traversal with all edges
  def E(): GremlinScala.Aux[Edge, HNil] =
    GremlinScala[Edge, HNil](underlying.E())

  // start traversal with some vertices identified by given ids
  def V(vertexIds: Any*): GremlinScala.Aux[Vertex, HNil] =
    GremlinScala[Vertex, HNil](underlying.V(vertexIds.asInstanceOf[Seq[AnyRef]]: _*))

  // start traversal with some edges identified by given ids
  def E(edgeIds: Any*): GremlinScala.Aux[Edge, HNil] =
    GremlinScala[Edge, HNil](underlying.E(edgeIds.asInstanceOf[Seq[AnyRef]]: _*))

  def withSack[A](initialValue: A): TraversalSource =
    withSack(() => initialValue)

  def withSack[A](initialValue: () => A) =
    TraversalSource(underlying.withSack(initialValue: Supplier[A]))

  def withSack[A](initialValue: A, splitOperator: A => A): TraversalSource =
    withSack(() => initialValue, splitOperator)

  def withSack[A](initialValue: () => A, splitOperator: A => A) =
    TraversalSource(underlying.withSack(initialValue: Supplier[A], splitOperator: UnaryOperator[A]))

  def withSack[A](initialValue: A, mergeOperator: (A, A) => A): TraversalSource =
    withSack(() => initialValue, mergeOperator)

  def withSack[A](initialValue: () => A, mergeOperator: (A, A) => A) =
    TraversalSource(
      underlying.withSack(initialValue: Supplier[A], mergeOperator: BinaryOperator[A]))

  def withSack[A](initialValue: A,
                  splitOperator: A => A,
                  mergeOperator: (A, A) => A): TraversalSource =
    withSack(() => initialValue, splitOperator, mergeOperator)

  def withSack[A](initialValue: () => A, splitOperator: A => A, mergeOperator: (A, A) => A) =
    TraversalSource(
      underlying.withSack(initialValue: Supplier[A],
                          splitOperator: UnaryOperator[A],
                          mergeOperator: BinaryOperator[A]))

  def withSideEffect[A](key: String, initialValue: A) =
    TraversalSource(underlying.withSideEffect(key, initialValue))

  def withSideEffect[A](key: String, initialValue: () => A) =
    TraversalSource(underlying.withSideEffect(key, initialValue: Supplier[A]))

  def withSideEffect[A](key: String, initialValue: A, reducer: (A, A) => A) =
    TraversalSource(underlying.withSideEffect(key, initialValue, reducer: BinaryOperator[A]))

  def withSideEffect[A](key: String, initialValue: () => A, reducer: (A, A) => A) =
    TraversalSource(
      underlying.withSideEffect(key, initialValue: Supplier[A], reducer: BinaryOperator[A]))

  def withRemote(configFile: String): TraversalSource =
    TraversalSource(traversal().withRemote(configFile))

  def withRemote(configuration: Configuration): TraversalSource =
    TraversalSource(traversal().withRemote(configuration))

  def withRemote(connection: RemoteConnection): TraversalSource =
    TraversalSource(traversal().withRemote(connection))
}
