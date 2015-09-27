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
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.weblogic.internal.AbstractWebLogicWlstStandaloneLocalConfiguration;
import org.codehaus.cargo.container.weblogic.internal.WebLogic9x10x103x12xWlstStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.weblogic.internal.WebLogicLocalContainer;
import org.codehaus.cargo.container.weblogic.internal.WebLogicLocalScriptingContainer;
import org.codehaus.cargo.container.weblogic.internal.configuration.WebLogic9x10x103x12xDataSourceConfigurationBuilder;

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
     * @see AbstractWebLogicWlstStandaloneLocalConfiguration#AbstractWebLogicWlstStandaloneLocalConfiguration(String)
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
        setProperty(WebLogicPropertySet.JMS_SERVER, "testJmsServer");
        setProperty(WebLogicPropertySet.JMS_MODULE, "testJmsModule");
        setProperty(WebLogicPropertySet.JMS_SUBDEPLOYMENT, "testJmsSubdeployment");
    }

    /**
     * {@inheritDoc}
     */
    public ConfigurationCapability getCapability()
    {
        return new WebLogic9x10x103x12xWlstStandaloneLocalConfigurationCapability();
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

        WebLogicLocalScriptingContainer weblogicContainer =
            (WebLogicLocalScriptingContainer) container;
        List<String> configurationScript = new ArrayList<String>();

        // create new domain
        configurationScript.addAll(prepareDomain(weblogicContainer));

        // add datasources to script
        for (DataSource dataSource : getDataSources())
        {
            configurationScript.add("cd('/')");
            configurationScript.add(getDataSourceScript(dataSource, weblogicContainer));
        }

        // add missing resources to list of resources
        addMissingResources();

        // sort resources
        sortResources();

        // add resources to script
        for (Resource resource : getResources())
        {
            configurationScript.add("cd('/')");
            configurationScript.add(getResourceScript(resource, weblogicContainer));
        }

        // add deployments to script
        WebLogic9x10x103x12xWlstOfflineInstalledLocalDeployer deployer =
            new WebLogic9x10x103x12xWlstOfflineInstalledLocalDeployer(weblogicContainer);
        for (Deployable deployable : getDeployables())
        {
            configurationScript.add("cd('/')");
            configurationScript.addAll(deployer.getDeployScript(deployable));
        }

        // write new domain to domain folder
        configurationScript.addAll(writeDomain());

        getLogger().info("Creating new Weblogic domain.", this.getClass().getName());

        // execute script
        weblogicContainer.executeScript(configurationScript);

        // deploy cargo ping
        deployCargoPing(weblogicContainer);
    }

    /**
     * Returns script for creating new domain from Weblogic template.
     *
     * @param weblogicContainer Weblogic container.
     * @return Script for creating new domain.
     */
    private List<String> prepareDomain(WebLogicLocalScriptingContainer weblogicContainer)
    {
        String weblogicHome = weblogicContainer.getWeblogicHome();

        // script for loading default Weblogic domain form template, configuring port and
        // administration user
        List<String> configurationScript = new ArrayList<String>();
        configurationScript.add("print \"Loading domain template.\"");
        configurationScript.add(String.format("readTemplate(r'%s/common/templates/wls/wls.jar')",
            weblogicHome));
        configurationScript.add("print \"Configuring domain and resources.\"");
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
        configurationScript.add("cd('/')");

        return configurationScript;
    }

    /**
     * Write domain and close domain template.
     *
     * @return Script writing new domain.
     */
    private List<String> writeDomain()
    {
        List<String> configurationScript = new ArrayList<String>();
        configurationScript.add("print \"Writing domain.\"");
        configurationScript.add(String.format("writeDomain(r'%s')", getDomainHome()));
        configurationScript.add("closeTemplate()");

        return configurationScript;
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
