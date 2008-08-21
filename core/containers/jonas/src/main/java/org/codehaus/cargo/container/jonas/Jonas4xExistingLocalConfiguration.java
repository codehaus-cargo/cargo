/* 
 * ========================================================================
 * 
 * Copyright 2007-2008 OW2.
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
package org.codehaus.cargo.container.jonas;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.jonas.internal.JonasExistingLocalConfigurationCapability;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractExistingLocalConfiguration;

/**
 * JOnAS existing {@link org.codehaus.cargo.container.configuration.Configuration} implementation.
 * 
 * @version $Id: Jonas4xExistingLocalConfiguration.java 14641 2008-07-25 11:46:29Z alitokmen $
 */
public class Jonas4xExistingLocalConfiguration extends AbstractExistingLocalConfiguration
{

    /**
     * Capability of the JOnAS existing configuration.
     */
    private static ConfigurationCapability capability =
        new JonasExistingLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * 
     * @see AbstractExistingLocalConfiguration#AbstractExistingLocalConfiguration(String)
     */
    public Jonas4xExistingLocalConfiguration(String dir)
    {
        super(dir);
        setProperty(GeneralPropertySet.PROTOCOL, "http");
        setProperty(GeneralPropertySet.HOSTNAME, "localhost");
        setProperty(ServletPropertySet.PORT, "9000");
        setProperty(GeneralPropertySet.JVMARGS, "-Xms128m -Xmx512m");
        setProperty(JonasPropertySet.JONAS_SERVER_NAME, "jonas");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.spi.configuration.AbstractLocalConfiguration#doConfigure(org.codehaus.cargo.container.LocalContainer)
     */
    protected void doConfigure(LocalContainer container) throws Exception
    {

        InstalledLocalContainer jonasContainer = (InstalledLocalContainer) container;

        checkDirExists("conf");
        checkDirExists("apps");
        checkDirExists("apps/autoload");
        checkDirExists("webapps");
        checkDirExists("webapps/autoload");
        checkDirExists("ejbjars");
        checkDirExists("ejbjars/autoload");

        Jonas4xInstalledLocalDeployer deployer = new Jonas4xInstalledLocalDeployer(jonasContainer);
        deployer.deploy(getDeployables());

        // Deploy the CPC (Cargo Ping Component) to the webapps directory.
        getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war",
            getFileHandler().append(getHome(), "/webapps/autoload/cargocpc.war"), getFileHandler());
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.configuration.Configuration#getCapability()
     */
    public ConfigurationCapability getCapability()
    {
        return capability;
    }

    /**
     * {@inheritDoc}
     * 
     * @see Object#toString()
     */
    public String toString()
    {
        return "JOnAS Existing Local Configuration";
    }

    /**
     * Check if the directory exists.
     * 
     * @param dir the directory name
     */
    private void checkDirExists(String dir)
    {
        String path = getFileHandler().append(getHome(), dir);
        boolean exists = getFileHandler().exists(path);

        if (!exists)
        {
            throw new ContainerException("Invalid existing configuration: The [" + path
                + "] directory does not exist");
        }
    }

}
