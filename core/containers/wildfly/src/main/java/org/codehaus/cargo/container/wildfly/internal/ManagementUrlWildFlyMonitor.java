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
package org.codehaus.cargo.container.wildfly.internal;

import java.net.MalformedURLException;
import java.net.URL;

import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.jboss.JBossPropertySet;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.spi.startup.AbstractPingContainerMonitor;
import org.codehaus.cargo.util.CargoException;

/**
 * WildFly monitor checking if management URL is available.
 */
public class ManagementUrlWildFlyMonitor extends AbstractPingContainerMonitor
{
    /**
     * Constructor.
     * 
     * @param container Container to be monitored.
     */
    public ManagementUrlWildFlyMonitor(Container container)
    {
        super(container);
    }

    /**
     * @return Management console URL for WildFly.
     */
    @Override
    protected URL getPingUrl()
    {
        String protocolProperty = getConfiguration().getPropertyValue(GeneralPropertySet.PROTOCOL);
        String hostnameProperty = getConfiguration().getPropertyValue(GeneralPropertySet.HOSTNAME);
        int managementPort = getPortWithOffset(JBossPropertySet.JBOSS_MANAGEMENT_HTTP_PORT);

        try
        {
            return new URL(protocolProperty, hostnameProperty, managementPort, "/console");
        }
        catch (MalformedURLException e)
        {
            throw new CargoException("Unable to construct management console URL.", e);
        }
    }
}
