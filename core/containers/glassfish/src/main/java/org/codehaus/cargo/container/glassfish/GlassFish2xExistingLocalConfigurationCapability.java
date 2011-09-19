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
package org.codehaus.cargo.container.glassfish;

import java.util.Map;

import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractExistingLocalConfigurationCapability;

/**
 * GlassFish 2.x existing local configuration capability.
 * 
 * @version $Id$
 */
public class GlassFish2xExistingLocalConfigurationCapability extends
    AbstractExistingLocalConfigurationCapability
{

    /**
     * Initialize the configuration-specific supports Map.
     */
    public GlassFish2xExistingLocalConfigurationCapability()
    {
        super();

        this.defaultSupportsMap.remove(GeneralPropertySet.PROTOCOL);
        this.defaultSupportsMap.remove(GeneralPropertySet.JAVA_HOME);

        this.defaultSupportsMap.put(RemotePropertySet.USERNAME, Boolean.TRUE);
        this.defaultSupportsMap.put(RemotePropertySet.PASSWORD, Boolean.TRUE);
        this.defaultSupportsMap.put(GlassFishPropertySet.ADMIN_PORT, Boolean.TRUE);
        this.defaultSupportsMap.put(GlassFishPropertySet.DOMAIN_NAME, Boolean.TRUE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, Boolean> getPropertySupportMap()
    {
        return this.defaultSupportsMap;
    }

}
