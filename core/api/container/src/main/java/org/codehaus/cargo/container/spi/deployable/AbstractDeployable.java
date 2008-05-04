/*
 * ========================================================================
 *
 * Copyright 2006 Vincent Massol.
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
package org.codehaus.cargo.container.spi.deployable;

import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.util.log.LoggedObject;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.DefaultFileHandler;

/**
 * Common class for easy Deployable implementations.
 *
 * @version $Id$
 */
public abstract class AbstractDeployable extends LoggedObject implements Deployable
{
    /**
     * The location of the Deployable file being wrapped.
     */
    private String file;

    /**
     * File utility class.
     */
    private FileHandler fileHandler;
    
    /**
     * @param file the location of the deploybale file being wrapped.
     */
    public AbstractDeployable(String file)
    {
        this.file = file;
        this.fileHandler = new DefaultFileHandler();
    }

    /**
     * {@inheritDoc}
     * @see Deployable#getFile()
     */
    public String getFile()
    {
        return this.file;
    }

    /**
     * @return the Cargo file utility class
     */
    public FileHandler getFileHandler()
    {
        return this.fileHandler;
    }

    /**
     * @param fileHandler the Cargo file utility class to use. This method is useful for unit
     *        testing with Mock objects as it can be passed a test file handler that doesn't perform
     *        any real file action.
     */
    public void setFileHandler(FileHandler fileHandler)
    {
        this.fileHandler = fileHandler;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isExpanded()
    {
        return getFileHandler().isDirectory(getFile());
    }
}
