/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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
package org.codehaus.cargo.container.tomcat;

import java.io.File;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.spi.configuration.AbstractExistingLocalConfiguration;
import org.codehaus.cargo.container.tomcat.internal.TomcatExistingLocalConfigurationCapability;

/**
 * Tomcat existing {@link org.codehaus.cargo.container.configuration.Configuration} implementation.
 * 
 * @version $Id$
 */
public class TomcatExistingLocalConfiguration extends AbstractExistingLocalConfiguration
{
    /**
     * Capability of the Tomcat exisiting configuration.
     */
    private static ConfigurationCapability capability =
        new TomcatExistingLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see AbstractExistingLocalConfiguration#AbstractExistingLocalConfiguration(String)
     */
    public TomcatExistingLocalConfiguration(String dir)
    {
        super(dir);
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
        if (container instanceof Tomcat5xEmbeddedLocalContainer)
        {
            // embedded Tomcat doesn't need CPC
            Tomcat5xEmbeddedLocalDeployer deployer =
                new Tomcat5xEmbeddedLocalDeployer((Tomcat5xEmbeddedLocalContainer) container);
            deployer.deploy(getDeployables());
        }
        else
        {
            InstalledLocalContainer tomcatContainer = (InstalledLocalContainer) container;

            File webappsDir = new File(getHome(), "webapps");

            if (!webappsDir.exists())
            {
                throw new ContainerException("Invalid existing configuration: The ["
                    + webappsDir.getPath() + "] directory does not exist");
            }

            TomcatCopyingInstalledLocalDeployer deployer =
                new TomcatCopyingInstalledLocalDeployer(tomcatContainer);
            deployer.setShouldDeployExpandedWARs(true);
            deployer.setShouldCopyWars(true);
            deployer.deploy(getDeployables());

            // Deploy the CPC (Cargo Ping Component) to the webapps directory.
            getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war",
                new File(webappsDir, "cargocpc.war"));
        }
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.configuration.ContainerConfiguration#verify()
     */
    @Override
    public void verify()
    {
        // Nothing to verify right now...
    }

    /**
     * {@inheritDoc}
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        return "Tomcat Existing Configuration";
    }
}
