/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
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
package org.codehaus.cargo.container.jboss;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.configuration.builder.ConfigurationEntryType;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.jboss.internal.JBoss75xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.util.CargoException;
import org.codehaus.cargo.util.DefaultXmlFileBuilder;
import org.codehaus.cargo.util.XmlFileBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * JBoss 7.5.x (EAP 6.4.x) standalone local configuration.
 */
public class JBoss75xStandaloneLocalConfiguration extends JBoss73xStandaloneLocalConfiguration
{

    /**
     * JBoss 7.5.x (EAP 6.4.x) container capability.
     */
    private static final ConfigurationCapability CAPABILITY =
        new JBoss75xStandaloneLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see JBoss73xStandaloneLocalConfiguration#JBoss73xStandaloneLocalConfiguration(String)
     */
    public JBoss75xStandaloneLocalConfiguration(String dir)
    {
        super(dir);
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
    protected void doConfigure(LocalContainer c) throws Exception
    {
        super.doConfigure(c);

        // Configure resources
        addResources((JBoss7xInstalledLocalContainer) c);
    }

    /**
     * Add resources to JBoss domain
     * @param container JBoss container
     */
    protected void addResources(JBoss7xInstalledLocalContainer container)
    {
        String version = container.getVersion("7.5.7.Final");
        getLogger().debug("Parsing micro version for version: " + version,
            this.getClass().getName());
        String microVersion =
            version.substring(version.indexOf('.', 2) + 1, version.indexOf('.', 4));
        int microVersionValue = Integer.parseInt(microVersion);

        String configurationXmlFile = "configuration/"
            + getPropertyValue(JBossPropertySet.CONFIGURATION) + ".xml";

        // Create resources - so far just Email resource
        Map<String, String> ns = new HashMap<String, String>();
        ns.put("mail", "urn:jboss:domain:mail:1.2");
        if (microVersionValue >= 7)
        {
            ns.put("domain", "urn:jboss:domain:1.8");
        }
        else
        {
            ns.put("domain", "urn:jboss:domain:1.7");
        }

        String configurationXmlFilePath = getFileHandler().append(getHome(), configurationXmlFile);

        for (Resource resource : getResources())
        {
            if (ConfigurationEntryType.MAIL_SESSION.equals(resource.getType()))
            {
                String host = resource.getParameter("mail.smtp.host") != null
                        ? resource.getParameter("mail.smtp.host") : "localhost";
                String port = resource.getParameter("mail.smtp.port") != null
                        ? resource.getParameter("mail.smtp.port") : "25";

                XmlFileBuilder manager = new DefaultXmlFileBuilder(getFileHandler(), true);
                manager.setFile(configurationXmlFilePath);
                Document document = manager.loadFile();
                manager.setNamespaces(ns);

                String jndiName = resource.getName();
                if (!jndiName.startsWith("java:/"))
                {
                    jndiName = "java:/" + jndiName;
                    getLogger().warn("JBoss 7 requires resource JNDI names to start with "
                        + "java:/, hence changing the given JNDI name to: " + jndiName,
                        this.getClass().getName());
                }

                Element mailSession = document.createElement("mail-session");
                mailSession.setAttribute("jndi-name", jndiName);
                mailSession.setAttribute("name", resource.getId());
                if (resource.getParameter("mail.smtp.from") != null)
                {
                    mailSession.setAttribute("from", resource.getParameter("mail.smtp.from"));
                }
                Element smtpServer = mailSession.getOwnerDocument().createElement("smtp-server");
                mailSession.appendChild(smtpServer);
                smtpServer.setAttribute("outbound-socket-binding-ref", resource.getId());

                manager.insertElementUnderXPath(mailSession, "//domain:profile/mail:subsystem");

                Element socketBinding = document.createElement("outbound-socket-binding");
                socketBinding.setAttribute("name", resource.getId());
                Element remoteDestination =
                    socketBinding.getOwnerDocument().createElement("remote-destination");
                socketBinding.appendChild(remoteDestination);
                remoteDestination.setAttribute("host", host);
                remoteDestination.setAttribute("port", port);

                manager.insertElementUnderXPath(socketBinding, "//domain:socket-binding-group");

                manager.writeFile();
            }
            else
            {
                throw new CargoException("Resource type " + resource.getType()
                    + " isn't supported.");
            }
        }
    }
}
