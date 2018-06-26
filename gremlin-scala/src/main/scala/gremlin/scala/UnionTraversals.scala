package gremlin.scala

import java.util.{List => JList}
import shapeless.{::, HList, HNil}
import shapeless.ops.hlist.Prepend

/** helper class to construct a typed union step */
class UnionTraversals[Start, Ends <: HList](val travsUntyped: Seq[GremlinScala.Aux[Start, HNil] => GremlinScala[_]]) {

  def join[End](trav: GremlinScala.Aux[Start, HNil] => GremlinScala[End])(
      implicit p: Prepend[Ends, JList[End] :: HNil]): UnionTraversals[Start, p.Out] =
    new UnionTraversals[Start, p.Out](travsUntyped :+ trav)

}
