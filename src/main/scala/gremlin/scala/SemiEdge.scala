package gremlin.scala

case class SemiEdge(out: Vertex, label: String, properties: Map[String, Any]) {
  def -->(in: Vertex) = out.addEdge(label, in).setProperties(properties)
}