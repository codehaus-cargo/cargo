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
package org.codehaus.cargo.container.geronimo.internal;

import java.io.File;
import java.io.FileFilter;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Various utility methods such as checking is Geronimo is started.
 *
 * @version $Id$
 */
public class GeronimoUtils
{
    /**
     * @param geronimoHomeDir the geronimo home dir location
     * @return a URL classloader containing all JARs located in <code>GERONIMO_HOME/lib</code>.
     */
    public ClassLoader createGeronimoURLClassloader(File geronimoHomeDir)
    {
        File libDir = new File(geronimoHomeDir, "lib");
        File[] files = libDir.listFiles(new FileFilter()
        {
            public boolean accept(File pathname)
            {
                return pathname.isFile() && pathname.getPath().endsWith(".jar");
            }
        });
        URL[] urls = new URL[files.length];
        for (int i = 0; i < files.length; i++)
        {
            try
            {
                urls[i] = files[i].toURI().toURL();
            }
            catch (MalformedURLException e)
            {
                // This shouldn't happen...
            }
        }
        return new URLClassLoader(urls);
    }

    /**
     * @param host the host where Geronimo is executing
     * @param rmiPort the RMI port to use to connect to the executing Geronimo server
     * @param username the username to authenticate against the executing Geronimo Server
     * @param password the password to authenticate against the executing Geronimo Server
     * @return true if Geronimo is fully started or false otherwise
     */
    public boolean isGeronimoStarted(String host, String rmiPort, String username, String password)
    {
        boolean isStarted = false;

        try
        {
            isStarted = isKernelFullyStarted(host, rmiPort, username, password);
        }
        catch (Exception e)
        {
            // Failed to check if container is started. We assume it's not started
        }

        return isStarted;
    }

    /**
     * @param host the host where Geronimo is executing
     * @param rmiPort the RMI port to use to connect to the executing Geronimo server
     * @param username the username to authenticate against the executing Geronimo Server
     * @param password the password to authenticate against the executing Geronimo Server
     * @return true if Geronimo is fully started or false otherwise
     * @throws Exception if an error occurred while checking the container's state
     */
    private boolean isKernelFullyStarted(String host, String rmiPort, String username,
        String password) throws Exception
    {
        Object kernel = getKernel(host, rmiPort, username, password);

        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        if (isKernelAlive(kernel))
        {
            Class gbeanQueryClass = cl.loadClass("org.apache.geronimo.gbean.GBeanQuery");
            Object gbeanQuery = gbeanQueryClass.getConstructor(new Class[] {String.class,
                String.class}).newInstance(new Object[] {null, cl.loadClass(
                    "org.apache.geronimo.kernel.config.PersistentConfigurationList").getName()});

            Set configLists = (Set) kernel.getClass().getMethod("listGBeans",
                new Class[] {gbeanQueryClass}).invoke(kernel, new Object[] {gbeanQuery});

            if (!configLists.isEmpty())
            {
                Object on = configLists.toArray()[0];

                Boolean b = (Boolean) kernel.getClass().getMethod("getAttribute",
                    new Class[] {cl.loadClass("javax.management.ObjectName"),
                        String.class}).invoke(kernel, new Object[] {on, "kernelFullyStarted"});

                return b.booleanValue();
            }
        }

        return false;
    }

    /**
     * @param kernel the Geronimo kernel object
     * @return true if the kernel is running or false otherwise
     * @throws Exception in case of error
     */
    private boolean isKernelAlive(Object kernel) throws Exception
    {
        Boolean running = (Boolean) kernel.getClass().getMethod("isRunning", null).invoke(kernel,
            null);
        return running.booleanValue();
    }

    /**
     * @param host the host where Geronimo is executing
     * @param rmiPort the RMI port to use to connect to the executing Geronimo server
     * @param username the username to authenticate against the executing Geronimo Server
     * @param password the password to authenticate against the executing Geronimo Server
     * @return the running Geronimo kernel
     * @throws Exception in case of error
     */
    private Object getKernel(String host, String rmiPort, String username, String password)
        throws Exception
    {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        String jmxServiceURL = "service:jmx:rmi://" + host + "/jndi/rmi://" + host + ":" + rmiPort
            + "/JMXConnector";

        Class jmxServiceURLClass = cl.loadClass("javax.management.remote.JMXServiceURL");
        Object address = jmxServiceURLClass.getConstructor(new Class[] {String.class})
            .newInstance(new Object[] {jmxServiceURL});

        Map map = new HashMap();
        map.put("jmx.remote.credentials", new String[] {username, password});

        Object jmxConnector = cl.loadClass("javax.management.remote.JMXConnectorFactory")
            .getMethod("connect", new Class[] {jmxServiceURLClass, Map.class})
                .invoke(null, new Object[] {address, map});

        Object mbServerConnection = jmxConnector.getClass().getMethod("getMBeanServerConnection",
            null).invoke(jmxConnector, null);

        return cl.loadClass("org.apache.geronimo.kernel.jmx.KernelDelegate")
            .getConstructor(new Class[] {cl.loadClass("javax.management.MBeanServerConnection")})
                .newInstance(new Object[] {mbServerConnection});
    }
}
