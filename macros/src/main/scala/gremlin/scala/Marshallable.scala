package gremlin.scala

import org.apache.tinkerpop.gremlin.structure.Graph.Hidden
import scala.language.experimental.macros
import scala.reflect.macros.blackbox

trait Marshallable[P <: Product] {
  type Id = AnyRef
  type Label = String
  type ValueMap = Map[String, Any]
  case class FromCC(id: Option[Id], label: Label, valueMap: ValueMap)

  def fromCC(cc: P): FromCC
  def toCC(id: Id, valueMap: ValueMap): P
}

object Marshallable {
  implicit def materializeMappable[P <: Product]: Marshallable[P] = macro materializeMappableImpl[P]

  def materializeMappableImpl[P <: Product: c.WeakTypeTag](c: blackbox.Context): c.Expr[Marshallable[P]] = {
    import c.universe._
    val tpe = weakTypeOf[P]
    val companion = tpe.typeSymbol.companion

    val (idParam, fromCCParams, toCCParams) = tpe.decls
      .foldLeft[(Tree, Seq[Tree], Seq[Tree])]((q"None", Seq.empty, Seq.empty)) {
        case ((_idParam, _fromCCParams, _toCCParams), field: MethodSymbol) if field.isCaseAccessor ⇒
          val name = field.name
          val decoded = name.decodedName.toString
          val returnType = tpe.decl(name).typeSignature

          def idAsOption =
            (q"cc.$name.asInstanceOf[Option[AnyRef]]",
              _fromCCParams,
              _toCCParams :+ q"Option(id).asInstanceOf[$returnType]")

          def idAsAnyRef =
            (q"Option(cc.$name.asInstanceOf[AnyRef])",
              _fromCCParams,
              _toCCParams :+ q"id.asInstanceOf[$returnType]")

          def optionProperty =
            (_idParam,
              //TODO: setting the `__gs` property isn't necessary
              _fromCCParams :+ q"""cc.$name.map{ name => $decoded -> name }.getOrElse("__gs" -> "")""",
              _toCCParams :+ q"valueMap.get($decoded).asInstanceOf[$returnType]")

          def property =
            (_idParam,
              _fromCCParams :+ q"$decoded -> cc.$name",
              _toCCParams :+ q"valueMap($decoded).asInstanceOf[$returnType]")

          if (field.annotations map (_.tree.tpe) contains weakTypeOf[id]) {
            if (returnType.typeSymbol == weakTypeOf[Option[_]].typeSymbol) idAsOption
            else idAsAnyRef
          } else { // normal property member
            assert(!Hidden.isHidden(decoded), s"The parameter name $decoded can't be used in the persistable case class $tpe")
            if (returnType.typeSymbol == weakTypeOf[Option[_]].typeSymbol) optionProperty
            else property
          }

        case (params, _) ⇒ params
      }

    val label = tpe.typeSymbol.asClass.annotations find (_.tree.tpe =:= weakTypeOf[label]) map { annotation ⇒
      val label = annotation.tree.children.tail.head
      q"""$label"""
    } getOrElse q"cc.getClass.getSimpleName"

    c.Expr[Marshallable[P]] {
      q"""
      new Marshallable[$tpe] {
        def fromCC(cc: $tpe) = FromCC($idParam, $label, Map(..$fromCCParams))
        def toCC(id: AnyRef, valueMap: Map[String, Any]): $tpe = $companion(..$toCCParams)
      }
    """
    }
  }
}
