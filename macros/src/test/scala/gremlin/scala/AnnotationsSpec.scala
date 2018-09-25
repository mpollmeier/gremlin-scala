package gremlin.scala

import org.scalatest.{FunSpec, Matchers}

case class CCWithoutLabel(foo: String)

@label("com.example.CCWithLiteralLabel")
case class CCWithLiteralLabel(foo: String)

object VertexLabel { val CCWithRefLabel = "com.example.CCWithRefLabel" }
@label(VertexLabel.CCWithRefLabel)
case class CCWithRefLabel(foo: String)

class AnnotationsSpec extends FunSpec with Matchers {

  def toLabelString(label: Option[label]): String =
    label.map(_.label).getOrElse(fail("Unable to locate a label"))

  describe("labelOf") {
    it("returns None for unlabeled classes") {
      Annotations.labelOf[CCWithoutLabel] shouldBe None
    }

    it("returns the label for classes labeled with a literal string") {
      toLabelString(Annotations.labelOf[CCWithLiteralLabel]) shouldBe "com.example.CCWithLiteralLabel"
    }

    it("returns the label for classes labeled indirectly with a constant string") {
      toLabelString(Annotations.labelOf[CCWithRefLabel]) shouldBe VertexLabel.CCWithRefLabel
    }
  }
}
