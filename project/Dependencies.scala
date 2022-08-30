import sbt._

object Dependencies {
  object V {
    val gremlin = "3.6.1"
    val scalaTest = "3.2.12"
    val shapeless = "2.3.9"
    val shapeless3 = "3.0.1"
    val scalaCollectionCompat = "2.8.1"
    val slf4j = "1.7.36"
    val scalamock = "5.2.0"
  }

  val common: Seq[ModuleID] = Seq(
    "org.apache.tinkerpop" % "gremlin-core" % V.gremlin,
    "org.scala-lang.modules" %% "scala-collection-compat" % V.scalaCollectionCompat,
    "org.slf4j" % "slf4j-nop" % V.slf4j % Test,
    "org.apache.tinkerpop" % "tinkergraph-gremlin" % V.gremlin % Test,
    "org.apache.tinkerpop" % "gremlin-test" % V.gremlin % Test,
    "org.scalatest" %% "scalatest-shouldmatchers" % V.scalaTest % Test,
    "org.scalatest" %% "scalatest-wordspec" % V.scalaTest % Test,
    "org.scalatest" %% "scalatest-funspec" % V.scalaTest % Test,
  )

  val common_scala2: Seq[ModuleID] = Seq(
    "com.chuusai" %% "shapeless" % V.shapeless
  )

  val common_scala3: Seq[ModuleID] = Seq(
    "org.typelevel" %% "shapeless3-deriving" % V.shapeless3
  )

  def commonForVersion(scalaVersion: String): Seq[ModuleID] =
    common ++ {
      CrossVersion.partialVersion(scalaVersion) match {
        case Some((2, _)) => common_scala2
        case _ => common_scala3
      }
    }

  def macroDepsForVersion(scalaVersion: String): Seq[ModuleID] =
    commonForVersion(scalaVersion) ++ {
      CrossVersion.partialVersion(scalaVersion) match {
        case Some((2, _)) => Seq("org.scala-lang" % "scala-reflect" % scalaVersion)
        case _ => Nil
      }
    }

  val resolvers: Seq[Resolver] = Seq(
    "Apache public".at("https://repository.apache.org/content/groups/public/"),
    Resolver.mavenLocal
  )
}
