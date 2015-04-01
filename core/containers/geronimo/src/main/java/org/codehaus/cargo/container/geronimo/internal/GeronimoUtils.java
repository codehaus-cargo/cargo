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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.deployable.Bundle;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.util.log.Logger;

/**
 * Various utility methods such as checking is Geronimo is started.
 * 
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
     * Returns the extra classpath dependencies XML for the Geronimo installed local container.
     * @param container Geronimo installed local container.
     * @return Extra classpath dependencies XML for <code>container</code>.
     * @throws IOException If reading the JAR files in the extra classpath fail.
     */
    public static String getGeronimoExtraClasspathDependiesXML(
        InstalledLocalContainer container) throws IOException
    {
        StringBuilder sb = new StringBuilder();

        if (container.getExtraClasspath() != null
            && container.getExtraClasspath().length > 0)
        {
            sb.append("    <dep:dependencies>\n");
            for (String extraClasspathElement : container.getExtraClasspath())
            {
                File extraClasspathElementFile = new File(extraClasspathElement);
                JarFile jarFile = new JarFile(extraClasspathElementFile);

                extraClasspathElement = extraClasspathElementFile.getName();

                String extension = extraClasspathElement.substring(
                    extraClasspathElement.lastIndexOf('.') + 1);
                String artifact =
                    jarFile.getManifest().getMainAttributes().getValue("Bundle-SymbolicName");
                if (artifact == null)
                {
                    artifact = extraClasspathElement.substring(
                        0, extraClasspathElement.lastIndexOf('.'));
                }
                String version =
                    jarFile.getManifest().getMainAttributes().getValue("Bundle-Version");
                if (version == null)
                {
                    if (artifact.indexOf('-') == -1)
                    {
                        version = "1.0";
                    }
                    else
                    {
                        int versionStart;
                        if (artifact.endsWith("-SNAPSHOT"))
                        {
                            versionStart = artifact.lastIndexOf('-', artifact.length() - 10);
                        }
                        else
                        {
                            versionStart = artifact.lastIndexOf('-');
                        }
                        version = artifact.substring(versionStart + 1);
                        artifact = artifact.substring(0, versionStart);
                    }
                }

                sb.append("      <dep:dependency>\n");
                sb.append("        <dep:groupId>org.codehaus.cargo.classpath</dep:groupId>\n");
                sb.append("        <dep:artifactId>" + artifact + "</dep:artifactId>\n");
                sb.append("        <dep:version>" + version + "</dep:version>\n");
                sb.append("        <dep:type>" + extension + "</dep:type>\n");
                sb.append("      </dep:dependency>\n");
            }
            sb.append("    </dep:dependencies>");
        }

        return sb.toString();
    }

    /**
     * @param bundle Bundle deployable
     * @return Bundle identifier
     * @throws Exception if an error occurred while checking the container's state
     */
    public long getBundleId(Bundle bundle) throws Exception
    {
        logger.debug("Looking for bundle " + bundle, this.getClass().getName());

        JarFile bundleJar = new JarFile(bundle.getFile());

        String fileName = new File(bundle.getFile()).getName();
        String symbolicName = getManifestAttribute(bundleJar, "Bundle-SymbolicName");
        String version = getManifestAttribute(bundleJar, "Bundle-Version");

        logger.debug("Detected symbolic name " + symbolicName + ", version " + version
            + " for bundle named " + fileName, this.getClass().getName());

        JMXServiceURL jmxServiceURL = new JMXServiceURL("service:jmx:rmi://" + host
            + "/jndi/rmi://" + host + ":" + rmiPort + "/JMXConnector");

        Map<String, Object> map = new HashMap<String, Object>();
        map.put(JMXConnector.CREDENTIALS, new String[] {username, password});

        long bundleId = 0;
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
                        if ((location.contains(symbolicName) && location.contains(version))
                            || location.contains(fileName))
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
            throw new ContainerException("Cannot find bundle " + bundle);
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
     * Returns a given manifest attribute value
     * @param jarFile JAR file to look into
     * @param attributeName Attribute to look for
     * @return Value of <code>attributeName</code> in the manifest of <code>jarFile</code>.
     * @throws Exception in case of error
     */
    private String getManifestAttribute(JarFile jarFile, String attributeName) throws Exception
    {
        Attributes attributes = jarFile.getManifest().getMainAttributes();
        String attributeValue = attributes.getValue(attributeName);
        if (attributeValue == null)
        {
            throw new IllegalArgumentException(
                "The file " + jarFile + " doesn't contain attribute " + attributeName);
        }
        return attributeValue;
    }
}
