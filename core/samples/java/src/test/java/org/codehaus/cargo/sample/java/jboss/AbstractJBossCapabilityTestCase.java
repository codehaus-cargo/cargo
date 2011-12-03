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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.codehaus.cargo.container.jboss.JBossPropertySet;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.sample.java.AbstractCargoTestCase;
import org.codehaus.cargo.sample.java.EnvironmentTestData;
import org.codehaus.cargo.util.CargoException;

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
     * Perform a JNDI lookup on JBoss. <b>Important</b>: This method will change the Thread's
     * ContextClassLoader, else the <code>create</code> method for EJB2's will fail on JBoss 5+
     * with <code>ClassNotFoundException: org.jboss.security.plugins.JBossSecurityContext</code>.
     * @param <T> Class of the JNDI object looked for.
     * @param name Name in the JNDI directory.
     * @return Reference to the remote object.
     * @throws NamingException If cannot connect to the JNDI or <code>name</code> cannot be found
     * in the JNDI directory.
     */
    protected <T> T jndiLookup(String name) throws NamingException
    {
        try
        {
            String port = getLocalContainer().getConfiguration().getPropertyValue(
                GeneralPropertySet.RMI_PORT);

            // In order to do JNDI lookups on JBoss, the Java Naming Context requires stub classes
            // from JBoss; and one of the place in which it looks for these is the Thread's
            // ContextClassLoader. We therefore need to include the JBoss client JAR in there.
            URL[] urls;
            if (getContainer().getId().startsWith("jboss7"))
            {
                List<File> files = new ArrayList<File>();
                addAllJars(new File(getInstalledLocalContainer().getHome(), "modules"), files);
                urls = new URL[files.size()];
                for (int i = 0; i < files.size(); i++)
                {
                    urls[i] = files.get(i).toURI().toURL();
                }
            }
            else
            {
                File allClientJar =
                    new File(getInstalledLocalContainer().getHome(), "client/jbossall-client.jar");
                if (!allClientJar.isFile())
                {
                    throw new IllegalStateException("Cannot find " + allClientJar);
                }
                urls = new URL[] {allClientJar.toURI().toURL()};
            }
            URLClassLoader classloader = new URLClassLoader(urls, getClass().getClassLoader());
            Thread.currentThread().setContextClassLoader(classloader);

            Properties props = new Properties();
            if (getContainer().getId().startsWith("jboss7"))
            {
                props.setProperty(
                    Context.INITIAL_CONTEXT_FACTORY, "org.jboss.as.naming.InitialContextFactory");
                props.setProperty(Context.URL_PKG_PREFIXES, "org.jboss.as.naming.interfaces");
            }
            else
            {
                props.setProperty(
                    Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
                props.setProperty(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
            }
            props.setProperty(Context.PROVIDER_URL, "jnp://localhost:" + port);
            Context jndi = new InitialContext(props);
            try
            {
                return (T) jndi.lookup(name);
            }
            catch (NameNotFoundException e)
            {
                StringBuilder sb = new StringBuilder("Cannot find " + name + ". Found names:");
                NamingEnumeration<Binding> list = jndi.listBindings("");
                while (list.hasMore())
                {
                    sb.append("\n\t- " + list.next().getName());
                }
                throw new CargoException(sb.toString(), e);
            }
        }
        catch (MalformedURLException e)
        {
            throw new IllegalStateException("Failed creating JBoss classpath", e);
        }
    }

    /**
     * Returns an <code>MBeanServerConnection</code> to use to query the JBoss container via JMX.
     * @return a <code>MBeanServerConnection</code>
     * @throws NamingException If a naming exception occurs.
     * @throws IOException If cannot connect to JBoss server.
     */
    protected MBeanServerConnection createMBeanServerConnection() throws NamingException,
        IOException
    {
        final String containerId = this.getContainer().getId();

        if (containerId.startsWith("jboss4"))
        {
            // JNDI name is "jmx/invoker/RMIAdaptor" for JBoss 4.x
            final ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();
            try
            {
                return jndiLookup("jmx/invoker/RMIAdaptor");
            }
            finally
            {
                Thread.currentThread().setContextClassLoader(oldTCCL);
            }
        }
        else
        {
            String username = getLocalContainer().getConfiguration().getPropertyValue(
                RemotePropertySet.USERNAME);
            String password = getLocalContainer().getConfiguration().getPropertyValue(
                RemotePropertySet.PASSWORD);

            Map<String, Object> env = new HashMap<String, Object>();
            if (username != null && password != null)
            {
                String[] credentials = new String[] {username, password};
                env.put(JMXConnector.CREDENTIALS, credentials);
            }

            String jndiName;
            if (containerId.startsWith("jboss5"))
            {
                // JNDI name is "jmxconnector" for JBoss 5.x
                jndiName = "jmxconnector";
            }
            else if (containerId.startsWith("jboss6") || containerId.startsWith("jboss7"))
            {
                // JNDI name is "jmxrmi" starting with JBoss 6.0.0 M3
                jndiName = "jmxrmi";
            }
            else
            {
                throw new UnsupportedOperationException("Method not supported for the current "
                    + "container: " + containerId);
            }

            String port =
                getLocalContainer().getConfiguration().getPropertyValue(
                    JBossPropertySet.JBOSS_JRMP_PORT);

            String serviceUrl =
                "service:jmx:rmi:///jndi/rmi://localhost:" + port + "/" + jndiName;
            JMXServiceURL url = new JMXServiceURL(serviceUrl);

            getLogger().debug("Creating MBeanServerConnection for service URL '" + serviceUrl + "'",
                this.getClass().getName());
            MBeanServerConnection srvCon;
            JMXConnector jmxc = JMXConnectorFactory.connect(url, env);
            srvCon = jmxc.getMBeanServerConnection();
            getLogger().debug("MBeanServerConnection created", this.getClass().getName());
            return srvCon;
        }
    }

    /**
     * Add all JARs in a folder in the list of files (recursive).
     * @param folder Folder to recursively scan.
     * @param files List containing all files.
     */
    public static void addAllJars(File folder, List<File> files)
    {
        if (folder.isDirectory())
        {
            for (File file : folder.listFiles())
            {
                if (file.isFile() && file.getName().endsWith(".jar"))
                {
                    files.add(file);
                }
                else if (file.isDirectory())
                {
                    addAllJars(file, files);
                }
            }
        }
    }
}
