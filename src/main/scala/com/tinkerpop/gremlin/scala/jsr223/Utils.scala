/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tinkerpop.gremlin.scala.jsr223

/**
 * General purpose utility functions
 */
object Utils {

  /**
   * Evaluate <code>f</code> on <code>s</code> if <code>s</code> is not null.
   * @param s
   * @param f
   * @return <code>f(s)</code> if s is not <code>null</code>, <code>null</code> otherwise.
   */
  protected[jsr223] def nullOrElse[S, T](s: S)(f: S => T): T =
    if (s == null) null.asInstanceOf[T]
    else f(s)

  /**
   * @param t
   * @param default
   * @return <code>t</code> or <code>default</code> if <code>null</code>.
   */
  protected[jsr223] def valueOrElse[T](t: T)(default: => T) =
    if (t == null) default
    else t

  /**
   * Converts a value into an Option.
   * @param value
   * @returns <code>Some(value)</code> if value is not <code>null</code>,
   * <code>None</code> otherwise.
   */
    protected[jsr223] def option[T](value: T): Option[T] =
    if (null == value) None else Some(value)

}
