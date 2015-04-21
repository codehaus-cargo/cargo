/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol, 2011-2015 Ali Tokmen.
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
package org.codehaus.cargo.container.weblogic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.configuration.builder.ConfigurationBuilder;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.weblogic.internal.WebLogic9x10x103x12xDataSourceConfigurationBuilder;
import org.codehaus.cargo.container.weblogic.internal.WebLogic9x10x103x12xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.weblogic.internal.WebLogicLocalContainer;

/**
 * WebLogic 12.1.x standalone
 * {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration} implementation.
 * WebLogic 12.1.x uses WLST for container configuration.
 */
public class WebLogic121xWlstStandaloneLocalConfiguration extends
    AbstractWebLogicWlstStandaloneLocalConfiguration
{

    /**
     * {@inheritDoc}
     *
     * @see AbstractLocalConfiguration#AbstractLocalConfiguration(String)
     */
    public WebLogic121xWlstStandaloneLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(WebLogicPropertySet.ADMIN_USER, "weblogic");
        setProperty(WebLogicPropertySet.ADMIN_PWD, "weblogic1");
        setProperty(WebLogicPropertySet.SERVER, "server");
        setProperty(WebLogicPropertySet.CONFIGURATION_VERSION, "12.1.3.0");
        setProperty(WebLogicPropertySet.DOMAIN_VERSION, "12.1.3.0");
        setProperty(ServletPropertySet.PORT, "7001");
        setProperty(GeneralPropertySet.HOSTNAME, "localhost");
    }

    /**
     * {@inheritDoc}
     */
    public ConfigurationCapability getCapability()
    {
        return new WebLogic9x10x103x12xStandaloneLocalConfigurationCapability();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ConfigurationBuilder createConfigurationBuilder(LocalContainer container)
    {
        return new WebLogic9x10x103x12xDataSourceConfigurationBuilder(container.getConfiguration());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doConfigure(LocalContainer container) throws Exception
    {
        setupConfigurationDir();

        WebLogic121xWlstInstalledLocalContainer weblogicContainer =
            (WebLogic121xWlstInstalledLocalContainer) container;

        // create domain
        createNewDomain(weblogicContainer);

        // deploy war
        WebLogic9x10x103x12xWlstOfflineInstalledLocalDeployer deployer =
            new WebLogic9x10x103x12xWlstOfflineInstalledLocalDeployer(weblogicContainer);
        deployer.deploy(getDeployables());

        // deploy cargo ping
        deployCargoPing(weblogicContainer);
    }

    /**
     * Creates new domain from Weblogic template.
     *
     * @param weblogicContainer Weblogic container.
     */
    private void createNewDomain(WebLogic121xWlstInstalledLocalContainer weblogicContainer)
    {
        String weblogicHome = weblogicContainer.getWeblogicHome();

        // script for loading default Weblogic domain form template, configuring port and
        // administration user and storing domain
        List<String> configurationScript = new ArrayList<String>();
        configurationScript.add(String.format("readTemplate('%s/common/templates/wls/wls.jar')",
            weblogicHome));
        configurationScript.add("cd('/')");
        configurationScript.add("cd('Servers/AdminServer')");
        configurationScript.add(String.format("cmo.setName('%s')",
            getPropertyValue(WebLogicPropertySet.SERVER)));
        configurationScript.add(String.format("set('ListenPort', %s)",
            getPropertyValue(ServletPropertySet.PORT)));
        configurationScript.add("cd('/')");
        configurationScript.add("cd('Security/base_domain/User/weblogic')");
        configurationScript.add(String.format("cmo.setName('%s')",
            getPropertyValue(WebLogicPropertySet.ADMIN_USER)));
        configurationScript.add(String.format("cmo.setPassword('%s')",
            getPropertyValue(WebLogicPropertySet.ADMIN_PWD)));
        configurationScript.add("cd('/')");
        configurationScript.add("setOption('OverwriteDomain', 'true')");
        configurationScript.add(String.format("writeDomain('%s')", getDomainHome()));
        configurationScript.add("closeTemplate()");

        getLogger().info("Creating new Weblogic domain.",
            this.getClass().getName());
        weblogicContainer.writeWithWlst(configurationScript);
    }

    /**
     * Deploy the Cargo Ping utility to the container.
     *
     * @param container the container to configure
     * @throws IOException if the cargo ping deployment fails
     */
    private void deployCargoPing(WebLogicLocalContainer container) throws IOException
    {
        // as this is an initial install, this directory will not exist, yet
        String deployDir =
            getFileHandler().createDirectory(getDomainHome(), container.getAutoDeployDirectory());

        // Deploy the cargocpc web-app by copying the WAR file
        getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war",
            getFileHandler().append(deployDir, "cargocpc.war"), getFileHandler());
    }

    /**
     * {@inheritDoc}
     *
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        return "WebLogic 12.1.x Standalone Configuration";
    }
}
