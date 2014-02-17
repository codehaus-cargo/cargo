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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.glassfish.internal.AbstractGlassFishInstalledLocalDeployer;
import org.codehaus.cargo.container.property.User;
import org.codehaus.cargo.util.CargoException;

/**
 * GlassFish 2.x installed local deployer, which uses the GlassFish asadmin to deploy and undeploy
 * applications.
 * 
 * @version $Id$
 */
public class GlassFish2xInstalledLocalDeployer extends AbstractGlassFishInstalledLocalDeployer
{

    /**
     * Calls parent constructor, which saves the container.
     * 
     * @param localContainer Container.
     */
    public GlassFish2xInstalledLocalDeployer(InstalledLocalContainer localContainer)
    {
        super(localContainer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
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

        this.addConnectOptions(args);

        this.addDeploymentArguments(args);
        args.add(new File(deployable.getFile()).getAbsolutePath());

        int returnValue = this.getLocalContainer().invokeAsAdmin(false, args);
        if (returnValue != 0)
        {
            throw new CargoException("The call to deploy returned " + returnValue);
        }
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
        this.addUndeploymentArguments(args);

        // not too sure how asadmin determines 'name'
        args.add(this.cutExtension(this.getFileHandler().getName(deployable.getFile())));

        int returnValue = this.getLocalContainer().invokeAsAdmin(false, args);
        if (returnValue != 0)
        {
            throw new CargoException("The call to undeploy returned " + returnValue);
        }
    }

    /**
     * Does not do anything since GlassFish 2.x cannot deploy datasources using
     * <code>java.sql.Driver</code> drivers. {@inheritDoc}
     */
    @Override
    public void deployDatasource(DataSource dataSource)
    {
        // Nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void undeployDatasource(String poolName, String jdbcName)
    {
        List<String> args = new ArrayList<String>();

        args.clear();
        this.addConnectOptions(args);
        args.add("delete-jdbc-resource");
        args.add(jdbcName);

        // The return value is checked by GlassFish3xAsAdmin.invokeAsAdmin
        this.getLocalContainer().invokeAsAdmin(false, args);

        args.clear();
        this.addConnectOptions(args);
        args.add("delete-jdbc-connection-pool");
        args.add(poolName);

        // The return value is checked by GlassFish3xAsAdmin.invokeAsAdmin
        this.getLocalContainer().invokeAsAdmin(false, args);
    }

    /**
     * Does not do anything since GlassFish 2.x support was not tested.
     * {@inheritDoc}
     */
    @Override
    public void deployResource(Resource resource)
    {
        // nothing
    }

    /**
     * Does not do anything since GlassFish 2.x support was not tested.
     * {@inheritDoc}
     */
    @Override
    public void createFileUser(User user)
    {
        // nothing
        
    }

    /**
     * Does not do anything since GlassFish 2.x support was not tested.
     * {@inheritDoc}
     */
    @Override
    public void activateDefaultPrincipalToRoleMapping()
    {
        // nothing
    }
}
