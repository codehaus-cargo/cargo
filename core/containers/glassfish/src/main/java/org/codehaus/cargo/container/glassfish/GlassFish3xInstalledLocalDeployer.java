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
import org.codehaus.cargo.container.deployable.Bundle;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.glassfish.internal.AbstractGlassFishInstalledLocalDeployer;

/**
 * GlassFish 3.x installed local deployer, which uses the GlassFish asadmin to deploy and undeploy
 * applications.
 * 
 * @version $Id$
 */
public class GlassFish3xInstalledLocalDeployer extends AbstractGlassFishInstalledLocalDeployer
{

    /**
     * Calls parent constructor, which saves the container.
     * 
     * @param localContainer Container.
     */
    public GlassFish3xInstalledLocalDeployer(InstalledLocalContainer localContainer)
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
        this.addConnectOptions(args);

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

        args.add(new File(deployable.getFile()).getAbsolutePath());

        // The return value is checked by GlassFish3xAsAdmin.invokeAsAdmin
        this.getLocalContainer().invokeAsAdmin(false, args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void undeploy(Deployable deployable)
    {
        List<String> args = new ArrayList<String>();
        this.addConnectOptions(args);

        args.add("undeploy");

        // not too sure how asadmin determines 'name'
        args.add(this.cutExtension(this.getFileHandler().getName(deployable.getFile())));

        // The return value is checked by GlassFish3xAsAdmin.invokeAsAdmin
        this.getLocalContainer().invokeAsAdmin(false, args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deployDatasource(DataSource dataSource)
    {
        List<String> args = new ArrayList<String>();

        String escapedUrl = dataSource.getUrl().replace("\\", "\\\\").replace(":", "\\:");

        StringBuilder dataSourceProperty = new StringBuilder();
        dataSourceProperty.append("user=");
        dataSourceProperty.append(dataSource.getUsername());
        dataSourceProperty.append(":password=");
        dataSourceProperty.append(dataSource.getPassword());
        dataSourceProperty.append(":url=\"");
        dataSourceProperty.append(escapedUrl);
        dataSourceProperty.append("\"");

        String dataSourceId = "cargo-datasource-" + dataSource.getId();

        args.clear();
        this.addConnectOptions(args);
        args.add("create-jdbc-connection-pool");
        args.add("--restype");
        args.add(dataSource.getConnectionType());
        if ("javax.sql.XADataSource".equals(dataSource.getConnectionType()))
        {
            args.add("--datasourceclassname");
        }
        else
        {
            args.add("--driverclassname");
        }
        args.add(dataSource.getDriverClass());
        args.add("--property");
        args.add(dataSourceProperty.toString());
        args.add(dataSourceId);

        // The return value is checked by GlassFish3xAsAdmin.invokeAsAdmin
        this.getLocalContainer().invokeAsAdmin(false, args);

        args.clear();
        this.addConnectOptions(args);
        args.add("create-jdbc-resource");
        args.add("--connectionpoolid");
        args.add(dataSourceId);
        args.add(dataSource.getJndiLocation());

        // The return value is checked by GlassFish3xAsAdmin.invokeAsAdmin
        this.getLocalContainer().invokeAsAdmin(false, args);
    }

}
