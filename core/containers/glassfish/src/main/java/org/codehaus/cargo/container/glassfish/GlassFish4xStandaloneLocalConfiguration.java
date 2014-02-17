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

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.property.ServletPropertySet;

/**
 * GlassFish 4.x standalone local configuration.
 * 
 * @version $Id$
 */
public class GlassFish4xStandaloneLocalConfiguration
    extends GlassFish3xStandaloneLocalConfiguration
{

    /**
     * Container capability instance.
     */
    private static final ConfigurationCapability CAPABILITY =
        new GlassFish4xStandaloneLocalConfigurationCapability();

    /**
     * Creates the local configuration object.
     * 
     * @param home The work directory where files needed to run Glassfish will be created.
     */
    public GlassFish4xStandaloneLocalConfiguration(String home)
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
    }

}
