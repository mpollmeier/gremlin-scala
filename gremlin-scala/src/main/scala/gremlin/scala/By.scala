package gremlin.scala

import org.apache.tinkerpop.gremlin.process.traversal.Order
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.structure.T
import shapeless.{HList, Poly2}
import java.util.function.{Function => JFunction}
import java.util.{Map => JMap}

/**
  * By step can be used in combination with all sorts of other steps
  * e.g. group, groupCount, order, dedup, sack, ...
  * http://tinkerpop.apache.org/docs/current/reference/#by-step
  * n.b. `By` can be used in place of `OrderBy`, hence extending OrderBy */
trait By[Modulated] extends OrderBy[Modulated] {
  def apply[End](traversal: GraphTraversal[_, End]): GraphTraversal[_, End]
}
trait OrderBy[Modulated] {
  def apply[End](traversal: GraphTraversal[_, End]): GraphTraversal[_, End]
}

object By {

  /* modulate by property */
  def apply[Modulated](key: Key[Modulated]) = new By[Modulated] {
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
  def label[Modulated] = new By[String] {
    override def apply[End](traversal: GraphTraversal[_, End]) =
      traversal.by(T.label)
  }

  /* modulate by label and order - alias for `apply[String](T.label, Order)` */
  def label[Modulated](order: Order) = new OrderBy[String] {
    override def apply[End](traversal: GraphTraversal[_, End]) =
      traversal.by(T.label, order)
  }

  /* modulate by T(oken) */
  def apply[Modulated](token: T) = new By[Modulated] {
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
   * n.b. you should better use one of the other modulators, see http://tinkerpop.apache.org/docs/current/reference/#a-note-on-lambdas */
  def apply[From, Modulated](fun: From => Modulated) = new By[Modulated] {
    override def apply[End](traversal: GraphTraversal[_, End]) =
      traversal.by[From](new JFunction[From, AnyRef] {
        override def apply(from: From): AnyRef = fun(from).asInstanceOf[AnyRef]
      })
  }

  /* modulate by function and order
   * n.b. you should better use one of the other modulators, see http://tinkerpop.apache.org/docs/current/reference/#a-note-on-lambdas */
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
    override def apply[End](traversal: GraphTraversal[_, End]) = traversal.by()
  }

  object combineModulatorWithValue extends Poly2 {
    implicit def atLabel[Value, Modulated, Label <: HList] = {
      at[(StepLabel[Value], By[Modulated]), (Label, JMap[String, Any])] {
        case (stepLabelByTuple, (acc, values)) =>
          (values.get(stepLabelByTuple._1.name).asInstanceOf[Modulated] :: acc, values)
      }
    }
  }
}
