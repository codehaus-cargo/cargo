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
package org.codehaus.cargo.container.websphere;

import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.websphere.internal.WebSphere9xExistingLocalConfigurationCapability;

/**
 * WebSphere 9.x existing {@link org.codehaus.cargo.container.configuration.Configuration}
 * implementation.
 */
public class WebSphere9xExistingLocalConfiguration extends WebSphere85xExistingLocalConfiguration
{
    /**
     * Capability of the WebSphere existing configuration.
     */
    private static final ConfigurationCapability CAPABILITY =
        new WebSphere9xExistingLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see WebSphere85xExistingLocalConfiguration#WebSphere85xExistingLocalConfiguration(String)
     */
    public WebSphere9xExistingLocalConfiguration(String dir)
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
    public String toString()
    {
        return "WebSphere 9.x Existing Configuration";
    }
}
