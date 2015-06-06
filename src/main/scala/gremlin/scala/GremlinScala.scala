package gremlin.scala

import java.lang.{ Long ⇒ JLong }
import java.util.function.{ Predicate ⇒ JPredicate, Consumer ⇒ JConsumer, BiPredicate, Supplier }
import java.util.{ Comparator, List ⇒ JList, Map ⇒ JMap, Collection ⇒ JCollection, Iterator ⇒ JIterator }

import collection.JavaConversions._
import collection.mutable
import org.apache.tinkerpop.gremlin._
import org.apache.tinkerpop.gremlin.process._
import org.apache.tinkerpop.gremlin.process.traversal.Order
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__
import org.apache.tinkerpop.gremlin.process.traversal.Path
import org.apache.tinkerpop.gremlin.process.traversal.Scope
import org.apache.tinkerpop.gremlin.process.traversal.Traversal
import org.apache.tinkerpop.gremlin.structure._
import shapeless.{ HList, HNil, :: }
import shapeless.ops.hlist.Prepend
import scala.language.existentials

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

  def cap(sideEffectKey: String, sideEffectKeys: String*) =
    GremlinScala[End, Labels](traversal.cap(sideEffectKey, sideEffectKeys: _*))

  def filter(p: End ⇒ Boolean) = GremlinScala[End, Labels](traversal.filter(new JPredicate[Traverser[End]] {
    override def test(h: Traverser[End]): Boolean = p(h.get)
  }))

  def filterWithTraverser(p: Traverser[End] ⇒ Boolean) = GremlinScala[End, Labels](traversal.filter(new JPredicate[Traverser[End]] {
    override def test(h: Traverser[End]): Boolean = p(h)
  }))

  def count() = GremlinScala[JLong, Labels](traversal.count())
  def count(scope: Scope) = GremlinScala[JLong, Labels](traversal.count(scope))

  def map[A](fun: End ⇒ A) = GremlinScala[A, Labels](traversal.map[A] { t: Traverser[End] ⇒ fun(t.get) })

  def mapWithTraverser[A](fun: Traverser[End] ⇒ A) =
    GremlinScala[A, Labels](traversal.map[A](fun))

  def flatMap[A](fun: End ⇒ Iterable[A]) =
    GremlinScala[A, Labels](
      traversal.flatMap[A] { t: Traverser[End] ⇒
        fun(t.get).toIterator: JIterator[A]
      }
    )

  def flatMapWithTraverser[A](fun: Traverser[End] ⇒ Iterable[A]) =
    GremlinScala[A, Labels](
      traversal.flatMap[A] { e: Traverser[End] ⇒
        fun(e).toIterator: JIterator[A]
      }
    )

  def path() = GremlinScala[Path, Labels](traversal.path())

  // like path, but type safe and contains only the labelled steps - see `as` step and `LabelledPathSpec` 
  def labelledPath() = GremlinScala[Labels, Labels](traversal.asAdmin.addStep(new LabelledPathStep[End, Labels](traversal)))

  def select() = GremlinScala[JMap[String, End], Labels](traversal.select())

  def select[A, B](label: String) = GremlinScala[B, Labels](traversal.select(label))

  def select(stepLabels: Seq[String]) =
    GremlinScala[JMap[String, End], Labels](traversal.select(stepLabels: _*))

  def order() = GremlinScala[End, Labels](traversal.order())
  def order(scope: Scope) = GremlinScala[End, Labels](traversal.order(scope))

  def simplePath() = GremlinScala[End, Labels](traversal.simplePath())
  def cyclicPath() = GremlinScala[End, Labels](traversal.cyclicPath())

  def dedup() = GremlinScala[End, Labels](traversal.dedup())

  def aggregate(sideEffectKey: String) = GremlinScala[End, Labels](traversal.aggregate(sideEffectKey))

  // keeps element on a probabilistic base - probability range: 0.0 (keep none) - 1.0 - keep all 
  def coin(probability: Double) = GremlinScala[End, Labels](traversal.coin(probability))

  def range(low: Int, high: Int) = GremlinScala[End, Labels](traversal.range(low, high))
  def range(scope: Scope, low: Int, high: Int) =
    GremlinScala[End, Labels](traversal.range(scope, low, high))

  def limit(limit: Long) = GremlinScala[End, Labels](traversal.limit(limit))
  def limit(scope: Scope, limit: Long) = GremlinScala[End, Labels](traversal.limit(scope, limit))

  // labels the current step and preserves the type - see `labelledPath` steps 
  def as(name: String)(implicit p: Prepend[Labels, End :: HNil]) = GremlinScala[End, p.Out](traversal.as(name))

  def label() = GremlinScala[String, Labels](traversal.label())

  def identity() = GremlinScala[End, Labels](traversal.identity())

  def to(direction: Direction, edgeLabels: String*) =
    GremlinScala[Vertex, Labels](traversal.to(direction, edgeLabels: _*))

  def sideEffect(fun: End ⇒ Any) =
    GremlinScala[End, Labels](traversal.sideEffect(
      new JConsumer[Traverser[End]] {
        override def accept(t: Traverser[End]) = fun(t.get)
      }
    ))

  def sideEffectWithTraverser(fun: Traverser[End] ⇒ Any) =
    GremlinScala[End, Labels](traversal.sideEffect(
      new JConsumer[Traverser[End]] {
        override def accept(t: Traverser[End]) = fun(t)
      }
    ))

  def group[A, B]() = GremlinScala[JMap[A, B], Labels](traversal.group())

  def group(sideEffectKey: String) = GremlinScala[End, Labels](traversal.group(sideEffectKey))

  def groupCount[A]() = GremlinScala[JMap[A, JLong], Labels](traversal.groupCount())

  // note that groupCount is a side effect step, other than the 'count' step..
  // https://groups.google.com/forum/#!topic/gremlin-users/5wXSizpqRxw
  def groupCount(sideEffectKey: String) = GremlinScala[End, Labels](traversal.groupCount(sideEffectKey))

  def profile() = GremlinScala[End, Labels](traversal.profile)

  def sack[A]() = GremlinScala[A, Labels](traversal.sack[A])

  // by steps can be used in combination with all sorts of other steps, e.g. group, order, dedup, ...
  def by() = GremlinScala[End, Labels](traversal.by())

  def by[A <: AnyRef](funProjection: End ⇒ A) = GremlinScala[End, Labels](traversal.by(funProjection))

  def by(tokenProjection: T) = GremlinScala[End, Labels](traversal.by(tokenProjection))

  def by(elementPropertyKey: String) = GremlinScala[End, Labels](traversal.by(elementPropertyKey))

  def by(elementPropertyKey: String, order: Order) = GremlinScala[End, Labels](traversal.by(elementPropertyKey, order))

  def by(lessThan: (End, End) ⇒ Boolean) =
    GremlinScala[End, Labels](traversal.by(new Comparator[End]() {
      override def compare(a: End, b: End) =
        if (lessThan(a, b)) -1
        else 0
    }))

  //TODO: rename to by (without P)
  // type A is when the element property resolves to
  // e.g. if the property "name" resolves to a String you gotta supply [String] there...
  def byP[A](elementPropertyKey: String, lessThan: (A, A) ⇒ Boolean) =
    GremlinScala[End, Labels](traversal.by(elementPropertyKey, new Comparator[A]() {
      override def compare(a: A, b: A) =
        if (lessThan(a, b)) -1
        else 0
    }))

  def by(byTraversal: Traversal[_, _]) = GremlinScala[End, Labels](traversal.by(byTraversal))

  def by(order: Order) = GremlinScala[End, Labels](traversal.by(order))

  //TODO: rename to by
  def byTraversal[A](byTraversal: GremlinScala[End, HNil] ⇒ GremlinScala[A, _]) =
    GremlinScala[End, Labels](traversal.by(byTraversal(start).traversal))

  //TODO: rename to by 
  def byTraversal[A](byTraversal: GremlinScala[End, HNil] ⇒ GremlinScala[A, _], order: Order) =
    GremlinScala[End, Labels](traversal.by(byTraversal(start).traversal, order))

  def `match`[A](startLabel: String, traversals: Seq[GremlinScala[_, _]]) =
    GremlinScala[JMap[String, A], Labels](
      traversal.`match`(
        startLabel,
        traversals map (_.traversal): _*
      )
    )

  def unfold[A]() = GremlinScala[A, Labels](traversal.unfold())

  def fold() = GremlinScala[JList[End], Labels](traversal.fold())

  def inject(injections: End*) = GremlinScala[End, Labels](traversal.inject(injections: _*))

  def emit() = GremlinScala[End, Labels](traversal.emit())

  def emit(predicate: Traverser[End] ⇒ Boolean) = GremlinScala[End, Labels](traversal.emit(predicate))

  def branch(fun: End ⇒ Iterable[String]) =
    GremlinScala[End, Labels](traversal.branch { t: Traverser[End] ⇒
      fun(t.get): JCollection[String]
    })

  def branchWithTraverser(fun: Traverser[End] ⇒ Iterable[String]) =
    GremlinScala[End, Labels](traversal.branch { t: Traverser[End] ⇒
      fun(t): JCollection[String]
    })

  def union(traversals: GremlinScala[End, _]*) =
    GremlinScala[End, Labels](traversal.union(traversals map (_.traversal): _*))

  // repeats the provided anonymous traversal which starts at the current End
  // best combined with `times` or `until` step
  // e.g. gs.V(1).repeat(_.out).times(2)
  def repeat(repeatTraversal: GremlinScala[End, HNil] ⇒ GremlinScala[End, _]) =
    GremlinScala[End, Labels](traversal.repeat(repeatTraversal(start).traversal))

  def until(predicate: Traverser[End] ⇒ Boolean) =
    GremlinScala[End, Labels](traversal.until(predicate))

  def times(maxLoops: Int) = GremlinScala[End, Labels](traversal.times(maxLoops))

  def tree(sideEffectKey: String) = GremlinScala[End, Labels](traversal.tree(sideEffectKey))


  // would rather use asJavaCollection, but unfortunately there are some casts to java.util.List in the tinkerpop codebase...
  protected def toJavaList[A](i: Iterable[A]): JList[A] = i.toList

  protected def start[A] = GremlinScala[A, HNil](__.start[A]())
}

case class ScalaGraph(graph: Graph) {
  import scala.reflect.ClassTag
  import scala.reflect.runtime.universe._

  def addVertex() = ScalaVertex(graph.addVertex())
  def addVertex(label: String) = ScalaVertex(graph.addVertex(label))
  def addVertex(label: String, properties: Map[String, Any]): ScalaVertex = {
    val v = addVertex(label)
    v.setProperties(properties)
    v
  }

  // get vertex by id
  def v(id: AnyRef): Option[ScalaVertex] =
    GremlinScala(graph.traversal.V(id)).headOption map ScalaVertex.apply

  // get edge by id
  def e(id: AnyRef): Option[ScalaEdge] =
    GremlinScala(graph.traversal.E(id)).headOption map ScalaEdge.apply

  // start traversal with all vertices 
  def V = GremlinScala[Vertex, HNil](graph.traversal.V().asInstanceOf[GraphTraversal[_, Vertex]])
  // start traversal with all edges 
  def E = GremlinScala[Edge, HNil](graph.traversal.E().asInstanceOf[GraphTraversal[_, Edge]])

  // start traversal with some vertices identified by given ids 
  def V(vertexIds: AnyRef*) = GremlinScala[Vertex, HNil](graph.traversal.V(vertexIds: _*).asInstanceOf[GraphTraversal[_, Vertex]])

  // start traversal with some edges identified by given ids 
  def E(edgeIds: AnyRef*) = GremlinScala[Edge, HNil](graph.traversal.E(edgeIds: _*).asInstanceOf[GraphTraversal[_, Edge]])

  // save an object's values into a new vertex
  def save[A: TypeTag: ClassTag](cc: A): ScalaVertex = {
    val persistableType = Seq(
      typeOf[Option.type],
      typeOf[String],
      typeOf[Int],
      typeOf[Double],
      typeOf[Float],
      typeOf[Long],
      typeOf[Short],
      typeOf[Char],
      typeOf[Byte]
    ) map (_.typeSymbol.fullName)

    val mirror = runtimeMirror(getClass.getClassLoader)
    val instanceMirror = mirror.reflect(cc)

    // TODO: when we don't need to support scala 2.10 any more, change to: typeOf[A].declarations
    val params = (typeOf[A].declarations map (_.asTerm) filter (t ⇒ t.isParamAccessor && t.isGetter) map { term ⇒
      val termName = term.name.decodedName.toString
      val termType = term.typeSignature.typeSymbol.fullName
      if (!persistableType.contains(termType))
        throw new IllegalArgumentException(s"The field '$termName: $termType' is not persistable.")

      val fieldMirror = instanceMirror.reflectField(term)
      termName → (term.typeSignature.typeSymbol.fullName match {
        case t if t == typeOf[Option.type].typeSymbol.fullName ⇒
          fieldMirror.get.asInstanceOf[Option[Any]].orNull
        case _ ⇒ fieldMirror.get
      })
    } filter { case (key, value) ⇒ key != "id" && value != null }).toMap + ("label" → cc.getClass.getSimpleName)

    addVertex().setProperties(params)
  }
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

    def has(key: String, propertyTraversal: GremlinScala[End, HNil] ⇒ GremlinScala[End, _]) =
      GremlinScala[End, Labels](traversal.has(key, propertyTraversal(start).traversal))

    /* there can e.g. be one of:
     * `(i: Int, s: String) ⇒ true` - there is an implicit conversion to BiPredicate in package.scala
     * org.apache.tinkerpop.gremlin.structure.Compare.{eq, gt, gte, lt, lte, ...}
     * org.apache.tinkerpop.gremlin.structure.Contains.{in, nin, ...}
     */
    // def has(key: String, predicate: BiPredicate[_, _], value: Any) =
    //   GremlinScala[End, Labels](traversal.has(key, predicate, value))

    // def has(key: String, predicate: BiPredicate[_, _], value: Seq[_]) =
    //   GremlinScala[End, Labels](traversal.has(key, predicate, toJavaList(value)))

    // def has(accessor: T, predicate: BiPredicate[_, _], value: Any) =
    //   GremlinScala[End, Labels](traversal.has(accessor, predicate, value))

    // def has(accessor: T, predicate: BiPredicate[_, _], value: Seq[_]) =
    //   GremlinScala[End, Labels](traversal.has(accessor, predicate, toJavaList(value)))

    def has(label: String, key: String, value: Any) =
      GremlinScala[End, Labels](traversal.has(label, key, value))

    // def has(label: String, key: String, value: Seq[_]) =
    //   GremlinScala[End, Labels](traversal.has(label, key, toJavaList(value)))

    // def has(label: String, key: String, predicate: BiPredicate[_, _], value: Any) =
    //   GremlinScala[End, Labels](traversal.has(label, key, predicate, value))

    // def has(label: String, key: String, predicate: BiPredicate[_, _], value: Seq[_]) =
    //   GremlinScala[End, Labels](traversal.has(label, key, predicate, toJavaList(value)))

    def hasId(ids: AnyRef*) = GremlinScala[End, Labels](traversal.hasId(ids: _*))

    def hasLabel(labels: String*) = GremlinScala[End, Labels](traversal.hasLabel(labels: _*))

    def hasKey(keys: String*) = GremlinScala[End, Labels](traversal.hasKey(keys: _*))

    def hasValue(values: String*) = GremlinScala[End, Labels](traversal.hasValue(values: _*))

    def hasNot(key: String) = GremlinScala[End, Labels](traversal.hasNot(key))

    def local[A](localTraversal: GremlinScala[End, HNil] ⇒ GremlinScala[A, _]) =
      GremlinScala[A, Labels](traversal.local(localTraversal(start).traversal))

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
