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
package org.codehaus.cargo.container.weblogic.internal;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractExistingLocalConfigurationCapability;
import org.codehaus.cargo.container.weblogic.WebLogicPropertySet;

/**
 * Capabilities of the WebLogic's
 * {@link org.codehaus.cargo.container.weblogic.WebLogicConfiguration} configuration.
 * 
 * @version $Id$
 */
public class WebLogicExistingLocalConfigurationCapability
    extends AbstractExistingLocalConfigurationCapability
{
    /**
     * Configuration-specific supports Map.
     */
    private Map<String, Boolean> supportsMap;

    /**
     * Initialize the configuration-specific supports Map.
     */
    public WebLogicExistingLocalConfigurationCapability()
    {
        super();

        this.supportsMap = new HashMap<String, Boolean>();

        this.supportsMap.put(GeneralPropertySet.HOSTNAME, Boolean.TRUE);

        this.supportsMap.put(WebLogicPropertySet.ADMIN_USER, Boolean.TRUE);
        this.supportsMap.put(WebLogicPropertySet.ADMIN_PWD, Boolean.TRUE);

        this.supportsMap.put(WebLogicPropertySet.SERVER, Boolean.TRUE);
        this.supportsMap.put(WebLogicPropertySet.BEA_HOME, Boolean.TRUE);

    }

    /**
     * {@inheritDoc}
     * @see AbstractExistingLocalConfigurationCapability#getPropertySupportMap()
     */
    @Override
    protected Map<String, Boolean> getPropertySupportMap()
    {
        return this.supportsMap;
    }
}
