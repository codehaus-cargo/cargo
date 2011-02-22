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
package org.codehaus.cargo.container.jboss;

import java.io.File;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.jboss.internal.JBossExistingLocalConfigurationCapability;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractExistingLocalConfiguration;

/**
 * JBoss existing {@link org.codehaus.cargo.container.configuration.Configuration} implementation.
 * 
 * @version $Id$
 */
public class JBossExistingLocalConfiguration extends AbstractExistingLocalConfiguration
{
    /**
     * JBoss container capability.
     */
    private static ConfigurationCapability capability =
        new JBossExistingLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see AbstractExistingLocalConfiguration#AbstractExistingLocalConfiguration(String)
     */
    public JBossExistingLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(GeneralPropertySet.RMI_PORT, "1099");
        setProperty(JBossPropertySet.CONFIGURATION, "default");
        setProperty(JBossPropertySet.JBOSS_NAMING_PORT, "1098");
        setProperty(JBossPropertySet.JBOSS_CLASSLOADING_WEBSERVICE_PORT, "8083");
        setProperty(JBossPropertySet.JBOSS_JRMP_PORT, "1090");
        setProperty(JBossPropertySet.JBOSS_JRMP_INVOKER_PORT, "4444");
        setProperty(JBossPropertySet.JBOSS_INVOKER_POOL_PORT, "4445");
        setProperty(JBossPropertySet.JBOSS_REMOTING_TRANSPORT_PORT, "4446");
        setProperty(JBossPropertySet.JBOSS_EJB3_REMOTING_PORT, "3873");
        setProperty(JBossPropertySet.JBOSS_TRANSACTION_RECOVERY_MANAGER_PORT, "4712");
        setProperty(JBossPropertySet.JBOSS_TRANSACTION_STATUS_MANAGER_PORT, "4713");
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.configuration.AbstractLocalConfiguration#configure(LocalContainer)
     */
    @Override
    protected void doConfigure(LocalContainer container) throws Exception
    {
        InstalledLocalContainer jbossContainer = (InstalledLocalContainer) container;

        File deployDir = new File(getHome(), "deploy");

        if (!deployDir.exists())
        {
            throw new ContainerException("Invalid existing configuration: The ["
                + deployDir.getPath() + "] directory does not exist");
        }

        JBossInstalledLocalDeployer deployer = new JBossInstalledLocalDeployer(jbossContainer);
        deployer.deploy(getDeployables());

        // Deploy the CPC (Cargo Ping Component) to the deploy directory.
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
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        return "JBoss Existing Configuration";
    }
}
