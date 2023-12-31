/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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
package org.codehaus.cargo.container.websphere;

import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.websphere.internal.WebSphere9xStandaloneLocalConfigurationCapability;

/**
 * WebSphere 9.x standalone
 * {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration} implementation.
 */
public class WebSphere9xStandaloneLocalConfiguration
    extends WebSphere85xStandaloneLocalConfiguration
{
    /**
     * Capability of the WebSphere standalone configuration.
     */
    private static ConfigurationCapability capability =
        new WebSphere9xStandaloneLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see WebSphere85xStandaloneLocalConfiguration#WebSphere85xStandaloneLocalConfiguration(String)
     */
    public WebSphere9xStandaloneLocalConfiguration(String dir)
    {
        super(dir);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConfigurationCapability getCapability()
    {
        return capability;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "WebSphere 9.x Standalone Configuration";
    }
}
