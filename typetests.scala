
import gremlin.scala.*
import gremlin.scala.given
import gremlin.scala.dsl.*
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory

import gremlin.scala.MacroUtils.compileTimePrintTypeName

implicit val graph: Graph = TinkerFactory.createModern

@label("person") case class Person(
  @id id: Option[Integer],
  name: String,
  age: Integer
) extends DomainRoot

@label("software") case class Software(
  name: String,
  lang: String
) extends DomainRoot

object PersonSteps {
  def apply(graph: Graph) = new PersonSteps[EmptyTuple](graph.V().hasLabel[Person]())
}

class PersonSteps[Labels <: Tuple]
(raw: GremlinScala[Vertex])
  extends NodeSteps[Person, Labels](raw) {

  def created = new SoftwareSteps[Labels](raw.out("created"))

  def name =
    new Steps[String, String, Labels](raw.map(_.value[String]("name")))

  def hasName(name: String) =
    new PersonSteps[Labels](raw.has(Key("name") -> name))
}

class SoftwareSteps[Labels <: Tuple]
(override val raw: GremlinScala[Vertex])
  extends NodeSteps[Software, Labels](raw) {
  def createdBy = new PersonSteps[Labels](raw.in("created"))

  def isRipple = new SoftwareSteps[Labels](raw.has(Key("name") -> "ripple"))
}

given[Labels <: Tuple]
: Conversion[Steps[Person, Vertex, Labels], PersonSteps[Labels]] with
  def apply(steps: Steps[Person, Vertex, Labels]) = new PersonSteps[Labels](steps.raw)

given (using graph: Graph): Conversion[Person, PersonSteps[EmptyTuple]] with
  def apply(person: Person) = new PersonSteps[EmptyTuple](graph.asScala().V(person.id.get))

given personStepsConstructor
  [Labels <: Tuple]
  : Constructor.Aux[Person, Labels, Vertex, PersonSteps[Labels]] =
    Constructor.forDomainNode[Person, Labels, PersonSteps[Labels]](new PersonSteps[Labels](_))

given softwareStepsConstructor
  [Labels <: Tuple]
  : Constructor.Aux[Software, Labels, Vertex, SoftwareSteps[Labels]] =
    Constructor.forDomainNode[Software, Labels, SoftwareSteps[Labels]](new SoftwareSteps[Labels](_))

val ps = PersonSteps(graph)

val softwareConv = summon[Converter[Software]]
val _ = compileTimePrintTypeName[softwareConv.GraphType]("softwareConv.GraphType")

val softwareConstr = summon[Constructor[Software, EmptyTuple]]
val _ = compileTimePrintTypeName[softwareConstr.GraphType]("softwareConstr.GraphType")
val _ = compileTimePrintTypeName[softwareConstr.StepsType]("softwareConstr.StepsType")

val ssConv = summon[Converter[(Software, String)]]
val _ = compileTimePrintTypeName[ssConv.GraphType]("ssConv.GraphType")

val ssConstr = summon[Constructor[(Software, String), EmptyTuple]]
val _ = compileTimePrintTypeName[ssConv.GraphType]("ssConv.GraphType")
val _ = compileTimePrintTypeName[ssConv.StepsType]("ssConv.StepsType")