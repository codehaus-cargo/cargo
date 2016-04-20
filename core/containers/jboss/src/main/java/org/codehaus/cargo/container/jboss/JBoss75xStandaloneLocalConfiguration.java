/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol, 2012-2015 Ali Tokmen.
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
import org.codehaus.cargo.util.Dom4JXmlFileBuilder;
import org.codehaus.cargo.util.XmlFileBuilder;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 * JBoss 7.5.x standalone local configuration.
 *
 */
public class JBoss75xStandaloneLocalConfiguration extends JBoss73xStandaloneLocalConfiguration
{

    /**
     * JBoss container capability.
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
     * @see org.codehaus.cargo.container.configuration.Configuration#getCapability()
     */
    @Override
    public ConfigurationCapability getCapability()
    {
        return CAPABILITY;
    }

    /**
     * {@inheritDoc}
     * @see JBoss7xStandaloneLocalConfiguration#doConfigure(LocalContainer)
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
        String microVersion = version.
                substring(version.indexOf(".", 2) + 1, version.indexOf(".", 4));
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

                XmlFileBuilder manager = new Dom4JXmlFileBuilder(getFileHandler());
                manager.setFile(configurationXmlFilePath);
                manager.loadFile();
                manager.setNamespaces(ns);

                String jndiName = resource.getName();
                if (!jndiName.startsWith("java:/"))
                {
                    jndiName = "java:/" + jndiName;
                    getLogger().warn("JBoss 7 requires resource JNDI names to start with "
                        + "java:/, hence changing the given JNDI name to: " + jndiName,
                        this.getClass().getName());
                }

                Element mailSession = DocumentHelper.createElement("mail-session");
                mailSession.addAttribute("jndi-name", jndiName);
                mailSession.addAttribute("name", resource.getId());
                if (resource.getParameter("mail.smtp.from") != null)
                {
                    mailSession.addAttribute("from", resource.getParameter("mail.smtp.from"));
                }
                Element smtpServer = mailSession.addElement("smtp-server");
                smtpServer.addAttribute("outbound-socket-binding-ref", resource.getId());

                manager.insertElementUnderXPath(mailSession, "//domain:profile/mail:subsystem");

                Element socketBinding = DocumentHelper.createElement("outbound-socket-binding");
                socketBinding.addAttribute("name", resource.getId());
                Element remoteDestination = socketBinding.addElement("remote-destination");
                remoteDestination.addAttribute("host", host);
                remoteDestination.addAttribute("port", port);

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
