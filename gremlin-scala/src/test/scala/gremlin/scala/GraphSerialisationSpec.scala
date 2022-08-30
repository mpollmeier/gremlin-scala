package gremlin.scala

import java.io.FileOutputStream
import org.apache.tinkerpop.gremlin.structure.io.graphml.GraphMLIo
import org.apache.tinkerpop.gremlin.structure.io.graphson.{GraphSONIo, GraphSONVersion}
import org.apache.tinkerpop.gremlin.structure.io.gryo.GryoIo
import org.apache.tinkerpop.gremlin.tinkergraph.structure.{TinkerFactory, TinkerGraph}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class GraphSerialisationSpec extends AnyWordSpec with Matchers {

  "serialising from/to" should {

    "support graphML" in {
      val file = "target/tinkerpop-modern.graphml"
      graph.io(GraphMLIo.build()).writeGraph(file)

      val newGraph = TinkerGraph.open
      newGraph.io(GraphMLIo.build()).readGraph(file)
      newGraph.V().count().head() shouldBe 6
      newGraph.E().count().head() shouldBe 6
    }

    "support graphson" in {
      val file = "target/tinkerpop-modern.graphson.json"
      graph.io(GraphSONIo.build()).writeGraph(file)

      val newGraph = TinkerGraph.open
      newGraph.io(GraphSONIo.build()).readGraph(file)
      newGraph.V().count().head() shouldBe 6
      newGraph.E().count().head() shouldBe 6
    }

    "support graphson v2" in {
      val file = "target/tinkerpop-modern.graphson2.json"
      val mapper = graph
        .io(GraphSONIo.build())
        .mapper
        .normalize(true)
        .version(GraphSONVersion.V2_0)
        .create
      graph
        .io(GraphSONIo.build())
        .writer
        .mapper(mapper)
        .create
        .writeGraph(new FileOutputStream(file), graph)

      val newGraph = TinkerGraph.open
      newGraph.io(GraphSONIo.build()).readGraph(file)
      newGraph.V().count().head() shouldBe 6
      newGraph.E().count().head() shouldBe 6
    }

    "support gryo/kryo" in {
      val file = "target/tinkerpop-modern.gryo"
      graph.io(GryoIo.build()).writeGraph(file)

      val newGraph = TinkerGraph.open
      newGraph.io(GryoIo.build()).readGraph(file)
      newGraph.V().count().head() shouldBe 6
      newGraph.E().count().head() shouldBe 6
    }
  }

  def graph: TinkerGraph = TinkerFactory.createModern
}
