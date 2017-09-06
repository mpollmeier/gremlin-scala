package gremlin.scala

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
// import org.apache.tinkerpop.gremlin.structure.T
// import java.util.function.{Function ⇒ JFunction}

/**
  * by step can be used in combination with all sorts of other steps, e.g. group, order, dedup, sack, ...
  * http://tinkerpop.apache.org/docs/current/reference/#by-step
  */

case class ByKey[ByWhat](key: Key[ByWhat])
case class ByTraversal[End, ByWhat](traversal: GremlinScala[End, _] => GremlinScala[ByWhat, _])

// type class that applies a by modulator to a traversal
trait ByApplier[By] {
  def apply[End](by: By, traversal: GraphTraversal[_, End]): GraphTraversal[_, End]
}
object ByApplier { //type class instances

  implicit def byKey[ByWhat] = new ByApplier[ByKey[ByWhat]] { 
    def apply[End](by: ByKey[ByWhat], traversal: GraphTraversal[_, End]) = traversal.by(by.key.name)
  }

  implicit def byTraversal[End, ByWhat] = new ByApplier[ByTraversal[End, ByWhat]] {
    def apply(by: ByTraversal[End, ByWhat], traversal: GraphTraversal[_, End]) = traversal.by(by.traversal(__[End]).traversal)
  }

  // def apply[ByWhat, End](byTraversal: GremlinScala[End, _] => GremlinScala[ByWhat, _]) = new By[ByWhat] {
  //   override def apply[End2](traversal: GraphTraversal[_, End2]) = traversal.by(byTraversal(__[End]).traversal)
  //   /* TODO: add (implicit ev: End =:= End2) so we don't need to annotate types on call site 
  //    * to achieve that, we'll need a different structure. don't enforce apply via inheritance, but type class? */ 
  // }
}



object by {

  // /* identity modulator */
  // def apply[ByWhat]() = new By[ByWhat] {
  //   override def apply[End](traversal: GraphTraversal[_, End]) = traversal.by()
  // }

  // /* modulate by java function (also covers Tokens (e.g. T.label)) */
  // def apply[ByWhat](fun: JFunction[AnyRef, ByWhat]) = new By[ByWhat] {
  //   override def apply[End](traversal: GraphTraversal[_, End]) = traversal.by[ByWhat](fun)
  // }
  // // def apply[ByWhat](token: T) = new By[ByWhat] {
  // //   override def apply[End](traversal: GraphTraversal[_, End]) = traversal.by(token)
  // // }

  // /* modulate by property */
  // def apply[ByWhat](key: Key[ByWhat]) = new By[ByWhat] {
  //   override def apply[End](traversal: GraphTraversal[_, End]) = traversal.by(key.name)
  // }

  // /* modulate by traversal */
  // def apply[ByWhat, End](byTraversal: GremlinScala[End, _] => GremlinScala[ByWhat, _]) = new By[ByWhat] {
  //   override def apply[End2](traversal: GraphTraversal[_, End2]) = traversal.by(byTraversal(__[End]).traversal)
  //   /* TODO: add (implicit ev: End =:= End2) so we don't need to annotate types on call site 
  //    * to achieve that, we'll need a different structure. don't enforce apply via inheritance, but type class? */ 
  // }
}
