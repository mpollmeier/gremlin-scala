package com.tinkerpop.gremlin.scala

import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory
import com.tinkerpop.gremlin.scala.ScalaVertex._
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.tinkerpop.blueprints._
import com.tinkerpop.blueprints.impls.tg.TinkerGraph
import scala.collection.JavaConversions._
import scala.collection.mutable.Map
import com.tinkerpop.gremlin.java.GremlinPipeline
import com.tinkerpop.pipes.filter.FilterFunctionPipe
import java.lang.{ Iterable ⇒ JIterable }

@RunWith(classOf[JUnitRunner])
class SampleUsage2Test extends FunSpec with ShouldMatchers with TestGraph {

  it("plays with pipeline") {
    import com.tinkerpop.pipes.util._
    import com.tinkerpop.pipes._
    import com.tinkerpop.blueprints.{ Graph, Vertex, Edge }

    class GremlinScalaPipeline[S, E] extends Pipeline[S, E] {

      def addPP[T](pipe: Pipe[_, T]): GremlinScalaPipeline[S, T] = {
        addPipe(pipe)
        this.asInstanceOf[GremlinScalaPipeline[S, T]]
      }

      def manualStart[T](start: JIterable[_]): GremlinScalaPipeline[T, T] = {
        val pipe = addPP(new StartPipe[S](start))
        FluentUtility.setStarts(this, start)
        pipe.asInstanceOf[GremlinScalaPipeline[T, T]]
      }

      def V(graph: Graph): GremlinScalaPipeline[ScalaVertex, ScalaVertex] = {
        import scala.collection.convert.wrapAsJava
        val scalaVertices = graph.getVertices.iterator.map { v ⇒ ScalaVertex(v) }
        val jIterator: JIterable[ScalaVertex] =
          wrapAsJava.asJavaIterable(scalaVertices.toIterable)
        manualStart(jIterator)
      }

      def filter(f: E ⇒ Boolean): GremlinScalaPipeline[S, E] = addPP(new FilterFunctionPipe[E](f))

    }

    class ScalaGraph(val graph: Graph) {
      def V: GremlinScalaPipeline[ScalaVertex, ScalaVertex] =
        new GremlinScalaPipeline[ScalaGraph, ScalaVertex].V(graph)
    }

    val graph: ScalaGraph = new ScalaGraph(TinkerGraphFactory.createTinkerGraph)
    val pipeline: GremlinScalaPipeline[ScalaVertex, ScalaVertex] = graph.V

    println(pipeline.filter { _.name != null }.toList)
  }

}
