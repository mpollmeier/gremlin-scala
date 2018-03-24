package gremlin.scala

case class SemiEdge(from: Vertex, label: String, properties: KeyValue[_]*) {
  def -->(to: Vertex) = from.asScala.addEdge(label, to, properties: _*)
}

case class SemiDoubleEdge(right: Vertex, label: String, properties: KeyValue[_]*)
