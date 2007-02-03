/*
 * ========================================================================
 * 
 * Copyright 2005-2006 Vincent Massol.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * ========================================================================
 */
package org.codehaus.cargo.container.jboss.internal;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.cargo.container.jboss.JBossPropertySet;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfigurationCapability;

/**
 * Capabilities of the JBoss's
 * {@link org.codehaus.cargo.container.jboss.JBossStandaloneLocalConfiguration}
 * configuration.
 * 
 * @version $Id$
 */
public class JBossStandaloneLocalConfigurationCapability extends
    AbstractStandaloneLocalConfigurationCapability
{
    /**
     * JBoss-specific configuration Map.
     */
    private Map propertySupportMap;

    /**
     * Initialize JBoss-specific configuration Map.
     */
    public JBossStandaloneLocalConfigurationCapability()
    {
        super();

        this.propertySupportMap = new HashMap();

        this.propertySupportMap.put(GeneralPropertySet.PROTOCOL, Boolean.FALSE);
        this.propertySupportMap.put(ServletPropertySet.USERS, Boolean.FALSE);
        this.propertySupportMap.put(JBossPropertySet.CONFIGURATION, Boolean.TRUE);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfigurationCapability#getPropertySupportMap()
     */
    protected Map getPropertySupportMap()
    {
        return this.propertySupportMap;
    }
}
