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
package org.codehaus.cargo.container.glassfish;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.security.MessageDigest;

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.glassfish.internal.AbstractAsAdmin;
import org.codehaus.cargo.container.glassfish.internal.AbstractGlassFishInstalledLocalContainer;
import org.codehaus.cargo.container.glassfish.internal.AbstractGlassFishStandaloneLocalConfiguration;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.property.User;
import org.codehaus.cargo.util.Base64;

/**
 * GlassFish 3.x standalone local configuration.
 * 
 * @version $Id$
 */
public class GlassFish3xStandaloneLocalConfiguration
    extends AbstractGlassFishStandaloneLocalConfiguration
{

    /**
     * Container capability instance.
     */
    private static final ConfigurationCapability CAPABILITY =
        new GlassFish3xStandaloneLocalConfigurationCapability();

    /**
     * Creates the local configuration object.
     * 
     * @param home The work directory where files needed to run Glassfish will be created.
     */
    public GlassFish3xStandaloneLocalConfiguration(String home)
    {
        super(home);
    }

    /**
     * {@inheritDoc}
     */
    public ConfigurationCapability getCapability()
    {
        return CAPABILITY;
    }

    /**
     * Add the necessary changes to support {@link ServletPropertySet#USERS}.
     * 
     * {@inheritDoc}
     */
    @Override
    protected void doConfigure(LocalContainer container) throws Exception
    {
        // TODO try to implement this using configureUsingAsAdmin in the future
        if (getPropertyValue(ServletPropertySet.USERS) != null)
        {
            // set activate-default-principal-to-role-mapping=true
            addXmlReplacement(this.getPropertyValue(GlassFishPropertySet.DOMAIN_NAME)
                    + "/config/domain.xml", 
                    "//security-service", 
                    "activate-default-principal-to-role-mapping",
                    "true");
        }            
        super.doConfigure(container);
        // TODO this part still does not work
        // Use the workaround of creating your own keyfile and overwriting for now
        if (getPropertyValue(ServletPropertySet.USERS) != null)
        {
            // Build the cargo key file after to overwrite the existing file
            PrintStream keyFileStream = new PrintStream(
                    new FileOutputStream(
                            getHome()
                                    + "/"
                                    + this.getPropertyValue(GlassFishPropertySet.DOMAIN_NAME)
                                    + "/config/keyfile"));
            for (User user : User.parseUsers(getPropertyValue(ServletPropertySet.USERS)))
            {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                StringBuilder line = new StringBuilder();
                line.append(user.getName().trim());
                line.append(";{SSHA256}");
                final int passwordLength = user.getPassword().length();
                byte[] saltedPassword = new byte[passwordLength + 1 ];
                System.arraycopy(user.getPassword().getBytes(), 0,
                        saltedPassword, 0, passwordLength);
                saltedPassword[passwordLength] = 0;
                final byte[] hash = digest.digest(saltedPassword);
                byte[] hashPlusSalt = new byte[33];
                System.arraycopy(hash, 0,
                        hashPlusSalt, 0, hash.length);
                hashPlusSalt[32] = 1;
                line.append(Base64.encodeToString(hashPlusSalt));
                line.append(";");
                for (String role : user.getRoles())
                {
                    line.append(role.trim());
                    line.append(',');
                }
                line.deleteCharAt(line.length() - 1);
                keyFileStream.println(line);
            }
            keyFileStream.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int configureUsingAsAdmin(
        AbstractGlassFishInstalledLocalContainer abstractGlassFishInstalledLocalContainer)
    {
        return abstractGlassFishInstalledLocalContainer.invokeAsAdmin(
                false,
                "--interactive=false",
                "--user",
                this.getPropertyValue(RemotePropertySet.USERNAME),
                "--passwordfile",
                AbstractAsAdmin.getPasswordFile(this).getAbsolutePath(),

                "create-domain",

                "--adminport",
                this.getPropertyValue(GlassFishPropertySet.ADMIN_PORT),
                "--instanceport",
                this.getPropertyValue(ServletPropertySet.PORT),
                "--domainproperties",

                this.getPropertyValueString(GlassFishPropertySet.JMS_PORT) + ':'
                    + this.getPropertyValueString(GlassFishPropertySet.IIOP_PORT) + ':'
                    + this.getPropertyValueString(GlassFishPropertySet.IIOPS_PORT) + ':'
                    + this.getPropertyValueString(GlassFishPropertySet.HTTPS_PORT) + ':'
                    + this.getPropertyValueString(GlassFishPropertySet.IIOP_MUTUAL_AUTH_PORT) + ':'
                    + this.getPropertyValueString(GlassFishPropertySet.JMX_ADMIN_PORT) + ':'
                    + this.getPropertyValueString(GlassFishPropertySet.DEBUGGER_PORT) + ':'
                    + this.getPropertyValueString(GlassFishPropertySet.OSGI_SHELL_PORT),

                "--domaindir", this.getHome(), this
                    .getPropertyValue(GlassFishPropertySet.DOMAIN_NAME));
    }

}
