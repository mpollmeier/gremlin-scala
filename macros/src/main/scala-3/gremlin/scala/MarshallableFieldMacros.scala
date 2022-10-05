package gremlin.scala

import scala.quoted.*
import scala.compiletime.erasedValue

class MarshallableFieldMacros[
  Q <: Quotes,
  P: Type,
  FeatureSet <: MarshallingFeatureSet : Type
](using val q: Q):
  import q.reflect.*

  private[this] val className = Type.show[P]

  // Every type member in `FeatureSet` adds another level of nesting to the resulting
  // `RefinedType` tree, so we drag 'em all out using recursion
  private[this] val flags =
    def loop(acc: Map[String, Boolean], tpe: TypeRepr): Map[String, Boolean] =
      tpe match {
        case Refinement(parent, flagName, TypeBounds(_, TypeRef(_, enabledType))) =>
          val isEnabled = enabledType == "FeatureEnabled"
          val nextAcc = acc + (flagName -> isEnabled)
          loop(nextAcc, parent)
        case _ => acc
      }

    loop(Map.empty, TypeTree.of[FeatureSet].tpe)

  private[this] val anyValEnabled: Boolean = flags("AnyValFlag")
  private[this] val caseClassEnabled: Boolean = flags("CaseClassFlag")

  private def nestedInstanceFor[R](tt: TypeRepr): Expr[MarshallableField[_]] =
    TypeRepr.of[MarshallableField[_]] match
      case AppliedType(reference, _) =>
        val sought = AppliedType(reference, List(tt))
        Implicits.search(sought) match
          case iss: ImplicitSearchSuccess =>
            iss.tree.asExprOf[MarshallableField[_]]
          case isf: ImplicitSearchFailure =>
            report.errorAndAbort(isf.explanation)
      case other => report.errorAndAbort(s"Unexpected: ${other.show}")

  def handleProduct: Expr[MarshallableField[P]] =
    Type.of[P] match
      case '[AnyVal] =>
        if anyValEnabled then handleAnyVal
        else report.errorAndAbort("AnyVal inspection not enabled")
      case _ =>
        if caseClassEnabled then
          report.errorAndAbort("Nested case class marshalling not implemented")
        else
          report.errorAndAbort("nested case class marshalling not enabled")

  private def handleAnyVal: Expr[MarshallableField[P]] =
    val classSym: Symbol = TypeTree.of[P].tpe.typeSymbol

    val getterSym: Symbol = classSym.caseFields.head
    val companionSym = classSym.companionModule

    getterSym.tree match
      case ValDef(valueName, tpt, _) =>
        val nestedTypeRepr = tpt.tpe
        val nestedType = nestedTypeRepr.asType
        val nestedMarshallableExpr = nestedInstanceFor(nestedTypeRepr)

        nestedType match
          case '[underlying] =>
            val companionIdent = Ident(companionSym.termRef)
            val companionApply = Select.unique(companionIdent, "apply")
            val classNameExpr = Expr(className)
            val valueNameExpr = Expr(valueName)
            '{
              new MarshallableField[P] {
                type Encoded = underlying
                val nestedInstance = $nestedMarshallableExpr
                def encode(value: P): underlying = nestedInstance.encode(
                  ${
                    '{value}.asTerm.select(getterSym).asExpr
                  }.asInstanceOf[nestedInstance.Repr]
                ).asInstanceOf[underlying]
                def decode(value: underlying): P =
                  ${
                    val arg = '{value}.asTerm
                    Apply(companionApply, arg :: Nil).asExprOf[P]
                  }
              }
            }

      case _ => report.errorAndAbort("AnyVal doesn't appear to have a constructor param")


