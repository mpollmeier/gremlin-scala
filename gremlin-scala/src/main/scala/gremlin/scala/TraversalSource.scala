package gremlin.scala

import java.util.function.{ BinaryOperator, Supplier, UnaryOperator }

import org.apache.commons.configuration.Configuration
import org.apache.tinkerpop.gremlin.process.remote.RemoteConnection
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource

object TraversalSource {
  def apply(graph: Graph): TraversalSource =
    TraversalSource(new GraphTraversalSource(graph))
}

case class TraversalSource(underlying: GraphTraversalSource) {
  def graph: Graph = underlying.getGraph

  def withSack[A](initialValue: () => A) =
    TraversalSource(underlying.withSack(initialValue: Supplier[A]))

  def withSack[A](initialValue: () => A, splitOperator: A => A) =
    TraversalSource(underlying.withSack(initialValue: Supplier[A], splitOperator: UnaryOperator[A]))

  def withSack[A](initialValue: () => A, mergeOperator: (A, A) => A) =
    TraversalSource(underlying.withSack(initialValue: Supplier[A], mergeOperator: BinaryOperator[A]))

  def withSack[A](initialValue: () => A, splitOperator: A => A, mergeOperator: (A, A) => A) =
    TraversalSource(underlying.withSack(initialValue: Supplier[A], splitOperator: UnaryOperator[A], mergeOperator: BinaryOperator[A]))

  def withSack[A](initialValue: A): TraversalSource =
    withSack(() => initialValue)

  def withSack[A](initialValue: A, splitOperator: A => A): TraversalSource =
    withSack(() => initialValue, splitOperator)

  def withSack[A](initialValue: A, mergeOperator: (A, A) => A): TraversalSource =
    withSack(() => initialValue, mergeOperator)

  def withSack[A](initialValue: A, splitOperator: A => A, mergeOperator: (A, A) => A): TraversalSource =
    withSack(() => initialValue, splitOperator, mergeOperator)

  def withRemote(configFile: String): TraversalSource =
    TraversalSource(underlying.withRemote(configFile))

  def withRemote(configuration: Configuration): TraversalSource =
    TraversalSource(underlying.withRemote(configuration))

  def withRemote(connection: RemoteConnection): TraversalSource =
    TraversalSource(underlying.withRemote(connection))
}
