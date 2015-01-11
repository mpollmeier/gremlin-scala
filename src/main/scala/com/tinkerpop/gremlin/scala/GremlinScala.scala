package com.tinkerpop.gremlin.scala

import java.lang.{ Long ⇒ JLong }
import java.util.function.{ Predicate ⇒ JPredicate, Consumer ⇒ JConsumer, BiPredicate, Supplier }
import java.util.{ Comparator, List ⇒ JList, Map ⇒ JMap, Collection ⇒ JCollection }

import collection.JavaConversions._
import collection.mutable
import com.tinkerpop.gremlin._
import com.tinkerpop.gremlin.process._
import com.tinkerpop.gremlin.process.graph.GraphTraversal
import com.tinkerpop.gremlin.process.T
import com.tinkerpop.gremlin.structure._
import shapeless.{ HList, HNil, :: }
import shapeless.ops.hlist.Prepend

case class GremlinScala[End, Labels <: HList](traversal: GraphTraversal[_, End]) {
  def toSeq(): Seq[End] = traversal.toList.toSeq
  def toList(): List[End] = traversal.toList.toList
  def toSet(): Set[End] = traversal.toList.toSet
  def head(): End = toList.head
  def headOption(): Option[End] = toList.headOption

  // execute pipeline - applies all side effects 
  def iterate() = {
    traversal.iterate()
    GremlinScala[End, Labels](traversal)
  }

  def cap() = GremlinScala[End, Labels](traversal.cap())
  def cap(sideEffectKey: String) = GremlinScala[End, Labels](traversal.cap(sideEffectKey))

  def filter(p: End ⇒ Boolean) = GremlinScala[End, Labels](traversal.filter(new JPredicate[Traverser[End]] {
    override def test(h: Traverser[End]): Boolean = p(h.get)
  }))

  def count() = GremlinScala[JLong, Labels](traversal.count())

  def map[A](fun: End ⇒ A) = GremlinScala[A, Labels](traversal.map[A] { t: Traverser[End] ⇒ fun(t.get) })

  def mapWithTraverser[A](fun: Traverser[End] ⇒ A) =
    GremlinScala[A, Labels](traversal.map[A](fun))

  def path() = GremlinScala[Path, Labels](traversal.path())

  // like path, but type safe and contains only the labelled steps - see `as` step and `LabelledPathSpec` 
  def labelledPath() = GremlinScala[Labels, Labels](traversal.asAdmin.addStep(new LabelledPathStep[End, Labels](traversal)))

  def select() = GremlinScala[JMap[String, End], Labels](traversal.select())

  def select[A, B](label: String) = GremlinScala[B, Labels](traversal.select(label))

  def select(stepLabels: Seq[String]) =
    GremlinScala[JMap[String, End], Labels](traversal.select(stepLabels: _*))

  def order() = GremlinScala[End, Labels](traversal.order())

  def shuffle() = GremlinScala[End, Labels](traversal.shuffle())

  def simplePath() = GremlinScala[End, Labels](traversal.simplePath())
  def cyclicPath() = GremlinScala[End, Labels](traversal.cyclicPath())

  def dedup() = GremlinScala[End, Labels](traversal.dedup())

  def aggregate() = GremlinScala[End, Labels](traversal.aggregate())
  def aggregate(sideEffectKey: String) = GremlinScala[End, Labels](traversal.aggregate(sideEffectKey))      

  def except(someObject: End) = GremlinScala[End, Labels](traversal.except(someObject))
  def except(list: Iterable[End]) = GremlinScala[End, Labels](traversal.except(list))
  // not named `except` because type End could be String 
  def exceptVar(variable: String) = GremlinScala[End, Labels](traversal.except(variable))

  // keeps element on a probabilistic base - probability range: 0.0 (keep none) - 1.0 - keep all 
  def coin(probability: Double) = GremlinScala[End, Labels](traversal.coin(probability))

  def range(low: Int, high: Int) = GremlinScala[End, Labels](traversal.range(low, high))

  def limit(limit: Long) = GremlinScala[End, Labels](traversal.limit(limit))

  def retain(variable: String) = GremlinScala[End, Labels](traversal.retain(variable))
  def retainOne(retainObject: End) = GremlinScala[End, Labels](traversal.retain(retainObject))
  def retainAll(retainCollection: Seq[End]) = GremlinScala[End, Labels](traversal.retain(retainCollection))

  // labels the current step and preserves the type - see `labelledPath` steps 
  def as(name: String)(implicit p: Prepend[Labels, End :: HNil]) = GremlinScala[End, p.Out](traversal.as(name))

  def back[A](to: String) = GremlinScala[A, Labels](traversal.back[A](to))

  def withSideEffect[A](key: String, value: A) =
    GremlinScala[End, Labels](
      traversal.withSideEffect(
        key,
        new Supplier[A] { override def get = value }
      )
    )

  def label() = GremlinScala[String, Labels](traversal.label())

  def identity() = GremlinScala[End, Labels](traversal.identity())

  def to(direction: Direction, edgeLabels: String*) =
    GremlinScala[Vertex, Labels](traversal.to(direction, edgeLabels: _*))

  def sideEffect(traverse: Traverser[End] ⇒ Any) =
    GremlinScala[End, Labels](traversal.sideEffect(
      new JConsumer[Traverser[End]] {
        override def accept(t: Traverser[End]) = traverse(t)
      })
    )

  def group() = GremlinScala[End, Labels](traversal.group())

  def group(sideEffectKey: String) = GremlinScala[End, Labels](traversal.group(sideEffectKey))

  // note that groupCount is a side effect step, other than the 'count' step..
  // https://groups.google.com/forum/#!topic/gremlin-users/5wXSizpqRxw
  def groupCount() = GremlinScala[End, Labels](traversal.groupCount())

  def groupCount(sideEffectKey: String) = GremlinScala[End, Labels](traversal.groupCount(sideEffectKey))

  def until(predicate: Traverser[End] ⇒ Boolean) =
    GremlinScala[End, Labels](traversal.until(predicate))

  def times(maxLoops: Int) = GremlinScala[End, Labels](traversal.times(maxLoops))

  def profile() = GremlinScala[End, Labels](traversal.profile)

  def sack[A]() = GremlinScala[A, Labels](traversal.sack[A])


  // by steps can be used in combination with all sorts of other steps, e.g. group, order, dedup, ...
  def by[A <: AnyRef](funProjection: End ⇒ A) = GremlinScala[End, Labels](traversal.by(funProjection))

  def by[A <: AnyRef](tokenProjection: T) = GremlinScala[End, Labels](traversal.by(tokenProjection))

  def by[A <: AnyRef](elementPropertyProjection: String) = GremlinScala[End, Labels](traversal.by(elementPropertyProjection))

  def by[A <: AnyRef](lessThan: (End, End) ⇒ Boolean) = 
    GremlinScala[End, Labels](traversal.by(new Comparator[End]() {
      override def compare(a: End, b: End) =
        if (lessThan(a, b)) -1
        else 0
    }))

  def `match`[A](startLabel: String, traversals: Seq[GremlinScala[_,_]]) =
    GremlinScala[JMap[String, A], Labels](
      traversal.`match`(
        startLabel,
        traversals map (_.traversal) : _*)
      )

  def unfold[A]() = GremlinScala[A, Labels](traversal.unfold())

  def fold() = GremlinScala[JList[End], Labels](traversal.fold())

  def inject(injections: End*) = GremlinScala[End, Labels](traversal.inject(injections: _*))
}

case class ScalaGraph(graph: Graph) extends AnyVal {
  def addVertex() = ScalaVertex(graph.addVertex())
  def addVertex(label: String) = ScalaVertex(graph.addVertex(label))
  def addVertex(label: String, properties: Map[String, Any]): ScalaVertex = {
    val v = addVertex(label)
    v.setProperties(properties)
    v
  }

  // get vertex by id
  def v(id: AnyRef): Option[ScalaVertex] =
    GremlinScala(graph.V(id)).headOption map ScalaVertex.apply

  // get edge by id
  def e(id: AnyRef): Option[ScalaEdge] = 
    GremlinScala(graph.E(id)).headOption map ScalaEdge.apply

  // start traversal with all vertices 
  def V = GremlinScala[Vertex, HNil](graph.V().asInstanceOf[GraphTraversal[_, Vertex]])
  // start traversal with all edges 
  def E = GremlinScala[Edge, HNil](graph.E().asInstanceOf[GraphTraversal[_, Edge]])

  // start traversal with some vertices identified by given ids 
  def V(vertexIds: Seq[AnyRef]) = GremlinScala[Vertex, HNil](graph.V(vertexIds: _*).asInstanceOf[GraphTraversal[_, Vertex]])

  // start traversal with some edges identified by given ids 
  def E(edgeIds: Seq[AnyRef]) = GremlinScala[Edge, HNil](graph.E(edgeIds: _*).asInstanceOf[GraphTraversal[_, Edge]])
}

object GS {
  // GS(graph) as a shorthand for GremlinScala(graph)
  def apply(graph: Graph) = GremlinScala(graph)
}

object GremlinScala {
  def apply(graph: Graph) = ScalaGraph(graph)

  class GremlinElementSteps[End <: Element, Labels <: HList](gremlinScala: GremlinScala[End, Labels])
      extends GremlinScala[End, Labels](gremlinScala.traversal) {

    def properties(keys: String*) =
      GremlinScala[Property[Any], Labels](traversal.properties(keys: _*)
        .asInstanceOf[GraphTraversal[_, Property[Any]]])

    def propertyMap(keys: String*) =
      GremlinScala[JMap[String, Any], Labels](traversal.propertyMap(keys: _*))

    def key() = GremlinScala[String, Labels](traversal.key)

    def value[A](key: String) =
      GremlinScala[A, Labels](traversal.values[A](key))

    def values[A](key: String*) =
      GremlinScala[A, Labels](traversal.values[A](key: _*))

    def valueMap(keys: String*) =
      GremlinScala[JMap[String, AnyRef], Labels](traversal.valueMap(keys: _*))

    def has(key: String) = GremlinScala[End, Labels](traversal.has(key))

    def has(key: String, value: Any) = GremlinScala[End, Labels](traversal.has(key, value))

    def has(accessor: T, value: Any) = GremlinScala[End, Labels](traversal.has(accessor, value))

    /* there can e.g. be one of: 
     * `(i: Int, s: String) ⇒ true` - there is an implicit conversion to BiPredicate in package.scala
     * com.tinkerpop.gremlin.structure.Compare.{eq, gt, gte, lt, lte, ...}
     * com.tinkerpop.gremlin.structure.Contains.{in, nin, ...}
     */
    def has(key: String, predicate: BiPredicate[_, _], value: Any) = GremlinScala[End, Labels](traversal.has(key, predicate, value))

    def has(key: String, predicate: BiPredicate[_, _], value: Seq[_]) = GremlinScala[End, Labels](traversal.has(key, predicate, asJavaCollection(value)))

    def has(accessor: T, predicate: BiPredicate[_, _], value: Any) = GremlinScala[End, Labels](traversal.has(accessor, predicate, value))

    def has(accessor: T, predicate: BiPredicate[_, _], value: Seq[_]) = GremlinScala[End, Labels](traversal.has(accessor, predicate, asJavaCollection(value)))

    def has(label: String, key: String, value: Any) =
      GremlinScala[End, Labels](traversal.has(label, key, value))

    def has(label: String, key: String, value: Seq[_]) = GremlinScala[End, Labels](traversal.has(label, key, asJavaCollection(value)))

    def has(label: String, key: String, predicate: BiPredicate[_, _], value: Any) = GremlinScala[End, Labels](traversal.has(label, key, predicate, value))

    def has(label: String, key: String, predicate: BiPredicate[_, _], value: Seq[_]) = GremlinScala[End, Labels](traversal.has(label, key, predicate, asJavaCollection(value)))

    def hasNot(key: String) = GremlinScala[End, Labels](traversal.hasNot(key))

    // startValue: greaterThanEqual,  endValue: less than
    def between[A, B](key: String, startValue: Comparable[A], endValue: Comparable[B]) =
      GremlinScala[End, Labels](traversal.between(key, startValue, endValue))

    def local[A](localTraversal: GremlinScala[A, _]) = GremlinScala[A, Labels](traversal.local(localTraversal.traversal))

    def timeLimit(millis: Long) = GremlinScala[End, Labels](traversal.timeLimit(millis))

  }

  class GremlinVertexSteps[End <: Vertex, Labels <: HList](gremlinScala: GremlinScala[End, Labels])
      extends GremlinScala[End, Labels](gremlinScala.traversal) {

    def out() = GremlinScala[Vertex, Labels](traversal.out())
    def out(labels: String*) = GremlinScala[Vertex, Labels](traversal.out(labels: _*))

    def outE() = GremlinScala[Edge, Labels](traversal.outE())
    def outE(labels: String*) = GremlinScala[Edge, Labels](traversal.outE(labels: _*))

    def in() = GremlinScala[Vertex, Labels](traversal.in())
    def in(labels: String*) = GremlinScala[Vertex, Labels](traversal.in(labels: _*))

    def inE() = GremlinScala[Edge, Labels](traversal.inE())
    def inE(labels: String*) = GremlinScala[Edge, Labels](traversal.inE(labels: _*))

    def both() = GremlinScala[Vertex, Labels](traversal.both())
    def both(labels: String*) = GremlinScala[Vertex, Labels](traversal.both(labels: _*))

    def bothE() = GremlinScala[Edge, Labels](traversal.bothE())
    def bothE(labels: String*) = GremlinScala[Edge, Labels](traversal.bothE(labels: _*))
  }

  class GremlinEdgeSteps[End <: Edge, Labels <: HList](gremlinScala: GremlinScala[End, Labels])
      extends GremlinScala[End, Labels](gremlinScala.traversal) {

    def inV = GremlinScala[Vertex, Labels](traversal.inV)

    def outV = GremlinScala[Vertex, Labels](traversal.outV)

    def bothV() = GremlinScala[Vertex, Labels](traversal.bothV())

    def otherV() = GremlinScala[Vertex, Labels](traversal.otherV())
  }
}
