![logo](https://github.com/mpollmeier/gremlin-scala/raw/master/doc/images/gremlin-scala-logo.png)

### Tinkerpop 3 - the future of Gremlin-Scala
This is the current development branch and it's based on Tinkerpop3. So if you're interested in the future of Gremlin-Scala, this is the right place. Both Tinkerpop3 and Gremlin-Scala are still in an early stage.

Here's some steps to get you started:
* install jdk 8 and configure it to be your `JAVA_HOME` and in your path (check with `echo $JAVA_HOME` and `java -version`)
* checkout Gremlin-Scala branch `tinkerpop3` and run tests. `sbt test`
* alternatively just pop over to the [examples project](https://github.com/mpollmeier/gremlin-scala-examples) - it
  contains preconfigured sbt projects for some graph databases

If you would like to help, here's a list of things that needs to be addressed:
* add more graph databases and examples into the [examples project](https://github.com/mpollmeier/gremlin-scala-examples)
* port over more TP3 steps - see [TP3 testsuite](https://github.com/tinkerpop/tinkerpop3/tree/master/gremlin-test/src/main/java/com/tinkerpop/gremlin/process/graph/step) and [Gremlin-Scala StandardTests](https://github.com/mpollmeier/gremlin-scala/blob/tinkerpop3/src/test/scala/com/tinkerpop/gremlin/scala/GremlinStandardTestSuite.scala)
* fill this readme and provide other documentation, or how-tos, e.g. a blog post or tutorial

### Compile time help - aka don't shoot yourself in the foot
Gremlin-Scala aims to helps you at compile time as much as possible. Take this simple example:

```scala
GremlinScala(graph).V.outE.outE //does _not_ compile
GremlinScala(graph).V.outE.inV  //compiles
```

In Gremlin-Java or Gremlin-Groovy there's nothing stopping you to create the first (wrong) traversal - it will fail badly at runtime. 

### Type safe traversals
What makes Gremlin-Scala stand out is the support for full type safety in a traversal. You can label any step you want and in the end call `labelledPath` - you will the values in each labelled step as an HList - in Java/Groovy you would have to cast, but the power of Scala and Shapeless takes that guesswork out. For example:

```scala
GremlinScala(graph).V.as("a").out.as("b").value[String]("name").as("c")
```

You can label as many steps as you like and Gremlin-Scala will preserve the types for you. If you now use the `labelledPath` step to iterate over all labelled steps in the graph that match your traversal you will get an HList of type `Vertex :: Vertex :: String :: HNil` for each route. For more examples see [LabelledPathSpec](https://github.com/mpollmeier/gremlin-scala/blob/tinkerpop3/src/test/scala/com/tinkerpop/gremlin/scala/LabelledPathSpec.scala).
In comparison: Gremlin-Java and Gremlin-Groovy just return a `List[Any]` and you then have to cast the elements - they have lost the types on the way. Kudos to [shapeless](https://github.com/milessabin/shapeless/) and Scala's sophisticated type system that made this possible. 

If you are curious how this works: the type of this traversal is `GremlinScala[String, Vertex :: Vertex :: String :: HNil]`...

### Usage
The easiest way is to clone the [examples project](https://github.com/mpollmeier/gremlin-scala-examples) and use the
graph db of your choice. 


#TODO
Gremlin-Scala for Tinkerpop3
=============
TODO

A thin wrapper for Gremlin to make it easily usable for Scala Developers. 
Gremlin is a graph DSL for traversing a number of graph databases including
[Neo4j](http://neo4j.org/),
[OrientDB](http://www.orientechnologies.com/),
[DEX](http://www.sparsity-technologies.com/dex),
[InfiniteGraph](http://www.infinitegraph.com/),
[Titan](http://thinkaurelius.github.com/titan/),
[Rexster graph server](http://rexster.tinkerpop.com),
and [Sesame 2.0 compliant RDF stores](http://www.openrdf.org).

[![Build Status](https://secure.travis-ci.org/mpollmeier/gremlin-scala.png?branch=master)](http://travis-ci.org/mpollmeier/gremlin-scala)

For more information about Gremlin see the [Gremlin docs](http://gremlindocs.com/), [Gremlin wiki](https://github.com/tinkerpop/gremlin/wiki).
[Gremlin-Steps](https://github.com/tinkerpop/gremlin/wiki/Gremlin-Steps) and [Methods](https://github.com/tinkerpop/gremlin/wiki/Gremlin-Methods). 
If you have a question please check out the [Gremlin users mailinglist](https://groups.google.com/forum/#!forum/gremlin-users).
Please note that while Gremlin-Scala is very close to the original Gremlin, there a slight differences to Gremlin-Groovy - don't be afraid, they hopefully all make sense to a Scala developer ;)


Gremlin-Console
=============
TODO

Using Gremlin-Scala in Rexster
=============
TODO

Type safety, Options and nulls
=============
TODO

Further reading
=============
[Shortest path algorithm with Gremlin-Scala 2.4.1 (Stefan Bleibinhaus)](http://bleibinha.us/blog/2013/10/scala-and-graph-databases-with-gremlin-scala)

