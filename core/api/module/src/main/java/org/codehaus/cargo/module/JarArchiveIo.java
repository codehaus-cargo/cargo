/* 
 * ========================================================================
 * 
 * Copyright 2005-2007 Vincent Massol.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ========================================================================
 */
package org.codehaus.cargo.module;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * Utility IO class for constructing Jar Archives. Use this class in preference
 * to the concrete implementations of JarArchive.
 * 
 * @version $Id: $
 *
 */
public class JarArchiveIo
{
	/**
	 * Open a jar archive.
	 * 
	 * @param file Input File 
	 * @return WarArchive
	 * @throws IOException if an IO error occurs
	 */
	public static JarArchive open(String file) throws IOException
	{
		return new DefaultJarArchive(file);
	}
	
	/**
	 * Open a jar archive.
	 * 
	 * @param is Input Stream 
	 * @return WarArchive
	 * @throws IOException if an IO error occurs
	 */
	public static JarArchive open(InputStream is) throws IOException
	{
		return new DefaultJarArchive(is);
	}
	
	/**
	 * Open a jar archive.
	 * 
	 * @param f Input File 
	 * @return WarArchive
	 * @throws IOException if an IO error occurs
	 */
	public static JarArchive open(File f) throws IOException
	{
		return new DefaultJarArchive(f.getAbsolutePath());
	}
	
}
