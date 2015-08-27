package gremlin.scala

case class SemiEdge(from: ScalaVertex, label: String, properties: Map[String, Any] = Map.empty) {

  def -->(to: ScalaVertex) = from.addEdge(label, to, properties)
}

case class SemiDoubleEdge(right: ScalaVertex, label: String, properties: Map[String, Any] = Map.empty)