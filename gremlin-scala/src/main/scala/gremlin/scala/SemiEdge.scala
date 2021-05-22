package gremlin.scala

case class SemiEdge(from: Vertex, label: String, properties: KeyValue[_]*) {

  /** `implicit ScalaGraph` required for configuration, e.g. when using remote graph */
  def -->(to: Vertex)(implicit graph: ScalaGraph) =
    from.asScala().addEdge(label, to, properties: _*)
}

case class SemiDoubleEdge(right: Vertex, label: String, properties: KeyValue[_]*)
