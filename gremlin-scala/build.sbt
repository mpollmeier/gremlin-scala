name := "gremlin-scala"

libraryDependencies <++= scalaVersion { scalaVersion =>
  val gremlinVersion = "3.0.0-incubating"
  Seq(
    "org.apache.tinkerpop" % "gremlin-core" % gremlinVersion exclude("org.slf4j", "slf4j-log4j12"),
    "org.apache.tinkerpop" % "tinkergraph-gremlin" % gremlinVersion exclude("org.slf4j", "slf4j-log4j12"),
    "org.scala-lang" % "scala-reflect" % scalaVersion,
    "com.novocode" % "junit-interface" % "0.11" % "test->default",
    "com.chuusai" %% "shapeless" % "2.2.5",
    "org.apache.tinkerpop" % "gremlin-test" % gremlinVersion % Test,
    "junit" % "junit" % "4.12" % Test,
    "com.thinkaurelius.titan" % "titan-berkeleyje" % "0.9.0-M2",
    "org.scalatest" %% "scalatest" % "2.2.5" % Test
  )
}
resolvers += "Apache public" at "https://repository.apache.org/content/groups/public/"

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
