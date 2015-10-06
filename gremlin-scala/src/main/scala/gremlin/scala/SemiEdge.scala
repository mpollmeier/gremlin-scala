package gremlin.scala

case class SemiEdge(from: Vertex, label: String, properties: Map[String, Any] = Map.empty) {

  def -->(to: Vertex) = from.addEdge(label, to, properties)
}

case class SemiDoubleEdge(right: Vertex, label: String, properties: Map[String, Any] = Map.empty)
