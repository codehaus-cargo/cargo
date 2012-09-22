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

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.websphere.internal.WebSphere85xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;
import org.codehaus.cargo.util.CargoException;

/**
 * IBM WebSphere 8.5 standalone
 * {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration} implementation.
 * 
 * @version $Id$
 */
public class WebSphere85xStandaloneLocalConfiguration extends AbstractStandaloneLocalConfiguration
{
    /**
     * Capability of the WebShere standalone configuration.
     */
    private static ConfigurationCapability capability =
        new WebSphere85xStandaloneLocalConfigurationCapability();

    /**
     * WebSphere container.
     */
    private WebSphere85xInstalledLocalContainer wsContainer;

    /**
     * Profile home.
     */
    private String home;

    /**
     * {@inheritDoc}
     * @see AbstractStandaloneLocalConfiguration#AbstractStandaloneLocalConfiguration(String)
     */
    public WebSphere85xStandaloneLocalConfiguration(String dir)
    {
        super(dir);
        this.home = dir;

        setProperty(ServletPropertySet.PORT, "9080");

        setProperty(WebSpherePropertySet.ADMIN_USERNAME, "websphere");
        setProperty(WebSpherePropertySet.ADMIN_PASSWORD, "websphere");

        setProperty(WebSpherePropertySet.PROFILE, "cargoProfile");
        setProperty(WebSpherePropertySet.CELL, "cargoNodeCell");
        setProperty(WebSpherePropertySet.NODE, "cargoNode");
        setProperty(WebSpherePropertySet.SERVER, "cargoServer");
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
     */
    @Override
    protected void doConfigure(LocalContainer container) throws Exception
    {
        this.wsContainer = (WebSphere85xInstalledLocalContainer) container;
        File home = new File(this.wsContainer.getHome(),
            "profiles/" + getPropertyValue(WebSpherePropertySet.PROFILE));
        this.home = home.getAbsolutePath();

        try
        {
            runManageProfileCommand(
                "-delete",
                "-profileName",
                getPropertyValue(WebSpherePropertySet.PROFILE));
        }
        catch (Exception e)
        {
            getLogger().debug("Error occured while deleting WebSphere profile",
                this.getClass().getName());
        }
        finally
        {
            getLogger().debug("Deleting profile folder " + this.home,
                this.getClass().getName());
            getFileHandler().delete(this.home);

            if (getFileHandler().isDirectory(this.home))
            {
                throw new CargoException("Directory " + this.home + " cannot be deleted");
            }
        }

        File portsFile = File.createTempFile("cargo-websphere-portdef-", ".properties");
        getResourceUtils().copyResource(RESOURCE_PATH + container.getId() + "/portdef.props",
            portsFile, createFilterChain(), "ISO-8859-1");

        try
        {
            runManageProfileCommand(
                "-create",
                "-profileName",
                getPropertyValue(WebSpherePropertySet.PROFILE),
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
                "-adminUserName",
                getPropertyValue(WebSpherePropertySet.ADMIN_USERNAME),
                "-adminPassword",
                getPropertyValue(WebSpherePropertySet.ADMIN_PASSWORD));
        }
        finally
        {
            portsFile.delete();
        }

        File cargoCpc = File.createTempFile("cargo-cpc-", ".war");
        getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war", cargoCpc);
        WAR cargoCpcWar = new WAR(cargoCpc.getAbsolutePath());
        cargoCpcWar.setContext("cargocpc");
        getDeployables().add(cargoCpcWar);
    }

    /**
     * {@inheritDoc}. This implementation overrides as WebSphere does not allow flexible paths.
     */
    @Override
    public String getHome()
    {
        return this.home;
    }

    /**
     * Run a manageprofile command.
     * @param arguments Arguments.
     */
    protected void runManageProfileCommand(String... arguments)
    {
        JvmLauncher java = this.wsContainer.createJvmLauncher();

        java.setSystemProperty("ws.ext.dirs",
            // new File(javaLib, "ext").getAbsolutePath().replace(File.separatorChar, '/')
            // + File.pathSeparatorChar
            new File(getHome(), "classes").getAbsolutePath().replace(File.separatorChar, '/')
            + File.pathSeparatorChar
            + new File(getHome(), "lib").getAbsolutePath().replace(File.separatorChar, '/')
            + File.pathSeparatorChar
            + new File(getHome(), "installedChannels").getAbsolutePath()
                .replace(File.separatorChar, '/')
            + File.pathSeparatorChar
            + new File(getHome(), "lib/ext").getAbsolutePath().replace(File.separatorChar, '/')
            + File.pathSeparatorChar
            + new File(getHome(), "web/help").getAbsolutePath().replace(File.separatorChar, '/')
            + File.pathSeparatorChar
            + new File(getHome(), "deploytool/itp/plugins/com.ibm.etools.ejbdeploy/runtime")
                .getAbsolutePath().replace(File.separatorChar, '/'));

        java.setMainClass("com.ibm.wsspi.bootstrap.WSPreLauncher");

        java.addAppArguments("-nosplash");
        java.addAppArguments("-application");
        java.addAppArguments("com.ibm.ws.bootstrap.WSLauncher");
        java.addAppArguments("com.ibm.ws.runtime.WsProfile");

        java.addAppArguments(arguments);

        java.setWorkingDirectory(new File(wsContainer.getHome()));

        int returnCode = java.execute();
        if (returnCode != 0)
        {
            throw new CargoException(
                "WebSphere configuration cannot be created: return code was " + returnCode);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "WebSphere 8.5 Standalone Configuration";
    }
}
