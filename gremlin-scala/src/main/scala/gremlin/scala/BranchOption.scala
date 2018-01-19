package gremlin.scala

import org.apache.tinkerpop.gremlin.process.traversal.step.TraversalOptionParent.Pick

/** Define the traversal to run if the given predicate is true - used in branch step
  * 
  * you might think that traversal should be `GremlinScala[End, _] => GremlinScala[Boolean, _]`,
  * but that's not how tp3 works: e.g. `.value(Age).is(30)` returns `30`, not `true`
  */
trait BranchOption[End, NewEnd] {
  def traversal: GremlinScala[End] => GremlinScala[NewEnd]
  def pickToken: Any
}

case class BranchCase[BranchOn, End, NewEnd](pickToken: BranchOn, traversal: GremlinScala[End] => GremlinScala[NewEnd]) extends BranchOption[End, NewEnd]

case class BranchMatchAll[End, NewEnd](traversal: GremlinScala[End] => GremlinScala[NewEnd]) extends BranchOption[End, NewEnd] {
  override def pickToken = Pick.any
}

/* if nothing else matched in branch/choose step */
case class BranchOtherwise[End, NewEnd](traversal: GremlinScala[End] => GremlinScala[NewEnd]) extends BranchOption[End, NewEnd] {
  override def pickToken = Pick.none
}

