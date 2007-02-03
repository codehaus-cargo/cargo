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
package org.codehaus.cargo.container.spi.packager;

import org.codehaus.cargo.container.packager.Packager;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.util.log.LoggedObject;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.DefaultFileHandler;

import java.util.List;
import java.util.ArrayList;

/**
 * Package a container distribution and its local configuration in a directory.
 *
 * @version $Id: Container.java 886 2006-02-28 12:40:47Z vmassol $
 */
public abstract class AbstractDirectoryPackager extends LoggedObject implements Packager
{
    /**
     * @see #getTargetDirectory()
     */
    private String targetDirectory;

    /**
     * File utility class.
     */
    private FileHandler fileHandler;

    /**
     * @param targetDirectory the directory where the container distribution and its local
     *        configuration will be packaged
     */
    public AbstractDirectoryPackager(String targetDirectory)
    {
        this.fileHandler = new DefaultFileHandler();
        this.targetDirectory = targetDirectory;
    }

    /**
     * @return the directory where the container distribution and its local configuration will be
     *         packaged
     */
    public String getTargetDirectory()
    {
        return this.targetDirectory;
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

    /**
     * {@inheritDoc}
     * @see Packager#packageContainer(org.codehaus.cargo.container.InstalledLocalContainer)
     */
    public void packageContainer(InstalledLocalContainer container)
    {
        getFileHandler().copyDirectory(container.getHome(), getTargetDirectory(),
            getDistributionExclusions());

        List configurationExclusions = getDefaultConfigurationExclusions();
        configurationExclusions.addAll(getConfigurationExclusions());

        getFileHandler().copyDirectory(container.getConfiguration().getHome(), getTargetDirectory(),
            configurationExclusions);
    }

    /**
     * @return the list of files to exclude by default from the configuration.
     * @see #getConfigurationExclusions()
     */
    private List getDefaultConfigurationExclusions()
    {
        List excludes = new ArrayList();
        excludes.add("**/cargocpc.war");
        return excludes;
    }

    /**
     * @return the list of distribution files (specified as
     *         <a href="http://ant.apache.org/manual/dirtasks.html#patterns">Ant File patterns</a>)
     *         which will not be present in the generated package. These files are files found in
     *         {@link org.codehaus.cargo.container.InstalledLocalContainer#getHome()}.
     */
    protected abstract List getDistributionExclusions();

    /**
     * @return the list of configuration files (specified as
     *         <a href="http://ant.apache.org/manual/dirtasks.html#patterns">Ant File patterns</a>)
     *         which will not be present in the generated package. These files are files found in
     *         {@link org.codehaus.cargo.container.configuration.LocalConfiguration#getHome()}.
     */
    protected abstract List getConfigurationExclusions();
}
