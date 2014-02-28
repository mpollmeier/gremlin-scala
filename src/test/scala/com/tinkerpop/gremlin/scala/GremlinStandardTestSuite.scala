package com.tinkerpop.gremlin.scala

import com.tinkerpop.gremlin.AbstractGremlinSuite
import com.tinkerpop.gremlin.AbstractGraphProvider
import com.tinkerpop.gremlin.process.ProcessStandardSuite
import com.tinkerpop.tinkergraph.TinkerGraph
import org.junit.runner.RunWith
import org.junit.runners.model.RunnerBuilder
import java.util.{Map => JMap}
import java.io.File
import org.apache.commons.configuration.Configuration
import scala.collection.JavaConversions._
import com.tinkerpop.gremlin.process.steps.filter.DedupTest
import com.tinkerpop.gremlin.process._

object Tests {
  // actual tests are inside an object so that they are not executed twice
  class ScalaDedupTest extends DedupTest with StandardTest {
    override def get_g_V_both_dedup_name(): Traversal[Vertex, String] = 
      ScalaGraph(g).V.both.dedup.value[String]("name")

    override def get_g_V_both_dedupXlangX_name(): Traversal[Vertex, String] = 
      ScalaGraph(g).V.both
        .dedup(_.getProperty[String]("lang").orElse(null))
        .value[String]("name")
  }
}

class ScalaProcessStandardSuite(clazz: Class[_], builder: RunnerBuilder) extends AbstractGremlinSuite(clazz, builder, Array(
    classOf[Tests.ScalaDedupTest]))

trait StandardTest {
  implicit def toTraversal[S,E](gs: GremlinScala[_,E]): Traversal[S,E] = gs.traversal.asInstanceOf[Traversal[S,E]]
}

@RunWith(classOf[ScalaProcessStandardSuite]) 
@AbstractGremlinSuite.GraphProviderClass(classOf[ScalaTinkerGraphProcessStandardTest])
class ScalaTinkerGraphProcessStandardTest extends AbstractGraphProvider {
  override def getBaseConfiguration(graphName: String): JMap[String, AnyRef] =
    Map("gremlin.graph" -> classOf[TinkerGraph].getName)

  override def clear(graph: Graph, configuration: Configuration): Unit = {
    graph.close()
    if (configuration.containsKey("gremlin.tg.directory"))
      new File(configuration.getString("gremlin.tg.directory")).delete()
  }
}
