package gremlin.scala

import io.github.netvl.picopickle.backends.collections.CollectionsPickler
import io.github.netvl.picopickle.ValueClassReaderWritersComponent

object GremlinPickler extends CollectionsPickler with ValueClassReaderWritersComponent

trait WithLabel {
  def label(): String
}
