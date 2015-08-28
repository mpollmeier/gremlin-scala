![logo](https://github.com/mpollmeier/gremlin-scala/raw/master/doc/images/gremlin-scala-logo.png)
[![Build Status](https://secure.travis-ci.org/mpollmeier/gremlin-scala.png?branch=master)](http://travis-ci.org/mpollmeier/gremlin-scala)
 [![Join the chat at https://gitter.im/mpollmeier/gremlin-scala](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/mpollmeier/gremlin-scala?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

## Gremlin-Scala for Apache Tinkerpop 3

A slim wrapper to make Gremlin - a JVM graph traversal library - usable from Scala. This is the current development branch for [Apache Tinkerpop3](https://github.com/apache/incubator-tinkerpop). The old version for Tinkerpop 2 is still in the [2.x branch](https://github.com/mpollmeier/gremlin-scala/tree/2.x).

### Benefits
* Scala friendly function signatures, aiming to be close to the standard collection library
* Nicer DSL e.g. to create vertices and edges
* You can use standard Scala functions instead of having to worry about how to implement things like `java.util.function.BiPredicate`
* Nothing is hidden away, you can always easily access the Gremlin-Java objects if needed. Examples include accessing graph db specifics things like indexes, or using a step that hasn't been implemented in Gremlin-Scala yet
* Minimal overhead - only allocates additional instances if absolutely necessary

### Getting started
The [examples project](https://github.com/mpollmeier/gremlin-scala-examples) comes with working examples for different graph databases. 

### Using the sbt console
* tl;dr: `sbt gremlin-scala/console`
* start `sbt` in the root project
```
> projects
[info]     gremlin-scala
[info]     macros
[info]   * root
>
```
* Next, change to the gremlin-scala project using `project gremlin-scala`
* Finally, to test out the API in a REPL type `console` 

### Creating vertices and edges

```scala
import gremlin.scala._
import gremlin.scala.schema.Key
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
val graph = TinkerGraph.open.asScala

// create labelled vertex
val paris = graph + "Paris"

// create vertex with typed properties
object Founded extends Key[String]("founded")
val london = graph + ("London", Founded("43 AD"))

// create labelled edges 
paris --- "OneWayRoad" --> london
paris <-- "OtherWayAround" --- london

// create labelled bidirectional edge
paris <-- "Eurostar" --> london

// create edge with typed properties
object Name extends Key[String]("name")
paris <-- ("EuroStar", Name("TrainName")) --- london
```

Many thanks to [@dkrieg](https://github.com/dkrieg) for contributing this. For more details check out the [SchemaSpec](https://github.com/mpollmeier/gremlin-scala/blob/master/gremlin-scala/src/test/scala/gremlin/scala/SchemaSpec.scala).

### Compiler helps to eliminate invalid traversals
Gremlin-Scala aims to helps you at compile time as much as possible. Take this simple example:

```scala
import gremlin.scala._
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
val graph = TinkerGraph.open.asScala
graph.V.outE.inV  //compiles
graph.V.outE.outE //does _not_ compile
```

In standard Gremlin there's nothing stopping you to create the first traversal - it will explode at runtime, as
outgoing edges do not have outgoing edges. This is simply an invalid step and we can use the compiler to help us. 

### Type safe traversals
Gremlin-Scala has support for full type safety in a traversal. You can label any step you want and in the end call `labelledPath` - you will the values in each labelled step as an HList. That's a type safe list, i.e. the compiler guarantees the types, which also helps you auto-complete in your IDE. In contrast: in Java and Groovy you would have to cast to the type you *think* it will be, which is ugly and error prone. 
For example:

```scala
import gremlin.scala._
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory
val graph = TinkerFactory.createModern.asScala
val traversal = graph.V.as("a").out.value[String]("name").as("b").labelledPath
traversal.toList
// returns `Vertex :: String :: HNil` for each path
```

You can label as many steps as you like and Gremlin-Scala will preserve the types for you. For more examples see [LabelledPathSpec](https://github.com/mpollmeier/gremlin-scala/blob/master/gremlin-scala/src/test/scala/gremlin/scala/LabelledPathSpec.scala).
In comparison: Gremlin-Java and Gremlin-Groovy just return a `List[Any]` and you then have to cast the elements - the types got lost on the way. Kudos to [shapeless](https://github.com/milessabin/shapeless/) and Scala's sophisticated type system that made this possible. 

### Saving / loading case classes
You can save and load case classes as a vertex - this is still experimental but pretty cool. Note: this does _not_ work in a REPL, you have to put it into a test. For examples check out the [MarshallerSpec](https://github.com/mpollmeier/gremlin-scala/blob/master/gremlin-scala/src/test/scala/gremlin/scala/MarshallerSpec.scala).

```scala
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
val graph = TinkerGraph.open.asScala
case class Example(i: Int, s:Option[String])

it("load a vertex into a case class") {
  val graph = TinkerGraph.open.asScala
  val example = Example(Int.MaxValue, Some("optional value"))
  val v = graph.addVertex(example)
  v.toCC[Example] shouldBe example
}
```

Note that you can also use Options as the example shows.
Thanks to <a href="https://github.com/joan38">joan38</a> for <a href="https://github.com/mpollmeier/gremlin-scala/pull/66">contributing</a> this feature!

## Help - it's open source!
If you would like to help, here's a list of things that needs to be addressed:
* add more graph databases and examples into the [examples project](https://github.com/mpollmeier/gremlin-scala-examples)
* port over more TP3 steps - see [TP3 testsuite](https://github.com/apache/incubator-tinkerpop/tree/master/gremlin-test/src/main/java/org/apache/tinkerpop/gremlin/process/traversal/step) and [Gremlin-Scala StandardTests](https://github.com/mpollmeier/gremlin-scala/blob/master/gremlin-scala/src/test/scala/gremlin/scala/GremlinStandardTestSuite.scala)
* fill this readme and provide other documentation, or how-tos, e.g. a blog post or tutorial

## Further reading
For more information about Gremlin see the [Gremlin docs](http://tinkerpop.incubator.apache.org/docs/3.0.0-incubating/) and the [Gremlin users mailinglist](https://groups.google.com/forum/#!forum/gremlin-users).
Please note that while Gremlin-Scala is very close to the original Gremlin, there a slight differences to Gremlin-Groovy - don't be afraid, they hopefully all make sense to a Scala developer ;)

Random links:
* [Shortest path algorithm with Gremlin-Scala 3.0.0 (Michael
  Pollmeier)](http://www.michaelpollmeier.com/2014/12/27/gremlin-scala-shortest-path/)
* [Shortest path algorithm with Gremlin-Scala 2.4.1 (Stefan Bleibinhaus)](http://bleibinha.us/blog/2013/10/scala-and-graph-databases-with-gremlin-scala)
