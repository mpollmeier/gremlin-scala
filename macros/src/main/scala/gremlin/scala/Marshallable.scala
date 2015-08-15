package gremlin.scala

import org.apache.tinkerpop.gremlin.structure.Graph.Hidden

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context
import org.apache.tinkerpop.gremlin.structure.T


trait Marshallable[P] {
  def fromCC(cc: P): (Option[AnyRef], String, Map[String, Any])

  def toCC(id: AnyRef, valueMap: Map[String, Any]): P
}

object Marshallable {
  implicit def materializeMappable[P]: Marshallable[P] = macro materializeMappableImpl[P]

  def materializeMappableImpl[P: c.WeakTypeTag](c: Context): c.Expr[Marshallable[P]] = {
    import c.universe._
    val tpe = weakTypeOf[P]
    val companion = tpe.typeSymbol.companionSymbol

    val (idParam, fromCCParams, toCCParams) = tpe.declarations
      .foldLeft[(Tree, Seq[Tree], Seq[Tree])]((q"None", Seq.empty, Seq.empty)) {
      case ((idParam, fromCCParams, toCCParams), field: MethodSymbol) if field.isCaseAccessor =>
        val name = field.name
        val decoded = name.decoded
        val returnType = tpe.declaration(name).typeSignature

        // @id as Option
        if ((field.annotations map (_.tpe) contains weakTypeOf[id]) &&
          returnType.typeSymbol == weakTypeOf[Option[_]].typeSymbol)
          (q"cc.$name.asInstanceOf[Option[AnyRef]]",
            fromCCParams,
            toCCParams :+ q"Option(id).asInstanceOf[$returnType]")
        // @id
        else if (field.annotations map (_.tpe) contains weakTypeOf[id])
          (q"Option(cc.$name.asInstanceOf[AnyRef])",
            fromCCParams,
            toCCParams :+ q"id.asInstanceOf[$returnType]")
        // Property
        else {
          assert(!Hidden.isHidden(decoded), s"The parameter name $decoded can't be used in the persistable case class $tpe")
          (idParam,
            fromCCParams :+ q"$decoded -> cc.$name",
            toCCParams :+ q"valueMap($decoded).asInstanceOf[$returnType]")
        }
      case (params, _) => params
    }

    // Label
    val label = tpe.typeSymbol.asClass.annotations find (_.tpe =:= weakTypeOf[label]) map { annotation =>
      val label = annotation.scalaArgs.head
      q"""$label"""
    } getOrElse q"cc.getClass.getSimpleName"

    c.Expr[Marshallable[P]] { q"""
      new Marshallable[$tpe] {
        def fromCC(cc: $tpe): (Option[AnyRef], String, Map[String, Any]) = ($idParam, $label, Map(..$fromCCParams))
        def toCC(id: AnyRef, valueMap: Map[String, Any]): $tpe = $companion(..$toCCParams)
      }
    """
    }
  }
}
