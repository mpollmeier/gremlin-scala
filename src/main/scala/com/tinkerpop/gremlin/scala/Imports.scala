package com.tinkerpop.gremlin.scala

import scala.io.Source._
import java.io.File

object Imports {

  def asList = classPathImports

  def path(dir: String = "") = dir + "scala-console-imports"

  def classPathImports =
    List(
      path(),
      path("META-INF/")
    ).flatMap(x => Option(getClass.getClassLoader.getResource(x)))
      .headOption
      .fold(fileSystemImports)(url => fromURL(url).getLines().toList)

  def fileSystemImports =
    List(
        path(),
        path("bin/"),
        path("src/main/resources/"),
        path("src/main/resources/META-INF/"),
        path("../src/main/resources/"),
        path("../src/main/resources/META-INF/")
    ).map(f => new File(f))
      .find(_.exists)
      .fold(defaultImports)(file => fromFile(file).getLines().toList)

  def defaultImports = List(
    "com.tinkerpop.gremlin.scala._",
    "com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory",
    "com.tinkerpop.gremlin.Tokens",
    "com.tinkerpop.blueprints._",
    "com.tinkerpop.blueprints.Compare._",
    "com.tinkerpop.blueprints.Direction._",
    "com.tinkerpop.pipes.util._",
    "com.tinkerpop.pipes.util.iterators._",
    "com.tinkerpop.pipes.util.structures._",
    "com.tinkerpop.blueprints.impls._",
    "com.tinkerpop.blueprints.impls.tg._",
    "com.tinkerpop.blueprints.impls.neo4j._",
    "com.tinkerpop.blueprints.impls.neo4j.batch._",
    //    "com.tinkerpop.blueprints.impls.orient._",
    //    "com.tinkerpop.blueprints.impls.orient.batch._",
    "com.tinkerpop.blueprints.impls.dex._",
    "com.tinkerpop.blueprints.impls.rexster._",
    "com.tinkerpop.blueprints.impls.sail._",
    "com.tinkerpop.blueprints.impls.sail.impls._",
    "com.tinkerpop.blueprints.util._",
    "com.tinkerpop.blueprints.util.io._",
    "com.tinkerpop.blueprints.util.io.gml._",
    "com.tinkerpop.blueprints.util.io.graphml._",
    "com.tinkerpop.blueprints.util.io.graphson._",
    "com.tinkerpop.blueprints.util.wrappers._",
    "com.tinkerpop.blueprints.util.wrappers.batch._",
    "com.tinkerpop.blueprints.util.wrappers.batch.cache._",
    "com.tinkerpop.blueprints.util.wrappers.event._",
    "com.tinkerpop.blueprints.util.wrappers.event.listener._",
    "com.tinkerpop.blueprints.util.wrappers.id._",
    "com.tinkerpop.blueprints.util.wrappers.partition._",
    "com.tinkerpop.blueprints.util.wrappers.readonly._"
  )

}