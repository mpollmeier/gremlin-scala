package gremlin.scala

import org.apache.tinkerpop.gremlin.structure._
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory
import org.scalatest.Matchers
import org.scalatest.FunSpec
import schema.Key

trait TestGraph {
  val graph = TinkerFactory.createClassic().asScala
  def v(i: Int) = graph.v(i: Integer).get
  def e(i: Int) = graph.e(i: Integer).get


  def print(gs: GremlinScala[_, _]) = println(gs.toList)
}

trait TestBase extends FunSpec with Matchers with TestGraph {
  val Name = Key[String]("name")
  val Weight = Key[Float]("weight")
  val DoesNotExist = Key[Any]("doesnt_exist")

  implicit class Properties[A](set: Traversable[Property[A]]) {
    def unroll(): Traversable[A] = set map (_.value)
  }
}
