package gremlin.scala

import org.apache.tinkerpop.gremlin.process.traversal.{P => JavaP}
import java.util.{Collection => JCollection}
import scala.jdk.CollectionConverters._

/** the scala version of tinkerpop's P, mostly to avoid unnecessarily complicated constructs
  * like P.within(vertices.asJava: JCollection[Vertex]) */
object P {
  // same as `eq`, in case you're having problems with overloaded definition of `eq`
  def is[A](value: A) = JavaP.eq(value)
  def eq[A](value: A) = JavaP.eq(value)
  def neq[A](value: A) = JavaP.neq(value)
  def gt[A](value: A) = JavaP.gt(value)
  def gte[A](value: A) = JavaP.gte(value)
  def lt[A](value: A) = JavaP.lt(value)
  def lte[A](value: A) = JavaP.lte(value)
  def between[A](a1: A, a2: A) = JavaP.between(a1, a2)
  def inside[A](a1: A, a2: A) = JavaP.inside(a1, a2)
  def outside[A](a1: A, a2: A) = JavaP.outside(a1, a2)
  def within[A](iterable: Iterable[A]) =
    JavaP.within(iterable.asJavaCollection: JCollection[A])
  def without[A](iterable: Iterable[A]) =
    JavaP.without(iterable.asJavaCollection: JCollection[A])

  def fromPredicate[A](predicate: (A, A) => Boolean, value: A) =
    new JavaP(toJavaBiPredicate(predicate), value)
}
