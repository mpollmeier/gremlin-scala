package gremlin.scala.dsl

import gremlin.scala._
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory
import org.scalatest.{WordSpec, Matchers}
import shapeless._

class DslSpec extends WordSpec with Matchers {
  import TestDomain._

  "finds all persons" in {
    val graph = TinkerFactory.createModern
    val personSteps = new PersonSteps(graph.V.hasLabel[Person])
    personSteps.toSet shouldBe Set(
      Person(Some(1), "marko", 29),
      Person(Some(2), "vadas", 27),
      Person(Some(4), "josh",  32),
      Person(Some(6), "peter", 35)
    )
  }

  "traverses from person to software" in {
    val graph = TinkerFactory.createModern
    val personSteps = new PersonSteps(graph.V(1))
    personSteps.created.toSet shouldBe Set(Software("lop", "java"))
  }

  "finds combination of person/software in for comprehension" in {
    implicit val graph = TinkerFactory.createModern
    val traversal = for {
      person   <- new PersonSteps(graph.V.hasLabel[Person])
      software <- person.created
    } yield (person.name, software)

    val tuples = traversal.toSet shouldBe Set(
      ("marko", Software("lop", "java")),
      ("josh", Software("lop", "java")),
      ("peter", Software("lop", "java")),
      ("josh", Software("ripple", "java"))
    )
  }
}

object TestDomain {
  @label("person") case class Person(@id id: Option[Integer], name: String, age: Integer) extends DomainRoot
  @label("software") case class Software(name: String, lang: String) extends DomainRoot

  class PersonSteps(override val raw: GremlinScala[Vertex, HNil]) extends NodeSteps[Person](raw) {
    def created() = new SoftwareSteps(raw.out("created"))
  }

  class SoftwareSteps(override val raw: GremlinScala[Vertex, HNil]) extends NodeSteps[Software](raw) {
    def createdBy() = new PersonSteps(raw.in("created"))
  }

  implicit val personStepsConstructor: Constructor.Aux[Person, Vertex, PersonSteps] =
    Constructor.forDomainNode(new PersonSteps(_))

  implicit val softwareStepsConstructor: Constructor.Aux[Software, Vertex, SoftwareSteps] =
    Constructor.forDomainNode(new SoftwareSteps(_))

  implicit def liftPerson(person: Person)(implicit graph: Graph): PersonSteps =
    new PersonSteps(graph.asScala.V(person.id.get))
}
