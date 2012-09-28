/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol.
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.util.log.Logger;

/**
 * Various utility methods such as checking is Geronimo is started.
 * 
 * @version $Id$
 */
public class GeronimoUtils
{
    /**
     * Logger.
     */
    private Logger logger;

    /**
     * Host name.
     */
    private String host;

    /**
     * RMI port.
     */
    private String rmiPort;

    /**
     * User name.
     */
    private String username;

    /**
     * Password.
     */
    private String password;

    /**
     * @param configuration Configuration to use.
     */
    public GeronimoUtils(Configuration configuration)
    {
        logger = configuration.getLogger();
        host = configuration.getPropertyValue(GeneralPropertySet.HOSTNAME);
        rmiPort = configuration.getPropertyValue(GeneralPropertySet.RMI_PORT);
        username = configuration.getPropertyValue(RemotePropertySet.USERNAME);
        password = configuration.getPropertyValue(RemotePropertySet.PASSWORD);
    }

    /**
     * @return true if Geronimo is fully started or false otherwise
     */
    public boolean isGeronimoStarted()
    {
        boolean isStarted = false;

        try
        {
            isStarted = isKernelFullyStarted();
        }
        catch (IOException e)
        {
            // Connection error, assume container not started
        }
        catch (SecurityException e)
        {
            // Security error, happens when container is starting
        }
        catch (InstanceNotFoundException e)
        {
            // happens when container is stopping
        }
        catch (Exception e)
        {
            throw new ContainerException("Internal error in the Geronimo container", e);
        }

        return isStarted;
    }

    /**
     * @param jarName name of the bundle JAR file
     * @return Bundle identifier
     * @throws Exception if an error occurred while checking the container's state
     */
    public long getBundleId(String jarName) throws Exception
    {
        logger.debug("Looking for bundle " + jarName, this.getClass().getName());

        String base;
        String version;
        if (jarName.indexOf('-') != -1)
        {
            String jarNameWithoutSnapshot = jarName.replace("-SNAPSHOT", "");
            base = jarName.substring(0, jarNameWithoutSnapshot.lastIndexOf('-'));
            version = jarName.substring(jarNameWithoutSnapshot.lastIndexOf('-') + 1);
            version = version.replace('-', '.');
        }
        else
        {
            base = "";
            version = jarName;
        }
        if (version.indexOf(".jar") != -1)
        {
            version = version.substring(0, version.lastIndexOf('.'));
        }
        long bundleId = 0;

        JMXServiceURL jmxServiceURL = new JMXServiceURL("service:jmx:rmi://" + host
            + "/jndi/rmi://" + host + ":" + rmiPort + "/JMXConnector");

        Map<String, Object> map = new HashMap<String, Object>();
        map.put(JMXConnector.CREDENTIALS, new String[] {username, password});

        JMXConnector connector = JMXConnectorFactory.connect(jmxServiceURL, map);
        try
        {
            MBeanServerConnection mbServerConnection = connector.getMBeanServerConnection();

            Set<ObjectName> bundleState = mbServerConnection.queryNames(
                new ObjectName("*:type=bundleState,*"), null);
            if (bundleState == null || bundleState.isEmpty())
            {
                throw new ContainerException("Cannot find bundle state MBean");
            }

            String[] parameterTypes = new String[]{"long"};
            for (ObjectName bs : bundleState)
            {
                try
                {
                    long testedBundleId = 0;
                    while (true)
                    {
                        testedBundleId++;
                        String location = (String) mbServerConnection.invoke(
                            bs, "getLocation", new Object[]{testedBundleId}, parameterTypes);
                        logger.debug("\tChecking bundle " + location + ", ID " + testedBundleId,
                            this.getClass().getName());
                        if (location.contains(base) && location.contains(version))
                        {
                            bundleId = testedBundleId;
                            break;
                        }
                    }
                }
                catch (MBeanException end)
                {
                    // Reached the end of available bundle IDs
                }
            }
        }
        finally
        {
            try
            {
                connector.close();
            }
            catch (IOException ignored)
            {
                // Ignored
            }

            connector = null;
            System.gc();
        }

        logger.debug("Returning bundle ID " + bundleId, this.getClass().getName());

        if (bundleId == 0)
        {
            throw new ContainerException("Cannot find bundle " + jarName);
        }
        return bundleId;
    }

    /**
     * @return true if Geronimo is fully started or false otherwise
     * @throws Exception if an error occurred while checking the container's state
     */
    private boolean isKernelFullyStarted() throws Exception
    {
        JMXServiceURL jmxServiceURL = new JMXServiceURL("service:jmx:rmi://" + host
            + "/jndi/rmi://" + host + ":" + rmiPort + "/JMXConnector");

        Map<String, Object> map = new HashMap<String, Object>();
        map.put(JMXConnector.CREDENTIALS, new String[] {username, password});

        JMXConnector connector = JMXConnectorFactory.connect(jmxServiceURL, map);
        try
        {
            MBeanServerConnection mbServerConnection = connector.getMBeanServerConnection();

            Set<ObjectName> attributeManagers = mbServerConnection.queryNames(
                new ObjectName("*:name=AttributeManager,*"), null);
            if (attributeManagers == null || attributeManagers.isEmpty())
            {
                return false;
            }

            boolean result = true;
            for (ObjectName attributeManager : attributeManagers)
            {
                Boolean kernelFullyStarted = (Boolean)
                    mbServerConnection.getAttribute(attributeManager, "kernelFullyStarted");

                if (!kernelFullyStarted)
                {
                    result = false;
                    break;
                }
            }
            return result;
        }
        finally
        {
            try
            {
                connector.close();
            }
            catch (IOException ignored)
            {
                // Ignored
            }

            connector = null;
            System.gc();
        }
    }

    /**
     * @param kernel the Geronimo kernel object
     * @return true if the kernel is running or false otherwise
     * @throws Exception in case of error
     */
    private boolean isKernelAlive(Object kernel) throws Exception
    {
        Boolean running = (Boolean) kernel.getClass().getMethod("isRunning", (Class<?>[]) null).
            invoke(kernel, (Object[]) null);
        return running.booleanValue();
    }
}
