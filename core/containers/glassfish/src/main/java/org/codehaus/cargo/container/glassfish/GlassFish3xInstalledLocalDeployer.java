/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
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
 */
public class GlassFish3xInstalledLocalDeployer extends AbstractGlassFishInstalledLocalDeployer
{

    /**
     * Allowed JMS resource types per <code>create-jms-resource</code>
     * <code>--restype</code> parameter.
     */
    private static final Set<String> JMS_RESOURCE_TYPES =
        Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
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
     * CARGO-1597: Add the ability to set connection pool attributes
     */
    private static final Set<String> CONNECTION_POOL_ATTRIBUTES =
        Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
            "allow-non-component-callers",
            "associate-with-thread",
            "connection-creation-retry-attempts",
            "connection-creation-retry-interval-in-seconds",
            "connection-leak-reclaim",
            "connection-leak-timeout-in-seconds",
            "connection-validation-method",
            "datasource-classname",
            "dynamic-reconfiguration-wait-timeout-in-seconds",
            "fail-all-connections",
            "idle-time-out-in-seconds",
            "idle-timeout-in-seconds",
            "init-sql",
            "is-connection-validation-required",
            "lazy-connection-association",
            "lazy-connection-enlistment",
            "log-jdbc-calls",
            "match-connections",
            "max-connection-usage-count",
            "max-pool-size",
            "max-wait-time-in-millis",
            "name",
            "non-transactional-connections",
            "pool-resize-quantity",
            "pooling",
            "res-type",
            "slow-query-threshold-in-seconds",
            "sql-trace-listeners",
            "statement-cache-size",
            "statement-leak-reclaim",
            "statement-leak-timeout-in-seconds",
            "statement-timeout-in-seconds",
            "steady-pool-size",
            "time-to-keep-queries-in-minutes",
            "transaction-isolation-level",
            "validate-atmost-once-period-in-seconds",
            "validation-classname",
            "validation-table-name",
            "wrap-jdbc-objects")));

    /**
     * CARGO-1598: Managed executor service attributes
     */
    private static final Set<String> MANAGED_EXECUTOR_SERVICE_ATTRIBUTES =
        Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
            "enabled",
            "contextinfoenabled",
            "contextinfo",
            "threadpriority",
            "longrunningtasks",
            "hungafterseconds",
            "corepoolsize",
            "maximumpoolsize",
            "keepaliveseconds",
            "threadlifetimeseconds",
            "taskqueuecapacity",
            "description",
            "property",
            "target")));

    /**
     * CARGO-1598: Add the ability to create managed executor service in GlassFish
     */
    private static final String MANAGED_EXECUTOR_SERVICE =
        "javax.enterprise.concurrent.ManagedExecutorService";

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

        if (!Boolean.parseBoolean(getContainer().getConfiguration().getPropertyValue(
            GlassFishPropertySet.DEPLOY_IGNORE_DEPLOYABLE_NAME)))
        {
            args.add("--name=" + getDeployableName(deployable));
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
        else if (deployable instanceof Bundle)
        {
            args.add("--type=osgi");
        }

        this.addDeploymentArguments(args);
        args.add(new File(deployable.getFile()).getAbsolutePath());
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

        args.add(getDeployableName(deployable));

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
                if (!CONNECTION_POOL_ATTRIBUTES.contains(propertyName))
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
        args.add(dataSourceId);
        this.getLocalContainer().invokeAsAdmin(false, args);

        for (Object propertyName : extraProperties.keySet())
        {
            if (propertyName != null && extraProperties.get(propertyName) != null)
            {
                if (CONNECTION_POOL_ATTRIBUTES.contains(propertyName))
                {
                    args.clear();
                    this.addConnectOptions(args);
                    args.add("set");
                    args.add(
                        this.getContainer().getConfiguration().getPropertyValue(
                            GlassFishPropertySet.DOMAIN_NAME)
                            + ".resources.jdbc-connection-pool." + dataSourceId + "."
                                + propertyName + "=" + extraProperties.get(propertyName));
                    this.getLocalContainer().invokeAsAdmin(false, args);
                }
            }
        }

        args.clear();
        this.addConnectOptions(args);
        args.add("create-jdbc-resource");
        args.add("--connectionpoolid");
        args.add(dataSourceId);
        args.add(dataSource.getJndiLocation());
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
        this.getLocalContainer().invokeAsAdmin(false, args);

        args.clear();
        this.addConnectOptions(args);
        args.add("delete-jdbc-connection-pool");
        args.add(poolName);
        this.getLocalContainer().invokeAsAdmin(false, args);
    }

    /**
     * This will be used to deploy JavaMail, JMS and Managed Executor Services resources only using
     * <code>create-javamail-resource</code>,
     * <code>create-jms-resource</code> and <code>create-managed-executor-service</code>
     * respectively. Other resource types will be created using
     * <code>create-custom-resource</code>. {@inheritDoc}
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
            if (this.isJakartaEe())
            {
                args.add(resource.getType().replace("javax.", "jakarta."));
            }
            else
            {
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
        else if (
            GlassFish3xInstalledLocalDeployer.MANAGED_EXECUTOR_SERVICE.equals(resource.getType())
            || GlassFish3xInstalledLocalDeployer.MANAGED_EXECUTOR_SERVICE
                .replace("javax.", "jakarta.").equals(resource.getType()))
        {
            List<String> args = new ArrayList<String>();
            this.addConnectOptions(args);
            args.add("create-managed-executor-service");

            Map<String, String> parameters = new HashMap<String, String>(resource.getParameters());
            for (String parameter : new ArrayList<String>(parameters.keySet()))
            {
                if (GlassFish3xInstalledLocalDeployer
                    .MANAGED_EXECUTOR_SERVICE_ATTRIBUTES.contains(parameter))
                {
                    args.add("--" + parameter);
                    args.add(parameters.get(parameter));
                    parameters.remove(parameter);
                }
            }
            // Let's check for remaining parameters
            if (!parameters.isEmpty())
            {
                args.add("--property");
                StringBuilder propertyBuilder = new StringBuilder();
                for (String parameterName : resource.getParameterNames())
                {
                    if (!GlassFish3xInstalledLocalDeployer
                        .MANAGED_EXECUTOR_SERVICE_ATTRIBUTES.contains(parameterName))
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

    /**
     * @return <code>true</code> if the the GlassFish container deployed on is Jakarta EE,
     * <code>false</code> in the base implementation.
     */
    protected boolean isJakartaEe()
    {
        return false;
    }
}
