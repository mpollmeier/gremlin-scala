![logo](https://github.com/mpollmeier/gremlin-scala/raw/master/doc/images/gremlin-scala-logo.png)
[![Build Status](https://secure.travis-ci.org/mpollmeier/gremlin-scala.png?branch=stable)](http://travis-ci.org/mpollmeier/gremlin-scala)

## Gremlin-Scala for Apache Tinkerpop 3
A slim wrapper to make Gremlin - a JVM graph traversal library - usable from Scala. 
This is the current development branch, based on [Apache Tinkerpop3](https://github.com/apache/incubator-tinkerpop). Note that both Tinkerpop3 and Gremlin-Scala are still in an early stage. If you are looking for an earlier (more mature) version check out the [2.x branch](https://github.com/mpollmeier/gremlin-scala-examples).

### Branches
* [master](https://github.com/mpollmeier/gremlin-scala/) - the latest snapshot
* [stable](https://github.com/mpollmeier/gremlin-scala/tree/stable) - the latest stable version (milestone)

### Usage
Clone the [examples project](https://github.com/mpollmeier/gremlin-scala-examples) and start with
a working example for the graph database of your choice (currently only neo4j and titan)

## Benefits
* Scala friendly function signatures, aiming to be close to the standard collection library
* No need to worry about how to implement Java 8 functions - do you really want to create instances of `java.util.function.BiPredicate`?
* Nothing is hidden away, you can always easily access the Gremlin-Java objects if needed. Examples include accessing graph db specifics things like indexes, or using a step that hasn't been implemented in Gremlin-Scala yet
* Only allocates additional instances if absolutely necessary

### Compiler helps to create only valid traversals
Gremlin-Scala aims to helps you at compile time as much as possible. Take this simple example:

```scala
GremlinScala(graph).V.outE.outE //does _not_ compile
GremlinScala(graph).V.outE.inV  //compiles
```

In standard Gremlin there's nothing stopping you to create the first traversal - it will explode at runtime, as
outgoing edges do not have outgoing edges. This is simply an invalid step and we can use the compiler to help us. 

### Type safe traversals
Gremlin-Scala has support for full type safety in a traversal. You can label any step you want and in the end call `labelledPath` - you will the values in each labelled step as an HList. That's a type safe list, i.e. the compiler guarantees the types, which also helps you auto-complete in your IDE. In contrast: in Java and Groovy you would have to cast to the type you *think* it will be, which is ugly and error prone. 
For example:

```scala
GremlinScala(graph).V.as("a")
  .out.as("b")
  .value[String]("name").as("c")
  .labelledPath
// returns `Vertex :: Vertex :: String :: HNil` for each path
```

You can label as many steps as you like and Gremlin-Scala will preserve the types for you. For more examples see [LabelledPathSpec](https://github.com/mpollmeier/gremlin-scala/blob/master/src/test/scala/gremlin/scala/LabelledPathSpec.scala).
In comparison: Gremlin-Java and Gremlin-Groovy just return a `List[Any]` and you then have to cast the elements - the types got lost on the way. Kudos to [shapeless](https://github.com/milessabin/shapeless/) and Scala's sophisticated type system that made this possible. 

### Saving / loading case classes
You can save and load case classes as a vertex - this is still experimental but pretty cool:

```scala
  import com.tinkerpop.gemlin.scala._
  val example = ExampleClass("some string", Int.MaxValue, Long.MaxValue, Some("option type"))
  val gs = GremlinScala(graph)
  val v = gs.save(example)
  v.start.load[ExampleClass].head shouldBe example
```

Note that you can also use Options as the example shows.
Thanks to <a href="https://github.com/joan38">joan38</a> for <a href="https://github.com/mpollmeier/gremlin-scala/pull/66">contributing</a> this feature!

## Help - it's open source!
If you would like to help, here's a list of things that needs to be addressed:
* add more graph databases and examples into the [examples project](https://github.com/mpollmeier/gremlin-scala-examples)
* port over more TP3 steps - see [TP3 testsuite](https://github.com/apache/incubator-tinkerpop/tree/master/gremlin-test/src/main/java/org/apache/tinkerpop/gremlin/process/graph/traversal/step) and [Gremlin-Scala StandardTests](https://github.com/mpollmeier/gremlin-scala/blob/master/src/test/scala/gremlin/scala/GremlinStandardTestSuite.scala)
* fill this readme and provide other documentation, or how-tos, e.g. a blog post or tutorial

## Further reading
For more information about Gremlin see the [Gremlin docs](http://www.tinkerpop.com/docs/current/) and the [Gremlin users mailinglist](https://groups.google.com/forum/#!forum/gremlin-users).
Please note that while Gremlin-Scala is very close to the original Gremlin, there a slight differences to Gremlin-Groovy - don't be afraid, they hopefully all make sense to a Scala developer ;)

Random links:
* [Shortest path algorithm with Gremlin-Scala 3.0.0 (Michael
  Pollmeier)](http://www.michaelpollmeier.com/2014/12/27/gremlin-scala-shortest-path/)
* [Shortest path algorithm with Gremlin-Scala 2.4.1 (Stefan Bleibinhaus)](http://bleibinha.us/blog/2013/10/scala-and-graph-databases-with-gremlin-scala)
