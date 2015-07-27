lazy val gremlinScalaMacros = project.in(file("gremlin-scala-macros"))

lazy val gremlinScala = project.in(file("gremlin-scala")).dependsOn(gremlinScalaMacros)