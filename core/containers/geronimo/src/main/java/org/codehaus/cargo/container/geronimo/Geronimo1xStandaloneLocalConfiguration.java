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
package org.codehaus.cargo.container.geronimo;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.geronimo.internal.AbstractGeronimoStandaloneLocalConfiguration;
import org.codehaus.cargo.container.geronimo.internal.Geronimo1xStandaloneLocalConfigurationCapability;

/**
 * Geronimo 1.x series standalone {@link org.codehaus.cargo.container.configuration.Configuration}
 * implementation.
 */
public class Geronimo1xStandaloneLocalConfiguration extends
    AbstractGeronimoStandaloneLocalConfiguration
{
    /**
     * Geronimo configuration capability.
     */
    private static final ConfigurationCapability CAPABILITY =
        new Geronimo1xStandaloneLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see AbstractGeronimoStandaloneLocalConfiguration#AbstractGeronimoStandaloneLocalConfiguration(String)
     */
    public Geronimo1xStandaloneLocalConfiguration(String dir)
    {
        super(dir);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConfigurationCapability getCapability()
    {
        return CAPABILITY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doConfigure(LocalContainer container) throws Exception
    {
        setupConfigurationDir();

        Map<String, String> replacements = createGeronimoReplacements(container);

        // The tmp directory needs to exist before the container starts
        getFileHandler().createDirectory(getHome(), "/var/temp");

        // TODO: Remove this once the system property for changing the var/ directory is
        // implemented in Geronimo.
        copyExtraStuffTemporarily(((InstalledLocalContainer) container).getHome());

        // Copy the geronimo configuration file
        String configDir = getFileHandler().createDirectory(getHome(), "var/config");
        getResourceUtils().copyResource(RESOURCE_PATH + container.getId() + "/"
            + getPropertyValue(GeronimoPropertySet.GERONIMO_SERVLET_CONTAINER_ID)
                + "/config.xml", new File(configDir, "config.xml"), replacements,
                    StandardCharsets.UTF_8);

        // Copy security-related files
        String securityDir = getFileHandler().createDirectory(getHome(), "/var/security");
        getResourceUtils().copyResource(RESOURCE_PATH + container.getId() + "/users.properties",
            new File(securityDir, "users.properties"), replacements, StandardCharsets.ISO_8859_1);
        getResourceUtils().copyResource(RESOURCE_PATH + container.getId() + "/groups.properties",
            new File(securityDir, "groups.properties"), replacements, StandardCharsets.ISO_8859_1);
        String keystoresDir = getFileHandler().createDirectory(securityDir, "keystores");
        getResourceUtils().copyResource(RESOURCE_PATH + container.getId() + "/keystore",
            new File(keystoresDir, "geronimo-default"));

        // Copy log settings
        String logDir = getFileHandler().createDirectory(getHome(), "/var/log");
        getResourceUtils().copyResource(RESOURCE_PATH + container.getId()
            + "/server-log4j.properties",
                new File(logDir, "server-log4j.properties"), replacements,
                    StandardCharsets.ISO_8859_1);
        getResourceUtils().copyResource(RESOURCE_PATH + container.getId()
            + "/client-log4j.properties", new File(logDir, "client-log4j.properties"),
                replacements, StandardCharsets.ISO_8859_1);
        getResourceUtils().copyResource(
            RESOURCE_PATH + container.getId() + "/deployer-log4j.properties",
                new File(logDir, "deployer-log4j.properties"), replacements,
                    StandardCharsets.ISO_8859_1);

        String deployDir = getFileHandler().createDirectory(getHome(), "deploy");

        if (!getFileHandler().exists(deployDir))
        {
            getFileHandler().mkdirs(deployDir);
        }
    }

    /**
     * Copy extra stuff to create a valid Geronimo configuration. Remove this once the system
     * property for changing the var/ directory is implemented in Geronimo.
     * 
     * @param containerHome location where the container is installed
     */
    private void copyExtraStuffTemporarily(String containerHome)
    {
        // The config store needs to exist before the container starts
        String configStore = getFileHandler().append(containerHome, "config-store");
        if (getFileHandler().isDirectory(configStore))
        {
            getFileHandler().copyDirectory(configStore,
                getFileHandler().append(getHome(), "config-store"));
        }

        // Create the Geronimo bin directory by copying it.
        getFileHandler().copyDirectory(
            getFileHandler().append(containerHome, "bin"),
                getFileHandler().append(getHome(), "bin"));

        // Create the Geronimo lib directory by copying it.
        getFileHandler().copyDirectory(
            getFileHandler().append(containerHome, "lib"),
                getFileHandler().append(getHome(), "lib"));

        // Create the Geronimo repository by copying it.
        getFileHandler().copyDirectory(
            getFileHandler().append(containerHome, "repository"),
                getFileHandler().append(getHome(), "repository"));

        // Create the Geronimo schema directory by copying it.
        getFileHandler().copyDirectory(
            getFileHandler().append(containerHome, "schema"),
                getFileHandler().append(getHome(), "schema"));
    }
}
