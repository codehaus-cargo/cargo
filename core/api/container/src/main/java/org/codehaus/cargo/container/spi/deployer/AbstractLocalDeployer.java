/*
 * ========================================================================
 *
 * Copyright 2005-2006 Vincent Massol.
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
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.DefaultFileHandler;

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
     * File utility class.
     */
    private FileHandler fileHandler;

    /**
     * @param container the local installed container into which to perform deployment operations
     */
    public AbstractLocalDeployer(LocalContainer container)
    {
        super();
        this.container = container;
        this.fileHandler = new DefaultFileHandler();

        setLogger(container.getLogger());
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
        return this.fileHandler;
    }

    /**
     * @param fileHandler the Cargo file utility class to use. This method is useful for unit
     *        testing with Mock objects as it can be passed a test file handler that doesn't perform
     *        any real file action.
     */
    protected void setFileHandler(FileHandler fileHandler)
    {
        this.fileHandler = fileHandler;
    }
}
