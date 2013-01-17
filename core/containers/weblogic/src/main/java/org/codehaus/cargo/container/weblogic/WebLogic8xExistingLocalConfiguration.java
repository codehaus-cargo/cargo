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
package org.codehaus.cargo.container.weblogic;

import java.io.File;
import java.io.IOException;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractExistingLocalConfiguration;
import org.codehaus.cargo.container.weblogic.internal.WebLogicExistingLocalConfigurationCapability;
import org.codehaus.cargo.container.weblogic.internal.WebLogicLocalContainer;

/**
 * WebLogic 8.x existing configuration implementation. The configuration home must point to a valid
 * WebLogic domain directory.
 * 
 * @version $Id$
 */
public class WebLogic8xExistingLocalConfiguration extends
        AbstractExistingLocalConfiguration implements WebLogicConfiguration
{
    /**
     * Capability of the WebLogic standalone configuration.
     */
    private static ConfigurationCapability capability =
        new WebLogicExistingLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see AbstractExistingLocalConfiguration#AbstractExistingLocalConfiguration(String)
     */
    public WebLogic8xExistingLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(ServletPropertySet.PORT, "7001");

        setProperty(WebLogicPropertySet.ADMIN_USER, "weblogic");
        setProperty(WebLogicPropertySet.ADMIN_PWD, "weblogic");
        setProperty(WebLogicPropertySet.SERVER, "server");
    }

    /**
     * {@inheritDoc}
     * @see AbstractExistingLocalConfiguration#configure(LocalContainer)
     */
    @Override
    protected void doConfigure(LocalContainer container) throws Exception
    {
        setupDeployables((WebLogicLocalContainer) container);
    }

    /**
     * {@inheritDoc}
     * @see AbstractExistingLocalConfiguration#getCapability()
     */
    public ConfigurationCapability getCapability()
    {
        return capability;
    }

    /**
     * {@inheritDoc}
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        return "WebLogic Existing Configuration";
    }

    /**
     * Deploy the Deployables to the weblogic configuration.
     * 
     * @param container the container to configure
     * @throws IOException if the cargo ping deployment fails
     */
    protected void setupDeployables(WebLogicLocalContainer container) throws IOException
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

        WebLogic8xConfigXmlInstalledLocalDeployer deployer =
            new WebLogic8xConfigXmlInstalledLocalDeployer(container);
        deployer.deploy(getDeployables());

        // Deploy the cargocpc web-app by copying the WAR file
        getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war",
                new File(deployDir, "cargocpc.war"));
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
