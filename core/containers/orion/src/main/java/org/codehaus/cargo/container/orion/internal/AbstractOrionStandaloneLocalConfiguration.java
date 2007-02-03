/*
 * ========================================================================
 *
 * Copyright 2004-2006 Vincent Massol.
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

import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.property.User;
import org.codehaus.cargo.container.property.DatasourcePropertySet;
import org.codehaus.cargo.container.property.DataSource;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.deployable.EAR;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.types.FilterChain;
import org.apache.tools.ant.filters.ReplaceTokens;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Standalone configuration methods common to both Orion and Oc4j9x.
 *
 * @version $Id$
 */
public abstract class AbstractOrionStandaloneLocalConfiguration
    extends AbstractStandaloneLocalConfiguration
{
    /**
     * Capability of the Orion standalone configuration.
     */
    private static ConfigurationCapability capability =
        new OrionStandaloneLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see AbstractStandaloneLocalConfiguration#AbstractStandaloneLocalConfiguration(String)
     */
    public AbstractOrionStandaloneLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(GeneralPropertySet.RMI_PORT, "25791");
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.configuration.Configuration#getCapability()
     */
    public ConfigurationCapability getCapability()
    {
        return capability;
    }

    /**
     * {@inheritDoc}
     * @see AbstractStandaloneLocalConfiguration#configure(org.codehaus.cargo.container.LocalContainer)
     */
    protected void doConfigure(LocalContainer container) throws Exception
    {
        setupConfigurationDir();

        FileUtils fileUtils = FileUtils.newFileUtils();
        FilterChain filterChain = createOrionFilterChain();

        String confDir = getFileHandler().createDirectory(getHome(), "conf");
        String resourcePath = RESOURCE_PATH + "orion1x2x";

        getResourceUtils().copyResource(resourcePath + "/server.xml",
            new File(confDir, "server.xml"), filterChain);
        getResourceUtils().copyResource(resourcePath + "/application.xml",
            new File(confDir, "application.xml"), filterChain);
        getResourceUtils().copyResource(resourcePath + "/default-web-site.xml",
            new File(confDir, "default-web-site.xml"), filterChain);

        getResourceUtils().copyResource(resourcePath + "/mime.types",
            new File(confDir, "mime.types"), filterChain);
        getResourceUtils().copyResource(resourcePath + "/principals.xml",
            new File(confDir, "principals.xml"), filterChain);
        getResourceUtils().copyResource(resourcePath + "/rmi.xml",
            new File(confDir, "rmi.xml"), filterChain);
        getResourceUtils().copyResource(resourcePath + "/data-sources.xml",
            new File(confDir, "data-sources.xml"), filterChain);

        copyCustomResources(new File(confDir), filterChain);

        // Create default web app (required by Orion unfortunately...)
        String defaultWebAppDir = getFileHandler().createDirectory(getHome(),
            "default-web-app/WEB-INF");
        getResourceUtils().copyResource(resourcePath + "/web.xml",
            new File(defaultWebAppDir, "web.xml"), filterChain);

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
        Iterator it = getDeployables().iterator();
        while (it.hasNext())
        {
            Deployable deployable = (Deployable) it.next();

            if ((deployable.getType() != DeployableType.WAR)
                || ((deployable.getType() == DeployableType.WAR)
                    && !((WAR) deployable).isExpandedWar()))
            {
                fileUtils.copyFile(new File(deployable.getFile()),
                    new File(appDir, new File(deployable.getFile()).getName()),
                    null, true);
            }
        }

        // Deploy the CPC (Cargo Ping Component) to the webapps directory
        getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war",
            new File(appDir, "cargocpc.war"));
    }

    /**
     * Copy resources that are different between the different standalone implementations.
     *
     * @param confDir the configuration dir where to copy the resources to
     * @param filterChain the Ant filter chain to apply when copying the resources
     * @throws Exception in case of an error during the copy
     */
    protected abstract void copyCustomResources(File confDir, FilterChain filterChain)
        throws Exception;

    /**
     * @return an Ant filter chain containing implementation for the filter
     *         tokens used in the Orion configuration files
     */
    private FilterChain createOrionFilterChain()
    {
        FilterChain filterChain = createFilterChain();

        // Add Orion RMI port token
        getAntUtils().addTokenToFilterChain(filterChain, GeneralPropertySet.RMI_PORT,
            getPropertyValue(GeneralPropertySet.RMI_PORT));

        // Add token filters for adding users and roles
        if (getPropertyValue(ServletPropertySet.USERS) != null)
        {
            getAntUtils().addTokenToFilterChain(filterChain, "orion.users", getUserToken());
            getAntUtils().addTokenToFilterChain(filterChain, "orion.roles", getRoleToken());
        }

        // Replace datasource token
        getAntUtils().addTokenToFilterChain(filterChain, "orion.datasource",
            createDatasourceTokenValue());

        // Add application deployment tokens

        ReplaceTokens.Token tokenApplications = new ReplaceTokens.Token();
        tokenApplications.setKey("orion.application");

        ReplaceTokens.Token tokenWebModules = new ReplaceTokens.Token();
        tokenWebModules.setKey("orion.web-module");

        ReplaceTokens.Token tokenWebApps = new ReplaceTokens.Token();
        tokenWebApps.setKey("orion.web-app");

        // Note: The following values must never be empty string as otherwise
        // the Ant filtering code fails.
        StringBuffer keyApplications = new StringBuffer(" ");
        StringBuffer keyWebModules = new StringBuffer(" ");
        StringBuffer keyWebApps = new StringBuffer(" ");

        Iterator it = getDeployables().iterator();
        while (it.hasNext())
        {
            Deployable deployable = (Deployable) it.next();

            if (deployable.getType() == DeployableType.EAR)
            {
                keyApplications.append("  <application name=\"");
                keyApplications.append(((EAR) deployable).getName());
                keyApplications.append("\" path=\"../applications/");
                keyApplications.append(new File(deployable.getFile()).getName());
                keyApplications.append("\"/>");

                Iterator itContexts = ((EAR) deployable).getWebContexts();
                while (itContexts.hasNext())
                {
                    String webContext = (String) itContexts.next();

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

                if (((WAR) deployable).isExpandedWar())
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
        StringBuffer token = new StringBuffer(" ");

        // Add token filters for authenticated users
        if (getPropertyValue(ServletPropertySet.USERS) != null)
        {
            Iterator users = User.parseUsers(getPropertyValue(ServletPropertySet.USERS)).iterator();
            while (users.hasNext())
            {
                User user = (User) users.next();
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
        StringBuffer token = new StringBuffer(" ");

        List users = User.parseUsers(getPropertyValue(ServletPropertySet.USERS));
        Map roles = User.createRoleMap(users);

        Iterator roleIt = roles.keySet().iterator();
        while (roleIt.hasNext())
        {
            String role = (String) roleIt.next();

            token.append("<security-role-mapping ");
            token.append("name=\"" + role + "\">");

            Iterator userIt = ((List) roles.get(role)).iterator();
            while (userIt.hasNext())
            {
                User user = (User) userIt.next();

                token.append("<user name=\"" + user.getName() + "\"/>");
            }

            token.append("</security-role-mapping>");

        }

        return token.toString();
    }

    /**
     * @return the XML to be put into the server.xml file
     */
    protected String createDatasourceTokenValue()
    {
        getLogger().debug("Orion createDatasourceTokenValue", this.getClass().getName());

        final String dataSourceProperty = getPropertyValue(DatasourcePropertySet.DATASOURCE);
        getLogger().debug("Datasource property value [" + dataSourceProperty + "]",
            this.getClass().getName());

        if (dataSourceProperty == null)
        {
            // have to return a non-empty string, as Ant's token stuff doesn't work otherwise
            return " ";
        }
        else
        {
            DataSource ds = new DataSource(dataSourceProperty);
            return " <data-source\n"
                + "    class='com.evermind.sql.DriverManagerDataSource' \n "
                + "    name='Cargo-Datasource' \n"
                + "    location='" + ds.getJndiLocation() + "' \n"
                + "    xa-location='" + ds.getJndiLocation() + "XA'\n"
                + "    ejb-location='" + ds.getJndiLocation() + "EJB'\n"
                + "    connection-driver='" + ds.getDriverClass() + "'\n"
                + "    username='" + ds.getUsername() + "'\n"
                + "    password='" + ds.getPassword() + "'\n"
                + "    url='" + ds.getUrl() + "'\n"
                + "    inactivity-timeout='30' \n"
                + "/>";
        }
    }
}
