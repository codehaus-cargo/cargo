/*
 * ========================================================================
 *
 * Copyright 2003-2006 The Apache Software Foundation. Code from this file
 * was originally imported from the Jakarta Cactus project.
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol, 2012-2020 Ali Tokmen.
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
package org.codehaus.cargo.container.internal.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Vector;

import org.apache.tools.ant.filters.util.ChainReaderHelper;
import org.apache.tools.ant.types.FilterChain;
import org.codehaus.cargo.util.CargoException;
import org.codehaus.cargo.util.DefaultFileHandler;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.log.LoggedObject;

/**
 * Utility class that provides a couple of methods for extracting files stored as resource in a JAR.
 */
public final class ResourceUtils extends LoggedObject
{
    /**
     * Size of the buffers / chunks used when copying resources.
     */
    private static final int BUFFER_CHUNK_SIZE = 256 * 1024;

    /**
     * Default file handler for the @link{ResourceUtils#copyResource(String, File)} and
     * @link{ResourceUtils#copyResource(String, File, FilterChain)} methods.
     */
    private static FileHandler defaultFileHandler = new DefaultFileHandler();

    /**
     * Class loader used for the <code>getResourceAsStream</code> calls.
     */
    private static ClassLoader resourceLoader = ResourceUtils.class.getClassLoader();

    /**
     * @return Class loader used for the <code>getResourceAsStream</code> calls.
     */
    public static ClassLoader getResourceLoader()
    {
        return ResourceUtils.resourceLoader;
    }

    /**
     * @param resourceLoader Class loader used for the <code>getResourceAsStream</code> calls.
     */
    public static void setResourceLoader(ClassLoader resourceLoader)
    {
        ResourceUtils.resourceLoader = resourceLoader;
    }

    /**
     * Copies a container resource from the JAR into the specified file.
     * 
     * @param resourceName The name of the resource
     * @param destFile The file to which the contents of the resource should be copied
     * @throws IOException If an I/O error occurs while copying the resource
     */
    public void copyResource(String resourceName, File destFile) throws IOException
    {
        copyResource(resourceName, destFile.getPath(), defaultFileHandler);
    }

    /**
     * Copies a container resource from the JAR into the specified file using the specified file
     * handler.
     * 
     * @param resourceName The name of the resource
     * @param destFile The file to which the contents of the resource should be copied
     * @param handler The file handler to use
     * @throws IOException If an I/O error occurs while copying the resource
     */
    public void copyResource(String resourceName, String destFile, FileHandler handler)
        throws IOException
    {
        InputStream in = ResourceUtils.resourceLoader.getResourceAsStream(resourceName);
        if (in == null)
        {
            throw new IOException("Resource [" + resourceName
                + "] not found in resource loader " + ResourceUtils.resourceLoader);
        }

        try (OutputStream out = handler.getOutputStream(destFile))
        {
            byte[] buf = new byte[4096];
            int numBytes;
            while ((numBytes = in.read(buf)) > 0)
            {
                out.write(buf, 0, numBytes);
            }
        }
        finally
        {
            in.close();
        }
    }

    /**
     * Copies a container resource from the JAR into the specified file, thereby applying the
     * specified filters.
     * 
     * @param resourceName The name of the resource, relative to the
     * org.codehaus.cargo.container.internal.util package
     * @param destFile The file to which the contents of the resource should be copied
     * @param filterChain The ordered list of filter readers that should be applied while copying
     * @param encoding The encoding that should be used when copying the resource. Use null for
     * system default encoding
     * @throws IOException If an I/O error occurs while copying the resource
     */
    public void copyResource(String resourceName, File destFile, FilterChain filterChain,
        Charset encoding) throws IOException
    {
        copyResource(resourceName, destFile.getPath(), defaultFileHandler, filterChain, encoding);
    }

    /**
     * Copies a container resource from the JAR into the specified file, using the specified file
     * handler thereby applying the specified filters.
     * 
     * @param resourceName The name of the resource, relative to the
     * org.codehaus.cargo.container.internal.util package
     * @param destFile The file to which the contents of the resource should be copied
     * @param handler The file handler to be used for file copy
     * @param filterChain The ordered list of filter readers that should be applied while copying
     * @param encoding The encoding that should be used when copying the resource. Use null for
     * system default encoding
     * @throws IOException If an I/O error occurs while copying the resource
     */
    public void copyResource(String resourceName, String destFile, FileHandler handler,
        FilterChain filterChain, Charset encoding) throws IOException
    {
        InputStream resource = ResourceUtils.resourceLoader.getResourceAsStream(resourceName);
        if (resource == null)
        {
            throw new IOException("Resource [" + resourceName
                + "] not found in resource loader " + ResourceUtils.resourceLoader);
        }

        ChainReaderHelper helper = new ChainReaderHelper();
        helper.setBufferSize(ResourceUtils.BUFFER_CHUNK_SIZE);
        helper.setPrimaryReader(new BufferedReader(createReader(resource, encoding)));
        Vector filterChains = new Vector();
        filterChains.add(filterChain);
        helper.setFilterChains(filterChains);
        try (BufferedReader in =
                new BufferedReader(DefaultFileHandler.getAssembledReader(helper));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                handler.getOutputStream(destFile))))
        {
            String line;
            while ((line = in.readLine()) != null)
            {
                if (line.isEmpty())
                {
                    out.newLine();
                }
                else
                {
                    out.write(line);
                    out.newLine();
                }
            }
        }
    }

    /**
     * Creates a new InputStreamReader with provide encoding
     * 
     * @param is the stream used to create the reader
     * @param encoding the encoding used to create the reader. If it is <code>null</code> then the
     * default system encoding will be used.
     * @return a new reader for provided stream and encoding
     * @throws UnsupportedEncodingException If the named charset is not supported
     */
    private InputStreamReader createReader(InputStream is, Charset encoding)
        throws UnsupportedEncodingException
    {
        InputStreamReader r;
        if (encoding != null)
        {
            r = new InputStreamReader(is, encoding);
        }
        else
        {
            r = new InputStreamReader(is);
        }
        return r;
    }

    /**
     * Search for the given resource and return the directory or archive that contains it.
     * 
     * @param where Class where to look for the resource (its class loader and parent class loaders
     * are used recursively for the lookup).
     * @param resourceName The name of the resource
     * @return The directory or archive containing the specified resource
     */
    public File getResourceLocation(Class where, String resourceName)
    {
        URL url = where.getResource(resourceName);
        if (url == null)
        {
            throw new CargoException("Cannot find resource [" + resourceName + "]");
        }

        File file = null;
        String urlString = url.toString();
        if (urlString.startsWith("jar:file:"))
        {
            int pling = urlString.indexOf("!");
            String jar = urlString.substring(9, pling);
            // TODO: URLDecoder.decode(String, Charset) was introduced in Java 10,
            //       simplify the below code when Codehaus Cargo is on Java 10+
            try
            {
                file = new File(URLDecoder.decode(jar, StandardCharsets.UTF_8.name()));
            }
            catch (UnsupportedEncodingException e)
            {
                throw new IllegalStateException("UTF-8 encoding is missing", e);
            }
        }
        else if (urlString.startsWith("file:"))
        {
            int tail = urlString.indexOf(resourceName);
            String dir = urlString.substring(5, tail);
            // TODO: URLDecoder.decode(String, Charset) was introduced in Java 10,
            //       simplify the below code when Codehaus Cargo is on Java 10+
            try
            {
                file = new File(URLDecoder.decode(dir, StandardCharsets.UTF_8.name()));
            }
            catch (UnsupportedEncodingException e)
            {
                throw new IllegalStateException("UTF-8 encoding is missing", e);
            }
        }

        getLogger().debug("Location for [" + resourceName + "] is [" + file + "]",
            this.getClass().getName());

        return file;
    }

    /**
     * Reads a container resource from the JAR, applies the specified filters and returns content
     * as String.
     * 
     * @param resourceName The name of the resource, relative to the
     * <code>org.codehaus.cargo.container.internal.util</code> package
     * @param filterChain The ordered list of filter readers that should be applied while reading
     * @param encoding The encoding that should be used when reading the resource. Use null for
     * system default encoding
     * @return Content of resource as String.
     * @throws IOException If an I/O error occurs while reading the resource
     */
    public String readResource(String resourceName, FilterChain filterChain, Charset encoding)
        throws IOException
    {
        String newLine = System.getProperty("line.separator");
        InputStream resource = ResourceUtils.resourceLoader.getResourceAsStream(resourceName);

        if (resource == null)
        {
            throw new CargoException("Resource [" + resourceName
                + "] not found in resource loader " + ResourceUtils.resourceLoader);
        }

        ChainReaderHelper helper = new ChainReaderHelper();
        helper.setBufferSize(8192);
        helper.setPrimaryReader(new BufferedReader(createReader(resource, encoding)));
        Vector<FilterChain> filterChains = new Vector<FilterChain>();
        filterChains.add(filterChain);
        helper.setFilterChains(filterChains);
        try (BufferedReader in =
                new BufferedReader(DefaultFileHandler.getAssembledReader(helper)))
        {
            String line;
            StringBuilder out = new StringBuilder();
            while ((line = in.readLine()) != null)
            {
                if (line.isEmpty())
                {
                    out.append(newLine);
                }
                else
                {
                    if (out.length() > 0)
                    {
                        out.append(newLine);
                    }
                    out.append(line);
                }
            }
            return out.toString();
        }
    }
}
