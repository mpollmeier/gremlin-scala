package com.tinkerpop.gremlin

import java.util.function.{Function => JFunction}

package object scala {
  implicit def toJavaFunction[A,B](f: Function1[A,B]) = new JFunction[A,B] {
    override def apply(a: A): B = f(a)
  }
}

