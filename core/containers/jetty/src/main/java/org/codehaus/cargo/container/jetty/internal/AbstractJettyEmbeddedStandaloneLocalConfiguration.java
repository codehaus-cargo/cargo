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
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.jetty.JettyPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration;

/**
 * Base class for Jetty standalone configurations.
 * 
 * @version $Id$
 */
public abstract class AbstractJettyEmbeddedStandaloneLocalConfiguration extends
    AbstractStandaloneLocalConfiguration
{
    /**
     * {@inheritDoc}
     * @see AbstractStandaloneLocalConfiguration#AbstractStandaloneLocalConfiguration(String)
     */
    public AbstractJettyEmbeddedStandaloneLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(JettyPropertySet.USE_FILE_MAPPED_BUFFER, "true");
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.configuration.Configuration#getCapability()
     */
    public abstract ConfigurationCapability getCapability();

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
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.configuration.LocalConfiguration#configure(LocalContainer)
     */
    @Override
    public void doConfigure(LocalContainer container) throws Exception
    {
        try
        {
            setupConfigurationDir();

            FilterChain filterChain = createJettyFilterChain();

            String etcDir = getFileHandler().createDirectory(getHome(), "etc");
            getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war",
                new File(getHome(), "cargocpc.war"));
            getResourceUtils().copyResource(RESOURCE_PATH + container.getId() + "/webdefault.xml",
                new File(etcDir, "webdefault.xml"), filterChain);

            if (container.getOutput() != null)
            {
                activateLogging(container);
            }
        }
        catch (Exception e)
        {
            throw new ContainerException("Failed to create a " + container.getName()
                + " container configuration", e);
        }
    }

    /**
     * Turn on the logging for the container.
     * @param container the container for which to establish logging
     * @throws Exception on error
     */
    protected abstract void activateLogging(LocalContainer container)
        throws Exception;
}
