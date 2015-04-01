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
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.jetty.internal.AbstractJettyEmbeddedStandaloneLocalConfiguration;
import org.codehaus.cargo.container.jetty.internal.Jetty6xEmbeddedStandaloneLocalConfigurationCapability;

/**
 * A mostly canned configuration for an embedded Jetty 7.x instance.
 * 
 */
public class Jetty7xEmbeddedStandaloneLocalConfiguration extends
    AbstractJettyEmbeddedStandaloneLocalConfiguration
{
    /**
     * capabilities supported by this config.
     */
    private static ConfigurationCapability capability =
        new Jetty6xEmbeddedStandaloneLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see AbstractJettyEmbeddedStandaloneLocalConfiguration#AbstractJettyEmbeddedStandaloneLocalConfiguration(String)
     */
    public Jetty7xEmbeddedStandaloneLocalConfiguration(String dir)
    {
        super(dir);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.configuration.Configuration#getCapability()
     */
    @Override
    public ConfigurationCapability getCapability()
    {
        return capability;
    }

    @Override
    protected FilterChain createJettyFilterChain()
    {
        FilterChain filterChain = super.createJettyFilterChain();

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

    /**
     * {@inheritDoc}
     * @see AbstractJettyEmbeddedStandaloneLocalConfiguration#activateLogging(org.codehaus.cargo.container.LocalContainer)
     */
    @Override
    protected void activateLogging(LocalContainer container)
    {
        getLogger().info("Jetty7x log configuration not implemented", this.getClass().getName());
    }

    /**
     * {@inheritDoc}
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        return "Jetty 7.x Embedded Standalone Configuration";
    }
}
