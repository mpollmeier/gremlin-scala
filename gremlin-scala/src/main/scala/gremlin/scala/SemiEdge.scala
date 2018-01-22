package gremlin.scala

case class SemiEdge(from: Vertex, label: String, properties: Seq[KeyValue[_]] = Nil) {
  def -->(to: Vertex) = from.asScala.addEdge(label, to, properties)
}

case class SemiDoubleEdge(right: Vertex, label: String, properties: Seq[KeyValue[_]] = Nil)
