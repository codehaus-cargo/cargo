/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.cargo.container.EmbeddedLocalContainer;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.configuration.builder.ConfigurationBuilder;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.LoggingLevel;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.tomcat.internal.AbstractCatalinaStandaloneLocalConfiguration;
import org.codehaus.cargo.container.tomcat.internal.Tomcat5x6x7xConfigurationBuilder;
import org.codehaus.cargo.container.tomcat.internal.Tomcat5xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.util.XmlReplacement;
import org.w3c.dom.Element;

/**
 * StandAloneLocalConfiguration that is appropriate for Tomcat 5.x containers.
 * <p>
 * This code needs to work with both {@link Tomcat5xInstalledLocalContainer} and
 * {@link Tomcat5xEmbeddedLocalContainer}.
 */
public class Tomcat5xStandaloneLocalConfiguration extends
    AbstractCatalinaStandaloneLocalConfiguration
{

    /**
     * {@inheritDoc}
     */
    private static final ConfigurationCapability CAPABILITY =
        new Tomcat5xStandaloneLocalConfigurationCapability();

    /**
     * used to insert DataSources and Resources into the configuration file.
     */
    protected Tomcat5x6x7xConfigurationBuilder configurationBuilder;

    /**
     * {@inheritDoc}
     * @see AbstractCatalinaStandaloneLocalConfiguration#AbstractCatalinaStandaloneLocalConfiguration(String)
     */
    public Tomcat5xStandaloneLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(TomcatPropertySet.CONNECTOR_EMPTY_SESSION_PATH, "true");
        configurationBuilder = new Tomcat5x6x7xConfigurationBuilder();

        addXmlReplacements();
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
    protected ConfigurationBuilder createConfigurationBuilder(LocalContainer container)
    {
        return configurationBuilder;
    }

    /**
     * {@inheritDoc} this does not deploy the manager, if the application is embedded.
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
        addXmlReplacement("conf/server.xml", connectorXpath(), "port", ServletPropertySet.PORT);
        addXmlReplacement("conf/server.xml", connectorXpath(), "emptySessionPath",
            TomcatPropertySet.CONNECTOR_EMPTY_SESSION_PATH);
        addXmlReplacement("conf/server.xml", connectorXpath(), "URIEncoding",
            TomcatPropertySet.URI_ENCODING);
        addXmlReplacement("conf/server.xml", connectorXpath(), "port", ServletPropertySet.PORT);

        if (container instanceof EmbeddedLocalContainer)
        {
            // when running in the embedded mode, there's no need to replace files.
        }
        else
        {
            addOptionalXmlReplacements(container);

            super.performXmlReplacements(container);
        }
    }

    /**
     * {@inheritDoc}
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
        contextTokenValue.append(getExtraContextAttributes());
        contextTokenValue.append(">");

        contextTokenValue.append(getExtraClasspathToken(deployable));

        contextTokenValue.append("</Context>");
        return contextTokenValue.toString();
    }

    /**
     * Allows adding attributes during the creation of &lt;Context/&gt; element for Tomcat &gt;
     * 5.x.
     * @return the extra "context" XML attributes to insert in the Tomcat <code>server.xml</code>
     * configuration file.
     */
    protected String getExtraContextAttributes()
    {
        return "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setupConfFiles(String confDir)
    {
        Map<String, String> replacements = getCatalinaPropertertiesReplacements();
        getFileHandler().replaceInFile(getFileHandler().append(confDir, "catalina.properties"),
            replacements, StandardCharsets.UTF_8);

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
            replacements, StandardCharsets.UTF_8);
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
     * Creates the extra classpath XML token.
     * @param deployable Deployable to create extra classpath XML token for.
     * @return Extra classpath XML token.
     */
    protected String getExtraClasspathToken(WAR deployable)
    {
        getLogger().warn("Tomcat 5.x doesn't support extra classpath on WARs",
            this.getClass().getName());
        return "";
    }

    /**
     * Configures the specified context element with the extra classpath (if any) of the given WAR.
     * @param deployable Deployable to create extra classpath XML token for.
     * @param context The context element to configure, must not be {@code null}.
     */
    protected void configureExtraClasspathToken(WAR deployable, Element context)
    {
        getLogger().warn("Tomcat 5.x doesn't support extra classpath on WARs",
            this.getClass().getName());
    }

    /**
     * Adds the XML replacements needed to configure the server.
     */
    private void addXmlReplacements()
    {
        addXmlReplacement("conf/server.xml", "//Server", "port", GeneralPropertySet.RMI_PORT);
        addXmlReplacement("conf/server.xml",
            "//Server/Service/Connector[@protocol='AJP/1.3']", "port", TomcatPropertySet.AJP_PORT,
            XmlReplacement.ReplacementBehavior.IGNORE_IF_NON_EXISTING);
        addXmlReplacement("conf/server.xml", "//Server/Service/Engine", "defaultHost",
            GeneralPropertySet.HOSTNAME);
        addXmlReplacement("conf/server.xml", "//Server/Service/Engine/Host", "name",
            GeneralPropertySet.HOSTNAME);
        addXmlReplacement("conf/server.xml", "//Server/Service/Engine/Host", "appBase",
            TomcatPropertySet.WEBAPPS_DIRECTORY);
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
        if (!"localhost".equals(container.getConfiguration().getPropertyValue(
            GeneralPropertySet.HOSTNAME)))
        {
            addXmlReplacement("conf/server.xml", connectorXpath(), "address",
                GeneralPropertySet.HOSTNAME);
            addXmlReplacement("conf/server.xml", "//Server/Service/Connector[@protocol='AJP/1.3']",
                "address", GeneralPropertySet.HOSTNAME,
                    XmlReplacement.ReplacementBehavior.IGNORE_IF_NON_EXISTING);
        }

        if (container.getConfiguration().getPropertyValue(
            TomcatPropertySet.USE_HTTP_ONLY) != null)
        {
            addXmlReplacement("conf/context.xml", "//Context", "useHttpOnly",
                TomcatPropertySet.USE_HTTP_ONLY);
        }

        if (container.getConfiguration().getPropertyValue(
            TomcatPropertySet.CONNECTOR_MAX_HTTP_HEADER_SIZE) != null)
        {
            addXmlReplacement("conf/server.xml", connectorXpath(), "maxHttpHeaderSize",
                TomcatPropertySet.CONNECTOR_MAX_HTTP_HEADER_SIZE);
        }
    }
}
