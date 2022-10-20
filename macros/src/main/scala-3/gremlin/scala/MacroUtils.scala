package gremlin.scala

object MacroUtils {
  import scala.quoted._

  def tpeNmeMacro[A: Type](using Quotes) = {
    val name = Type.show[A]
    println(s"Type name is $name")
    Expr(name)
  }

  inline def typeName[A] = ${tpeNmeMacro[A]}

  def compileTimePrintTypeNameMacro[A: Type](descExpr: Expr[String])(using Quotes) = {
    import quotes.reflect.{TypeRepr, Printer}
    val name = TypeRepr.of[A].dealias.show(using Printer.TypeReprShortCode)
    val desc = descExpr.valueOrAbort
    println(s"Type name for $desc is $name")
    Expr(name)
  }

  inline def compileTimePrintTypeName[A](desc: String) =
    ${compileTimePrintTypeNameMacro[A]('desc)}
}

