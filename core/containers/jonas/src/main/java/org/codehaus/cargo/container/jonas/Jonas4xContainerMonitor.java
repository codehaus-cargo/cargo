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
package org.codehaus.cargo.container.jonas;

import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.jonas.internal.Jonas4xAdmin;
import org.codehaus.cargo.container.spi.startup.AbstractContainerMonitor;

/**
 * JOnAS 4.x monitor checking if JOnAS server reaches a given state.
 */
public class Jonas4xContainerMonitor extends AbstractContainerMonitor
{
    /**
     * The JOnAS admin.
     */
    private Jonas4xAdmin jonasAdmin;

    /**
     * Whether to wait for container start or stop.
     */
    private boolean waitForStarting;

    /**
     * Constructor.
     * 
     * @param container Container to be monitored.
     * @param jonasAdmin The JOnAS admin.
     * @param waitForStarting Whether to wait for container start or stop.
     */
    public Jonas4xContainerMonitor(Container container, Jonas4xAdmin jonasAdmin,
        boolean waitForStarting)
    {
        super(container);
        this.jonasAdmin = jonasAdmin;
        this.waitForStarting = waitForStarting;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRunning()
    {
        if (waitForStarting)
        {
            // Wait for JOnAS to start by pinging
            // (to ensure all modules are deployed and ready)
            return jonasAdmin.isServerRunning("ping", 0);
        }
        else
        {
            // Wait for JOnAS to stop by listing JNDI
            return !jonasAdmin.isServerRunning("j", 2);
        }
    }
}
