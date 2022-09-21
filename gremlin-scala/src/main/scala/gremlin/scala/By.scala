package gremlin.scala

import org.apache.tinkerpop.gremlin.process.traversal.Order
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.structure.T
import java.util.function.{Function => JFunction}
import java.util.{Collection => JCollection}

/**
  * By step can be used in combination with all sorts of other steps
  * e.g. group, groupCount, order, dedup, sack, ...
  * http://tinkerpop.apache.org/docs/current/reference/#by-step
  * n.b. `By` can be used in place of `OrderBy`, hence extending OrderBy */
trait By[Modulated] extends OrderBy[Modulated] {

  /** When used as the latter By of group method */
  type ValueFold[X]
  def apply[End](traversal: GraphTraversal[_, End]): GraphTraversal[_, End]
}
trait OrderBy[Modulated] {
  def apply[End](traversal: GraphTraversal[_, End]): GraphTraversal[_, End]
}

object By {

  /* modulate by property */
  def apply[Modulated](key: Key[Modulated]) = new By[Modulated] {
    override type ValueFold[A] = JCollection[A]
    override def apply[End](traversal: GraphTraversal[_, End]) =
      traversal.by(key.name)
  }

  /* modulate by property and order */
  def apply[Modulated](key: Key[Modulated], order: Order) =
    new OrderBy[Modulated] {
      override def apply[End](traversal: GraphTraversal[_, End]) =
        traversal.by(key.name, order)
    }

  /* modulate by label - alias for `apply[String](T.label)` */
  def label = new By[Label] {
    override type ValueFold[A] = JCollection[A]
    override def apply[End](traversal: GraphTraversal[_, End]) =
      traversal.by(T.label)
  }

  /* modulate by label and order - alias for `apply[String](T.label, Order)` */
  def label(order: Order) = new OrderBy[Label] {
    override def apply[End](traversal: GraphTraversal[_, End]) =
      traversal.by(T.label, order)
  }

  /* modulate by T(oken) */
  def apply[Modulated](token: T) = new By[Modulated] {
    override type ValueFold[A] = JCollection[A]
    override def apply[End](traversal: GraphTraversal[_, End]) =
      traversal.by(token)
  }

  /* modulate by T(oken) and order */
  def apply[Modulated](token: T, order: Order) = new OrderBy[Modulated] {
    override def apply[End](traversal: GraphTraversal[_, End]) =
      traversal.by(token, order)
  }

  /* modulate by anonymous traversal, e.g. __.inE.value(Name) */
  def apply[Modulated](by: GremlinScala[Modulated]) = new By[Modulated] {
    override type ValueFold[A] = A
    override def apply[End](traversal: GraphTraversal[_, End]) =
      traversal.by(by.traversal)
  }

  /* modulate by anonymous traversal and order, e.g. (__.inE.value(Name), Order.decr) */
  def apply[Modulated](by: GremlinScala[Modulated], order: Order) =
    new OrderBy[Modulated] {
      override def apply[End](traversal: GraphTraversal[_, End]) =
        traversal.by(by.traversal, order)
    }

  /* modulate by function
   * n.b. prefer one of the other modulators, see http://tinkerpop.apache.org/docs/current/reference/#a-note-on-lambdas */
  def apply[From, Modulated](fun: From => Modulated) = new By[Modulated] {
    override type ValueFold[A] = JCollection[A]
    override def apply[End](traversal: GraphTraversal[_, End]) =
      traversal.by[From](new JFunction[From, AnyRef] {
        override def apply(from: From): AnyRef = fun(from).asInstanceOf[AnyRef]
      })
  }

  /* modulate by function and order
   * n.b. prefer one of the other modulators, see http://tinkerpop.apache.org/docs/current/reference/#a-note-on-lambdas */
  def apply[From, Modulated](fun: From => Modulated, order: Order) =
    new OrderBy[Modulated] {
      override def apply[End](traversal: GraphTraversal[_, End]) =
        traversal.by[From](
          new JFunction[From, AnyRef] {
            override def apply(from: From): AnyRef =
              fun(from).asInstanceOf[AnyRef]
          },
          order
        )
    }

  def apply[Modulated](order: Order) = new OrderBy[Modulated] {
    override def apply[End](traversal: GraphTraversal[_, End]) =
      traversal.by(order)
  }

  /* identity modulator */
  def apply[Modulated]() = new By[Modulated] {
    override type ValueFold[A] = JCollection[A]
    override def apply[End](traversal: GraphTraversal[_, End]) = traversal.by()
  }
}
