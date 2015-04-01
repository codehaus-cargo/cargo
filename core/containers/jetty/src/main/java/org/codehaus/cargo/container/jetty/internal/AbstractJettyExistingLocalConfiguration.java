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
package org.codehaus.cargo.container.jetty.internal;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.jetty.JettyPropertySet;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractExistingLocalConfiguration;
import org.codehaus.cargo.container.spi.deployer.AbstractInstalledLocalDeployer;

import java.io.File;

/** Abstract configuration for existing local Jetty
 *
 */
public abstract class AbstractJettyExistingLocalConfiguration
        extends AbstractExistingLocalConfiguration
{
    /**
     * Capability of the Jetty Existing local configuration.
     */ 
    private static ConfigurationCapability capability =
            new JettyExistingLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.configuration.AbstractLocalConfiguration#AbstractLocalConfiguration(String)
     */
    public AbstractJettyExistingLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(GeneralPropertySet.RMI_PORT, "8079");
        setProperty(JettyPropertySet.CREATE_CONTEXT_XML, "true");
    }

    /** Creation of deployer according to the container
     *
     * @param container reference for current container
     * @return new instance of deployer for the container
     */
    public abstract AbstractInstalledLocalDeployer createDeployer(LocalContainer container);

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.configuration.AbstractLocalConfiguration#doConfigure(org.codehaus.cargo.container.LocalContainer)
     */
    @Override
    protected void doConfigure(LocalContainer container) throws Exception
    {

        File deployDir = new File(getHome(), "webapps");
        if (!deployDir.exists())
        {
            throw new ContainerException("Invalid existing configuration: The ["
                    + deployDir.getPath() + "] directory does not exist");
        }

        // create the correct deployer
        AbstractInstalledLocalDeployer deployer = createDeployer(container);
        deployer.redeploy(getDeployables());

        // Deploy the CPC (Cargo Ping Component) to the webapps directory
        getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war",
                new File(deployDir, "cargocpc.war"));

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
     *
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        return "Jetty Existing Configuration";
    }
}
