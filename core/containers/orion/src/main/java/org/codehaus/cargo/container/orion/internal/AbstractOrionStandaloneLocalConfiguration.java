/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol, 2012-2021 Ali Tokmen.
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
package org.codehaus.cargo.container.orion.internal;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.filters.ReplaceTokens;
import org.apache.tools.ant.types.FilterChain;
import org.apache.tools.ant.util.FileUtils;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.configuration.builder.ConfigurationBuilder;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.EAR;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.User;
import org.codehaus.cargo.container.spi.configuration.builder.AbstractStandaloneLocalConfigurationWithXMLConfigurationBuilder;

/**
 * Standalone configuration methods common to both Orion and Oc4j containers.
 */
public abstract class AbstractOrionStandaloneLocalConfiguration extends
    AbstractStandaloneLocalConfigurationWithXMLConfigurationBuilder
{
    /**
     * Where elements for resources will be inserted. This expression evaluates to: {@value
     * XML_PARENT_OF_RESOURCES}
     */
    public static final String XML_PARENT_OF_RESOURCES = "//data-sources";

    /**
     * Where to find resources for this configuration.
     */
    private static final String ORION_RESOURCE_PATH = RESOURCE_PATH + "oc4j9x10x";

    /**
     * Capability of the Orion standalone configuration.
     */
    private static ConfigurationCapability capability =
        new OrionStandaloneLocalConfigurationCapability();

    /**
     * construct the instance and set the rmi port.
     * 
     * @param dir - home of this configuration
     */
    public AbstractOrionStandaloneLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(GeneralPropertySet.RMI_PORT, "25791");
    }

    /**
     * {@inheritDoc} This implementation throws an UnsupportedOperationException as Resource
     * configuration is not supported in Orion.
     */
    @Override
    protected String getOrCreateResourceConfigurationFile(Resource resource,
        LocalContainer container)
    {
        throw new UnsupportedOperationException(
            OrionConfigurationBuilder.RESOURCE_CONFIGURATION_UNSUPPORTED);
    }

    /**
     * {@inheritDoc} This implementation throws an UnsupportedOperationException as Resource
     * configuration is not supported in Orion.
     */
    @Override
    protected String getXpathForResourcesParent()
    {
        throw new UnsupportedOperationException(
            OrionConfigurationBuilder.RESOURCE_CONFIGURATION_UNSUPPORTED);
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
    protected ConfigurationBuilder createConfigurationBuilder(LocalContainer container)
    {
        return new OrionConfigurationBuilder();
    }

    /**
     * {@inheritDoc} In this implementation, we will return the <code>data-sources.xml</code> file.
     */
    @Override
    public String getOrCreateDataSourceConfigurationFile(DataSource ds, LocalContainer container)
    {
        String confDir = getFileHandler().createDirectory(getHome(), "conf");
        return getFileHandler().append(confDir, "data-sources.xml");
    }

    /**
     * {@inheritDoc} Orion application servers currently use DTD, and therefore return and empty
     * map;
     */
    @Override
    protected Map<String, String> getNamespaces()
    {
        return Collections.emptyMap();
    }

    /**
     * This expression evaluates to: {@value XML_PARENT_OF_RESOURCES} {@inheritDoc}
     */
    @Override
    protected String getXpathForDataSourcesParent()
    {
        return XML_PARENT_OF_RESOURCES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doConfigure(LocalContainer container) throws Exception
    {
        setupConfigurationDir();

        FileUtils fileUtils = FileUtils.getFileUtils();
        FilterChain filterChain = createOrionFilterChain();

        String confDir = getFileHandler().createDirectory(getHome(), "conf");

        getResourceUtils().copyResource(ORION_RESOURCE_PATH + "/server.xml",
            getFileHandler().append(confDir, "server.xml"), getFileHandler(), filterChain,
                StandardCharsets.UTF_8);
        getResourceUtils().copyResource(ORION_RESOURCE_PATH + "/application.xml",
            getFileHandler().append(confDir, "application.xml"), getFileHandler(), filterChain,
                StandardCharsets.UTF_8);
        getResourceUtils().copyResource(ORION_RESOURCE_PATH + "/default-web-site.xml",
            getFileHandler().append(confDir, "default-web-site.xml"), getFileHandler(),
            filterChain, StandardCharsets.UTF_8);

        getResourceUtils().copyResource(ORION_RESOURCE_PATH + "/mime.types",
            getFileHandler().append(confDir, "mime.types"), getFileHandler(), filterChain,
                StandardCharsets.UTF_8);
        getResourceUtils().copyResource(ORION_RESOURCE_PATH + "/principals.xml",
            getFileHandler().append(confDir, "principals.xml"), getFileHandler(), filterChain,
                StandardCharsets.UTF_8);
        getResourceUtils().copyResource(ORION_RESOURCE_PATH + "/rmi.xml",
            getFileHandler().append(confDir, "rmi.xml"), getFileHandler(), filterChain,
                StandardCharsets.UTF_8);

        // create a default data-sources.xml file/
        getResourceUtils().copyResource(ORION_RESOURCE_PATH + "/data-sources.xml",
            getFileHandler().append(confDir, "data-sources.xml"), getFileHandler(), filterChain,
                StandardCharsets.UTF_8);

        copyCustomResources(confDir, filterChain);

        // Create default web app (required by Orion unfortunately...)
        String defaultWebAppDir =
            getFileHandler().createDirectory(getHome(), "default-web-app/WEB-INF");
        getResourceUtils().copyResource(ORION_RESOURCE_PATH + "/web.xml",
            getFileHandler().append(defaultWebAppDir, "web.xml"), getFileHandler(), filterChain,
                StandardCharsets.UTF_8);

        // Orion need to have a /persistence directory created, otherwise it
        // throws an error
        getFileHandler().createDirectory(getHome(), "persistence");

        // Directory where modules to be deployed are located
        String appDir = getFileHandler().createDirectory(getHome(), "applications");

        // Deployment directory (i.e. where Orion expands modules)
        getFileHandler().createDirectory(getHome(), "application-deployments");

        // Orion log directory
        getFileHandler().createDirectory(getHome(), "log");

        // Deploy all deployables into the applications directory
        for (Deployable deployable : getDeployables())
        {
            if (deployable.getType() != DeployableType.WAR
                || deployable.getType() == DeployableType.WAR && !deployable.isExpanded())
            {
                fileUtils.copyFile(new File(deployable.getFile()).getAbsoluteFile(),
                    new File(appDir, new File(deployable.getFile()).getName()), null, true);
            }
        }
        // Deploy the CPC (Cargo Ping Component) to the webapps directory
        getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war",
            getFileHandler().append(appDir, "cargocpc.war"), getFileHandler());
    }

    /**
     * Copy resources that are different between the different standalone implementations.
     * 
     * @param confDir the configuration dir where to copy the resources to
     * @param filterChain the Ant filter chain to apply when copying the resources
     * @throws Exception in case of an error during the copy
     */
    protected abstract void copyCustomResources(String confDir, FilterChain filterChain)
        throws Exception;

    /**
     * @return an Ant filter chain containing implementation for the filter tokens used in the Orion
     * configuration files
     */
    private FilterChain createOrionFilterChain()
    {
        FilterChain filterChain = getFilterChain();

        // Add Orion RMI port token
        getAntUtils().addTokenToFilterChain(filterChain, GeneralPropertySet.RMI_PORT,
            getPropertyValue(GeneralPropertySet.RMI_PORT));

        // Add token filters for adding users and roles
        if (!getUsers().isEmpty())
        {
            getAntUtils().addTokenToFilterChain(filterChain, "orion.users", getUserToken());
            getAntUtils().addTokenToFilterChain(filterChain, "orion.roles", getRoleToken());
        }

        // Add application deployment tokens

        ReplaceTokens.Token tokenApplications = new ReplaceTokens.Token();
        tokenApplications.setKey("orion.application");

        ReplaceTokens.Token tokenWebModules = new ReplaceTokens.Token();
        tokenWebModules.setKey("orion.web-module");

        ReplaceTokens.Token tokenWebApps = new ReplaceTokens.Token();
        tokenWebApps.setKey("orion.web-app");

        // Note: The following values must never be empty string as otherwise
        // the Ant filtering code fails.
        StringBuilder keyApplications = new StringBuilder(" ");
        StringBuilder keyWebModules = new StringBuilder(" ");
        StringBuilder keyWebApps = new StringBuilder(" ");

        for (Deployable deployable : getDeployables())
        {
            if (deployable.getType() == DeployableType.EAR)
            {
                keyApplications.append("  <application name=\"");
                keyApplications.append(((EAR) deployable).getName());
                keyApplications.append("\" path=\"../applications/");
                keyApplications.append(new File(deployable.getFile()).getName());
                keyApplications.append("\"/>");

                List<String> webContexts = ((EAR) deployable).getWebContexts();
                for (String webContext : webContexts)
                {
                    keyWebApps.append("<web-app application=\"");
                    keyWebApps.append(((EAR) deployable).getName());
                    keyWebApps.append("\" name=\"");

                    // The name must be the name of the war file without the
                    // extension.
                    String name = ((EAR) deployable).getWebUri(webContext);
                    int warIndex = name.toLowerCase().lastIndexOf(".war");
                    if (warIndex >= 0)
                    {
                        name = name.substring(0, warIndex);
                    }

                    keyWebApps.append(name);
                    keyWebApps.append("\" root=\"/");
                    keyWebApps.append(webContext);
                    keyWebApps.append("\"/>");
                }
            }
            else if (deployable.getType() == DeployableType.WAR)
            {
                keyWebModules.append("  <web-module id=\"");
                keyWebModules.append(((WAR) deployable).getContext());

                if (deployable.isExpanded())
                {
                    keyWebModules.append("\" path=\"");
                    keyWebModules.append(deployable.getFile());
                }
                else
                {
                    keyWebModules.append("\" path=\"../applications/");
                    keyWebModules.append(getFileHandler().getName(deployable.getFile()));
                }
                keyWebModules.append("\"/>");

                keyWebApps.append("<web-app application=\"default\" name=\"");
                keyWebApps.append(((WAR) deployable).getContext());
                keyWebApps.append("\" root=\"/");
                keyWebApps.append(((WAR) deployable).getContext());
                keyWebApps.append("\"/>");
            }
        }

        tokenApplications.setValue(keyApplications.toString());
        tokenWebModules.setValue(keyWebModules.toString());
        tokenWebApps.setValue(keyWebApps.toString());

        ReplaceTokens replaceApplications = new ReplaceTokens();
        replaceApplications.addConfiguredToken(tokenApplications);
        filterChain.addReplaceTokens(replaceApplications);

        ReplaceTokens replaceWebModules = new ReplaceTokens();
        replaceWebModules.addConfiguredToken(tokenWebModules);
        filterChain.addReplaceTokens(replaceWebModules);

        ReplaceTokens replaceWebApps = new ReplaceTokens();
        replaceWebApps.addConfiguredToken(tokenWebApps);
        filterChain.addReplaceTokens(replaceWebApps);

        return filterChain;
    }

    /**
     * @return an Ant filter token containing all the user-defined users
     */
    protected String getUserToken()
    {
        StringBuilder token = new StringBuilder(" ");

        // Add token filters for authenticated users
        if (!getUsers().isEmpty())
        {
            for (User user : getUsers())
            {
                token.append("<user deactivated=\"false\" ");
                token.append("username=\"" + user.getName() + "\" ");
                token.append("password=\"" + user.getPassword() + "\"");
                token.append("/>");
            }
        }

        return token.toString();
    }

    /**
     * @return an Ant filter token containing all the role-defined roles
     */
    protected String getRoleToken()
    {
        StringBuilder token = new StringBuilder(" ");

        List<User> users = getUsers();
        Map<String, List<User>> roles = User.createRoleMap(users);

        for (Map.Entry<String, List<User>> role : roles.entrySet())
        {
            token.append("<security-role-mapping ");
            token.append("name=\"" + role.getKey() + "\">");

            for (User user : role.getValue())
            {
                token.append("<user name=\"" + user.getName() + "\"/>");
            }

            token.append("</security-role-mapping>");
        }

        return token.toString();
    }

}
