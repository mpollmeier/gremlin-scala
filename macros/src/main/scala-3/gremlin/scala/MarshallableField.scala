package gremlin.scala


import scala.annotation.{implicitNotFound, targetName}
import scala.deriving.Mirror
import MacroUtils.*

@implicitNotFound(
  "Could not find an implicit MarshallableField[${T}]\n" +
  "You may be missing an `MarshallingFeatureSet` instance\n" +
  "Try one of the following:\n" +
  " - import gremlin.scala.MarshallingFeatureSet.PrimitivesOnly.given\n" +
  " - import gremlin.scala.MarshallingFeatureSet.Neptune.given\n" +
  " - import gremlin.scala.MarshallingFeatureSet.FullMonty.given"
)
trait MarshallableField[T]:
  def inspect(): String

object MarshallableField:

  import scala.quoted.*
  import scala.compiletime.erasedValue

  inline given MarshallableField[Int] = () => "Int"
  inline given MarshallableField[Long] = () => "Long"
  inline given MarshallableField[Float] = () => "Float"
  inline given MarshallableField[Double] = () => "Double"
  inline given MarshallableField[String] = () => "String"

  inline given [T, Coll[_] <: Iterable[_], FeatureSet <: MarshallingFeatureSet]
    (using featureSet: FeatureSet)
    (using nested: MarshallableField[T])
  : MarshallableField[Coll[T]] =
    inline erasedValue[Coll[T]] match
      case _: Seq[T] =>
        inline erasedValue[featureSet.SeqFlag] match
          case _: FeatureEnabled =>
            () => s"Seq[${nested.inspect()}]"
          case _ => compiletime.error("Seq inspection not enabled")
      case _: Set[T] =>
        inline erasedValue[featureSet.SetFlag] match
          case _: FeatureEnabled =>
            () => s"Set[${nested.inspect()}]"
          case _ => compiletime.error("Set inspection not enabled")
      case _ => compiletime.error("Unknown collection type")

  inline given [K, V, M[_,_] <: Map[_,_], FeatureSet <: MarshallingFeatureSet]
    (using featureSet: FeatureSet)
    (using keyInstance: MarshallableField[K], valInstance: MarshallableField[V])
  : MarshallableField[M[K,V]] =
    inline erasedValue[featureSet.MapFlag] match
      case _: FeatureEnabled =>
        () => s"Map[${keyInstance.inspect()}, ${valInstance.inspect()}]"
      case _ => compiletime.error("Seq inspection not enabled")

  inline given [P <: Product, FeatureSet <: MarshallingFeatureSet]
    (using featureSet: FeatureSet)
  : MarshallableField[P] =
    ${productMacro[P, FeatureSet]}

  private def productMacro[
    P: Type,
    FeatureSet <: MarshallingFeatureSet : Type,
  ](using Quotes): Expr[MarshallableField[P]] =
    import quotes.reflect._
    val className = Type.show[P]
    val featureSetType = TypeTree.of[FeatureSet].tpe

    // Every type member in `FeatureSet` adds another level of nesting to the resulting
    // `RefinedType` tree, so we drag 'em all out using recursion

    val flags =
      def loop(acc: Map[String, Boolean], tpe: TypeRepr): Map[String, Boolean] =
        tpe match {
          case Refinement(parent, flagName, TypeBounds(_,TypeRef(_,enabledType))) =>
            val isEnabled = enabledType == "FeatureEnabled"
            val nextAcc = acc + (flagName -> isEnabled)
            loop(nextAcc, parent)
          case _ => acc
        }
      loop(Map.empty, TypeTree.of[FeatureSet].tpe)

    val anyValEnabled: Boolean = flags("AnyValFlag")
    val caseClassEnabled: Boolean = flags("CaseClassFlag")

    def marshallableFieldTypeFor(tt: TypeRepr): AppliedType =
      val AppliedType(reference, _) = TypeRepr.of[MarshallableField[_]]: @unchecked
      AppliedType(reference, List(tt))

    def withNestedInstance[T](tt: TypeRepr)(fn: Expr[MarshallableField[_]] => Expr[T]): Expr[T] =
      Implicits.search(marshallableFieldTypeFor(tt)) match {
        case iss: ImplicitSearchSuccess =>
          val foundExpr = iss.tree.asExpr.asInstanceOf[Expr[MarshallableField[_]]]
          fn(foundExpr)
        case isf: ImplicitSearchFailure =>
          '{compiletime.error(${Expr(isf.explanation)})}
      }

    Type.of[P] match
      case '[AnyVal] =>
        if anyValEnabled then
          TypeTree.of[P].tpe.typeSymbol.caseFields.head.tree match {
            case ValDef(valueName, tpt, _) =>
              withNestedInstance(tpt.tpe) { nestedExpr =>
                val classNameExpr = Expr(className)
                val valueNameExpr = Expr(valueName)
                '{
                new MarshallableField[P] {
                  val nestedInspect: String = $nestedExpr.inspect()
                  val className: String = $classNameExpr
                  val valueName: String = $valueNameExpr
                  def inspect() = s"AnyVal: $className($valueName: $nestedInspect)"
                }
                }
              }
            case _ => '{compiletime.error("AnyVal doesn't appear to have a constructor param")}
          }
        else '{compiletime.error("AnyVal inspection not enabled")}
      case _ =>
        if caseClassEnabled then
          '{
            new MarshallableField[P] {
              def inspect() = ${Expr(s"Case Class: $className")}
            }
          }
        else '{compiletime.error("case class inspection not enabled")}

  object UnknownInstance:
    inline given [T]: MarshallableField[T] = ${unknownMacro[T]}

    private def unknownMacro[T](using Type[T])(using Quotes): Expr[MarshallableField[T]] =
      import quotes.reflect._
      val name = TypeTree.of[T].show
      '{
        new MarshallableField[T] {
          def inspect() = ${Expr("Unknown: " + name)}
        }
      }


