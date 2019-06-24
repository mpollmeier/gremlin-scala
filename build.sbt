val gremlinVersion = "3.4.1"

inThisBuild(
  Seq(
    organization := "com.michaelpollmeier",
    // scalaVersion := "2.12.8",
    scalaVersion := "2.13.0",
    crossScalaVersions := Seq("2.11.12", "2.12.8", "2.13.0"),
    libraryDependencies ++= Seq(
      "org.apache.tinkerpop" % "gremlin-core" % gremlinVersion,
      "com.chuusai" %% "shapeless" % "2.3.3",
      "org.slf4j" % "slf4j-nop" % "1.7.25" % Test,
      "org.apache.tinkerpop" % "tinkergraph-gremlin" % gremlinVersion % Test,
      "org.apache.tinkerpop" % "gremlin-test" % gremlinVersion % Test,
      "org.scalatest" %% "scalatest" % "3.0.8" % Test,
      "org.scalamock" %% "scalamock" % "4.3.0" % Test
    ),
    resolvers += "Apache public".at("https://repository.apache.org/content/groups/public/"),
    resolvers += Resolver.mavenLocal,
    scalacOptions ++= Seq(
      // "-Xlint"
      // "-Xfatal-warnings",
      // , "-Xlog-implicits"
      //"-Ydebug",
      "-target:jvm-1.8",
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
    licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html"))
  )
)

lazy val macros = project // macros must be in a separate compilation unit
  .in(file("macros"))
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.scala-lang" % "scala-compiler" % scalaVersion.value
    ))
lazy val `gremlin-scala` = project.in(file("gremlin-scala")).dependsOn(macros)

ThisBuild / publishTo := sonatypePublishTo.value
enablePlugins(GitVersioning)

// virtual root project settings
name := "root"
publish / skip := true
