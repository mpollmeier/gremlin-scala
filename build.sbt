name := "gremlin-scala"

version := "3.0.0-SNAPSHOT"

organization := "com.michaelpollmeier"

crossScalaVersions := Seq("2.10.4", "2.11.2")

scalaVersion := "2.11.2"

scalacOptions ++= Seq(
  //"-Xlog-implicits"
  //"-Ydebug"
)

// doesn't work on travis ;(
// incOptions := incOptions.value.withNameHashing(true)

libraryDependencies <++= scalaVersion { scalaVersion =>
  val gremlinVersion = "3.0.0.M1"
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
    "org.scalatest" %% "scalatest" % "2.1.4" % "test"
  )
}

resolvers ++= Seq(
  "Local Maven Repository" at Path.userHome.asFile.toURI.toURL + "/.m2/repository"
  /*"Maven Central" at "http://repo1.maven.org/maven2/",*/
  /*"Sonatype releases" at "http://oss.sonatype.org/content/repositories/releases/",*/
  /*"Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",*/
  /*"Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",*/
)
