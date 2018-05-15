name := "root"
organization in ThisBuild := "com.michaelpollmeier"

scalaVersion in ThisBuild := "2.12.6"
crossScalaVersions := Seq(scalaVersion.value, "2.11.12")
releaseCrossBuild := true

import ReleaseTransformations._
val gremlinVersion = "3.3.2"
val commonSettings = Seq(
  libraryDependencies ++= Seq(
    "org.apache.tinkerpop" % "gremlin-core" % gremlinVersion,
    "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    "com.chuusai" %% "shapeless" % "2.3.3",
    "org.scala-lang.modules" %% "scala-xml" % "1.0.6", //just specified to eliminate sbt warnings
    "org.slf4j" % "slf4j-nop" % "1.7.25" % Test,
    "org.apache.tinkerpop" % "tinkergraph-gremlin" % gremlinVersion % Test,
    "org.apache.tinkerpop" % "gremlin-test" % gremlinVersion % Test,
    "org.scalatest" %% "scalatest" % "3.0.3" % Test,
    "org.scalamock" %% "scalamock-scalatest-support" % "3.5.0" % Test
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
  publishTo := { // format: off
    if (isSnapshot.value) Some("snapshots".at("https://oss.sonatype.org/content/repositories/snapshots"))
    else Some("releases".at("https://oss.sonatype.org/service/local/staging/deploy/maven2"))
  },
  homepage := Some(url("https://github.com/mpollmeier/gremlin-scala")),
  licenses +=("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  pomExtra :=
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
    </developers>, // format: on
  releasePublishArtifactsAction := PgpKeys.publishSigned.value
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
