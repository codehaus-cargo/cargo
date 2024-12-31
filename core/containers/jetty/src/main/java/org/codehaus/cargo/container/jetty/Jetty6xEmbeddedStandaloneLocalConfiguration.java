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

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.jetty.internal.AbstractJettyEmbeddedStandaloneLocalConfiguration;
import org.codehaus.cargo.container.jetty.internal.Jetty6xEmbeddedStandaloneLocalConfigurationCapability;

/**
 * A mostly canned configuration for an embedded Jetty 6.x instance.
 */
public class Jetty6xEmbeddedStandaloneLocalConfiguration extends
    AbstractJettyEmbeddedStandaloneLocalConfiguration
{
    /**
     * capabilities supported by this config.
     */
    private static final ConfigurationCapability CAPABILITY =
        new Jetty6xEmbeddedStandaloneLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see AbstractJettyEmbeddedStandaloneLocalConfiguration#AbstractJettyEmbeddedStandaloneLocalConfiguration(String)
     */
    public Jetty6xEmbeddedStandaloneLocalConfiguration(String dir)
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
        return CAPABILITY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doConfigure(LocalContainer container) throws Exception
    {
        super.doConfigure(container);

        String sessionPath = getPropertyValue(JettyPropertySet.SESSION_PATH);
        if (sessionPath != null)
        {
            String sessionContextParam =
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
    protected void activateLogging(LocalContainer container)
    {
        getLogger().info("Jetty 6.x log configuration not implemented", this.getClass().getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "Jetty 6.x Embedded Standalone Configuration";
    }
}
