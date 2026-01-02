/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.jo.internal.Jo1xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.LoggingLevel;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration;
import org.codehaus.cargo.util.FileHandler;

/**
 * jo! standalone {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration}
 * implementation.
 */
public class Jo1xStandaloneLocalConfiguration extends AbstractStandaloneLocalConfiguration
{
    /**
     * Token key for the WAR directory.
     */
    private static final String TOKEN_KEY_WAR_DIR = "jo.wardir";

    /**
     * Token key for the Web applications.
     */
    private static final String TOKEN_KEY_WEBAPP = "jo.webapp";

    /**
     * Token key for the log level.
     */
    private static final String TOKEN_KEY_LOGLEVEL = "jo.loglevel";

    /**
     * Default metaserver port.
     */
    private static final String DEFAULT_METASERVER_PORT = "9090";

    /**
     * Capability of the jo! standalone configuration.
     */
    private static final ConfigurationCapability CAPABILITY =
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
     */
    @Override
    public ConfigurationCapability getCapability()
    {
        return CAPABILITY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doConfigure(LocalContainer container) throws Exception
    {
        setupConfigurationDir();

        Map<String, String> replacements = createJoReplacements();

        String confDir = getFileHandler().createDirectory(getHome(), "etc");
        String resourcePath = RESOURCE_PATH + container.getId();

        getResourceUtils().copyResource(resourcePath + "/factory.properties",
            new File(confDir, "factory.properties"), replacements, StandardCharsets.ISO_8859_1);
        getResourceUtils().copyResource(resourcePath + "/groups.properties",
            new File(confDir, "groups.properties"), replacements, StandardCharsets.ISO_8859_1);
        getResourceUtils().copyResource(resourcePath + "/hosts.properties",
            new File(confDir, "hosts.properties"), replacements, StandardCharsets.ISO_8859_1);

        getResourceUtils().copyResource(resourcePath + "/listener.properties",
            new File(confDir, "listener.properties"), replacements, StandardCharsets.ISO_8859_1);
        getResourceUtils().copyResource(resourcePath + "/mime.properties",
            new File(confDir, "mime.properties"), replacements, StandardCharsets.ISO_8859_1);
        getResourceUtils().copyResource(resourcePath + "/roles.properties",
            new File(confDir, "roles.properties"), replacements, StandardCharsets.ISO_8859_1);
        getResourceUtils().copyResource(resourcePath + "/server.properties",
            new File(confDir, "server.properties"), replacements, StandardCharsets.ISO_8859_1);
        getResourceUtils().copyResource(resourcePath + "/users.properties",
            new File(confDir, "users.properties"), replacements, StandardCharsets.ISO_8859_1);
        getResourceUtils().copyResource(resourcePath + "/metaserver.properties",
            new File(confDir, "metaserver.properties"), replacements, StandardCharsets.ISO_8859_1);
        getResourceUtils().copyResource(resourcePath + "/metalistener.properties",
            new File(confDir, "metalistener.properties"), replacements,
                StandardCharsets.ISO_8859_1);

        // jo! log directory
        getFileHandler().createDirectory(getHome(), "log");

        // make sure the webapp directory exists.
        getFileHandler().createDirectory(getHome(), "webapp/host");

        // Deploy the CPC (Cargo Ping Component) to the webapps directory
        getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war",
            new File(getHome(), "webapp/host/cargocpc.war"));
    }

    /**
     * @return token with all the user-defined token value
     * @throws MalformedURLException if the document base cannot be determined
     */
    private Map<String, String> createJoReplacements() throws MalformedURLException
    {
        Map<String, String> replacements = getReplacements();

        // set loglevel
        replacements.put(TOKEN_KEY_LOGLEVEL, createLogLevelToken());
        // set war dir
        replacements.put(TOKEN_KEY_WAR_DIR, createWarDirToken());
        // add application deployment tokens
        replacements.put(TOKEN_KEY_WEBAPP, createWebappToken());
        // TODO: Add token filters for adding users and roles

        return replacements;
    }

    /**
     * Create token for the deployed webapps.
     * 
     * @return token for the deployed webapps.
     * @throws MalformedURLException if the document base cannot be determined
     */
    private String createWebappToken() throws MalformedURLException
    {
        StringBuilder keyWebApps = new StringBuilder();

        for (Deployable deployable : getDeployables())
        {
            if (deployable.getType() == DeployableType.WAR)
            {
                WAR war = (WAR) deployable;
                // This is what we need to generate:
                // <hostname>.webapp.<webapp-name>.mapping=...
                // <hostname>.webapp.<webapp-name>.docbase=...
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
                else if (!mapping.startsWith("/") && !mapping.isEmpty())
                {
                    mapping = "/" + mapping;
                }
                String docbase = getFileHandler().getURL(war.getFile());
                if (war.isExpanded())
                {
                    docbase += "/";
                }

                keyWebApps.append("# CARGO! Context: " + war.getContext() + " File: "
                    + war.getFile() + FileHandler.NEW_LINE);
                keyWebApps.append("host.webapp." + webappName + ".mapping=" + mapping
                    + FileHandler.NEW_LINE);
                keyWebApps.append("host.webapp." + webappName + ".docbase=" + docbase
                    + FileHandler.NEW_LINE);
                keyWebApps.append(FileHandler.NEW_LINE);
            }
        }

        return keyWebApps.toString();
    }

    /**
     * Creates tokens for the loglevel.
     * 
     * @return loglevel token
     */
    private String createLogLevelToken()
    {
        String logLevel = getPropertyValue(GeneralPropertySet.LOGGING);
        String joLogLevel;
        if (LoggingLevel.LOW.equalsLevel(logLevel))
        {
            joLogLevel = "1";
        }
        else if (LoggingLevel.HIGH.equalsLevel(logLevel))
        {
            joLogLevel = "5";
        }
        else
        {
            joLogLevel = "2";
        }

        return joLogLevel;
    }

    /**
     * Creates tokens for the hotdeployment war dir.
     * 
     * @return wardir token
     */
    private String createWarDirToken()
    {
        return getFileHandler().append(getHome(), "webapp/host");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "jo! 1.x Standalone Configuration";
    }
}
