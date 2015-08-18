package gremlin.scala

case class SemiEdge(from: ScalaVertex, properties: Map[String, Any]) {
  lazy val label = properties("label").asInstanceOf[String]

  def -->(to: ScalaVertex) = from.addEdge(label, to, properties)
}

case class SemiDoubleEdge(right: ScalaVertex, properties: Map[String, Any]) {
  lazy val label = properties("label").asInstanceOf[String]
}