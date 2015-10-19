package gremlin.scala

import schema.Key

case class SemiEdge(from: Vertex, label: String, properties: Map[Key[_], Any] = Map.empty) {
  def -->(to: Vertex) = from.asScala.addEdge(label, to, properties)
}

case class SemiDoubleEdge(right: Vertex, label: String, properties: Map[Key[_], Any] = Map.empty)
