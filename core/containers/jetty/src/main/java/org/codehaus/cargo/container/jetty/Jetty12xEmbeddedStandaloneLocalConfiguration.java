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

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.jetty.internal.Jetty12xEmbeddedStandaloneLocalConfigurationCapability;

/**
 * A mostly canned configuration for an embedded Jetty 12.x instance.
 */
public class Jetty12xEmbeddedStandaloneLocalConfiguration extends
    Jetty11xEmbeddedStandaloneLocalConfiguration
{
    /**
     * capabilities supported by this config.
     */
    private static final ConfigurationCapability CAPABILITY =
        new Jetty12xEmbeddedStandaloneLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see Jetty11xEmbeddedStandaloneLocalConfiguration#Jetty11xEmbeddedStandaloneLocalConfiguration(String)
     */
    public Jetty12xEmbeddedStandaloneLocalConfiguration(String dir)
    {
        super(dir);
        setProperty(JettyPropertySet.DEPLOYER_EE_VERSION,
            Jetty12xInstalledLocalContainer.DEFAULT_DEPLOYER_EE_VERSION);
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
        if ("ee11".equals(getPropertyValue(JettyPropertySet.DEPLOYER_EE_VERSION)))
        {
            removeXmlReplacement(
               "etc/webdefault.xml",
                "//servlet/init-param/param-name[text()='useFileMappedBuffer']"
                    + "/parent::init-param/param-value");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getWebdefaultXmlPath()
    {
        return "org/eclipse/jetty/" + getPropertyValue(JettyPropertySet.DEPLOYER_EE_VERSION)
            + "/webapp/webdefault-" + getPropertyValue(JettyPropertySet.DEPLOYER_EE_VERSION)
                + ".xml";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void activateLogging(LocalContainer container)
    {
        getLogger().info("Jetty 12.x log configuration not implemented", this.getClass().getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "Jetty 12.x Embedded Standalone Configuration";
    }
}
