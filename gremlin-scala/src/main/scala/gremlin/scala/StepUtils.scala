package gremlin.scala

import org.apache.tinkerpop.gremlin.process.traversal.P

/** Define the traversal to run if the given predicate is true - used in branch step
  * 
  * you might think that onTrue should be `GremlinScala[End, _] => GremlinScala[Boolean, _]`,
  * but that's not how tp3 works: e.g. `.value(Age).is(30)` returns `30`, not `true`
  **/
case class BranchOption[BranchOn, End, NewEnd](pickToken: BranchOn, onTrue: GremlinScala[End, _] => GremlinScala[NewEnd, _])
