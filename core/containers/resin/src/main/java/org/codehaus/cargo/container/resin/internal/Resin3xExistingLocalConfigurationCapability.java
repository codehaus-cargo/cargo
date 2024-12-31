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
package org.codehaus.cargo.container.resin.internal;

import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.resin.ResinPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractExistingLocalConfigurationCapability;

/**
 * Capabilities of the Resin's
 * {@link org.codehaus.cargo.container.resin.Resin3xExistingLocalConfiguration}
 * configuration.
 */
public class Resin3xExistingLocalConfigurationCapability
    extends AbstractExistingLocalConfigurationCapability
{
    /**
     * Initialize the configuration-specific supports Map.
     */
    public Resin3xExistingLocalConfigurationCapability()
    {
        this.propertySupportMap.put(GeneralPropertySet.PROTOCOL, Boolean.FALSE);
        this.propertySupportMap.put(GeneralPropertySet.HOSTNAME, Boolean.FALSE);

        this.propertySupportMap.put(ResinPropertySet.SOCKETWAIT_PORT, Boolean.TRUE);
    }
}
