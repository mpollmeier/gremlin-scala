package gremlin.scala

case class BiDirectionalEdge(out: Vertex, label: String, properties: Map[String, Any]) {
  def <-->(in: Vertex): (ScalaEdge, ScalaEdge) = {
    val e0: Edge = out.addEdge(label, in).setProperties(properties)
    val e1: Edge = in.addEdge(label, out).setProperties(properties)
    (e0, e1)
  }
}