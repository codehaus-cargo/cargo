/*
 * ========================================================================
 *
 * Copyright 2003-2006 The Apache Software Foundation. Code from this file
 * was originally imported from the Jakarta Cactus project.
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.codehaus.cargo.util.CargoException;
import org.codehaus.cargo.util.DefaultFileHandler;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.log.LoggedObject;
import org.codehaus.cargo.util.log.Logger;

/**
 * Utility class that provides a couple of methods for extracting files stored as resource in a JAR.
 */
public final class ResourceUtils extends LoggedObject
{
    /**
     * Default file handler for the @link{ResourceUtils#copyResource(String, File)} and
     * @link{ResourceUtils#copyResource(String, File, Map, Charset)} methods.
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
     * Overriden in order to set the logger on ancillary components.
     * {@inheritDoc}
     * 
     * @param logger the logger to set and set in the ancillary objects
     */
    @Override
    public void setLogger(Logger logger)
    {
        super.setLogger(logger);
        ResourceUtils.defaultFileHandler.setLogger(logger);
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
     * @param replacements The ordered list of replacements that should be applied while copying
     * @param encoding The encoding that should be used when copying the resource. Use null for
     * system default encoding
     * @throws IOException If an I/O error occurs while copying the resource
     */
    public void copyResource(String resourceName, File destFile, Map<String, String> replacements,
        Charset encoding) throws IOException
    {
        copyResource(resourceName, destFile.getPath(), defaultFileHandler, replacements, encoding);
    }

    /**
     * Copies a container resource from the JAR into the specified file, using the specified file
     * handler thereby applying the specified filters.
     * 
     * @param resourceName The name of the resource, relative to the
     * org.codehaus.cargo.container.internal.util package
     * @param destFile The file to which the contents of the resource should be copied
     * @param handler The file handler to be used for file copy
     * @param replacements The ordered list of replacements that should be applied while copying
     * @param encoding The encoding that should be used when copying the resource. Use null for
     * system default encoding
     * @throws IOException If an I/O error occurs while copying the resource
     */
    public void copyResource(String resourceName, String destFile, FileHandler handler,
        Map<String, String> replacements, Charset encoding) throws IOException
    {
        handler.writeTextFile(
            destFile, readResource(resourceName, replacements, encoding), encoding);
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
            int pling = urlString.indexOf('!');
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
     * @param replacements The ordered list of replacements that should be applied while reading
     * @param encoding The encoding that should be used when reading the resource. Use null for
     * system default encoding
     * @return Content of resource as String.
     * @throws IOException If an I/O error occurs while reading the resource
     */
    public String readResource(String resourceName, Map<String, String> replacements,
        Charset encoding) throws IOException
    {
        try (InputStream resource = ResourceUtils.resourceLoader.getResourceAsStream(resourceName))
        {
            if (resource == null)
            {
                throw new CargoException("Resource [" + resourceName
                    + "] not found in resource loader " + ResourceUtils.resourceLoader);
            }

            try (BufferedReader in = new BufferedReader(new InputStreamReader(resource, encoding)))
            {
                String line;
                StringBuilder out = new StringBuilder();
                while ((line = in.readLine()) != null)
                {
                    if (line.isEmpty())
                    {
                        out.append(FileHandler.NEW_LINE);
                    }
                    else
                    {
                        if (out.length() > 0)
                        {
                            out.append(FileHandler.NEW_LINE);
                        }
                        out.append(line);
                    }
                }
                String output = out.toString();
                if (replacements != null)
                {
                    for (Map.Entry<String, String> replacement : replacements.entrySet())
                    {
                        String replacementKey = "@" + replacement.getKey() + "@";
                        output = output.replace(replacementKey, replacement.getValue());
                    }
                }
                return output;
            }
        }
    }
}
