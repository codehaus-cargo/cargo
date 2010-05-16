/*
 * ========================================================================
 *
 * Copyright 2005-2006 Vincent Massol.
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
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfigurationCapability;

/**
 * GlassFish standalone local configuration capability.
 * 
 * @version $Id$
 */
public final class GlassFishStandaloneLocalConfigurationCapability extends
    AbstractStandaloneLocalConfigurationCapability
{

    /**
     * Initialize the configuration-specific supports Map.
     */
    public GlassFishStandaloneLocalConfigurationCapability()
    {
        super();

        // unsupported
        this.defaultSupportsMap.put(GeneralPropertySet.LOGGING, Boolean.FALSE);
        this.defaultSupportsMap.put(GeneralPropertySet.LOGGING, Boolean.FALSE);
        this.defaultSupportsMap.put(ServletPropertySet.USERS, Boolean.FALSE);

        // recognize those
        this.defaultSupportsMap.put(RemotePropertySet.USERNAME, Boolean.TRUE);
        this.defaultSupportsMap.put(RemotePropertySet.PASSWORD, Boolean.TRUE);
        this.defaultSupportsMap.put(GeneralPropertySet.HOSTNAME, Boolean.TRUE);
        this.defaultSupportsMap.put(GeneralPropertySet.JVMARGS, Boolean.TRUE);

        // this.defaultSupportsMap.put(ServletPropertySet.PORT, Boolean.TRUE);
    }

    /**
     * {@inheritDoc}
     * 
     * @see AbstractStandaloneLocalConfigurationCapability#getPropertySupportMap()
     */
    @Override
    protected Map < String, Boolean > getPropertySupportMap()
    {
        return this.defaultSupportsMap;
    }

}
