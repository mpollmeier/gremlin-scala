package gremlin.scala

case class SemiEdge(from: ScalaVertex, label: String) {
  def ->(to: ScalaVertex) = from.addEdge(label, to)
}