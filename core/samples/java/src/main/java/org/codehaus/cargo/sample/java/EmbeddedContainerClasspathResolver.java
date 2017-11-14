/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol, 2012-2017 Ali Tokmen.
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
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.cargo.container.internal.util.JdkUtils;
import org.codehaus.cargo.util.CargoException;

/**
 * Create a classloader to load container classes.
 */
public class EmbeddedContainerClasspathResolver
{
    /**
     * Map of dependencies, with key the container identifier and value the list of dependencies.
     */
    private static final Map<String, List<String>> DEPENDENCIES =
        new HashMap<String, List<String>>();

    static
    {
        List<String> jetty4xDependencies = new ArrayList<String>();
        jetty4xDependencies.add("lib/*.jar");
        jetty4xDependencies.add("ext/*.jar");

        List<String> jetty5xDependencies = new ArrayList<String>();
        jetty5xDependencies.add("lib/*.jar");
        jetty5xDependencies.add("ext/*.jar");

        List<String> jetty6xDependencies = new ArrayList<String>();
        jetty6xDependencies.add("lib/*.jar");
        jetty6xDependencies.add("lib/jsp-2.0/*.jar");
        jetty6xDependencies.add("lib/management/*.jar");
        jetty6xDependencies.add("lib/naming/*.jar");
        jetty6xDependencies.add("lib/plus/*.jar");
        jetty6xDependencies.add("lib/xbean/*.jar");

        List<String> jetty7xDependencies = new ArrayList<String>();
        jetty7xDependencies.add("lib/*.jar");
        jetty7xDependencies.add("lib/jndi/*.jar");
        jetty7xDependencies.add("lib/jsp/*.jar");

        List<String> jetty8x9xDependencies = new ArrayList<String>();
        jetty8x9xDependencies.add("lib/*.jar|lib/jndi/*.jar|lib/websocket/*.jar");
        jetty8x9xDependencies.add("lib/annotations/*.jar");
        jetty8x9xDependencies.add("lib/jsp/*.jar|lib/apache-jsp/*.jar");

        List<String> tomcat5xDependencies = new ArrayList<String>();
        tomcat5xDependencies.add("bin/*.jar");
        tomcat5xDependencies.add("common/lib/*.jar");
        tomcat5xDependencies.add("server/lib/*.jar");

        List<String> tomcat6x7x8x9xDependencies = new ArrayList<String>();
        tomcat6x7x8x9xDependencies.add("bin/*.jar");
        tomcat6x7x8x9xDependencies.add("lib/*.jar");

        DEPENDENCIES.put("jetty4x", jetty4xDependencies);
        DEPENDENCIES.put("jetty5x", jetty5xDependencies);
        DEPENDENCIES.put("jetty6x", jetty6xDependencies);
        DEPENDENCIES.put("jetty7x", jetty7xDependencies);
        DEPENDENCIES.put("jetty8x", jetty8x9xDependencies);
        DEPENDENCIES.put("jetty9x", jetty8x9xDependencies);
        DEPENDENCIES.put("tomcat5x", tomcat5xDependencies);
        DEPENDENCIES.put("tomcat6x", tomcat6x7x8x9xDependencies);
        DEPENDENCIES.put("tomcat7x", tomcat6x7x8x9xDependencies);
        DEPENDENCIES.put("tomcat8x", tomcat6x7x8x9xDependencies);
        DEPENDENCIES.put("tomcat9x", tomcat6x7x8x9xDependencies);
    }

    /**
     * Resolve dependencies for an embedded container and create classpath.
     * @param containerId Container identifier.
     * @param containerHome Container home.
     * @return {@link ClassLoader} with dependencies, <code>null</code> if the container is not
     * supported in the embedded mode.
     * @throws FileNotFoundException If some dependencies cannot be found.
     */
    public ClassLoader resolveDependencies(String containerId, String containerHome)
        throws FileNotFoundException
    {
        List<String> dependencies = DEPENDENCIES.get(containerId);
        if (dependencies == null)
        {
            return null;
        }

        ClassLoader classloader = null;

        try
        {
            List<URL> urls = new ArrayList<URL>();

            // Until JDK version 7, we need to have Xerces in the classpath
            if (JdkUtils.getMajorJavaVersion() < 7)
            {
                String xerces = System.getProperty("cargo.testdata.xerces-jars");
                if (xerces == null)
                {
                    throw new IllegalArgumentException("cargo.testdata.xerces-jars not defined");
                }
                File[] xercesJARs = new File(xerces).listFiles();
                if (xercesJARs == null)
                {
                    throw new FileNotFoundException("Directory not found: " + xerces);
                }
                for (File xercesJAR : xercesJARs)
                {
                    urls.add(xercesJAR.toURI().toURL());
                }
            }

            // Jetty 9.3.x and 9.4.x have a WebSocket implementation that needs CDI
            if ("jetty9x".equals(containerId))
            {
                String cdi = System.getProperty("cargo.testdata.cdi-jars");
                if (cdi == null)
                {
                    throw new IllegalArgumentException("cargo.testdata.cdi-jars not defined");
                }
                File[] cdiJARs = new File(cdi).listFiles();
                if (cdiJARs == null)
                {
                    throw new FileNotFoundException("Directory not found: " + cdi);
                }
                for (File cdiJAR : cdiJARs)
                {
                    urls.add(cdiJAR.toURI().toURL());
                }
            }

            for (String dependencyRelativePath : dependencies)
            {
                if (dependencyRelativePath.endsWith("*.jar"))
                {
                    // JAR folder - Add all JARs in this directory
                    boolean found = false;
                    String folders = "";
                    String[] dependencyRelativeSubPaths = dependencyRelativePath.split("\\|");
                    for (String dependencyRelativeSubPath : dependencyRelativeSubPaths)
                    {
                        if (!dependencyRelativeSubPath.endsWith("*.jar"))
                        {
                            throw new IllegalArgumentException("Dependency paths with "
                                + "alternatives can only be used for many folder / folder "
                                    + "alternatives; i.e. all need to end with *.jar");
                        }
                        File folder = new File(containerHome, dependencyRelativeSubPath
                            .substring(0, dependencyRelativeSubPath.length() - 5));
                        File[] jars = folder.listFiles(new FilenameFilter()
                        {
                            @Override
                            public boolean accept(File dir, String name)
                            {
                                return name.endsWith(".jar");
                            }
                        });
                        if (jars != null)
                        {
                            found = true;
                            for (File jar : jars)
                            {
                                urls.add(jar.toURI().toURL());
                            }
                        }
                        if (!folders.isEmpty())
                        {
                            folders += ", ";
                        }
                        folders += folder.toString();
                    }
                    if (!found)
                    {
                        throw new FileNotFoundException("No files matched in folders: " + folders);
                    }
                }
                else
                {
                    // Single JAR file or directory
                    File dependencyPath = new File(containerHome, dependencyRelativePath);
                    if (!dependencyPath.isFile())
                    {
                        throw new FileNotFoundException(dependencyPath.toString());
                    }
                    urls.add(dependencyPath.toURI().toURL());
                }
            }

            // On OSX, the tools.jar classes are included in the classes.jar so there is no need to
            // include any tools.jar file to the cp. On Java 9, there is no more tools.jar.
            if (!JdkUtils.isOSX() && JdkUtils.getMajorJavaVersion() < 9)
            {
                urls.add(JdkUtils.getToolsJar().toURI().toURL());
            }

            // We pass the GSSException as the parent to ensure no other JARs are in the classpath,
            // and still allow Java 9 to properly function
            classloader = new URLClassLoader(urls.toArray(new URL[urls.size()]),
                org.ietf.jgss.GSSException.class.getClassLoader());
        }
        catch (MalformedURLException e)
        {
            throw new CargoException("Failed to resolve dependency", e);
        }

        return classloader;
    }
}
