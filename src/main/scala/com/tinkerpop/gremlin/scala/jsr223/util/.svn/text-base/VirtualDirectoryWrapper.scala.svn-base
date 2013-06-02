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

class VirtualDirectoryWrapper(val wrapped: AbstractFile,
							  val childWrapper: (AbstractFile) => AbstractFile) extends VirtualDirectory(null, None)
																				   with GenericFileWrapperTrait {
	lastModified =wrapped.lastModified

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
	override val name = {
		wrapped.name
	}
	override def lookupPath(path: String, directory: Boolean): AbstractFile = {
		childWrapper(wrapped.lookupPath(path, directory))
	}
	override def lookupPathUnchecked(path: String, directory: Boolean): AbstractFile = {
		childWrapper(wrapped.lookupPathUnchecked(path, directory))
	}

}

object VirtualDirectoryWrapper {
	trait VirtualDirectoryFlavour extends VirtualDirectoryWrapper {
		abstract override def output = {
			println("unexpected call to output "+name)
			super.output
		}
	}

	def wrap(f: AbstractFile, outputListenerParam: (AbstractFile) => Unit): AbstractFile = {
		def innerWrap(f: AbstractFile) = wrap(f, outputListenerParam)
		f match {
			case d: VirtualDirectory => new VirtualDirectoryWrapper(d, 
										innerWrap)
										with LoggingFileWrapper with VirtualDirectoryFlavour {
					override def output = d.output
					val outputListener = outputListenerParam
				}
			case o => new FileWrapper(o, innerWrap) with LoggingFileWrapper {
					val outputListener = outputListenerParam
				}
		}
	}

	trait LoggingFileWrapper extends GenericFileWrapperTrait {

		val outputListener: (AbstractFile) => Unit

		abstract override def output = {
			outputListener(this)
			super.output
		}
	}
}
