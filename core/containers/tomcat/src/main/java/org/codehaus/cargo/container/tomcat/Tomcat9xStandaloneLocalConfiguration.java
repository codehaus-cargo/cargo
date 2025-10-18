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

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.tomcat.internal.Tomcat9xStandaloneLocalConfigurationCapability;

/**
 * Catalina standalone {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration}
 * implementation.
 */
public class Tomcat9xStandaloneLocalConfiguration extends Tomcat8xStandaloneLocalConfiguration
{
    /**
     * Tomcat configuration capability.
     */
    private static final ConfigurationCapability CAPABILITY =
        new Tomcat9xStandaloneLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see Tomcat8xStandaloneLocalConfiguration#Tomcat8xStandaloneLocalConfiguration(String)
     */
    public Tomcat9xStandaloneLocalConfiguration(String dir)
    {
        super(dir);
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
    protected void performXmlReplacements(LocalContainer container)
    {
        if (getPropertyValue(TomcatPropertySet.CONNECTOR_MAX_PART_COUNT) != null)
        {
            addXmlReplacement("conf/server.xml", connectorXpath(), "maxPartCount",
                TomcatPropertySet.CONNECTOR_MAX_PART_COUNT);
        }
        super.performXmlReplacements(container);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "Tomcat 9.x Standalone Configuration";
    }
}
