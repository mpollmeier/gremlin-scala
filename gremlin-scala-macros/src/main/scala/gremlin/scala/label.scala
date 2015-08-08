package gremlin.scala

import scala.annotation.StaticAnnotation
import scala.annotation.meta.{beanGetter, beanSetter, getter, setter}

@getter @setter @beanGetter @beanSetter
class label extends StaticAnnotation
