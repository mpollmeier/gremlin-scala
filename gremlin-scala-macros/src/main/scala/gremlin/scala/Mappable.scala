package gremlin.scala

import scala.language.experimental.macros
import scala.reflect.ClassTag
import scala.reflect.macros.blackbox.Context


trait Mappable[T] {
  def toMap(t: T): Map[String, Any]
  def fromMap(map: Map[String, Any]): T
}

object Mappable {
  implicit def materializeMappable[T]: Mappable[T] =
  macro materializeMappableImpl[T]

  def materializeMappableImpl[T: c.WeakTypeTag](c: Context): c.Expr[Mappable[T]] = {
    import c.universe._
    val tpe = weakTypeOf[T]
    val companion = tpe.typeSymbol.companionSymbol

    val (toMapParams, fromMapParams) = tpe.declarations.collect {
      case field: MethodSymbol if field.isCaseAccessor =>
      val name = field.name
      val decoded = name.decoded
      val returnType = tpe.declaration(name).typeSignature

      (q"$decoded -> t.$name", q"map($decoded).asInstanceOf[$returnType]")
    }.unzip

    c.Expr[Mappable[T]] { q"""
      new Mappable[$tpe] {
        def toMap(t: $tpe): Map[String, Any] = Map(..$toMapParams)
        def fromMap(map: Map[String, Any]): $tpe = $companion(..$fromMapParams)
      }
    """ }
  }
}
