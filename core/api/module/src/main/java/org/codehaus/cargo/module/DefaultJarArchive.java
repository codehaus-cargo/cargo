/*
 * ========================================================================
 *
 * Copyright 2003 The Apache Software Foundation. Code from this file
 * was originally imported from the Jakarta Cactus project.
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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
     */
    @Override
    public boolean containsClass(String className) throws IOException
    {
        String resourceName = className.replace('.', '/') + ".class";
        return getResource(resourceName) != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String findResource(String name) throws IOException
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
     */
    @Override
    public InputStream getResource(String path) throws IOException
    {
        try (JarInputStream in = getContentAsStream())
        {
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
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getResources(String path) throws IOException
    {
        String normalizedPath = path;
        if (!path.endsWith("/") && !path.isEmpty())
        {
            normalizedPath = path + "/";
        }

        List<String> resources = new ArrayList<String>();
        try (JarInputStream in = getContentAsStream())
        {
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
     */
    @Override
    public void expandToPath(String path) throws IOException
    {
        try (JarInputStream inputStream = getContentAsStream())
        {
            byte[] buffer = new byte[40960];
            ZipEntry entry;
            while ((entry = inputStream.getNextEntry()) != null)
            {
                String outFile = getFileHandler().append(
                    path, DefaultFileHandler.sanitizeFilename(entry.getName(),
                        getFileHandler().getLogger()));
                if (entry.isDirectory())
                {
                    getFileHandler().mkdirs(outFile);
                }
                else
                {
                    String parent = getFileHandler().getParent(outFile);
                    if (!getFileHandler().exists(parent))
                    {
                        getFileHandler().mkdirs(parent);
                    }

                    if (!getFileHandler().exists(outFile))
                    {
                        getFileHandler().createFile(outFile);
                    }

                    try (OutputStream out = getFileHandler().getOutputStream(outFile))
                    {
                        int read;
                        while ((read = inputStream.read(buffer)) > 0)
                        {
                            out.write(buffer, 0, read);
                        }
                    }
                }
            }
        }
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
