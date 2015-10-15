package gremlin.scala

case class SemiEdge(from: Vertex, label: Label, properties: Map[String, Any] = Map.empty) {
  def -->(to: Vertex) = from.asScala.addEdge(label, to, properties)
}

case class SemiDoubleEdge(right: Vertex, label: Label, properties: Map[String, Any] = Map.empty)
