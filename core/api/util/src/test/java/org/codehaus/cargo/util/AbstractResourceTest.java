/*
 * ========================================================================
 *
 * Copyright 2005-2006 Vincent Massol.
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

import junit.framework.TestCase;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;

/**
 * Helper {@link junit.framework.TestCase} that provides convenience methods to retrieve test
 * resources from the classpath.
 *
 * @version $Id$
 */
public class AbstractResourceTest extends TestCase
{
    /**
     * Returns a resource found in the classpath.
     *
     * @param fileName The name of the file in the classpath. For example,
     *            "org/codehaus/cargo/module/empty.jar".
     * @return The resource as a URL
     * @throws java.net.MalformedURLException if the resource produces an invalid URL
     */
    protected URL getResource(String fileName) throws MalformedURLException
    {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource(fileName);

        assertNotNull("The test input file [" + fileName + "] does not exist", resource);

        // NOTE: this is required since URLClassLoader.getResource() can
        // produce unencoded URLs that are not compatible with URIs.
        // URLClassLoader subclasses should encode URLs as per the boot
        // classloader sun.misc.Launcher.AppClassLoader
        resource = new URL(resource, AbstractResourceTest.encodePath(resource.getPath()));

        return resource;
    }

    /**
     * Returns a resource file found in the classpath.
     *
     * @param relativePath The relative path name in the classpath.
     *        For example, "org/codehaus/cargo/module/empty.jar".
     * @return The resource as a File object
     * @throws java.net.MalformedURLException if the resource produces an invalid URL
     * @throws java.net.URISyntaxException if the resource produces an invalid URI
     */
    protected String getResourcePath(String relativePath) throws MalformedURLException, URISyntaxException
    {
        return new URI(getResource(relativePath).toString()).getPath();
    }

    /**
     * Encodes the given path for compatibility with URIs.
     *
     * @param path the path to encode
     * @return the encoded path
     */
    private static String encodePath(String path)
    {
        // TODO: this method should perform the same full encoding as per
        // sun.net.www.ParseUtil.encodePath(String)

        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < path.length(); i++)
        {
            char c = path.charAt(i);
            if (c == ' ')
                buffer.append("%20");
            else
                buffer.append(c);
        }
        return buffer.toString();
    }

    /**
     * Asserts whether the content of the specified input stream matches the
     * specified string line per line.
     *
     * @param theInput The input stream to check
     * @param theExpectedString The expected string
     * @throws java.io.IOException If an I/O error occurs reading from the input stream
     */
    protected void assertContains(InputStream theInput, String theExpectedString) throws IOException
    {
        try
        {
            BufferedReader inReader = new BufferedReader(new InputStreamReader(theInput));
            BufferedReader stringReader = new BufferedReader(new StringReader(theExpectedString));
            String line;
            while ((line = inReader.readLine()) != null)
            {
                assertEquals(stringReader.readLine(), line);
            }
        }
        finally
        {
            if (theInput != null)
            {
                theInput.close();
            }
        }
    }
}
