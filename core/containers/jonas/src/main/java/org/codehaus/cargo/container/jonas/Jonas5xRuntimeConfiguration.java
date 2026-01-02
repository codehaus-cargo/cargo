/*
 * ========================================================================
 *
 * Copyright 2007-2008 OW2. Code from this file
 * was originally imported from the OW2 JOnAS project.
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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
package org.codehaus.cargo.container.jonas;

import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.jonas.internal.Jonas5xRuntimeConfigurationCapability;

/**
 * Configuration to use when using a JOnAS remote container.
 */
public class Jonas5xRuntimeConfiguration extends Jonas4xRuntimeConfiguration
{
    /**
     * Capability of the JOnAS runtime configuration.
     */
    private static final ConfigurationCapability CAPABILITY =
        new Jonas5xRuntimeConfigurationCapability();

    /**
     * Creates the configuration and saves the default values of options.
     */
    public Jonas5xRuntimeConfiguration()
    {
        setProperty(JonasPropertySet.JONAS_UNDEPLOY_IGNORE_VERSION, "false");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConfigurationCapability getCapability()
    {
        return CAPABILITY;
    }
}
