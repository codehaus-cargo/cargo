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
package org.codehaus.cargo.container.geronimo;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.geronimo.internal.AbstractGeronimoStandaloneLocalConfiguration;
import org.codehaus.cargo.container.geronimo.internal.Geronimo2xStandaloneLocalConfigurationCapability;

/**
 * Geronimo 2.x series standalone {@link org.codehaus.cargo.container.configuration.Configuration}
 * implementation.
 */
public class Geronimo2xStandaloneLocalConfiguration extends
    AbstractGeronimoStandaloneLocalConfiguration
{
    /**
     * Geronimo configuration capability.
     */
    private static final ConfigurationCapability CAPABILITY =
        new Geronimo2xStandaloneLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see AbstractGeronimoStandaloneLocalConfiguration#AbstractGeronimoStandaloneLocalConfiguration(String)
     */
    public Geronimo2xStandaloneLocalConfiguration(String dir)
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

        final String varDirectory = getHome() + "/var";
        if (!getFileHandler().exists(varDirectory))
        {
            getFileHandler().createDirectory(getHome(), "/var");

            // The /var directory does not exist, create it
            final String originalVarDirectory = ((InstalledLocalContainer) container).getHome()
                + "/var";
            getFileHandler().copyDirectory(originalVarDirectory, varDirectory);

            String securityDir = getFileHandler().createDirectory(getHome(), "/var/security");
            getResourceUtils().copyResource(
                RESOURCE_PATH + container.getId() + "/users.properties",
                    new File(securityDir, "users.properties"), replacements,
                        StandardCharsets.ISO_8859_1);
            getResourceUtils().copyResource(
                RESOURCE_PATH + container.getId() + "/groups.properties",
                    new File(securityDir, "groups.properties"), replacements,
                        StandardCharsets.ISO_8859_1);

            getFileHandler().createDirectory(getHome(), "/var/deploy");
            getFileHandler().createDirectory(getHome(), "/var/temp");
        }
    }
}
