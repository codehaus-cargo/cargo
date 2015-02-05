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
package org.codehaus.cargo.container.websphere;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractExistingLocalConfiguration;
import org.codehaus.cargo.container.websphere.internal.WebSphere85xExistingLocalConfigurationCapability;
import org.codehaus.cargo.util.CargoException;

/**
 * WebSphere existing {@link org.codehaus.cargo.container.configuration.Configuration}
 * implementation.
 *
 * @version $Id$
 */
public class WebSphere85xExistingLocalConfiguration extends AbstractExistingLocalConfiguration
{
    /**
     * Capability of the WebSphere existing configuration.
     */
    private static ConfigurationCapability capability =
        new WebSphere85xExistingLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see AbstractExistingLocalConfiguration#AbstractExistingLocalConfiguration(String)
     */
    public WebSphere85xExistingLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(ServletPropertySet.PORT, "9080");

        setProperty(WebSpherePropertySet.PROFILE, "cargoProfile");
        setProperty(WebSpherePropertySet.CELL, "cargoNodeCell");
        setProperty(WebSpherePropertySet.NODE, "cargoNode");
        setProperty(WebSpherePropertySet.SERVER, "cargoServer");

        setProperty(WebSpherePropertySet.CLASSLOADER_MODE, "PARENT_FIRST");
        setProperty(WebSpherePropertySet.WAR_CLASSLOADER_POLICY, "MULTIPLE");

        setProperty(WebSpherePropertySet.OVERWRITE_EXISTING_CONFIGURATION,
                WebSphereExistingConfigurationSetting.ALL.getName());
    }

    /**
     * {@inheritDoc}
     */
    public ConfigurationCapability getCapability()
    {
        return capability;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.configuration.AbstractLocalConfiguration#configure(LocalContainer)
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

        List<String> wsAdminCommands = new ArrayList<String>();

        // First we need to find *our* server
        wsAdminCommands.add("set server [$AdminConfig getid "
                        + "/Cell:" + getPropertyValue(WebSpherePropertySet.CELL)
                        + "/Node:" + getPropertyValue(WebSpherePropertySet.NODE)
                        + "/Server:" + getPropertyValue(WebSpherePropertySet.SERVER)
                        + "/]"
        );

        // ... and the JVM settings of that server
        wsAdminCommands.add("set jvm [$AdminConfig list JavaVirtualMachine $server]");


        if (configurationSetting == WebSphereExistingConfigurationSetting.ALL
                || configurationSetting == WebSphereExistingConfigurationSetting.JVM)
        {
            // we need to extract minimum and maximum memory settings from given string.
            int initialHeap = -1;
            int maxHeap = -1;
            StringBuilder genericArgs = new StringBuilder();

            String jvmArgs = getPropertyValue(GeneralPropertySet.JVMARGS);
            if (jvmArgs != null)
            {
                for (String arg : jvmArgs.split(" "))
                {
                    if (arg.startsWith("-Xms"))
                    {
                        initialHeap = wsContainer.convertJVMArgToMegaByte(arg.substring(4));
                    }
                    else if (arg.startsWith("-Xmx"))
                    {
                        maxHeap = wsContainer.convertJVMArgToMegaByte(arg.substring(4));
                    }
                    else
                    {
                        if (genericArgs.length() > 0)
                        {
                            genericArgs.append(' ');
                        }
                        genericArgs.append(arg);
                    }
                }
            }

            // setting default memory settings
            if (maxHeap < 1)
            {
                maxHeap = 512;
            }

            if (initialHeap < 1)
            {
                initialHeap = maxHeap;
            }

            // Now we can set our memory settings and other JVM arguments
            wsAdminCommands.add("$AdminConfig modify $jvm { "
                            + "{initialHeapSize " + initialHeap + "} "
                            + "{maximumHeapSize " + maxHeap + "} "
                            + "{genericJvmArguments \"" + genericArgs + "\"} "
                            + "}"
            );
        }

        if (configurationSetting == WebSphereExistingConfigurationSetting.ALL
                || configurationSetting == WebSphereExistingConfigurationSetting.SystemProperties)
        {
            wsAdminCommands.addAll(Arrays.asList(
                    // Deleting all existing system properties first
                    "set jvmProperties [$AdminConfig list Property $jvm]",
                    "if { ${jvmProperties} != \"\" } {",
                    "  foreach propertyID ${jvmProperties} {",
                    "    $AdminConfig remove $propertyID",
                    "  }",
                    "}"
            ));

            for (Map.Entry<String, String> systemProperty : wsContainer.getSystemProperties()
                    .entrySet())
            {
                wsAdminCommands.add("$AdminConfig create Property $jvm { "
                        + "{name \"" + systemProperty.getKey() + "\"} "
                        + "{value \"" + systemProperty.getValue() + "\"} "
                        + "}"
                );
            }
        }

        if (configurationSetting != WebSphereExistingConfigurationSetting.NONE)
        {
            wsAdminCommands.add("$AdminConfig save");
            wsContainer.executeWsAdmin(wsAdminCommands.toArray(new String[0]));
        }

        File cargoCpc = File.createTempFile("cargo-cpc-", ".war");
        getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war", cargoCpc);
        WAR cargoCpcWar = new WAR(cargoCpc.getAbsolutePath());
        cargoCpcWar.setContext("cargocpc");
        getDeployables().add(cargoCpcWar);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.configuration.ContainerConfiguration#verify()
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
        return "WebSphere 8.5 Existing Configuration";
    }

}
