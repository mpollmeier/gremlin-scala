/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.apache.clerezza.scala.scripting.util

import scala.tools.nsc.io.AbstractFile
import scala.tools.nsc.io.VirtualDirectory

class SplittingDirectory()
		extends VirtualDirectory(null, None) with GenericFileWrapperTrait {

	var currentTarget: VirtualDirectory = null

	protected def wrapped: VirtualDirectory = {
		if (currentTarget == null) {
			throw new RuntimeException("No current Target set, SplittingDirectory not usable")
		}
		currentTarget
	}

	private def wrap(f: AbstractFile): AbstractFile =  {
		f match {
			case d: VirtualDirectory => new VirtualDirectoryWrapper(d, wrap) {
					override def output = d.output
				}
			case o => new FileWrapper(o, wrap)
		}
	}
	val childWrapper: (AbstractFile) => AbstractFile = wrap

	//lastModified = wrapped.lastModified

	override def output = {
		wrapped.asInstanceOf[VirtualDirectory].output
	}
	override def input = {
		wrapped.asInstanceOf[VirtualDirectory].input
	}
	override def file = {
		wrapped.asInstanceOf[VirtualDirectory].file
	}
	override def container = {
		wrapped.asInstanceOf[VirtualDirectory].container
	}
	override def absolute = {
		wrapped.asInstanceOf[VirtualDirectory].absolute
	}
	override val name = "(splitting)"
	
	override def lookupPath(path: String, directory: Boolean): AbstractFile = {
		childWrapper(wrapped.lookupPath(path, directory))
	}
	override def lookupPathUnchecked(path: String, directory: Boolean): AbstractFile = {
		childWrapper(wrapped.lookupPathUnchecked(path, directory))
	}

}

