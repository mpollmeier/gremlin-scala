package com.tinkerpop.gremlin.scala
import org.scalatest.matchers.ShouldMatchers

class MapSpec extends TestBase {

  it("maps") {
    v(1).out.map { holder ⇒ s"mapped ${holder.get}" }
      .toSet should be(Set("mapped v[2]", "mapped v[3]", "mapped v[4]"))
  }

  //TODO: flatmap
  //TODO: imports cleanup

}
