name := "gremlin-scala"

version := "3.0.0-SNAPSHOT"

organization := "com.tinkerpop.gremlin"

scalaVersion := "2.10.3"

scalacOptions ++= Seq(
 // "-Xlog-implicits"
  /*"-Ydebug"*/
)

libraryDependencies <++= scalaVersion { scalaVersion =>
  val gremlinVersion = "3.0.0-SNAPSHOT"
  val junitVersion = "4.11"
  Seq(
    "com.tinkerpop.gremlin" % "gremlin-core" % gremlinVersion,
    "com.tinkerpop" % "tinkergraph" % gremlinVersion,
    "com.tinkerpop.gremlin" % "gremlin-test" % gremlinVersion % "test",
    "junit" % "junit" % junitVersion % "test",
    "com.novocode" % "junit-interface" % "0.9" % "test->default",
    /*"com.tinkerpop.blueprints" % "blueprints-tinkergraph" % gremlinVersion,*/
    /*"com.tinkerpop.blueprints" % "blueprints-core" % gremlinVersion,*/
    /*"com.tinkerpop.blueprints" % "blueprints-io" % gremlinVersion,*/
    /*"com.tinkerpop.blueprints" % "blueprints-generator" % gremlinVersion,*/
    "com.chuusai" % "shapeless" % "2.0.0-M1" cross CrossVersion.full,
    /*"org.scala-lang" % "scala-library" % scalaVersion,*/
    /*"org.scala-lang" % "scala-compiler" % scalaVersion,*/
    /*"org.scala-lang" % "jline" % scalaVersion,*/
    "org.scalatest" %% "scalatest" % "2.0" % "test"
  )
}

resolvers ++= Seq(
  "Local Maven Repository" at Path.userHome.asFile.toURI.toURL + "/.m2/repository"
  /*"Maven Central" at "http://repo1.maven.org/maven2/",*/
  /*"Sonatype releases" at "http://oss.sonatype.org/content/repositories/releases/",*/
  /*"Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",*/
  /*"Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",*/
  /*"Aduna Software" at "http://repo.aduna-software.org/maven2/releases/", //for org.openrdf.sesame*/
  /*"Restlet Framework" at "http://maven.restlet.org"*/
)
