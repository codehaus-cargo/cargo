/* 
 * ========================================================================
 * 
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration;

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
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.configuration.Configuration#getCapability()
     */
    public abstract ConfigurationCapability getCapability();

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

            getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war",
                new File(getHome(), "cargocpc.war"));

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
