name := "gremlin-scala"

version := "2.5.0-SNAPSHOT"

organization := "com.tinkerpop.gremlin"

scalaVersion := "2.10.3"

libraryDependencies <++= scalaVersion { scalaVersion =>
  val gremlinVersion = "2.5.0-SNAPSHOT"
  Seq(
    "com.tinkerpop.gremlin" % "gremlin-java" % gremlinVersion withSources,
    "com.tinkerpop" % "pipes" % gremlinVersion withSources,
    "com.tinkerpop.blueprints" % "blueprints-graph-jung" % gremlinVersion % "provided",
    "com.tinkerpop.blueprints" % "blueprints-graph-sail" % gremlinVersion % "provided",
    "com.tinkerpop.blueprints" % "blueprints-sail-graph" % gremlinVersion % "provided",
    "com.tinkerpop.blueprints" % "blueprints-neo4j-graph" % gremlinVersion % "provided",
    // "com.tinkerpop.blueprints" % "blueprints-neo4jbatch-graph" % gremlinVersion % "provided", //doesn't exist in snapshot repositories as of now... try again later
    "com.tinkerpop.blueprints" % "blueprints-orient-graph" % gremlinVersion % "provided",
    "com.tinkerpop.blueprints" % "blueprints-dex-graph" % gremlinVersion % "provided",
    "com.tinkerpop.blueprints" % "blueprints-rexster-graph" % gremlinVersion % "provided",
    // REPL dependencies
    "org.scala-lang" % "scala-library" % scalaVersion,
    "org.scala-lang" % "scala-compiler" % scalaVersion,
    "org.scala-lang" % "jline" % scalaVersion,
    // test dependencies
    "org.scalatest" % "scalatest_2.10" % "2.0.RC2" % "test",
    "com.tinkerpop.gremlin" % "gremlin-test" % gremlinVersion % "test"
  )
}

resolvers ++= Seq(
  //"Local Maven Repository" at Path.userHome.asFile.toURI.toURL + "/.m2/repository",
  "Maven Central" at "http://repo1.maven.org/maven2/",
  "Sonatype releases" at "http://oss.sonatype.org/content/repositories/releases/",
  "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Aduna Software" at "http://repo.aduna-software.org/maven2/releases/", //for org.openrdf.sesame
  "Restlet Framework" at "http://maven.restlet.org"
)
