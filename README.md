![logo](https://github.com/mpollmeier/gremlin-scala/raw/master/doc/images/gremlin-scala-logo.png)

gremlin-scala
=============
A thin Scala wrapper for Gremlin, a graph DSL for traversing a number of graph databases including
[Neo4j](http://neo4j.org/),
[OrientDB](http://www.orientechnologies.com/),
[DEX](http://www.sparsity-technologies.com/dex),
[InfiniteGraph](http://www.infinitegraph.com/),
[Titan](http://thinkaurelius.github.com/titan/),
[Rexster graph server](http://rexster.tinkerpop.com),
and [Sesame 2.0 compliant RDF stores](http://www.openrdf.org).

For more information about Gremlin see the [Gremlin docs](http://gremlindocs.com/), [Gremlin wiki](https://github.com/tinkerpop/gremlin/wiki).
[Gremlin-Steps](https://github.com/tinkerpop/gremlin/wiki/Gremlin-Steps) and [Methods](https://github.com/tinkerpop/gremlin/wiki/Gremlin-Methods). 
If you have a question please check out the [Gremlin users mailinglist](https://groups.google.com/forum/#!forum/gremlin-users).
Please note that while Gremlin-Scala is very close to the original Gremlin, there a slight differences to Gremlin-Groovy - don't be afraid, they hopefully all make sense to a Scala developer ;)


Sample usage
=============
The test specifications are documenting how you can use Gremlin-Scala, this is an excerpt from SampleUsageTest.scala:
```scala
  describe("Usage with Tinkergraph") {
    it("finds all names of vertices") {
      vertices.name.toScalaList should be(List("lop", "vadas", "marko", "peter", "ripple", "josh"))
    }

    it("has different ways to get the properties of a vertex") {
      val vertex = graph.v(1)

      //dynamic invocation for property is untyped and may return null, like the groovy dsl
      vertex.name should be("marko")
      vertex.nonExistentProperty should equal(null)

      //apply method returns Any type
      vertex("age") should be(29)
      vertex("nonExistentProperty") should equal(null)

      //property returns Some[A] if element present and of type A, otherwise None
      vertex.property[Integer]("age") should be(Some(29))
      vertex.property[String]("age") should be(None)
      vertex.property[Int]("age") should be(None)
      vertex.property("nonExistentProperty") should equal(None)
    }

    it("finds everybody who is over 30 years old") {
      vertices.filter { v: ScalaVertex ⇒
        v.property[Integer]("age") match {
          case Some(age) if age > 30 ⇒ true
          case _                     ⇒ false
        }
      }.propertyMap.toScalaList should be(List(
        Map("name" -> "peter", "age" -> 35),
        Map("name" -> "josh", "age" -> 32)))
    }

    it("finds who marko knows") {
      val marko = graph.v(1)
      marko.out("knows").map { _("name") }.toScalaList should be(List("vadas", "josh"))
    }

    it("finds who marko knows if a given edge property `weight` is > 0.8") {
      val marko = graph.v(1)
      marko.outE("knows").filter { e: Edge ⇒
        e.property[java.lang.Float]("weight") match {
          case Some(weight) if weight > 0.8 ⇒ true
          case _                            ⇒ false
        }
      }.inV.propertyMap.toScalaList should be(List(Map("name" -> "josh", "age" -> 32)))
    }

    it("finds all vertices") {
      vertices.count should be(6)
      vertices.propertyMap.toScalaList should be(List(
        Map("name" -> "lop", "lang" -> "java"),
        Map("age" -> 27, "name" -> "vadas"),
        Map("name" -> "marko", "age" -> 29),
        Map("name" -> "peter", "age" -> 35),
        Map("name" -> "ripple", "lang" -> "java"),
        Map("name" -> "josh", "age" -> 32)))
    }

    describe("Usage with empty Graph") {
      it("creates a vertex with properties") {
        val graph = new TinkerGraph
        val id = 42
        val vertex = graph.addV(id)
        vertex.setProperty("key", "value")

        graph.v(id).key should be("value")
      }

      it("creates vertices without specific ids") {
        val graph = new TinkerGraph
        graph.addV()
        graph.addV()
        graph.V.count should be(2)
      }

      it("creates edges between vertices") {
        val graph = new TinkerGraph
        val v1 = graph.addV()
        val v2 = graph.addV()
        graph.addE(v1, v2, "label")

        val foundVertices = v1.out("label").toList
        foundVertices.size should be(1)
        foundVertices.get(0) should be(v2)
      }
    }

    describe("Graph navigation") {
      it("follows outEdge and inVertex") {
        graph.v(1).outE("created").inV.name.toScalaList should be(List("lop"))
      }
    }
  }
```

Adding Gremlin-Scala as a dependency to your project
=============
```scala
/* sbt */ "com.michaelpollmeier" % "gremlin-scala" % "2.4.1"
```
```xml
<!-- Maven -->
<dependency>
  <groupId>com.michaelpollmeier</groupId>
  <artifactId>gremlin-scala</artifactId>
  <version>2.4.1</version>
</dependency>
```

Gremlin-Console
=============
You can play with Gremlin in an interactive console in gremlin-standalone/bin/gremlin-scala.sh: [Download](http://www.michaelpollmeier.com/fileshare/gremlin-scala-2.4.0-standalone.tgz)
```shell
         \,,,/
         (o o)
-----oOOo-(_)-oOOo-----
# create a sample in-memory graph
gremlin> val g = TinkerGraphFactory.createTinkerGraph
==> tinkergraph[vertices:6 edges:6]

# list all vertices
gremlin> g.V
==> v[3]
==> v[2]
==> v[1]
==> v[6]
==> v[5]
==> v[4]

# show properties map of the vertices
gremlin> g.V.propertyMap
==> {name=lop, lang=java}
==> {name=vadas, age=27}
==> {name=marko, age=29}
==> {name=peter, age=35}
==> {name=ripple, lang=java}
==> {name=josh, age=32}

# show names of all vertices
gremlin> g.V.name
==>lop
==>vadas
==>marko
==>peter
==>ripple
==>josh

# get marko's vertex
gremlin> val marko = g.v(1)
==> v[1]

gremlin> marko.age
==> 29

# find marko's friends' names
gremlin> marko.out("knows")(_.name)
==> vadas
==> josh

# create a new graph with two vertices and an edge between them
val g = new TinkerGraph()
val v1 = graph.addV()
val v2 = graph.addV()
graph.addE(v1, v2, "label")

For many more examples (in Gremlin-Groovy syntax) see the [Getting started page of Gremlin (in groovy syntax)]https://github.com/tinkerpop/gremlin/wiki/Getting-Started].
This is a normal Scala REPL, so you run arbitrary Scala code here. 
```

Using Gremlin-Scala as a Rexster Console drop-in
=============
[Rexster](https://github.com/tinkerpop/rexster/) is a RESTful server for the Tinkerpop stack.  It comes with a [Console](https://github.com/tinkerpop/rexster/wiki/Rexster-Console) that let's you execute ad hoc Gremlin queries on the configured graphs inside a REPL.
In order to use Gremlin-Scala in that Console, just follow these simple steps:

1) download the [dropin.zip](http://www.michaelpollmeier.com/fileshare/gremlin-scala-2.5.0-SNAPSHOT-rexster-dropin.zip)

2) unzip the four jars into rexster-standalone/ext

3) Add this block to rexster-standalone/config/rexster.xml inside <script-engines>:
```xml
<script-engine>
  <name>gremlin-scala</name>
</script-engine>
```
4) start the console: bin/rexster-console.sh
For more information see the [Rexster Console Wiki](https://github.com/tinkerpop/rexster/wiki/Rexster-Console).

And this is how you switch the language to Gremlin-Scala and execute a simple query:
```scala
rexster[groovy]> ?scala
rexster[scala]> val g = rexster.getGraph("tinkergraph")
==>g: com.tinkerpop.blueprints.Graph = tinkergraph[vertices:6 edges:6 directory:data/graph-example-1]
rexster[scala]> g.v(1).name
==>res1: Any = marko
```


Building Gremlin-Scala manually
=============
```shell
git clone https://github.com/mpollmeier/gremlin-scala.git
mvn test      #run all tests
mvn install   #installs into your local maven repository
```

A word about type safety, Options and nulls
=============
Gremlin-Scala should be as idiomatic Scala as possible, i.e. you can work with Scala's Option type instead of dealing with nulls etc. However, I want to stick close to Gremlin-Groovy, so that it still feels like a real Gremlin and we can use the existing documentation. Often you have multiple ways to achieve the same. 
Example 1: Gremlin defines a step *transform*, which in functional Scala land is typically called *map*. Gremlin-Scala simply defines both, so it's your choice which one to use. I encourage you to use *map*. 
Example 2: to access the properties of elements (vertices or edges) you have the following options:
```scala
  element.someProp //return type: Any, could be null
  element[Int]("intProp") //returns an Int, throws ClassCastException for wrong type
  element.property[Int]("intProp") //returns Some[Int] if property defined, otherwise None. Idiomatic Scala
```
Check out the sample usage above for more details.


Contributors
=============
[Michael Pollmeier](http://www.michaelpollmeier.com) - project maintainer since Gremlin 2.2

[Marko Rodriguez](http://markorodriguez.com/) and [Zach Cox](http://theza.ch) - started this project

[Tinkerpop team](http://www.tinkerpop.com) - created a whole awesome stack around graph databases

[Antonio](https://twitter.com/Vantonio) - helped with jsr223 script engine
