package com.tinkerpop.gremlin.scala

import com.tinkerpop.gremlin.process.T
import shapeless.HNil

case class ScalaVertex(vertex: Vertex) extends ScalaElement[Vertex] {
   override def element = vertex

   def setProperty(key: String, value: Any): ScalaVertex = {
     element.property(key, value)
     this
   }

   def setProperties(properties: Map[String, Any]): ScalaVertex = {
     properties foreach { case (k, v) ⇒ setProperty(k, v) }
     this
   }

   def removeProperty(key: String): ScalaVertex = {
     val p = property(key)
     if (p.isPresent) p.remove
     this
   }

   def out() = GremlinScala[ Vertex, HNil](vertex.out())
   def out(labels: String*) = GremlinScala[ Vertex, HNil](vertex.out(labels: _*))

   def outE() = GremlinScala[ Edge, HNil](vertex.outE())
   def outE(labels: String*) = GremlinScala[ Edge, HNil](vertex.outE(labels: _*))

   def in() = GremlinScala[ Vertex, HNil](vertex.in())
   def in(labels: String*) = GremlinScala[ Vertex, HNil](vertex.in(labels: _*))

   def inE() = GremlinScala[ Edge, HNil](vertex.inE())
   def inE(labels: String*) = GremlinScala[ Edge, HNil](vertex.inE(labels: _*))

   def both() = GremlinScala[ Vertex, HNil](vertex.both())
   def both(labels: String*) = GremlinScala[ Vertex, HNil](vertex.both(labels: _*))

   def bothE() = GremlinScala[ Edge, HNil](vertex.bothE())
   def bothE(labels: String*) = GremlinScala[ Edge, HNil](vertex.bothE(labels: _*))

   def addEdge(label: String, inVertex: ScalaVertex, properties: Map[String, Any]): ScalaEdge = {
     val e = ScalaEdge(vertex.addEdge(label, inVertex.vertex))
     e.setProperties(properties)
     e
   }

   def addEdge(id: AnyRef, label: String, inVertex: ScalaVertex, properties: Map[String, Any]): ScalaEdge = {
     val e = ScalaEdge(vertex.addEdge(label, inVertex.vertex, T.id, id))
     e.setProperties(properties)
     e
   }

   def withSideEffect[A](key: String, value: A) = start.withSideEffect(key, value)

   def start() = GremlinScala[ Vertex, HNil](vertex.start)
 }