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
package org.codehaus.cargo.container.jetty;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.jetty.internal.AbstractJettyStandaloneLocalConfiguration;
import org.codehaus.cargo.container.jetty.internal.JettyStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.spi.deployer.AbstractCopyingInstalledLocalDeployer;

/**
 * Jetty 6.x standalone
 * {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration} implementation.
 */
public class Jetty6xStandaloneLocalConfiguration extends
    AbstractJettyStandaloneLocalConfiguration
{
    /**
     * Jetty's one and only <code>webdefault.xml</code> file.
     */
    private static final String[] WEBDEFAULT_XML_FILE = new String[] {"webdefault.xml"};

    /**
     * Capability of the Jetty 6.x standalone local configuration.
     */
    private static final ConfigurationCapability CAPABILITY =
        new JettyStandaloneLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see AbstractJettyStandaloneLocalConfiguration#AbstractJettyStandaloneLocalConfiguration(String)
     */
    public Jetty6xStandaloneLocalConfiguration(String dir)
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
    public void doConfigure(LocalContainer container) throws Exception
    {
        InstalledLocalContainer installedLocalContainer = (InstalledLocalContainer) container;

        // Only add the XML replacement now to allow customized getWebdefaultFiles()
        for (String webDefaultXmlFile : getWebdefaultFiles(installedLocalContainer))
        {
            addUseFileMappedBufferXmlReplacement(webDefaultXmlFile);
        }

        super.doConfigure(container);

        String sessionPath = getPropertyValue(JettyPropertySet.SESSION_PATH);
        String sessionContextParam = "";
        if (sessionPath != null)
        {
            sessionContextParam =
                "  <context-param>\n"
                    + "    <param-name>org.mortbay.jetty.servlet.SessionPath</param-name>\n"
                    + "    <param-value>" + sessionPath + "</param-value>\n"
                    + "  </context-param>\n";
            Map<String, String> replacements = new HashMap<String, String>(1);
            replacements.put("</web-app>", sessionContextParam + "</web-app>");
            for (String webDefaultXmlFile : getWebdefaultFiles(installedLocalContainer))
            {
                String webdefault = getFileHandler().append(getHome(), "etc/" + webDefaultXmlFile);
                getFileHandler().replaceInFile(
                    webdefault, replacements, StandardCharsets.UTF_8, false);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AbstractCopyingInstalledLocalDeployer createDeployer(
        InstalledLocalContainer container)
    {
        Jetty6x7x8xInstalledLocalDeployer deployer =
            new Jetty6x7x8xInstalledLocalDeployer(container);
        return deployer;
    }

    /**
     * Get the Jetty <code>webdefault.xml</code> file name(s) for the given local container.
     * @param container Local container.
     * @return Jetty <code>webdefault.xml</code> file name.
     */
    protected String[] getWebdefaultFiles(InstalledLocalContainer container)
    {
        return Jetty6xStandaloneLocalConfiguration.WEBDEFAULT_XML_FILE;
    }

    /**
     * Add the {@link JettyPropertySet#USE_FILE_MAPPED_BUFFER} property XML replacement for the
     * given <code>webdefault.xml</code> file name.
     * @param webDefaultXmlFile <code>webdefault.xml</code> file to add the XML replacement to.
     */
    protected void addUseFileMappedBufferXmlReplacement(String webDefaultXmlFile)
    {
        addXmlReplacement(
            "etc/" + webDefaultXmlFile,
            "//servlet/init-param/param-name[text()='useFileMappedBuffer']"
                + "/parent::init-param/param-value",
            null, JettyPropertySet.USE_FILE_MAPPED_BUFFER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "Jetty 6.x Standalone Configuration";
    }

}
