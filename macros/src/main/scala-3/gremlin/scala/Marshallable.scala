package gremlin.scala

import org.apache.tinkerpop.gremlin.structure.Graph.Hidden
import org.apache.tinkerpop.gremlin.structure.Element


trait Marshallable[CC] {
  type Id = AnyRef
  type Label = String
  case class FromCC(id: Option[Id], label: Label, properties: List[(String, Any)])

  def fromCC(cc: CC): FromCC
  def toCC(element: Element): CC
}

object Marshallable:
  import deriving.*
  import scala.compiletime.*
  import scala.quoted.*

  inline given derived[T](using m: Mirror.Of[T]): Marshallable[T] =
    inline m match
      case m: Mirror.ProductOf[T] =>
        derivedProduct[T, m.type](m)
      case m: Mirror.SumOf[T] =>
        derivedSum[T, m.type](m)
      case _ => compiletime.error("Cannot derive Marshallable for non-struct classes")

  inline def mkFieldInfo[ProdType, MemberLbl, MemberType]: FieldInfo =
    ${mkFieldInfoImplMacro[ProdType, MemberLbl, MemberType]}

  inline def enquote(s: String) = s"\"s\""
  inline def dequote(s: String) = s.stripPrefix("\"").stripSuffix("\"")

  private def mkFieldInfoImplMacro[
    ProdType: Type,
    MemberLbl: Type,
    MemberType: Type
  ](using Quotes): Expr[FieldInfo] =
    import quotes.reflect._

    val fieldName = dequote(Type.show[MemberLbl])
    val fieldType = Type.show[MemberType]

    val caseFieldTypes = determineCaseFieldTypes[ProdType]

    val annotationInSymbol = caseFieldTypes collectFirst {
      case (name, AnnotatedType(_, Apply(Select(fnType, _), params))) if name == fieldName =>
        s"${fnType.tpe.show}" -> params.map(_.show)
    }

    val fieldNameExpr = Expr(fieldName)
    val fieldTypeExpr = Expr(fieldType)
//    val fieldTypeExpr = Expr.summon[FieldInspectable[MemberType]] match {
//      case Some(fi) => '{$fi.summarise()}
//      case _ => '{"???"}
//    }

    annotationInSymbol match {
      case None =>
        '{
          FieldInfo(
            name = $fieldNameExpr,
            typeName = $fieldTypeExpr,
            annotation = None
          )
        }
      case Some(name, params) =>
        '{
          FieldInfo(
            name = $fieldNameExpr,
            typeName = $fieldTypeExpr,
            annotation = Some(
              FieldAnnotation(
                className = $
                {Expr(name)},
                params = $
                {Expr(params)}
              )
            )
          )
        }
    }

  private def determineCaseFieldTypes[P](using Type[P])(using q: Quotes): Seq[(String, q.reflect.TypeRepr)] =
    import quotes.reflect._
    val ts = TypeTree.of[P].tpe.typeSymbol
    for
      fieldSym <- ts.caseFields
      ValDef(name, tpe, _) = fieldSym.tree: @unchecked
    yield
      name -> tpe.tpe

  inline private def deriveFields[ProductType, FieldTypes <: Tuple, Labels <: Tuple]: List[FieldInfo] =
    inline erasedValue[FieldTypes] match
      case _: (fieldType *: fieldTypes) =>
        inline erasedValue[Labels] match
          case _: (fieldLabel *: fieldLabels) =>
            val fieldInfo = mkFieldInfo[ProductType, fieldLabel, fieldType]
            fieldInfo :: deriveFields[ProductType, fieldTypes, fieldLabels]
          case _: EmptyTuple => compiletime.error("LOGIC ERROR: Ran out of field labels for field types")
      case _: EmptyTuple => Nil


  inline def extractElem[ProductType, FieldTypes <: Tuple, Labels <: Tuple](element: Element): Tuple =
    inline erasedValue[FieldTypes] match
      case _: (fieldType *: fieldTypes) =>
        inline erasedValue[Labels] match
          case _: (fieldLabel *: fieldLabels) =>
            val value = element.value[fieldType](mkFieldInfo[ProductType, fieldType, fieldLabel].name)
            value *: extractElem[ProductType, fieldTypes, fieldLabels](element)
          case _: EmptyTuple => compiletime.error("LOGIC ERROR: Ran out of field labels for field types")
      case _: EmptyTuple => EmptyTuple

  inline def derivedProduct[P, M <: Mirror.ProductOf[P]](m: M): Marshallable[P] =
    new Marshallable[P] :
      val className = constValue[m.MirroredLabel]
      val fieldInfos = deriveFields[P, m.MirroredElemTypes, m.MirroredElemLabels]

      def fromCC(cc: P): FromCC = {
        val elemNames = cc.asInstanceOf[Product].productElementNames
        val elemValues = cc.asInstanceOf[Product].productIterator
        FromCC(
          id = None,
          label = className,
          properties = (elemNames zip elemValues).toList
        )
      }

      def toCC(element: Element): P =
        m.fromProduct(extractElem[P, m.MirroredElemTypes, m.MirroredElemLabels](element))

//      override def summarise(): String =
//        className + ": " + fieldInfos.mkString("[\n  ", ",\n  ", "\n]")
//
//      override def inspect(value: P): String =
//        className + ": " + fieldInfos.mkString("[\n  ", ",\n  ", "\n]")


  inline def derivedSum[T, M <: Mirror.SumOf[T]](m: M): Marshallable[T] =
    compiletime.error("Can't inspect sum types")



