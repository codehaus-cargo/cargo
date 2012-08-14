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
package org.codehaus.cargo.container.spi.deployer;

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.util.CargoException;
import org.codehaus.cargo.util.FileHandler;

/**
 * Base deployer to deploy to local containers (installed or embedded).
 * 
 * @version $Id$
 */
public abstract class AbstractLocalDeployer extends AbstractDeployer
{
    /**
     * Local installed container into which to perform deployment operations.
     */
    private LocalContainer container;

    /**
     * @param container the local installed container into which to perform deployment operations
     */
    public AbstractLocalDeployer(LocalContainer container)
    {
        super(container);
        this.container = container;

        String configurationHome = container.getConfiguration().getHome();
        if (configurationHome != null && !getFileHandler().isDirectory(configurationHome))
        {
            throw new CargoException("The container configuration directory \""
                + configurationHome + "\" does not exist. Please configure the container before "
                + "attempting to perform any local deployment. Read more on: "
                + "http://cargo.codehaus.org/Local+Configuration");
        }
    }

    /**
     * @return the local container into which to perform deployment operations
     */
    protected LocalContainer getContainer()
    {
        return this.container;
    }

    /**
     * @return the Cargo file utility class
     */
    protected FileHandler getFileHandler()
    {
        return container.getFileHandler();
    }
}
