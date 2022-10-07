package gremlin.scala

import java.util.{List => JList}

/** helper class to construct a typed union step */
class UnionTraversals[Start, Ends <: Tuple](
  val travsUntyped: Seq[GremlinScala.Aux[Start, EmptyTuple] => GremlinScala[_]]
) {
  def join[End](
    trav: GremlinScala.Aux[Start, EmptyTuple] => GremlinScala[End]
  ): UnionTraversals[Start, Tuple.Append[Ends, JList[End]]] =
    UnionTraversals[Start, Tuple.Append[Ends, JList[End]]](travsUntyped :+ trav)
}
