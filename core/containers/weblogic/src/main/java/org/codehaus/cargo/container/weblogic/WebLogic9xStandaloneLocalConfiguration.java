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
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
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

        FilterChain filterChain = createWebLogicFilterChain();

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
            getFileHandler().append(securityDir, "SerializedSystemIni.dat"), getFileHandler(),
            filterChain);

        setupDeployables((WebLogicLocalContainer) container);
    }

    /**
     * @return an Ant filter chain containing implementation for the filter tokens used in the
     *         WebLogic configuration files
     */
    private FilterChain createWebLogicFilterChain()
    {
        FilterChain filterChain = getFilterChain();

        StringBuffer appTokenValue = new StringBuffer(" ");

        Iterator it = getDeployables().iterator();
        while (it.hasNext())
        {
            Deployable deployable = (Deployable) it.next();

            if ((deployable.getType() == DeployableType.WAR)
                && ((WAR) deployable).isExpandedWar())
            {
                String context = ((WAR) deployable).getContext();
                appTokenValue.append("<app-deployment>");
                appTokenValue.append("<name>");
                appTokenValue.append("_" + context + "_app");
                appTokenValue.append("</name>");
                appTokenValue.append("<target>server</target>");
                appTokenValue.append("<source-path>");
                appTokenValue.append(getFileHandler().getParent(deployable.getFile()));
                appTokenValue.append("</source-path>");
                appTokenValue.append("<uri>");
                appTokenValue.append(context);
                appTokenValue.append("</uri>");
                appTokenValue.append("</app-deployment>");
            }
        }
        getAntUtils().addTokenToFilterChain(filterChain, "weblogic.apps",
            appTokenValue.toString());

        getAntUtils().addTokenToFilterChain(filterChain,
            WebLogicPropertySet.CONFIGURATION_VERSION,
            getPropertyValue(WebLogicPropertySet.CONFIGURATION_VERSION));

        getAntUtils().addTokenToFilterChain(filterChain, WebLogicPropertySet.DOMAIN_VERSION,
            getPropertyValue(WebLogicPropertySet.DOMAIN_VERSION));

        getAntUtils().addTokenToFilterChain(filterChain, WebLogicPropertySet.SERVER,
            getPropertyValue(WebLogicPropertySet.SERVER));

        return filterChain;
    }

    /**
     * Deploy the Deployables to the weblogic configuration.
     * 
     * @param container the container to configure
     * @throws IOException if the cargo ping deployment fails
     */
    protected void setupDeployables(WebLogicLocalContainer container) throws IOException
    {
        // as this is an initial install, this directory will not exist, yet
        String deployDir =
            getFileHandler().createDirectory(getDomainHome(), container.getAutoDeployDirectory());

        WebLogicCopyingInstalledLocalDeployer deployer =
            new WebLogicCopyingInstalledLocalDeployer((InstalledLocalContainer) container);
        deployer.deploy(getDeployables());

        // Deploy the cargocpc web-app by copying the WAR file
        getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war",
            getFileHandler().append(deployDir, "cargocpc.war"), getFileHandler());
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
