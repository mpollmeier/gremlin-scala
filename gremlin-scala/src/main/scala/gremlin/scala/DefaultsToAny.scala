package gremlin.scala

sealed class DefaultsToAny[A]

object DefaultsToAny {
  implicit def overrideDefault[A] = new DefaultsToAny[A]

  implicit def default = new DefaultsToAny[Any]
}
