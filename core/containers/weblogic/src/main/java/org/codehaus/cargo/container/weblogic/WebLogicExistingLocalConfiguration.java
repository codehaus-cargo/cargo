/* 
 * ========================================================================
 * 
 * Copyright 2005 Vincent Massol.
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
package org.codehaus.cargo.container.weblogic;

import java.io.File;
import java.util.Iterator;

import org.apache.tools.ant.util.FileUtils;
import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.spi.configuration.AbstractExistingLocalConfiguration;
import org.codehaus.cargo.container.weblogic.internal.WebLogicExistingLocalConfigurationCapability;

/**
 * WebLogic existing configuration implementation. The configuration home must point to a valid
 * WebLogic domain directory.
 *  
 * @version $Id$
 */
public class WebLogicExistingLocalConfiguration extends AbstractExistingLocalConfiguration
{
    /**
     * Capability of the WebLogic standalone configuration.
     */
    private static ConfigurationCapability capability =
        new WebLogicExistingLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see AbstractExistingLocalConfiguration#AbstractExistingLocalConfiguration(String)
     */
    public WebLogicExistingLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(WebLogicPropertySet.ADMIN_USER, "weblogic");
        setProperty(WebLogicPropertySet.ADMIN_PWD, "weblogic");
        setProperty(WebLogicPropertySet.SERVER, "server");
    }

    /**
     * {@inheritDoc}
     * @see AbstractExistingLocalConfiguration#configure(LocalContainer)
     */
    protected void doConfigure(LocalContainer container) throws Exception
    {
        setupDeployables(container);
    }

    /**
     * {@inheritDoc}
     * @see AbstractExistingLocalConfiguration#getCapability()
     */
    public ConfigurationCapability getCapability()
    {
        return capability;
    }

    /**
     * {@inheritDoc}
     * @see Object#toString()
     */
    public String toString()
    {
        return "WebLogic Existing Configuration";
    }

    /**
     * Deploy the Deployables to the weblogic configuration.
     * 
     * @param container the container to configure
     */
    protected void setupDeployables(Container container)
    {
        try
        {
            FileUtils fileUtils = FileUtils.newFileUtils();

            // Create the applications directory
            String appDir = getFileHandler().createDirectory(getHome(), "applications");

            // Deploy all deployables into the applications directory
            Iterator it = getDeployables().iterator();
            while (it.hasNext())
            {
                Deployable deployable = (Deployable) it.next();
                if ((deployable.getType() == DeployableType.WAR)
                    && ((WAR) deployable).isExpandedWar())
                {
                    continue;
                }

                fileUtils.copyFile(deployable.getFile(),
                    getFileHandler().append(appDir, getFileHandler().getName(deployable.getFile())),
                    null, true);
            }

            // Deploy the cargocpc web-app by copying the WAR file
            getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war",
                new File(appDir, "cargocpc.war"));
        }
        catch (Exception e)
        {
            throw new ContainerException("Failed to deploy Deployables in the "
                + container.getName() + " [" + getHome() + "] domain directory", e);
        }
    }
}
