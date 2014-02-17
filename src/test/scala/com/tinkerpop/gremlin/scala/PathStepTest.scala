package com.tinkerpop.gremlin.scala

import com.tinkerpop.gremlin.scala._
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec
import com.tinkerpop.gremlin.structure._
import shapeless._
import shapeless.test.illTyped
import GremlinScala._

class PathStepTest extends GremlinSpec {

  //it("works") {
    //import scala.collection.JavaConversions._
    //import com.tinkerpop.gremlin.process._
    //import com.tinkerpop.gremlin.process.steps.util.optimizers.HolderOptimizer

    //doesnt work: simpleholder
    //v(1).out.traversal.path().toList.toList foreach println

    //still doesnt work - how can i get the holderoptimizer to be part of the graph?
    //val t0:Traversal[_,_] = graph.v(1).out()
    //val t1: Traversal[_,_] = t0.path()
    //println(HolderOptimizer.trackPaths(t0))
    //println(HolderOptimizer.trackPaths(t1))
    //HolderOptimizer.doPathTracking(t1)
    //new HolderOptimizer().optimize(t1)
    //println(HolderOptimizer.trackPaths(t1))
    //t1.toList.toList foreach println
  //}

}
