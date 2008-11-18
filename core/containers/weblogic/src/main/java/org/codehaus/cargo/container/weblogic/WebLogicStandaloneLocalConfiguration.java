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

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.apache.tools.ant.types.FilterChain;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration;
import org.codehaus.cargo.container.weblogic.internal.WebLogicStandaloneLocalConfigurationCapability;

/**
 * WebLogic standalone
 * {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration}
 * implementation.
 *  
 * @version $Id$
 */
public class WebLogicStandaloneLocalConfiguration extends
        AbstractStandaloneLocalConfiguration implements WebLogicConfiguration
{
    /**
     * Capability of the WebLogic standalone configuration.
     */
    private static ConfigurationCapability capability = 
        new WebLogicStandaloneLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see AbstractStandaloneLocalConfiguration#AbstractStandaloneLocalConfiguration(String)
     */
    public WebLogicStandaloneLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(WebLogicPropertySet.ADMIN_USER, "weblogic");
        setProperty(WebLogicPropertySet.ADMIN_PWD, "weblogic");
        setProperty(WebLogicPropertySet.SERVER, "server");
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
     * @see AbstractStandaloneLocalConfiguration#configure(LocalContainer)
     */
    protected void doConfigure(LocalContainer container) throws Exception
    {
        setupConfigurationDir();

        FilterChain filterChain = createWebLogicFilterChain();

        getResourceUtils().copyResource(
                RESOURCE_PATH + container.getId() + "/config.xml",
            new File(getHome(), "config.xml"), filterChain);

        getResourceUtils().copyResource(
                RESOURCE_PATH + container.getId()
            + "/DefaultAuthenticatorInit.ldift",
                new File(getHome(), "DefaultAuthenticatorInit.ldift"),
                filterChain);

        setupDeployables((WebLogicLocalContainer) container);
    }

    /**
     * @return an Ant filter chain containing implementation for the filter
     *         tokens used in the WebLogic configuration files
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
                appTokenValue.append("<Application "); 
                appTokenValue.append("Name=\"_" + context + "_app\" ");
                appTokenValue.append("Path=\""
                        + getFileHandler().getParent(deployable.getFile())
                        + "\" ");
                appTokenValue
                        .append("StagedTargets=\"server\" StagingMode=\"stage\" TwoPhase=\"true\"");
                appTokenValue.append(">");
                
                appTokenValue.append("<WebAppComponent ");
                appTokenValue.append("Name=\"" + context + "\" "); 
                appTokenValue.append("Targets=\"server\" ");
                appTokenValue.append("URI=\"" + context + "\"");
                appTokenValue.append("/></Application>");
            }
        }
        
        getAntUtils().addTokenToFilterChain(filterChain, "weblogic.apps",
                appTokenValue.toString());
            
        return filterChain;
    }

    /**
     * Deploy the Deployables to the weblogic configuration.
     * 
     * @param container
     *                the container to configure
     * @throws IOException
     *                 if the cargo ping deployment fails
     */
    protected void setupDeployables(WebLogicLocalContainer container)
        throws IOException
    {
        WebLogicLocalContainer weblogicContainer = container;
            // Get the deployable folder from container config. If it is not set
        File deployDir = new File(getDomainHome(), weblogicContainer
                .getAutoDeployDirectory());
            // use the default one.
        if (!deployDir.exists())
        {
            throw new ContainerException(
                    "Invalid existing configuration: The ["
                            + deployDir.getPath()
                            + "] directory does not exist");
        }

        WebLogicCopyingInstalledLocalDeployer deployer = new WebLogicCopyingInstalledLocalDeployer(
                (InstalledLocalContainer) container);
        deployer.deploy(getDeployables());
            
        // Deploy the cargocpc web-app by copying the WAR file
        getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war",
                new File(deployDir, "cargocpc.war"));
    }    

    /**
     * {@inheritDoc}
     * @see Object#toString()
     */
    public String toString()
    {
        return "WebLogic Standalone Configuration";
    }

    /**
     * {@inheritDoc}
     * 
     */
    public String getDomainHome()
    {
        return getHome();
    }
}
