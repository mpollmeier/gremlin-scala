package gremlin.scala.dsl

import gremlin.scala._
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory
import org.scalatest.{WordSpec, Matchers}
import scala.collection.mutable
import shapeless._

class DslSpec extends WordSpec with Matchers {
  import TestDomain._

  "finds all persons" in {
    val personSteps = PersonSteps(TinkerFactory.createModern)
    personSteps.toSet shouldBe Set(
      Person(Some(1), "marko", 29),
      Person(Some(2), "vadas", 27),
      Person(Some(4), "josh",  32),
      Person(Some(6), "peter", 35)
    )
  }

  "label with `as` and typesafe `select` of domain types" in {
    implicit val graph = TinkerFactory.createModern

    val personAndSoftware: List[(Person, Software)] =
      PersonSteps(graph)
        .as("person")
        .created
        .as("software")
        .select2
        .toList
    personAndSoftware should have size 4

    val softwareByCreator: Map[String, Software] = personAndSoftware
      .map { case (person, software) => (person.name, software) }
      .toMap
    softwareByCreator("marko") shouldBe Software("lop", "java")
  }

  "finds combination of person/software in for comprehension" in {
    implicit val graph = TinkerFactory.createModern

    val traversal = for {
      person   <- PersonSteps(graph)
      software <- person.created
    } yield (person.name, software)

    val tuples = traversal.toSet shouldBe Set(
      ("marko", Software("lop", "java")),
      ("josh", Software("lop", "java")),
      ("peter", Software("lop", "java")),
      ("josh", Software("ripple", "java"))
    )
  }

  "filter with traversal on domain type" when {
    "domain type is a case class" in {
      val ripples = PersonSteps(TinkerFactory.createModern)
          .filter(_.created.isRipple)

      ripples.toList shouldBe List(
        Person(Some(4), "josh",  32)
      )
    }
  }

  "filterNot with traversal on domain type" in {
    val notRipple = PersonSteps(TinkerFactory.createModern)
        .filterNot(_.created.isRipple)

    notRipple.toList.size shouldBe 3
  }

  "filter on domain type" in {
    val markos: List[Person] =
      PersonSteps(TinkerFactory.createModern)
        .filterOnEnd(_.name == "marko")
        .toList

    markos.size shouldBe 1
  }

  "aggregate intermediary results into a collection" in {
    val allPersons = mutable.ArrayBuffer.empty[Person]
    val markos: List[Person] =
      PersonSteps(TinkerFactory.createModern)
        .aggregate(allPersons)
        .filterOnEnd(_.name == "marko")
        .toList

    markos.size shouldBe 1
    allPersons.size should be > 1
  }

  "deduplicates" in {
    val results: List[Person] =
      PersonSteps(TinkerFactory.createModern)
        .created.createdBy
        .dedup()
        .toList
    results.size shouldBe 3
  }

  "allows to use underlying gremlin-scala steps" in {
    val steps: PersonSteps[_, _] =
      PersonSteps(TinkerFactory.createModern)
        .onRaw(_.hasId(1: Integer))
    steps.toList.size shouldBe 1
  }

  "traverses from person to software" in {
    val personSteps =
      PersonSteps(TinkerFactory.createModern)
        .onRaw(_.hasId(1: Integer))

    personSteps.created.toSet shouldBe Set(Software("lop", "java"))
  }

  "supports collections in map/flatMap" when {
    implicit val graph = TinkerFactory.createModern
    def personSteps = PersonSteps(graph)

    "using List" in {
      val query = personSteps.map { person =>
        (person.name, person.created.toList)
      }

      val results: List[(String, List[Software])] = query.toList
      results.size shouldBe 4
    }

    "using Set" in {
      val query = personSteps.map { person =>
        (person.name, person.created.toSet)
      }

      val results: List[(String, Set[Software])] = query.toList
      results.size shouldBe 4
    }
  }
}

object TestDomain {
  @label("person") case class Person(@id id: Option[Integer], name: String, age: Integer) extends DomainRoot
  @label("software") case class Software(name: String, lang: String) extends DomainRoot

  object PersonSteps {
    def apply(graph: Graph) = new PersonSteps[HNil, HNil](graph.V.hasLabel[Person])
  }
  class PersonSteps[LabelsDomain <: HList, LabelsGraph <: HList](override val raw: GremlinScala[Vertex, LabelsGraph])
      extends NodeSteps[Person, LabelsDomain, LabelsGraph](raw) {

    def created = new SoftwareSteps[LabelsDomain, LabelsGraph](raw.out("created"))

    def name = new Steps[String, String, LabelsDomain, LabelsGraph](raw.map(_.value[String]("name")))

    def hasName(name: String) = new PersonSteps[LabelsDomain, LabelsGraph](raw.has(Key("name") -> name))
  }

  class SoftwareSteps[LabelsDomain <: HList, LabelsGraph <: HList](override val raw: GremlinScala[Vertex, LabelsGraph])
      extends NodeSteps[Software, LabelsDomain, LabelsGraph](raw) {

    def createdBy = new PersonSteps[LabelsDomain, LabelsGraph](raw.in("created"))

    def isRipple = new SoftwareSteps[LabelsDomain, LabelsGraph](raw.has(Key("name") -> "ripple"))
  }

  implicit def personStepsConstructor[LabelsDomain <: HList, LabelsGraph <: HList]
    : Constructor.Aux[Person, LabelsDomain, Vertex, LabelsGraph, PersonSteps[LabelsDomain, LabelsGraph]] =
    Constructor.forDomainNode[Person, LabelsDomain, LabelsGraph, PersonSteps[LabelsDomain, LabelsGraph]](new PersonSteps[LabelsDomain, LabelsGraph](_))

  implicit def softwareStepsConstructor[LabelsDomain <: HList, LabelsGraph <: HList]
    : Constructor.Aux[Software, LabelsDomain, Vertex, LabelsGraph, SoftwareSteps[LabelsDomain, LabelsGraph]] =
    Constructor.forDomainNode[Software, LabelsDomain, LabelsGraph, SoftwareSteps[LabelsDomain, LabelsGraph]](new SoftwareSteps[LabelsDomain, LabelsGraph](_))

  implicit def liftPerson(person: Person)(implicit graph: Graph): PersonSteps[HNil, HNil] =
    new PersonSteps[HNil, HNil](graph.asScala.V(person.id.get))
}
