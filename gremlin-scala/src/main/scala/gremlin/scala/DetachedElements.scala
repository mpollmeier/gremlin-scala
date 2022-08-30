package gremlin.scala

import scala.jdk.CollectionConverters._

// useful e.g. for optional step with a default

object DetachedVertex {
  def apply(id: AnyRef = "detached",
            label: String = "detached",
            properties: Map[String, AnyRef] = Map.empty) =
    new org.apache.tinkerpop.gremlin.structure.util.detached.DetachedVertex(id,
                                                                            label,
                                                                            properties.asJava)
}

object DetachedEdge {
  def apply(id: AnyRef = "detached",
            label: String = "detached",
            properties: Map[String, AnyRef] = Map.empty,
            outVId: AnyRef = "",
            outVLabel: String = "",
            inVId: AnyRef = "",
            inVLabel: String = "") =
    new org.apache.tinkerpop.gremlin.structure.util.detached.DetachedEdge(id,
                                                                          label,
                                                                          properties.asJava,
                                                                          outVId,
                                                                          outVLabel,
                                                                          inVId,
                                                                          inVLabel)
}
