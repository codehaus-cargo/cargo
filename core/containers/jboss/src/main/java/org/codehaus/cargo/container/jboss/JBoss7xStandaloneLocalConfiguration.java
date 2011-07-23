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
package org.codehaus.cargo.container.jboss;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.jboss.internal.JBoss7xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration;
import org.codehaus.cargo.util.CargoException;

/**
 * 
 * @version $Id$
 */
public class JBoss7xStandaloneLocalConfiguration extends AbstractStandaloneLocalConfiguration
{

    /**
     * JBoss configuration used as base.
     */
    public static final String CONFIGURATION = "standalone";

    /**
     * JBoss container capability.
     */
    private static ConfigurationCapability capability =
        new JBoss7xStandaloneLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see AbstractStandaloneLocalConfiguration#AbstractStandaloneLocalConfiguration(String)
     */
    public JBoss7xStandaloneLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(GeneralPropertySet.RMI_PORT, "1099");
        setProperty(JBossPropertySet.JBOSS_JRMP_PORT, "1090");
        setProperty(JBossPropertySet.JBOSS_JMX_PORT, "1091");
        setProperty(JBossPropertySet.JBOSS_MANAGEMENT_PORT, "9999");
        setProperty(JBossPropertySet.JBOSS_OSGI_HTTP_PORT, "8090");
        setProperty(JBossPropertySet.JBOSS_REMOTING_TRANSPORT_PORT, "4447");
        setProperty(JBossPropertySet.CONFIGURATION, CONFIGURATION);
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
    @Override
    protected void doConfigure(LocalContainer c) throws Exception
    {
        if (!(c instanceof InstalledLocalContainer))
        {
            throw new CargoException("Only InstalledLocalContainers are supported, got "
                + c.getClass().getName());
        }

        InstalledLocalContainer container = (InstalledLocalContainer) c;

        getLogger().info("Configuring JBoss using the [" + CONFIGURATION
            + "] server configuration", this.getClass().getName());

        setupConfigurationDir();

        // Copy initial configuration
        String initialConfiguration = getFileHandler().append(container.getHome(), CONFIGURATION);
        getFileHandler().copyDirectory(initialConfiguration, getHome());

        String configurationXML = getFileHandler().append(getHome(),
            "/configuration/standalone.xml");
        if (!getFileHandler().exists(configurationXML))
        {
            throw new CargoException("Missing configuration XML file: " + configurationXML);
        }

        // TODO: Replace better
        Map<String, String> replacements = new HashMap<String, String>();
        replacements.put("8080", getPropertyValue(ServletPropertySet.PORT));
        replacements.put("1090", getPropertyValue(JBossPropertySet.JBOSS_JRMP_PORT));
        replacements.put("1091", getPropertyValue(JBossPropertySet.JBOSS_JMX_PORT));
        replacements.put("1099", getPropertyValue(GeneralPropertySet.RMI_PORT));
        replacements.put("9999", getPropertyValue(JBossPropertySet.JBOSS_MANAGEMENT_PORT));
        replacements.put("8090", getPropertyValue(JBossPropertySet.JBOSS_OSGI_HTTP_PORT));
        replacements.put("4447", getPropertyValue(JBossPropertySet.JBOSS_REMOTING_TRANSPORT_PORT));
        getFileHandler().replaceInFile(configurationXML, replacements, "UTF-8");

        // Deploy the CPC (Cargo Ping Component) to the deployments directory
        getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war",
            new File(getHome(), "/deployments/cargocpc.war"));

        // Deploy with user defined deployables with the appropriate deployer
        JBoss7xInstalledLocalDeployer deployer = new JBoss7xInstalledLocalDeployer(container);
        deployer.deploy(getDeployables());
    }

}
