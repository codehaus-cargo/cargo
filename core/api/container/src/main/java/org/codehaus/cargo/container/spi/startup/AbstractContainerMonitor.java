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

import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.startup.ContainerMonitor;
import org.codehaus.cargo.util.CargoException;
import org.codehaus.cargo.util.log.Logger;

/**
 * Abstract implementation of monitor used for checking container status.
 */
public abstract class AbstractContainerMonitor implements ContainerMonitor
{
    /**
     * Container to be monitored.
     */
    private LocalContainer container;

    /**
     * Configuration of monitored container.
     */
    private LocalConfiguration configuration;

    /**
     * Logger.
     */
    private Logger logger;

    /**
     * Constructor.
     * 
     * @param container Container to be monitored.
     */
    public AbstractContainerMonitor(Container container)
    {
        setLogger(container.getLogger());

        if (container instanceof LocalContainer)
        {
            this.container = (LocalContainer) container;
            this.configuration = ((LocalContainer) container).getConfiguration();
        }
        else
        {
            throw new CargoException("Container monitor is applicable just for local containers.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLogger(Logger logger)
    {
        this.logger = logger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Logger getLogger()
    {
        return logger;
    }

    /**
     * @return Container to be monitored.
     */
    protected LocalContainer getContainer()
    {
        return container;
    }

    /**
     * @return Configuration of monitored container.
     */
    protected LocalConfiguration getConfiguration()
    {
        return configuration;
    }
}
