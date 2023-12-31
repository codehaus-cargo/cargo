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
package org.codehaus.cargo.container.websphere;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.script.ScriptCommand;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployer.DeployerType;
import org.codehaus.cargo.container.spi.deployer.AbstractLocalDeployer;
import org.codehaus.cargo.container.websphere.internal.configuration.WebSphereJythonConfigurationFactory;
import org.codehaus.cargo.util.CargoException;

/**
 * Static deployer that deploys WARs to WebSphere 8.5.x.
 */
public class WebSphere85xInstalledLocalDeployer extends AbstractLocalDeployer
{

    /**
     * WebSphere container.
     */
    private WebSphere85xInstalledLocalContainer container;

    /**
     * {@inheritDoc}
     * @see AbstractLocalDeployer#AbstractLocalDeployer(org.codehaus.cargo.container.LocalContainer)
     */
    public WebSphere85xInstalledLocalDeployer(InstalledLocalContainer container)
    {
        super(container);
        this.container = (WebSphere85xInstalledLocalContainer) container;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DeployerType getType()
    {
        return DeployerType.INSTALLED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deploy(Deployable deployable)
    {
        try
        {
            WebSphereJythonConfigurationFactory factory =
                    ((WebSphereConfiguration) container.getConfiguration()).getFactory();
            List<ScriptCommand> wsAdminCommands = new ArrayList<ScriptCommand>();

            wsAdminCommands.add(factory.deployDeployableScript(deployable));
            wsAdminCommands.add(factory.saveSyncScript());

            container.executeScript(wsAdminCommands);
        }
        catch (Exception e)
        {
            throw new CargoException("Deploy failed", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void undeploy(Deployable deployable)
    {
        try
        {
            WebSphereJythonConfigurationFactory factory =
                    ((WebSphereConfiguration) container.getConfiguration()).getFactory();
            List<ScriptCommand> wsAdminCommands = new ArrayList<ScriptCommand>();

            wsAdminCommands.add(factory.undeployDeployableScript(deployable));
            wsAdminCommands.add(factory.saveSyncScript());

            container.executeScript(wsAdminCommands);
        }
        catch (Exception e)
        {
            throw new CargoException("Undeploy failed", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Deployable deployable)
    {
        try
        {
            WebSphereJythonConfigurationFactory factory =
                    ((WebSphereConfiguration) container.getConfiguration()).getFactory();
            List<ScriptCommand> wsAdminCommands = new ArrayList<ScriptCommand>();

            wsAdminCommands.add(factory.startDeployableScript(deployable));
            wsAdminCommands.add(factory.saveSyncScript());

            container.executeScript(wsAdminCommands);
        }
        catch (Exception e)
        {
            throw new CargoException("Start deployable failed", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop(Deployable deployable)
    {
        try
        {
            WebSphereJythonConfigurationFactory factory =
                    ((WebSphereConfiguration) container.getConfiguration()).getFactory();
            List<ScriptCommand> wsAdminCommands = new ArrayList<ScriptCommand>();

            wsAdminCommands.add(factory.stopDeployableScript(deployable));
            wsAdminCommands.add(factory.saveSyncScript());

            container.executeScript(wsAdminCommands);
        }
        catch (Exception e)
        {
            throw new CargoException("Stop deployable failed", e);
        }
    }
}
