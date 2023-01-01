/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2023 Ali Tokmen.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.packager.Packager;
import org.codehaus.cargo.util.DefaultFileHandler;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.log.LoggedObject;
import org.codehaus.cargo.util.log.Logger;

/**
 * Package a container distribution and its local configuration in a directory.
 */
public abstract class AbstractDirectoryPackager extends LoggedObject implements Packager
{
    /**
     * The portion of that we by default exclude from packaging.
     */
    private static final List<String> EXCLUDED_FROM_CONFIGURATION =
        Arrays.asList("**/cargocpc.war");

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
     * configuration will be packaged
     */
    public AbstractDirectoryPackager(String targetDirectory)
    {
        this.fileHandler = new DefaultFileHandler();
        this.targetDirectory = targetDirectory;
    }

    /**
     * @return the directory where the container distribution and its local configuration will be
     * packaged
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
     * testing with Mock objects as it can be passed a test file handler that doesn't perform any
     * real file action.
     */
    protected void setFileHandler(FileHandler fileHandler)
    {
        this.fileHandler = fileHandler;
    }

    /**
     * Overriden in order to set the logger on ancillary components.
     * {@inheritDoc}
     * 
     * @param logger the logger to set and set in the ancillary objects
     */
    @Override
    public void setLogger(Logger logger)
    {
        super.setLogger(logger);
        this.fileHandler.setLogger(logger);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void packageContainer(InstalledLocalContainer container)
    {
        getFileHandler().copyDirectory(container.getHome(), getTargetDirectory(),
            getDistributionExclusions());

        List<String> configurationExclusions = new ArrayList<String>();
        configurationExclusions.addAll(getDefaultConfigurationExclusions());
        configurationExclusions.addAll(getConfigurationExclusions());

        getFileHandler().copyDirectory(container.getConfiguration().getHome(),
            getTargetDirectory(), configurationExclusions);
    }

    /**
     * @return the list of files to exclude by default from the configuration.
     * @see #getConfigurationExclusions()
     */
    private List<String> getDefaultConfigurationExclusions()
    {
        return AbstractDirectoryPackager.EXCLUDED_FROM_CONFIGURATION;
    }

    /**
     * @return the list of distribution files (specified as <a
     * href="http://ant.apache.org/manual/dirtasks.html#patterns">Ant File patterns</a>) which will
     * not be present in the generated package. These files are files found in
     * {@link org.codehaus.cargo.container.InstalledLocalContainer#getHome()}.
     */
    protected abstract List<String> getDistributionExclusions();

    /**
     * @return the list of configuration files (specified as <a
     * href="http://ant.apache.org/manual/dirtasks.html#patterns">Ant File patterns</a>) which will
     * not be present in the generated package. These files are files found in
     * {@link org.codehaus.cargo.container.configuration.LocalConfiguration#getHome()}.
     */
    protected abstract List<String> getConfigurationExclusions();
}
