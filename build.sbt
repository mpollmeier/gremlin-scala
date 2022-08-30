
val Scala211 = "2.11.12"
val Scala212 = "2.12.16"
val Scala213 = "2.13.8"
val Scala3   = "3.1.3"

ThisBuild / organization := "com.michaelpollmeier"
ThisBuild / scalaVersion := Scala213
ThisBuild / crossScalaVersions := Seq(Scala211, Scala212, Scala213, Scala3)

def scalacOptionsVersion(scalaVersion: String): Seq[String] = {
  Seq(
    // "-Xlint"
    // "-Xfatal-warnings",
    // , "-Xlog-implicits"
    //"-Ydebug",
    "-language:implicitConversions",
    "-language:existentials",
    "-feature",
    "-deprecation" //hard to handle when supporting multiple scala versions...
  ) ++ (CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, _)) => Seq("-target:jvm-1.8")
    case _ => Seq("-explain")
  })
}

ThisBuild / scalacOptions ++= scalacOptionsVersion(scalaVersion.value)

ThisBuild / Test / console / initialCommands :=
  """|import gremlin.scala._
     |import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory
     |import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
     |import org.apache.tinkerpop.gremlin.process.traversal.{Order, P, Scope}
     |implicit val graph = TinkerFactory.createModern.asScala
     |val g = graph.traversal""".stripMargin

ThisBuild / scmInfo := Some(
  ScmInfo(url("https://github.com/mpollmeier/gremlin-scala"),
          "scm:git@github.com:mpollmeier/gremlin-scala.git"))
ThisBuild / developers := List(
  Developer("mpollmeier",
            "Michael Pollmeier",
            "michael@michaelpollmeier.com",
            url("https://michaelpollmeier.com")))
ThisBuild / homepage := Some(url("https://github.com/mpollmeier/gremlin-scala"))
ThisBuild / licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html"))
ThisBuild / publishTo := sonatypePublishToBundle.value

// virtual root project
name := "root"
publish / skip := true

lazy val macros = project // macros must be in a separate compilation unit
  .in(file("macros"))
  .settings(
    resolvers ++= Dependencies.resolvers,
    libraryDependencies ++= Dependencies.macroDepsForVersion(scalaVersion.value)
  )

lazy val `gremlin-scala` =
  project
    .in(file("gremlin-scala"))
    .settings(
      resolvers ++= Dependencies.resolvers,
      libraryDependencies ++= Dependencies.commonForVersion(scalaVersion.value)
    )
    .dependsOn(macros)
