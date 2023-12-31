/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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

import java.io.File;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.resin.internal.Resin3xExistingLocalConfigurationCapability;
import org.codehaus.cargo.container.resin.internal.ResinRun;
import org.codehaus.cargo.container.spi.configuration.AbstractExistingLocalConfiguration;

/**
 * Resin existing {@link org.codehaus.cargo.container.configuration.Configuration} implementation.
 */
public class Resin3xExistingLocalConfiguration extends AbstractExistingLocalConfiguration
{
    /**
     * Capability of the Resin standalone configuration.
     */
    private static ConfigurationCapability capability =
        new Resin3xExistingLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see AbstractExistingLocalConfiguration#AbstractExistingLocalConfiguration(String)
     */
    public Resin3xExistingLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(ResinPropertySet.SOCKETWAIT_PORT,
            Integer.toString(ResinRun.DEFAULT_KEEPALIVE_SOCKET_PORT));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConfigurationCapability getCapability()
    {
        return capability;
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
        deployer.redeploy(getDeployables());

        // Deploy the CPC (Cargo Ping Component) to the webapps directory.
        getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war",
            new File(webappsDir, "cargocpc.war"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "Resin Existing Configuration";
    }
}
