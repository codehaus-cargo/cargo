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
package org.codehaus.cargo.container.jrun;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.types.FilterChain;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.configuration.builder.ConfigurationBuilder;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.configuration.entry.ResourceSupport;
import org.codehaus.cargo.container.jrun.internal.JRun4xConfigurationBuilder;
import org.codehaus.cargo.container.jrun.internal.JRun4xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.spi.configuration.builder.AbstractStandaloneLocalConfigurationWithXMLConfigurationBuilder;

/**
 * JRun standalone {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration}
 * implementation.
 * 
 * @version $Id$
 */
public class JRun4xStandaloneLocalConfiguration extends
    AbstractStandaloneLocalConfigurationWithXMLConfigurationBuilder implements ResourceSupport
{

    /**
     * Capability of the JRun4x standalone local configuration.
     */
    private static ConfigurationCapability capability =
        new JRun4xStandaloneLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see AbstractCatalinaStandaloneLocalConfiguration#AbstractCatalinaStandaloneLocalConfiguration(String)
     */
    public JRun4xStandaloneLocalConfiguration(String dir)
    {
        super(dir);
        getServerName();
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.configuration.Configuration#getCapability()
     */
    public ConfigurationCapability getCapability()
    {
        return capability;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.configuration.AbstractLocalConfiguration#configure(LocalContainer)
     */
    @Override
    protected void doConfigure(LocalContainer container) throws Exception
    {
        setupConfigurationDir();

        filterResources(container);

        String to = getHome();
        String from = ((InstalledLocalContainer) container).getHome();

        // copy required server files
        for (String path : getRequiredFiles())
        {
            getFileHandler().copyFile(from + "/" + path, to + "/" + path);
        }

        // copy hot fix jar for spaces in path if present.
        File spaceHotFix = new File(from + "/servers/lib/54101.jar");
        if (spaceHotFix.exists())
        {
            getFileHandler().copyFile(spaceHotFix.getAbsolutePath(),
                to + "/servers/lib/54101.jar");
        }

        // get the required binary
        if (System.getProperty("os.name").toLowerCase().contains("windows"))
        {
            getFileHandler().copyFile(from + "/bin/jrun.exe", to + "/bin/jrun.exe");
        }
        else
        {
            getFileHandler().copyFile(from + "/bin/jrun", to + "/bin/jrun");
        }

        InstalledLocalContainer jrunContainer = (InstalledLocalContainer) container;
        JRun4xInstalledLocalDeployer deployer = new JRun4xInstalledLocalDeployer(jrunContainer);
        deployer.deploy(getDeployables());

        // Deploy the CPC (Cargo Ping Component) to the webapps directory.
        getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war",
            new File(deployer.getDeployableDir(), "cargocpc.war"));
    }

    /**
     * Get the JRun server name for this configuration.
     * @return the JRun server name.
     */
    private String getServerName()
    {
        // set default server instance name if not set
        if (getPropertyValue(JRun4xPropertySet.SERVER_NAME) == null)
        {
            setProperty(JRun4xPropertySet.SERVER_NAME, JRun4xPropertySet.DEFAULT_SERVER_NAME);
        }
        return getPropertyValue(JRun4xPropertySet.SERVER_NAME);
    }

    /**
     * The list of required files needed to start up JRun4.
     * @return the list of files required to start JRun4.
     */
    private List<String> getRequiredFiles()
    {
        String serverInfDirectory = "/servers/" + getServerName() + "/SERVER-INF/";

        List<String> files = new ArrayList<String>();
        files.add(serverInfDirectory + "auth.config");
        files.add(serverInfDirectory + "connector.properties");
        files.add(serverInfDirectory + "default-web.xml");
        files.add(serverInfDirectory + "jndi.properties");
        files.add(serverInfDirectory + "jrun-dtd-mappings.xml");
        files.add(serverInfDirectory + "jrun-jms.xml");
        files.add(serverInfDirectory + "jrun-resources.xml");
        files.add(serverInfDirectory + "jrun-security-policy.xml");

        files.add("/lib/jrun.jar");
        files.add("/lib/flashgateway.ear");
        files.add("/lib/jrun-comp.ear");
        files.add("/lib/security.properties");
        files.add("/lib/license.properties");
        files.add("/lib/jrun.policy");
        files.add("/lib/mime.types");

        return files;
    }

    /**
     * Filter configuration files.
     * @param container the current {@link LocalContainer}.
     * @throws IOException if something goes wrong with the file processing.
     */
    private void filterResources(LocalContainer container) throws IOException
    {
        FilterChain chain = new JRun4xFilterChain(container);

        String to = getHome();
        String libDir = getFileHandler().createDirectory(to, "lib");
        String resourcePath = RESOURCE_PATH + container.getId();

        // filter server name in servers.xml
        getResourceUtils().copyResource(resourcePath + "/servers.xml",
            new File(libDir, "/servers.xml"), chain);

        // filter VM config in jvm.config
        getFileHandler().createDirectory(to, "bin");
        getResourceUtils().copyResource(resourcePath + "/jvm.config",
            new File(to + "/bin/jvm.config"), chain);

        String serverInf = "servers/" + getServerName() + "/SERVER-INF";
        String serverInfDir = getFileHandler().createDirectory(getHome(), serverInf);

        // filter port and logging level in jrun.xml
        getResourceUtils().copyResource(resourcePath + "/jrun.xml",
            new File(serverInfDir, "/jrun.xml"), chain);

        // filter users in jrun-users.xml
        getResourceUtils().copyResource(resourcePath + "/jrun-users.xml",
            new File(serverInfDir, "/jrun-users.xml"), chain);

        // filter rmi port in jndi.propertiess
        getResourceUtils().copyResource(resourcePath + "/jndi.properties",
            new File(serverInfDir, "/jndi.properties"), chain);
    }

    /**
     * {@inheritDoc}
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        return "JRun 4.x Standalone Configuration";
    }

    /**
     * Gets this configuration's {@link ConfigurationBuilder}.
     * @param container the current Container.
     * @return the {@link ConfigurationBuilder}.
     */
    @Override
    protected ConfigurationBuilder createConfigurationBuilder(LocalContainer container)
    {
        return new JRun4xConfigurationBuilder();
    }

    /**
     * The xml namespaces.
     * @return an empty Map.
     */
    @Override
    protected Map<String, String> getNamespaces()
    {
        return Collections.emptyMap();
    }

    /**
     * Returns the Xpath for the parent element of the datasource xml.
     * @return the Xpath for the parent element of the datasource xml.
     */
    @Override
    protected String getXpathForDataSourcesParent()
    {
        return getResourceXPath();
    }

    /**
     * Gets the file to insert Datasource Configuraton into.
     * @param ds the Datasource instance.
     * @param container the current Container.
     * @return the file to insert Datasource Configuraton into.
     */
    @Override
    protected String getOrCreateDataSourceConfigurationFile(DataSource ds,
            LocalContainer container)
    {
        return getResourceFile(container);
    }

    /**
     * Gets the file to insert Resource Configuraton into.
     * @param resource the Resource definition.
     * @param container the current Container.
     * @return the file to insert Resource Configuraton into.
     */
    @Override
    protected String getOrCreateResourceConfigurationFile(Resource resource,
            LocalContainer container)
    {
        return getResourceFile(container);
    }

    /**
     * The XPath of the parent Element of resource configuration.
     * @return XPath of the parent Element of resource configuration.
     */
    @Override
    protected String getXpathForResourcesParent()
    {
        return getResourceXPath();
    }

    /**
     * Get the file that Datasource and Resource configuration is defined in.
     * @param container the current Container.
     * @return the path to the configuration file that holds Resource information.
     */
    private String getResourceFile(LocalContainer container)
    {
        return getFileHandler().append(getHome(),
                "servers/" + getServerName() + "/SERVER-INF/jrun-resources.xml");
    }

    /**
     * Returns the parent element XPath for Resource configuration.
     * @return the parent element XPath for Resource configuration.
     */
    private String getResourceXPath()
    {
        return "//jrun-resources";
    }

}
