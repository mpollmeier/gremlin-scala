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

    tpe.decls.foreach {
      case sym: MethodSymbol if sym.toString == ("value i") ⇒
      // println(s"method: $sym")
      // val rt = sym.returnType
      // println("return type: " + rt)
      // println(rt <:< typeOf[AnyVal])
      // println("return type: " + rt.getClass)

      case sym if sym.toString == ("value i") ⇒
      // println(s"other: $sym")

      // case sym if sym.toString == ("value i") =>
      //   println(sym)
      //   println("class: " + sym.getClass)
      //   println("info: " + sym.info)
      //   println("is class: " + sym.isClass)
      //   // println(sym.asClass.isDerivedValueClass)

      case other ⇒
    }

    val (idParam, fromCCParams, toCCParams) = tpe.decls
      .foldLeft[(Tree, Seq[Tree], Seq[Tree])]((q"None", Seq.empty, Seq.empty)) {
        case ((_idParam, _fromCCParams, _toCCParams), field: MethodSymbol) if field.isCaseAccessor ⇒
          val name = field.name
          val decoded = name.decodedName.toString
          val returnType = field.returnType

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

          def property = {
            val valueGetters = returnType.declarations
              .sorted
              .filter(_.isMethod)
              .map(_.asMethod)
              .takeWhile(!_.isConstructor)
              .filter(_.paramss == Nil /* nullary */ )

            if (returnType <:< typeOf[AnyVal] && valueGetters.size > 0) {
              val valueName = valueGetters.head.name
              // println("companion members: " + returnType.companion.members)
              // println("companion declarations: " + returnType.companion.declarations)
              // TODO: this is the part that doesn't work yet
              val valueClassConstructor: MethodSymbol = {
                // println(valueGetters.head)
                // println(returnType)
                // TODO: make this work for all value class companions, not just this specific one - find by name and one constructor argument?
                  returnType.companion.declarations.tail.tail.head
                // returnType.companion.members.tail.head
                .asInstanceOf[MethodSymbol] // TODO: remove cast
                // println(
                //   returnType.companion.declarations.filter { s =>
                //     println("inside "+ s.name.decodedName)
                //     s.name.decodedName == "apply"
                //   }
                // )
              }
              // println(valueClassConstructor)
              // println(valueClassConstructor.paramLists)
              val valueClassCompanion = returnType.companion

              // TODO: cast valueMap(x) to the right type - get from the apply method
              // TODO: more safety here
              val wrappedType = valueClassConstructor.paramLists.head.head.typeSignature
              println(returnType)
              println(wrappedType)

              (_idParam,
                _fromCCParams :+ q"$decoded -> cc.$name.$valueName",

                //ClassCastException: Int is not MyValueClass
                _toCCParams :+ q"valueMap($decoded).asInstanceOf[$returnType]")

               //works, but not generic
               // _toCCParams :+ q"gremlin.scala.MyValueClass(valueMap($decoded).asInstanceOf[$wrappedType]).asInstanceOf[$returnType]")

              //scala.MatchError: apply (of class scala.reflect.internal.Trees$Ident)
                // _toCCParams :+ q"$valueClassConstructor{valueMap($decoded).asInstanceOf[$wrappedType]}.asInstanceOf[$returnType]")

                // scala.reflect.internal.FatalError: Unexpected tree in genLoad
                // _toCCParams :+ q"$valueClassCompanion.apply(valueMap($decoded).asInstanceOf[$wrappedType]).asInstanceOf[$returnType]")
            } else {
              (_idParam,
                _fromCCParams :+ q"$decoded -> cc.$name",
                _toCCParams :+ q"valueMap($decoded).asInstanceOf[$returnType]")
            }
          }

          if (field.annotations map (_.tree.tpe) contains weakTypeOf[id]) {
            if (returnType.typeSymbol == weakTypeOf[Option[_]].typeSymbol)
              idAsOption
            else
              idAsAnyRef
          } else { // normal property member
            assert(!Hidden.isHidden(decoded), s"The parameter name $decoded can't be used in the persistable case class $tpe")
            if (returnType.typeSymbol == weakTypeOf[Option[_]].typeSymbol)
              optionProperty
            else
              property
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
