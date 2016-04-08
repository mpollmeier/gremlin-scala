package gremlin.scala
import io.github.netvl.picopickle.backends.collections.CollectionsPickler
import io.github.netvl.picopickle.{BackendComponent, TypesComponent}

import io.github.netvl.picopickle.backends.collections.CollectionsPickler
import io.github.netvl.picopickle.ValueClassReaderWritersComponent

trait WithLabel {
  def label(): String
}

trait IdPicklers { this: BackendComponent with TypesComponent ⇒
  implicit def idWriter[IdType](implicit w: Writer[IdType]): Writer[Id[IdType]] = Writer {
    case Id(value) ⇒
      println("XXXXXXXXXXXXXXX yay, writer getting invoked")
      // TODO: get rid of cast
      Map("__id" → w.write(value)).asInstanceOf[backend.BValue]
  }

  implicit def idReader[IdType](implicit r: Reader[IdType]): Reader[Id[IdType]] = Reader {
    case idValueMap: Map[_, _] ⇒
      println("YYYYYYYYYYYYYYY yay, reader getting invoked")
      // TODO: get rid of cast
      Id[IdType](idValueMap.asInstanceOf[Map[String, IdType]]("__id"))
  }
}

object GremlinPickler extends CollectionsPickler with ValueClassReaderWritersComponent with IdPicklers
