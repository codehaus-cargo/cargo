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
package org.codehaus.cargo.container.glassfish;

import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.glassfish.internal.AbstractAsAdmin;
import org.codehaus.cargo.container.glassfish.internal.AbstractGlassFishInstalledLocalContainer;
import org.codehaus.cargo.container.glassfish.internal.AbstractGlassFishStandaloneLocalConfiguration;
import org.codehaus.cargo.container.glassfish.internal.GlassFish3xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;

/**
 * GlassFish 3.x standalone local configuration.
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
     * @param home The work directory where files needed to run GlassFish will be created.
     */
    public GlassFish3xStandaloneLocalConfiguration(String home)
    {
        super(home);
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
