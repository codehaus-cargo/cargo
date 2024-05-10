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
package org.codehaus.cargo.container.spi.configuration;

import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;

/**
 * Base implementation of {@link org.codehaus.cargo.container.configuration.ConfigurationCapability}
 * that needs to be extended by each local configuration's implementation.
 */
public abstract class AbstractLocalConfigurationCapability extends AbstractConfigurationCapability
{
    /**
     * {@inheritDoc}
     * @see AbstractConfigurationCapability#AbstractConfigurationCapability()
     */
    public AbstractLocalConfigurationCapability()
    {
        this.propertySupportMap.put(GeneralPropertySet.PROTOCOL, Boolean.FALSE);
        this.propertySupportMap.put(GeneralPropertySet.HOSTNAME, Boolean.TRUE);
        this.propertySupportMap.put(ServletPropertySet.PORT, Boolean.TRUE);
        this.propertySupportMap.put(GeneralPropertySet.JAVA_HOME, Boolean.TRUE);
        this.propertySupportMap.put(GeneralPropertySet.JVMARGS, Boolean.TRUE);
        this.propertySupportMap.put(GeneralPropertySet.RUNTIME_ARGS, Boolean.TRUE);
        this.propertySupportMap.put(GeneralPropertySet.START_JVMARGS, Boolean.TRUE);
        this.propertySupportMap.put(GeneralPropertySet.SPAWN_PROCESS, Boolean.TRUE);
        this.propertySupportMap.put(GeneralPropertySet.PORT_OFFSET, Boolean.TRUE);
    }
}
