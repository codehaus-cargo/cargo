/*
 * ========================================================================
 *
 * Copyright 2007-2008 OW2. Code from this file
 * was originally imported from the OW2 JOnAS project.
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
package org.codehaus.cargo.container.jonas.internal;

import org.codehaus.cargo.container.jonas.JonasPropertySet;
import org.codehaus.cargo.container.property.DatasourcePropertySet;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfigurationCapability;

/**
 * Capabilities of the JOnAS standalone local configuration.
 */
public class JonasStandaloneLocalConfigurationCapability extends
    AbstractStandaloneLocalConfigurationCapability
{
    /**
     * Initialize Jonas-specific configuration Map.
     */
    public JonasStandaloneLocalConfigurationCapability()
    {
        this.propertySupportMap.remove(GeneralPropertySet.PROTOCOL);
        this.propertySupportMap.put(GeneralPropertySet.RMI_PORT, Boolean.TRUE);
        this.propertySupportMap.put(ServletPropertySet.USERS, Boolean.FALSE);

        this.propertySupportMap.put(GeneralPropertySet.RMI_PORT, Boolean.TRUE);
        this.propertySupportMap.put(JonasPropertySet.JONAS_SERVER_NAME, Boolean.TRUE);
        this.propertySupportMap.put(JonasPropertySet.JONAS_DOMAIN_NAME, Boolean.TRUE);
        this.propertySupportMap.put(JonasPropertySet.JONAS_SERVICES_LIST, Boolean.TRUE);
        this.propertySupportMap.put(JonasPropertySet.JONAS_JMS_PORT, Boolean.TRUE);
        this.propertySupportMap.put(DatasourcePropertySet.DATASOURCE, Boolean.TRUE);
        this.propertySupportMap.put(DatasourcePropertySet.TRANSACTION_SUPPORT, Boolean.TRUE);
        this.propertySupportMap.put(RemotePropertySet.USERNAME, Boolean.TRUE);
        this.propertySupportMap.put(RemotePropertySet.PASSWORD, Boolean.TRUE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportsProperty(String property)
    {
        if (property != null && property.startsWith(JonasPropertySet.CONFIGURATOR_PREFIX))
        {
            return true;
        }
        else
        {
            return super.supportsProperty(property);
        }
    }
}
