package com.tinkerpop.gremlin.scala

import com.tinkerpop.blueprints._
import com.tinkerpop.gremlin.java.GremlinPipeline
import shapeless._

  //[>* add vertex; id defaults to null which will generate a random id<]
  //def addV(id: Any = null) = graph.addVertex(id)

  //[>* add edge; id defaults to null which will generate a random id <]
  //def addE(out: Vertex, in: Vertex, label: String, id: Any = null) = graph.addEdge(id, out.vertex, in.vertex, label)

/**Implicit conversions between [[com.tinkerpop.blueprints.Graph]] and [[com.tinkerpop.gremlin.scala.ScalaGraph]]. */
  def apply(graph: Graph) = wrap(graph)
  implicit def wrap(graph: Graph) = new ScalaGraph(graph)
  implicit def unwrap(wrapper: ScalaGraph) = wrapper.graph
