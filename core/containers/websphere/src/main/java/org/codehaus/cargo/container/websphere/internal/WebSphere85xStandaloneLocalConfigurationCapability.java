/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol, 2012-2017 Ali Tokmen.
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

import org.codehaus.cargo.container.property.DatasourcePropertySet;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ResourcePropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.websphere.WebSpherePropertySet;

/**
 * Capabilities of WebSphere 8.5.x standalone local configuration.
 */
public class WebSphere85xStandaloneLocalConfigurationCapability
    extends AbstractStandaloneLocalConfigurationCapability
{
    /**
     * Initialize the configuration-specific supports Map.
     */
    public WebSphere85xStandaloneLocalConfigurationCapability()
    {
        this.propertySupportMap.put(ServletPropertySet.USERS, Boolean.TRUE);
        this.propertySupportMap.put(GeneralPropertySet.PROTOCOL, Boolean.FALSE);

        this.propertySupportMap.put(WebSpherePropertySet.ADMIN_USERNAME, Boolean.TRUE);
        this.propertySupportMap.put(WebSpherePropertySet.ADMIN_PASSWORD, Boolean.TRUE);

        this.propertySupportMap.put(WebSpherePropertySet.PROFILE, Boolean.TRUE);
        this.propertySupportMap.put(WebSpherePropertySet.NODE, Boolean.TRUE);
        this.propertySupportMap.put(WebSpherePropertySet.CELL, Boolean.TRUE);
        this.propertySupportMap.put(WebSpherePropertySet.SERVER, Boolean.TRUE);

        this.propertySupportMap.put(WebSpherePropertySet.CLASSLOADER_MODE, Boolean.TRUE);
        this.propertySupportMap.put(WebSpherePropertySet.WAR_CLASSLOADER_POLICY, Boolean.TRUE);

        this.propertySupportMap.put(WebSpherePropertySet.JMS_SIBUS, Boolean.TRUE);
        this.propertySupportMap.put(WebSpherePropertySet.EJB_TO_ACT_SPEC_BINDING, Boolean.TRUE);
        this.propertySupportMap.put(WebSpherePropertySet.EJB_TO_RES_REF_BINDING, Boolean.TRUE);
        this.propertySupportMap.put(WebSpherePropertySet.APPLICATION_SECURITY, Boolean.TRUE);
        this.propertySupportMap.put(WebSpherePropertySet.GLOBAL_SECURITY_PROPERTIES, Boolean.TRUE);
        this.propertySupportMap.put(WebSpherePropertySet.SESSION_MANAGEMENT_PROPERTIES,
                Boolean.TRUE);
        this.propertySupportMap.put(WebSpherePropertySet.LOGGING_ROLLOVER, Boolean.TRUE);
        this.propertySupportMap.put(WebSpherePropertySet.JYTHON_SCRIPT_OFFLINE, Boolean.TRUE);
        this.propertySupportMap.put(WebSpherePropertySet.JYTHON_SCRIPT_ONLINE, Boolean.TRUE);

        this.propertySupportMap.put(DatasourcePropertySet.DATASOURCE, Boolean.TRUE);
        this.propertySupportMap.put(DatasourcePropertySet.TRANSACTION_SUPPORT, Boolean.TRUE);
        this.propertySupportMap.put(ResourcePropertySet.RESOURCE, Boolean.TRUE);

        this.propertySupportMap.put(WebSpherePropertySet.ADMINISTRATION_PORT, Boolean.TRUE);

        this.propertySupportMap.put(WebSpherePropertySet.ONLINE_DEPLOYMENT, Boolean.TRUE);
    }
}
