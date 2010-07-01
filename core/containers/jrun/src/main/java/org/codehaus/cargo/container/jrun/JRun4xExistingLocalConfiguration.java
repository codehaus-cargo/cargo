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
package org.codehaus.cargo.container.jrun;

import java.io.File;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.jrun.internal.JRun4xExistingLocalConfigurationCapability;
import org.codehaus.cargo.container.spi.configuration.AbstractExistingLocalConfiguration;

/**
 * JRun existing {@link org.codehaus.cargo.container.configuration.Configuration} implementation.
 *  
 * @version $Id$
 */
public class JRun4xExistingLocalConfiguration extends AbstractExistingLocalConfiguration 
{
    /**
     * Capability of the JRun standalone configuration.
     */
    private static ConfigurationCapability capability = 
        new JRun4xExistingLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see AbstractExistingLocalConfiguration#AbstractExistingLocalConfiguration(String)
     */
    public JRun4xExistingLocalConfiguration(String dir)
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
    @Override
    protected void doConfigure(LocalContainer container) throws Exception
    {
        // set default server instance if none defined.
        if (getPropertyValue(JRun4xPropertySet.SERVER_NAME) == null)
        {
            setProperty(JRun4xPropertySet.SERVER_NAME, JRun4xPropertySet.DEFAULT_SERVER_NAME);
        }

        InstalledLocalContainer jrunContainer = (InstalledLocalContainer) container;
        JRun4xInstalledLocalDeployer deployer = new JRun4xInstalledLocalDeployer(jrunContainer);
        deployer.deploy(getDeployables());        

        // Deploy the CPC (Cargo Ping Component) to the webapps directory.
        getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war",
            new File(deployer.getDeployableDir(), "cargocpc.war"));
    }

    /**
     * {@inheritDoc}
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        return "JRun 4x Existing Configuration";
    }
}
