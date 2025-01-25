/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
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
package org.codehaus.cargo.container.tomcat;

import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractRuntimeConfiguration;
import org.codehaus.cargo.container.tomcat.internal.Tomcat4x5x6xRuntimeConfigurationCapability;

/**
 * Configuration to use when using a
 * {@link org.codehaus.cargo.container.tomcat.Tomcat4xRemoteContainer}.
 */
public class Tomcat4xRuntimeConfiguration extends AbstractRuntimeConfiguration
{
    /**
     * Default timeout for Tomcat (in milliseconds).
     */
    private static final long TWO_HOURS = 2 * 60 * 60 * 1000;

    /**
     * Capability of the Tomcat runtime configuration.
     */
    private static final ConfigurationCapability CAPABILITY =
        new Tomcat4x5x6xRuntimeConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see AbstractRuntimeConfiguration#AbstractRuntimeConfiguration()
     */
    public Tomcat4xRuntimeConfiguration()
    {
        setProperty(TomcatPropertySet.DEPLOY_UPDATE, "false");
        setProperty(RemotePropertySet.TIMEOUT, Long.toString(TWO_HOURS));
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
    public String toString()
    {
        return "Tomcat 4.x Runtime Configuration";
    }
}
