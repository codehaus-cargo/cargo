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
package org.codehaus.cargo.container.wildfly.internal.configuration.factory;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.builder.ConfigurationEntryType;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.configuration.script.ScriptCommand;
import org.codehaus.cargo.container.property.TransactionSupport;
import org.codehaus.cargo.container.wildfly.internal.configuration.commands.wildfly9.custom.CustomWildFlyScriptCommand;
import org.codehaus.cargo.container.wildfly.internal.configuration.commands.wildfly9.domain.ConfigurePortsScriptCommand;
import org.codehaus.cargo.container.wildfly.internal.configuration.commands.wildfly9.domain.LoggingScriptCommand;
import org.codehaus.cargo.container.wildfly.internal.configuration.commands.wildfly9.domain.SystemPropertyScriptCommand;
import org.codehaus.cargo.container.wildfly.internal.configuration.commands.wildfly9.resource.DataSourceScriptCommand;
import org.codehaus.cargo.container.wildfly.internal.configuration.commands.wildfly9.resource.DriverScriptCommand;
import org.codehaus.cargo.container.wildfly.internal.configuration.commands.wildfly9.resource.JmsQueueScriptCommand;
import org.codehaus.cargo.container.wildfly.internal.configuration.commands.wildfly9.resource.JmsTopicScriptCommand;
import org.codehaus.cargo.container.wildfly.internal.configuration.commands.wildfly9.resource.MailScriptCommand;
import org.codehaus.cargo.container.wildfly.internal.configuration.commands.wildfly9.resource.XaDataSourceScriptCommand;
import org.codehaus.cargo.container.wildfly.internal.configuration.commands.wildfly9.resource.XaDriverScriptCommand;
import org.codehaus.cargo.container.wildfly.internal.configuration.commands.wildfly9.server.BatchScriptCommand;
import org.codehaus.cargo.container.wildfly.internal.configuration.commands.wildfly9.server.RunBatchScriptCommand;
import org.codehaus.cargo.container.wildfly.internal.configuration.commands.wildfly9.server.StartEmbedServerScriptCommand;
import org.codehaus.cargo.util.CargoException;

/**
 * WildFly9x CLI configuration factory returning specific configuration scripts.
 */
public class WildFly9xCliConfigurationFactory extends WildFly8xCliConfigurationFactory
{
    /**
     * Type to resource command script class map.
     */
    private static Map<String, Class<? extends ScriptCommand>> resourceMap =
            new HashMap<String, Class<? extends ScriptCommand>>();

    /**
     * Path to configuration script resources.
     */
    private static final String RESOURCE_PATH =
            "org/codehaus/cargo/container/internal/resources/wildfly-9/cli/";

    static
    {
        resourceMap.put(ConfigurationEntryType.JMS_QUEUE, JmsQueueScriptCommand.class);
        resourceMap.put(ConfigurationEntryType.JMS_TOPIC, JmsTopicScriptCommand.class);
        resourceMap.put(ConfigurationEntryType.MAIL_SESSION, MailScriptCommand.class);
    }

    /**
     * Sets configuration containing all needed information for building configuration scripts.
     * 
     * @param configuration Container configuration.
     */
    public WildFly9xCliConfigurationFactory(Configuration configuration)
    {
        super(configuration);
    }

    /* Server configuration*/

    /**
     * @return Start embed server CLI script.
     */
    public ScriptCommand startEmbedServerScript()
    {
        return new StartEmbedServerScriptCommand(configuration, RESOURCE_PATH);
    }

    /**
     * @return Batch CLI script.
     */
    public ScriptCommand batchScript()
    {
        return new BatchScriptCommand(configuration, RESOURCE_PATH);
    }

    /**
     * @return Run batch CLI script.
     */
    public ScriptCommand runBatchScript()
    {
        return new RunBatchScriptCommand(configuration, RESOURCE_PATH);
    }

    /* Domain configuration*/

    /**
     * @return Configure ports CLI script.
     */
    public ScriptCommand configurePortsScript()
    {
        return new ConfigurePortsScriptCommand(configuration, RESOURCE_PATH);
    }

    /**
     * @return Configure logger CLI script.
     */
    public ScriptCommand loggingScript()
    {
        return new LoggingScriptCommand(configuration, RESOURCE_PATH);
    }

    /**
     * @param name System property name.
     * @param value System property value.
     * @return Configure logger CLI script.
     */
    public ScriptCommand systemPropertyScript(String name, String value)
    {
        return new SystemPropertyScriptCommand(configuration, RESOURCE_PATH, name, value);
    }

    /* Resources configuration*/

    /**
     * @param ds DataSource to be configured.
     * @return Configure DataSource CLI script.
     */
    public ScriptCommand dataSourceScript(DataSource ds)
    {
        if (ConfigurationEntryType.XA_DATASOURCE.equals(ds.getConnectionType()))
        {
            return new XaDataSourceScriptCommand(configuration, RESOURCE_PATH, ds);
        }
        else
        {
            return new DataSourceScriptCommand(configuration, RESOURCE_PATH, ds);
        }
    }

    /**
     * @param ds DataSource to be configured.
     * @param driverModule Module containing DataSource driver.
     * @return Configure DataSource Driver CLI script.
     */
    public ScriptCommand dataSourceDriverScript(DataSource ds, String driverModule)
    {
        if (TransactionSupport.XA_TRANSACTION.equals(ds.getTransactionSupport()))
        {
            return new XaDriverScriptCommand(configuration, RESOURCE_PATH, ds, driverModule);
        }
        else
        {
            return new DriverScriptCommand(configuration, RESOURCE_PATH, ds, driverModule);
        }
    }

    /**
     * @param resource Resource.
     * @return Create resource CLI script.
     */
    public ScriptCommand resourceScript(Resource resource)
    {
        Class<? extends ScriptCommand> resourceClass = resourceMap.get(resource.getType());
        if (resourceClass == null)
        {
            throw new CargoException("Resources of type " + resource.getType()
                + " are not supported by the WildFly CLI configuration factory.");
        }
        ScriptCommand newInstance = null;
        try
        {
            newInstance = resourceClass.getConstructor(Configuration.class,
                    String.class, Resource.class).newInstance(configuration,
                            RESOURCE_PATH, resource);
        }
        catch (Exception e)
        {
            throw new CargoException("Failed instantiation of resource command.", e);
        }

        return newInstance;
    }

    /* Custom configuration*/

    /**
     * @param scriptPath Path to script file.
     * @return Custom CLI script.
     */
    public ScriptCommand customScript(String scriptPath)
    {
        return new CustomWildFlyScriptCommand(configuration, scriptPath);
    }
}
