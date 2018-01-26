package gremlin.scala

// a type safe key for a property of vertices or edges
case class Key[A](name: String) {
  def ->(value: A): KeyValue[A] = KeyValue(this, value)

  def of(value: A): KeyValue[A] = KeyValue(this, value)
}

case class KeyValue[A](key: Key[A], value: A)
