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

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.internal.util.PropertyUtils;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.tomcat.internal.Tomcat7xStandaloneLocalConfigurationCapability;

/**
 * Catalina standalone {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration}
 * implementation.
 */
public class Tomcat7xStandaloneLocalConfiguration extends Tomcat6xStandaloneLocalConfiguration
{

    /**
     * Context tag attribute for allowing multi part.
     */
    private static final String CONTEXT_ALLOWMULTIPART_ATTR_NAME = "allowCasualMultipartParsing";
    /**
     * Context tag attribute for allowing webjars.
     */
    private static final String CONTEXT_ALLOWWEBJARS_ATTR_NAME = "addWebinfClassesResources";

    /**
     * {@inheritDoc}
     */
    private static ConfigurationCapability capability =
        new Tomcat7xStandaloneLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see Tomcat6xStandaloneLocalConfiguration#Tomcat6xStandaloneLocalConfiguration(String)
     */
    public Tomcat7xStandaloneLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(ServletPropertySet.USERS, "admin::manager-script");
        setProperty(TomcatPropertySet.CONTEXT_ALLOW_MULTIPART, "true");
        setProperty(TomcatPropertySet.CONTEXT_ALLOW_WEB_JARS, "true");

        // CARGO-1271: Starting Tomcat 7 with Cargo logs warning on emptySessionPath
        getProperties().remove(TomcatPropertySet.CONNECTOR_EMPTY_SESSION_PATH);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doConfigure(LocalContainer container) throws Exception
    {
        super.doConfigure(container);

        // CARGO-1272: Starting Tomcat generates warnings on not existing folders in classloader
        getFileHandler().createDirectory(getHome(), "common/lib");
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
    protected void setupConfFiles(String confDir)
    {
        Map<String, String> replacements = getCatalinaPropertertiesReplacements();
        getFileHandler().replaceInFile(getFileHandler().append(confDir, "catalina.properties"),
            replacements, StandardCharsets.UTF_8);

        replacements.clear();
        replacements.put("</Host>", this.createTomcatWebappsToken()
            + "\n      </Host>");
        getFileHandler().replaceInFile(getFileHandler().append(confDir, "server.xml"),
            replacements, StandardCharsets.UTF_8);

        // Add custom Valves
        for (Map.Entry<String, String> property : getProperties().entrySet())
        {
            String propertyName = property.getKey();
            if (propertyName.startsWith(TomcatPropertySet.CUSTOM_VALVE))
            {
                StringBuilder replacement = new StringBuilder("  <Valve ");
                String customValve = property.getValue();
                Properties valveProps = PropertyUtils.splitPropertiesOnPipe(customValve);

                for (Entry<Object, Object> valveEntry : valveProps.entrySet())
                {
                    String key = valveEntry.getKey().toString();
                    String value = valveEntry.getValue().toString();

                    replacement.append(key);
                    replacement.append("=\"");
                    replacement.append(value);
                    replacement.append("\" ");
                }
                replacement.append("/>\n      </Host>");
                replacements.clear();
                replacements.put("</Host>", replacement.toString());

                getFileHandler().replaceInFile(getFileHandler().append(confDir, "server.xml"),
                    replacements, StandardCharsets.UTF_8);
            }
        }
    }

    @Override
    protected String getExtraContextAttributes()
    {
        return new StringBuilder(" ")
            .append(CONTEXT_ALLOWMULTIPART_ATTR_NAME).append("=\"")
            .append(getPropertyValue(TomcatPropertySet.CONTEXT_ALLOW_MULTIPART)).append("\" ")
            .append(CONTEXT_ALLOWWEBJARS_ATTR_NAME).append("=\"")
            .append(getPropertyValue(TomcatPropertySet.CONTEXT_ALLOW_WEB_JARS)).append("\"")
            .toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void performXmlReplacements(LocalContainer container)
    {
        String serverXmlFileName = "conf/server.xml";

        removeXmlReplacement(serverXmlFileName, connectorXpath(), "emptySessionPath");

        String startStopThreads = getPropertyValue(TomcatPropertySet.HOST_START_STOP_THREADS);
        if (startStopThreads != null)
        {
            addXmlReplacement(serverXmlFileName, "//Server/Service/Engine/Host",
                "startStopThreads", startStopThreads);
        }

        addXmlReplacement(serverXmlFileName,
            "//Server/Service/Engine/Host/Valve["
                + "@className='org.apache.catalina.valves.AccessLogValve']",
                    "prefix", getPropertyValue(GeneralPropertySet.HOSTNAME) + "_access_log.");

        super.performXmlReplacements(container);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "Tomcat 7.x Standalone Configuration";
    }
}
