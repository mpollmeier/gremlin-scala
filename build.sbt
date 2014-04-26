name := "gremlin-scala"

version := "3.0.0-SNAPSHOT"

organization := "com.tinkerpop.gremlin"

scalaVersion := "2.11.0"

scalacOptions ++= Seq(
 // "-Xlog-implicits"
  /*"-Ydebug"*/
)

libraryDependencies <++= scalaVersion { scalaVersion =>
  val gremlinVersion = "3.0.0-SNAPSHOT"
  val junitVersion = "4.11"
  Seq(
    "com.tinkerpop.tinkerpop3" % "gremlin-core" % gremlinVersion,
    "com.tinkerpop.tinkerpop3" % "tinkergraph-gremlin" % gremlinVersion,
    "com.novocode" % "junit-interface" % "0.9" % "test->default",
    "com.chuusai" %% "shapeless" % "2.0.0",
    /*"org.scala-lang" % "scala-library" % scalaVersion,*/
    /*"org.scala-lang" % "scala-compiler" % scalaVersion,*/
    /*"org.scala-lang" % "jline" % scalaVersion,*/
    "com.tinkerpop.tinkerpop3" % "gremlin-test" % gremlinVersion % "test",
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
