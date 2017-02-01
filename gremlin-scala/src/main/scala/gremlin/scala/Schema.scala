package gremlin.scala

// a type safe key for a property of vertices or edges
case class Key[+A](value: String)

case class KeyValue[+A](key: Key[A], value: A)
