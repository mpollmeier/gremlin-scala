package com.tinkerpop.gremlin.scala

import shapeless.HNil

case class ScalaEdge(edge: Edge) extends ScalaElement[Edge] {
   override def element = edge

   def setProperty(key: String, value: Any): ScalaEdge = {
     element.property(key, value)
     this
   }

   def setProperties(properties: Map[String, Any]): ScalaEdge = {
     properties foreach { case (k, v) ⇒ setProperty(k, v) }
     this
   }

   def removeProperty(key: String): ScalaEdge = {
     val p = property(key)
     if (p.isPresent) p.remove()
     this
   }

   def withSideEffect[A](key: String, value: A) = start().withSideEffect(key, value)

   def start() = GremlinScala[ Edge, HNil](edge.start)

   //TODO: wait until this is consistent in T3 between Vertex and Edge
   //currently Vertex.outE returns a GraphTraversal, Edge.inV doesnt quite exist
   //def inV() = GremlinScala[ Vertex, HNil](edge.inV())
 }