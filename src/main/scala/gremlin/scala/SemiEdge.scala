package gremlin.scala

case class SemiEdge(from: Vertex, label: String, properties: Map[String, Any]) {
  def ->(to: Vertex) = from.addEdge(label, to).setProperties(properties)
}