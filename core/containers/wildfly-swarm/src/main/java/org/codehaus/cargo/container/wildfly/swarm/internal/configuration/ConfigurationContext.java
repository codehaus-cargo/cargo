/*
 * ========================================================================
 *
 *  Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  ========================================================================
 */
package org.codehaus.cargo.container.wildfly.swarm.internal.configuration;

import java.io.File;

import org.codehaus.cargo.util.FileHandler;

/**
 * Context passed to configurators.
 */
public class ConfigurationContext
{
    /**
     * File handler utility instance.
     */
    private final FileHandler fileHandler;

    /**
     * Configuration home directory.
     */
    private final String configurationHome;

    /**
     * WildFly Swarm project descriptor.
     */
    private final File projectDescriptor;

    /**
     * Constructor.
     * @param fileHandler FileHandler utility.
     * @param configurationHome Configuration home directory.
     * @param projectDescriptor Project descriptor file.
     */
    public ConfigurationContext(FileHandler fileHandler, String configurationHome,
                                File projectDescriptor)
    {
        this.fileHandler = fileHandler;
        this.configurationHome = configurationHome;
        this.projectDescriptor = projectDescriptor;
    }

    /**
     * FileHandler getter.
     * @return File Handler instance.
     */
    public FileHandler getFileHandler()
    {
        return fileHandler;
    }

    /**
     * Configuration home directory getter.
     * @return Path to configuration home.
     */
    public String getConfigurationHome()
    {
        return configurationHome;
    }

    /**
     * Project descriptor getter.
     * @return Project descriptor file.
     */
    public File getProjectDescriptor()
    {
        return projectDescriptor;
    }
}
