package gremlin.scala

import scala.annotation.{compileTimeOnly, StaticAnnotation}
import scala.annotation.meta._

/** the underlying graph element, typically a vertex or edge */
@getter @beanGetter
class underlying extends StaticAnnotation

/** the id of the underlying graph element */
@getter @beanGetter
class id extends StaticAnnotation

/** annotate a non-option member with `@nullable` to allow `null` values.
  * by default this would throw an error on deserialization
  * a.k.a. license to shoot yourself in the foot */
@getter @beanGetter
class nullable extends StaticAnnotation

class label(val label: String = "") extends StaticAnnotation
