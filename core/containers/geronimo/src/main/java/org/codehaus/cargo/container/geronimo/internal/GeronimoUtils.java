/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.RemotePropertySet;

/**
 * Various utility methods such as checking is Geronimo is started.
 *
 * @version $Id$
 */
public class GeronimoUtils
{
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
            isStarted = isKernelFullyStarted(host, rmiPort, username, password);
        }
        catch (IOException e)
        {
            // Connection error, assume container not started
        }
        catch (SecurityException e)
        {
            // Security error, happens when container is starting
        }
        catch (Exception e)
        {
            throw new ContainerException("Internal error in the Geronimo container", e);
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
        JMXServiceURL jmxServiceURL = new JMXServiceURL("service:jmx:rmi://" + host
            + "/jndi/rmi://" + host + ":" + rmiPort + "/JMXConnector");

        Map map = new HashMap();
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
                try
                {
                    Boolean kernelFullyStarted = (Boolean)
                        mbServerConnection.getAttribute(attributeManager, "kernelFullyStarted");

                    if (!kernelFullyStarted)
                    {
                        result = false;
                        break;
                    }
                }
                catch (AttributeNotFoundException ignored)
                {
                    // Ignored
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
