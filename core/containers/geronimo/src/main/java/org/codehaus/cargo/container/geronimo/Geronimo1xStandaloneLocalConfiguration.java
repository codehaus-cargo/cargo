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
package org.codehaus.cargo.container.geronimo;

import java.io.File;

import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.FilterChain;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.geronimo.internal.AbstractGeronimoStandaloneLocalConfiguration;
import org.codehaus.cargo.container.geronimo.internal.Geronimo1xStandaloneLocalConfigurationCapability;

/**
 * Geronimo 1.x series standalone {@link org.codehaus.cargo.container.configuration.Configuration}
 * implementation.
 * 
 */
public class Geronimo1xStandaloneLocalConfiguration extends
    AbstractGeronimoStandaloneLocalConfiguration
{
    /**
     * Geronimo configuration capability.
     */
    private static ConfigurationCapability capability =
        new Geronimo1xStandaloneLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.geronimo.internal.AbstractGeronimoStandaloneLocalConfiguration#AbstractGeronimoStandaloneLocalConfiguration(String)
     */
    public Geronimo1xStandaloneLocalConfiguration(String dir)
    {
        super(dir);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.configuration.Configuration#getCapability()
     */
    public ConfigurationCapability getCapability()
    {
        return capability;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.configuration.AbstractLocalConfiguration#configure(LocalContainer)
     */
    @Override
    protected void doConfigure(LocalContainer container) throws Exception
    {
        setupConfigurationDir();

        FilterChain filterChain = createGeronimoFilterChain(container);

        // The tmp directory needs to exist before the container starts
        getFileHandler().createDirectory(getHome(), "/var/temp");

        // TODO: Remove this once the system property for changing the var/ directory is
        // implemented in Geronimo.
        copyExtraStuffTemporarily(new File(((InstalledLocalContainer) container).getHome()));

        // Copy the geronimo configuration file
        String configDir = getFileHandler().createDirectory(getHome(), "var/config");
        getResourceUtils().copyResource(RESOURCE_PATH + container.getId() + "/"
            + getPropertyValue(GeronimoPropertySet.GERONIMO_SERVLET_CONTAINER_ID)
            + "/config.xml", new File(configDir, "config.xml"), filterChain, "UTF-8");

        // Copy security-related files
        String securityDir = getFileHandler().createDirectory(getHome(), "/var/security");
        getResourceUtils().copyResource(RESOURCE_PATH + container.getId() + "/users.properties",
            new File(securityDir, "users.properties"), filterChain, "ISO-8859-1");
        getResourceUtils().copyResource(RESOURCE_PATH + container.getId() + "/groups.properties",
            new File(securityDir, "groups.properties"), filterChain, "ISO-8859-1");
        String keystoresDir = getFileHandler().createDirectory(securityDir, "keystores");
        getResourceUtils().copyResource(RESOURCE_PATH + container.getId() + "/keystore",
            new File(keystoresDir, "geronimo-default"));

        // Copy log settings
        String logDir = getFileHandler().createDirectory(getHome(), "/var/log");
        getResourceUtils().copyResource(RESOURCE_PATH + container.getId()
            + "/server-log4j.properties",
            new File(logDir, "server-log4j.properties"), filterChain, "ISO-8859-1");
        getResourceUtils().copyResource(RESOURCE_PATH + container.getId()
            + "/client-log4j.properties", new File(logDir, "client-log4j.properties"), filterChain, 
            "ISO-8859-1");
        getResourceUtils().copyResource(RESOURCE_PATH + container.getId()
            + "/deployer-log4j.properties",
            new File(logDir, "deployer-log4j.properties"), filterChain, "ISO-8859-1");

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
    private void copyExtraStuffTemporarily(File containerHome)
    {
        // The config store needs to exist before the container starts
        File configStore = new File(containerHome, "config-store");
        if (configStore.isDirectory())
        {
            Copy copyStore = (Copy) getAntUtils().createAntTask("copy");
            FileSet fileSetStore = new FileSet();
            fileSetStore.setDir(new File(containerHome, "config-store"));
            copyStore.addFileset(fileSetStore);
            copyStore.setTodir(new File(getHome(), "config-store"));
            copyStore.execute();
        }

        // Create the Geronimo bin directory by copying it.
        Copy copyBin = (Copy) getAntUtils().createAntTask("copy");
        FileSet fileSetBin = new FileSet();
        fileSetBin.setDir(new File(containerHome, "bin"));
        copyBin.addFileset(fileSetBin);
        copyBin.setTodir(new File(getHome(), "bin"));
        copyBin.execute();

        // Create the Geronimo lib directory by copying it.
        Copy copyLib = (Copy) getAntUtils().createAntTask("copy");
        FileSet fileSetLib = new FileSet();
        fileSetLib.setDir(new File(containerHome, "lib"));
        copyLib.addFileset(fileSetLib);
        copyLib.setTodir(new File(getHome(), "lib"));
        copyLib.execute();

        // Create the Geronimo repository by copying it.
        Copy copyRepo = (Copy) getAntUtils().createAntTask("copy");
        FileSet fileSetRepo = new FileSet();
        fileSetRepo.setDir(new File(containerHome, "repository"));
        copyRepo.addFileset(fileSetRepo);
        copyRepo.setTodir(new File(getHome(), "repository"));
        copyRepo.execute();

        // Create the Geronimo schema directory by copying it.
        Copy copySchema = (Copy) getAntUtils().createAntTask("copy");
        FileSet fileSetSchema = new FileSet();
        fileSetSchema.setDir(new File(containerHome, "schema"));
        copySchema.addFileset(fileSetSchema);
        copySchema.setTodir(new File(getHome(), "schema"));
        copySchema.execute();
    }
}
