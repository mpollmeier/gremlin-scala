package gremlin.scala

import scala.annotation.{compileTimeOnly, StaticAnnotation}
import scala.annotation.meta._
import scala.reflect.runtime.universe.runtimeMirror
import scala.reflect.runtime.universe._
import scala.tools.reflect.ToolBox

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

object Annotations {

  private val mirror = runtimeMirror(getClass.getClassLoader)
  private val toolbox = mirror.mkToolBox()

  /**
    * Extract a label annotation, and return an instance of that label
    * @tparam T Target class
    * @return
    */
  def labelOf[T: WeakTypeTag]: Option[label] = {
    for (anno <- classAnnotations[T].find(_.tree.tpe =:= typeOf[label])) {
      return Some(instantiate[label](anno))
    }
    None
  }

  /**
    * Extract all class-level annotations from the given class
    * @tparam T Target class
    * @return
    */
  private def classAnnotations[T: WeakTypeTag]: List[Annotation] = {
    weakTypeOf[T].typeSymbol.asClass.annotations
  }

  /**
    * Given an annotation symbol, evaluate the annotation to instantiate and return an instance of it. The annotations
    * retrieved with the reflection API are recorded compile-time AST structures as opposed to the materialized
    * instances that Java's reflection API returns under runtime retention.
    *
    * @param annotation Annotation declaration
    * @tparam T Expected annotation type
    * @return
    */
  private def instantiate[T: TypeTag](annotation: Annotation): T = {
    toolbox.eval(toolbox.untypecheck(annotation.tree)).asInstanceOf[T]
  }
}
