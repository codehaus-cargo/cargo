/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2022 Ali Tokmen.
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
import org.codehaus.cargo.container.deployable.EAR;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.glassfish.internal.AbstractGlassFishInstalledLocalDeployer;
import org.codehaus.cargo.container.property.User;

/**
 * GlassFish 3.x installed local deployer, which uses the GlassFish asadmin to deploy and undeploy
 * applications.
 */
public class GlassFish3xInstalledLocalDeployer extends AbstractGlassFishInstalledLocalDeployer
{

    /**
     * Allowed JMS resource types per <code>create-jms-resource</code>
     * <code>--restype</code> parameter.
     */
    private static final Set<String> JMS_RESOURCE_TYPES =
        Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
            // CARGO-1541: GlassFish 6.x onwards uses Jakarta EE
            "jakarta.jms.Topic",
            "jakarta.jms.Queue",
            "jakarta.jms.ConnectionFactory",
            "jakarta.jms.TopicConnectionFactory",
            "jakarta.jms.QueueConnectionFactory",

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
            if (((WAR) deployable).getContext().isEmpty())
            {
                // CARGO-1179: If --contextroot is '', deployment fails on Windows
                args.add("/");
            }
            else
            {
                args.add(((WAR) deployable).getContext());
            }
        }
        else if (deployable instanceof EAR)
        {
            args.add("--name=" + deployable.getName());
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
        List<String> dataSourceConnectionPoolProperties = new ArrayList<String>();
        Map<String, String> dataSourceProperties = new HashMap<String, String>();

        dataSourceProperties.put("user", dataSource.getUsername());
        dataSourceProperties.put("password", dataSource.getPassword());
        dataSourceProperties.put("url", dataSource.getUrl());
        Properties extraProperties = dataSource.getConnectionProperties();
        for (Object propertyName : extraProperties.keySet())
        {
            if (propertyName != null && extraProperties.get(propertyName) != null)
            {
                // CARGO-1597: Add the ability to change database connection pool properties
                if (propertyName.toString().startsWith("----"))
                {
                    String dataSourceConnectionPoolProperty =
                        propertyName + "=" + extraProperties.get(propertyName);
                    dataSourceConnectionPoolProperties.add(dataSourceConnectionPoolProperty);
                }
                else
                {
                    dataSourceProperties.put(
                        propertyName.toString(), extraProperties.get(propertyName).toString());
                }
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
            if (dataSourceProperty.getValue() != null)
            {
                dataSourcePropertyString.append(dataSourceProperty.getValue()
                    .replace("\\", "\\\\").replace(":", "\\:").replace("=", "\\="));
            }
            dataSourcePropertyString.append("\"");
        }

        String dataSourceId = "cargo-datasource-" + dataSource.getId();

        List<String> args = new ArrayList<String>();
        this.addConnectOptions(args);
        args.add("create-jdbc-connection-pool");
        args.add("--restype");
        args.add(dataSource.getConnectionType());
        if (null == dataSource.getConnectionType())
        {
            args.add("--driverclassname");
        }
        else
        {
            switch (dataSource.getConnectionType())
            {
                case "javax.sql.XADataSource":
                    args.add("--datasourceclassname");
                    break;

                case "javax.sql.DataSource":
                    args.add("--datasourceclassname");
                    break;

                default:
                    args.add("--driverclassname");
                    break;
            }
        }
        args.add(dataSource.getDriverClass());
        args.add("--property");
        args.add(dataSourcePropertyString.toString());
        args.addAll(dataSourceConnectionPoolProperties);
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
            if (getContainer() instanceof GlassFish6xInstalledLocalContainer)
            {
                // CARGO-1541: GlassFish 6.x onwards uses Jakarta EE
                args.add(resource.getType().replace("javax.", "jakarta."));
            }
            else
            {
                // CARGO-1541: GlassFish 5.x downwards uses Java EE
                args.add(resource.getType().replace("jakarta.", "javax."));
            }
            if (!resource.getParameters().isEmpty())
            {
                args.add("--property");
                StringBuilder propertyBuilder = new StringBuilder();
                for (String parameterName : resource.getParameterNames())
                {
                    if (propertyBuilder.length() > 0)
                    {
                        propertyBuilder.append(":");
                    }
                    propertyBuilder.append(parameterName);
                    propertyBuilder.append("=");
                    propertyBuilder.append(resource.getParameter(parameterName)
                            .replace("\\", "\\\\").replace(":", "\\:")
                            .replace("=", "\\="));
                }
                args.add(propertyBuilder.toString());
            }
            args.add(resource.getName());
            this.getLocalContainer().invokeAsAdmin(false, args);
        }
        else if (ConfigurationEntryType.MAIL_SESSION.equals(resource.getType())
            || ConfigurationEntryType.MAIL_SESSION.replace("javax.", "jakarta.")
                .equals(resource.getType()))
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
            if (!resource.getParameters().isEmpty())
            {
                args.add("--property");
                StringBuilder propertyBuilder = new StringBuilder();
                for (String parameterName : resource.getParameterNames())
                {
                    if (propertyBuilder.length() > 0)
                    {
                        propertyBuilder.append(":");
                    }
                    propertyBuilder.append(parameterName);
                    propertyBuilder.append("=");
                    propertyBuilder.append(resource.getParameter(parameterName)
                            .replace("\\", "\\\\").replace(":", "\\:")
                            .replace("=", "\\="));
                }
                args.add(propertyBuilder.toString());
            }
            args.add(resource.getName());
            this.getLocalContainer().invokeAsAdmin(false, args);
        }
        else
        {
            /*
            Adding support for Custom Resource types with no parameter validation

            asadmin create-custom-resource
            --restype=java.lang.String
            --enabled=true
            --description="SAC Server Name"
            --factoryclass=org.glassfish.resources.custom.factory.PrimitivesAndStringFactory
            --property value=http\\://mymachine/cenas
            "sac/sacServerName"
            */
            List<String> args = new ArrayList<String>();
            this.addConnectOptions(args);
            args.add("create-custom-resource");
            args.add("--enabled=true");
            args.add("--restype");
            args.add(resource.getType());
            args.add("--factoryclass");
            args.add(resource.getClassName());
            if (!resource.getParameters().isEmpty())
            {
                args.add("--property");
                StringBuilder propertyBuilder = new StringBuilder();
                for (String parameterName : resource.getParameterNames())
                {
                    if (propertyBuilder.length() > 0)
                    {
                        propertyBuilder.append(":");
                    }
                    propertyBuilder.append(parameterName);
                    propertyBuilder.append("=");
                    propertyBuilder.append(resource.getParameter(parameterName)
                            .replace("\\", "\\\\").replace(":", "\\:")
                            .replace("=", "\\="));
                }
                args.add(propertyBuilder.toString());
            }
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
