/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2023 Ali Tokmen.
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
     * Capability of the Jetty 6.x standalone local configuration.
     */
    private static ConfigurationCapability capability =
        new JettyStandaloneLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see AbstractJettyStandaloneLocalConfiguration#AbstractJettyStandaloneLocalConfiguration(String)
     */
    public Jetty6xStandaloneLocalConfiguration(String dir)
    {
        super(dir);

        addXmlReplacement(
            "etc/webdefault.xml",
            "//servlet/init-param/param-name[text()='useFileMappedBuffer']"
                + "/parent::init-param/param-value",
            null, JettyPropertySet.USE_FILE_MAPPED_BUFFER);
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
    public void doConfigure(LocalContainer container) throws Exception
    {
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
            String webdefault = getFileHandler().append(getHome(), "etc/webdefault.xml");
            getFileHandler().replaceInFile(
                webdefault, replacements, StandardCharsets.UTF_8, false);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AbstractCopyingInstalledLocalDeployer createDeployer(
        InstalledLocalContainer container)
    {
        Jetty6xInstalledLocalDeployer deployer = new Jetty6xInstalledLocalDeployer(container);
        return deployer;
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
