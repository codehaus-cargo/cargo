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
package org.codehaus.cargo.container.tomcat;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.codehaus.cargo.container.EmbeddedLocalContainer;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.configuration.builder.ConfigurationBuilder;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.internal.util.PropertyUtils;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.LoggingLevel;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.tomcat.internal.AbstractCatalinaStandaloneLocalConfiguration;
import org.codehaus.cargo.container.tomcat.internal.Tomcat5x6x7xConfigurationBuilder;
import org.codehaus.cargo.container.tomcat.internal.Tomcat5x6xStandaloneLocalConfigurationCapability;

/**
 * StandAloneLocalConfiguration that is appropriate for Tomcat 5.x containers.
 * <p>
 * This code needs to work with both {@link Tomcat5xInstalledLocalContainer} and
 * {@link Tomcat5xEmbeddedLocalContainer}.
 * 
 * @version $Id$
 */
public class Tomcat5xStandaloneLocalConfiguration extends
    AbstractCatalinaStandaloneLocalConfiguration
{

    /**
     * XPath expression for identifying the "Connector" element in the server.xml file.
     */
    protected static final String CONNECTOR_XPATH = 
        "//Server/Service/Connector[not(@protocol) or @protocol='HTTP/1.1' "
            + "or @protocol='org.apache.coyote.http11.Http11NioProtocol']";

    /**
     * {@inheritDoc}
     * 
     * @see TomcatStandaloneLocalConfigurationCapability
     */
    private static ConfigurationCapability capability =
        new Tomcat5x6xStandaloneLocalConfigurationCapability();

    /**
     * used to insert DataSources and Resources into the configuration file.
     */
    protected Tomcat5x6x7xConfigurationBuilder configurationBuilder;

    /**
     * {@inheritDoc}
     * 
     * @see AbstractCatalinaStandaloneLocalConfiguration#AbstractCatalinaStandaloneLocalConfiguration(String)
     */
    public Tomcat5xStandaloneLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(TomcatPropertySet.CONNECTOR_EMPTY_SESSION_PATH, "true");
        setProperty(TomcatPropertySet.HTTP_SECURE, "false");
        configurationBuilder = new Tomcat5x6x7xConfigurationBuilder();

        addXmlReplacements();
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
     * @see Tomcat5x6x7xConfigurationBuilder
     */
    @Override
    protected ConfigurationBuilder createConfigurationBuilder(LocalContainer container)
    {
        return configurationBuilder;
    }

    /**
     * {@inheritDoc} this does not deploy the manager, if the application is embedded.
     * 
     * @see AbstractCatalinaStandaloneLocalConfiguration#setupManager(org.codehaus.cargo.container.LocalContainer)
     */
    @Override
    protected void setupManager(LocalContainer container)
    {
        if (container instanceof EmbeddedLocalContainer)
        {
            // when running in the embedded mode, there's no need of any manager application.
        }
        else
        {
            String from = ((InstalledLocalContainer) container).getHome();
            String to = getHome();
            getFileHandler().copyDirectory(from + "/server/webapps/manager",
                to + "/server/webapps/manager");
            getFileHandler().copyFile(from + "/conf/Catalina/localhost/manager.xml",
                to + "/conf/Catalina/localhost/manager.xml");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void performXmlReplacements(LocalContainer container)
    {
        if (container instanceof EmbeddedLocalContainer)
        {
            // when running in the embedded mode, there's no need to replace files.
        }
        else
        {
            setProperty(
                org.codehaus.cargo.container.tomcat.TomcatPropertySet.HTTP_SECURE,
                String.valueOf("https".equalsIgnoreCase(
                    container.getConfiguration().getPropertyValue(
                        GeneralPropertySet.PROTOCOL))));

            addOptionalXmlReplacements(container);

            super.performXmlReplacements(container);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        return "Tomcat 5.x Standalone Configuration";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setupTransactionManager()
    {
        Resource transactionManagerResource =
            new Resource("UserTransaction", "javax.transaction.UserTransaction");

        Properties parameters = new Properties();
        PropertyUtils.setPropertyIfNotNull(parameters, "jotm.timeout", "60");
        PropertyUtils.setPropertyIfNotNull(parameters, "factory",
            "org.objectweb.jotm.UserTransactionFactory");
        transactionManagerResource.setParameters(PropertyUtils.toMap(parameters));
        getResources().add(transactionManagerResource);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getXpathForResourcesParent()
    {
        return "//Context";
    }

    /**
     * {@inheritDoc} In Tomcat 5.5+, we use context.xml to avoid configuration problems.
     */
    @Override
    protected String getOrCreateResourceConfigurationFile(Resource rs, LocalContainer container)
    {
        String confDir = getFileHandler().createDirectory(getHome(), "conf");
        return getFileHandler().append(confDir, "context.xml");
    }

    /**
     * Translate Cargo logging levels into java.util.logging levels.
     * Available levels are: SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST or ALL.
     * @param cargoLoggingLevel Cargo logging level
     * @return the corresponding Tomcat java.util.logging level
     */
    @Override
    protected String getTomcatLoggingLevel(String cargoLoggingLevel)
    {
        String level;

        if (LoggingLevel.LOW.equalsLevel(cargoLoggingLevel))
        {
            level = "WARNING";
        }
        else if (LoggingLevel.MEDIUM.equalsLevel(cargoLoggingLevel))
        {
            level = "INFO";
        }
        else
        {
            level = "FINE";
        }

        return level;
    }

    /**
     * Override the context element creation as Tomcat 5x and newer don't support the
     * debug attribute.
     * @param deployable the WAR to deploy
     * @return the "context" XML element to instert in the Tomcat <code>server.xml</code>
     * configuration file
     */
    @Override
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

        contextTokenValue.append(" reloadable=\"");
        contextTokenValue.append(getPropertyValue(TomcatPropertySet.CONTEXT_RELOADABLE));
        contextTokenValue.append("\"");

        contextTokenValue.append(">");
        contextTokenValue.append("</Context>");
        return contextTokenValue.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setupConfFiles(String confDir)
    {
        Map<String, String> replacements = getCatalinaPropertertiesReplacements();
        getFileHandler().replaceInFile(getFileHandler().append(confDir, "catalina.properties"),
            replacements, "UTF-8");

        replacements.clear();
        replacements.put("</Host>", this.createTomcatWebappsToken()
            + "\n        <Valve className=\"org.apache.catalina.valves.AccessLogValve\" "
            + "\n               directory=\"logs\" prefix=\""
                + getPropertyValue(GeneralPropertySet.HOSTNAME) + "_access_log.\" "
            + "\n               suffix=\".txt\""
            + "\n               pattern=\"%h %l %u %t &quot;%r&quot; %s %b\""
            + "\n               resolveHosts=\"false\"/>"
            + "\n      </Host>");
        getFileHandler().replaceInFile(getFileHandler().append(confDir, "server.xml"),
            replacements, "UTF-8");
    }

    /**
     * @return The replacements for <code>catalina.properties</code>.
     */
    protected Map<String, String> getCatalinaPropertertiesReplacements()
    {
        Map<String, String> replacements = new HashMap<String, String>();
        replacements.put("common.loader=",
            "common.loader=${catalina.base}/common/classes,${catalina.base}/common/lib/*.jar,");
        replacements.put("server.loader=",
            "server.loader=${catalina.base}/server/classes,${catalina.base}/server/lib/*.jar,");
        return replacements;
    }

    /**
     * Adds the XML replacements needed to configure the server.
     */
    private void addXmlReplacements()
    {
        addXmlReplacement("conf/server.xml",
            "//Server", "port",
                GeneralPropertySet.RMI_PORT);
        addXmlReplacement("conf/server.xml",
            CONNECTOR_XPATH,
                    "port", ServletPropertySet.PORT);
        addXmlReplacement("conf/server.xml",
            CONNECTOR_XPATH,
                    "scheme", GeneralPropertySet.PROTOCOL);
        addXmlReplacement("conf/server.xml",
            CONNECTOR_XPATH,
                    "secure", TomcatPropertySet.HTTP_SECURE);
        addXmlReplacement("conf/server.xml",
            CONNECTOR_XPATH,
                    "emptySessionPath", TomcatPropertySet.CONNECTOR_EMPTY_SESSION_PATH);
        addXmlReplacement("conf/server.xml",
            CONNECTOR_XPATH,
                    "URIEncoding", GeneralPropertySet.URI_ENCODING);
        addXmlReplacement("conf/server.xml",
            CONNECTOR_XPATH,
                    "port", ServletPropertySet.PORT);
        addXmlReplacement("conf/server.xml",
            "//Server/Service/Connector[@protocol='AJP/1.3']",
                "port", TomcatPropertySet.AJP_PORT);
        addXmlReplacement("conf/server.xml",
            "//Server/Service/Engine",
                "defaultHost", GeneralPropertySet.HOSTNAME);
        addXmlReplacement("conf/server.xml",
            "//Server/Service/Engine/Host",
                "name", GeneralPropertySet.HOSTNAME);
        addXmlReplacement("conf/server.xml",
            "//Server/Service/Engine/Host",
                "appBase", TomcatPropertySet.WEBAPPS_DIRECTORY);
    }

    /**
     * Adds optional XML replacements that can only be determined with the
     * actual configuration being used to perform the replacements.
     * 
     * @param container
     *            the container, with configuration properties, used to
     *            determine if the optional replacements are needed
     */
    private void addOptionalXmlReplacements(LocalContainer container)
    {
        if (container.getConfiguration().getPropertyValue(
            TomcatPropertySet.USE_HTTP_ONLY) != null)
        {
            addXmlReplacement("conf/context.xml", "//Context", "useHttpOnly",
                TomcatPropertySet.USE_HTTP_ONLY);
        }

        if (container.getConfiguration().getPropertyValue(
                TomcatPropertySet.CONNECTOR_KEY_STORE_FILE) != null)
        {
            addXmlReplacement("conf/server.xml", CONNECTOR_XPATH, "keystoreFile",
                    TomcatPropertySet.CONNECTOR_KEY_STORE_FILE);
        }

        if (container.getConfiguration().getPropertyValue(
                TomcatPropertySet.CONNECTOR_KEY_STORE_PASSWORD) != null)
        {
            addXmlReplacement("conf/server.xml", CONNECTOR_XPATH, "keystorePass",
                    TomcatPropertySet.CONNECTOR_KEY_STORE_PASSWORD);
        }

        if (container.getConfiguration().getPropertyValue(
                TomcatPropertySet.CONNECTOR_KEY_STORE_TYPE) != null)
        {
            addXmlReplacement("conf/server.xml", CONNECTOR_XPATH, "keystoreType",
                    TomcatPropertySet.CONNECTOR_KEY_STORE_TYPE);
        }

        if (container.getConfiguration().getPropertyValue(
                TomcatPropertySet.CONNECTOR_KEY_ALIAS) != null)
        {
            addXmlReplacement("conf/server.xml", CONNECTOR_XPATH, "keyAlias",
                    TomcatPropertySet.CONNECTOR_KEY_ALIAS);
        }

        if (container.getConfiguration().getPropertyValue(
                TomcatPropertySet.CONNECTOR_CLIENT_AUTH) != null)
        {
            addXmlReplacement("conf/server.xml", CONNECTOR_XPATH, "clientAuth",
                    TomcatPropertySet.CONNECTOR_CLIENT_AUTH);
        }

        if (container.getConfiguration().getPropertyValue(
                TomcatPropertySet.CONNECTOR_TRUST_STORE_FILE) != null)
        {
            addXmlReplacement("conf/server.xml", CONNECTOR_XPATH, "truststoreFile",
                    TomcatPropertySet.CONNECTOR_TRUST_STORE_FILE);
        }

        if (container.getConfiguration().getPropertyValue(
                TomcatPropertySet.CONNECTOR_TRUST_STORE_PASSWORD) != null)
        {
            addXmlReplacement("conf/server.xml", CONNECTOR_XPATH, "truststorePass",
                    TomcatPropertySet.CONNECTOR_TRUST_STORE_PASSWORD);
        }

        if (container.getConfiguration().getPropertyValue(
                TomcatPropertySet.CONNECTOR_TRUST_STORE_TYPE) != null)
        {
            addXmlReplacement("conf/server.xml", CONNECTOR_XPATH, "truststoreType",
                    TomcatPropertySet.CONNECTOR_TRUST_STORE_TYPE);
        }

        if (container.getConfiguration().getPropertyValue(
                TomcatPropertySet.CONNECTOR_SSL_PROTOCOL) != null)
        {
            addXmlReplacement("conf/server.xml", CONNECTOR_XPATH, "sslProtocol",
                    TomcatPropertySet.CONNECTOR_SSL_PROTOCOL);
        }
    }
}
