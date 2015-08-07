lazy val gremlinScalaMacros = project.in(file("gremlin-scala-macros"))

lazy val gremlinScalaCore = project.in(file("gremlin-scala-core")).dependsOn(gremlinScalaMacros)