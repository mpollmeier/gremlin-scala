package gremlin.scala

import org.apache.tinkerpop.gremlin.process.traversal.P

/** Define the traversal to run if the given predicate is true - used in branch step
  * 
  * you might think that optionTraversal should be `GremlinScala[End, _] => GremlinScala[Boolean, _]`,
  * but that's not how tp3 works: e.g. `.value(Age).is(30)` returns `30`, not `true`
  **/
trait BranchOption[End, NewEnd] {
 def optionTraversal: GremlinScala[End, _] => GremlinScala[NewEnd, _]
}

case class BranchCase[BranchOn, End, NewEnd](pickToken: BranchOn, optionTraversal: GremlinScala[End, _] => GremlinScala[NewEnd, _]) extends BranchOption[End, NewEnd]
case class BranchOtherwise[End, NewEnd](optionTraversal: GremlinScala[End, _] => GremlinScala[NewEnd, _]) extends BranchOption[End, NewEnd]
