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
package org.codehaus.cargo.container.weblogic;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableException;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.EAR;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.spi.deployer.AbstractInstalledLocalDeployer;

/**
 * Static deployer that manages deployment configuration calling WLST offline script.
 *
 */
public class WebLogic9x10x103x12xWlstOfflineInstalledLocalDeployer extends
    AbstractInstalledLocalDeployer
{

    /**
     * {@inheritDoc}
     *
     * @param container container to configure
     */
    public WebLogic9x10x103x12xWlstOfflineInstalledLocalDeployer(InstalledLocalContainer container)
    {
        super(container);
    }

    /**
     * {@inheritDoc} deploys files by sending WLST script to WebLogic server.
     *
     * @see org.codehaus.cargo.container.spi.deployer.AbstractDeployer#deploy(org.codehaus.cargo.container.deployable.Deployable)
     */
    @Override
    public void deploy(Deployable deployable)
    {
        WebLogic121xWlstInstalledLocalContainer weblogicContainer =
            (WebLogic121xWlstInstalledLocalContainer) getContainer();

        String id = createIdForDeployable(deployable);
        String path = getAbsolutePath(deployable);
        String serverName = getServerName();

        // script for deploying deployable to Weblogic using WLST
        List<String> configurationScript = new ArrayList<String>();
        configurationScript.add(String.format("app=create('%s','AppDeployment')", id));
        configurationScript.add(String.format("app.setSourcePath('%s')", path));
        configurationScript.add("cd('/')");
        configurationScript.add(String.format(
            "assign('AppDeployment', '%s', 'Target', '%s')",
            id, serverName));

        weblogicContainer.modifyDomainConfigurationWithWlst(configurationScript);
    }

    /**
     * {@inheritDoc} undeploys files by sending WLST script to WebLogic server.
     *
     * @see org.codehaus.cargo.container.spi.deployer.AbstractDeployer#undeploy(org.codehaus.cargo.container.deployable.Deployable)
     */
    @Override
    public void undeploy(Deployable deployable)
    {
        WebLogic121xWlstInstalledLocalContainer weblogicContainer =
            (WebLogic121xWlstInstalledLocalContainer) getContainer();

        String id = createIdForDeployable(deployable);

        List<String> configurationScript = new ArrayList<String>();
        configurationScript.add(String.format("delete('%s','AppDeployment')", id));

        weblogicContainer.modifyDomainConfigurationWithWlst(configurationScript);
    }

    /**
     * Get a string name for the configuration of this deployable. This should be XML friendly. For
     * example, the String returned will have no slashes or colons, and be as short as possible.
     *
     * @param deployable used to construct the id
     * @return a string that can be used to name this configuration
     */
    protected String createIdForDeployable(Deployable deployable)
    {
        String name = null;
        // TODO this code should be moved into the deployable objects themselves, as they
        // are better responsible for their name.
        if (deployable.getType() == DeployableType.WAR)
        {
            name = ((WAR) deployable).getContext();
        }
        else if (deployable.getType() == DeployableType.EAR)
        {
            name = ((EAR) deployable).getName();
        }
        else if (deployable.getType() == DeployableType.EJB
            || deployable.getType() == DeployableType.RAR)
        {
            name = createIdFromFileName(deployable);
        }
        else
        {
            throw new DeployableException("name extraction for " + deployable.getType()
                + " not currently supported");
        }
        return name;
    }

    /**
     * Get a string name for the configuration of this deployable based on its filename.
     *
     * @param deployable used to construct the id
     * @return a string that can be used to name this configuration
     */
    protected String createIdFromFileName(Deployable deployable)
    {
        File file = new File(deployable.getFile());
        return file.getName();
    }

    /**
     * return the running server's name.
     *
     * @return the WebLogic server's name
     */
    private String getServerName()
    {
        return getContainer().getConfiguration().getPropertyValue(WebLogicPropertySet.SERVER);
    }

    /**
     * gets the absolute path from a file that may be relative to the current directory.
     *
     * @param deployable - what to extract the file path from
     * @return - absolute path to the deployable
     */
    private String getAbsolutePath(Deployable deployable)
    {
        String path = deployable.getFile();
        return getFileHandler().getAbsolutePath(path);
    }

}
