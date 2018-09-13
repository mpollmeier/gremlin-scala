package gremlin.scala

import org.apache.tinkerpop.gremlin.structure.Graph.Hidden
import org.apache.tinkerpop.gremlin.structure.Element
import scala.language.experimental.macros
import scala.reflect.macros.blackbox

trait Marshallable[CC <: Product] {
  type Id = AnyRef
  type Label = String
  type ValueMap = Map[String, Any]
  case class FromCC(id: Option[Id], label: Label, valueMap: ValueMap)

  def fromCC(cc: CC): FromCC
  def toCC(element: Element): CC
}

object Marshallable {
  implicit def materializeMappable[CC <: Product]: Marshallable[CC] =
    macro materializeMappableImpl[CC]

  def materializeMappableImpl[CC <: Product: c.WeakTypeTag](
      c: blackbox.Context): c.Expr[Marshallable[CC]] = {
    import c.universe._
    val tpe = weakTypeOf[CC]
    val companion = tpe.typeSymbol.companion

    val (idParam, fromCCParams, toCCParams) = tpe.decls
      .foldLeft[(Tree, Seq[Tree], Seq[Tree])]((q"None", Seq.empty, Seq.empty)) {
        case ((_idParam, _fromCCParams, _toCCParams), field: MethodSymbol)
            if field.isCaseAccessor =>
          val name = field.name
          val decoded = name.decodedName.toString
          val returnType = field.returnType

          def optionProperty = {
            // check if the property is an Option[AnyVal] and try to extract everything we need to unwrap it
            val treesForOptionValue = for {
              innerValueClassType <- returnType.typeArgs.headOption
              if innerValueClassType <:< typeOf[AnyVal]
              valueName <- valueGetter(innerValueClassType).map(_.name)
              wrappedType <- wrappedTypeMaybe(innerValueClassType)
            } yield {
              val valueClassCompanion = innerValueClassType.typeSymbol.companion
              (_idParam,
               //TODO: setting the `__gs` property isn't necessary
               _fromCCParams :+ q"""cc.$name.map{ name => $decoded -> name.$valueName }.getOrElse("__gs" -> "")""",
               _toCCParams :+ q"Option(element.value[$wrappedType]($decoded)).map($valueClassCompanion.apply).asInstanceOf[$returnType]")
            }
            treesForOptionValue.getOrElse { //normal option property
              (_idParam,
               //TODO: setting the `__gs` property isn't necessary
               _fromCCParams :+ q"""cc.$name.map{ name => $decoded -> name }.getOrElse("__gs" -> "")""",
               _toCCParams :+ q"element.property($decoded).toOption.asInstanceOf[$returnType]")
            }
          }

          def property = {
            // check if the property is a value class and try to extract everything we need to unwrap it
            val treesForValueClass = for {
              valueName <- valueGetter(returnType)
              if returnType <:< typeOf[AnyVal]
              wrappedType <- wrappedTypeMaybe(returnType)
            } yield {
              val valueClassCompanion = returnType.typeSymbol.companion
              (_idParam,
               _fromCCParams :+ q"$decoded -> cc.$name.$valueName",
               _toCCParams :+ q"$valueClassCompanion(element.value[$wrappedType]($decoded)).asInstanceOf[$returnType]")
            }
            treesForValueClass.getOrElse { //normal property
              (_idParam,
               _fromCCParams :+ q"$decoded -> cc.$name",
               _toCCParams :+ q"element.value[$returnType]($decoded)")
            }
          }

          def valueGetter(tpe: Type): Option[MethodSymbol] =
            tpe.declarations.sorted
              .filter(_.isMethod)
              .map(_.asMethod)
              .takeWhile(!_.isConstructor)
              .filter(_.paramLists == Nil /* nullary */ )
              .headOption

          def valueClassConstructor(tpe: Type): Option[MethodSymbol] =
            tpe.companion.decls
              .filter(_.name.toString == "apply")
              .headOption match {
              case Some(m: MethodSymbol) => Some(m)
              case _                     => None
            }

          def wrappedTypeMaybe(tpe: Type): Option[Type] =
            util
              .Try(valueClassConstructor(tpe).get.paramLists.head.head.typeSignature)
              .toOption

          if (field.annotations.map(_.tree.tpe) contains weakTypeOf[id]) { // @id
            assert(
              returnType.typeSymbol == weakTypeOf[Option[_]].typeSymbol,
              "@id parameter *must* be of type `Option[A]`. In the context of " +
                "Marshallable, we have to let the graph assign an id"
            )
            (q"cc.$name.asInstanceOf[Option[AnyRef]]",
             _fromCCParams,
             _toCCParams :+ q"Option(element.id).asInstanceOf[$returnType]")

          } else if (field.annotations.map(_.tree.tpe) contains weakTypeOf[underlying]) { // @underlying
            assert(
              returnType.typeSymbol == weakTypeOf[Option[_]].typeSymbol,
              "@underlying parameter *must* be of type `Option[A]`, since" +
                " it can only be defined after it has been added to the graph")
            (q"cc.$name.asInstanceOf[Option[AnyRef]]",
             _fromCCParams,
             _toCCParams :+ q"Option(element).asInstanceOf[$returnType]")
          } else { // normal property member
            assert(!Hidden.isHidden(decoded),
                   s"The parameter name $decoded can't be used in the persistable case class $tpe")
            if (returnType.typeSymbol == weakTypeOf[Option[_]].typeSymbol)
              optionProperty
            else
              property
          }

        case (params, _) => params
      }

    val label = tpe.typeSymbol.asClass.annotations
      .find(_.tree.tpe =:= weakTypeOf[label])
      .map { annotation =>
        val label = annotation.tree.children.tail.head
        q"""$label"""
      }
      .getOrElse(q"cc.getClass.getSimpleName")

    val ret = c.Expr[Marshallable[CC]] {
      q"""
      import gremlin.scala._
      new Marshallable[$tpe] {
        def fromCC(cc: $tpe) = FromCC($idParam, $label, Map(..$fromCCParams))
        def toCC(element: Element): $tpe = $companion(..$toCCParams)
      }
      """
    }
    // if (tpe.toString == "gremlin.scala.CCWithUnderlyingVertex") {
    //   println(ret)
    // }
    ret
  }
}
