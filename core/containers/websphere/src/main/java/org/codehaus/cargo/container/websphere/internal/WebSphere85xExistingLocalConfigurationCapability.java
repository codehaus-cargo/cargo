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
package org.codehaus.cargo.container.websphere.internal;

import java.util.Map;

import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractExistingLocalConfigurationCapability;
import org.codehaus.cargo.container.websphere.WebSpherePropertySet;

/**
 * Capabilities of WebSphere's existing local configuration.
 *
 * @version $Id$
 */
public class WebSphere85xExistingLocalConfigurationCapability
    extends AbstractExistingLocalConfigurationCapability
{

    /**
     * Initialize the configuration-specific supports Map.
     */
    public WebSphere85xExistingLocalConfigurationCapability()
    {
        super();

        this.defaultSupportsMap.put(ServletPropertySet.USERS, Boolean.FALSE);
        this.defaultSupportsMap.put(GeneralPropertySet.PROTOCOL, Boolean.FALSE);

        this.defaultSupportsMap.put(WebSpherePropertySet.ADMIN_USERNAME, Boolean.TRUE);
        this.defaultSupportsMap.put(WebSpherePropertySet.ADMIN_PASSWORD, Boolean.TRUE);

        this.defaultSupportsMap.put(WebSpherePropertySet.PROFILE, Boolean.TRUE);
        this.defaultSupportsMap.put(WebSpherePropertySet.NODE, Boolean.TRUE);
        this.defaultSupportsMap.put(WebSpherePropertySet.CELL, Boolean.TRUE);
        this.defaultSupportsMap.put(WebSpherePropertySet.SERVER, Boolean.TRUE);

        this.defaultSupportsMap.put(WebSpherePropertySet.PROCESSOR_ARCH, Boolean.TRUE);
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
