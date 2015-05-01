name := "gremlin-scala"
version := "3.0.0-SNAPSHOT"
organization := "com.michaelpollmeier"
scalaVersion := "2.11.6"
crossScalaVersions := Seq("2.10.4", scalaVersion.value)

libraryDependencies <++= scalaVersion { scalaVersion =>
  val gremlinVersion = "3.0.0.M8-incubating"
  val junitVersion = "4.11"
  Seq(
    "org.apache.tinkerpop" % "gremlin-core" % gremlinVersion,
    "org.apache.tinkerpop" % "tinkergraph-gremlin" % gremlinVersion,
    "com.novocode" % "junit-interface" % "0.11" % "test->default",
    "com.chuusai" % "shapeless" % "2.1.0" cross CrossVersion.fullMapped {
      case "2.10.4" ⇒ "2.10.4"
      case v if v.startsWith("2.11") ⇒ "2.11"
      case v ⇒ v
    },
    "org.apache.tinkerpop" % "gremlin-test" % gremlinVersion % "test",
    "junit" % "junit" % junitVersion % "test",
    "org.scalatest" %% "scalatest" % "2.2.1" % "test"
  )
}

scalacOptions ++= Seq(
  //"-Xlog-implicits"
  //"-Ydebug"
)
// full stack traces
// testOptions in Test += Tests.Argument("-oF")

// doesn't work on travis ;(
// incOptions := incOptions.value.withNameHashing(true)

net.virtualvoid.sbt.graph.Plugin.graphSettings

resolvers += "Apache public" at "https://repository.apache.org/content/groups/public/"
resolvers += Resolver.mavenLocal

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
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

credentials += Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", System.getenv("SONATYPE_USER"), System.getenv("SONATYPE_PASS"))
// credentials += Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", "mpollmeier", "o_o")
