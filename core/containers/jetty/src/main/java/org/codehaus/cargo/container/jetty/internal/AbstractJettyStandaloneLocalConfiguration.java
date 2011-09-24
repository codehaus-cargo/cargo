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
package org.codehaus.cargo.container.jetty.internal;

import java.io.File;

import org.apache.tools.ant.types.FilterChain;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.jetty.JettyPropertySet;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration;
import org.codehaus.cargo.container.spi.deployer.AbstractCopyingInstalledLocalDeployer;

/**
 * Base class for Jetty standalone configurations.
 * 
 * @version $Id$
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
        setProperty(JettyPropertySet.CREATE_CONTEXT_XML, "true");
    }

    /**
     * Creates the filter chain that should be applied while copying container configuration files
     * to the working directory from which the container is started.
     * 
     * @return The filter chain, never {@code null}.
     */
    protected FilterChain createJettyFilterChain()
    {
        FilterChain filterChain = createFilterChain();
        return filterChain;
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
     * 
     * @see org.codehaus.cargo.container.spi.configuration.AbstractLocalConfiguration#configure(LocalContainer)
     */
    @Override
    protected void doConfigure(LocalContainer container) throws Exception
    {
        setupConfigurationDir();

        FilterChain filterChain = createJettyFilterChain();

        String etcDir = getFileHandler().createDirectory(getHome(), "etc");
        getResourceUtils().copyResource(RESOURCE_PATH + container.getId() + "/jetty.xml",
            new File(etcDir, "jetty.xml"));
        getResourceUtils().copyResource(RESOURCE_PATH + container.getId() + "/webdefault.xml",
            new File(etcDir, "webdefault.xml"), filterChain, "UTF-8");

        // Create a webapps directory for automatic deployment of WARs dropped inside.
        String appDir = getFileHandler().createDirectory(getHome(), "webapps");

        // Create log directory
        getFileHandler().createDirectory(getHome(), "logs");

        // Create contexts directory for hot deployments
        getFileHandler().createDirectory(getHome(), "contexts");

        // Deploy all deployables into the webapps directory.
        AbstractCopyingInstalledLocalDeployer deployer =
            createDeployer((InstalledLocalContainer) container);
        deployer.deploy(getDeployables());

        // Deploy the CPC (Cargo Ping Component) to the webapps directory
        getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war",
            new File(appDir, "cargocpc.war"));
    }

}
