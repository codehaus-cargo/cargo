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
package org.codehaus.cargo.container.spi.startup;

import java.net.URL;

import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.internal.util.HttpUtils;
import org.codehaus.cargo.container.property.GeneralPropertySet;

/**
 * Abstract implementation of monitor checking container status by pinging URL.
 */
public abstract class AbstractPingContainerMonitor extends AbstractContainerMonitor
{
    /**
     * HTTP utils.
     */
    private HttpUtils httpUtils;

    /**
     * Constructor.
     * 
     * @param container Container to be monitored.
     */
    public AbstractPingContainerMonitor(Container container)
    {
        super(container);
        httpUtils = new HttpUtils();
        httpUtils.setLogger(container.getLogger());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRunning()
    {
        URL pingUrl = getPingUrl();
        return httpUtils.ping(pingUrl);
    }

    /**
     * @return URL to be pinged.
     */
    protected abstract URL getPingUrl();

    /**
     * Returns port with offset.
     * 
     * @param portProperty Port property.
     * @return Port value with offset.
     */
    protected int getPortWithOffset(String portProperty)
    {
        String portOffset = getConfiguration().getPropertyValue(GeneralPropertySet.PORT_OFFSET);

        boolean portOffsetApplicable = portOffset != null && !portOffset.equals("0");
        boolean portOffsetApplied = getConfiguration().isOffsetApplied();
        boolean applyPortOffset = portOffsetApplicable && !portOffsetApplied;

        int portValue = Integer.parseInt(getConfiguration().getPropertyValue(portProperty));

        if (applyPortOffset)
        {
            int portOffsetValue = Integer.parseInt(portOffset);
            portValue = portValue + portOffsetValue;
        }

        return portValue;
    }
}
