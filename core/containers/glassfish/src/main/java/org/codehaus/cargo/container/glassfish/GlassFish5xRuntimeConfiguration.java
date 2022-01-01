/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2022 Ali Tokmen.
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
package org.codehaus.cargo.container.glassfish;

import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.glassfish.internal.GlassFish5xRuntimeConfigurationCapability;

/**
 * Configuration to use when using a GlassFish 5.x remote container.
 */
public class GlassFish5xRuntimeConfiguration extends GlassFish4xRuntimeConfiguration
{
    /**
     * Capability of the GlassFish runtime configuration.
     */
    private static ConfigurationCapability capability =
        new GlassFish5xRuntimeConfigurationCapability();

    /**
     * {@inheritDoc}
     */
    @Override
    public ConfigurationCapability getCapability()
    {
        return capability;
    }

}
