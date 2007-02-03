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
package org.codehaus.cargo.container.resin;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.resin.internal.ResinExistingLocalConfigurationCapability;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.spi.configuration.AbstractExistingLocalConfiguration;

import java.io.File;

/**
 * Resin existing {@link org.codehaus.cargo.container.configuration.Configuration} implementation.
 *  
 * @version $Id$
 */
public class ResinExistingLocalConfiguration extends AbstractExistingLocalConfiguration 
{
    /**
     * Capability of the Resin standalone configuration.
     */
    private static ConfigurationCapability capability =
        new ResinExistingLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see AbstractExistingLocalConfiguration#AbstractExistingLocalConfiguration(String)
     */
    public ResinExistingLocalConfiguration(String dir)
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
     * @see org.codehaus.cargo.container.spi.configuration.AbstractLocalConfiguration#doConfigure(org.codehaus.cargo.container.LocalContainer)
     */
    protected void doConfigure(LocalContainer container) throws Exception
    {
        InstalledLocalContainer resinContainer = (InstalledLocalContainer) container;

        File webappsDir = new File(getHome(), "webapps");

        if (!webappsDir.exists())
        {
            throw new ContainerException("Invalid existing configuration: The ["
                + webappsDir.getPath() + "] directory does not exist");
        }

        ResinInstalledLocalDeployer deployer = new ResinInstalledLocalDeployer(resinContainer);
        deployer.deploy(getDeployables());

        // Deploy the CPC (Cargo Ping Component) to the webapps directory.
        getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war",
            new File(webappsDir, "cargocpc.war"));
    }

    /**
     * {@inheritDoc}
     * @see Object#toString()
     */
    public String toString()
    {
        return "Resin Existing Configuration";
    }
}
