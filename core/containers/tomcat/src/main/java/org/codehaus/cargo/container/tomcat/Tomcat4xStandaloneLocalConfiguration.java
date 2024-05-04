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

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.configuration.builder.ConfigurationBuilder;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.tomcat.internal.AbstractCatalinaStandaloneLocalConfiguration;
import org.codehaus.cargo.container.tomcat.internal.Tomcat4xConfigurationBuilder;
import org.codehaus.cargo.container.tomcat.internal.Tomcat4xStandaloneLocalConfigurationCapability;

/**
 * StandAloneLocalConfiguration that is appropriate for Tomcat 4.x containers.
 */
public class Tomcat4xStandaloneLocalConfiguration extends
    AbstractCatalinaStandaloneLocalConfiguration
{
    /**
     * {@inheritDoc}
     */
    private static final ConfigurationCapability CAPABILITY =
        new Tomcat4xStandaloneLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see AbstractCatalinaStandaloneLocalConfiguration#AbstractCatalinaStandaloneLocalConfiguration(String)
     */
    public Tomcat4xStandaloneLocalConfiguration(String dir)
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
    protected ConfigurationBuilder createConfigurationBuilder(LocalContainer container)
    {
        return new Tomcat4xConfigurationBuilder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setupManager(LocalContainer container)
    {
        String from = ((InstalledLocalContainer) container).getHome();
        String to = getHome();
        getFileHandler().copyDirectory(from + "/server/webapps/manager",
            to + "/server/webapps/manager");
        getFileHandler().copyFile(from + "/webapps/manager.xml", to + "/webapps/manager.xml");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getXpathForResourcesParent()
    {
        return "//Engine/DefaultContext";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "Tomcat 4.x Standalone Configuration";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getOrCreateResourceConfigurationFile(Resource rs, LocalContainer container)
    {
        String confDir = getFileHandler().createDirectory(getHome(), "conf");
        return getFileHandler().append(confDir, "server.xml");
    }

    /**
     * Tomcat 4.x does not load <code>$CATALINA_BASE/common/lib</code> (as explained in
     * http://tomcat.apache.org/tomcat-4.1-doc/class-loader-howto.html), therefore we copy the
     * extra classpath into <code>$CATALINA_HOME/common/lib</code>. {@inheritDoc}
     */
    @Override
    protected void doConfigure(LocalContainer container) throws Exception
    {
        if (container instanceof InstalledLocalContainer)
        {
            InstalledLocalContainer installedContainer = (InstalledLocalContainer) container;
            String[] classPath = installedContainer.getExtraClasspath();
            if (classPath != null)
            {
                String commonLib = getFileHandler().append(installedContainer.getHome(),
                    "common/lib");

                for (String path : classPath)
                {
                    String target = getFileHandler().append(commonLib,
                        getFileHandler().getName(path));

                    getLogger().debug("Copying extra classpath JAR to " + target,
                        this.getClass().getName());

                    getFileHandler().copyFile(path, target);
                }
            }
        }

        super.doConfigure(container);

        String serverXml = getFileHandler().append(getHome() + "/conf", "server.xml");
        Map<String, String> replacements = getReplacements();
        replacements.put("catalina.logging.level",
            getTomcatLoggingLevel(getPropertyValue(GeneralPropertySet.LOGGING)));
        replacements.put("tomcat.webapps", createTomcatWebappsToken());
        getResourceUtils().copyResource(RESOURCE_PATH + container.getId() + "/server.xml",
            serverXml, getFileHandler(), replacements, StandardCharsets.UTF_8);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setupConfFiles(String confDir)
    {
        // Nothing to do
    }

}
