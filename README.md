![logo](https://github.com/mpollmeier/gremlin-scala/raw/master/doc/images/gremlin-scala-logo.png)

gremlin-scala
=============
A thin wrapper for Gremlin so that it feels natural to use the Gremlin graph DSL in Scala. 
Gremlin is a domain specific language for traversing a number of graph databases including
[Neo4j](http://neo4j.org/),
[OrientDB](http://www.orientechnologies.com/),
[DEX](http://www.sparsity-technologies.com/dex),
[InfiniteGraph](http://www.infinitegraph.com/),
[Titan](http://thinkaurelius.github.com/titan/),
[Rexster graph server](http://rexster.tinkerpop.com)
and [Sesame 2.0 compliant RDF stores](http://www.openrdf.org).

For more information about Gremlin see the [Gremlin wiki](https://github.com/tinkerpop/gremlin/wiki).
[Gremlin-Steps](https://github.com/tinkerpop/gremlin/wiki/Gremlin-Steps) and [Methods](https://github.com/tinkerpop/gremlin/wiki/Gremlin-Methods) will give you a quick deep dive. 

Sample usage
=============

```scala
  // this is an executable ScalaTest specification - see SampleUsageTest.scala for full setup
  describe("Usage with default Tinkergraph") {
    val graph = TinkerGraphFactory.createTinkerGraph
    def vertices = graph.V

    it("finds all vertices") {
      vertices.count should be(6)
      vertices.propertyMap.toList.toString should be(
        "[{name=lop, lang=java}, {age=27, name=vadas}, {age=29, name=marko}, " +
          "{age=35, name=peter}, {name=ripple, lang=java}, {age=32, name=josh}]")
    }

    it("finds all names of vertices") {
      val names = vertices.property("name").toList
      names.toString should be("[lop, vadas, marko, peter, ripple, josh]")
    }

    it("can get a specific vertex by id and get it's properties") {
      val marko = graph.v(1)
      marko("name") should be("marko")
      marko("age") should be(29)
    }

    it("finds everybody who is over 30 years old") {
      vertices.filter { v: Vertex ⇒
        v.get[Int]("age") match {
          case Some(age) if age > 30 ⇒ true
          case _                     ⇒ false
        }
      }.propertyMap().toList.toString should be(
        "[{age=35, name=peter}, {age=32, name=josh}]")
    }

    it("finds who marko knows") {
      val marko = graph.v(1)
      marko.out("knows")(_("name"))
        .toList.toString should be("[vadas, josh]")
    }

    it("finds who marko knows if a given edge property `weight` is > 0.8") {
      val marko = graph.v(1)
      marko.outE("knows").filter { e: Edge ⇒
        e.get[Float]("weight") match {
          case Some(weight) if weight > 0.8 ⇒ true
          case _                            ⇒ false
        }
      }.inV.propertyMap().toList.toString should be("[{age=32, name=josh}]")
    }

    describe("Usage with empty Graph") {
      it("creates a vertex with properties") {
        val graph = new TinkerGraph
        val id = 42
        val vertex = graph.addV(id)
        vertex.setProperty("key", "value")

        graph.v(id)("key") should be("value")
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
```

Adding Gremlin-Scala as a dependency to your project
=============
```scala
/* sbt */ "com.michaelpollmeier" % "gremlin-scala" % "VERSION"
```
```xml
<!-- Maven -->
<dependency>
  <groupId>com.michaelpollmeier</groupId>
  <artifactId>gremlin-scala</artifactId>
  <version>VERSION</version>
</dependency>
```

Gremlin-Console
=============
You can play with Gremlin in an interactive console:
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
gremlin> g.V.property("name")
==>lop
==>vadas
==>marko
==>peter
==>ripple
==>josh

# get marko's vertex
gremlin> val marko = g.v(1)
==> v[1]

gremlin> marko("age")
==> 29

# find marko's friends' names
gremlin> marko.out("knows")(_("name"))
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

Building Gremlin-Scala
=============
```shell
git clone https://github.com/mpollmeier/gremlin-scala.git
mvn test      #run all tests - should find all dependencies and run the tests fine
mvn install   #install into your local maven repository so that you can use it (groupId=com.tinkerpop.gremlin, artifactId=gremlin-scala
```

Kudos go to [Zach Cox](http://theza.ch) for starting this project and the whole [Tinkerpop](http://www.tinkerpop.com) team for creating a whole awesome stack around graph databases.
