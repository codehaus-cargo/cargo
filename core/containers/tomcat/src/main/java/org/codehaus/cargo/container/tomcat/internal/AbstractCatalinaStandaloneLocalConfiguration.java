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
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.tools.ant.types.FilterChain;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.EmbeddedLocalContainer;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
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
     * @see TomcatStandaloneLocalConfigurationCapability
     */
    private static ConfigurationCapability capability =
        new TomcatStandaloneLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * 
     * @see AbstractStandaloneLocalConfigurationWithXMLConfigurationBuilder#AbstractStandaloneLocalConfigurationWithDataSourceSupport(String)
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

        FilterChain filterChain = createTomcatFilterChain();

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
        }

        setupConfFiles(container, filterChain);

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
     * files that should be copied to the conf directory for the server to operate.
     * 
     * @return set of filenames to copy upon doConfigure
     */
    protected Set<String> getConfFiles()
    {
        Set<String> confFiles = new HashSet<String>();
        confFiles.add("server.xml");
        confFiles.add("tomcat-users.xml");
        confFiles.add("web.xml");
        return confFiles;
    }

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
                String appDir = getFileHandler().createDirectory(getHome(), "webapps");

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
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration#createFilterChain()
     */
    protected FilterChain createTomcatFilterChain()
    {
        FilterChain filterChain = getFilterChain();

        // Add logging property tokens
        getAntUtils().addTokenToFilterChain(filterChain, "catalina.logging.level",
            getTomcatLoggingLevel(getPropertyValue(GeneralPropertySet.LOGGING)));

        // Add Tomcat shutdown port token
        getAntUtils().addTokenToFilterChain(filterChain, GeneralPropertySet.RMI_PORT,
            getPropertyValue(GeneralPropertySet.RMI_PORT));

        // Add AJP connector port token
        getAntUtils().addTokenToFilterChain(filterChain, TomcatPropertySet.AJP_PORT,
            getPropertyValue(TomcatPropertySet.AJP_PORT));

        // Add Catalina secure token, set to true if the protocol is https, false otherwise
        getAntUtils().addTokenToFilterChain(
            filterChain,
            "catalina.secure",
            String.valueOf("https"
                .equalsIgnoreCase(getPropertyValue(GeneralPropertySet.PROTOCOL))));

        // Add token filters for authenticated users
        getAntUtils().addTokenToFilterChain(filterChain, "tomcat.users", getSecurityToken());

        getAntUtils().addTokenToFilterChain(filterChain, "catalina.servlet.uriencoding",
            getPropertyValue(GeneralPropertySet.URI_ENCODING));

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

        getAntUtils().addTokenToFilterChain(filterChain, "tomcat.webapps",
            webappTokenValue.toString());

        return filterChain;
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
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.configuration.Configuration#getCapability()
     */
    public ConfigurationCapability getCapability()
    {
        return capability;
    }

    /**
     * @return an Ant filter token containing all the user-defined users
     */
    protected String getSecurityToken()
    {
        StringBuilder token = new StringBuilder(" ");

        // Add token filters for authenticated users
        if (getPropertyValue(ServletPropertySet.USERS) != null)
        {
            for (User user : User.parseUsers(getPropertyValue(ServletPropertySet.USERS)))
            {
                token.append("<user ");
                token.append("name=\"" + user.getName() + "\" ");
                token.append("password=\"" + user.getPassword() + "\" ");

                token.append("roles=\"");
                Iterator<String> roles = user.getRoles().iterator();
                while (roles.hasNext())
                {
                    String role = roles.next();
                    token.append(role);
                    if (roles.hasNext())
                    {
                        token.append(',');
                    }
                }
                token.append("\"/>");
            }
        }

        return token.toString();
    }

    /**
     * copy files to the conf directory, replacing tokens based on the filterchain parameter.
     * 
     * @param container - type of container configuration we are using.
     * @param filterChain - holds tokenization details
     * @throws IOException - if we cannot copy a file to the 'conf' directory
     */
    protected void setupConfFiles(LocalContainer container, FilterChain filterChain)
        throws IOException
    {
        String confDir = getFileHandler().createDirectory(getHome(), "conf");
        for (String file : getConfFiles())
        {
            getResourceUtils().copyResource(RESOURCE_PATH + container.getId() + "/" + file,
                getFileHandler().append(confDir, file), getFileHandler(), filterChain, "UTF-8");
        }
    }

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
