name := "root"
organization in ThisBuild := "com.michaelpollmeier"

scalaVersion in ThisBuild := "2.12.6"
crossScalaVersions := Seq(scalaVersion.value, "2.11.12" /*, "2.13.0-M4"*/ )

val gremlinVersion = "3.3.3"
val commonSettings = Seq(
  libraryDependencies ++= Seq(
    "org.apache.tinkerpop" % "gremlin-core" % gremlinVersion,
    "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    "com.chuusai" %% "shapeless" % "2.3.3",
    "org.scala-lang.modules" %% "scala-xml" % "1.0.6", //just specified to eliminate sbt warnings
    "org.slf4j" % "slf4j-nop" % "1.7.25" % Test,
    "org.apache.tinkerpop" % "tinkergraph-gremlin" % gremlinVersion % Test,
    "org.apache.tinkerpop" % "gremlin-test" % gremlinVersion % Test,
    "org.scalatest" %% "scalatest" % "3.0.5" % Test,
    "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % Test
  ),
  resolvers += "Apache public".at("https://repository.apache.org/content/groups/public/"),
  resolvers += Resolver.mavenLocal,
  scalacOptions ++= Seq(
    // "-Xlint"
    // "-Xfatal-warnings",
    // , "-Xlog-implicits"
    //"-Ydebug",
    "-language:implicitConversions",
    "-language:existentials",
    "-feature",
    "-deprecation" //hard to handle when supporting multiple scala versions...
  ),
  Test / console / initialCommands :=
    """|import gremlin.scala._
       |import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory
       |import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
       |import org.apache.tinkerpop.gremlin.process.traversal.{Order, P, Scope}
       |implicit val graph = TinkerFactory.createModern.asScala
       |val g = graph.traversal""".stripMargin,
  scmInfo := Some(
    ScmInfo(url("https://github.com/mpollmeier/gremlin-scala"),
            "scm:git@github.com:mpollmeier/gremlin-scala.git")),
  developers := List(
    Developer("mpollmeier",
              "Michael Pollmeier",
              "michael@michaelpollmeier.com",
              url("https://michaelpollmeier.com"))),
  homepage := Some(url("https://github.com/mpollmeier/gremlin-scala")),
  licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")),
  publishTo := sonatypePublishTo.value,
  publishArtifact in Test := false
)

lazy val root = project
  .in(file("."))
  .aggregate(`gremlin-scala`, macros)
  .settings(skip in publish := true, publishTo := {
    Some("publishMeNot".at("https://publish/me/not"))
  })

lazy val `gremlin-scala` = project
  .in(file("gremlin-scala"))
  .settings(commonSettings: _*)
  .dependsOn(macros)

// macros can't be in the same compilation unit
lazy val macros = project
  .in(file("macros"))
  .settings(commonSettings: _*)

ThisBuild / scalafmtOnCompile := true
