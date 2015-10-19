package gremlin.scala

import org.apache.tinkerpop.gremlin.structure._
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory
import org.scalatest.Matchers
import org.scalatest.FunSpec

trait TestGraph {
  val graph = TinkerFactory.createClassic().asScala
  def v(i: Int) = graph.v(i: Integer).get
  def e(i: Int) = graph.e(i: Integer).get


  def print(gs: GremlinScala[_, _]) = println(gs.toList)
}

object TestGraph {
  val Name = Key[String]("name")
  val Age = Key[Int]("age")
  val Created = Key[Int]("created")
  val Location = Key[String]("location")
  val Weight = Key[Double]("weight")
  val DoesNotExist = Key[Any]("doesnt_exist")
}

trait TestBase extends FunSpec with Matchers with TestGraph {
  implicit class Properties[A](set: Traversable[Property[A]]) {
    def unroll(): Traversable[A] = set map (_.value)
  }
}
