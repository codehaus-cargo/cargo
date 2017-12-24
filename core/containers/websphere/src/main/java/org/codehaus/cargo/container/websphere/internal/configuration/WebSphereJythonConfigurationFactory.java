/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol, 2012-2017 Ali Tokmen.
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
package org.codehaus.cargo.container.websphere.internal.configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.configuration.script.ScriptCommand;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.property.User;
import org.codehaus.cargo.container.spi.configuration.AbstractLocalConfiguration;
import org.codehaus.cargo.container.websphere.internal.configuration.commands.ImportWsadminlibScriptCommand;
import org.codehaus.cargo.container.websphere.internal.configuration.commands.deployment.AddSharedLibraryToDeployableScriptCommand;
import org.codehaus.cargo.container.websphere.internal.configuration.commands.deployment.DeployDeployableScriptCommand;
import org.codehaus.cargo.container.websphere.internal.configuration.commands.deployment.DeploySharedLibraryScriptCommand;
import org.codehaus.cargo.container.websphere.internal.configuration.commands.deployment.StartDeployableScriptCommand;
import org.codehaus.cargo.container.websphere.internal.configuration.commands.deployment.UndeployDeployableScriptCommand;
import org.codehaus.cargo.container.websphere.internal.configuration.commands.domain.LoggingScriptCommand;
import org.codehaus.cargo.container.websphere.internal.configuration.commands.domain.MiscConfigurationScriptCommand;
import org.codehaus.cargo.container.websphere.internal.configuration.commands.domain.SaveSyncScriptCommand;
import org.codehaus.cargo.container.websphere.internal.configuration.commands.domain.SetGlobalSecurityPropertyScriptCommand;
import org.codehaus.cargo.container.websphere.internal.configuration.commands.domain.SetJvmPropertyScriptCommand;
import org.codehaus.cargo.container.websphere.internal.configuration.commands.domain.SetSessionManagementPropertyScriptCommand;
import org.codehaus.cargo.container.websphere.internal.configuration.commands.domain.SetSystemPropertyScriptCommand;
import org.codehaus.cargo.container.websphere.internal.configuration.commands.resource.DataSourceConnectionPropertyScriptCommand;
import org.codehaus.cargo.container.websphere.internal.configuration.commands.resource.DataSourceScriptCommand;
import org.codehaus.cargo.container.websphere.internal.configuration.commands.resource.JmsConnectionFactoryScriptCommand;
import org.codehaus.cargo.container.websphere.internal.configuration.commands.resource.JmsQueueScriptCommand;
import org.codehaus.cargo.container.websphere.internal.configuration.commands.resource.JmsSiBusMemberScriptCommand;
import org.codehaus.cargo.container.websphere.internal.configuration.commands.resource.JmsSiBusScriptCommand;
import org.codehaus.cargo.container.websphere.internal.configuration.commands.user.AddUserToGroupScriptCommand;
import org.codehaus.cargo.container.websphere.internal.configuration.commands.user.CreateGroupScriptCommand;
import org.codehaus.cargo.container.websphere.internal.configuration.commands.user.CreateUserScriptCommand;
import org.codehaus.cargo.util.CargoException;

/**
 * WebSphere configuration factory returning specific jython configuration scripts.
 */
public class WebSphereJythonConfigurationFactory
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
            AbstractLocalConfiguration.RESOURCE_PATH + "websphere85x/commands/";

    /**
     * Container configuration.
     */
    private Configuration configuration;

    static
    {
        resourceMap.put(WebSphereConfigurationEntryType.JMS_SIBUS, JmsSiBusScriptCommand.class);
        resourceMap.put(WebSphereConfigurationEntryType.JMS_SIBUS_MEMBER,
                JmsSiBusMemberScriptCommand.class);
        resourceMap.put(WebSphereConfigurationEntryType.JMS_CONNECTION_FACTORY,
                JmsConnectionFactoryScriptCommand.class);
        resourceMap.put(WebSphereConfigurationEntryType.JMS_QUEUE,
                JmsQueueScriptCommand.class);
    }

    /**
     * Sets configuration containing all needed information for building configuration scripts.
     *
     * @param configuration Container configuration.
     */
    public WebSphereJythonConfigurationFactory(Configuration configuration)
    {
        this.configuration = configuration;
    }

    /**
     * @param wsadminlibPath Path to wsadminlib script.
     * @return Import wsadminlib jython script.
     */
    public ScriptCommand importWsadminlibScript(String wsadminlibPath)
    {
        return new ImportWsadminlibScriptCommand(configuration, RESOURCE_PATH, wsadminlibPath);
    }

    /* Domain configuration*/

    /**
     * @return Save and sync wsadminlib jython script.
     */
    public ScriptCommand saveSyncScript()
    {
        return new SaveSyncScriptCommand(configuration, RESOURCE_PATH);
    }

    /**
     * @param propertyName Name of JVM property.
     * @param propertyValue Value of JVM property.
     * @return Set JVM property jython script.
     */
    public ScriptCommand setJvmPropertyScript(String propertyName, String propertyValue)
    {
        return new SetJvmPropertyScriptCommand(configuration, RESOURCE_PATH, propertyName,
                propertyValue);
    }

    /**
     * @param propertyName Name of system property.
     * @param propertyValue Value of system property.
     * @return Set system property jython script.
     */
    public ScriptCommand setSystemPropertyScript(String propertyName, String propertyValue)
    {
        return new SetSystemPropertyScriptCommand(configuration, RESOURCE_PATH, propertyName,
                propertyValue);
    }

    /**
     * @param propertyName Name of global security property.
     * @param propertyValue Value of global security property.
     * @return Set global security property jython script.
     */
    public ScriptCommand setGlobalSecurityPropertyScript(String propertyName, String propertyValue)
    {
        return new SetGlobalSecurityPropertyScriptCommand(configuration, RESOURCE_PATH,
                propertyName, propertyValue);
    }

    /**
     * @param propertyName Name of session management property.
     * @param propertyValue Value of session management property.
     * @return Set session management property jython script.
     */
    public ScriptCommand setSessionManagementPropertyScript(String propertyName,
            String propertyValue)
    {
        return new SetSessionManagementPropertyScriptCommand(configuration, RESOURCE_PATH,
                propertyName, propertyValue);
    }

    /**
     * @return Miscellaneous configuration jython script.
     */
    public ScriptCommand miscConfigurationScript()
    {
        return new MiscConfigurationScriptCommand(configuration, RESOURCE_PATH);
    }

    /**
     * @return Logging configuration jython script.
     */
    public ScriptCommand loggingScript()
    {
        return new LoggingScriptCommand(configuration, RESOURCE_PATH);
    }

    /* Deployment configuration*/

    /**
     * @param sharedLibraryPath Shared library to be deployed.
     * @return Deploy shared library jython script.
     */
    public ScriptCommand deploySharedLibraryScript(String sharedLibraryPath)
    {
        return new DeploySharedLibraryScriptCommand(configuration, RESOURCE_PATH,
                sharedLibraryPath);
    }

    /**
     * @param deployable Deployable to be deployed.
     * @return Deploy deployable jython script.
     */
    public ScriptCommand deployDeployableScript(Deployable deployable)
    {
        return new DeployDeployableScriptCommand(configuration, RESOURCE_PATH, deployable);
    }

    /**
     * @param deployable Deployable to be started.
     * @return Start deployable jython script.
     */
    public ScriptCommand startDeployableScript(Deployable deployable)
    {
        return new StartDeployableScriptCommand(configuration, RESOURCE_PATH, deployable);
    }

    /**
     * @param deployable Deployable to be deployed.
     * @param sharedLibraries Shared libraries used by this deployable.
     * @return Deploy deployable using shared libraries jython script.
     */
    public List<ScriptCommand> deployDeployableScript(Deployable deployable,
            Collection<String> sharedLibraries)
    {
        List<ScriptCommand> scriptCommands = new ArrayList<ScriptCommand>();
        scriptCommands.add(deployDeployableScript(deployable));

        for (String sharedLibrary : sharedLibraries)
        {
            scriptCommands.add(new AddSharedLibraryToDeployableScriptCommand(configuration,
                    RESOURCE_PATH, deployable, sharedLibrary));
        }

        return scriptCommands;
    }

    /**
     * @param deployable Deployable to be undeployed.
     * @return Undeploy deployable jython script.
     */
    public ScriptCommand undeployDeployableScript(Deployable deployable)
    {
        return new UndeployDeployableScriptCommand(configuration, RESOURCE_PATH, deployable);
    }

    /* User/group configuration*/

    /**
     * @param user User to be created.
     * @return Create user jython script.
     */
    public List<ScriptCommand> createUserScript(User user)
    {
        List<ScriptCommand> scriptCommands = new ArrayList<ScriptCommand>();

        scriptCommands.add(new CreateUserScriptCommand(configuration, RESOURCE_PATH, user));

        for (String role : user.getRoles())
        {
            scriptCommands.add(new CreateGroupScriptCommand(configuration, RESOURCE_PATH, role));
            scriptCommands.add(new AddUserToGroupScriptCommand(configuration, RESOURCE_PATH,
                    user, role));
        }

        return scriptCommands;
    }

    /* DataSource/Resource configuration*/

    /**
     * @param dataSource DataSource to be created.
     * @param sharedLibraries Shared libraries containing database drivers.
     * @return Create datasource jython script.
     */
    public List<ScriptCommand> createDataSourceScript(DataSource dataSource,
            Collection<String> sharedLibraries)
    {
        List<ScriptCommand> scriptCommands = new ArrayList<ScriptCommand>();

        scriptCommands.add(new DataSourceScriptCommand(configuration, RESOURCE_PATH, dataSource,
                sharedLibraries));

        for (Entry<Object, Object> property : dataSource.getConnectionProperties().entrySet())
        {
            scriptCommands.add(new DataSourceConnectionPropertyScriptCommand(configuration,
                    RESOURCE_PATH, dataSource, property));
        }

        return scriptCommands;
    }

    /**
     * @param resource Resource.
     * @return Create resource jython script.
     */
    public ScriptCommand createResourceScript(Resource resource)
    {
        Class<? extends ScriptCommand> resourceClass = resourceMap.get(resource.getType());
        ScriptCommand newInstance = null;

        if (resourceClass == null)
        {
            throw new CargoException("WebSphere doesn't support resource type "
                    + resource.getType());
        }

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
}
