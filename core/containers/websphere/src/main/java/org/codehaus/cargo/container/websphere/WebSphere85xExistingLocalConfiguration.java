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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.configuration.script.ScriptCommand;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractExistingLocalConfiguration;
import org.codehaus.cargo.container.websphere.internal.WebSphere85xExistingLocalConfigurationCapability;
import org.codehaus.cargo.container.websphere.internal.configuration.WebSphereJythonConfigurationFactory;
import org.codehaus.cargo.container.websphere.util.ByteUnit;
import org.codehaus.cargo.container.websphere.util.JvmArguments;
import org.codehaus.cargo.util.CargoException;

/**
 * WebSphere 8.5.x existing {@link org.codehaus.cargo.container.configuration.Configuration}
 * implementation.
 */
public class WebSphere85xExistingLocalConfiguration extends AbstractExistingLocalConfiguration
    implements WebSphereConfiguration
{
    /**
     * Capability of the WebSphere existing configuration.
     */
    private static final ConfigurationCapability CAPABILITY =
        new WebSphere85xExistingLocalConfigurationCapability();

    /**
     * Configuration factory for creating WebSphere jython configuration scripts.
     */
    private WebSphereJythonConfigurationFactory factory;

    /**
     * {@inheritDoc}
     * @see AbstractExistingLocalConfiguration#AbstractExistingLocalConfiguration(String)
     */
    public WebSphere85xExistingLocalConfiguration(String dir)
    {
        super(dir);
        factory = new WebSphereJythonConfigurationFactory(this);

        setProperty(ServletPropertySet.PORT, "9080");
        setProperty(WebSpherePropertySet.ADMINISTRATION_PORT, "9060");

        setProperty(WebSpherePropertySet.ADMIN_USERNAME, "websphere");
        setProperty(WebSpherePropertySet.ADMIN_PASSWORD, "websphere");

        setProperty(WebSpherePropertySet.PROFILE, "cargoProfile");
        setProperty(WebSpherePropertySet.CELL, "cargoNodeCell");
        setProperty(WebSpherePropertySet.NODE, "cargoNode");
        setProperty(WebSpherePropertySet.SERVER, "cargoServer");

        setProperty(WebSpherePropertySet.CLASSLOADER_MODE, "PARENT_FIRST");
        setProperty(WebSpherePropertySet.WAR_CLASSLOADER_POLICY, "MULTIPLE");

        setProperty(WebSpherePropertySet.OVERWRITE_EXISTING_CONFIGURATION,
                WebSphereExistingConfigurationSetting.ALL.getName());

        setProperty(WebSpherePropertySet.ONLINE_DEPLOYMENT, "false");
        setProperty(WebSpherePropertySet.JYTHON_SCRIPT_REPLACE_PROPERTIES, "false");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConfigurationCapability getCapability()
    {
        return CAPABILITY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doConfigure(LocalContainer container) throws Exception
    {
        WebSphere85xInstalledLocalContainer wsContainer =
                (WebSphere85xInstalledLocalContainer) container;

        WebSphereExistingConfigurationSetting configurationSetting =
                WebSphereExistingConfigurationSetting.getByName(getPropertyValue(
                        WebSpherePropertySet.OVERWRITE_EXISTING_CONFIGURATION));

        if (configurationSetting == null)
        {
            throw new CargoException("Value for property "
                    + WebSpherePropertySet.OVERWRITE_EXISTING_CONFIGURATION + " is invalid: "
                    + getPropertyValue(WebSpherePropertySet.OVERWRITE_EXISTING_CONFIGURATION));
        }

        getLogger().info("Updating existing profile.", this.getClass().getName());

        List<ScriptCommand> wsAdminCommands = new ArrayList<ScriptCommand>();

        // add JVM configuration
        if (configurationSetting == WebSphereExistingConfigurationSetting.ALL
                || configurationSetting == WebSphereExistingConfigurationSetting.JVM)
        {
            String jvmArgs = getPropertyValue(GeneralPropertySet.JVMARGS);
            JvmArguments parsedArguments = JvmArguments.parseArguments(jvmArgs);

            wsAdminCommands.add(factory.setJvmPropertyScript("initialHeapSize",
                    Long.toString(parsedArguments.getInitialHeap(ByteUnit.MEGABYTES))));
            wsAdminCommands.add(factory.setJvmPropertyScript("maximumHeapSize",
                    Long.toString(parsedArguments.getMaxHeap(ByteUnit.MEGABYTES))));
            wsAdminCommands.add(factory.setJvmPropertyScript("genericJvmArguments",
                    parsedArguments.getGenericArgs()));
        }

        // add system properties
        if (configurationSetting == WebSphereExistingConfigurationSetting.ALL
                || configurationSetting == WebSphereExistingConfigurationSetting.SystemProperties)
        {
            for (Map.Entry<String, String> systemProperty
                    : wsContainer.getSystemProperties().entrySet())
            {
                wsAdminCommands.add(factory.setSystemPropertyScript(systemProperty.getKey(),
                        systemProperty.getValue()));
            }
        }

        // redeploy deployables
        List<String> extraLibraries = Arrays.asList(wsContainer.getExtraClasspath());
        for (Deployable deployable : getDeployables())
        {
            wsAdminCommands.add(factory.undeployDeployableScript(deployable));
            wsAdminCommands.addAll(factory.deployDeployableScript(deployable, extraLibraries));
        }

        //save and activate
        wsAdminCommands.add(factory.saveSyncScript());

        wsContainer.executeScript(wsAdminCommands);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void verify()
    {
        // Nothing to verify right now...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "WebSphere 8.5.x Existing Configuration";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebSphereJythonConfigurationFactory getFactory()
    {
        return factory;
    }
}
