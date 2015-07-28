package gremlin.scala

import gremlin.scala.ScalaVertex
import scala.Product
import scala.language.experimental.macros
import scala.reflect.ClassTag
import scala.reflect.api.TypeTags.TypeTag
import scala.reflect.macros.blackbox.Context
import scala.reflect.api.Symbols.MethodSymbol

object MarshallingMacros {
  // Sources:
  // http://stackoverflow.com/questions/17223213/scala-macros-making-a-map-out-of-fields-of-a-class-in-scala
  // http://blog.echo.sh/post/65955606729/exploring-scala-macros-map-to-case-class-conversion

  /**
   * Case Class to Map
   */
  def toMap[P <: Product](cc: P): Map[String, Any] = macro toMapMacro(cc)
  
  def toMapMacro[P: c.WeakTypeTag](c: Context)(cc: c.Tree) = {
    import c.universe._
    
    val tpe = weakTypeOf[P]
    
    val toMapParams = tpe.declarations.collect {
      case field: MethodSymbol if field.isCaseAccessor =>
        val name = field.name
        val decoded = name.decoded
        val returnType = tpe.declaration(name).typeSignature
        // http://www.scala-lang.org/api/2.10.4/index.html#scala.reflect.api.Types$TypeApi
        val value = if (returnType == Product) toMapMacro(field) else q"$cc.$name"
        q"$decoded -> $value"
    }
    
    q"Map(..$toMapParams)"
  }

  /**
   * Map to Case Class
   */
  def fromMap[P <: Product](map: Map[String, Any]): Map[String, Any] = macro fromMapMacro[P](map)
  
  def fromMapMacro[P: c.WeakTypeTag](c: Context)(map: c.Tree) = {
    import c.universe._
    
    val tpe = weakTypeOf[P]
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
