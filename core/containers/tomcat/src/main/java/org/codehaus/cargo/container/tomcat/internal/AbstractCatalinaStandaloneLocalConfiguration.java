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
package org.codehaus.cargo.container.tomcat.internal;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.tools.ant.types.FilterChain;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.EmbeddedLocalContainer;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.FileConfig;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.configuration.entry.ResourceSupport;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.LoggingLevel;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.property.User;
import org.codehaus.cargo.container.spi.configuration.builder.AbstractStandaloneLocalConfigurationWithXMLConfigurationBuilder;
import org.codehaus.cargo.container.tomcat.Tomcat5xEmbeddedLocalContainer;
import org.codehaus.cargo.container.tomcat.Tomcat5xEmbeddedLocalDeployer;
import org.codehaus.cargo.container.tomcat.TomcatCopyingInstalledLocalDeployer;
import org.codehaus.cargo.container.tomcat.TomcatPropertySet;

/**
 * Catalina standalone {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration}
 * implementation.
 * 
 * @version $Id$
 */
public abstract class AbstractCatalinaStandaloneLocalConfiguration extends
    AbstractStandaloneLocalConfigurationWithXMLConfigurationBuilder implements ResourceSupport
{

    /**
     * {@inheritDoc}
     * 
     * @see AbstractStandaloneLocalConfigurationWithXMLConfigurationBuilder#AbstractStandaloneLocalConfigurationWithXMLConfigurationBuilder(String)
     */
    public AbstractCatalinaStandaloneLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(ServletPropertySet.USERS, "admin::manager");
        setProperty(GeneralPropertySet.RMI_PORT, "8205");
        setProperty(GeneralPropertySet.URI_ENCODING, "ISO-8859-1");
        setProperty(TomcatPropertySet.AJP_PORT, "8009");
        setProperty(TomcatPropertySet.CONTEXT_RELOADABLE, "false");
        setProperty(TomcatPropertySet.COPY_WARS, "true");
        setProperty(TomcatPropertySet.WEBAPPS_DIRECTORY, "webapps");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.spi.configuration.AbstractLocalConfiguration#configure(LocalContainer)
     */
    @Override
    protected void doConfigure(LocalContainer container) throws Exception
    {
        setupConfigurationDir();

        String confDir = getFileHandler().createDirectory(getHome(), "conf");
        FilterChain emptyFilterChain = createFilterChain();

        getFileHandler().createDirectory(getHome(), "temp");
        getFileHandler().createDirectory(getHome(), "logs");

        if (container instanceof InstalledLocalContainer)
        {
            InstalledLocalContainer installedContainer = (InstalledLocalContainer) container;
            String[] classPath = installedContainer.getExtraClasspath();
            if (classPath != null)
            {
                for (String path : classPath)
                {
                    FileConfig fc = new FileConfig();
                    fc.setFile(path);
                    fc.setToDir("common/lib");
                    setFileProperty(fc);
                }
            }
            classPath = installedContainer.getSharedClasspath();
            if (classPath != null)
            {
                for (String path : classPath)
                {
                    FileConfig fc = new FileConfig();
                    fc.setFile(path);
                    fc.setToDir("shared/lib");
                    setFileProperty(fc);
                }
            }

            String sourceConf = getFileHandler().append(installedContainer.getHome(), "conf");
            String targetConf = getFileHandler().createDirectory(getHome(), "conf");
            getFileHandler().copyDirectory(sourceConf, targetConf);

            setupConfFiles(targetConf);
        }
        else if (container instanceof EmbeddedLocalContainer)
        {
            String webXml = getFileHandler().append(confDir, "web.xml");
            getResourceUtils().copyResource(RESOURCE_PATH + container.getId() + "/web.xml", webXml,
                getFileHandler(), emptyFilterChain, "UTF-8");
        }

        String tomcatUsersXml = getFileHandler().append(confDir, "tomcat-users.xml");
        getResourceUtils().copyResource(RESOURCE_PATH + "tomcat/tomcat-users.xml", tomcatUsersXml,
            getFileHandler(), emptyFilterChain, "UTF-8");
        Map<String, String> replacements = new HashMap<String, String>(1);
        replacements.put("@tomcat.users@", getSecurityToken());
        getFileHandler().replaceInFile(tomcatUsersXml, replacements, "UTF-8");
        String loggingProperties = getFileHandler().append(confDir, "logging.properties");
        getResourceUtils().copyResource(RESOURCE_PATH + "tomcat/logging.properties",
            loggingProperties, getFileHandler(), emptyFilterChain, "UTF-8");
        replacements.clear();
        replacements.put("@catalina.logging.level@",
            getTomcatLoggingLevel(getPropertyValue(GeneralPropertySet.LOGGING)));
        getFileHandler().replaceInFile(loggingProperties, replacements, "UTF-8");
        getResourceUtils().copyResource(RESOURCE_PATH + "tomcat/context.xml",
            getFileHandler().append(confDir, "context.xml"), getFileHandler(),
            createFilterChain(), "UTF-8");

        setupManager(container);

        // deploy the web-app by copying the WAR file
        setupWebApps(container);
    }

    /**
     * Escapes a Windows path: backslashes become slashes, drive paths get prefixed with a slash.
     * 
     * @param path Path to escape.
     * @return Escaped path.
     */
    protected String escapePath(String path)
    {
        String escapedPath = path;

        if (escapedPath.contains("\\"))
        {
            // This is a Windows that needs to be converted
            if (escapedPath.contains(":\\"))
            {
                // This is a path with a drive letter,
                // that needs to be prefixed
                escapedPath = '/' + escapedPath;
            }
            escapedPath = escapedPath.replace('\\', '/');
        }

        return escapedPath;
    }

    /**
     * {@inheritDoc} note that if there is any datasource configured, this will imply an addition of
     * the transaction manager.
     * 
     * @see #setupTransactionManager()
     */
    @Override
    public void configureDataSources(LocalContainer container)
    {
        super.configureDataSources(container);
        this.setupTransactionManager();
    }

    /**
     * Adds an implementation of UserTransaction to the configuration.
     */
    protected abstract void setupTransactionManager();

    /**
     * Setup the manager webapp.
     * 
     * @param container the container to configure
     */
    protected abstract void setupManager(LocalContainer container);

    /**
     * Setup the web apps directory and deploy applications.
     * 
     * @param container the container to configure
     */
    private void setupWebApps(LocalContainer container)
    {
        try
        {
            if (container instanceof EmbeddedLocalContainer)
            {
                // embedded Tomcat doesn't need CPC
                Tomcat5xEmbeddedLocalDeployer deployer =
                    new Tomcat5xEmbeddedLocalDeployer((Tomcat5xEmbeddedLocalContainer) container);
                deployer.deploy(getDeployables());
            }
            else
            {
                // Create a webapps directory for automatic deployment of WARs dropped inside.
                String appDir = getFileHandler().createDirectory(getHome(),
                    getPropertyValue(TomcatPropertySet.WEBAPPS_DIRECTORY));

                // Deploy all deployables into the webapps directory.
                TomcatCopyingInstalledLocalDeployer deployer =
                    new TomcatCopyingInstalledLocalDeployer((InstalledLocalContainer) container);
                deployer.setShouldCopyWars(Boolean.parseBoolean(
                    getPropertyValue(TomcatPropertySet.COPY_WARS)));
                deployer.deploy(getDeployables());

                // Deploy the CPC (Cargo Ping Component) to the webapps directory
                getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war",
                    getFileHandler().append(appDir, "cargocpc.war"), getFileHandler());
            }
        }
        catch (Exception e)
        {
            throw new ContainerException("Failed to create a " + container.getName()
                + " container configuration", e);
        }
    }

    /**
     * Translate Cargo logging levels into Tomcat logging levels.
     * 
     * @param cargoLoggingLevel Cargo logging level
     * @return the corresponding Tomcat logging level
     */
    protected String getTomcatLoggingLevel(String cargoLoggingLevel)
    {
        String level;

        if (LoggingLevel.LOW.equalsLevel(cargoLoggingLevel))
        {
            level = "1";
        }
        else if (LoggingLevel.MEDIUM.equalsLevel(cargoLoggingLevel))
        {
            level = "2";
        }
        else
        {
            level = "4";
        }

        return level;
    }

    /**
     * Create the Tomcat <code>&lt;webapp&gt;</code> token.
     * 
     * @return The Tomcat <code>&lt;webapp&gt;</code> token.
     */
    protected String createTomcatWebappsToken()
    {
        // Add webapp contexts in order to explicitely point to where the
        // wars are located.
        StringBuilder webappTokenValue = new StringBuilder(" ");

        // Determine whether to copyWars on deployment
        boolean copyWars = Boolean.parseBoolean(getPropertyValue(TomcatPropertySet.COPY_WARS));

        for (Deployable deployable : getDeployables())
        {
            if (deployable.getType() != DeployableType.WAR)
            {
                throw new ContainerException("Only WAR archives are supported for deployment "
                    + "in Tomcat. Got [" + deployable.getFile() + "]");
            }

            // Do not create tokens for WARs which are copied to the webapps directory:
            // either by configuration or when containing a context file.
            if (copyWars || TomcatUtils.containsContextFile(deployable))
            {
                continue;
            }

            webappTokenValue.append(createContextToken((WAR) deployable));
        }

        return webappTokenValue.toString();
    }

    /**
     * @param deployable the WAR to deploy
     * @return the "context" XML element to instert in the Tomcat <code>server.xml</code>
     * configuration file
     */
    protected String createContextToken(WAR deployable)
    {
        StringBuilder contextTokenValue = new StringBuilder();
        contextTokenValue.append("<Context");

        // Tomcat requires a context path equal to a zero-length string for default web application
        contextTokenValue.append(" path=\"");
        if (!"".equals(deployable.getContext()) && !"/".equals(deployable.getContext()))
        {
            contextTokenValue.append("/" + deployable.getContext());
        }
        contextTokenValue.append("\"");

        // Tomcat requires an absolute path for the "docBase" attribute.
        contextTokenValue.append(" docBase=\"");
        contextTokenValue.append(new File(deployable.getFile()).getAbsolutePath());
        contextTokenValue.append("\"");

        contextTokenValue.append(" debug=\"");
        contextTokenValue.append(getTomcatLoggingLevel(getPropertyValue(
            GeneralPropertySet.LOGGING)));
        contextTokenValue.append("\"");

        contextTokenValue.append(" reloadable=\"");
        contextTokenValue.append(getPropertyValue(TomcatPropertySet.CONTEXT_RELOADABLE));
        contextTokenValue.append("\"");

        contextTokenValue.append(">");
        contextTokenValue.append("</Context>");
        return contextTokenValue.toString();
    }

    /**
     * {@inheritDoc}
     * 
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        return "Catalina Standalone Configuration";
    }

    /**
     * @return an Ant filter token containing all the user-defined users
     */
    protected String getSecurityToken()
    {
        StringBuilder token = new StringBuilder("");

        // Add token filters for authenticated users
        if (getPropertyValue(ServletPropertySet.USERS) != null)
        {
            StringBuilder usersToken = new StringBuilder("");

            Set<String> rolesSet = new HashSet<String>();
            for (User user : User.parseUsers(getPropertyValue(ServletPropertySet.USERS)))
            {
                usersToken.append("<user ");
                usersToken.append("name=\"" + user.getName() + "\" ");
                usersToken.append("password=\"" + user.getPassword() + "\" ");

                usersToken.append("roles=\"");
                Iterator<String> roles = user.getRoles().iterator();
                while (roles.hasNext())
                {
                    String role = roles.next();
                    usersToken.append(role);
                    if (roles.hasNext())
                    {
                        usersToken.append(',');
                    }
                    rolesSet.add(role);
                }
                usersToken.append("\"/>\n  ");
            }

            StringBuilder rolesToken = new StringBuilder("");
            for (String role : rolesSet)
            {
                rolesToken.append("<role rolename=\"" + role + "\"/>\n  ");
            }

            token.append(rolesToken).append(usersToken);
        }

        return token.toString();
    }

    /**
     * setup the files in the configuration's <code>conf</code> directory.
     * 
     * @param confDir - the <code>conf</code> directory.
     */
    protected abstract void setupConfFiles(String confDir);

    /**
     * Resource entries must be stored in the xml configuration file. Under which element do we
     * insert the entries? example: //Engine/DefaultContext
     * 
     * @return path the the parent element resources should be inserted under.
     */
    @Override
    protected String getXpathForDataSourcesParent()
    {
        return getXpathForResourcesParent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getOrCreateDataSourceConfigurationFile(DataSource ds, LocalContainer container)
    {
        return getOrCreateResourceConfigurationFile(null, container);
    }

    /**
     * Implementations should avoid passing null, and instead pass
     * <code>Collections.emptyMap()</code>, if the document is DTD bound.
     * 
     * @return a map of prefixes to the url namespaces used in the datasource or resource
     * configuration file.
     */
    @Override
    protected Map<String, String> getNamespaces()
    {
        return Collections.emptyMap();
    }
}
