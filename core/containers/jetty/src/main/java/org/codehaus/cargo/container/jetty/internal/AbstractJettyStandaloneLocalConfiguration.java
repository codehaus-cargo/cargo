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
package org.codehaus.cargo.container.jetty.internal;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.jetty.JettyPropertySet;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration;
import org.codehaus.cargo.container.spi.deployer.AbstractCopyingInstalledLocalDeployer;

/**
 * Base class for Jetty standalone configurations.
 */
public abstract class AbstractJettyStandaloneLocalConfiguration extends
    AbstractStandaloneLocalConfiguration
{

    /**
     * {@inheritDoc}
     * @see AbstractStandaloneLocalConfiguration#AbstractStandaloneLocalConfiguration(String)
     */
    public AbstractJettyStandaloneLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(GeneralPropertySet.RMI_PORT, "8079");
        setProperty(JettyPropertySet.USE_FILE_MAPPED_BUFFER, "true");
        setProperty(JettyPropertySet.DEPLOYER_CREATE_CONTEXT_XML, "true");
    }

    /**
     * Creates a new deployer for the specified container.
     * 
     * @param container The container for which to create the deployer, must not be {@code null}.
     * @return The new deployer, never {@code null}.
     */
    protected abstract AbstractCopyingInstalledLocalDeployer createDeployer(
        InstalledLocalContainer container);

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doConfigure(LocalContainer container) throws Exception
    {
        InstalledLocalContainer ilContainer = (InstalledLocalContainer) container;

        setupConfigurationDir();

        String etcDir = getFileHandler().createDirectory(getHome(), "etc");
        getFileHandler().copyDirectory(
            getFileHandler().append(ilContainer.getHome(), "etc"), etcDir);
        Map<String, String> replaceJettyHome = new HashMap<String, String>(1);
        replaceJettyHome.put("jetty.home", "config.home");
        for (String etcChild : getFileHandler().getChildren(etcDir))
        {
            if (!getFileHandler().isDirectory(etcChild))
            {
                getFileHandler().replaceInFile(
                    etcChild, replaceJettyHome, StandardCharsets.UTF_8, true);
            }
        }

        // Create a webapps directory for automatic deployment of WARs dropped inside.
        String appDir = getFileHandler().createDirectory(getHome(), "webapps");

        // Create log directory
        getFileHandler().createDirectory(getHome(), "logs");

        // Create contexts directory for hot deployments
        getFileHandler().createDirectory(getHome(), "contexts");

        // Deploy all deployables into the webapps directory.
        AbstractCopyingInstalledLocalDeployer deployer = createDeployer(ilContainer);
        deployer.deploy(getDeployables());

        // Deploy the CPC (Cargo Ping Component) to the webapps directory
        getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war",
            new File(appDir, "cargocpc.war"));
    }

}
