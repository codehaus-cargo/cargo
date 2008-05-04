/* 
 * ========================================================================
 * 
 * Copyright 2004-2006 Vincent Massol.
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
package org.codehaus.cargo.container.tomcat.internal;

import org.apache.tools.ant.types.FilterChain;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.EmbeddedLocalContainer;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.tomcat.TomcatCopyingInstalledLocalDeployer;
import org.codehaus.cargo.container.tomcat.TomcatWAR;
import org.codehaus.cargo.container.tomcat.Tomcat5xEmbeddedLocalDeployer;
import org.codehaus.cargo.container.tomcat.Tomcat5xEmbeddedLocalContainer;

import java.io.File;
import java.util.Iterator;

/**
 * Catalina standalone {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration}
 * implementation.
 *
 * @version $Id$
 */
public abstract class AbstractCatalinaStandaloneLocalConfiguration
    extends AbstractTomcatStandaloneLocalConfiguration
{
    /**
     * {@inheritDoc}
     * @see AbstractTomcatStandaloneLocalConfiguration#AbstractTomcatStandaloneLocalConfiguration(String)
     */
    public AbstractCatalinaStandaloneLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(ServletPropertySet.USERS, "admin::manager");
        setProperty(GeneralPropertySet.RMI_PORT, "8205");
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.configuration.AbstractLocalConfiguration#configure(LocalContainer)
     */
    protected void doConfigure(LocalContainer container) throws Exception
    {
        setupConfigurationDir();

        FilterChain filterChain = createTomcatFilterChain();

        getFileHandler().createDirectory(getHome(), "temp");
        getFileHandler().createDirectory(getHome(), "logs");

        String confDir = getFileHandler().createDirectory(getHome(), "conf");

        getResourceUtils().copyResource(RESOURCE_PATH + container.getId() + "/server.xml",
            new File(confDir, "server.xml"), filterChain);


        if (container instanceof InstalledLocalContainer)
        {
            InstalledLocalContainer installedContainer = (InstalledLocalContainer) container;
            String[] sharedClassPath = installedContainer.getSharedClasspath();
            StringBuffer tmp = new StringBuffer();
            if (sharedClassPath != null)
            {
                for (int i = 0; i < sharedClassPath.length; i++)
                {
                    tmp.append(',').append(sharedClassPath[i]);
                }
            }
            getAntUtils().addTokenToFilterChain(filterChain, "catalina.common.loader",
                tmp.toString());
        }

        getResourceUtils().copyResource(RESOURCE_PATH + container.getId() + "/catalina.properties",
            new File(confDir, "catalina.properties"), filterChain);

        getResourceUtils().copyResource(RESOURCE_PATH + container.getId()
            + "/tomcat-users.xml", new File(confDir, "tomcat-users.xml"), filterChain);
        getResourceUtils().copyResource(RESOURCE_PATH + container.getId() + "/web.xml",
            new File(confDir, "web.xml"));

        setupManager(container);

        // deploy the web-app by copying the WAR file
        setupWebApps(container);
    }

    /**
     * Setup the manager webapp.
     *
     * @param container the container to configure
     */
    protected abstract void setupManager(LocalContainer container);

    /**
     * Setup the web apps directory and deploy applications.
     *
     * @param container the container to configure
     */
    private void setupWebApps(LocalContainer container)
    {
        try 
        {
            if (container instanceof EmbeddedLocalContainer)
            {
                // embedded Tomcat doesn't need CPC
                Tomcat5xEmbeddedLocalDeployer deployer =
                    new Tomcat5xEmbeddedLocalDeployer((Tomcat5xEmbeddedLocalContainer) container);
                deployer.deploy(getDeployables());
            }
            else
            {
                // Create a webapps directory for automatic deployment of WARs dropped inside.
                String appDir = getFileHandler().createDirectory(getHome(), "webapps");

                // Deploy all deployables into the webapps directory.
                TomcatCopyingInstalledLocalDeployer deployer =
                    new TomcatCopyingInstalledLocalDeployer((InstalledLocalContainer) container);
                deployer.setShouldDeployExpandedWARs(true);
                deployer.setShouldCopyWars(false);
                deployer.deploy(getDeployables());

                // Deploy the CPC (Cargo Ping Component) to the webapps directory
                getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war",
                    new File(appDir, "cargocpc.war"));
            }
        }
        catch (Exception e)
        {
            throw new ContainerException("Failed to create a " + container.getName()
                + " container configuration", e);
        }
    }

    /**
     * Translate Cargo logging levels into Tomcat logging levels.
     *
     * @param cargoLoggingLevel Cargo logging level
     * @return the corresponding Tomcat logging level
     */
    private String getTomcatLoggingLevel(String cargoLoggingLevel)
    {
        String level;

        if (cargoLoggingLevel.equalsIgnoreCase("low"))
        {
            level = "1";
        }
        else if (cargoLoggingLevel.equalsIgnoreCase("medium"))
        {
            level = "2";
        }
        else
        {
            level = "4";
        }

        return level;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration#createFilterChain()
     */
    protected FilterChain createTomcatFilterChain()
    {
        FilterChain filterChain = getFilterChain();
        
        // Add logging property tokens
        getAntUtils().addTokenToFilterChain(filterChain, "catalina.logging.level", 
            getTomcatLoggingLevel(getPropertyValue(GeneralPropertySet.LOGGING)));

        // Add Tomcat shutdown port token
        getAntUtils().addTokenToFilterChain(filterChain, GeneralPropertySet.RMI_PORT,
            getPropertyValue(GeneralPropertySet.RMI_PORT));

        // Add Catalina secure token, set to true if the protocol is https, false otherwise
        getAntUtils().addTokenToFilterChain(filterChain, "catalina.secure",
            String.valueOf("https".equalsIgnoreCase(getPropertyValue(
                GeneralPropertySet.PROTOCOL))));

        // Add token filters for authenticated users
        getAntUtils().addTokenToFilterChain(filterChain, "tomcat.users", getSecurityToken());

        // Add webapp contexts in order to explicitely point to where the
        // wars are located.
        StringBuffer webappTokenValue = new StringBuffer(" ");
        
        Iterator it = getDeployables().iterator();
        while (it.hasNext())
        {
            Deployable deployable = (Deployable) it.next();

            if (deployable.getType() != DeployableType.WAR)
            {
                throw new ContainerException("Only WAR archives are supported for deployment "
                    + "in Tomcat. Got [" + deployable.getFile() + "]");
            }
           
            // Do not create tokens for WARs containing a context file as they
            // are copied to the webapps directory.
            if (deployable instanceof TomcatWAR)
            {
                TomcatWAR tomcatWar = (TomcatWAR) deployable;
                if (tomcatWar.containsContextFile())
                {
                    continue;
                }
            }

            webappTokenValue.append(createContextToken((WAR) deployable));    
        }
        
        getAntUtils().addTokenToFilterChain(filterChain, "tomcat.webapps",
            webappTokenValue.toString());
        
        return filterChain;
    }

    /**
     * @return The XML that should be inserted into the server.xml file.  If no datasource,
     * return " ".  Do not return empty string, as and cannot handle this.
     */
    protected abstract String createDatasourceTokenValue();

    /**
     * @param deployable the WAR to deploy
     * @return the "context" XML element to instert in the Tomcat <code>server.xml</code> 
     *         configuration file 
     */
    protected String createContextToken(WAR deployable)
    {
        StringBuffer contextTokenValue = new StringBuffer();

        contextTokenValue.append("<Context path=\"");
        contextTokenValue.append("/" + deployable.getContext());
        contextTokenValue.append("\" docBase=\"");

        // Tomcat requires an absolute path for the "docBase" attribute.
        contextTokenValue.append(new File(deployable.getFile()).getAbsolutePath());

        contextTokenValue.append("\" debug=\"");
        contextTokenValue.append(getTomcatLoggingLevel(
            getPropertyValue(GeneralPropertySet.LOGGING)));
        contextTokenValue.append("\">");

        contextTokenValue.append("\n" + createDatasourceTokenValue() + "\n");

        contextTokenValue.append("</Context>");
        return contextTokenValue.toString();
    }

    /**
     * {@inheritDoc}
     * @see Object#toString()
     */
    public String toString()
    {
        return "Catalina Standalone Configuration";
    }
}
