/* 
 * ========================================================================
 * 
 * Copyright 2007-2008 OW2.
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
package org.codehaus.cargo.container.jonas.internal;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.codehaus.cargo.container.configuration.RuntimeConfiguration;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.RemotePropertySet;

/**
 * JMX remoting (JSR 160) implementation to get a remote MBeanServerConnection.
 * 
 * @version $Id$
 */
public class JSR160MBeanServerConnectionFactory implements MBeanServerConnectionFactory
{
    /**
     * The default JMX remote URL to use with JOnAS server.
     */
    private static final String DEFAULT_URI =
        "service:jmx:rmi://localhost/jndi/rmi://localhost:1099/jrmpconnector_jonas";

    /**
     * JMX Connector to use with JOnAS server.
     */
    private JMXConnector connector;

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.jonas.internal.MBeanServerConnectionFactory#getServerConnection()
     */
    public MBeanServerConnection getServerConnection(RuntimeConfiguration configuration)
        throws IOException
    {
        String username = configuration.getPropertyValue(RemotePropertySet.USERNAME);
        String password = configuration.getPropertyValue(RemotePropertySet.PASSWORD);
        String jmxRemoteURL = configuration.getPropertyValue(RemotePropertySet.URI);

        if (jmxRemoteURL == null || jmxRemoteURL.trim().length() == 0)
        {
            jmxRemoteURL = DEFAULT_URI;

            String port = configuration.getPropertyValue(GeneralPropertySet.RMI_PORT);
            if (port != null)
            {
                jmxRemoteURL = jmxRemoteURL.replace("1099", port);
            }

            String hostname = configuration.getPropertyValue(GeneralPropertySet.HOSTNAME);
            if (hostname != null)
            {
                jmxRemoteURL = jmxRemoteURL.replace("localhost", hostname);
            }
        }

        Map environment = new HashMap();

        if (username != null && username.trim().length() > 0 && password != null
            && password.trim().length() > 0)
        {
            Object credentials = new Object[]
            {
                username, password
            };
            environment.put(JMXConnector.CREDENTIALS, credentials);
        }

        if (!environment.containsKey(JMXConnectorFactory.PROTOCOL_PROVIDER_CLASS_LOADER))
        {
            environment.put(JMXConnectorFactory.PROTOCOL_PROVIDER_CLASS_LOADER, this.getClass()
                .getClassLoader());
        }

        JMXServiceURL address = new JMXServiceURL(jmxRemoteURL);
        connector = JMXConnectorFactory.connect(address, environment);

        MBeanServerConnection mbsc = connector.getMBeanServerConnection();

        return mbsc;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.jonas.internal.MBeanServerConnectionFactory#destroy()
     */
    public void destroy()
    {
        if (connector != null)
        {
            try
            {
                connector.close();
            }
            catch (IOException e)
            {
                e.getMessage();
            }
            connector = null;
        }

    }
}
