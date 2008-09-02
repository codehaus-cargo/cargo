/* 
 * ========================================================================
 * 
 * Copyright 2004-2006 Vincent Massol.
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
package org.codehaus.cargo.sample.java;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.installer.Proxy;

/**
 * Groups together all environmental test datat (ie data that depends on how the user has
 * configured its tests to run in Maven).
 *
 * @version $Id$
 */
public class EnvironmentTestData
{
    /**
     * Name of container to run (this is the container ID, see
     * {@link org.codehaus.cargo.container.Container#getId()}. We use it guess the XML name of
     * the Ant task.
     */
    public String containerId;

    /**
     * The container type (local, remote, etc)
     */
    public ContainerType containerType;

    /**
     * URL where the zipped container is located.
     */
    public URL installURL;

    /**
     * Location where to put the zipped distributions and where to unpack them.
     */
    public String installDir;

    /**
     * Port on which to start the web container.
     */
    public int port;

    /**
     * Timeout to test if a container is correctly started or stopped.
     */
    public long containerTimeout;

    /**
     * Target directory where build results are stored and where container execution will happen.
     */
    public String targetDir;

    /**
     * Home for the already installed container
     * (in that case installDir and installURL are ignored).
     */
    public String home;

    /**
     * Version of Cargo being built (this is required to compute the exact location of the test
     * data files in the local Maven repository)
     */
    public String version;

    /**
     * Proxy properties if defined (can be null).
     */
    public Proxy proxy;

    /**
     * @param containerId the container's name (eg "resin3x")
     * @param containerType the container's type
     * @param targetDirSuffix relative directory from the base target dir where to put
     *        test-generated files
     */
    public EnvironmentTestData(String containerId, ContainerType containerType,
        String targetDirSuffix)
    {
        this.containerId = containerId;
        this.containerType = containerType;
        this.targetDir = new File(getFileFromString(getSystemProperty("cargo.target.dir")),
            targetDirSuffix).getPath();
        this.installDir = getSystemProperty("cargo.install.dir");
        this.proxy = createProxyElement();
        this.installURL = createInstallURL(containerId);
        this.port = createPort(containerId);
        this.home = getSystemProperty("cargo." + containerId + ".home");
        this.version = System.getProperty("cargo.version");
        this.containerTimeout = Long.parseLong(getSystemProperty("cargo.containers.timeout"));
    }

    /**
     * @param containerName the container's name
     * @return the port to use for the specified container
     */
    private int createPort(String containerName)
    {
        String portString = getSystemProperty("cargo." + containerName + ".port");
        return Integer.parseInt(portString);
    }

    /**
     * @param containerName the container's name
     * @return the install URL for the specified container
     */
    private URL createInstallURL(String containerName)
    {
        URL installURL = null;
        String url = getSystemProperty("cargo." + containerName + ".url");
        if (url != null)
        {
            try
            {
                installURL = new URL(url);
            }
            catch (MalformedURLException e)
            {
                throw new ContainerException("invalid install URL [" + url + "]", e);
            }
        }
        return installURL;
    }

    /**
     * @param name the property name
     * @return the System property for the specified name or null if the System property does not
     *         exist or is empty 
     */
    private String getSystemProperty(String name)
    {
        String result = System.getProperty(name);
        if (result == null || result.length() == 0)
        {
            result = null;
        }
        return result;
    }

    /**
     * @param name the property name
     * @param defaultValue the default value to return if the System property does not exist or is
     *        empty
     * @return the System property for the specified name or the default value
     */
    private String getSystemProperty(String name, String defaultValue)
    {
        String result = System.getProperty(name);
        return (((result == null) || (result.length() == 0)) ? defaultValue : result);
    }

    /**
     * @param fileName the file name as a string
     * @return a File object wrapping the files passed as parameter or null if the file name is 
     *         null
     */
    private File getFileFromString(String fileName)
    {
        File result = null;
        if (fileName != null)
        {
            result = new File(fileName);
        }
        return result;
    }

    /**
     * @return a configured {@link Proxy}, using the System properties defining the proxy
     *         configuration
     */
    private Proxy createProxyElement()
    {
        String proxyHost = getSystemProperty("cargo.proxy.host");
        String proxyPort = getSystemProperty("cargo.proxy.port", "80");
        String proxyUser = getSystemProperty("cargo.proxy.user");
        String proxyPassword = System.getProperty("cargo.proxy.password");
        String proxyExcludeHosts = getSystemProperty("cargo.proxy.excludehosts", "");

        Proxy proxy = null;
        if (proxyHost != null)
        {
            proxy = new Proxy();
            proxy.setHost(proxyHost);
            proxy.setPort(Integer.parseInt(proxyPort));
            if (proxyUser != null)
            {
                proxy.setUser(proxyUser);
                proxy.setPassword(proxyPassword);
            }
            proxy.setExcludeHosts(proxyExcludeHosts);
        }
        return proxy;
    }

    /**
     * @param artifactName the artifact for which to return the location (eg "simple-war").
     * @return the location of the artifact as a string
     */
    public String getTestDataFileFor(String artifactName)
    {
        String localMavenRepository = System.getProperty("localRepository");
        String location = System.getProperty("cargo.testdata." + artifactName);
        if (location == null)
        {
            throw new ContainerException("Test data artifact not found [" + artifactName 
                + "] under base directory [" + localMavenRepository + "]");
        }
        
        return new File(localMavenRepository, location).getPath();
    }
}
