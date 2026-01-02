/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.jrun.internal.JRun4xExistingLocalConfigurationCapability;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractExistingLocalConfiguration;

/**
 * JRun existing {@link org.codehaus.cargo.container.configuration.Configuration} implementation.
 */
public class JRun4xExistingLocalConfiguration extends AbstractExistingLocalConfiguration
{
    /**
     * Capability of the JRun standalone configuration.
     */
    private static final ConfigurationCapability CAPABILITY =
        new JRun4xExistingLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see AbstractExistingLocalConfiguration#AbstractExistingLocalConfiguration(String)
     */
    public JRun4xExistingLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(GeneralPropertySet.RMI_PORT, "2999");
        setProperty(ServletPropertySet.PORT, JRun4xPropertySet.DEFAULT_PORT);
        setProperty(JRun4xPropertySet.SERVER_NAME, JRun4xPropertySet.DEFAULT_SERVER_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConfigurationCapability getCapability()
    {
        return CAPABILITY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doConfigure(LocalContainer container) throws Exception
    {
        InstalledLocalContainer jrunContainer = (InstalledLocalContainer) container;
        JRun4xInstalledLocalDeployer deployer = new JRun4xInstalledLocalDeployer(jrunContainer);
        deployer.redeploy(getDeployables());

        // Deploy the CPC (Cargo Ping Component) to the webapps directory.
        getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war",
            new File(deployer.getDeployableDir(null), "cargocpc.war"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "JRun 4x Existing Configuration";
    }
}
