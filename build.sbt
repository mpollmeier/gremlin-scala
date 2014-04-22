name := "gremlin-scala"

version := "2.5.0"

organization := "com.michaelpollmeier"

scalaVersion := "2.10.3"

libraryDependencies <++= scalaVersion { scalaVersion =>
  val gremlinVersion = "2.5.0"
  Seq(
    "com.tinkerpop.gremlin" % "gremlin-java" % gremlinVersion,
    "com.tinkerpop" % "pipes" % gremlinVersion,
    "com.tinkerpop.blueprints" % "blueprints-graph-jung" % gremlinVersion % "provided",
    "com.tinkerpop.blueprints" % "blueprints-graph-sail" % gremlinVersion % "provided",
    "com.tinkerpop.blueprints" % "blueprints-sail-graph" % gremlinVersion % "provided",
    "com.tinkerpop.blueprints" % "blueprints-neo4j-graph" % gremlinVersion % "provided",
    // "com.tinkerpop.blueprints" % "blueprints-neo4jbatch-graph" % gremlinVersion % "provided", //doesn't exist in snapshot repositories as of now... try again later
    "com.tinkerpop.blueprints" % "blueprints-rexster-graph" % gremlinVersion % "provided",
    // REPL dependencies
    "org.scala-lang" % "scala-library" % scalaVersion,
    "org.scala-lang" % "scala-compiler" % scalaVersion,
    "org.scala-lang" % "jline" % scalaVersion,
    // test dependencies
    "org.scalatest" % "scalatest_2.10" % "2.1.3" % "test",
    "com.tinkerpop.gremlin" % "gremlin-test" % gremlinVersion % "test"
  )
}

resolvers ++= Seq(
  //"Local Maven Repository" at Path.userHome.asFile.toURI.toURL + "/.m2/repository",
  "Maven Central" at "http://repo1.maven.org/maven2/",
  "Sonatype releases" at "http://oss.sonatype.org/content/repositories/releases/",
  "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Restlet Framework" at "http://maven.restlet.org"
)

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { x => false }

pomExtra := <url>https://github.com/mpollmeier/gremlin-scala</url>
  <licenses>
    <license>
      <name>Apache License, Version 2.0</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0.html</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:mpollmeier/gremlin-scala.git</url>
    <connection>scm:git:git@github.com:mpollmeier/gremlin-scala.git</connection>
  </scm>
  <developers>
    <developer>
      <id>mpollmeier</id>
      <name>Michael Pollmeier</name>
      <url>http://www.michaelpollmeier.com</url>
    </developer>
  </developers>

credentials += Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", System.getenv("SONATYPE_USER"), System.getenv("SONATYPE_PASS"))
