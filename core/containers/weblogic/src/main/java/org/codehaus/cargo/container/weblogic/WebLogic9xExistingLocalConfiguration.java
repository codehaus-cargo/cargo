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
import java.io.IOException;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.InstalledLocalContainer;

/**
 * WebLogic existing configuration implementation for WebLogic9x style domains. The configuration
 * home must point to a valid WebLogic domain directory.
 * 
 * @version $Id$
 */
public class WebLogic9xExistingLocalConfiguration extends WebLogicExistingLocalConfiguration
{
    /**
     * {@inheritDoc}
     * 
     * @see WebLogicExistingLocalConfiguration#WebLogicExistingLocalConfiguration(String)
     */
    public WebLogic9xExistingLocalConfiguration(String dir)
    {
        super(dir);
    }

    /**
     * Deploy the Deployables to the weblogic configuration.
     * 
     * @param container the container to configure
     * @throws IOException if the cargo ping deployment fails
     */
    @Override
    protected void setupDeployables(WebLogicLocalContainer container) throws IOException
    {
        WebLogicLocalContainer weblogicContainer = container;
        // Get the deployable folder from container config. If it is not set
        File deployDir = new File(getDomainHome(), weblogicContainer.getAutoDeployDirectory());
        // use the default one.
        if (!deployDir.exists())
        {
            throw new ContainerException("Invalid existing configuration: The ["
                + deployDir.getPath() + "] directory does not exist");
        }

        // use a copying deployer until we have an XML-based one
        WebLogicCopyingInstalledLocalDeployer deployer =
            new WebLogicCopyingInstalledLocalDeployer((InstalledLocalContainer) container);
        deployer.deploy(getDeployables());

        // Deploy the cargocpc web-app by copying the WAR file
        getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war",
            new File(deployDir, "cargocpc.war"));
    }

}
