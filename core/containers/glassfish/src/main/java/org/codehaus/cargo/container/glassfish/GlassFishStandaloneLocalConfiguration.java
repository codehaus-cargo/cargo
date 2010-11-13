/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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
import java.io.FileWriter;
import java.io.IOException;

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.glassfish.internal.AbstractGlassFishInstalledLocalContainer;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration;
import org.codehaus.cargo.util.CargoException;
import org.codehaus.cargo.util.DefaultFileHandler;

/**
 * GlassFish standalone local configuration.
 * 
 * @version $Id$
 */
public class GlassFishStandaloneLocalConfiguration extends AbstractStandaloneLocalConfiguration
{

    /**
     * Container capability instance.
     */
    private static final ConfigurationCapability CAPABILITY =
        new GlassFishStandaloneLocalConfigurationCapability();

    /**
     * Creates the local configuration object.
     *
     * @param home The work directory where files needed to run Glassfish will be created.
     */
    public GlassFishStandaloneLocalConfiguration(String home)
    {
        super(home);

        // default properties
        this.setProperty(RemotePropertySet.USERNAME, "admin");
        this.setProperty(RemotePropertySet.PASSWORD, "adminadmin");
        this.setProperty(GeneralPropertySet.HOSTNAME, "localhost");
        this.setProperty(GlassFishPropertySet.ADMIN_PORT, "4848");
        this.setProperty(GlassFishPropertySet.JMS_PORT, "7676");
        this.setProperty(GlassFishPropertySet.IIOP_PORT, "3700");
        this.setProperty(GlassFishPropertySet.HTTPS_PORT, "8181");
        this.setProperty(GlassFishPropertySet.IIOPS_PORT, "3820");
        this.setProperty(GlassFishPropertySet.IIOP_MUTUAL_AUTH_PORT, "3920");
        this.setProperty(GlassFishPropertySet.JMX_ADMIN_PORT, "8686");
        this.setProperty(GlassFishPropertySet.DOMAIN_NAME, "cargo-domain");

        // ServletPropertySet.PORT default set to 8080 by the super class
    }

    /**
     * {@inheritDoc}
     */
    public ConfigurationCapability getCapability()
    {
        return CAPABILITY;
    }

    /**
     * Returns the password file that contains admin's password.
     *
     * @return The password file that contains admin's password.
     */
    File getPasswordFile()
    {
        String password = this.getPropertyValue(RemotePropertySet.PASSWORD);
        if (password == null)
        {
            password = "";
        }

        try
        {
            File f = new File(this.getHome(), "password.properties");
            if (!f.exists())
            {
                this.getFileHandler().mkdirs(this.getHome());
                FileWriter w = new FileWriter(f);
                // somehow glassfish uses both. Brain-dead.
                w.write("AS_ADMIN_PASSWORD=" + password + "\n");
                w.write("AS_ADMIN_ADMINPASSWORD=" + password + "\n");
                w.close();
            }
            return f;
        }
        catch (IOException e)
        {
            throw new CargoException("Failed to create a password file", e);
        }
    }

    /**
     * Creates a new domain and set up the workspace by invoking the "asadmin" command.
     *
     * {@inheritDoc}
     */
    @Override
    protected void doConfigure(LocalContainer container) throws Exception
    {
        DefaultFileHandler fileHandler = new DefaultFileHandler();
        fileHandler.delete(this.getHome());

        ((AbstractGlassFishInstalledLocalContainer) container).invokeAsAdmin(false, new String[]
        {
            "create-domain",
            "--interactive=false",
            "--adminport",
            this.getPropertyValue(GlassFishPropertySet.ADMIN_PORT),
            "--user",
            this.getPropertyValue(RemotePropertySet.USERNAME),
            "--passwordfile",
            this.getPasswordFile().getAbsolutePath(),
            "--instanceport",
            this.getPropertyValue(ServletPropertySet.PORT),
            "--domainproperties",

            this.getPropertyValueString(GlassFishPropertySet.JMS_PORT) + ':'
                + this.getPropertyValueString(GlassFishPropertySet.IIOP_PORT) + ':'
                + this.getPropertyValueString(GlassFishPropertySet.IIOPS_PORT) + ':'
                + this.getPropertyValueString(GlassFishPropertySet.HTTPS_PORT) + ':'
                + this.getPropertyValueString(GlassFishPropertySet.IIOP_MUTUAL_AUTH_PORT) + ':'
                + this.getPropertyValueString(GlassFishPropertySet.JMX_ADMIN_PORT),

            "--domaindir",
            this.getHome(),
            this.getPropertyValue(GlassFishPropertySet.DOMAIN_NAME)
        });

        // schedule cargocpc for deployment
        String cpcWar = this.getFileHandler().append(this.getHome(), "cargocpc.war");
        this.getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war", new File(cpcWar));
        this.getDeployables().add(new WAR(cpcWar));
    }

    /**
     * Returns a system property value string.
     *
     * @param key Key to look for.
     * @return Associaed value.
     */
    private String getPropertyValueString(String key)
    {
        String value = this.getPropertyValue(key);
        return key.substring("cargo.glassfish.".length()) + '=' + value;
    }

}
