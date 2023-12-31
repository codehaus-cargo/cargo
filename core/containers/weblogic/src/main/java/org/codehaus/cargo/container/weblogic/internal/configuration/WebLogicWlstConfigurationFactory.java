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
package org.codehaus.cargo.container.weblogic.internal.configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.builder.ConfigurationEntryType;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.configuration.script.ScriptCommand;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.property.User;
import org.codehaus.cargo.container.weblogic.internal.configuration.commands.deployment.DeployDeployableOnlineScriptCommand;
import org.codehaus.cargo.container.weblogic.internal.configuration.commands.deployment.DeployDeployableScriptCommand;
import org.codehaus.cargo.container.weblogic.internal.configuration.commands.deployment.UndeployDeployableOnlineScriptCommand;
import org.codehaus.cargo.container.weblogic.internal.configuration.commands.deployment.UndeployDeployableScriptCommand;
import org.codehaus.cargo.container.weblogic.internal.configuration.commands.domain.CreateDomainScriptCommand;
import org.codehaus.cargo.container.weblogic.internal.configuration.commands.domain.JtaScriptCommand;
import org.codehaus.cargo.container.weblogic.internal.configuration.commands.domain.LoggingScriptCommand;
import org.codehaus.cargo.container.weblogic.internal.configuration.commands.domain.PasswordValidatorScriptCommand;
import org.codehaus.cargo.container.weblogic.internal.configuration.commands.domain.ReadDomainOfflineScriptCommand;
import org.codehaus.cargo.container.weblogic.internal.configuration.commands.domain.ReadDomainOnlineScriptCommand;
import org.codehaus.cargo.container.weblogic.internal.configuration.commands.domain.ShutdownDomainScriptCommand;
import org.codehaus.cargo.container.weblogic.internal.configuration.commands.domain.SslScriptCommand;
import org.codehaus.cargo.container.weblogic.internal.configuration.commands.domain.UpdateDomainOfflineScriptCommand;
import org.codehaus.cargo.container.weblogic.internal.configuration.commands.domain.UpdateDomainOnlineScriptCommand;
import org.codehaus.cargo.container.weblogic.internal.configuration.commands.domain.WriteDomainScriptCommand;
import org.codehaus.cargo.container.weblogic.internal.configuration.commands.resource.DataSourceConnectionPropertyScriptCommand;
import org.codehaus.cargo.container.weblogic.internal.configuration.commands.resource.DataSourceScriptCommand;
import org.codehaus.cargo.container.weblogic.internal.configuration.commands.resource.JmsConnectionFactoryScriptCommand;
import org.codehaus.cargo.container.weblogic.internal.configuration.commands.resource.JmsModuleScriptCommand;
import org.codehaus.cargo.container.weblogic.internal.configuration.commands.resource.JmsQueueScriptCommand;
import org.codehaus.cargo.container.weblogic.internal.configuration.commands.resource.JmsServerScriptCommand;
import org.codehaus.cargo.container.weblogic.internal.configuration.commands.resource.JmsSubdeploymentScriptCommand;
import org.codehaus.cargo.container.weblogic.internal.configuration.commands.resource.MailSessionScriptCommand;
import org.codehaus.cargo.container.weblogic.internal.configuration.commands.user.AddUserToGroupScriptCommand;
import org.codehaus.cargo.container.weblogic.internal.configuration.commands.user.CreateGroupScriptCommand;
import org.codehaus.cargo.container.weblogic.internal.configuration.commands.user.CreateUserScriptCommand;
import org.codehaus.cargo.util.CargoException;

/**
 * WLST configuration factory returning specific configuration scripts.
 */
public class WebLogicWlstConfigurationFactory
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
            "org/codehaus/cargo/container/internal/resources/weblogicWlst/";

    /**
     * Container configuration.
     */
    private Configuration configuration;

    static
    {
        resourceMap.put(WebLogicConfigurationEntryType.JMS_SERVER, JmsServerScriptCommand.class);
        resourceMap.put(WebLogicConfigurationEntryType.JMS_MODULE, JmsModuleScriptCommand.class);
        resourceMap.put(WebLogicConfigurationEntryType.JMS_SUBDEPLOYMENT,
                JmsSubdeploymentScriptCommand.class);
        resourceMap.put(WebLogicConfigurationEntryType.JMS_CONNECTION_FACTORY,
                JmsConnectionFactoryScriptCommand.class);
        resourceMap.put(WebLogicConfigurationEntryType.JMS_QUEUE, JmsQueueScriptCommand.class);
        resourceMap.put(ConfigurationEntryType.MAIL_SESSION, MailSessionScriptCommand.class);
    }

    /**
     * Sets configuration containing all needed information for building configuration scripts.
     * 
     * @param configuration Container configuration.
     */
    public WebLogicWlstConfigurationFactory(Configuration configuration)
    {
        this.configuration = configuration;
    }

    /* Domain configuration*/

    /**
     * @param weblogicHome WebLogic home.
     * @return Create domain WLST script.
     */
    public ScriptCommand createDomainScript(String weblogicHome)
    {
        return new CreateDomainScriptCommand(configuration, RESOURCE_PATH, weblogicHome);
    }

    /**
     * @return Read domain offline WLST script.
     */
    public ScriptCommand readDomainOfflineScript()
    {
        return new ReadDomainOfflineScriptCommand(configuration, RESOURCE_PATH);
    }

    /**
     * @return Read domain online WLST script.
     */
    public ScriptCommand readDomainOnlineScript()
    {
        return new ReadDomainOnlineScriptCommand(configuration, RESOURCE_PATH);
    }

    /**
     * @return Update offline domain WLST script.
     */
    public ScriptCommand updateDomainOfflineScript()
    {
        return new UpdateDomainOfflineScriptCommand(configuration, RESOURCE_PATH);
    }

    /**
     * @return Update online domain WLST script.
     */
    public ScriptCommand updateDomainOnlineScript()
    {
        return new UpdateDomainOnlineScriptCommand(configuration, RESOURCE_PATH);
    }

    /**
     * @return Write domain WLST script.
     */
    public ScriptCommand writeDomainScript()
    {
        return new WriteDomainScriptCommand(configuration, RESOURCE_PATH);
    }

    /**
     * @return Shutdown domain WLST script.
     */
    public ScriptCommand shutdownDomainScript()
    {
        return new ShutdownDomainScriptCommand(configuration, RESOURCE_PATH);
    }

    /**
     * @return Logging WLST script.
     */
    public ScriptCommand loggingScript()
    {
        return new LoggingScriptCommand(configuration, RESOURCE_PATH);
    }

    /**
     * @return Configure SSL WLST script.
     */
    public ScriptCommand sslScript()
    {
        return new SslScriptCommand(configuration, RESOURCE_PATH);
    }

    /**
     * @return Configure JTA WLST script.
     */
    public ScriptCommand jtaScript()
    {
        return new JtaScriptCommand(configuration, RESOURCE_PATH);
    }

    /**
     * @return Configure password validator script.
     */
    public ScriptCommand passwordValidatorScript()
    {
        return new PasswordValidatorScriptCommand(configuration, RESOURCE_PATH);
    }

    /* Deployment configuration*/

    /**
     * @param deployable Deployable to be deployed.
     * @return Deploy deployable WLST script.
     */
    public ScriptCommand deployDeployableScript(Deployable deployable)
    {
        return new DeployDeployableScriptCommand(configuration, RESOURCE_PATH, deployable);
    }

    /**
     * @param deployable Deployable to be deployed.
     * @return Deploy deployable online WLST script.
     */
    public ScriptCommand deployDeployableOnlineScript(Deployable deployable)
    {
        return new DeployDeployableOnlineScriptCommand(configuration, RESOURCE_PATH, deployable);
    }

    /**
     * @param deployable Deployable to be undeployed.
     * @return Undeploy deployable WLST script.
     */
    public ScriptCommand undeployDeployableScript(Deployable deployable)
    {
        return new UndeployDeployableScriptCommand(configuration, RESOURCE_PATH, deployable);
    }

    /**
     * @param deployable Deployable to be undeployed.
     * @return Undeploy deployable online WLST script.
     */
    public ScriptCommand undeployDeployableOnlineScript(Deployable deployable)
    {
        return new UndeployDeployableOnlineScriptCommand(configuration, RESOURCE_PATH, deployable);
    }

    /* Resource configuration*/

    /**
     * @param ds DataSource.
     * @return Create datasource WLST script.
     */
    public Collection<ScriptCommand> dataSourceScript(DataSource ds)
    {
        Collection<ScriptCommand> script = new ArrayList<ScriptCommand>();

        script.add(new DataSourceScriptCommand(configuration, RESOURCE_PATH, ds));

        for (Entry<Object, Object> driverProperty : ds.getConnectionProperties().entrySet())
        {
            script.add(new DataSourceConnectionPropertyScriptCommand(configuration, RESOURCE_PATH,
                    ds, driverProperty));
        }

        return script;
    }

    /**
     * @param resource Resource.
     * @return Create datasource WLST script.
     */
    public ScriptCommand resourceScript(Resource resource)
    {
        Class<? extends ScriptCommand> resourceClass = resourceMap.get(resource.getType());
        if (resourceClass == null)
        {
            throw new CargoException("Resources of type " + resource.getType()
                + " are not supported by the WebLogic WSLT configuration factory");
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

    /* User configuration*/

    /**
     * @param user User to be created.
     * @return Create user WLST script.
     */
    public ScriptCommand createUserScript(User user)
    {
        return new CreateUserScriptCommand(configuration, RESOURCE_PATH, user);
    }

    /**
     * @param groupRole Group role.
     * @return Create group WLST script.
     */
    public ScriptCommand createGroupScript(String groupRole)
    {
        return new CreateGroupScriptCommand(configuration, RESOURCE_PATH, groupRole);
    }

    /**
     * @param user User to be paired with groups.
     * @return Pair user with groups WLST script.
     */
    public Collection<ScriptCommand> addUserToGroupsScript(User user)
    {
        Collection<ScriptCommand> script = new ArrayList<ScriptCommand>();

        for (String role : user.getRoles())
        {
            script.add(new AddUserToGroupScriptCommand(configuration, RESOURCE_PATH, user, role));
        }

        return script;
    }
}
