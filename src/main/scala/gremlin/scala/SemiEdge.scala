package gremlin.scala

case class SemiEdge(from: Vertex, label: String, properties: Map[String, Any]) {
  def ->(to: Vertex) = from.addEdge(label, to).setProperties(properties)

  def <->(to: Vertex) = {
    val e0: Edge = from.addEdge(label, to).setProperties(properties)
    val e1: Edge = to.addEdge(label, from).setProperties(properties)
//    (e0, e1)
    to.graph().E(e0, e1)
  }
}