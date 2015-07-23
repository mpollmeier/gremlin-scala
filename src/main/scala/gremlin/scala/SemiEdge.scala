package gremlin.scala

case class SemiEdge(from: Vertex, label: String) {
  def ->(to: Vertex) = from.addEdge(label, to)
}