name := "gremlin-scala"

version := "2.2.0-SNAPSHOT"

organization := "com.tinkerpop.gremlin"

scalaVersion := "2.10.0"

scalaBinaryVersion := "2.10"

libraryDependencies <++= scalaVersion { scalaVersion =>
  val gremlinVersion = "2.2.0"
  val blueprintsVersion = "2.2.0"
  val pipesVersion = "2.2.0"
  val junitVersion = "4.5"
  Seq(
    "com.tinkerpop.gremlin" % "gremlin-java" % gremlinVersion withSources,
    "com.tinkerpop" % "pipes" % pipesVersion withSources,
    "com.tinkerpop.blueprints" % "blueprints-graph-jung" % blueprintsVersion % "provided",
    "com.tinkerpop.blueprints" % "blueprints-graph-sail" % blueprintsVersion % "provided",
    "com.tinkerpop.blueprints" % "blueprints-sail-graph" % blueprintsVersion % "provided",
    "com.tinkerpop.blueprints" % "blueprints-neo4j-graph" % blueprintsVersion % "provided",
    "com.tinkerpop.blueprints" % "blueprints-neo4jbatch-graph" % blueprintsVersion % "provided",
    "com.tinkerpop.blueprints" % "blueprints-orient-graph" % blueprintsVersion % "provided",
    "com.tinkerpop.blueprints" % "blueprints-dex-graph" % blueprintsVersion % "provided",
    "com.tinkerpop.blueprints" % "blueprints-rexster-graph" % blueprintsVersion % "provided",
    // REPL dependencies
    "org.scala-lang" % "scala-library" % scalaVersion,
    "org.scala-lang" % "scala-compiler" % scalaVersion,
    "org.scala-lang" % "jline" % scalaVersion,
    // test dependencies
    "junit" % "junit" % junitVersion % "test",
    "com.novocode" % "junit-interface" % "0.8" % "test->default",
    "com.tinkerpop.gremlin" % "gremlin-test" % gremlinVersion % "test"
  )
}

resolvers ++= Seq(
  //"Local Maven Repository" at Path.userHome.asFile.toURI.toURL + "/.m2/repository",
  "Maven Central" at "http://repo1.maven.org/maven2/",
  "Sonatype releases" at "http://oss.sonatype.org/content/repositories/releases/",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  // org.openrdf.sesame
  "Aduna Software" at "http://repo.aduna-software.org/maven2/releases/"
)
