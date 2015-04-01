/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol.
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
package org.codehaus.cargo.container.jboss.internal;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.cargo.container.jboss.JBossPropertySet;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractExistingLocalConfigurationCapability;

/**
 * Capabilities of the JBoss's
 * {@link org.codehaus.cargo.container.jboss.JBossExistingLocalConfiguration} configuration.
 * 
 */
public class JBoss7xExistingLocalConfigurationCapability extends
    AbstractExistingLocalConfigurationCapability
{
    /**
     * JBoss-specific configuration Map.
     */
    protected Map<String, Boolean> propertySupportMap;

    /**
     * Initialize JBoss-specific configuration Map.
     */
    public JBoss7xExistingLocalConfigurationCapability()
    {
        super();

        this.propertySupportMap = new HashMap<String, Boolean>();

        this.propertySupportMap.put(GeneralPropertySet.RMI_PORT, Boolean.TRUE);
        this.propertySupportMap.put(GeneralPropertySet.PROTOCOL, Boolean.TRUE);
        this.propertySupportMap.put(ServletPropertySet.PORT, Boolean.TRUE);
        this.propertySupportMap.put(JBossPropertySet.CONFIGURATION, Boolean.TRUE);
        this.propertySupportMap.put(JBossPropertySet.JBOSS_MANAGEMENT_NATIVE_PORT, Boolean.TRUE);
        this.propertySupportMap.put(JBossPropertySet.JBOSS_MANAGEMENT_HTTP_PORT, Boolean.FALSE);
        this.propertySupportMap.put(JBossPropertySet.ALTERNATIVE_MODULES_DIR,
            Boolean.TRUE);
        this.propertySupportMap.put(RemotePropertySet.USERNAME, Boolean.TRUE);
        this.propertySupportMap.put(RemotePropertySet.PASSWORD, Boolean.TRUE);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfigurationCapability#getPropertySupportMap()
     */
    @Override
    protected Map<String, Boolean> getPropertySupportMap()
    {
        return this.propertySupportMap;
    }
}
