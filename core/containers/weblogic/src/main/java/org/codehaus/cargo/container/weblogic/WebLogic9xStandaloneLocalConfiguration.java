/* 
 * ========================================================================
 * 
 * Copyright 2004-2005 Vincent Massol.
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
package org.codehaus.cargo.container.weblogic;

import java.io.IOException;
import java.util.Iterator;

import org.apache.tools.ant.types.FilterChain;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.EAR;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration;
import org.codehaus.cargo.container.weblogic.internal.WebLogicStandaloneLocalConfigurationCapability;

/**
 * WebLogic standalone {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration}
 * implementation.
 * 
 * @version $Id$
 */
public class WebLogic9xStandaloneLocalConfiguration extends AbstractStandaloneLocalConfiguration
    implements WebLogicConfiguration
{
    /**
     * Capability of the WebLogic standalone configuration.
     */
    private static ConfigurationCapability capability =
        new WebLogicStandaloneLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * 
     * @see AbstractStandaloneLocalConfiguration#AbstractStandaloneLocalConfiguration(String)
     */
    public WebLogic9xStandaloneLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(WebLogicPropertySet.ADMIN_USER, "weblogic");
        setProperty(WebLogicPropertySet.ADMIN_PWD, "weblogic");
        setProperty(WebLogicPropertySet.SERVER, "server");
        setProperty(WebLogicPropertySet.CONFIGURATION_VERSION, "9.2.3.0");
        setProperty(WebLogicPropertySet.DOMAIN_VERSION, "9.2.3.0");
        setProperty(ServletPropertySet.PORT, "7001");
        setProperty(GeneralPropertySet.HOSTNAME, "localhost");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.configuration.Configuration#getCapability()
     */
    public ConfigurationCapability getCapability()
    {
        return capability;
    }

    /**
     * {@inheritDoc}
     * 
     * @see AbstractStandaloneLocalConfiguration#configure(LocalContainer)
     */
    protected void doConfigure(LocalContainer container) throws Exception
    {
        setupConfigurationDir();

        // in weblogic 9+ config.xml is organized under the config directory
        String configDir = getFileHandler().createDirectory(getDomainHome(), "/config");

        // in weblogic 9+ sensitive files are organized under the security
        // directory
        String securityDir = getFileHandler().createDirectory(getDomainHome(), "/security");

        // as this is an initial install, this directory will not exist, yet
        getFileHandler().createDirectory(getDomainHome(),
            ((WebLogicLocalContainer) container).getAutoDeployDirectory());

        FilterChain filterChain = createWebLogicFilterChain();
        setupDeployables(filterChain);

        // make sure you use this method, as it ensures the same filehandler
        // that created the directory will be used to copy the resource.
        // This is especially important for unit testing
        getResourceUtils().copyResource(RESOURCE_PATH + container.getId() + "/config.xml",
            getFileHandler().append(configDir, "config.xml"), getFileHandler(), filterChain);

        getResourceUtils().copyResource(
            RESOURCE_PATH + container.getId() + "/DefaultAuthenticatorInit.ldift",
            getFileHandler().append(securityDir, "DefaultAuthenticatorInit.ldift"),
            getFileHandler(), filterChain);

        getResourceUtils().copyResource(
            RESOURCE_PATH + container.getId() + "/SerializedSystemIni.dat",
            getFileHandler().append(securityDir, "SerializedSystemIni.dat"), getFileHandler());

        deployCargoPing((WebLogicLocalContainer) container);
    }

    /**
     * @return an Ant filter chain containing implementation for the filter tokens used in the
     *         WebLogic configuration files.
     */
    private FilterChain createWebLogicFilterChain()
    {
        FilterChain filterChain = getFilterChain();

        getAntUtils().addTokenToFilterChain(filterChain,
            WebLogicPropertySet.CONFIGURATION_VERSION,
            getPropertyValue(WebLogicPropertySet.CONFIGURATION_VERSION));

        getAntUtils().addTokenToFilterChain(filterChain, WebLogicPropertySet.DOMAIN_VERSION,
            getPropertyValue(WebLogicPropertySet.DOMAIN_VERSION));

        getAntUtils().addTokenToFilterChain(filterChain, WebLogicPropertySet.SERVER,
            getPropertyValue(WebLogicPropertySet.SERVER));

        getAntUtils().addTokenToFilterChain(filterChain, WebLogicPropertySet.LOGGING,
            getWebLogicLogLevel(getPropertyValue(GeneralPropertySet.LOGGING)));

        return filterChain;
    }

    /**
     * Translate Cargo logging levels into WebLogic logging levels.
     * 
     * @param cargoLogLevel Cargo logging level
     * @return the corresponding WebLogic logging level
     */
    private String getWebLogicLogLevel(String cargoLogLevel)
    {
        String returnVal = "Info";

        if (cargoLogLevel == null || cargoLogLevel.trim().equals("")
            || cargoLogLevel.equalsIgnoreCase("medium"))
        {
            // accept default of medium/Info
        }
        else if (cargoLogLevel.equalsIgnoreCase("low"))
        {
            returnVal = "Warning";
        }
        else if (cargoLogLevel.equalsIgnoreCase("high"))
        {
            returnVal = "Debug";
        }

        return returnVal;
    }

    /**
     * Add applications into the WebLogic configuration.
     * 
     * @param filterChain where to insert the application configuration
     */
    protected void setupDeployables(FilterChain filterChain)
    {
        StringBuffer appTokenValue = new StringBuffer(" ");

        Iterator it = getDeployables().iterator();
        while (it.hasNext())
        {
            Deployable deployable = (Deployable) it.next();

            String name = getNameFromDeployable(deployable);
            if (name != null)
            {
                appTokenValue.append("<app-deployment>");
                appTokenValue.append("<name>");
                appTokenValue.append(name);
                appTokenValue.append("</name>");
                appTokenValue.append("<target>" + getPropertyValue(WebLogicPropertySet.SERVER)
                    + "</target>");
                appTokenValue.append("<source-path>");
                appTokenValue.append(getAbsolutePath(deployable));
                appTokenValue.append("</source-path>");
                appTokenValue.append("</app-deployment>");
            }
        }
        getAntUtils().addTokenToFilterChain(filterChain, "weblogic.apps",
            appTokenValue.toString());
    }

    /**
     * extract the name we want to call the deployable.
     * 
     * @param deployable - file we are going to deploy
     * @return name we wish to use for the application or null, if not supported
     */
    private String getNameFromDeployable(Deployable deployable)
    {
        String name = null;
        if (deployable.getType() == DeployableType.WAR)
        {
            name = ((WAR) deployable).getContext();
        }
        else if (deployable.getType() == DeployableType.EAR)
        {
            name = ((EAR) deployable).getName();
        }
        return name;
    }

    /**
     * Deploy the Cargo Ping utility to the container.
     * 
     * @param container the container to configure
     * @throws IOException if the cargo ping deployment fails
     */
    protected void deployCargoPing(WebLogicLocalContainer container) throws IOException
    {
        // as this is an initial install, this directory will not exist, yet
        String deployDir =
            getFileHandler().createDirectory(getDomainHome(), container.getAutoDeployDirectory());

        // Deploy the cargocpc web-app by copying the WAR file
        getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war",
            getFileHandler().append(deployDir, "cargocpc.war"), getFileHandler());
    }

    /**
     * gets the absolute path from a file that may be relative to the current directory.
     * 
     * @param deployable - what to extract the file path from
     * @return - absolute path to the deployable
     */
    String getAbsolutePath(Deployable deployable)
    {
        String path = deployable.getFile();
        return getFileHandler().getAbsolutePath(path);
    }

    /**
     * {@inheritDoc}
     * 
     * @see Object#toString()
     */
    public String toString()
    {
        return "WebLogic 9x Standalone Configuration";
    }

    /**
     * {@inheritDoc}
     */
    public String getDomainHome()
    {
        return getHome();
    }
}
