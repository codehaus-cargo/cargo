/*
 * ========================================================================
 *
 * Copyright 2006 Vincent Massol.
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

import org.codehaus.cargo.util.CargoException;
import org.codehaus.cargo.container.internal.util.JdkUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.net.URLClassLoader;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Create a classloader to load container classes.
 *
 * @version $Id$
 */
public class EmbeddedContainerClasspathResolver
{
    private static final Map dependencies = new HashMap();

    static
    {
        List jetty4xDependencies = new ArrayList();
        jetty4xDependencies.add("lib/*.jar");
        jetty4xDependencies.add("ext/*.jar");

        List jetty5xDependencies = new ArrayList();
        jetty5xDependencies.add("lib/*.jar");
        jetty5xDependencies.add("ext/*.jar");

        List jetty6xDependencies = new ArrayList();
        jetty6xDependencies.add("lib/*.jar");
        jetty6xDependencies.add("lib/jsp-2.0/*.jar");
        jetty6xDependencies.add("lib/management/*.jar");
        jetty6xDependencies.add("lib/naming/*.jar");
        jetty6xDependencies.add("lib/plus/*.jar");
        jetty6xDependencies.add("lib/xbean/*.jar");

        List tomcat5xDependencies = new ArrayList();
        tomcat5xDependencies.add("bin/*.jar");
        tomcat5xDependencies.add("common/lib/*.jar");
        tomcat5xDependencies.add("server/lib/*.jar");

        dependencies.put("jetty4x", jetty4xDependencies);
        dependencies.put("jetty5x", jetty5xDependencies);
        dependencies.put("jetty6x", jetty6xDependencies);
        dependencies.put("tomcat5x", tomcat5xDependencies);

    }

    private JdkUtils jdkUtils = new JdkUtils();

    /**
     * @return null if the container is not supported in the embedded mode.
     */
    public ClassLoader resolveDependencies(String containerId, String containerHome)
        throws FileNotFoundException
    {
        List depList = (List) dependencies.get(containerId);
        if (depList == null)
        {
            return null;
        }

        ClassLoader classloader;

        try
        {
            List urls = new ArrayList();
            Iterator it = depList.iterator();
            while (it.hasNext())
            {
                String dependencyRelativePath = (String) it.next();
                if (dependencyRelativePath.endsWith("*.jar")) {
                    // jar folder. add all jars in this directory
                    File folder = new File(containerHome, dependencyRelativePath).getParentFile();
                    File[] jars = folder.listFiles(new FilenameFilter()
                        {
                            public boolean accept(File dir, String name)
                            {
                                return name.endsWith(".jar");
                            }
                        });
                    for (int i = 0; i < jars.length; i++)
                    {
                        urls.add(jars[i].toURL());
                    }
                }
                else
                {
                    // single jar file or directory
                    File dependencyPath = new File(containerHome, dependencyRelativePath);
                    urls.add(dependencyPath.toURL());
                }
            }

            // On OSX, the tools.jar classes are included in the classes.jar so there is no need to
            // include any tools.jar file to the cp.
            if (!this.jdkUtils.isOSX())
            {
                urls.add(this.jdkUtils.getToolsJar().toURL());
            }

            classloader = null;

            if (containerId.equals("tomcat5x"))
            {
                /*
                    Here is the problem this code is trying to solve.

                    When this is run inside forked JUnit, the system classloader
                    contains a bunch of classes, and that includes servlet API.
                    These classes are made available by the <junit> ant task
                    (which is what Maven used behind the scene), and we can't
                    do anything about it.

                    Now, the web application classloader that Tomcat uses has
                    a non-standard delegation order; it tries to load classes
                    from war before it delegates to ancestors. To avoid loading
                    JavaSE classes from war (the servlet spec describes some of those),
                    this classloader checks the system classloader.

                    So when we load Tomcat 5.x classes (URLClassLoader below), this
                    classloader nees to delegate to the system class loader, so that
                    both tomcat implementation and web application loads the servlet API
                    from the same place. Ohtherwise you get ClassCastException.
                    So that's why we set "getClass().getClassLoader()" as the parent.

                    However, that causes another problem in a separate place. Now, with
                    this change, Tomcat will load Ant from the system classloader
                    (because that's another jar that <junit> puts into the classpath),
                    but the system classloader doesn't have tools.jar. So <javac> fails,
                    and this breaks jasper, which compiles JSP files through <javac>
                    Ant task.

                    To avoid this problem, we want to load Ant from the classloader
                    we create below, which will also contain tools.jar. To do this,
                    we insert another classloader and cut the delegation chain for
                    org.apache.tools.ant.
                */
                classloader = new URLClassLoader(new URL[0], getClass().getClassLoader())
                {
                    protected synchronized Class loadClass(String name, boolean resolve)
                        throws ClassNotFoundException
                    {
                        if (name.startsWith("org.apache.tools.ant"))
                        {
                            throw new ClassNotFoundException();
                        }
                        return super.loadClass(name, resolve);
                    }
                };
            }

            // We pass null as the parent to ensure no jars outside of the ones we've added are
            // added to the classpath.
            classloader = new URLClassLoader((URL[])urls.toArray(new URL[0]), classloader);
        }
        catch (MalformedURLException e)
        {
            throw new CargoException("Failed to resolve dependency", e);
        }

        return classloader;
    }
}
