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
package org.codehaus.cargo.container.jetty;

import java.io.File;

import org.apache.tools.ant.types.FilterChain;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.jetty.internal.Jetty6xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration;

/**
 * Jetty 6.x standalone
 * {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration} implementation.
 * 
 * @version $Id$
 */
public class Jetty6xStandaloneLocalConfiguration extends AbstractStandaloneLocalConfiguration
{
    /**
     * Capability of the Jetty 6.x standalone local configuration.
     */
    private static ConfigurationCapability capability =
        new Jetty6xStandaloneLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see AbstractStandaloneLocalConfiguration#AbstractStandaloneLocalConfiguration(String)
     */
    public Jetty6xStandaloneLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(GeneralPropertySet.RMI_PORT, "8079");
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
        setupConfigurationDir();

        FilterChain filterChain = createFilterChain();
        String sessionPath = getPropertyValue(JettyPropertySet.SESSION_PATH);
        String sessionContextParam = "";

        if (sessionPath != null)
        {
            sessionContextParam = "  <context-param>\n"
                    + "    <param-name>org.mortbay.jetty.servlet.SessionPath</param-name>\n"
                    + "    <param-value>" + sessionPath + "</param-value>\n"
                    + "  </context-param>\n";
        }

        getAntUtils().addTokenToFilterChain(filterChain, "cargo.jetty.session.path.context-param",
                sessionContextParam);

        String etcDir = getFileHandler().createDirectory(getHome(), "etc");
        getResourceUtils().copyResource(RESOURCE_PATH + container.getId()
            + "/jetty.xml", new File(etcDir, "jetty.xml"));
        getResourceUtils().copyResource(RESOURCE_PATH + container.getId()
                + "/webdefault.xml", new File(etcDir, "webdefault.xml"), filterChain);

        // Create a webapps directory for automatic deployment of WARs dropped inside.
        String appDir = getFileHandler().createDirectory(getHome(), "webapps");

        // Create log directory
        getFileHandler().createDirectory(getHome(), "logs");

        // Create contexts directory for hot deployments
        getFileHandler().createDirectory(getHome(), "contexts");

        // Deploy all deployables into the webapps directory.
        Jetty6xInstalledLocalDeployer deployer =
            new Jetty6xInstalledLocalDeployer((InstalledLocalContainer) container);
        deployer.setShouldDeployExpandedWARs(true);
        deployer.deploy(getDeployables());

        // Deploy the CPC (Cargo Ping Component) to the webapps directory
        getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war",
            new File(appDir, "cargocpc.war"));
    }

    /**
     * {@inheritDoc}
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        return "Jetty 6.x Standalone Configuration";
    }

}
