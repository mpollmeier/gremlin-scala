package gremlin.scala

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.structure.T
import java.util.function.{Function ⇒ JFunction}

/**
  * by step can be used in combination with all sorts of other steps, e.g. group, order, dedup, sack, ...
  * http://tinkerpop.apache.org/docs/current/reference/#by-step
  */
trait By[ByWhat] {
  def apply[End](traversal: GraphTraversal[_, End]): GraphTraversal[_, End]
}

object by {

  /* identity modulator */
  def apply[ByWhat]() = new By[ByWhat] {
    override def apply[End](traversal: GraphTraversal[_, End]) = traversal.by()
  }

  // def apply[ByWhat](token: T) = new By[ByWhat] {
  //   override def apply[End](traversal: GraphTraversal[_, End]) = traversal.by(token)
  // }

  /* modulate by property */
  def apply[ByWhat](key: Key[ByWhat]) = new By[ByWhat] {
    override def apply[End](traversal: GraphTraversal[_, End]) = traversal.by(key.name)
  }

  /* modulate by traversal 
   * note: cast here is a dirty hack, but we can't connect then End type of the start of byTraversal and `End`, 
   * so the calling site would have to specify type params which looks ugly and superfluous. only risk here is 
   * that caller is not restricted from doing non-element steps (e.g. call `.has()` on an integer). */
  def apply[ByWhat](byTraversal: GremlinScala[Element, _] => GremlinScala[ByWhat, _]) = new By[ByWhat] {
    override def apply[End](traversal: GraphTraversal[_, End]) =
      traversal.by(byTraversal(__[End].asInstanceOf[GremlinScala[Element, _]]).traversal)
  }

  /* modulate by java function
   * this is not called `apply` to discourage it's use (see http://tinkerpop.apache.org/docs/current/reference/#a-note-on-lambdas)
   * and because it conflicts with `apply(byTraversal)`
*/
  def function[From, ByWhat](fun: JFunction[From, ByWhat]) = new By[ByWhat] {
    override def apply[End](traversal: GraphTraversal[_, End]) = traversal.by[From](fun.asInstanceOf[JFunction[From, AnyRef]])
  }

}
