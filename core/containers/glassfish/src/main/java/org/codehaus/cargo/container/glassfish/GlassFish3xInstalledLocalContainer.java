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
package org.codehaus.cargo.container.glassfish;

import java.io.File;
import java.util.jar.JarFile;

import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.glassfish.internal.AbstractAsAdmin;
import org.codehaus.cargo.container.glassfish.internal.AbstractGlassFishInstalledLocalContainer;
import org.codehaus.cargo.container.glassfish.internal.AbstractGlassFishInstalledLocalDeployer;
import org.codehaus.cargo.container.glassfish.internal.GlassFish3xAsAdmin;
import org.codehaus.cargo.container.glassfish.internal.GlassFish3x4x5x6x7xContainerCapability;

/**
 * GlassFish 3.x installed local container.
 */
public class GlassFish3xInstalledLocalContainer extends AbstractGlassFishInstalledLocalContainer
{

    /**
     * Container capability instance.
     */
    private static final ContainerCapability CAPABILITY =
        new GlassFish3x4x5x6x7xContainerCapability();

    /**
     * GlassFish version.
     */
    private String version;

    /**
     * Calls parent constructor, which saves the configuration.
     * 
     * @param localConfiguration Configuration.
     */
    public GlassFish3xInstalledLocalContainer(LocalConfiguration localConfiguration)
    {
        super(localConfiguration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AbstractAsAdmin getAsAdmin()
    {
        return new GlassFish3xAsAdmin(this.getHome());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AbstractGlassFishInstalledLocalDeployer getLocalDeployer()
    {
        return new GlassFish3xInstalledLocalDeployer(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContainerCapability getCapability()
    {
        return CAPABILITY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId()
    {
        return "glassfish3x";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName()
    {
        return "GlassFish " + getVersion("3.x");
    }

    /**
     * Parse installed GlassFish version.
     * 
     * @param defaultVersion the version used if the exact GlassFish version can't be determined
     * @return the GlassFish version, or <code>defaultVersion</code> if the version number could
     * not be determined.
     */
    protected synchronized String getVersion(String defaultVersion)
    {
        String version = this.version;

        if (version == null)
        {
            try
            {
                File glassfish = new File(getHome(), "glassfish");
                File adminCli = new File(glassfish, "modules/admin-cli.jar");
                if (!adminCli.isFile())
                {
                    adminCli = new File(glassfish, "admin-cli.jar");
                }
                if (adminCli.isFile())
                {
                    try (JarFile jarFile = new JarFile(adminCli))
                    {
                        version = jarFile.getManifest().getMainAttributes().getValue(
                            "Bundle-Version");
                    }
                }

                getLogger().info("Parsed GlassFish version = [" + version + "]",
                    this.getClass().getName());
            }
            catch (Exception e)
            {
                getLogger().debug(
                    "Failed to find GlassFish version, base error [" + e.getMessage() + "]",
                    this.getClass().getName());
            }

            if (version == null)
            {
                version = defaultVersion;
            }
            this.version = version;
        }

        return version;
    }

}
