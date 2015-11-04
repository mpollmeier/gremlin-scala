package gremlin.scala

// a type safe key for a property of a vertex/edge
case class Key[A](value: String)

case class KeyValue[A]( key: Key[A], value: A) 
