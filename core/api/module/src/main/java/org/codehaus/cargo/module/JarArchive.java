/*
 * ========================================================================
 *
 * Copyright 2003 The Apache Software Foundation. Code from this file 
 * was originally imported from the Jakarta Cactus project.
 *
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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

import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Provide convenient methods to read information from a Jar archive.
 * 
 * @version $Id$
 */
public interface JarArchive
{
    /**
     * Returns whether a class of the specified name is contained in the archive.
     * 
     * @param className The name of the class to search for
     * @return Whether the class was found
     * @throws java.io.IOException If an I/O error occurred reading the archive
     */
    boolean containsClass(String className) throws IOException;

    /**
     * Returns the full path of a named resource in the archive.
     * 
     * @param name The name of the resource
     * @return The full path to the resource inside the archive
     * @throws java.io.IOException If an I/O error occurred reading the archive
     */
    String findResource(String name) throws IOException;

    /**
     * Returns a resource from the archive as input stream.
     * 
     * @param path The path to the resource in the archive
     * @return An input stream containing the specified resource, or <code>null</code> if the
     * resource was not found in the JAR
     * @throws java.io.IOException If an I/O error occurs
     */
    InputStream getResource(String path) throws IOException;

    /**
     * Returns the list of resources in the specified directory in the archive.
     * 
     * @param path The directory
     * @return The list of resources
     * @throws java.io.IOException If an I/O error occurs
     */
    List<String> getResources(String path) throws IOException;

    /**
     * Expand the archive to the specified directory.
     * @param path The path to expand to
     * @throws java.io.IOException If an I/O error occurs
     */
    void expandToPath(String path) throws IOException;

    /**
     * Expand the archive to the specified directory, filtering out files.
     * 
     * @param path The path to expand to
     * @param filter The filter to use
     * @throws java.io.IOException If an I/O error occurs
     */
    void expandToPath(String path, FileFilter filter) throws IOException;
}
