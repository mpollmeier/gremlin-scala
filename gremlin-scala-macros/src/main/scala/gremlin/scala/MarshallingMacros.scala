package gremlin.scala

import scala.language.experimental.macros
import scala.reflect.ClassTag
import scala.reflect.macros.blackbox.Context

object MarshallingMacros {
  // Sources:
  // http://stackoverflow.com/questions/17223213/scala-macros-making-a-map-out-of-fields-of-a-class-in-scala
  // http://blog.echo.sh/post/65955606729/exploring-scala-macros-map-to-case-class-conversion

  /**
   * Case Class to Map
   */
  def toMap[P <: Product](cc: P): Map[String, Any] = macro toMapMacro[P]
  
  def toMapMacro[P: c.WeakTypeTag](c: Context)(cc: c.Expr[P]): c.Tree = {
    import c.universe._

//    println(showRaw(cc))
//    val q = tq"Product".symbol.typeSignature

//    val q"case class $className(..$fields) extends ..$parents { ..$body }" = cc
//    val f = fields: List[ValDef]

    q"Map()"

    val tpe = weakTypeOf[P]
    require(tpe.typeSymbol.asClass.isCaseClass)
    tpe.baseClasses // All types

    val toMapParams = tpe.declarations.collect {
      case field: MethodSymbol if field.isCaseAccessor =>
        val name = field.name
        val decoded = name.decoded
        val returnType = tpe.declaration(name).typeSignature
        // http://www.scala-lang.org/api/2.10.4/index.html#scala.reflect.api.Types$TypeApi
        val value = /*if (tpe.baseClasses contains tq"Product".symbol) toMapMacro(c)(field) else*/ q"$cc.$name"
        q"$decoded -> $value"
    }

    q"Map(..$toMapParams)"
  }

  /**
   * Map to Case Class
   */
  def fromMap[A <: Any](map: Map[String, Any]): A = macro fromMapMacro[A]

  def fromMapMacro[A: c.WeakTypeTag](c: Context)(map: c.Tree) = {
    import c.universe._

    val tpe = weakTypeOf[A]
    require(tpe.typeSymbol.asClass.isCaseClass)
    val companion = tpe.typeSymbol.companionSymbol

    val fromMapParams = tpe.declarations.collect {
      case field: MethodSymbol if field.isCaseAccessor =>
        val name = field.name
        val decoded = name.decoded
        val returnType = tpe.declaration(name).typeSignature
        q"map($decoded).asInstanceOf[$returnType]"
    }

    q"$companion(..$fromMapParams)"
  }
}
