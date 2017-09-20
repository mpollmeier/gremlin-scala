package gremlin.scala

// a type safe key for a property of vertices or edges
case class Key[A](name: String) {
  def →(value: A): KeyValue[A] = KeyValue(this, value)

  def ->(value: A): KeyValue[A] = KeyValue(this, value)

  def of(value: A): KeyValue[A] = KeyValue(this, value)
}


case class KeyValue[A](key: Key[A], value: A)

trait WithVertex[A] {
  //returns a copy of itself with the underlying vertex set
  def withVertex(vertex: Vertex): A
}
