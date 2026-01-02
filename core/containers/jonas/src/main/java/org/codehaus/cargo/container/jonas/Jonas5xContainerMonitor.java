/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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

import org.codehaus.cargo.container.spi.startup.AbstractContainerMonitor;

/**
 * JOnAS 5.x monitor checking if JOnAS server reaches a given state.
 */
public class Jonas5xContainerMonitor extends AbstractContainerMonitor
{
    /**
     * Constructor.
     * 
     * @param container Container to be monitored.
     */
    public Jonas5xContainerMonitor(Jonas5xInstalledLocalContainer container)
    {
        super(container);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRunning()
    {
        return ((Jonas5xInstalledLocalContainer) getContainer()).ping() == 0;
    }
}
