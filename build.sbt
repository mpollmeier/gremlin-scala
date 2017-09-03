name := "root"
val org = "com.michaelpollmeier"
organization := org

val defaultScalaV = "2.12.3"
scalaVersion := defaultScalaV
crossScalaVersions := Seq("2.11.11")
releaseCrossBuild := true
import ReleaseTransformations._

val gremlinVersion = "3.3.0"
val commonSettings = Seq(
  organization := org,
  scalaVersion := defaultScalaV,
  libraryDependencies ++= Seq(
      "org.apache.tinkerpop" % "gremlin-core" % gremlinVersion,
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "com.chuusai" %% "shapeless" % "2.3.2",
      "org.scala-lang.modules" %% "scala-xml" % "1.0.6", //just specified to eliminate sbt warnings
      "org.slf4j" % "slf4j-nop" % "1.7.25" % Test,
      "org.apache.tinkerpop" % "tinkergraph-gremlin" % gremlinVersion % Test,
      "org.apache.tinkerpop" % "gremlin-test" % gremlinVersion % Test,
      "org.scalatest" %% "scalatest" % "3.0.3" % Test,
      "org.scalamock" %% "scalamock-scalatest-support" % "3.5.0" % Test
  ),
  resolvers += "Apache public" at "https://repository.apache.org/content/groups/public/",
  scalacOptions ++= Seq(
    // "-Xlint"
    // "-Xfatal-warnings",
    // , "-Xlog-implicits"
    //"-Ydebug",
    "-language:implicitConversions",
    "-feature",
    "-deprecation" //hard to handle when supporting multiple scala versions...
  ),
  publishTo := { // format: off
    if (isSnapshot.value) Some("snapshots" at "https://oss.sonatype.org/content/repositories/snapshots")
    else Some("releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2")
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
  releaseCrossBuild := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    releaseStepCommand("publishSigned"),
    setNextVersion,
    commitNextVersion,
    releaseStepCommand("sonatypeReleaseAll"),
    pushChanges
  )
)


lazy val root = project.in(file("."))
  .aggregate(`gremlin-scala`, macros)
  .settings(publishArtifact := false)

lazy val `gremlin-scala` = project.in(file("gremlin-scala"))
  .settings(commonSettings: _*)
  .dependsOn(macros)

// macros can't be in the same compilation unit
lazy val macros = project.in(file("macros"))
  .settings(commonSettings: _*)
