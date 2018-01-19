package gremlin.scala

import java.io.FileOutputStream
import org.apache.tinkerpop.gremlin.structure.io.IoCore
import org.apache.tinkerpop.gremlin.structure.io.graphson.GraphSONVersion
import org.apache.tinkerpop.gremlin.tinkergraph.structure.{
  TinkerFactory,
  TinkerGraph
}
import org.scalatest.{Matchers, WordSpec}

class GraphSerialisationSpec extends WordSpec with Matchers {

  "serialising from/to" should {

    "support graphML" in {
      val file = "target/tinkerpop-modern.graphml"
      graph.io(IoCore.graphml).writeGraph(file)

      val newGraph = TinkerGraph.open
      newGraph.io(IoCore.graphml).readGraph(file)
      newGraph.V.count.head shouldBe 6
      newGraph.E.count.head shouldBe 6
    }

    "support graphson" in {
      val file = "target/tinkerpop-modern.graphson.json"
      graph.io(IoCore.graphson).writeGraph(file)

      val newGraph = TinkerGraph.open
      newGraph.io(IoCore.graphson).readGraph(file)
      newGraph.V.count.head shouldBe 6
      newGraph.E.count.head shouldBe 6
    }

    "support graphson v2" in {
      val file = "target/tinkerpop-modern.graphson2.json"
      val mapper = graph
        .io(IoCore.graphson)
        .mapper
        .normalize(true)
        .version(GraphSONVersion.V2_0)
        .create
      graph
        .io(IoCore.graphson)
        .writer
        .mapper(mapper)
        .create
        .writeGraph(new FileOutputStream(file), graph)

      val newGraph = TinkerGraph.open
      newGraph.io(IoCore.graphson).readGraph(file)
      newGraph.V.count.head shouldBe 6
      newGraph.E.count.head shouldBe 6
    }

    "support gryo/kryo" in {
      val file = "target/tinkerpop-modern.gryo"
      graph.io(IoCore.gryo).writeGraph(file)

      val newGraph = TinkerGraph.open
      newGraph.io(IoCore.gryo).readGraph(file)
      newGraph.V.count.head shouldBe 6
      newGraph.E.count.head shouldBe 6
    }
  }

  def graph = TinkerFactory.createModern
}
