/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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
package org.codehaus.cargo.container.tomee;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.tomcat.Tomcat10xStandaloneLocalConfiguration;
import org.codehaus.cargo.container.tomcat.TomcatCopyingInstalledLocalDeployer;
import org.codehaus.cargo.container.tomee.internal.Tomee9x10xStandaloneLocalConfigurationCapability;

/**
 * Standalone local configuration for TomEE 9.x.
 */
public class Tomee9xStandaloneLocalConfiguration extends Tomcat10xStandaloneLocalConfiguration
{

    /**
     * {@inheritDoc}
     */
    private static final ConfigurationCapability CAPABILITY =
        new Tomee9x10xStandaloneLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see Tomcat10xStandaloneLocalConfiguration#Tomcat10xStandaloneLocalConfiguration(String)
     */
    public Tomee9xStandaloneLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(TomeePropertySet.APPS_DIRECTORY, "apps");
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
    public String toString()
    {
        return "TomEE 9.x Standalone Configuration";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doConfigure(LocalContainer container) throws Exception
    {
        super.doConfigure(container);

        String apps = getPropertyValue(TomeePropertySet.APPS_DIRECTORY);
        String tomeeXml = getFileHandler().append(getHome(), "conf/tomee.xml");
        Map<String, String> replacements = new HashMap<String, String>();
        replacements.put(
            "<!-- activate next line to be able to deploy applications in apps -->",
            "<!-- activate deployment applications in " +  apps + " directory -->");
        replacements.put(
            "<!-- <Deployments dir=\"apps\" /> -->",
            "<Deployments dir=\"" +  apps + "\" />");
        StringBuilder resourceReplacements = new StringBuilder();
        for (Resource resource : getResources())
        {
            if (resource.getType().startsWith("jakarta.jms.")
                || resource.getType().startsWith("javax.jms."))
            {
                if (resourceReplacements.length() == 0)
                {
                    resourceReplacements.append(
                        "\n  <!-- JMS resources deployed by Codehaus Cargo -->\n");
                }
                resourceReplacements.append("  <Resource id=\"");
                resourceReplacements.append(resource.getName());
                resourceReplacements.append("\" type=\"");
                resourceReplacements.append(resource.getType().replace("javax.", "jakarta."));
                resourceReplacements.append("\"/>\n");
            }
        }
        if (resourceReplacements.length() > 0)
        {
            resourceReplacements.append("</tomee>");
            replacements.put("</tomee>", resourceReplacements.toString());
        }
        getFileHandler().replaceInFile(tomeeXml, replacements, StandardCharsets.UTF_8);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected TomcatCopyingInstalledLocalDeployer createDeployer(LocalContainer container)
    {
        return new TomeeCopyingInstalledLocalDeployer((InstalledLocalContainer) container);
    }

}
