name := "gremlin-scala"
version := "3.0.0.M9-incubating"
organization := "com.michaelpollmeier"
scalaVersion := "2.11.7"
crossScalaVersions := Seq("2.10.5", scalaVersion.value)

libraryDependencies <++= scalaVersion { scalaVersion =>
  val gremlinVersion = "3.0.0.M9-incubating"
  Seq(
    "org.apache.tinkerpop" % "gremlin-core" % gremlinVersion exclude("org.slf4j", "slf4j-log4j12"),
    "org.apache.tinkerpop" % "tinkergraph-gremlin" % gremlinVersion exclude("org.slf4j", "slf4j-log4j12"),
    "org.scala-lang" % "scala-reflect" % scalaVersion,
    "com.novocode" % "junit-interface" % "0.11" % "test->default",
    "com.chuusai" %% "shapeless" % "2.2.0",
    "org.apache.tinkerpop" % "gremlin-test" % gremlinVersion % Test,
    "junit" % "junit" % "4.12" % Test,
    "org.scalatest" %% "scalatest" % "2.2.5" % Test
  )
}

scalacOptions ++= Seq(
  "-Xlint"
  // "-Xfatal-warnings",
  // "-feature"
  // "-deprecation", //hard to handle when supporting multiple scala versions...
  //"-Xlog-implicits"
  //"-Ydebug"
)
// testOptions in Test += Tests.Argument("-oF") // full stack traces
incOptions := incOptions.value.withNameHashing(true) // doesn't work on travis ;(

resolvers += "Apache public" at "https://repository.apache.org/content/groups/public/"
resolvers += Resolver.mavenLocal

publishTo := {
  val sonatype = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at sonatype + "content/repositories/snapshots")
  else
    Some("releases"  at sonatype + "service/local/staging/deploy/maven2")
}
publishMavenStyle := true
publishArtifact in Test := false
pomIncludeRepository := { _ ⇒ false }
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
  </developers>;

// credentials += Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", System.getenv("SONATYPE_USER"), System.getenv("SONATYPE_PASS"))
