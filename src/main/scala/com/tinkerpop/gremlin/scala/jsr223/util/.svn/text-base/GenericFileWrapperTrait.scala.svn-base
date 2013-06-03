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

trait GenericFileWrapperTrait extends AbstractFile with Wrapper[AbstractFile] {
	override def lookupNameUnchecked(name: String,directory: Boolean) = {
		childWrapper(wrapped.lookupNameUnchecked(name, directory))
	}
	override def lookupName(name: String,directory: Boolean) = {
		wrapped.lookupName(name, directory)
	}
	override def iterator = {
		//TODO wrap
		wrapped.iterator
	}
	override def output = {
		wrapped.output
	}
	override def input = {
		wrapped.input
	}
	
	override def isDirectory = {
		wrapped.isDirectory
	}
	override def delete = {
		wrapped.delete
	}
	override def create = {
		wrapped.create
	}
	override def file = {
		wrapped.file
	}
	override def container = {
		childWrapper(wrapped.container)
	}
	override def absolute = {
		childWrapper(wrapped.absolute)
	}
	override def path = {
		wrapped.path
	}
	override def name = {
		wrapped.name
	}

	override def sizeOption = {
		wrapped.sizeOption
	}

	override def lookupPath(path: String, directory: Boolean): AbstractFile = {
		childWrapper(wrapped.lookupPath(path, directory))
	}
	override def lookupPathUnchecked(path: String, directory: Boolean): AbstractFile = {
		childWrapper(wrapped.lookupPathUnchecked(path, directory))
	}
	override def fileNamed(name: String): AbstractFile = {
		childWrapper(wrapped.fileNamed(name))
	}

	override def subdirectoryNamed(name: String): AbstractFile = {
		childWrapper(wrapped.subdirectoryNamed(name))
	}
}
