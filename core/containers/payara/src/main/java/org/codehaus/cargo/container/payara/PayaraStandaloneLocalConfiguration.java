/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2023 Ali Tokmen.
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
package org.codehaus.cargo.container.payara;

import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.glassfish.GlassFish5xStandaloneLocalConfiguration;
import org.codehaus.cargo.container.payara.internal.PayaraStandaloneLocalConfigurationCapability;

/**
 * Payara standalone local configuration.
 */
public class PayaraStandaloneLocalConfiguration
    extends GlassFish5xStandaloneLocalConfiguration
{

    /**
     * Container capability instance.
     */
    private static final ConfigurationCapability CAPABILITY =
        new PayaraStandaloneLocalConfigurationCapability();

    /**
     * Creates the local configuration object.
     * 
     * @param home The work directory where files needed to run Payara will be created.
     */
    public PayaraStandaloneLocalConfiguration(String home)
    {
        super(home);
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
