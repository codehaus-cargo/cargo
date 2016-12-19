/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol, 2012-2016 Ali Tokmen.
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

import java.util.ArrayList;
import java.util.List;

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.configuration.script.ScriptCommand;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.internal.util.ComplexPropertyUtils;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.weblogic.internal.AbstractWebLogicWlstStandaloneLocalConfiguration;
import org.codehaus.cargo.container.weblogic.internal.WebLogicLocalScriptingContainer;
import org.codehaus.cargo.container.weblogic.internal.WebLogicWlstStandaloneLocalConfigurationCapability;

/**
 * WebLogic 12.1.x standalone
 * {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration} implementation.
 * WebLogic 12.1.x uses WLST for container configuration.
 */
public class WebLogic121xStandaloneLocalConfiguration extends
    AbstractWebLogicWlstStandaloneLocalConfiguration
{

    /**
     * {@inheritDoc}
     * @see AbstractWebLogicWlstStandaloneLocalConfiguration#AbstractWebLogicWlstStandaloneLocalConfiguration(String)
     */
    public WebLogic121xStandaloneLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(WebLogicPropertySet.ADMIN_USER, "weblogic");
        setProperty(WebLogicPropertySet.ADMIN_PWD, "weblogic1");
        setProperty(WebLogicPropertySet.SERVER, "server");
        setProperty(ServletPropertySet.PORT, "7001");
        setProperty(GeneralPropertySet.HOSTNAME, "localhost");
        setProperty(WebLogicPropertySet.JMS_SERVER, "testJmsServer");
        setProperty(WebLogicPropertySet.JMS_MODULE, "testJmsModule");
        setProperty(WebLogicPropertySet.JMS_SUBDEPLOYMENT, "testJmsSubdeployment");
        setProperty(WebLogicPropertySet.LOG_ROTATION_TYPE, "none");
        setProperty(WebLogicPropertySet.SSL_HOSTNAME_VERIFICATION_IGNORED, "true");
        setProperty(WebLogicPropertySet.SSL_HOSTNAME_VERIFIER_CLASS, "None");
        setProperty(WebLogicPropertySet.PASSWORD_LENGTH_MIN, "8");
        setProperty(WebLogicPropertySet.PASSWORD_SPNUM_MIN, "1");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConfigurationCapability getCapability()
    {
        return new WebLogicWlstStandaloneLocalConfigurationCapability();
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
        List<ScriptCommand> configurationScript = new ArrayList<ScriptCommand>();

        // create new domain
        configurationScript.add(getConfigurationFactory().createDomainScript(
                weblogicContainer.getWeblogicHome()));

        // configure logging
        configurationScript.add(getConfigurationFactory().loggingScript());

        // configure SSL
        configurationScript.add(getConfigurationFactory().sslScript());

        // configure JTA
        if (getConfigurationFactory().jtaScript().isApplicable())
        {
            configurationScript.add(getConfigurationFactory().jtaScript());
        }

        // configure password validator
        configurationScript.add(getConfigurationFactory().passwordValidatorScript());

        // add datasources to script
        for (DataSource dataSource : getDataSources())
        {
            configurationScript.addAll(getConfigurationFactory().dataSourceScript(dataSource));
        }

        // add missing resources to list of resources
        addMissingResources();

        // sort resources
        sortResources();

        // add resources to script
        for (Resource resource : getResources())
        {
            configurationScript.add(getConfigurationFactory().resourceScript(resource));
        }

        // add deployments to script
        for (Deployable deployable : getDeployables())
        {
            configurationScript.add(getConfigurationFactory().deployDeployableScript(deployable));
        }

        // write new domain to domain folder
        configurationScript.add(getConfigurationFactory().writeDomainScript());

        getLogger().info("Creating new WebLogic domain.", this.getClass().getName());

        // execute script
        weblogicContainer.executeScript(configurationScript);

        // Execute offline jython scripts
        String scriptPaths = getPropertyValue(WebLogicPropertySet.JYTHON_SCRIPT_OFFLINE);
        List<String> scriptPathList = ComplexPropertyUtils.parseProperty(scriptPaths, "|");
        weblogicContainer.executeScriptFiles(scriptPathList);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "WebLogic 12.1.x Standalone Configuration";
    }
}
