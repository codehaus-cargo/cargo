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
package org.codehaus.cargo.sample.java.jboss;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.NamingException;

import org.codehaus.cargo.container.jboss.JBossPropertySet;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.sample.java.AbstractCargoTestCase;
import org.codehaus.cargo.sample.java.EnvironmentTestData;

/**
 * Abstract test case for JBoss capabilities.
 * 
 * @version $Id$
 */
public abstract class AbstractJBossCapabilityTestCase extends AbstractCargoTestCase
{
    /**
     * Initializes the test case.
     * @param testName Test name.
     * @param testData Test environment data.
     * @throws Exception If anything goes wrong.
     */
    public AbstractJBossCapabilityTestCase(String testName, EnvironmentTestData testData)
        throws Exception
    {
        super(testName, testData);
    }

    /**
     * Returns an <code>MBeanServerConnection</code> to use to query the JBoss container via jmx.
     * <b>Important: This only works with JBoss 5+!</b>
     * 
     * @param username the username to use for authentication, or <code>null</code> for no
     *            authentication
     * @param password the password to use for authentication, or <code>null</code> for no
     *            authentication
     * @return a <code>MBeanServerConnection</code>
     * @throws NamingException If a naming exception occurs.
     * @throws IOException If cannot connect to JBoss server.
     */
    protected MBeanServerConnection createMBeanServerConnection(String username, String password)
        throws NamingException, IOException
    {
        Map<String, Object> env = new HashMap<String, Object>();
        if (username != null && password != null)
        {
            String[] credentials = new String[] {username, password};
            env.put(JMXConnector.CREDENTIALS, credentials);
        }
        String serviceUrl = getServiceUrl();
        JMXServiceURL url = new JMXServiceURL(serviceUrl);

        getLogger().debug("Creating MBeanServerConnection for service URL '" + serviceUrl + "'",
            this.getClass().getName());
        JMXConnector jmxc = JMXConnectorFactory.connect(url, env);
        MBeanServerConnection srvCon = jmxc.getMBeanServerConnection();
        getLogger().debug("MBeanServerConnection created", this.getClass().getName());

        return srvCon;
    }

    /**
     * @return JBoss service URL.
     */
    private String getServiceUrl()
    {
        String hostname =
            getLocalContainer().getConfiguration().getPropertyValue(GeneralPropertySet.HOSTNAME);
        String port =
            getLocalContainer().getConfiguration().getPropertyValue(
                JBossPropertySet.JBOSS_JRMP_PORT);
        String containerId = this.getContainer().getId();
        String objectName;
        if (Pattern.matches("^jboss[5].*", containerId))
        {
            // object name is "jmxconnector" for JBoss 5.x
            objectName = "jmxconnector";
        }
        else if (Pattern.matches("^jboss[6-9].*", containerId))
        {
            // object name is "jmxrmi" starting with JBoss 6.0.0M3
            objectName = "jmxrmi";
        }
        else
        {
            throw new UnsupportedOperationException("Method not supported for the current "
                + "container: " + containerId);
        }

        String serviceUrl =
            "service:jmx:rmi:///jndi/rmi://" + hostname + ":" + port + "/" + objectName;
        return serviceUrl;
    }
}
