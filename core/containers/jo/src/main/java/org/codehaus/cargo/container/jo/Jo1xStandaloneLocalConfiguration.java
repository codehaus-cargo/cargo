/*
 * ========================================================================
 *
 * Copyright 2005-2006 Vincent Massol.
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
package org.codehaus.cargo.container.jo;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Iterator;

import org.apache.tools.ant.filters.ReplaceTokens;
import org.apache.tools.ant.types.FilterChain;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.jo.internal.Jo1xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration;

/**
 * jo! standalone {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration} 
 * implementation.
 *
 * @version $Id$
 */
public class Jo1xStandaloneLocalConfiguration extends AbstractStandaloneLocalConfiguration
{
    /**
     * Line separator.
     */
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    /**
     * Token key.
     */
    private static final String TOKEN_KEY_WAR_DIR = "jo.wardir";
    
    /**
     * Token key.
     */
    private static final String TOKEN_KEY_WEBAPP = "jo.webapp";
    
    /**
     * Token key.
     */
    private static final String TOKEN_KEY_LOGLEVEL = "jo.loglevel";
    
    /**
     * Default hostname.
     */
    private static final String DEFAULT_HOSTNAME = "*";
    
    /**
     * Default port.
     */
    private static final String DEFAULT_PORT = "8080";
    
    /**
     * Default loglevel.
     */
    private static final String DEFAULT_LOGLEVEL = "medium";
    
    /**
     * Default metaserver port.
     */
    private static final String DEFAULT_METASERVER_PORT = "9090";

    /**
     * Capability of the jo! standalone configuration.
     */
    private static ConfigurationCapability capability = 
        new Jo1xStandaloneLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see AbstractStandaloneLocalConfiguration#AbstractStandaloneLocalConfiguration(String) 
     */
    public Jo1xStandaloneLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(GeneralPropertySet.RMI_PORT, DEFAULT_METASERVER_PORT);
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
     * @see AbstractStandaloneLocalConfiguration#configure(LocalContainer)
     */
    protected void doConfigure(LocalContainer container) throws Exception
    {
        setupConfigurationDir();

        FilterChain filterChain = createJoFilterChain();

        String confDir = getFileHandler().createDirectory(getHome(), "etc");
        String resourcePath = RESOURCE_PATH + container.getId();

        getResourceUtils().copyResource(resourcePath + "/factory.properties",
                new File(confDir, "factory.properties"), filterChain);
        getResourceUtils().copyResource(resourcePath + "/groups.properties",
                new File(confDir, "groups.properties"), filterChain);
        getResourceUtils().copyResource(resourcePath + "/hosts.properties",
                new File(confDir, "hosts.properties"), filterChain);

        getResourceUtils().copyResource(resourcePath + "/listener.properties",
                new File(confDir, "listener.properties"), filterChain);
        getResourceUtils().copyResource(resourcePath + "/mime.properties",
                new File(confDir, "mime.properties"), filterChain);
        getResourceUtils().copyResource(resourcePath + "/roles.properties",
                new File(confDir, "roles.properties"), filterChain);
        getResourceUtils().copyResource(resourcePath + "/server.properties",
                new File(confDir, "server.properties"), filterChain);
        getResourceUtils().copyResource(resourcePath + "/users.properties",
                new File(confDir, "users.properties"), filterChain);
        getResourceUtils().copyResource(resourcePath + "/metaserver.properties",
                new File(confDir, "metaserver.properties"), filterChain);
        getResourceUtils().copyResource(resourcePath + "/metalistener.properties",
                new File(confDir, "metalistener.properties"), filterChain);


        // jo! log directory
        getFileHandler().createDirectory(getHome(), "log");

        // make sure the webapp directory exists.
        new File(getHome(), "webapp/host/").mkdirs();

        // Deploy the CPC (Cargo Ping Component) to the webapps directory
        getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war",
                new File(getHome(), "webapp/host/cargocpc.war"));
    }

    /**
     * @return an Ant filter chain containing implementation for the filter
     *         tokens used in the Orion configuration files
     * @throws MalformedURLException if the document base cannot be determined
     */
    private FilterChain createJoFilterChain() throws MalformedURLException
    {
        FilterChain filterChain = getFilterChain();

        // TODO: Add token filters for adding users and roles
        // Add application deployment tokens
        filterChain.addReplaceTokens(createWebappToken());
        // set virtual hostname
        filterChain.addReplaceTokens(createHostnameToken());
        // set port
        filterChain.addReplaceTokens(createPortToken());
        // set loglevel
        filterChain.addReplaceTokens(createLogLevelToken());
        // set metaserver port
        filterChain.addReplaceTokens(createMetaserverPortToken());
        // set war dir
        filterChain.addReplaceTokens(createWarDirToken());
        return filterChain;
    }

    /**
     * Create {@link ReplaceTokens} for the deployed webapps.
     *
     * @return tokens
     * @throws MalformedURLException if the document base cannot be determined
     */
    private ReplaceTokens createWebappToken() throws MalformedURLException
    {
        ReplaceTokens.Token tokenWebApps = new ReplaceTokens.Token();
        tokenWebApps.setKey(TOKEN_KEY_WEBAPP);

        StringBuffer keyWebApps = new StringBuffer();

        Iterator it = getDeployables().iterator();
        while (it.hasNext())
        {
            Deployable deployable = (Deployable) it.next();

            if (deployable.getType() == DeployableType.WAR)
            {
                WAR war = (WAR) deployable;
                /*
                // This is what we need to generate:
                <hostname>.webapp.<webapp-name>.mapping=
                <hostname>.webapp.<webapp-name>.docbase=
                */
                final String webappName = war.getContext().replace('.', '_').replace('=', '_');
                String mapping = war.getContext();
                if (mapping == null)
                {
                    mapping = "";
                }
                else if ("/".equals(mapping))
                {
                    mapping = "";
                }
                else if (!mapping.startsWith("/") && mapping.length() > 1)
                {
                    mapping = "/" + mapping;
                }
                String docbase = getFileHandler().getURL(war.getFile()).toString();
                if (war.isExpandedWar())
                {
                    docbase += "/";
                }

                keyWebApps.append("# CARGO! Context: " + war.getContext() + " File: "
                    + war.getFile() + LINE_SEPARATOR);
                keyWebApps.append("host.webapp." + webappName + ".mapping=" + mapping
                    + LINE_SEPARATOR);
                keyWebApps.append("host.webapp." + webappName + ".docbase=" + docbase
                    + LINE_SEPARATOR);
                keyWebApps.append(LINE_SEPARATOR);
            }
        }

        // Note: The following values must never be empty string as otherwise
        // the Ant filtering code fails.
        if (keyWebApps.length() == 0)
        {
            keyWebApps.append(" ");
        }
        tokenWebApps.setValue(keyWebApps.toString());
        ReplaceTokens replaceWebApps = new ReplaceTokens();
        replaceWebApps.addConfiguredToken(tokenWebApps);

        return replaceWebApps;
    }

    /**
     * Creates tokens for the (virtual) hostname.
     *
     * @return hostname token
     */
    private ReplaceTokens createHostnameToken()
    {
        ReplaceTokens.Token tokenHostname = new ReplaceTokens.Token();
        tokenHostname.setKey(GeneralPropertySet.HOSTNAME);

        String hostname = getPropertyValue(GeneralPropertySet.HOSTNAME);
        // default to *
        if (hostname == null)
        {
            hostname = DEFAULT_HOSTNAME;
        }

        tokenHostname.setValue(hostname);
        ReplaceTokens replaceHostname = new ReplaceTokens();
        replaceHostname.addConfiguredToken(tokenHostname);
        return replaceHostname;
    }

    /**
     * Creates tokens for the port.
     *
     * @return port token
     */
    private ReplaceTokens createPortToken()
    {
        ReplaceTokens.Token tokenPort = new ReplaceTokens.Token();
        tokenPort.setKey(ServletPropertySet.PORT);

        String port = getPropertyValue(ServletPropertySet.PORT);
        // default to 8080
        if (port == null)
        {
            port = DEFAULT_PORT;
        }

        tokenPort.setValue(port);
        ReplaceTokens replacePort = new ReplaceTokens();
        replacePort.addConfiguredToken(tokenPort);
        return replacePort;
    }

    /**
     * Creates tokens for the loglevel.
     *
     * @return loglevel token
     */
    private ReplaceTokens createLogLevelToken()
    {
        ReplaceTokens.Token tokenLogLevel = new ReplaceTokens.Token();
        tokenLogLevel.setKey(TOKEN_KEY_LOGLEVEL);

        String logLevel = getPropertyValue(GeneralPropertySet.LOGGING);
        // default to medium
        if (logLevel == null)
        {
            logLevel = DEFAULT_LOGLEVEL;
        }
        else
        {
            logLevel = logLevel.toLowerCase();
        }
        String joLogLevel;
        if ("low".equals(logLevel))
        {
            joLogLevel = "1";
        }
        else if ("medium".equals(logLevel))
        {
            joLogLevel = "2";
        }
        else if ("high".equals(logLevel))
        {
            joLogLevel = "5";
        }
        else
        {
            joLogLevel = "2";
        }

        tokenLogLevel.setValue(joLogLevel);
        ReplaceTokens replacePort = new ReplaceTokens();
        replacePort.addConfiguredToken(tokenLogLevel);
        return replacePort;
    }


    /**
     * Creates tokens for the hotdeployment war dir.
     *
     * @return wardir token
     */
    private ReplaceTokens createWarDirToken()
    {
        ReplaceTokens.Token tokenWarDir = new ReplaceTokens.Token();
        tokenWarDir.setKey(TOKEN_KEY_WAR_DIR);
        tokenWarDir.setValue(new File(getHome(), "webapp/host").toString());
        ReplaceTokens replaceWarDir = new ReplaceTokens();
        replaceWarDir.addConfiguredToken(tokenWarDir);
        return replaceWarDir;
    }

    /**
     * Creates tokens for the metaserver listener port.
     *
     * @return port token
     */
    private ReplaceTokens createMetaserverPortToken()
    {
        ReplaceTokens.Token tokenPort = new ReplaceTokens.Token();
        tokenPort.setKey(GeneralPropertySet.RMI_PORT);

        String port = getPropertyValue(GeneralPropertySet.RMI_PORT);
        if (port == null)
        {
            throw new IllegalArgumentException("Property " + GeneralPropertySet.RMI_PORT
                + " not set!");
        }

        tokenPort.setValue(port);
        ReplaceTokens replacePort = new ReplaceTokens();
        replacePort.addConfiguredToken(tokenPort);
        return replacePort;
    }

    /**
     * {@inheritDoc}
     * @see Object#toString()
     */
    public String toString()
    {
        return "jo! 1.x Standalone Configuration";
    }
}
