/* 
 * ========================================================================
 * 
 * Copyright 2004-2005 Vincent Massol.
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

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.apache.tools.ant.types.FilterChain;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.EAR;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration;
import org.codehaus.cargo.container.weblogic.internal.WebLogicStandaloneLocalConfigurationCapability;

/**
 * WebLogic standalone {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration}
 * implementation.
 * 
 * @version $Id$
 */
public class WebLogicStandaloneLocalConfiguration extends AbstractStandaloneLocalConfiguration
    implements WebLogicConfiguration
{
    /**
     * Capability of the WebLogic standalone configuration.
     */
    private static ConfigurationCapability capability =
        new WebLogicStandaloneLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * 
     * @see AbstractStandaloneLocalConfiguration#AbstractStandaloneLocalConfiguration(String)
     */
    public WebLogicStandaloneLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(WebLogicPropertySet.ADMIN_USER, "weblogic");
        setProperty(WebLogicPropertySet.ADMIN_PWD, "weblogic");
        setProperty(WebLogicPropertySet.SERVER, "server");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.configuration.Configuration#getCapability()
     */
    public ConfigurationCapability getCapability()
    {
        return capability;
    }

    /**
     * {@inheritDoc}
     * 
     * @see AbstractStandaloneLocalConfiguration#configure(LocalContainer)
     */
    protected void doConfigure(LocalContainer container) throws Exception
    {
        setupConfigurationDir();

        FilterChain filterChain = createWebLogicFilterChain();
        setupDeployables(filterChain);

        // make sure you use this method, as it ensures the same filehandler
        // that created the directory will be used to copy the resource.
        // This is especially important for unit testing
        getResourceUtils()
            .copyResource(RESOURCE_PATH + container.getId() + "/config.xml",
                getFileHandler().append(getDomainHome(), "config.xml"), getFileHandler(),
                filterChain);

        getResourceUtils().copyResource(
            RESOURCE_PATH + container.getId() + "/DefaultAuthenticatorInit.ldift",
            getFileHandler().append(getDomainHome(), "DefaultAuthenticatorInit.ldift"),
            getFileHandler(), filterChain);

        deployCargoPing((WebLogicLocalContainer) container);
    }

    /**
     * @return an Ant filter chain containing implementation for the filter tokens used in the
     *         WebLogic configuration files
     */
    private FilterChain createWebLogicFilterChain()
    {
        FilterChain filterChain = getFilterChain();

        return filterChain;
    }

    /**
     * Add applications into the WebLogic configuration.
     * 
     * @param filterChain where to insert the application configuration
     */
    protected void setupDeployables(FilterChain filterChain)
    {
        StringBuffer appTokenValue = new StringBuffer(" ");

        Iterator it = getDeployables().iterator();
        while (it.hasNext())
        {
            Deployable deployable = (Deployable) it.next();

            if (deployable.getType() == DeployableType.WAR)
            {
                WAR war = (WAR) deployable;
                appTokenValue.append(getConfigElement(war));
            }
            else if (deployable.getType() == DeployableType.EAR)
            {
                EAR ear = (EAR) deployable;
                appTokenValue.append(getConfigElement(ear));
            }
        }

        getAntUtils().addTokenToFilterChain(filterChain, "weblogic.apps",
            appTokenValue.toString());

    }

    /**
     * insert WebLogic 8 configuration tag corresponding to the current war file.
     * 
     * @param war - file we want to configure
     * @return xml element corresponding to the war file
     */
    private String getConfigElement(WAR war)
    {
        String context = war.getContext();
        StringBuffer element = new StringBuffer();
        element.append("<Application ");
        element.append("Name=\"_" + context + "_app\" ");
        element.append("Path=\"" + getFileHandler().getParent(getAbsolutePath(war)) + "\" ");
        element.append("StagedTargets=\"" + getPropertyValue(WebLogicPropertySet.SERVER)
            + "\" StagingMode=\"stage\" TwoPhase=\"true\"");
        element.append(">");

        element.append("<WebAppComponent ");
        element.append("Name=\"" + context + "\" ");
        element.append("Targets=\"" + getPropertyValue(WebLogicPropertySet.SERVER) + "\" ");
        element.append("URI=\"" + getURI(war) + "\"");
        element.append("/></Application>");
        return element.toString();
    }

    /**
     * insert WebLogic 8 configuration tag corresponding to the current ear file.
     * 
     * @param ear - file we want to configure
     * @return xml element corresponding to the ear file
     */
    private String getConfigElement(EAR ear)
    {
        StringBuffer element = new StringBuffer();
        element.append("<Application ");
        element.append("Name=\"_" + ear.getName() + "_app\" ");
        element.append("Deployed=\"true\" ");
        element.append("Path=\"" + getAbsolutePath(ear) + "\" ");
        element.append("StagedTargets=\"" + getPropertyValue(WebLogicPropertySet.SERVER)
            + "\" StagingMode=\"stage\" TwoPhase=\"true\"");
        element.append(">");
        Iterator contexts = ear.getWebContexts();
        while (contexts.hasNext())
        {
            String context = (String) contexts.next();
            element.append("<WebAppComponent ");
            element.append("Name=\"" + context + "\" ");
            element.append("Targets=\"" + getPropertyValue(WebLogicPropertySet.SERVER) + "\" ");
            element.append("URI=\"" + ear.getWebUri(context) + "\"");
            element.append("/>");
        }
        element.append("</Application>");
        return element.toString();
    }

    /**
     * Deploy the Cargo Ping utility to the container.
     * 
     * @param container the container to configure
     * @throws IOException if the cargo ping deployment fails
     */
    protected void deployCargoPing(WebLogicLocalContainer container) throws IOException
    {
        // as this is an initial install, this directory will not exist, yet
        String deployDir =
            getFileHandler().createDirectory(getDomainHome(), container.getAutoDeployDirectory());

        // Deploy the cargocpc web-app by copying the WAR file
        getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war",
            getFileHandler().append(deployDir, "cargocpc.war"), getFileHandler());
    }

    /**
     * gets the URI from a file. This is the basic filename. ex. web.war
     * 
     * @param deployable - what to extract the uri from
     * @return - uri of the deployable
     */
    String getURI(Deployable deployable)
    {
        String path = deployable.getFile();
        return new File(path).getName();
    }

    /**
     * gets the absolute path from a file that may be relative to the current directory.
     * 
     * @param deployable - what to extract the file path from
     * @return - absolute path to the deployable
     */
    String getAbsolutePath(Deployable deployable)
    {
        String path = deployable.getFile();
        return getFileHandler().getAbsolutePath(path);
    }

    /**
     * {@inheritDoc}
     * 
     * @see Object#toString()
     */
    public String toString()
    {
        return "WebLogic Standalone Configuration";
    }

    /**
     * {@inheritDoc}
     */
    public String getDomainHome()
    {
        return getHome();
    }
}
