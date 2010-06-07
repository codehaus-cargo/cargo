/* 
 * ========================================================================
 * 
 * Copyright 2006-2008 Vincent Massol.
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

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.jetty.internal.AbstractJettyStandaloneLocalConfiguration;
import org.codehaus.cargo.container.jetty.internal.Jetty7xEmbeddedStandaloneLocalConfigurationCapability;

/**
 * A mostly canned configuration for an embedded Jetty 7.x instance.
 * 
 * @version $Id$
 */
public class Jetty7xEmbeddedStandaloneLocalConfiguration extends
    AbstractJettyStandaloneLocalConfiguration
{
    /**
     * capabilities supported by this config.
     */
    private static Jetty7xEmbeddedStandaloneLocalConfigurationCapability capability =
        new Jetty7xEmbeddedStandaloneLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see AbstractJettyStandaloneLocalConfiguration#AbstractJettyStandaloneLocalConfiguration(String)
     */
    public Jetty7xEmbeddedStandaloneLocalConfiguration(String dir)
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
     * @see org.codehaus.cargo.container.jetty.internal.AbstractJettyStandaloneLocalConfiguration#activateLogging(org.codehaus.cargo.container.LocalContainer)
     */
    protected void activateLogging(LocalContainer container)
    {
        getLogger().info("Jetty7x log configuration not implemented",
            Jetty7xEmbeddedStandaloneLocalConfiguration.class.getName());
    }

    /**
     * {@inheritDoc}
     * @see Object#toString()
     */
    public String toString()
    {
        return "Jetty 7.x Embedded Standalone Configuration";
    }
}
