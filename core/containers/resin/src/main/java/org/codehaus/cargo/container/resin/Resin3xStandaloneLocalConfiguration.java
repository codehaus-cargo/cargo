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
package org.codehaus.cargo.container.resin;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.configuration.builder.ConfigurationBuilder;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.LoggingLevel;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.resin.internal.AbstractResinStandaloneLocalConfiguration;
import org.codehaus.cargo.container.resin.internal.Resin3xConfigurationBuilder;
import org.codehaus.cargo.container.resin.internal.Resin3xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.resin.internal.ResinRun;

/**
 * Resin 3.x standalone
 * {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration} implementation.
 * 
 * @version $Id$
 */
public class Resin3xStandaloneLocalConfiguration extends
    AbstractResinStandaloneLocalConfiguration
{
    /**
     * Where elements for resources will be inserted. This expression evaluates to: {@value
     * XML_PARENT_OF_RESOURCES}
     */
    public static final String XML_PARENT_OF_RESOURCES = "//resin:resin";

    /**
     * Capability of the Resin standalone configuration.
     * 
     * @see ResinStandaloneLocalConfigurationCapability
     */
    private static ConfigurationCapability capability =
        new Resin3xStandaloneLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * 
     * @see AbstractResinStandaloneLocalConfiguration#AbstractResinStandaloneLocalConfiguration(String)
     */
    public Resin3xStandaloneLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(ResinPropertySet.SOCKETWAIT_PORT,
            Integer.toString(ResinRun.DEFAULT_KEEPALIVE_SOCKET_PORT));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration#AbstractStandaloneLocalConfiguration(String)
     */
    public ConfigurationCapability getCapability()
    {
        return capability;
    }

    /**
     * {@inheritDoc}
     * 
     * @see Resin3xConfigurationBuilder
     */
    @Override
    protected ConfigurationBuilder createConfigurationBuilder(
        LocalContainer container)
    {
        return new Resin3xConfigurationBuilder();
    }

    /**
     * This expression evaluates to: {@value XML_PARENT_OF_RESOURCES} {@inheritDoc}
     */
    @Override
    protected String getXpathForResourcesParent()
    {
        return "//resin:resin";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, String> getNamespaces()
    {
        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("resin", "http://caucho.com/ns/resin");
        return namespaces;
    }

    /**
     * @param cargoLoggingLevel the cargo logging level (ie "low", "medium" or "high")
     * @return the Resin logging level corresponding to the cargo logging level
     */
    protected String getResinLoggingLevel(String cargoLoggingLevel)
    {
        String level;

        if (LoggingLevel.LOW.equalsLevel(cargoLoggingLevel))
        {
            level = "severe";
        }
        else if (LoggingLevel.MEDIUM.equalsLevel(cargoLoggingLevel))
        {
            level = "warning";
        }
        else
        {
            level = "config";
        }

        return level;
    }

    /**
     * {@inheritDoc}
     * 
     * @see AbstractResinStandaloneLocalConfiguration#prepareConfigurationDirectory(org.codehaus.cargo.container.Container, java.lang.String)
     */
    protected void prepareConfigurationDirectory(Container container, String confDir)
        throws IOException
    {
        String sourceConf = getFileHandler().append(
            ((InstalledLocalContainer) container).getHome(), "conf");

        getFileHandler().copyFile(getFileHandler().append(sourceConf, "app-default.xml"),
            getFileHandler().append(confDir, "app-default.xml"));
        getFileHandler().copyFile(getFileHandler().append(sourceConf, "resin.conf"),
            getFileHandler().append(confDir, "resin.conf"));

        Map<String, String> replacements = new HashMap<String, String>();
        replacements.put("<allow-servlet-el/>",
            "<allow-servlet-el/>\n"
            + "\n"
            + "<authenticator>\n"
                + "<type>com.caucho.server.security.XmlAuthenticator</type>\n"
                + "<init>\n"
                    + getSecurityToken("<user>", "</user>") + "\n"
                    + "<password-digest>none</password-digest>\n"
                + "</init>\n"
            + "</authenticator>");
        replacements.put("<host id=\"\" root-directory=\".\">",
            "<host id=\"\" root-directory=\".\">\n"
            + createExpandedWarTokenValue("document-directory"));
        getFileHandler().replaceInFile(getFileHandler().append(confDir, "resin.conf"),
                replacements, "UTF-8");

        addXmlReplacement("conf/resin.conf", "//resin/log[@name='']", "level",
            getResinLoggingLevel(getPropertyValue(GeneralPropertySet.LOGGING)));
        addXmlReplacement("conf/resin.conf", "//resin/server/http", "port",
            ServletPropertySet.PORT);
    }

}
