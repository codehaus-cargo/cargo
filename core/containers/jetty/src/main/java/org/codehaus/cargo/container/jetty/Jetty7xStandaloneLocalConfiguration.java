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

import org.apache.tools.ant.types.FilterChain;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.jetty.internal.AbstractJettyStandaloneLocalConfiguration;
import org.codehaus.cargo.container.jetty.internal.Jetty7xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.spi.deployer.AbstractCopyingInstalledLocalDeployer;

/**
 * Jetty 7.x standalone
 * {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration} implementation.
 * 
 * @version $Id$
 */
public class Jetty7xStandaloneLocalConfiguration extends
    AbstractJettyStandaloneLocalConfiguration
{
    /**
     * Capability of the Jetty 7.x standalone local configuration.
     */
    private static ConfigurationCapability capability =
        new Jetty7xStandaloneLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see AbstractJettyStandaloneLocalConfiguration#AbstractJettyStandaloneLocalConfiguration(String)
     */
    public Jetty7xStandaloneLocalConfiguration(String dir)
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

    @Override
    protected FilterChain createJettyFilterChain()
    {
        FilterChain filterChain = createFilterChain();

        String sessionPath = getPropertyValue(JettyPropertySet.SESSION_PATH);
        String sessionContextParam = "";
        if (sessionPath != null)
        {
            sessionContextParam =
                "  <context-param>\n"
                    + "    <param-name>org.eclipse.jetty.servlet.SessionPath</param-name>\n"
                    + "    <param-value>" + sessionPath + "</param-value>\n"
                    + "  </context-param>\n";
        }

        getAntUtils().addTokenToFilterChain(filterChain,
            "cargo.jetty.session.path.context-param", sessionContextParam);

        return filterChain;
    }

    @Override
    protected AbstractCopyingInstalledLocalDeployer createDeployer(
        InstalledLocalContainer container)
    {
        Jetty7xInstalledLocalDeployer deployer = new Jetty7xInstalledLocalDeployer(container);
        deployer.setShouldDeployExpandedWARs(true);
        return deployer;
    }

    /**
     * {@inheritDoc}
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        return "Jetty 7.x Standalone Configuration";
    }

}
