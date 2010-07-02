/*
 * ========================================================================
 *
 * Copyright 2003-2005 The Apache Software Foundation. Code from this file 
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
package org.codehaus.cargo.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
import java.net.URI;

/**
 * Set of common File manipulation utility methods.
 *
 * @deprecated use {@link FileHandler} instead
 * @version $Id$
 */
@Deprecated
public class FileUtils
{
    /**
     * Counter for creating unique temp directories.
     */
    private static int uniqueNameCounter = -1;

    /**
     * Convenience method for creating a new directory inside another one.
     *
     * @param parentDir The directory in which the new directory should be created
     * @param name The name of the directory to create
     *
     * @return The new directory
     *
     * @throws IOException If the directory could not be created
     */
    public final File createDirectory(URI parentDir, String name) throws IOException
    {
        File dir = new File(parentDir.getPath(), name);
        dir.mkdirs();
        if (!dir.isDirectory())
        {
            throw new IOException("Couldn't create directory " + dir.getAbsolutePath());
        }
        return dir;
    }

    /**
     * Convenience method for creating a new directory inside another one.
     *
     * @param parentDir The directory in which the new directory should be created
     * @param name The name of the directory to create
     *
     * @return The new directory
     *
     * @throws IOException If the directory could not be created
     */
    public final File createDirectory(File parentDir, String name) throws IOException
    {
        return createDirectory(parentDir.toURI(), name);
    }

    /**
     * Creates a temporary directory.
     *
     * @param name The name of the directory to create
     * @return the newly created temporary directory
     */
    public File createTmpDirectory(String name)
    {
        File tmpDir = new File(new File(System.getProperty("java.io.tmpdir"), "cargo"),
            name);
        tmpDir.mkdirs();
        return tmpDir;
    }

    /**
     * Convenience method that returns a relative filename and its extension from a complete file
     * path (path, name and extension).
     *
     * @param filePath the full file path (including relative name and extension)
     *
     * @return the filename with its extension
     */
    public final String getFilename(String filePath)
    {
        int index = filePath.lastIndexOf(File.separator);
        return (index >= 0 ? filePath.substring(index + 1) : filePath);
    }

    /**
     * Creates a unique temporary directory.
     *
     * @return the newly created temporary directory
     */
    public synchronized File createUniqueTmpDirectory()
    {
        if (uniqueNameCounter == -1)
        {
            uniqueNameCounter = new Random().nextInt() & 0xffff;
        }
        File tmpDir;
        do
        {
            uniqueNameCounter++;
            tmpDir = new File(new File(System.getProperty("java.io.tmpdir")),
                "cargo/" + Integer.toString(uniqueNameCounter));
        }
        while (tmpDir.exists());
        tmpDir.deleteOnExit();
        tmpDir.mkdirs();

        return tmpDir;
    }

    /**
     * Copies data from an InputStream to an OutputStream.
     *
     * @param in InputStream to copy data from
     * @param out OutputStream to copy data to
     *
     * @throws IOException if an I/O error occurs
     */
    public void copy(InputStream in, OutputStream out) throws IOException
    {
        copy(in, out, 1024);
    }

    /**
     * Copies data from an InputStream to an OutputStream.
     *
     * @param in InputStream to copy data from
     * @param out OutputStream to copy data to
     * @param bufSize size of the copy buffer
     *
     * @throws IOException if an I/O error occurs
     */
    public void copy(InputStream in, OutputStream out, int bufSize) throws IOException
    {
        byte[] buf = new byte[bufSize];
        int length;
        while ((length = in.read(buf)) != -1)
        {
            out.write(buf, 0, length);
        }
    }

    /**
     * Deletes a file or directory, removing any children as appropriate.
     * 
     * @param item in file or directory to remove
     */
    public void delete(File item)
    {
        if (item.isDirectory())
        {
            File[] children = item.listFiles();
            for (File element : children)
            {
                delete(element);
            }
        }
        item.delete();
    }
}
