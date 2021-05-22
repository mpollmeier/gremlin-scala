package gremlin.scala.dsl

import gremlin.scala._
import java.util.{Map => JMap}
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import scala.collection.mutable
import scala.collection.JavaConverters._
import shapeless._

class DslSpec extends AnyWordSpec with Matchers {
  import TestDomain._

  "finds all persons" in {
    val personSteps = PersonSteps(TinkerFactory.createModern)
    personSteps.toSet() shouldBe Set(
      Person(Some(1), "marko", 29),
      Person(Some(2), "vadas", 27),
      Person(Some(4), "josh", 32),
      Person(Some(6), "peter", 35)
    )
  }

  "label with `as` and typesafe `select` of domain types" should {

    "select all labelled steps by default" in {
      implicit val graph = TinkerFactory.createModern

      val personAndSoftware: List[(Person, Software)] =
        PersonSteps(graph)
          .as("person")
          .created
          .as("software")
          .select()
          .toList()
      (personAndSoftware should have).size(4)

      val softwareByCreator: Map[String, Software] = personAndSoftware.map {
        case (person, software) => (person.name, software)
      }.toMap
      softwareByCreator("marko") shouldBe Software("lop", "java")
    }

    "allow to select one labelled step only" in {
      implicit val graph = TinkerFactory.createModern
      val labelPerson = StepLabel[Person]("p")
      val labelSoftware = StepLabel[Software]("s")

      val personAndSoftware: Set[Software] =
        PersonSteps(graph)
          .as(labelPerson)
          .created
          .as(labelSoftware)
          .select(labelSoftware)
          .toSet()
      personAndSoftware shouldBe Set(Software("lop", "java"), Software("ripple", "java"))
    }

    "allow to select multiple labelled steps" in {
      implicit val graph = TinkerFactory.createModern
      val labelPerson = StepLabel[Person]("p")
      val labelSoftware = StepLabel[Software]("s")

      val personAndSoftware: List[(Software, Person)] =
        PersonSteps(graph)
          .as(labelPerson)
          .created
          .as(labelSoftware)
          .select((labelSoftware, labelPerson))
          .toList()
      (personAndSoftware should have).size(4)

      val softwareByCreator: Map[String, Software] = personAndSoftware.map {
        case (software, person) => (person.name, software)
      }.toMap
      softwareByCreator("marko") shouldBe Software("lop", "java")
    }
  }

  "finds combination of person/software in for comprehension" in {
    implicit val graph = TinkerFactory.createModern

    val traversal = for {
      person <- PersonSteps(graph)
      software <- person.created
    } yield (person.name, software)

    val tuples = traversal.toSet() shouldBe Set(
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

      ripples.toList() shouldBe List(
        Person(Some(4), "josh", 32)
      )
    }
  }

  "filterNot with traversal on domain type" in {
    val notRipple = PersonSteps(TinkerFactory.createModern)
      .filterNot(_.created.isRipple)

    notRipple.toList().size shouldBe 3
  }

  "filter on domain type" in {
    val markos: List[Person] =
      PersonSteps(TinkerFactory.createModern)
        .filterOnEnd(_.name == "marko")
        .toList()

    markos.size shouldBe 1
  }

  "aggregate intermediary results into a collection" in {
    val allPersons = mutable.ArrayBuffer.empty[Person]
    val markos: List[Person] =
      PersonSteps(TinkerFactory.createModern)
        .aggregate(allPersons)
        .filterOnEnd(_.name == "marko")
        .toList()

    markos.size shouldBe 1
    allPersons.size should be > 1
  }

  "allow side effects" in {
    var i = 0
    PersonSteps(TinkerFactory.createModern).sideEffect(_ => i = i + 1).iterate()
    i shouldBe 4
  }

  "deduplicates" in {
    val results: List[Person] =
      PersonSteps(TinkerFactory.createModern).created.createdBy.dedup().toList()
    results.size shouldBe 3
  }

  "allows to use underlying gremlin-scala steps" in {
    val steps: PersonSteps[_] =
      PersonSteps(TinkerFactory.createModern)
        .onRaw(_.hasId(1: Integer))
    steps.toList().size shouldBe 1
  }

  "traverses from person to software" in {
    val personSteps =
      PersonSteps(TinkerFactory.createModern)
        .onRaw(_.hasId(1: Integer))

    personSteps.created.toSet() shouldBe Set(Software("lop", "java"))
  }

  "supports collections in map/flatMap" when {
    implicit val graph = TinkerFactory.createModern
    def personSteps = PersonSteps(graph)

    "using List" in {
      val query = personSteps.map { person =>
        (person.name, person.created.toList())
      }

      val results: List[(String, List[Software])] = query.toList()
      results.size shouldBe 4
    }

    "using Set" in {
      val query = personSteps.map { person =>
        (person.name, person.created.toSet())
      }

      val results: List[(String, Set[Software])] = query.toList()
      results.size shouldBe 4
    }
  }

  "allows to be cloned" in {
    val graph = TinkerFactory.createModern
    def personSteps = PersonSteps(graph)

    val query = personSteps.hasName("marko")
    val queryCloned = query.clone()
    query.toList().size shouldBe 1
    queryCloned.toList().size shouldBe 1
  }
}

object TestDomain {
  @label("person") case class Person(@id id: Option[Integer], name: String, age: Integer)
      extends DomainRoot
  @label("software") case class Software(name: String, lang: String) extends DomainRoot

  object PersonSteps {
    def apply(graph: Graph) = new PersonSteps[HNil](graph.V().hasLabel[Person]())
  }
  class PersonSteps[Labels <: HList](override val raw: GremlinScala[Vertex])
      extends NodeSteps[Person, Labels](raw) {

    def created = new SoftwareSteps[Labels](raw.out("created"))

    def name =
      new Steps[String, String, Labels](raw.map(_.value[String]("name")))

    def hasName(name: String) =
      new PersonSteps[Labels](raw.has(Key("name") -> name))
  }

  class SoftwareSteps[Labels <: HList](override val raw: GremlinScala[Vertex])
      extends NodeSteps[Software, Labels](raw) {

    def createdBy = new PersonSteps[Labels](raw.in("created"))

    def isRipple = new SoftwareSteps[Labels](raw.has(Key("name") -> "ripple"))
  }

  implicit def toPersonSteps[Labels <: HList](
      steps: Steps[Person, Vertex, Labels]): PersonSteps[Labels] =
    new PersonSteps[Labels](steps.raw)

  implicit def personStepsConstructor[Labels <: HList]
    : Constructor.Aux[Person, Labels, Vertex, PersonSteps[Labels]] =
    Constructor.forDomainNode[Person, Labels, PersonSteps[Labels]](new PersonSteps[Labels](_))

  implicit def softwareStepsConstructor[Labels <: HList]
    : Constructor.Aux[Software, Labels, Vertex, SoftwareSteps[Labels]] =
    Constructor.forDomainNode[Software, Labels, SoftwareSteps[Labels]](new SoftwareSteps[Labels](_))

  implicit def liftPerson(person: Person)(implicit graph: Graph): PersonSteps[HNil] =
    new PersonSteps[HNil](graph.asScala().V(person.id.get))
}
