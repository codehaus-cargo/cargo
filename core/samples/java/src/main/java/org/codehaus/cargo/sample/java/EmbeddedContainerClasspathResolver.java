/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
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

        List<String> jetty8x9x10x11xDependencies = new ArrayList<String>();
        jetty8x9x10x11xDependencies.add(
            "lib/*.jar|lib/logging/*.jar|lib/jndi/*.jar|lib/websocket/*.jar");
        jetty8x9x10x11xDependencies.add("lib/annotations/*.jar");
        jetty8x9x10x11xDependencies.add("lib/jsp/*.jar|lib/apache-jsp/*.jar");

        List<String> jetty12xDependencies = new ArrayList<String>();
        // logging-slf4j
        jetty12xDependencies.add("lib/logging/slf4j-api-*.jar");
        // logging-jetty
        jetty12xDependencies.add("lib/logging/jetty-slf4j-impl-*.jar");
        // server
        jetty12xDependencies.add("lib/jetty-http-*.jar");
        jetty12xDependencies.add("lib/jetty-server-*.jar");
        jetty12xDependencies.add("lib/jetty-xml-*.jar");
        jetty12xDependencies.add("lib/jetty-util-*.jar");
        jetty12xDependencies.add("lib/jetty-io-*.jar");
        // sessions
        jetty12xDependencies.add("lib/jetty-session-*.jar");
        // ee10-servlet
        jetty12xDependencies.add("lib/jakarta.servlet-api-6.*.jar");
        jetty12xDependencies.add("lib/jetty-ee10-servlet-*.jar");
        // jndi
        jetty12xDependencies.add("lib/jetty-jndi-*.jar");
        // plus
        jetty12xDependencies.add("lib/jetty-plus-*.jar");
        // security
        jetty12xDependencies.add("lib/jetty-security-*.jar");
        // ee-webapp
        jetty12xDependencies.add("lib/jetty-ee-*.jar");
        // ee10-webapp
        jetty12xDependencies.add("lib/jetty-ee10-webapp-*.jar");
        // ee10-plus
        jetty12xDependencies.add("lib/jetty-ee10-plus-*.jar");
        jetty12xDependencies.add("lib/jakarta.transaction-api-2.*.jar");
        jetty12xDependencies.add("lib/jakarta.interceptor-api-2.*.jar");
        jetty12xDependencies.add("lib/jakarta.enterprise.cdi-api-4.*.jar");
        jetty12xDependencies.add("lib/jakarta.inject-api-2.*.jar");
        jetty12xDependencies.add("lib/jakarta.enterprise.lang-model-4.*.jar");
        // ee10-annotations
        jetty12xDependencies.add("lib/jetty-ee10-annotations-*.jar");
        jetty12xDependencies.add("lib/ee10-annotations/*.jar");
        // ee10-apache-jsp
        jetty12xDependencies.add("lib/jetty-ee10-apache-jsp-*.jar");
        jetty12xDependencies.add("lib/ee10-apache-jsp/*.jar");

        List<String> tomcat5xDependencies = new ArrayList<String>();
        tomcat5xDependencies.add("bin/*.jar");
        tomcat5xDependencies.add("common/lib/*.jar");
        tomcat5xDependencies.add("server/lib/*.jar");

        List<String> tomcat6x7x8x9x10x11xDependencies = new ArrayList<String>();
        tomcat6x7x8x9x10x11xDependencies.add("bin/*.jar");
        tomcat6x7x8x9x10x11xDependencies.add("lib/*.jar");

        DEPENDENCIES.put("jetty5x", jetty5xDependencies);
        DEPENDENCIES.put("jetty6x", jetty6xDependencies);
        DEPENDENCIES.put("jetty7x", jetty7xDependencies);
        DEPENDENCIES.put("jetty8x", jetty8x9x10x11xDependencies);
        DEPENDENCIES.put("jetty9x", jetty8x9x10x11xDependencies);
        DEPENDENCIES.put("jetty10x", jetty8x9x10x11xDependencies);
        DEPENDENCIES.put("jetty11x", jetty8x9x10x11xDependencies);
        DEPENDENCIES.put("jetty12x", jetty12xDependencies);
        DEPENDENCIES.put("tomcat5x", tomcat5xDependencies);
        DEPENDENCIES.put("tomcat6x", tomcat6x7x8x9x10x11xDependencies);
        DEPENDENCIES.put("tomcat7x", tomcat6x7x8x9x10x11xDependencies);
        DEPENDENCIES.put("tomcat8x", tomcat6x7x8x9x10x11xDependencies);
        DEPENDENCIES.put("tomcat9x", tomcat6x7x8x9x10x11xDependencies);
        DEPENDENCIES.put("tomcat10x", tomcat6x7x8x9x10x11xDependencies);
        DEPENDENCIES.put("tomcat11x", tomcat6x7x8x9x10x11xDependencies);
    }

    /**
     * Get the dependencies list for a given container.
     * @param containerId Container identifier.
     * @return Dependencies list.
     */
    protected List<String> getDependencies(String containerId)
    {
        return DEPENDENCIES.get(containerId);
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
        List<String> dependencies = getDependencies(containerId);
        if (dependencies == null)
        {
            return null;
        }

        ClassLoader classloader = null;

        try
        {
            List<URL> urls = new ArrayList<URL>();

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
                            .substring(0, dependencyRelativeSubPath.lastIndexOf("/")));
                        File[] jars =
                            folder.listFiles((File dir, String name) -> name.endsWith(".jar"));
                        if (jars != null)
                        {
                            for (File jar : jars)
                            {
                                // To avoid issues caused by the behaviour described in
                                // https://github.com/eclipse/jetty.project/issues/4746, do not
                                // include the Jetty JASPI JARs in the embedded container classpath
                                if (!jar.getName().startsWith("demo-")
                                    && !jar.getName().startsWith("jetty-jaspi-"))
                                {
                                    String dependencyEndPath =
                                        dependencyRelativeSubPath.substring(
                                            dependencyRelativeSubPath.lastIndexOf("/") + 1);
                                    if (!"*.jar".equals(dependencyEndPath))
                                    {
                                        dependencyEndPath = dependencyEndPath.substring(
                                            0, dependencyEndPath.length() - 5);
                                        if (!jar.getName().startsWith(dependencyEndPath))
                                        {
                                            continue;
                                        }
                                    }

                                    urls.add(jar.toURI().toURL());
                                    found = true;
                                }
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
                        throw new FileNotFoundException(
                            "No files matching [" + dependencyRelativePath
                                + "] in folders: " + folders);
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
