package gremlin.scala

// graph dependent type, e.g. Id[Integer] for TinkerGraph
case class Id[IdType](value: IdType)

// a type safe key for a property of vertices or edges
case class Key[A](value: String)

case class KeyValue[A]( key: Key[A], value: A)
