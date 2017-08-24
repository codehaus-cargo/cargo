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
package org.codehaus.cargo.container.websphere;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.configuration.script.ScriptCommand;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.internal.util.ComplexPropertyUtils;
import org.codehaus.cargo.container.internal.util.PropertyUtils;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration;
import org.codehaus.cargo.container.websphere.internal.WebSphere85xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.websphere.internal.configuration.WebSphereJythonConfigurationFactory;
import org.codehaus.cargo.container.websphere.internal.configuration.rules.WebSphereResourceRules;
import org.codehaus.cargo.container.websphere.util.ByteUnit;
import org.codehaus.cargo.container.websphere.util.JvmArguments;
import org.codehaus.cargo.container.websphere.util.WebSphereResourceComparator;
import org.codehaus.cargo.util.CargoException;

/**
 * WebSphere 8.5.x standalone
 * {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration} implementation.
 */
public class WebSphere85xStandaloneLocalConfiguration extends AbstractStandaloneLocalConfiguration
    implements WebSphereConfiguration
{
    /**
     * Capability of the WebSphere standalone configuration.
     */
    private static ConfigurationCapability capability =
        new WebSphere85xStandaloneLocalConfigurationCapability();

    /**
     * WebSphere container.
     */
    private WebSphere85xInstalledLocalContainer wsContainer;

    /**
     * Configuration factory for creating WebSphere jython configuration scripts.
     */
    private WebSphereJythonConfigurationFactory factory;

    /**
     * {@inheritDoc}
     * @see AbstractStandaloneLocalConfiguration#AbstractStandaloneLocalConfiguration(String)
     */
    public WebSphere85xStandaloneLocalConfiguration(String dir)
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
        setProperty(WebSpherePropertySet.APPLICATION_SECURITY, "true");
        setProperty(WebSpherePropertySet.LOGGING_ROLLOVER, "50");

        setProperty(WebSpherePropertySet.JMS_SIBUS, "jmsBus");

        setProperty(WebSpherePropertySet.ONLINE_DEPLOYMENT, "false");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConfigurationCapability getCapability()
    {
        return capability;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doConfigure(LocalContainer container) throws Exception
    {
        this.wsContainer = (WebSphere85xInstalledLocalContainer) container;

        // delete old profile and create new profile
        deleteOldProfile();
        createNewProfile(container);

        getLogger().info("Configuring profile.", this.getClass().getName());

        List<ScriptCommand> commands = new ArrayList<ScriptCommand>();

        // add miscellaneous configuration
        commands.add(factory.miscConfigurationScript());

        // add logging configuration
        commands.add(factory.loggingScript());

        // add JVM configuration
        commands.addAll(createJvmPropertiesScripts(wsContainer));

        // add system properties
        for (Map.Entry<String, String> systemProperty
                : wsContainer.getSystemProperties().entrySet())
        {
            if (systemProperty.getValue() != null)
            {
                commands.add(factory.setSystemPropertyScript(systemProperty.getKey(),
                        systemProperty.getValue()));
            }
        }

        // add global security properties
        commands.addAll(createGlobalSecurityPropertiesScripts());

        // add session management properties
        commands.addAll(createSessionManagementPropertiesScripts());

        // add shared libraries
        List<String> extraLibraries = Arrays.asList(wsContainer.getExtraClasspath());
        for (String extraLibrary : extraLibraries)
        {
            commands.add(factory.deploySharedLibraryScript(extraLibrary));
        }

        // create datasources
        for (DataSource dataSource : getDataSources())
        {
            commands.addAll(factory.createDataSourceScript(dataSource, extraLibraries));
        }

        // add missing resources to list of resources
        WebSphereResourceRules.addMissingJmsResources(this);

        // sort resources
        WebSphereResourceComparator resourceComparator = new WebSphereResourceComparator();
        List<Resource> resources = getResources();
        Collections.sort(resources, resourceComparator);

        // create resources
        for (Resource resource : getResources())
        {
            commands.add(factory.createResourceScript(resource));
        }

        String onlineDeploymentValue = getPropertyValue(WebSpherePropertySet.ONLINE_DEPLOYMENT);
        boolean onlineDeployment = Boolean.parseBoolean(onlineDeploymentValue);
        if (!onlineDeployment)
        {
            // deploy deployables
            for (Deployable deployable : getDeployables())
            {
                commands.addAll(factory.deployDeployableScript(deployable, extraLibraries));
            }
        }

        //save and activate
        commands.add(factory.saveSyncScript());

        wsContainer.executeScript(commands);

        // Execute offline jython scripts
        String scriptPaths = getPropertyValue(WebSpherePropertySet.JYTHON_SCRIPT_OFFLINE);
        List<String> scriptPathList = ComplexPropertyUtils.parseProperty(scriptPaths, "|");
        wsContainer.executeScriptFiles(scriptPathList);
    }

    /**
     * Delete old profile.
     * @throws Exception if any error is raised during deleting of profile
     */
    private void deleteOldProfile() throws Exception
    {
        getLogger().info("Deleting old profile.", this.getClass().getName());

        // Delete profile in WebSphere
        wsContainer.runManageProfileCommand(
            "-delete",
            "-profileName",
            getPropertyValue(WebSpherePropertySet.PROFILE));

        // Profile directory has to be deleted too.
        getLogger().debug("Deleting profile folder " + getHome(), this.getClass().getName());
        getFileHandler().delete(getHome());

        if (getFileHandler().isDirectory(getHome()))
        {
            throw new CargoException("Directory " + getHome() + " cannot be deleted");
        }

        // Update profile informations in WebSphere
        wsContainer.runManageProfileCommand("-validateAndUpdateRegistry");
    }

    /**
     * Create new profile.
     * @param container Container.
     * @throws Exception if any error is raised during deleting of profile
     */
    private void createNewProfile(Container container) throws Exception
    {
        File portsFile = File.createTempFile("cargo-websphere-portdef-", ".properties");
        getResourceUtils().copyResource(RESOURCE_PATH + container.getId() + "/portdef.props",
            portsFile, createFilterChain(), "ISO-8859-1");

        try
        {
            getLogger().info("Creating new profile.", this.getClass().getName());
            wsContainer.runManageProfileCommand(
                "-create",
                "-profileName",
                getPropertyValue(WebSpherePropertySet.PROFILE),
                "-profilePath",
                getHome(),
                "-nodeName",
                getPropertyValue(WebSpherePropertySet.NODE),
                "-cellName",
                getPropertyValue(WebSpherePropertySet.CELL),
                "-serverName",
                getPropertyValue(WebSpherePropertySet.SERVER),
                "-portsFile",
                portsFile.getAbsolutePath(),
                "-winserviceCheck",
                "false",
                "-enableService",
                "false",
                "-enableAdminSecurity",
                "true",
                "-adminUserName",
                getPropertyValue(WebSpherePropertySet.ADMIN_USERNAME),
                "-adminPassword",
                getPropertyValue(WebSpherePropertySet.ADMIN_PASSWORD));
        }
        finally
        {
            portsFile.delete();
        }
    }

    /**
     * Create JVM properties.
     * @param container Container.
     * @return Scripts for creating JVM properties.
     */
    private Collection<ScriptCommand> createJvmPropertiesScripts(
            WebSphere85xInstalledLocalContainer container)
    {
        Collection<ScriptCommand> jvmCommands = new ArrayList<ScriptCommand>();

        String jvmArgs = getPropertyValue(GeneralPropertySet.JVMARGS);
        JvmArguments parsedArguments = JvmArguments.parseArguments(jvmArgs);

        jvmCommands.add(factory.setJvmPropertyScript("initialHeapSize",
                Long.toString(parsedArguments.getInitialHeap(ByteUnit.MEGABYTES))));
        jvmCommands.add(factory.setJvmPropertyScript("maximumHeapSize",
                Long.toString(parsedArguments.getMaxHeap(ByteUnit.MEGABYTES))));
        jvmCommands.add(factory.setJvmPropertyScript("genericJvmArguments",
                parsedArguments.getGenericArgs()));

        return jvmCommands;
    }

    /**
     * Create global security properties.
     * @return Scripts for creating global security properties.
     */
    private Collection<ScriptCommand> createGlobalSecurityPropertiesScripts()
    {
        Collection<ScriptCommand> globalSecPropertiesCommands = new ArrayList<ScriptCommand>();
        String globSecProps = getPropertyValue(WebSpherePropertySet.GLOBAL_SECURITY_PROPERTIES);

        if (globSecProps != null && !globSecProps.isEmpty())
        {
            Properties parsedProperty = PropertyUtils.splitPropertiesOnPipe(globSecProps);

            for (Entry<Object, Object> propertyItem : parsedProperty.entrySet())
            {
                String propertyName = propertyItem.getKey().toString();
                String propertyValue = propertyItem.getValue().toString();
                globalSecPropertiesCommands.add(factory.setGlobalSecurityPropertyScript(
                        propertyName, propertyValue));
            }
        }

        return globalSecPropertiesCommands;
    }

    /**
     * Create session management properties.
     * @return Scripts for creating session management properties.
     */
    private Collection<ScriptCommand> createSessionManagementPropertiesScripts()
    {
        Collection<ScriptCommand> sessionManPropertiesCommands = new ArrayList<ScriptCommand>();
        String sessManProps = getPropertyValue(WebSpherePropertySet.SESSION_MANAGEMENT_PROPERTIES);

        if (sessManProps != null && !sessManProps.isEmpty())
        {
            Properties parsedProperty = PropertyUtils.splitPropertiesOnPipe(sessManProps);

            for (Entry<Object, Object> propertyItem : parsedProperty.entrySet())
            {
                String propertyName = propertyItem.getKey().toString();
                String propertyValue = propertyItem.getValue().toString();
                sessionManPropertiesCommands.add(factory.setSessionManagementPropertyScript(
                        propertyName, propertyValue));
            }
        }

        return sessionManPropertiesCommands;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "WebSphere 8.5.x Standalone Configuration";
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
