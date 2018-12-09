package gremlin.scala

import org.apache.tinkerpop.gremlin.structure.Graph.Hidden
import org.apache.tinkerpop.gremlin.structure.Element
import scala.language.experimental.macros
import scala.reflect.macros.blackbox

trait Marshallable[CC <: Product] {
  type Id = AnyRef
  type Label = String
  case class FromCC(id: Option[Id], label: Label, properties: List[(String, Any)])

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
      .foldLeft[(Tree, Seq[Tree], Seq[Tree])]((q"_root_.scala.None", Seq.empty, Seq.empty)) {
        case ((_idParam, _fromCCParams, _toCCParams), field: MethodSymbol)
            if field.isCaseAccessor =>
          val name = field.name
          val decoded = name.decodedName.toString
          val returnType = field.returnType

          def handleStandardProperty(nullable: Boolean) = {
            // check if the property is a value class and try to extract everything we need to unwrap it
            val treesForValueClass = for {
              valueName <- valueGetter(returnType)
              if returnType <:< typeOf[AnyVal]
              wrappedType <- wrappedTypeMaybe(returnType)
            } yield { // ValueClass property
              val valueClassCompanion = returnType.typeSymbol.companion
              (_idParam,
               _fromCCParams :+ q"_root_.scala.collection.immutable.List($decoded -> cc.$name.$valueName)",
               _toCCParams :+ q"$valueClassCompanion(element.value[$wrappedType]($decoded)).asInstanceOf[$returnType]")
            }
            treesForValueClass.getOrElse { //normal property
              val toCCParams =
                if (!nullable) {
                  q"element.value[$returnType]($decoded)"
                } else {
                  // for people who like to shoot themselves in the foot
                  q"new _root_.gremlin.scala.PropertyOps(element.property[$returnType]($decoded)).toOption.orNull"
                }
              (_idParam,
               _fromCCParams :+ q"_root_.scala.collection.immutable.List($decoded -> cc.$name)",
               _toCCParams :+ toCCParams)
            }
          }

          def handleOptionProperty = {
            // check if the property is an Option[AnyVal] and try to extract everything we need to unwrap it
            returnType.typeArgs.headOption match {
              case Some(innerAnyValClassType) if innerAnyValClassType <:< typeOf[AnyVal] =>
                valueGetter(innerAnyValClassType) match {
                  case Some(wrappedValueGetter) => //Option[ValueClass]
                    val valueClassCompanion = innerAnyValClassType.typeSymbol.companion
                    (_idParam,
                     _fromCCParams :+
                       q"""
                           _root_.scala.collection.immutable.List(
                             cc.$name.map{ x => $decoded -> x.${wrappedValueGetter.name} }.getOrElse($decoded -> null)
                           )
                       """,
                     _toCCParams :+
                       q"""
                           new _root_.gremlin.scala.PropertyOps(element.property($decoded))
                             .toOption
                             .map($valueClassCompanion.apply)
                             .asInstanceOf[$returnType]
                       """)
                  case None => // Option[AnyVal]
                    (_idParam,
                     _fromCCParams :+
                       q"""
                           _root_.scala.collection.immutable.List(
                             cc.$name.map{ x => $decoded -> x }.getOrElse($decoded -> null)
                           )
                       """,
                     _toCCParams :+
                       q"""
                           new _root_.gremlin.scala.PropertyOps(element.property($decoded))
                             .toOption
                             .asInstanceOf[$returnType]
                       """)
                }

              case _ => // normal option property
                (_idParam,
                 _fromCCParams :+ q"_root_.scala.collection.immutable.List($decoded -> cc.$name.orNull)",
                 _toCCParams :+
                   q"""
                       new _root_.gremlin.scala.PropertyOps(element.property($decoded))
                         .toOption
                         .asInstanceOf[$returnType]
                   """)
            }
          }

          def handleListProperty = {
            (_idParam,
             _fromCCParams :+ q"cc.$name.map { x => $decoded -> x }",
             _toCCParams :+
               q"""
                   element.properties($decoded)
                     .asScala
                     .toList
                     .map(_.value)
                     .asInstanceOf[$returnType]
               """)
            //       element.properties[String]("ss").asScala.toList.map(_.value),
            // _toCCParams :+ q"element.value[$returnType]($decoded)")
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

          def handleId = {
            assert(
              returnType.typeSymbol == weakTypeOf[Option[_]].typeSymbol,
              "@id parameter *must* be of type `Option[A]`. In the context of " +
                "Marshallable, we have to let the graph assign an id"
            )
            (q"cc.$name.asInstanceOf[_root_.scala.Option[AnyRef]]",
             _fromCCParams,
             _toCCParams :+ q"_root_.scala.Option(element.id).asInstanceOf[$returnType]")
          }

          def handleUnderlying = {
            assert(
              returnType.typeSymbol == weakTypeOf[Option[_]].typeSymbol,
              "@underlying parameter *must* be of type `Option[A]`, since" +
                " it can only be defined after it has been added to the graph"
            )
            (q"cc.$name.asInstanceOf[_root_.scala.Option[AnyRef]]",
             _fromCCParams,
             _toCCParams :+ q"_root_.scala.Option(element).asInstanceOf[$returnType]")
          }

          // main control flow
          if (field.annotations.map(_.tree.tpe) contains weakTypeOf[id]) {
            handleId // @id
          } else if (field.annotations.map(_.tree.tpe) contains weakTypeOf[underlying]) {
            handleUnderlying // @underlying
          } else { // normal property member
            assert(!Hidden.isHidden(decoded),
                   s"The parameter name $decoded can't be used in the persistable case class $tpe")
            if (returnType.typeSymbol == weakTypeOf[Option[_]].typeSymbol) {
              handleOptionProperty
            } else if (returnType.typeSymbol == weakTypeOf[List[_]].typeSymbol) {
              handleListProperty
            } else {
              handleStandardProperty(
                nullable = field.annotations.map(_.tree.tpe).contains(weakTypeOf[nullable]))
            }
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
          new _root_.gremlin.scala.Marshallable[$tpe] {
            import _root_.scala.collection.JavaConverters._
            def fromCC(cc: $tpe) =
              this.FromCC(
                $idParam,
                $label,
                _root_.scala.collection.immutable.List[_root_.scala.collection.immutable.List[(_root_.scala.Predef.String,_root_.scala.Any)]](..$fromCCParams)
                  .flatten
                  .filter { kv =>
                    _root_.scala.Option(kv._2).isDefined
                  }
              )
            def toCC(element: _root_.gremlin.scala.Element): $tpe = $companion(..$toCCParams)
          }
      """
    }
    // if (tpe.toString.contains("CCWithNullable")) println(ret)
    ret
  }

}
