package com.tinkerpop.gremlin.scala

import org.scalatest.matchers.ShouldMatchers
import shapeless._

class PathSpec extends TestBase {

  // it("keeps the types in the traversal") {
  //   val path: List[Vertex :: Vertex :: String :: HNil] = 
  //     v(1).out.value[String]("name").path.toList
  //
  //   path should be(List(
  //     v(1).vertex :: v(3).vertex :: "lop" :: HNil,
  //     v(1).vertex :: v(2).vertex :: "vadas" :: HNil,
  //     v(1).vertex :: v(4).vertex :: "josh" :: HNil
  //   ))
  // }

  //TODO: also test with steps that don't add to the path:
  //  order, shuffle, has, filter, dedup, except, as, aggregate
  //TODO: back, jump etc.
}
