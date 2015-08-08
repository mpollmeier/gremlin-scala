package gremlin.scala

case class SemiEdge(from: ScalaVertex, label: String, properties: Map[String, Any] = Map.empty) {
  def ->(to: Vertex) = from.addEdge(label, to, properties)
}