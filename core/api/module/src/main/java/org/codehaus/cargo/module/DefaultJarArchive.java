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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;

import org.codehaus.cargo.util.DefaultFileHandler;
import org.codehaus.cargo.util.FileHandler;

/**
 * Provide convenient methods to read information from a Jar archive.
 * 
 * @version $Id$
 */
public class DefaultJarArchive implements JarArchive
{
    /**
     * The content of the archive as an input stream.
     */
    private byte[] content;

    /**
     * The archive itself, if passed in as a file.
     */
    private String sourceFile;

    /**
     * File utility class to use for performing all file I/O.
     */
    private FileHandler fileHandler = new DefaultFileHandler();

    /**
     * Constructor.
     * 
     * @param file The archive file
     */
    public DefaultJarArchive(String file)
    {
        if (file == null)
        {
            throw new NullPointerException();
        }
        this.sourceFile = file;
        this.content = null;
    }

    /**
     * Constructor.
     * 
     * @param inputStream The input stream for the archive (it will be closed after the constructor
     * returns)
     * @throws java.io.IOException If there was a problem reading the WAR
     */
    public DefaultJarArchive(InputStream inputStream) throws IOException
    {
        this.sourceFile = null;
        this.content = streamToByteArray(inputStream);
    }

    /**
     * @return the file utility class to use for performing all file I/O.
     */
    public FileHandler getFileHandler()
    {
        return this.fileHandler;
    }

    /**
     * @param fileHandler the file utility class to use for performing all file I/O.
     */
    public void setFileHandler(FileHandler fileHandler)
    {
        this.fileHandler = fileHandler;
    }

    /**
     * {@inheritDoc}
     * @see JarArchive#containsClass(String)
     */
    public boolean containsClass(String className) throws IOException
    {
        String resourceName = className.replace('.', '/') + ".class";
        return getResource(resourceName) != null;
    }

    /**
     * {@inheritDoc}
     * @see JarArchive#findResource(String)
     */
    public final String findResource(String name) throws IOException
    {
        String result = null;
        for (String entryPath : getResources(""))
        {
            String entryName = entryPath;

            int lastSlashIndex = entryName.lastIndexOf('/');
            if (lastSlashIndex >= 0)
            {
                entryName = entryName.substring(lastSlashIndex + 1);
            }

            if (entryName.equals(name))
            {
                result = entryPath;
                break;
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     * @see JarArchive#getResource(String)
     */
    public final InputStream getResource(String path) throws IOException
    {
        JarInputStream in = null;
        try
        {
            in = getContentAsStream();
            ZipEntry zipEntry;
            while ((zipEntry = in.getNextEntry()) != null)
            {
                if (path.equals(zipEntry.getName()))
                {
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    byte[] bytes = new byte[2048];
                    int bytesRead;
                    while ((bytesRead = in.read(bytes)) != -1)
                    {
                        buffer.write(bytes, 0, bytesRead);
                    }
                    return new ByteArrayInputStream(buffer.toByteArray());
                }
            }
        }
        finally
        {
            if (in != null)
            {
                in.close();
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.module.JarArchive#getResources(String)
     */
    public final List<String> getResources(String path) throws IOException
    {
        String normalizedPath = path;
        if (!path.endsWith("/") && !path.equals(""))
        {
            normalizedPath = path + "/";
        }

        List<String> resources = new ArrayList<String>();
        JarInputStream in = null;
        try
        {
            in = getContentAsStream();
            ZipEntry zipEntry;
            while ((zipEntry = in.getNextEntry()) != null)
            {
                if (zipEntry.getName().startsWith(normalizedPath)
                    && !zipEntry.getName().equals(normalizedPath))
                {
                    resources.add(zipEntry.getName());
                }
            }
        }
        finally
        {
            if (in != null)
            {
                in.close();
            }
        }
        return resources;
    }

    /**
     * Returns the content of the archive as <code>JarInputStream</code>.
     * 
     * @return The input stream
     * @throws IOException If an exception occurred reading the archive
     */
    protected final JarInputStream getContentAsStream() throws IOException
    {
        if (this.content != null)
        {
            return new JarInputStream(new ByteArrayInputStream(this.content));
        }

        return new JarInputStream(getFileHandler().getInputStream(this.sourceFile));
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.module.JarArchive#expandToPath(String)
     */
    public void expandToPath(String path) throws IOException
    {
        expandToPath(path, null);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.module.JarArchive#expandToPath(String, FileFilter)
     */
    public void expandToPath(String path, FileFilter filter) throws IOException
    {
        JarInputStream inputStream = getContentAsStream();

        byte[] buffer = new byte[40960];

        ZipEntry entry;
        while ((entry = inputStream.getNextEntry()) != null)
        {
            String entryName = entry.getName();

            String outFile = getFileHandler().append(path, entryName);

            if (filter == null || filter.accept(new File(entryName)))
            {
                if (outFile.endsWith("/"))
                {
                    getFileHandler().mkdirs(outFile);
                }
                else
                {
                    if (!getFileHandler().exists(getFileHandler().getParent(outFile)))
                    {
                        getFileHandler().mkdirs(getFileHandler().getParent(outFile));
                    }

                    if (!getFileHandler().exists(outFile))
                    {
                        getFileHandler().createFile(outFile);
                    }

                    OutputStream out = getFileHandler().getOutputStream(outFile);
                    int read;
                    while ((read = inputStream.read(buffer)) > 0)
                    {
                        out.write(buffer, 0, read);
                    }

                    out.close();
                }
            }
        }
        inputStream.close();
    }

    /**
     * Read a stream into a byte array.
     * @param inputStream the input stream
     * @return the byte array
     * @throws java.io.IOException if an IO Exception
     */
    protected byte[] streamToByteArray(InputStream inputStream) throws IOException
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[40960];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1)
            {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        }
        finally
        {
            if (inputStream != null)
            {
                inputStream.close();
            }
        }
    }
}
