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
package org.codehaus.cargo.container.spi.startup;

import java.util.Arrays;
import java.util.List;

import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.startup.ContainerMonitor;

/**
 * Monitor which gathers information from multiple monitors.
 */
public class CombinedContainerMonitor extends AbstractContainerMonitor
{

    /**
     * Underlying monitors
     */
    private final List<ContainerMonitor> monitors;

    /**
     * Constructor.
     * 
     * @param container Container to be monitored.
     * @param monitors Underlying monitors
     */
    public CombinedContainerMonitor(Container container, ContainerMonitor... monitors)
    {
        super(container);
        if (monitors == null || monitors.length == 0)
        {
            throw new IllegalArgumentException("Specify at least one monitor");
        }
        this.monitors = Arrays.asList(monitors);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRunning()
    {
        for (ContainerMonitor monitor : monitors)
        {
            if (!monitor.isRunning())
            {
                return false;
            }
        }
        return true;
    }

}
