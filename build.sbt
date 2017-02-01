name := "root"
val org = "com.michaelpollmeier"
organization := org
publishArtifact := false

val gremlinVersion = "3.2.3"

val defaultScalaV = "2.12.1"
scalaVersion := defaultScalaV // if not using crossScalaVersions, i.e. prefixing sbt command with `+`
crossScalaVersions := Seq("2.11.8", defaultScalaV)
releaseCrossBuild := true

val commonSettings = Seq(
  organization := org,
  licenses +=("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")),
  homepage := Some(url("https://github.com/mpollmeier/gremlin-scala")),
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  scalaVersion := defaultScalaV,
  libraryDependencies ++= Seq(
      "org.apache.tinkerpop" % "gremlin-core" % gremlinVersion,
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "com.chuusai" %% "shapeless" % "2.3.2",
      "org.scala-lang.modules" %% "scala-xml" % "1.0.6", //just specified to eliminate sbt warnings
      "org.apache.tinkerpop" % "tinkergraph-gremlin" % gremlinVersion % Test,
      "org.apache.tinkerpop" % "gremlin-test" % gremlinVersion % Test,
      "org.scalatest" %% "scalatest" % "3.0.1" % Test
  ),
  resolvers += "Apache public" at "https://repository.apache.org/content/groups/public/",
  scalacOptions ++= Seq(
    "-Xlint"
    // "-Xfatal-warnings",
    // "-feature"
    // "-deprecation", //hard to handle when supporting multiple scala versions...
    // , "-Xlog-implicits"
    //"-Ydebug"
  ),
  incOptions := incOptions.value.withNameHashing(true), // doesn't work on travis ;(
  publishTo := {
    val sonatype = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at sonatype + "content/repositories/snapshots")
    else
      Some("releases" at sonatype + "service/local/staging/deploy/maven2")
  },
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
      </developers>
)

lazy val root = project.in(file("."))
  .aggregate(`gremlin-scala`, macros)

lazy val `gremlin-scala` = project.in(file("gremlin-scala"))
  .settings(commonSettings: _*)
  .dependsOn(macros)

// macros can't be in the same compilation unit according to joan: https://github.com/mpollmeier/gremlin-scala/issues/100
lazy val macros = project.in(file("macros"))
  .settings(commonSettings: _*)
