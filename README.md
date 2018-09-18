![logo](https://github.com/mpollmeier/gremlin-scala/raw/master/doc/images/gremlin-scala-logo.png)

[![Build Status](https://secure.travis-ci.org/mpollmeier/gremlin-scala.png?branch=master)](http://travis-ci.org/mpollmeier/gremlin-scala)
[![Join the chat at https://gitter.im/mpollmeier/gremlin-scala](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/mpollmeier/gremlin-scala?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Scaladex](https://index.scala-lang.org/mpollmeier/gremlin-scala/gremlin-scala/latest.svg)](https://index.scala-lang.org/mpollmeier/gremlin-scala/gremlin-scala/)
[![scaladoc](http://www.javadoc.io/badge/com.michaelpollmeier/gremlin-scala_2.12.svg?color=blue&label=scaladoc)](http://www.javadoc.io/doc/com.michaelpollmeier/gremlin-scala_2.12)

<!-- [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.michaelpollmeier/gremlin-scala_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.michaelpollmeier/gremlin-scala_2.12) -->
## Gremlin-Scala for Apache Tinkerpop 3

A wrapper to use [Apache Tinkerpop3](https://github.com/apache/incubator-tinkerpop) - a JVM graph traversal library - from Scala.

* Beautiful DSL to create vertices and edges
* Type safe traversals
* Scala friendly function signatures
* Minimal runtime overhead - only allocates additional instances if absolutely necessary
* Nothing is hidden away, you can always easily access the underlying Gremlin-Java objects if needed, e.g. to access graph db specifics things like indexes

### TOC
<!-- markdown-toc --maxdepth 2 --no-firsth1 README.md -->
- [Getting started](#getting-started)
- [Using the sbt console](#using-the-sbt-console)
- [Simple traversals](#simple-traversals)
- [Vertices and edges with type safe properties](#vertices-and-edges-with-type-safe-properties)
- [Compiler helps to eliminate invalid traversals](#compiler-helps-to-eliminate-invalid-traversals)
- [Type safe traversals](#type-safe-traversals)
- [A note on predicates](#a-note-on-predicates)
- [Build a custom DSL on top of Gremlin-Scala](#build-a-custom-dsl-on-top-of-gremlin-scala)
- [Common and useful steps](#common-and-useful-steps)
- [Mapping vertices from/to case classes](#mapping-vertices-fromto-case-classes)
- [More advanced traversals](#more-advanced-traversals)
- [Serialise to and from files](#serialise-to-and-from-files)
- [Help - it's open source!](#help---its-open-source)
- [Why such a long version number?](#why-such-a-long-version-number)
- [Further reading](#further-reading)
- [Random things worth knowing](#random-things-worth-knowing)
- [Releases](#releases)
- [Breaking changes](#breaking-changes)

### Getting started
The [examples project](https://github.com/mpollmeier/gremlin-scala-examples) comes with working examples for different graph databases. Typically you just need to add a dependency on `"com.michaelpollmeier" %% "gremlin-scala" % "SOME_VERSION"` and one for the graph db of your choice to your `build.sbt` (this readme assumes tinkergraph). The latest version is displayed at the top of this readme in the maven badge. 

### Using the sbt console
* `sbt gremlin-scala/Test/console`
```scala
import gremlin.scala._
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory
implicit val graph = TinkerFactory.createModern.asScala

val name = Key[String]("name")

val g = graph.traversal
g.V.hasLabel("person").value(name).toList
// List(marko, vadas, josh, peter)
```

### Simple traversals

The below create traversals, which are lazy computations. To run a traversal, you can use e.g. `toSet`, `toList`, `head`, `headOption` etc. 

```scala
import gremlin.scala._
import org.apache.tinkerpop.gremlin.process.traversal.{Order, P}
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory

implicit val graph = TinkerFactory.createModern.asScala
val g = graph.traversal

g.V //all vertices
g.E //all edges

g.V(1).outE("knows") //follow outgoing edges
g.V(1).out("knows") //follow outgoing edges to incoming vertex

val weight = Key[Double]("weight")
for {
  person <- g.V.hasLabel("person")
  favorite <- person.outE("likes").order(By(weight, Order.decr)).limit(1).inV
} yield (person, favorite.label)

// remove all people over 30 from the g - also removes corresponding edges
val Age = Key[Int]("age")
g.V.hasLabel("person").has(Age, P.gte(30)).drop.iterate
```

Warning: GremlinScala is _not_ a monad, because the underlying Tinkerpop GraphTraversal is not. I.e. while GremlinScala offers `map`, `flatMap` etc. and you can use it in a for comprehension for syntactic sugar, it does not fulfil all monad laws. 

More working examples in [TraversalSpec](https://github.com/mpollmeier/gremlin-scala/blob/master/gremlin-scala/src/test/scala/gremlin/scala/TraversalSpec.scala).

### Vertices and edges with type safe properties

```scala
import gremlin.scala._
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
implicit val graph = TinkerGraph.open.asScala

// Keys for properties which can later be used for type safe traversals
val Founded = Key[String]("founded")
val Distance = Key[Int]("distance")

// create labelled vertex
val paris = graph + "Paris"

// create vertex with typed properties
val london = graph + ("London", Founded -> "43 AD")

// create labelled edges 
paris --- "OneWayRoad" --> london
paris <-- "OtherWayAround" --- london
paris <-- "Eurostar" --> london

// create edge with typed properties
paris --- ("Eurostar", Distance -> 495) --> london

// type safe access to properties
paris.out("Eurostar").value(Founded).head //43 AD
paris.outE("Eurostar").value(Distance).head //495
london.valueOption(Founded) //Some(43 AD)
london.valueOption(Distance) //None
paris.setProperty(Founded, "300 BC")

val Name = Key[String]("name")
val Age = Key[Int]("age")

val v1 = graph + ("person", Name -> "marko", Age -> 29) asScala

v1.keys // Set(Key("name"), Key("age"))
v1.property(Name) // "marko"
v1.valueMap // Map("name" -> "marko", "age" -> 29)
v1.valueMap("name", "age") // Map("name" -> "marko", "age" -> 29)
```

More working examples in [SchemaSpec](https://github.com/mpollmeier/gremlin-scala/blob/master/gremlin-scala/src/test/scala/gremlin/scala/SchemaSpec.scala), [ArrowSyntaxSpec](https://github.com/mpollmeier/gremlin-scala/blob/master/gremlin-scala/src/test/scala/gremlin/scala/ArrowSyntaxSpec.scala) and [ElementSpec](https://github.com/mpollmeier/gremlin-scala/blob/master/gremlin-scala/src/test/scala/gremlin/scala/ElementSpec.scala).

### Compiler helps to eliminate invalid traversals
Gremlin-Scala aims to helps you at compile time as much as possible. Take this simple example:

```scala
import gremlin.scala._
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
implicit val graph = TinkerGraph.open.asScala
val g = graph.traversal
g.V.outE.inV  //compiles
g.V.outE.outE //does _not_ compile
```

In Gremlin-Groovy there's nothing stopping you to create the second traversal - it will explode at runtime, as outgoing edges do not have outgoing edges. In Gremlin-Scala this simply doesn't compile.

### Type safe traversals
You can label any step using `as(StepLabel)` and the compiler will infer the correct types for you in the select step using an HList (a type safe list, i.e. the compiler knows the types of the elements of the list). In Gremlin-Java and Gremlin-Groovy you get a `Map[String, Any]`, so you have to cast to the type you *think* it will be, which is ugly and error prone. For example:

```scala
import gremlin.scala._
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
def graph = TinkerFactory.createModern.asScala
val g = graph.traversal

// select all labelled steps
g.V(1).as("a").outE.as("b").select.toList
// returns a `(Vertex, Edge)` for each path

// select subset of labelled steps
val a = StepLabel[Vertex]()
val b = StepLabel[Edge]()
val c = StepLabel[Double]()

val traversal = g.V(1).as(a).outE("created").as(b).value("weight").as(c)
    
traversal.select((b, c)).head
// returns a `(Edge, Double)`
```

More working examples in [SelectSpec](https://github.com/mpollmeier/gremlin-scala/blob/master/gremlin-scala/src/test/scala/gremlin/scala/SelectSpec.scala). Kudos to [shapeless](https://github.com/milessabin/shapeless/) and Scala's sophisticated type system that made this possible. 

As of 3.3.3.2 there is a typesafe `union` step that supports heterogeneous queries:
```scala
val traversal =
  g.V(1).union(
    _.join(_.outE)
     .join(_.out)
  )
// derived type: GremlinScala[(List[Edge], List[Vertex])] 
val (outEdges, outVertices) = traversal.head
```

### A note on predicates
tl;dr: use gremlin.scala.P to create predicates of type P. 

Many steps in take a tinkerpop3 predicate of type `org.apache.tinkerpop.gremlin.process.traversal.P`. Creating Ps that take collection types is dangerous though, because you need to ensure you're creating the correct P. For example `P.within(Set("a", "b"))` would be calling the wrong overload (which checks if the value IS the given set). In that instance you actually wanted to create `P.within(Set("a", "b").asJava: java.util.Collection[String])`. To avoid that confusion, it's best to just `import gremlin.scala._` and create it as `P.within(Set("a", "b"))`.

### Build a custom DSL on top of Gremlin-Scala
You can now build your own domain specific language, which is super helpful if you don't want to expose your users to the world of graphs and tinkerpop, but merely build an API for them. All you need to do is setup your ADT as case classes, define your DSL as Steps and create one implicit constructor (the only boilerplate code). The magic in gremlin.scala.dsl._ allows you to even write for comprehensions like this (DSL for tinkerpop testgraph):

```scala
case class Person  (name: String, age: Integer) extends DomainRoot
case class Software(name: String, lang: String) extends DomainRoot

val traversal = for {
  person   <- PersonSteps(graph)
  software <- person.created
} yield (person.name, software)

// note: `traversal` is inferred by the compiler as `gremlin.scala.dsl.Steps[(String, Software)]`

traversal.toSet // returns: 
Set(
  ("marko", Software("lop", "java")),
  ("josh", Software("lop", "java")),
  ("peter", Software("lop", "java")),
  ("josh", Software("ripple", "java"))
)

// DSL also supports typesafe as/select:
PersonSteps(graph)
  .as("person")
  .created
  .as("software")
  .select
  .toList
// inferred return type is `List[(Person, Software)]`
```

See the full setup and more tests in [DslSpec](https://github.com/mpollmeier/gremlin-scala/blob/master/gremlin-scala/src/test/scala/gremlin/scala/dsl/DslSpec.scala).

### Common and useful steps

```scala
// get a vertex by id
g.V(1).headOption

// get all vertices
g.V.toList

// group all vertices by their label
g.V.group(By.label)

// group vertices by a property
val age = Key[Int]("age")
g.V.has(age).group(By(age))

// order by property decreasing
val age = Key[Int]("age")
g.V.has(age).order(By(age, Order.decr))
```

More working examples in [TraversalSpec](https://github.com/mpollmeier/gremlin-scala/blob/master/gremlin-scala/src/test/scala/gremlin/scala/TraversalSpec.scala).

### Mapping vertices from/to case classes
You can save and load case classes as a vertex - implemented with a [blackbox macro](http://docs.scala-lang.org/overviews/macros/blackbox-whitebox.html). You can optionally annotate the id and label of your case class. Scala's `Option` types will be automatically unwrapped, i.e. a `Some[A]` will be stored as the value of type `A` in the database, or `null` if it's `None`. If we wouldn't unwrap it, the database would have to understand Scala's Option type itself. The same goes for value classes, i.e. a `case class ShoeSize(value: Int) extends AnyVal` will be stored as an Integer. 

Note: your classes must be defined outside the scope where they are being used (e.g. in the code below the class `Example` cannot be inside `object Main`). 

Note: you cannot specify the id when adding a vertex like this. Using `@id` only works when retrieving the vertex back from the graph and it therefor must be an `Option`.

```scala
// this does _not_ work in a REPL
import gremlin.scala._
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph

@label("my_custom_label")
case class Example(longValue: Long, stringValue: Option[String], @underlying vertex: Option[Vertex] = None)

object Main extends App {
  implicit val graph = TinkerGraph.open.asScala
  val example = Example(Long.MaxValue, Some("optional value"))
  val v = graph + example
  v.toCC[Example] // equal to `example`, but with `vertex` set

  // find all vertices with the label of the case class `Example`
  graph.V.hasLabel[Example]

  // modify the vertex like a case class
  v.updateAs[Example](_.copy(longValue = 0L))
}
```

You can also define your own marshaller, if the macro generated one doesn't quite cut it. For that and more examples check out the [MarshallableSpec](https://github.com/mpollmeier/gremlin-scala/blob/master/gremlin-scala/src/test/scala/gremlin/scala/MarshallableSpec.scala).

### More advanced traversals
Here are some examples of more complex traversals from the [examples repo](https://github.com/mpollmeier/gremlin-scala-examples/). If you want to run them yourself, check out the tinkergraph examples in there. 

_What is `Die Hard's` average rating?_
```scala
graph.V.has("movie", "name", "Die Hard")
  .inE("rated")
  .values("stars")
  .mean
  .head
```

_Get the maximum number of movies a single user rated_
```scala
g.V.hasLabel("person")
  .flatMap(_.outE("rated").count)
  .max
  .head
```

_What 80's action movies do 30-something programmers like?_
_Group count the movies by their name and sort the group count map in decreasing order by value._
```scala
g.V
  .`match`(
    __.as("a").hasLabel("movie"),
    __.as("a").out("hasGenre").has("name", "Action"),
    __.as("a").has("year", P.between(1980, 1990)),
    __.as("a").inE("rated").as("b"),
    __.as("b").has("stars", 5),
    __.as("b").outV().as("c"),
    __.as("c").out("hasOccupation").has("name", "programmer"),
    __.as("c").has("age", P.between(30, 40))
  )
  .select[Vertex]("a")
  .map(_.value[String]("name"))
  .groupCount()
  .order(Scope.local).by(Order.valueDecr)
  .limit(Scope.local, 10)
  .head
```

_What is the most liked movie in each decade?_
```scala
g.V()
  .hasLabel(Movie)
  .where(_.inE(Rated).count().is(P.gt(10)))
  .groupBy { movie =>
    val year = movie.value2(Year)
    val decade = (year / 10)
    (decade * 10): Integer
  }
  .map { moviesByDecade =>
    val highestRatedByDecade = moviesByDecade.mapValues { movies =>
      movies.toList
        .sortBy { _.inE(Rated).value(Stars).mean().head }
        .reverse.head //get the movie with the highest mean rating
    }
    highestRatedByDecade.mapValues(_.value2(Name))
  }
  .order(Scope.local).by(Order.keyIncr)
  .head
```

### Serialise to and from files
Currently graphML, graphson and gryo/kryo are supported file formats, it is very easy to serialise and deserialise into those: see [GraphSerialisationSpec](https://github.com/mpollmeier/gremlin-scala/blob/master/gremlin-scala/src/test/scala/gremlin/scala/GraphSerialisationSpec.scala). 
An easy way to visualise your graph is to export it into graphML and import it into [gephi](https://gephi.org/). 

### Help - it's open source!
If you would like to help, here's a list of things that needs to be addressed:
* add more graph databases and examples into the [examples project](https://github.com/mpollmeier/gremlin-scala-examples)
* port over more TP3 steps - see [TP3 testsuite](https://github.com/apache/incubator-tinkerpop/tree/master/gremlin-test/src/main/java/org/apache/tinkerpop/gremlin/process/traversal/step) and [Gremlin-Scala StandardTests](https://github.com/mpollmeier/gremlin-scala/blob/master/gremlin-scala/src/test/scala/gremlin/scala/GremlinStandardTestSuite.scala)
* ideas for more type safety in traversals
* fill this readme and provide other documentation, or how-tos, e.g. a blog post or tutorial

### Why such a long version number?
The first three digits is the TP3 version number, only the last digit is automatically incremented on every release of gremlin-scala.

### Further reading
For more information about Gremlin see the [Gremlin docs](http://tinkerpop.incubator.apache.org/) and the [Gremlin users mailinglist](https://groups.google.com/forum/#!forum/gremlin-users).
Please note that while Gremlin-Scala is very close to the original Gremlin, there are differences to Gremlin-Groovy - don't be afraid, they hopefully all make sense to a Scala developer ;)

Random links:
* Social Network using Titan Db: [part 1](https://partialflow.wordpress.com/2017/02/26/social-network-using-titan-db-part-1/) and [part 2](https://partialflow.wordpress.com/2017/03/04/social-network-using-titan-db-part-2/)
* [Shortest path algorithm with Gremlin-Scala 3.0.0 (Michael Pollmeier)](http://www.michaelpollmeier.com/2014/12/27/gremlin-scala-shortest-path)
* [Shortest path algorithm with Gremlin-Scala 2.4.1 (Stefan Bleibinhaus)](http://bleibinha.us/blog/2013/10/scala-and-graph-databases-with-gremlin-scala)

### Random things worth knowing
* `org.apache.tinkerpop.gremlin.structure.Transaction` is not thread-safe. It's implemented with Apache's ThreadLocal class, see https://github.com/mpollmeier/gremlin-scala/issues/196#issuecomment-301625679

### Releases
... happen automatically for every commit on `master` from [travis.ci](https://travis-ci.org/mpollmeier/gremlin-scala) thanks to [sbt-ci-release-early](https://github.com/ShiftLeftSecurity/sbt-ci-release-early)

### Breaking changes
#### 3.3.3.2
We now have a fully typed `union` step which supports heterogeneous queries. The old version is still available as `unionFlat`, since it may still be relevant in some situations where the union traversals are homogeneous.

#### 3.3.2.0
The `by` modulator is now called `By`. E.g. `order(by(Order.decr))` becomes `order(By(Order.decr))`.
Background: case insensitive platforms like OSX (default) and Windows fail to compile `object by` and `trait By` because they lead to two separate .class files. I decided for this option because it conforms to Scala's naming best practices. 
See https://github.com/mpollmeier/gremlin-scala/issues/237#issuecomment-375928284. 

#### 3.3.1.2
To fix problems with remote graphs and the arrow syntax (e.g. `vertex1 --- "label" --> vertex2`) there now needs to be an `implicit ScalaGraph` in scope. Background: the configuration for remote is unfortunately not stored in the Tinkerpop Graph instance, but in the TraversalSource. Since a vertex only holds a reference to the graph instance, this configuration must be passed somehow. `ScalaGraph` does contain the configuration, e.g. for remote connections, so we now pass it implicitly. 

#### 3.3.1.1
The type signature of GremlinScala changed: the former type parameter `Labels` is now a type member, which shortens the type if you don't care about Labels. The Labels were only used in a small percentage of steps, but had to be written out by users all the time even if they didn't care.
Rewrite rules (old -> new), using `Vertex` as an example:
`GremlinScala[Vertex, _]` -> `GremlinScala[Vertex]` (existential type: most common, i.e the user doesn't use or care about the Labels)
`GremlinScala[Vertex, HNil]` -> `GremlinScala.Aux[Vertex, HNil]` (equivalent: `GremlinScala[Vertex] {type Labels = HNil}`)
`GremlinScala[Vertex, Vertex :: HNil]` -> `GremlinScala.Aux[Vertex, Vertex :: HNil]` (equivalent: `GremlinScala[Vertex] {type Labels = Vertex :: HNil}`)
Notice: GremlinScala isn't a case class any more - it shouldn't have been in the first place.

#### 3.2.4.8 
The `filter` step changed it's signature and now takes a traversal: `filter(predicate: GremlinScala[End, _] => GremlinScala[_, _])`. The old `filter(predicate: End => Boolean)` is now called `filterOnEnd`, in case you still need it. This change might affect your for comprehensions. 

The reasoning for the change is that it's discouraged to use lambdas (see http://tinkerpop.apache.org/docs/current/reference/#a-note-on-lambdas). Instead we are now creating anonymous traversals, which can be optimised by the driver, sent over the wire as gremlin binary for remote execution etc.
