/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol, 2012-2015 Ali Tokmen.
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

import java.util.ArrayList;
import java.util.List;

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.script.ScriptCommand;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.spi.deployer.AbstractInstalledLocalDeployer;
import org.codehaus.cargo.container.weblogic.internal.WebLogicLocalScriptingContainer;

/**
 * Static deployer that manages deployment configuration calling WLST offline script.
 */
public class WebLogicWlstOfflineInstalledLocalDeployer extends AbstractInstalledLocalDeployer
{

    /**
     * {@inheritDoc}
     *
     * @param container container to configure
     */
    public WebLogicWlstOfflineInstalledLocalDeployer(LocalContainer container)
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
        WebLogicLocalScriptingContainer weblogicContainer =
            (WebLogicLocalScriptingContainer) getContainer();

        WebLogicWlstConfiguration configuration =
            (WebLogicWlstConfiguration) weblogicContainer.getConfiguration();

        // script for deploying deployable to WebLogic using WLST
        List<ScriptCommand> configurationScript = new ArrayList<ScriptCommand>();

        configurationScript.add(configuration.getConfigurationFactory().readDomainOfflineScript());
        configurationScript.add(getDeployScript(deployable));
        configurationScript.add(configuration.getConfigurationFactory().
                updateDomainOfflineScript());

        getLogger().info("Deploying application " + deployable.getName()
                + " to WebLogic domain.", this.getClass().getName());
        weblogicContainer.executeScript(configurationScript);
    }

    /**
     * Method returning WLST script used for deploying deployable.
     * @param deployable the Deployable to deploy
     * @return WLST script for deploying.
     */
    public ScriptCommand getDeployScript(Deployable deployable)
    {
        WebLogicWlstConfiguration configuration =
            (WebLogicWlstConfiguration) getContainer().getConfiguration();

        String path = getAbsolutePath(deployable);

        return configuration.getConfigurationFactory().deployDeployableScript(deployable.getName(),
                path);
    }

    /**
     * {@inheritDoc} undeploys files by sending WLST script to WebLogic server.
     *
     * @see org.codehaus.cargo.container.spi.deployer.AbstractDeployer#undeploy(org.codehaus.cargo.container.deployable.Deployable)
     */
    @Override
    public void undeploy(Deployable deployable)
    {
        WebLogicLocalScriptingContainer weblogicContainer =
            (WebLogicLocalScriptingContainer) getContainer();

        WebLogicWlstConfiguration configuration =
            (WebLogicWlstConfiguration) weblogicContainer.getConfiguration();

        List<ScriptCommand> configurationScript = new ArrayList<ScriptCommand>();

        configurationScript.add(configuration.getConfigurationFactory().readDomainOfflineScript());
        configurationScript.add(configuration.getConfigurationFactory().
                undeployDeployableScript(deployable.getName()));
        configurationScript.add(configuration.getConfigurationFactory().
                updateDomainOfflineScript());

        getLogger().info("Undeploying application " + deployable.getName()
            + " from WebLogic domain.", this.getClass().getName());
        weblogicContainer.executeScript(configurationScript);
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
