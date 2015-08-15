package gremlin.scala

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context


trait Marshallable[T] {
  def fromCC(t: T): (String, Map[String, Any])

  def toCC(label: String, valueMap: Map[String, Any]): T
}

object Marshallable {
  implicit def materializeMappable[T]: Marshallable[T] =
  macro materializeMappableImpl[T]

  def materializeMappableImpl[T: c.WeakTypeTag](c: Context): c.Expr[Marshallable[T]] = {
    import c.universe._
    val tpe = weakTypeOf[T]
    val companion = tpe.typeSymbol.companionSymbol

    val (labelParam, toMapParams, fromMapParams) = tpe.declarations
      .foldLeft[(Tree, Seq[Tree], Seq[Tree])]((q"""t.getClass.getSimpleName""", Seq.empty, Seq.empty)) {
      case ((labelParam, toMapParams, fromMapParams), field: MethodSymbol) if field.isCaseAccessor =>
        val name = field.name
        val decoded = name.decoded
        val returnType = tpe.declaration(name).typeSignature.resultType

        if (field.annotations map (_.tpe) contains weakTypeOf[label]) {
          assert(returnType =:= weakTypeOf[String], "The label should be of type String")
          (q"t.$name",
            toMapParams,
            fromMapParams :+ q"label")
        } else {
          (labelParam,
            toMapParams :+ q"$decoded -> t.$name",
            fromMapParams :+ q"valueMap($decoded).asInstanceOf[$returnType]")
        }
      case (params, _) => params
    }

    c.Expr[Marshallable[T]] { q"""
      new Marshallable[$tpe] {
        def fromCC(t: $tpe): (String, Map[String, Any]) = ($labelParam, Map(..$toMapParams))
        def toCC(label: String, valueMap: Map[String, Any]): $tpe = $companion(..$fromMapParams)
      }
    """
    }
  }
}
