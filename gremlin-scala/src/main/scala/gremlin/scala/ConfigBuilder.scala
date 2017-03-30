package gremlin.scala

import java.util.function.{ BinaryOperator, Supplier, UnaryOperator }
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource

case class TraversalSource(underlying: GraphTraversalSource) {
  
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
}
