/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.deployable.Bundle;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.deployer.DeployerType;
import org.codehaus.cargo.container.glassfish.internal.AbstractGlassFishInstalledLocalContainer;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.spi.deployer.AbstractLocalDeployer;

/**
 * GlassFish installed local deployer, which uses the GlassFish asadmin to deploy and undeploy
 * applications.
 * 
 * @version $Id$
 */
public class GlassFishInstalledLocalDeployer extends AbstractLocalDeployer
{

    /**
     * Calls parent constructor, which saves the container.
     *
     * @param localContainer Container.
     */
    public GlassFishInstalledLocalDeployer(InstalledLocalContainer localContainer)
    {
        super(localContainer);
    }

    /**
     * Casts the container and returns.
     *
     * @return Cast container.
     */
    protected AbstractGlassFishInstalledLocalContainer getLocalContainer()
    {
        return (AbstractGlassFishInstalledLocalContainer) super.getContainer();
    }

    /**
     * Casts the configuration and returns.
     *
     * @return Cast configuration.
     */
    private GlassFishStandaloneLocalConfiguration getConfiguration()
    {
        return (GlassFishStandaloneLocalConfiguration) this.getLocalContainer().getConfiguration();
    }

    /**
     * {@inheritDoc}
     */
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
        this.doDeploy(deployable, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void redeploy(Deployable deployable)
    {
        this.doDeploy(deployable, true);
    }

    /**
     * Does the actual deployment.
     *
     * @param deployable Deployable to deploy.
     * @param overwrite Whether to overwrite.
     */
    protected void doDeploy(Deployable deployable, boolean overwrite)
    {
        List<String> args = new ArrayList<String>();
        args.add("deploy");
        if (overwrite)
        {
            args.add("--force");
        }

        if (deployable instanceof WAR)
        {
            args.add("--contextroot");
            args.add(((WAR) deployable).getContext());
        }
        else if (deployable instanceof Bundle)
        {
            args.add("--type=osgi");
        }

        this.addConnectOptions(args);

        args.add(new File(deployable.getFile()).getAbsolutePath());

        String[] arguments = new String[args.size()];
        args.toArray(arguments);
        this.getLocalContainer().invokeAsAdmin(false, arguments);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void undeploy(Deployable deployable)
    {
        List<String> args = new ArrayList<String>();
        args.add("undeploy");

        this.addConnectOptions(args);

        // not too sure how asadmin determines 'name'
        args.add(this.cutExtension(this.getFileHandler().getName(deployable.getFile())));

        String[] arguments = new String[args.size()];
        args.toArray(arguments);
        this.getLocalContainer().invokeAsAdmin(false, arguments);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Deployable deployable)
    {
        super.start(deployable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop(Deployable deployable)
    {
        super.stop(deployable);
    }

    /**
     * Returns a filename without its extension.
     *
     * @param name Filename.
     * @return Filename without its extension.
     */
    private String cutExtension(String name)
    {
        int idx = name.lastIndexOf('.');
        if (idx >= 0)
        {
            return name.substring(0, idx);
        }
        else
        {
            return name;
        }
    }

    /**
     * Adds connection options.
     *
     * @param args List to add to.
     */
    private void addConnectOptions(List<String> args)
    {
        args.add("--interactive=false");
        args.add("--port");
        args.add(this.getConfiguration().getPropertyValue(GlassFishPropertySet.ADMIN_PORT));
        args.add("--user");
        args.add(this.getConfiguration().getPropertyValue(RemotePropertySet.USERNAME));
        args.add("--passwordfile");
        args.add(this.getConfiguration().getPasswordFile().getAbsolutePath());
    }

}
