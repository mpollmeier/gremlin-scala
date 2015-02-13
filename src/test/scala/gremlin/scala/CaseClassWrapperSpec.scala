package gremlin.scala

import com.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers

class CaseClassWrapperSpec extends FunSpec with ShouldMatchers {
  import CaseClassWrapper._

  case class Example(s: String, i: Int, l: Long)
  val caseClass = Example("some string", Int.MaxValue, Long.MaxValue)

  it("saves a case class as a vertex") {
    val graph = TinkerGraph.open
    val gs = GremlinScala(graph)

    gs.saveCC(caseClass)

    val v: ScalaVertex = gs.V.toList.head
    v.valueMap should contain ("s" → caseClass.s)
    v.valueMap should contain ("i" → caseClass.i)
    v.valueMap should contain ("l" → caseClass.l)
  }

  it("converts a Vertex into a case class") {
    val graph = TinkerGraph.open
    val gs = GremlinScala(graph)

    gs.saveCC(caseClass)
    val v = gs.V.toList.head
    // v.toCC
  }

}
