package gremlin.scala

import scala.annotation.{compileTimeOnly, StaticAnnotation}
import scala.annotation.meta._

class label(val label: String = "") extends StaticAnnotation
