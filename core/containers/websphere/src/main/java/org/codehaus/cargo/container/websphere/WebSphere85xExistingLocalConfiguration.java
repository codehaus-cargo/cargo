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
package org.codehaus.cargo.container.websphere;

import java.io.File;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractExistingLocalConfiguration;
import org.codehaus.cargo.container.websphere.internal.WebSphere85xExistingLocalConfigurationCapability;

/**
 * WebSphere existing {@link org.codehaus.cargo.container.configuration.Configuration}
 * implementation.
 * 
 * @version $Id$
 */
public class WebSphere85xExistingLocalConfiguration extends AbstractExistingLocalConfiguration
{
    /**
     * Capability of the WebShere standalone configuration.
     */
    private static ConfigurationCapability capability =
        new WebSphere85xExistingLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see AbstractExistingLocalConfiguration#AbstractExistingLocalConfiguration(String)
     */
    public WebSphere85xExistingLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(ServletPropertySet.PORT, "9080");

        setProperty(WebSpherePropertySet.ADMIN_USERNAME, "websphere");
        setProperty(WebSpherePropertySet.ADMIN_PASSWORD, "websphere");

        setProperty(WebSpherePropertySet.PROFILE, "cargoProfile");
        setProperty(WebSpherePropertySet.CELL, "cargoNodeCell");
        setProperty(WebSpherePropertySet.NODE, "cargoNode");
        setProperty(WebSpherePropertySet.SERVER, "cargoServer");
    }

    /**
     * {@inheritDoc}
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
        File cargoCpc = File.createTempFile("cargo-cpc-", ".war");
        getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war", cargoCpc);
        WAR cargoCpcWar = new WAR(cargoCpc.getAbsolutePath());
        cargoCpcWar.setContext("cargocpc");
        getDeployables().add(cargoCpcWar);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.configuration.ContainerConfiguration#verify()
     */
    @Override
    public void verify()
    {
        // Nothing to verify right now...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "WebSphere 8.5 Existing Configuration";
    }
    
}
