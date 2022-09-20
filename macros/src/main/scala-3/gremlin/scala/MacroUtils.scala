package gremlin.scala

object MacroUtils {
  import scala.quoted.{Type, Expr, Quotes}

  def tpeNmeMacro[A: Type](using Quotes) = {
    val name = Type.show[A]
    Expr(name)
  }

  inline def typeName[A] = ${tpeNmeMacro[A]}
}

