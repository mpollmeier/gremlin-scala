name := "gremlin-scala"

version := "3.0.0.M3"

organization := "com.michaelpollmeier"

crossScalaVersions := Seq("2.10.4", "2.11.2")

scalaVersion := "2.11.2"

scalacOptions ++= Seq(
  //"-Xlog-implicits"
  //"-Ydebug"
)

// doesn't work on travis ;(
// incOptions := incOptions.value.withNameHashing(true)

// full stack traces
// testOptions in Test += Tests.Argument("-oF")

libraryDependencies <++= scalaVersion { scalaVersion =>
  val gremlinVersion = "3.0.0.M3"
  val junitVersion = "4.11"
  Seq(
    "com.tinkerpop" % "gremlin-core" % gremlinVersion,
    "com.tinkerpop" % "tinkergraph-gremlin" % gremlinVersion,
    "com.novocode" % "junit-interface" % "0.9" % "test->default",
    "com.chuusai" % "shapeless" % "2.0.0" cross CrossVersion.fullMapped {
      case "2.10.4" ⇒ "2.10.4"
      case "2.11.2" ⇒ "2.11"
      case x ⇒ x
    },
    "com.tinkerpop" % "gremlin-test" % gremlinVersion % "test",
    "junit" % "junit" % junitVersion % "test",
    "org.scalatest" %% "scalatest" % "2.2.1" % "test"
  )
}

resolvers ++= Seq(
  "Local Maven Repository" at Path.userHome.asFile.toURI.toURL + "/.m2/repository"
  /*"Maven Central" at "http://repo1.maven.org/maven2/",*/
  /*"Sonatype releases" at "http://oss.sonatype.org/content/repositories/releases/",*/
  /*"Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",*/
  /*"Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",*/
)

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
  </developers>                                                                                 

credentials += Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", System.getenv("SONATYPE_USER"), System.getenv("SONATYPE_PASS"))
//credentials += Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", "mpollmeier", "o_o")
