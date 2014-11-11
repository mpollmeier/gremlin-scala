package com.tinkerpop.gremlin.scala

import scala.reflect._
import scala.reflect.ClassTag

// contributed by Joan38: https://gist.github.com/joan38/a95877c8c776a6db42b1
object CaseClassWrapper {

  implicit class ScalaGraphCaseClass(graph: ScalaGraph) {
    def saveCC[A: ClassTag](cc: A) = {
      val runtimeClass = classTag[A].runtimeClass
      val params = (runtimeClass.getDeclaredFields map { field ⇒
        field.setAccessible(true)
        field.getName -> field.get(cc)
      } filter (_._1 != "id")).toMap + ("label" -> runtimeClass.getSimpleName)

      graph.addVertex().setProperties(params)
    }
  }

  implicit class VertexCaseClass(vertex: Vertex) {
    def toCC[T: ClassTag] = {
      val runtimeClass = classTag[T].runtimeClass
      val params = runtimeClass.getDeclaredFields map {
        case field if field.getName == "id" ⇒ vertex.id.toString
        case field                          ⇒ vertex.value[AnyRef](field.getName)
      }
      runtimeClass.getDeclaredConstructors.head.newInstance(params: _*).asInstanceOf[T]
    }
  }

}
