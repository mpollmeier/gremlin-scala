package gremlin.scala

import scala.language.experimental.macros
import scala.reflect.ClassTag
import scala.reflect.macros.blackbox.Context


trait Mappable[T] {
  def toMap(t: T): (String, Map[String, Any])

  def fromMap(label: String, valueMap: Map[String, Any]): T
}

object Mappable {
  implicit def materializeMappable[T]: Mappable[T] =
  macro materializeMappableImpl[T]

  def materializeMappableImpl[T: c.WeakTypeTag](c: Context): c.Expr[Mappable[T]] = {
    import c.universe._
    val tpe = weakTypeOf[T]
    val companion = tpe.typeSymbol.companionSymbol

    val (labelParam, toMapParams, fromMapParams) = tpe.declarations
      .foldLeft[(Tree, Seq[Tree], Seq[Tree])]((q"""t.getClass.getSimpleName""", Seq.empty, Seq.empty)) {
      case ((labelParam, toMapParams, fromMapParams), field: MethodSymbol) if field.isCaseAccessor =>
        val name = field.name
        val decoded = name.decoded
        val returnType = tpe.declaration(name).typeSignature
        val annotations = field.annotations
        println("===annotations= " + annotations)
        println("===all= " + decoded + " Annotations:" + annotations)
        if (field.annotations contains weakTypeOf[label]) {
          assert(returnType == weakTypeOf[String], "Bonjour") //TODO
          println("===label=")
          (q"t.$name",
            toMapParams,
            fromMapParams :+ q"label")
        } else {
          println("===params= " + decoded)
          (labelParam,
            toMapParams :+ q"$decoded -> t.$name",
            fromMapParams :+ q"valueMap($decoded).asInstanceOf[$returnType]")
        }
      case (params, _) => params
    }
    println(toMapParams)

    c.Expr[Mappable[T]] { q"""
      new Mappable[$tpe] {
        def toMap(t: $tpe): (String, Map[String, Any]) = ($labelParam, Map(..$toMapParams))
        def fromMap(label: String, valueMap: Map[String, Any]): $tpe = $companion(..$fromMapParams)
      }
    """
    }
  }
}
