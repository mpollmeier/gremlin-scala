package com.tinkerpop.gremlin.scala

import org.scalatest.matchers.ShouldMatchers

class PathSpec extends TestBase {

  it("works") {
    import scala.collection.JavaConversions._
    import com.tinkerpop.gremlin.process._
    import com.tinkerpop.gremlin.process.steps.map._
    import com.tinkerpop.gremlin.process.steps.util.optimizers.HolderOptimizer

    //works now
    //v(1).out.traversal.path().toList.toList foreach println

    //this works
    //class MyPathStep[S](traversal: Traversal[_,_]) extends PathStep[S](traversal)

    //doesnt work: SimpleHolder?? anything specific about PathStep?
    class MyPathStep[S](traversal: Traversal[_,_]) extends MapStep[S, Path](traversal) {
      this.setFunction { holder: Holder[S] => holder.getPath }
    }

    // if pathStep is part of the traversal, it works
    //val t = v(1).out.traversal.path()
    val t = v(1).out.traversal
    t.addStep(new MyPathStep[String](t))
    //HolderOptimizer.doPathTracking(t)
    //t.toList.toList foreach println


    //still doesnt work - how can i get the holderoptimizer to be part of the graph?
    //val t0:Traversal[_,_] = graph.v(1).out()
    //val t1: Traversal[_,_] = t0.path()
    //println(HolderOptimizer.trackPaths(t0))
    //println(HolderOptimizer.trackPaths(t1))
    //HolderOptimizer.doPathTracking(t1)
    //new HolderOptimizer().optimize(t1)
    //println(HolderOptimizer.trackPaths(t1))
    //t1.toList.toList foreach println
  }

}
