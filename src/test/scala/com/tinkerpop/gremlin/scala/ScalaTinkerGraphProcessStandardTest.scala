package com.tinkerpop.gremlin.scala

import org.scalatest.matchers.ShouldMatchers
import com.tinkerpop.gremlin.process.steps.filter.DedupTest
import com.tinkerpop.gremlin.process._

class ScalaDedupTest extends DedupTest {
    //TODO: provide implicit conversion to traversal incl. cast?
  override def get_g_V_both_dedup_name(): Traversal[Vertex, String] = 
    ScalaGraph(g).V.both.dedup.value[String]("name").traversal.asInstanceOf[Traversal[Vertex, String]]

    //TODO properly implement
  override def get_g_V_both_dedupXlangX_name(): Traversal[Vertex, String] = 
    ScalaGraph(g).V.both.dedup.value[String]("name").traversal.asInstanceOf[Traversal[Vertex, String]]
}

//TODO move to other file
import com.tinkerpop.gremlin.AbstractGremlinSuite
import org.junit.runners.model.RunnerBuilder
class ScalaProcessStandardSuite(clazz: Class[_], builder: RunnerBuilder) extends AbstractGremlinSuite(clazz, builder, Array(
    classOf[ScalaDedupTest]))

//TODO move to other file
import com.tinkerpop.gremlin.AbstractGraphProvider
import com.tinkerpop.gremlin.process.ProcessStandardSuite
import com.tinkerpop.tinkergraph.TinkerGraph
import org.junit.runner.RunWith
import java.util.{Map => JMap}
import java.io.File
import org.apache.commons.configuration.Configuration
import scala.collection.JavaConversions._

@RunWith(classOf[ScalaProcessStandardSuite]) 
@AbstractGremlinSuite.GraphProviderClass(classOf[ScalaTinkerGraphProcessStandardTest])
class ScalaTinkerGraphProcessStandardTest extends AbstractGraphProvider {

  override def getBaseConfiguration(graphName: String): JMap[String, AnyRef] = {
    Map("gremlin.graph" -> classOf[TinkerGraph].getName)
  }

  override def clear(graph: Graph, configuration: Configuration): Unit = {
    graph.close()

    if (configuration.containsKey("gremlin.tg.directory"))
      new File(configuration.getString("gremlin.tg.directory")).delete()
  }
}


//class FilterStandardSpec extends TestBase {

  //it("dedups") {
  //}
//}
