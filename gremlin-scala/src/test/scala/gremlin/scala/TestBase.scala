package gremlin.scala

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

trait TestGraph {
  implicit val graph = TinkerFactory.createClassic.asScala()
  def v(i: Int) = graph.V(i: Integer).head()
  def e(i: Int) = graph.E(i: Integer).head()

  def print(gs: GremlinScala[_]) = println(gs.toList())
}

object TestGraph {
  val Name = Key[String]("name")
  val Age = Key[Int]("age")
  val Created = Key[Int]("created")
  val Location = Key[String]("location")
  val Weight = Key[Double]("weight")
  val DoesNotExist = Key[Any]("doesnt_exist")

  @label("person")
  case class Person(name: String, age: Int)

  @label("software")
  case class Software(name: String, lang: String)
}

trait TestBase extends AnyFunSpec with Matchers with TestGraph {
  implicit class Properties[A](set: Iterable[Property[A]]) {
    def unroll(): Iterable[A] = set.map(_.value)
  }
}
