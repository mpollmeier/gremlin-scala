package gremlin.scala

final case class FieldAnnotation(
  className: String,
  params: List[String]
)

final case class FieldInfo(
  name: String,
  typeName: String,
  annotation: Option[FieldAnnotation]
) {
  private def annotationStr = annotation match {
    case None => ""
    case Some(FieldAnnotation(name, params)) => s" @$name(${params.mkString(", ")})"
  }
  override def toString(): String = s"$name: $typeName$annotationStr"
}
