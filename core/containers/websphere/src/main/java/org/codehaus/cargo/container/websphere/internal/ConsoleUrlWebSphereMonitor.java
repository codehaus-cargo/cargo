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
package org.codehaus.cargo.container.websphere.internal;

import java.net.MalformedURLException;
import java.net.URL;

import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.spi.startup.AbstractPingContainerMonitor;
import org.codehaus.cargo.container.websphere.WebSpherePropertySet;
import org.codehaus.cargo.util.CargoException;

/**
 * WebSphere monitor checking if console URL is available.
 */
public class ConsoleUrlWebSphereMonitor extends AbstractPingContainerMonitor
{
    /**
     * Constructor.
     * 
     * @param container Container to be monitored.
     */
    public ConsoleUrlWebSphereMonitor(Container container)
    {
        super(container);
    }

    /**
     * @return Console URL for WebSphere.
     */
    @Override
    protected URL getPingUrl()
    {
        String protocolProperty = getConfiguration().getPropertyValue(GeneralPropertySet.PROTOCOL);
        String hostnameProperty = getConfiguration().getPropertyValue(GeneralPropertySet.HOSTNAME);
        int administrationPort = getPortWithOffset(WebSpherePropertySet.ADMINISTRATION_PORT);

        try
        {
            return new URL(protocolProperty, hostnameProperty, administrationPort, "/ibm/console");
        }
        catch (MalformedURLException e)
        {
            throw new CargoException("Unable to construct console URL.", e);
        }
    }
}
