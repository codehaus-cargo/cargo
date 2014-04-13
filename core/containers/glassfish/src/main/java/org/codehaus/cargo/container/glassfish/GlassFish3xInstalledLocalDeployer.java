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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.builder.ConfigurationEntryType;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.deployable.Bundle;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.glassfish.internal.AbstractGlassFishInstalledLocalDeployer;
import org.codehaus.cargo.container.property.User;

/**
 * GlassFish 3.x installed local deployer, which uses the GlassFish asadmin to deploy and undeploy
 * applications.
 * 
 * @version $Id$
 */
public class GlassFish3xInstalledLocalDeployer extends AbstractGlassFishInstalledLocalDeployer
{

    /**
     * Allowed JMS resource types per <code>create-jms-resource</code>
     * <code>--restype</code> parameter.
     */
    private static final Set<String> JMS_RESOURCE_TYPES = Collections
            .unmodifiableSet(new HashSet<String>(Arrays.asList(
                    "javax.jms.Topic",
                    "javax.jms.Queue",
                    "javax.jms.ConnectionFactory",
                    "javax.jms.TopicConnectionFactory",
                    "javax.jms.QueueConnectionFactory")));
    
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
            if (((WAR) deployable).getContext().length() == 0)
            {
                // CARGO-1179: If --contextroot is '', deployment fails on Windows
                args.add("/");
            }
            else
            {
                args.add(((WAR) deployable).getContext());
            }
        }
        else if (deployable instanceof Bundle)
        {
            args.add("--type=osgi");
        }

        this.addDeploymentArguments(args);
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

        this.addUndeploymentArguments(args);

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
        StringBuilder dataSourcePropertyString = new StringBuilder();
        Map<String, String> dataSourceProperties = new HashMap<String, String>();

        dataSourceProperties.put("user", dataSource.getUsername());
        dataSourceProperties.put("password", dataSource.getPassword());
        dataSourceProperties.put("url", dataSource.getUrl());
        Properties extraProperties = dataSource.getConnectionProperties();
        for (Object propertyName : extraProperties.keySet())
        {
            if (propertyName != null && extraProperties.get(propertyName) != null)
            {
                dataSourceProperties.put(
                    propertyName.toString(), extraProperties.get(propertyName).toString());
            }
        }
        for (Map.Entry<String, String> dataSourceProperty : dataSourceProperties.entrySet())
        {
            if (dataSourcePropertyString.length() > 0)
            {
                dataSourcePropertyString.append(":");
            }
            dataSourcePropertyString.append(dataSourceProperty.getKey());
            dataSourcePropertyString.append("=\"");
            dataSourcePropertyString.append(dataSourceProperty.getValue()
                .replace("\\", "\\\\").replace(":", "\\:").replace("=", "\\="));
            dataSourcePropertyString.append("\"");
        }

        String dataSourceId = "cargo-datasource-" + dataSource.getId();

        List<String> args = new ArrayList<String>();
        this.addConnectOptions(args);
        args.add("create-jdbc-connection-pool");
        args.add("--restype");
        args.add(dataSource.getConnectionType());
        if ("javax.sql.XADataSource".equals(dataSource.getConnectionType()))
        {
            args.add("--datasourceclassname");
        }
        else if ("javax.sql.DataSource".equals(dataSource.getConnectionType()))
        {
            args.add("--datasourceclassname");
        }
        else
        {
            args.add("--driverclassname");
        }
        args.add(dataSource.getDriverClass());
        args.add("--property");
        args.add(dataSourcePropertyString.toString());
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
     * This will be used to deploy JavaMail and JMS resources only using
     * <code>create-javamail-resource</code> and
     * <code>create-jms-resource</code> respectively. {@inheritDoc}
     */
    @Override
    public void deployResource(Resource resource)
    {
        if (JMS_RESOURCE_TYPES.contains(resource.getType()))
        {
            List<String> args = new ArrayList<String>();
            this.addConnectOptions(args);
            args.add("create-jms-resource");
            args.add("--restype");
            args.add(resource.getType());
            args.add(resource.getName());

            this.getLocalContainer().invokeAsAdmin(false, args);
        }        
        else if (ConfigurationEntryType.MAIL_SESSION.equals(resource.getType()))
        {
            List<String> args = new ArrayList<String>();
            this.addConnectOptions(args);
            args.add("create-javamail-resource");
            args.add("--mailhost");
            args.add(resource.getParameter("mail.smtp.host"));
            args.add("--mailuser");
            args.add(resource.getParameter("mail.smtp.user"));
            args.add("--fromaddress");
            args.add(resource.getParameter("mail.smtp.from"));
            args.add("--property");
            
            StringBuilder propertyBuilder = new StringBuilder();
            for (String parameterName : resource.getParameterNames())
            {
                propertyBuilder.append(parameterName);
                propertyBuilder.append("=");
                propertyBuilder.append(resource.getParameter(parameterName)
                        .replace("\\", "\\\\").replace(":", "\\:")
                        .replace("=", "\\="));
                propertyBuilder.append(":");
            }
            args.add(propertyBuilder.toString());
            args.add(resource.getName());
            this.getLocalContainer().invokeAsAdmin(false, args);
        }
    }

    /**
     * Does not do anything since GlassFish 3.x support was not tested.
     * 
     * {@inheritDoc}
     */
    @Override
    public void createFileUser(final User user)
    {
        // nothing
    }

    /**
     * Does not do anything since GlassFish 3.x support was not tested.
     * {@inheritDoc}
     */
    @Override
    public void activateDefaultPrincipalToRoleMapping()
    {
        // nothing
    }
}
