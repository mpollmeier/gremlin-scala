package com.tinkerpop.gremlin.scala

import shapeless._
import ops.hlist._
import scala.collection.JavaConversions._
import com.tinkerpop.gremlin._
import com.tinkerpop.gremlin.structure._
import com.tinkerpop.gremlin.process._
import java.util.Comparator
import java.util.function.Predicate

case class GremlinScala[Types <: HList, End](traversal: Traversal[_, End]) {
  def toList(): List[End] = traversal.toList.toList
  def toSet(): Set[End] = traversal.toList.toSet
  def head(): End = toList.head
  def headOption(): Option[End] = Option(head)

  def map[A](fun: Holder[End] => A)(implicit p:Prepend[Types, A::HNil]) =
    GremlinScala[p.Out, A](traversal.map[A](fun))

  def property[A](key: String)(implicit p:Prepend[Types, Property[A]::HNil]) =
    GremlinScala[p.Out, Property[A]](traversal.property[A](key))

  def value[A](key: String)(implicit p:Prepend[Types, A::HNil]) =
    GremlinScala[p.Out, A](traversal.value[A](key))
  def value[A](key: String, default: A)(implicit p:Prepend[Types, A::HNil]) =
    GremlinScala[p.Out, A](traversal.value[A](key, default))

  def path()(implicit p:Prepend[Types, Types::HNil]) =
    GremlinScala[p.Out, Types](traversal.addStep(new TypedPathStep[End, Types](traversal)))

  def order() = GremlinScala[Types, End](traversal.order())
  def order(lessThan: (End, End) => Boolean) =
    GremlinScala[Types, End](traversal.order(new Comparator[Holder[End]]() {
      override def compare(a: Holder[End], b: Holder[End]) = 
        if(lessThan(a.get,b.get)) -1
        else 0
    }))

  def shuffle() = GremlinScala[Types, End](traversal.shuffle())

  def filter(p: End => Boolean) = GremlinScala[Types, End](traversal.filter(new Predicate[Holder[End]] {
      override def test(h: Holder[End]): Boolean = p(h.get)
  }))

  def dedup() = GremlinScala[Types, End](traversal.dedup())
}

case class ScalaGraph(graph: Graph) {
  def addVertex(): ScalaVertex = ScalaVertex(graph.addVertex())
  def addVertex(id: AnyRef): ScalaVertex = addVertex(id, Map.empty)
  def addVertex(id: AnyRef, properties: Map[String, Any]): ScalaVertex = {
     val v = ScalaVertex(graph.addVertex(Element.ID, id))
     v.setProperties(properties)
     v
  }

  /** get vertex by id */
  def v(id: AnyRef): Option[ScalaVertex] = graph.v(id) match {
    case v: Vertex ⇒  Some(ScalaVertex(v))
    case _ ⇒  None
  }

  /** get edge by id */
  def e(id: AnyRef): Option[ScalaEdge] = graph.e(id) match {
    case e: Edge ⇒  Some(ScalaEdge(e))
    case _ ⇒  None
  }

  /** get all vertices */
  def V() = GremlinScala[Vertex :: HNil, Vertex](graph.V.asInstanceOf[Traversal[_, Vertex]])
  /** get all edges */
  def E() = GremlinScala[Edge :: HNil, Edge](graph.E.asInstanceOf[Traversal[_, Edge]])
}

trait ScalaElement {
  def element: Element

  def id: AnyRef = element.getId

  def property[A](key: String): Property[A] = element.getProperty[A](key)
  def propertyKeys(): Set[String] = element.getPropertyKeys.toSet
  def properties: Map[String, Any] = element.getProperties.toMap mapValues (_.get)
  def setProperty(key: String, value: Any): Unit = element.setProperty(key, value)
  def setProperties(properties: Map[String, Any]): Unit = 
    properties foreach { case (k,v) => setProperty(k,v) }
  def removeProperty(key: String): Unit = {
    val p = property(key)
    if(p.isPresent) p.remove
  }

  /** note: this may throw an IllegalStateException!
    * in scala exceptions are typically discouraged in situations like this...
    * `value` is only provided so that we are on par with Gremlin Groovy */
  def value[A](key: String): A = element.getValue[A](key)
  def value[A](key: String, default: A): A = property[A](key).orElse(default)

  def remove(): Unit = element.remove()
}

case class ScalaVertex(vertex: Vertex) extends ScalaElement {
  override def element = vertex

  def out() = GremlinScala[Vertex :: Vertex :: HNil, Vertex](vertex.out())
  def out(labels: String*) = GremlinScala[Vertex :: Vertex :: HNil, Vertex](vertex.out(labels: _*))
  def out(branchFactor: Int, labels: String*) = GremlinScala[Vertex :: Vertex :: HNil, Vertex](vertex.out(branchFactor, labels: _*))

  def outE() = GremlinScala[Vertex :: Edge :: HNil, Edge](vertex.outE())
  def outE(labels: String*) = GremlinScala[Vertex :: Edge :: HNil, Edge](vertex.outE(labels: _*))
  def outE(branchFactor: Int, labels: String*) = GremlinScala[Vertex :: Edge :: HNil, Edge](vertex.outE(branchFactor, labels: _*))

  def in() = GremlinScala[Vertex :: Vertex :: HNil, Vertex](vertex.in())
  def in(labels: String*) = GremlinScala[Vertex :: Vertex :: HNil, Vertex](vertex.in(labels: _*))
  def in(branchFactor: Int, labels: String*) = GremlinScala[Vertex :: Vertex :: HNil, Vertex](vertex.in(branchFactor, labels: _*))

  def inE() = GremlinScala[Vertex :: Edge :: HNil, Edge](vertex.inE())
  def inE(labels: String*) = GremlinScala[Vertex :: Edge :: HNil, Edge](vertex.inE(labels: _*))
  def inE(branchFactor: Int, labels: String*) = GremlinScala[Vertex :: Edge :: HNil, Edge](vertex.inE(branchFactor, labels: _*))

  def both() = GremlinScala[Vertex :: Vertex :: HNil, Vertex](vertex.both())
  def both(labels: String*) = GremlinScala[Vertex :: Vertex :: HNil, Vertex](vertex.both(labels: _*))
  def both(branchFactor: Int, labels: String*) = GremlinScala[Vertex :: Vertex :: HNil, Vertex](vertex.both(branchFactor, labels: _*))

  def bothE() = GremlinScala[Vertex :: Edge :: HNil, Edge](vertex.bothE())
  def bothE(labels: String*) = GremlinScala[Vertex :: Edge :: HNil, Edge](vertex.bothE(labels: _*))
  def bothE(branchFactor: Int, labels: String*) = GremlinScala[Vertex :: Edge :: HNil, Edge](vertex.bothE(branchFactor, labels: _*))

  def addEdge(label: String, inVertex: ScalaVertex, properties: Map[String, Any] = Map.empty): ScalaEdge = {
    val e = ScalaEdge(vertex.addEdge(label, inVertex.vertex))
    e.setProperties(properties)
    e
  }
}

case class ScalaEdge(edge: Edge) extends ScalaElement {
  override def element = edge

  def label(): String = edge.getLabel

  //TODO: wait until this is consistent in T3 between Vertex and Edge
  //currently Vertex.outE returns a Traversal, Edge.inV doesnt quite exist
  //def inV() = GremlinScala[Edge :: Vertex :: HNil, Vertex](edge.inV())
}

object GremlinScala {
  def of(graph: Graph): ScalaGraph = ScalaGraph(graph)

  class GremlinVertexSteps[Types <: HList, End <: Vertex](gremlinScala: GremlinScala[Types, End])
    extends GremlinScala[Types, End](gremlinScala.traversal) {

    def out()(implicit p:Prepend[Types, Vertex::HNil]) = GremlinScala[p.Out, Vertex](traversal.out())
    def out(labels: String*)(implicit p:Prepend[Types, Vertex::HNil]) = GremlinScala[p.Out, Vertex](traversal.out(labels: _*))
    def out(branchFactor: Int, labels: String*)(implicit p:Prepend[Types, Vertex::HNil]) = GremlinScala[p.Out, Vertex](traversal.out(branchFactor, labels: _*))

    def outE()(implicit p:Prepend[Types, Edge::HNil]) = GremlinScala[p.Out, Edge](traversal.outE())
    def outE(labels: String*)(implicit p:Prepend[Types, Edge::HNil]) = GremlinScala[p.Out, Edge](traversal.outE(labels: _*))
    def outE(branchFactor: Int, labels: String*)(implicit p:Prepend[Types, Edge::HNil]) = GremlinScala[p.Out, Edge](traversal.outE(branchFactor, labels: _*))

    def in()(implicit p:Prepend[Types, Vertex::HNil]) = GremlinScala[p.Out, Vertex](traversal.in())
    def in(labels: String*)(implicit p:Prepend[Types, Vertex::HNil]) = GremlinScala[p.Out, Vertex](traversal.in(labels: _*))
    def in(branchFactor: Int, labels: String*)(implicit p:Prepend[Types, Vertex::HNil]) = GremlinScala[p.Out, Vertex](traversal.in(branchFactor, labels: _*))

    def inE()(implicit p:Prepend[Types, Edge::HNil]) = GremlinScala[p.Out, Edge](traversal.inE())
    def inE(labels: String*)(implicit p:Prepend[Types, Edge::HNil]) = GremlinScala[p.Out, Edge](traversal.inE(labels: _*))
    def inE(branchFactor: Int, labels: String*)(implicit p:Prepend[Types, Edge::HNil]) = GremlinScala[p.Out, Edge](traversal.inE(branchFactor, labels: _*))

    def both()(implicit p:Prepend[Types, Vertex::HNil]) = GremlinScala[p.Out, Vertex](traversal.both())
    def both(labels: String*)(implicit p:Prepend[Types, Vertex::HNil]) = GremlinScala[p.Out, Vertex](traversal.both(labels: _*))
    def both(branchFactor: Int, labels: String*)(implicit p:Prepend[Types, Vertex::HNil]) = GremlinScala[p.Out, Vertex](traversal.both(branchFactor, labels: _*))

    def bothE()(implicit p:Prepend[Types, Edge::HNil]) = GremlinScala[p.Out, Edge](traversal.bothE())
    def bothE(labels: String*)(implicit p:Prepend[Types, Edge::HNil]) = GremlinScala[p.Out, Edge](traversal.bothE(labels: _*))
    def bothE(branchFactor: Int, labels: String*)(implicit p:Prepend[Types, Edge::HNil]) = GremlinScala[p.Out, Edge](traversal.bothE(branchFactor, labels: _*))
  }

  class GremlinEdgeSteps[Types <: HList, End <: Edge](gremlinScala: GremlinScala[Types, End])
    extends GremlinScala[Types, End](gremlinScala.traversal) {

    //def inV(implicit p:Prepend[Types, Vertex::HNil]) = 
      //GremlinScala[p.Out, Vertex](traversal.inV)
  }
}
